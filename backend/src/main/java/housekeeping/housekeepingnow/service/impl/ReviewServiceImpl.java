package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.Review;
import housekeeping.housekeepingnow.mapper.AppointmentMapper;
import housekeeping.housekeepingnow.mapper.ReviewMapper;
import housekeeping.housekeepingnow.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Review getById(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        return review;
    }

    @Override
    public Review getByAppointmentId(Long appointmentId) {
        Review review = reviewMapper.selectByAppointmentId(appointmentId);
        if (review == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        return review;
    }

    @Override
    public List<Review> listByStaffId(Long staffId) {
        return reviewMapper.selectByStaffId(staffId);
    }

    @Override
    public List<Review> listByCustomerId(Long customerId) {
        return reviewMapper.selectByCustomerId(customerId);
    }

    @Override
    @Transactional
    public Review createReview(Review review) {
        // 校验预约存在
        Appointment appointment = appointmentMapper.selectById(review.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
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
        return reviewMapper.selectByAppointmentId(review.getAppointmentId());
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
        return reviewMapper.selectById(dbReview.getReviewId());
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
    public List<Review> listReviews(Long staffId, Long appointmentId) {
        return reviewMapper.selectList(staffId, appointmentId);
    }

    @Override
    public void maskReview(Long reviewId, String reason) {
        Review db = reviewMapper.selectById(reviewId);
        if (db == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        Review update = new Review();
        update.setReviewId(reviewId);
        String content = "[已屏蔽]";
        if (reason != null && !reason.isBlank()) {
            content = content + " " + reason;
        }
        update.setReviewContent(content);
        reviewMapper.update(update);
    }

    @Override
    public void approveReview(Long reviewId) {
        Review db = reviewMapper.selectById(reviewId);
        if (db == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        // 暂无额外字段，视为审核通过（可在此扩展操作日志等）
    }
}


