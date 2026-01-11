package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.Payment;
import housekeeping.housekeepingnow.service.AppointmentService;
import housekeeping.housekeepingnow.service.PaymentService;
import housekeeping.housekeepingnow.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务人员预约管理接口控制器
 */
@Tag(name = "服务人员预约管理接口", description = "服务人员查看和管理预约的接口")
@RestController
@RequestMapping("/api/staff/appointments")
@RequireAuth
@RequireRole({2}) // 仅服务人员可访问
public class StaffAppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private StaffService staffService;
    
    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "获取当前登录服务人员的预约列表")
    @GetMapping
    public Result<List<Appointment>> listAppointments(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        List<Appointment> appointments = appointmentService.listByStaffId(staff.getStaffId());
        // 如果指定了状态，进行过滤
        if (status != null) {
            appointments = appointments.stream()
                    .filter(apt -> status.equals(apt.getStatus()))
                    .toList();
        }
        return Result.success(appointments);
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{appointmentId}")
    public Result<Appointment> getById(@PathVariable Long appointmentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        Appointment appointment = appointmentService.getById(appointmentId);
        // 验证预约属于当前服务人员
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        return Result.success(appointment);
    }

    @Operation(summary = "接单（确认预约）")
    @PostMapping("/{appointmentId}/accept")
    public Result<Void> acceptAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        Appointment appointment = appointmentService.getById(appointmentId);
        // 验证预约属于当前服务人员
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        // 状态：0-待确认，1-已确认
        if (appointment.getStatus() != 0) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.APPOINTMENT_STATUS_ERROR);
        }
        appointmentService.updateStatus(appointmentId, 1);
        return Result.success();
    }

    @Operation(summary = "拒绝预约")
    @PostMapping("/{appointmentId}/reject")
    public Result<Void> rejectAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        Appointment appointment = appointmentService.getById(appointmentId);
        // 验证预约属于当前服务人员
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        // 状态：0-待确认，5-已拒绝
        if (appointment.getStatus() != 0) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.APPOINTMENT_STATUS_ERROR);
        }
        appointmentService.updateStatus(appointmentId, 5);
        return Result.success();
    }

    @Operation(summary = "开始服务", description = "对于已支付的订单，服务人员可以手动开始服务，将订单状态从'已确认'改为'进行中'")
    @PostMapping("/{appointmentId}/start")
    public Result<Void> startService(@PathVariable Long appointmentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        Appointment appointment = appointmentService.getById(appointmentId);
        // 验证预约属于当前服务人员
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        // 检查支付状态：只有已支付的订单才能开始服务
        Payment payment = paymentService.getByAppointmentId(appointmentId);
        if (payment == null || payment.getPaymentStatus() == null || payment.getPaymentStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID, "订单未支付，无法开始服务");
        }
        // 状态：1-已确认，2-进行中
        if (appointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_ERROR, "只有'已确认'状态的订单才能开始服务");
        }
        appointmentService.updateStatus(appointmentId, 2);
        return Result.success();
    }

    @Operation(summary = "完成服务", description = "对于已支付的订单，服务人员可以手动结束服务，将订单状态从'进行中'改为'已完成'")
    @PostMapping("/{appointmentId}/complete")
    public Result<Void> completeService(@PathVariable Long appointmentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        Appointment appointment = appointmentService.getById(appointmentId);
        // 验证预约属于当前服务人员
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        // 检查支付状态：只有已支付的订单才能结束服务
        Payment payment = paymentService.getByAppointmentId(appointmentId);
        if (payment == null || payment.getPaymentStatus() == null || payment.getPaymentStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID, "订单未支付，无法结束服务");
        }
        // 状态：2-进行中，3-已完成
        if (appointment.getStatus() != 2) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_ERROR, "只有'进行中'状态的订单才能结束服务");
        }
        appointmentService.updateStatus(appointmentId, 3);
        return Result.success();
    }
}

