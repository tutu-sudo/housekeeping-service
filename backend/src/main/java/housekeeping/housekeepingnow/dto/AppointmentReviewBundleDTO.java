package housekeeping.housekeepingnow.dto;

import housekeeping.housekeepingnow.entity.Review;
import housekeeping.housekeepingnow.entity.ReviewExtra;
import lombok.Data;

/**
 * 订单三方评价包（顾客评价 + 服务人员自评 + 管理员评价）
 */
@Data
public class AppointmentReviewBundleDTO {
    private Long appointmentId;

    /**
     * 顾客对服务人员的评价（旧表 review）
     */
    private Review customerReview;

    /**
     * 服务人员自评（新表 review_extra，reviewerRole=2）
     */
    private ReviewExtra staffSelfReview;

    /**
     * 管理员针对顾客的评价（reviewerRole=3, reviewTarget=1）
     */
    private ReviewExtra adminReviewForCustomer;

    /**
     * 管理员针对服务人员的评价（reviewerRole=3, reviewTarget=2）
     */
    private ReviewExtra adminReviewForStaff;
}

