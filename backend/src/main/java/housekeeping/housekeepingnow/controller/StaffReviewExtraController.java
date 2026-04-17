package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.AppointmentReviewBundleDTO;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.ReviewExtra;
import housekeeping.housekeepingnow.service.AppointmentReviewBundleService;
import housekeeping.housekeepingnow.service.AppointmentService;
import housekeeping.housekeepingnow.service.ReviewExtraService;
import housekeeping.housekeepingnow.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务人员-自评/评价包接口
 */
@Tag(name = "服务人员-评价接口")
@RestController
@RequestMapping("/api/staff/reviews")
@RequireAuth
@RequireRole({2})
public class StaffReviewExtraController {

    @Autowired
    private ReviewExtraService reviewExtraService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private AppointmentReviewBundleService appointmentReviewBundleService;

    @Operation(summary = "提交服务人员自评（每单一次）", description = "仅已完成且已支付的订单允许自评")
    @PostMapping("/self")
    public Result<ReviewExtra> createSelfReview(@RequestBody ReviewExtra reviewExtra,
                                                HttpServletRequest request) {
        Long userId = parseUserId(request);
        var staff = staffService.getByUserId(userId);

        if (reviewExtra.getAppointmentId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约ID不能为空");
        }

        Appointment appointment = appointmentService.getById(reviewExtra.getAppointmentId());
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 固定：服务人员自评，评价对象为「服务人员本人」
        reviewExtra.setReviewerRole(2);
        reviewExtra.setReviewTarget(2);
        reviewExtra.setReviewerUserId(userId);

        return Result.success(reviewExtraService.createReviewExtra(reviewExtra));
    }

    @Operation(summary = "查询当前登录服务人员的自评列表")
    @GetMapping("/my-self-reviews")
    public Result<List<ReviewExtra>> mySelfReviews(HttpServletRequest request) {
        Long userId = parseUserId(request);
        return Result.success(reviewExtraService.listByReviewerUserId(userId));
    }

    @Operation(summary = "按订单获取三方评价包（服务人员视角）", description = "包含：顾客评价、服务人员自评、管理员评价")
    @GetMapping("/appointment/{appointmentId}/bundle")
    public Result<AppointmentReviewBundleDTO> getBundle(@PathVariable Long appointmentId,
                                                        HttpServletRequest request) {
        Long userId = parseUserId(request);
        var staff = staffService.getByUserId(userId);

        Appointment appointment = appointmentService.getById(appointmentId);
        if (!appointment.getStaffId().equals(staff.getStaffId())) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        return Result.success(appointmentReviewBundleService.getBundleByAppointmentId(appointmentId));
    }

    private Long parseUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return Long.valueOf(userIdObj.toString());
    }
}

