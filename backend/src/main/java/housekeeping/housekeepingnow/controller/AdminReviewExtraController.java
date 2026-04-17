package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.AppointmentReviewBundleDTO;
import housekeeping.housekeepingnow.entity.ReviewExtra;
import housekeeping.housekeepingnow.service.AppointmentReviewBundleService;
import housekeeping.housekeepingnow.service.ReviewExtraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-扩展评价管理（管理员评价）
 */
@Tag(name = "后台-扩展评价管理接口")
@RestController
@RequestMapping("/api/admin/review-extras")
@RequireAuth
@RequireRole({3})
public class AdminReviewExtraController {

    @Autowired
    private ReviewExtraService reviewExtraService;

    @Autowired
    private AppointmentReviewBundleService appointmentReviewBundleService;

    @Operation(summary = "管理员提交评价（每单一次）", description = "仅已完成且已支付的订单允许评价")
    @PostMapping
    public Result<ReviewExtra> create(@RequestBody ReviewExtra reviewExtra,
                                      HttpServletRequest request) {
        Long userId = parseUserId(request);
        if (reviewExtra.getAppointmentId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约ID不能为空");
        }
        if (reviewExtra.getReviewTarget() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评价对象不能为空，1-顾客，2-服务人员");
        }
        // 固定：管理员评价（可对顾客或服务人员各一条）
        reviewExtra.setReviewerRole(3);
        reviewExtra.setReviewerUserId(userId);
        return Result.success(reviewExtraService.createReviewExtra(reviewExtra));
    }

    @Operation(summary = "查询我的管理员评价列表（按 reviewerUserId）")
    @GetMapping("/mine")
    public Result<List<ReviewExtra>> mine(HttpServletRequest request) {
        Long userId = parseUserId(request);
        return Result.success(reviewExtraService.listByReviewerUserId(userId));
    }

    @Operation(summary = "按订单获取三方评价包（管理员视角）")
    @GetMapping("/appointment/{appointmentId}/bundle")
    public Result<AppointmentReviewBundleDTO> getBundle(@PathVariable Long appointmentId) {
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

