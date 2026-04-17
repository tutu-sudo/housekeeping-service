package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 支付Mapper接口
 */
@Mapper
public interface PaymentMapper {
    
    /**
     * 根据支付ID查询
     */
    Payment selectById(@Param("paymentId") Long paymentId);
    
    /**
     * 根据预约ID查询支付记录
     */
    Payment selectByAppointmentId(@Param("appointmentId") Long appointmentId);
    
    /**
     * 根据交易号查询
     */
    Payment selectByTransactionId(@Param("transactionId") String transactionId);

    /**
     * 支付记录列表（可按预约/状态/方式过滤）
     */
    List<Payment> selectList(@Param("appointmentId") Long appointmentId,
                             @Param("paymentStatus") Integer paymentStatus,
                             @Param("paymentMethod") String paymentMethod);
    
    /**
     * 插入支付记录
     */
    int insert(Payment payment);
    
    /**
     * 更新支付记录
     */
    int update(Payment payment);
    
    /**
     * 更新支付状态
     */
    int updatePaymentStatus(@Param("paymentId") Long paymentId, 
                           @Param("paymentStatus") Integer paymentStatus,
                           @Param("transactionId") String transactionId);

    /**
     * 根据预约ID删除支付记录
     */
    int deleteByAppointmentId(@Param("appointmentId") Long appointmentId);
}

