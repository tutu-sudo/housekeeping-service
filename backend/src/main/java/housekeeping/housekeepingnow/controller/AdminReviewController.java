package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.AdminReviewSummaryDTO;
import housekeeping.housekeepingnow.entity.Review;
import housekeeping.housekeepingnow.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-评价管理
 */
@Tag(name = "后台-评价管理接口")
@RestController
@RequestMapping("/api/admin/reviews")
@RequireAuth
@RequireRole({3}) // 仅管理员可访问
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "评价详情")
    @GetMapping("/{reviewId}")
    public Result<Review> getById(@PathVariable Long reviewId) {
        return Result.success(reviewService.getById(reviewId));
    }

    @Operation(summary = "评价列表")
    @GetMapping
    public Result<List<AdminReviewSummaryDTO>> list(@RequestParam(required = false) Long staffId,
                                                    @RequestParam(required = false) Long appointmentId,
                                                    @RequestParam(required = false) Integer reviewerRole,
                                                    @RequestParam(required = false) Integer reviewTarget) {
        return Result.success(reviewService.listReviews(staffId, appointmentId, reviewerRole, reviewTarget));
    }

    @Operation(summary = "审核通过评价")
    @PatchMapping("/{reviewId}/approve")
    public Result<Void> approve(@PathVariable Long reviewId) {
        reviewService.approveReview(reviewId);
        return Result.success();
    }

    @Operation(summary = "屏蔽评价")
    @PatchMapping("/{reviewId}/mask")
    public Result<Void> mask(@PathVariable Long reviewId,
                             @RequestParam(required = false) String reason) {
        reviewService.maskReview(reviewId, reason);
        return Result.success();
    }
}


