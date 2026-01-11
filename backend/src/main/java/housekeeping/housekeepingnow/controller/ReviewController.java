package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.CustomerDetail;
import housekeeping.housekeepingnow.entity.Review;
import housekeeping.housekeepingnow.service.CustomerDetailService;
import housekeeping.housekeepingnow.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价接口控制器
 */
@Slf4j
@Tag(name = "评价接口", description = "提交评价、更新评价、查询评价等接口")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private CustomerDetailService customerDetailService;

    @Operation(summary = "根据评价ID查询")
    @GetMapping("/{reviewId}")
    public Result<Review> getById(@PathVariable Long reviewId) {
        return Result.success(reviewService.getById(reviewId));
    }

    @Operation(summary = "根据预约ID查询评价")
    @GetMapping("/appointment/{appointmentId}")
    public Result<Review> getByAppointmentId(@PathVariable Long appointmentId) {
        return Result.success(reviewService.getByAppointmentId(appointmentId));
    }

    @Operation(summary = "查询当前登录用户的评价列表", description = "根据当前登录用户的userId自动查询对应的评价列表，无需传递customerId参数")
    @RequireAuth
    @RequireRole({1}) // 仅顾客类型（user_type=1）可以查询自己的评价
    @GetMapping("/my-reviews")
    public Result<List<Review>> getMyReviews(HttpServletRequest request) {
        // 从JWT token中获取当前登录用户的userId
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("查询评价列表失败：无法获取当前登录用户ID，请先登录");
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

        log.info("查询当前登录用户的评价列表，userId={}", userId);

        // 获取顾客详细信息
        CustomerDetail customerDetail = customerDetailService.getOrCreateByUserId(userId);
        log.info("获取到顾客信息，customerId={}, userId={}", customerDetail.getCustomerId(), userId);

        // 查询该顾客的评价列表
        List<Review> reviews = reviewService.listByCustomerId(customerDetail.getCustomerId());
        return Result.success(reviews);
    }

    @Operation(summary = "查询服务人员的评价列表")
    @GetMapping("/staff/{staffId}")
    public Result<List<Review>> listByStaff(@PathVariable Long staffId) {
        return Result.success(reviewService.listByStaffId(staffId));
    }

    @Operation(summary = "查询顾客的评价列表")
    @GetMapping("/customer/{customerId}")
    public Result<List<Review>> listByCustomer(@PathVariable Long customerId) {
        return Result.success(reviewService.listByCustomerId(customerId));
    }

    @Operation(summary = "提交评价", description = "同一预约仅允许一条评价")
    @PostMapping
    public Result<Review> create(@RequestBody Review review) {
        // 确保appointmentId是正确的Long类型
        if (review.getAppointmentId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约ID不能为空");
        }
        // 如果是字符串或其他类型，这里已经由Jackson转换为Long了
        // 如果转换失败，会在请求绑定阶段抛出异常
        return Result.success(reviewService.createReview(review));
    }

    @Operation(summary = "更新评价")
    @PutMapping("/{reviewId}")
    public Result<Review> update(@PathVariable Long reviewId,
                                 @RequestBody Review review) {
        review.setReviewId(reviewId);
        return Result.success(reviewService.updateReview(review));
    }
}


