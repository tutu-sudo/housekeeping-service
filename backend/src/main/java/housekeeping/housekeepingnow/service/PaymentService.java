package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Payment;

import java.util.List;

/**
 * 支付记录服务接口
 */
public interface PaymentService {

    /**
     * 根据支付ID查询
     */
    Payment getById(Long paymentId);

    /**
     * 根据预约ID查询（如果不存在返回null）
     */
    Payment getByAppointmentId(Long appointmentId);

    /**
     * 根据预约ID查询（不存在时抛出异常）
     */
    Payment getByAppointmentIdOrThrow(Long appointmentId);

    /**
     * 根据交易号查询
     */
    Payment getByTransactionId(String transactionId);

    /**
     * 创建支付记录
     */
    Payment createPayment(Payment payment);

    /**
     * 更新支付记录
     */
    Payment updatePayment(Payment payment);

    /**
     * 更新支付状态
     */
    void updatePaymentStatus(Long paymentId, Integer paymentStatus, String transactionId);

    /**
     * 后台-支付记录列表查询
     */
    List<Payment> listPayments(Long appointmentId, Integer paymentStatus, String paymentMethod);

    /**
     * 根据预约ID删除支付记录
     */
    void deleteByAppointmentId(Long appointmentId);
}


