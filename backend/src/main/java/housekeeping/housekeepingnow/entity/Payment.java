package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付实体类
 */
@Data
public class Payment {
    /**
     * 支付ID
     */
    private Long paymentId;
    
    /**
     * 预约ID（关联appointment表）
     */
    private Long appointmentId;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付方式：alipay-支付宝，wechat-微信支付
     */
    private String paymentMethod;
    
    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer paymentStatus;
    
    /**
     * 第三方交易号
     */
    private String transactionId;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

