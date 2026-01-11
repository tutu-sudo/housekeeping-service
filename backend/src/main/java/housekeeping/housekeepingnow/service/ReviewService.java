package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Review;

import java.util.List;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 根据评价ID查询
     */
    Review getById(Long reviewId);

    /**
     * 根据预约ID查询评价
     */
    Review getByAppointmentId(Long appointmentId);

    /**
     * 查询服务人员的评价列表
     */
    List<Review> listByStaffId(Long staffId);

    /**
     * 查询顾客的评价列表（通过预约关联查询）
     */
    List<Review> listByCustomerId(Long customerId);

    /**
     * 创建评价（同一预约仅允许一条评价）
     */
    Review createReview(Review review);

    /**
     * 更新评价
     */
    Review updateReview(Review review);

    /**
     * 后台-评价列表查询（可按服务人员/预约过滤）
     */
    List<Review> listReviews(Long staffId, Long appointmentId);

    /**
     * 后台-屏蔽评价（将内容打码）
     */
    void maskReview(Long reviewId, String reason);

    /**
     * 后台-审核通过（占位，便于后续扩展）
     */
    void approveReview(Long reviewId);
}


