package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Payment;
import housekeeping.housekeepingnow.mapper.PaymentMapper;
import housekeeping.housekeepingnow.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Payment getById(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    @Override
    public Payment getByAppointmentId(Long appointmentId) {
        // 如果不存在返回null，不抛异常
        return paymentMapper.selectByAppointmentId(appointmentId);
    }

    @Override
    public Payment getByAppointmentIdOrThrow(Long appointmentId) {
        Payment payment = paymentMapper.selectByAppointmentId(appointmentId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    @Override
    public Payment getByTransactionId(String transactionId) {
        return paymentMapper.selectByTransactionId(transactionId);
    }

    @Override
    @Transactional
    public Payment createPayment(Payment payment) {
        if (payment.getCreateTime() == null) {
            payment.setCreateTime(LocalDateTime.now());
        }
        if (payment.getUpdateTime() == null) {
            payment.setUpdateTime(LocalDateTime.now());
        }
        // 默认状态：待支付
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(0);
        }
        paymentMapper.insert(payment);
        return payment;
    }

    @Override
    @Transactional
    public Payment updatePayment(Payment payment) {
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.update(payment);
        return paymentMapper.selectById(payment.getPaymentId());
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long paymentId, Integer paymentStatus, String transactionId) {
        paymentMapper.updatePaymentStatus(paymentId, paymentStatus, transactionId);
    }

    @Override
    public List<Payment> listPayments(Long appointmentId, Integer paymentStatus, String paymentMethod) {
        return paymentMapper.selectList(appointmentId, paymentStatus, paymentMethod);
    }

    @Override
    @Transactional
    public void deleteByAppointmentId(Long appointmentId) {
        paymentMapper.deleteByAppointmentId(appointmentId);
    }
}


