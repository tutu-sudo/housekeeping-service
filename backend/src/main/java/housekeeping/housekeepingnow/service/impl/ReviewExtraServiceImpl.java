package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.Payment;
import housekeeping.housekeepingnow.entity.ReviewExtra;
import housekeeping.housekeepingnow.mapper.AppointmentMapper;
import housekeeping.housekeepingnow.mapper.ReviewExtraMapper;
import housekeeping.housekeepingnow.service.PaymentService;
import housekeeping.housekeepingnow.service.ReviewExtraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReviewExtraServiceImpl implements ReviewExtraService {

    @Autowired
    private ReviewExtraMapper reviewExtraMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PaymentService paymentService;

    @Override
    public ReviewExtra getById(Long reviewExtraId) {
        ReviewExtra re = reviewExtraMapper.selectById(reviewExtraId);
        if (re == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        return re;
    }

    @Override
    public ReviewExtra getByAppointmentIdAndRole(Long appointmentId, Integer reviewerRole) {
        // 默认查询「评价对象为服务人员」，主要用于服务人员自评（role=2, target=2）
        ReviewExtra re = reviewExtraMapper.selectByAppointmentIdAndRoleAndTarget(appointmentId, reviewerRole, 2);
        if (re == null) {
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }
        return re;
    }

    @Override
    public List<ReviewExtra> listByAppointmentId(Long appointmentId) {
        return reviewExtraMapper.selectByAppointmentId(appointmentId);
    }

    @Override
    public List<ReviewExtra> listByReviewerUserId(Long reviewerUserId) {
        return reviewExtraMapper.selectByReviewerUserId(reviewerUserId);
    }

    @Override
    @Transactional
    public ReviewExtra createReviewExtra(ReviewExtra reviewExtra) {
        if (reviewExtra.getAppointmentId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "预约ID不能为空");
        }
        if (reviewExtra.getReviewerRole() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评价者角色不能为空");
        }
        if (reviewExtra.getReviewTarget() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评价对象不能为空");
        }
        if (reviewExtra.getReviewerUserId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评价者用户ID不能为空");
        }

        // 校验预约存在
        Appointment appointment = appointmentMapper.selectById(reviewExtra.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        // 检查支付状态：只有已支付的订单才能评价
        Payment payment = paymentService.getByAppointmentId(reviewExtra.getAppointmentId());
        if (payment == null || payment.getPaymentStatus() == null || payment.getPaymentStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID, "订单未支付，无法评价");
        }

        // 检查预约状态：只有已完成的订单才能评价
        if (appointment.getStatus() == null || appointment.getStatus() != 3) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_ERROR, "只有已完成的订单才能评价");
        }

        // 校验评分区间 0~5
        if (reviewExtra.getOverallRating() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评分不能为空");
        }
        checkRange(reviewExtra.getOverallRating());

        // 每单每角色只能评价一次
        ReviewExtra exist = reviewExtraMapper.selectByAppointmentIdAndRoleAndTarget(
                reviewExtra.getAppointmentId(), reviewExtra.getReviewerRole(), reviewExtra.getReviewTarget());
        if (exist != null) {
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS, "该订单该角色已评价");
        }

        reviewExtraMapper.insert(reviewExtra);
        return reviewExtraMapper.selectByAppointmentIdAndRoleAndTarget(
                reviewExtra.getAppointmentId(), reviewExtra.getReviewerRole(), reviewExtra.getReviewTarget());
    }

    private void checkRange(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BusinessException(ResultCode.REVIEW_SCORE_INVALID);
        }
    }
}

