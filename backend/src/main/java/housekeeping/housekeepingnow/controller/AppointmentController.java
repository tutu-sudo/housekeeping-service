package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.CustomerDetail;
import housekeeping.housekeepingnow.service.AppointmentService;
import housekeeping.housekeepingnow.service.CustomerDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约接口控制器
 */
@Slf4j
@Tag(name = "预约接口", description = "预约创建、修改、查询等接口")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private CustomerDetailService customerDetailService;

    @Operation(summary = "根据预约ID查询详情")
    @GetMapping("/{appointmentId}")
    public Result<Appointment> getById(@PathVariable Long appointmentId) {
        return Result.success(appointmentService.getById(appointmentId));
    }

    @Operation(summary = "查询当前登录用户的预约列表", description = "根据当前登录用户的userId自动查询对应的预约列表，无需传递customerId参数")
    @RequireAuth
    @RequireRole({1}) // 仅顾客类型（user_type=1）可以查询自己的预约
    @GetMapping("/my-appointments")
    public Result<List<Appointment>> getMyAppointments(HttpServletRequest request) {
        try {
            // 从JWT token中获取当前登录用户的userId
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                log.error("查询预约列表失败：无法获取当前登录用户ID，请先登录");
                throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
            }

            Long userId;
            try {
                if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else {
                    userId = Long.valueOf(userIdObj.toString());
                }
            } catch (Exception e) {
                log.error("解析userId失败，userIdObj={}, 类型={}", userIdObj, userIdObj != null ? userIdObj.getClass().getName() : "null", e);
                throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID格式错误");
            }

            log.info("查询当前登录用户的预约列表，userId={}", userId);

            // 获取顾客详细信息
            CustomerDetail customerDetail;
            try {
                customerDetail = customerDetailService.getOrCreateByUserId(userId);
                log.info("获取到顾客信息，customerId={}, userId={}", customerDetail.getCustomerId(), userId);
            } catch (BusinessException e) {
                log.error("获取或创建顾客信息失败，userId={}, 错误信息={}", userId, e.getMessage(), e);
                throw e; // 重新抛出业务异常
            } catch (Exception e) {
                log.error("获取或创建顾客信息异常，userId={}", userId, e);
                throw new BusinessException(ResultCode.OPERATION_FAILED, "获取顾客信息失败：" + e.getMessage());
            }

            // 查询该顾客的预约列表
            List<Appointment> appointments;
            try {
                appointments = appointmentService.listByCustomerId(customerDetail.getCustomerId());
                log.info("查询预约列表成功，customerId={}, 预约数量={}", customerDetail.getCustomerId(), appointments != null ? appointments.size() : 0);
            } catch (Exception e) {
                log.error("查询预约列表失败，customerId={}", customerDetail.getCustomerId(), e);
                throw new BusinessException(ResultCode.OPERATION_FAILED, "查询预约列表失败：" + e.getMessage());
            }

            return Result.success(appointments);
        } catch (BusinessException e) {
            // 业务异常直接抛出，由全局异常处理器处理
            throw e;
        } catch (Exception e) {
            // 其他未预期的异常，记录日志并抛出业务异常
            log.error("查询我的预约列表时发生未预期的异常", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "查询预约列表失败，请稍后重试");
        }
    }

    @Operation(summary = "查询顾客的预约列表")
    @GetMapping("/customer/{customerId}")
    public Result<List<Appointment>> listByCustomer(@PathVariable Long customerId) {
        return Result.success(appointmentService.listByCustomerId(customerId));
    }

    @Operation(summary = "查询服务人员的预约列表")
    @GetMapping("/staff/{staffId}")
    public Result<List<Appointment>> listByStaff(@PathVariable Long staffId) {
        return Result.success(appointmentService.listByStaffId(staffId));
    }

    @Operation(summary = "创建预约", description = "创建新的预约记录，并校验时间冲突。支持跨天预约和多种结算方式（hourly-按小时，daily-按天，times-按次数）。系统会自动计算totalDuration、totalDays和totalAmount。注意：customer_id将从当前登录用户的userId自动获取，无需前端传递。")
    @RequireAuth
    @RequireRole({1}) // 仅顾客类型（user_type=1）可以创建预约
    @PostMapping
    public Result<Appointment> create(@RequestBody Appointment appointment, 
                                      HttpServletRequest request) {
        // 从JWT token中获取当前登录用户的userId（由JwtInterceptor设置到request中）
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("创建预约失败：无法获取当前登录用户ID，请先登录");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        Long userId;
        if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else {
            userId = Long.valueOf(userIdObj.toString());
        }

        log.info("创建预约，当前登录用户userId={}", userId);

        // 获取或创建顾客详细信息（确保customer_detail记录存在）
        CustomerDetail customerDetail;
        try {
            customerDetail = customerDetailService.getOrCreateByUserId(userId);
            log.info("获取到顾客信息，customerId={}, userId={}", customerDetail.getCustomerId(), userId);
        } catch (BusinessException e) {
            log.error("获取或创建顾客信息失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取或创建顾客信息异常", e);
            throw new BusinessException(ResultCode.OPERATION_FAILED, "获取顾客信息失败：" + e.getMessage());
        }

        // 设置customer_id（忽略前端传入的customer_id，使用从token中获取的userId对应的customer_id）
        appointment.setCustomerId(customerDetail.getCustomerId());
        log.info("设置预约的customerId={}", appointment.getCustomerId());

        // 创建预约
        return Result.success(appointmentService.createAppointment(appointment));
    }

    @Operation(summary = "修改预约", description = "仅待确认或已确认的预约允许修改，会重新校验时间冲突。支持修改跨天预约和结算方式，系统会自动重新计算totalDuration、totalDays和totalAmount。")
    @PutMapping("/{appointmentId}")
    public Result<Appointment> update(@PathVariable Long appointmentId,
                                      @RequestBody Appointment appointment) {
        appointment.setAppointmentId(appointmentId);
        return Result.success(appointmentService.updateAppointment(appointment));
    }

    @Operation(summary = "修改预约状态", description = "例如确认、完成、取消等")
    @PatchMapping("/{appointmentId}/status")
    public Result<Void> updateStatus(@PathVariable Long appointmentId,
                                     @RequestParam Integer status) {
        appointmentService.updateStatus(appointmentId, status);
        return Result.success();
    }
}


