package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.dto.AdminReviewSummaryDTO;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.Payment;
import housekeeping.housekeepingnow.entity.Review;
import housekeeping.housekeepingnow.mapper.AdminReviewMapper;
import housekeeping.housekeepingnow.mapper.AppointmentMapper;
import housekeeping.housekeepingnow.mapper.ReviewMapper;
import housekeeping.housekeepingnow.service.PaymentService;
import housekeeping.housekeepingnow.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {
    private static final String MASK_DISPLAY_TEXT = "[已屏蔽]";
    private static final String MASK_STORAGE_PREFIX = "__MASKED__:";

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AdminReviewMapper adminReviewMapper;

    @Override
    public Review getById(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        return sanitizeReviewForOutput(review);
    }

    @Override
    public Review getByAppointmentId(Long appointmentId) {
        Review review = reviewMapper.selectByAppointmentId(appointmentId);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        return sanitizeReviewForOutput(review);
    }

    @Override
    public List<Review> listByStaffId(Long staffId) {
        List<Review> reviews = reviewMapper.selectByStaffId(staffId);
        for (Review review : reviews) {
            sanitizeReviewForOutput(review);
        }
        return reviews;
    }

    @Override
    public List<Review> listByCustomerId(Long customerId) {
        List<Review> reviews = reviewMapper.selectByCustomerId(customerId);
        for (Review review : reviews) {
            sanitizeReviewForOutput(review);
        }
        return reviews;
    }

    @Override
    @Transactional
    public Review createReview(Review review) {
        // 校验预约存在
        Appointment appointment = appointmentMapper.selectById(review.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        // 检查支付状态：只有已支付的订单才能评价
        Payment payment = paymentService.getByAppointmentId(review.getAppointmentId());
        if (payment == null || payment.getPaymentStatus() == null || payment.getPaymentStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID, "订单未支付，无法评价");
        }

        // 检查预约状态：只有已完成的订单才能评价
        if (appointment.getStatus() == null || appointment.getStatus() != 3) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_ERROR, "只有已完成的订单才能评价");
        }

        // 同一预约只能评价一次
        Review exist = reviewMapper.selectByAppointmentId(review.getAppointmentId());
        if (exist != null) {
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS);
        }

        // 校验评分
        validateScores(review);

        // 默认匿名标志
        if (review.getIsAnonymous() == null) {
            review.setIsAnonymous(0);
        }

        reviewMapper.insert(review);
        return sanitizeReviewForOutput(reviewMapper.selectByAppointmentId(review.getAppointmentId()));
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        Review dbReview = reviewMapper.selectById(review.getReviewId());
        if (dbReview == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        // 校验评分（仅当有传入新分数时）
        validateScores(review);

        reviewMapper.update(review);
        return sanitizeReviewForOutput(reviewMapper.selectById(dbReview.getReviewId()));
    }

    /**
     * 校验评分区间 0~5
     */
    private void validateScores(Review review) {
        if (review.getOverallRating() != null) {
            checkRange(review.getOverallRating());
        }
        if (review.getServiceAttitudeRating() != null) {
            checkRange(review.getServiceAttitudeRating());
        }
        if (review.getProfessionalAbilityRating() != null) {
            checkRange(review.getProfessionalAbilityRating());
        }
        if (review.getServiceQualityRating() != null) {
            checkRange(review.getServiceQualityRating());
        }
    }

    private void checkRange(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BusinessException(ResultCode.REVIEW_SCORE_INVALID);
        }
    }

    @Override
    public List<AdminReviewSummaryDTO> listReviews(Long staffId,
                                                   Long appointmentId,
                                                   Integer reviewerRole,
                                                   Integer reviewTarget) {
        List<AdminReviewSummaryDTO> results;
        // 默认场景：不传 reviewerRole / reviewTarget 时，按顾客评价列表处理
        if (reviewerRole == null && reviewTarget == null) {
            results = adminReviewMapper.selectCustomerReviews(staffId, appointmentId);
            sanitizeAdminSummariesForOutput(results);
            return results;
        }

        // 顾客评价列表：reviewerRole=1, reviewTarget=2
        if (Integer.valueOf(1).equals(reviewerRole)) {
            // 目前只支持“评价服务人员”的顾客评价
            results = adminReviewMapper.selectCustomerReviews(staffId, appointmentId);
            sanitizeAdminSummariesForOutput(results);
            return results;
        }

        // 员工自评列表：reviewerRole=2, reviewTarget=2
        if (Integer.valueOf(2).equals(reviewerRole) && (reviewTarget == null || Integer.valueOf(2).equals(reviewTarget))) {
            results = adminReviewMapper.selectStaffSelfReviews(staffId, appointmentId);
            sanitizeAdminSummariesForOutput(results);
            return results;
        }

        // 其他组合（例如管理员评价）暂未实现，先返回空列表，后续再扩展
        return java.util.Collections.emptyList();
    }

    @Override
    public void maskReview(Long reviewId, String reason) {
        Review db = reviewMapper.selectById(reviewId);
        if (db == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        if (isMaskedContent(db.getReviewContent())) {
            return;
        }

        Review update = new Review();
        update.setReviewId(reviewId);
        update.setReviewContent(toMaskedStorageContent(db.getReviewContent()));
        reviewMapper.update(update);
    }

    @Override
    public void approveReview(Long reviewId) {
        Review db = reviewMapper.selectById(reviewId);
        if (db == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        if (!isMaskedContent(db.getReviewContent())) {
            return;
        }

        Review update = new Review();
        update.setReviewId(reviewId);
        update.setReviewContent(restoreOriginalContent(db.getReviewContent()));
        reviewMapper.update(update);
    }

    private void sanitizeAdminSummariesForOutput(List<AdminReviewSummaryDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AdminReviewSummaryDTO dto : list) {
            boolean masked = isMaskedContent(dto.getReviewContent());
            dto.setIsMasked(masked);
            if (masked) {
                dto.setReviewContent(MASK_DISPLAY_TEXT);
            }
        }
    }

    private Review sanitizeReviewForOutput(Review review) {
        if (review == null) {
            return null;
        }
        boolean masked = isMaskedContent(review.getReviewContent());
        review.setIsMasked(masked);
        if (masked) {
            review.setReviewContent(MASK_DISPLAY_TEXT);
        }
        return review;
    }

    private boolean isMaskedContent(String reviewContent) {
        if (reviewContent == null) {
            return false;
        }
        return reviewContent.startsWith(MASK_STORAGE_PREFIX) || reviewContent.startsWith(MASK_DISPLAY_TEXT);
    }

    private String toMaskedStorageContent(String original) {
        String source = original == null ? "" : original;
        String base64 = Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
        return MASK_STORAGE_PREFIX + base64;
    }

    private String restoreOriginalContent(String storedContent) {
        if (storedContent == null) {
            return null;
        }
        if (!storedContent.startsWith(MASK_STORAGE_PREFIX)) {
            // 兼容历史已被覆盖为纯“[已屏蔽]”的数据，无法恢复原文时回落为空字符串
            return "";
        }
        String base64 = storedContent.substring(MASK_STORAGE_PREFIX.length());
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }
}


