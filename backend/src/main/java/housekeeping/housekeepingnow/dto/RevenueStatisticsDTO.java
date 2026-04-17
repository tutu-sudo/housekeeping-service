package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收入统计数据传输对象
 */
@Data
public class RevenueStatisticsDTO {
    /**
     * 日期（用于按日期统计）
     */
    private LocalDate date;
    
    /**
     * 支付方式：alipay-支付宝，wechat-微信支付
     */
    private String paymentMethod;
    
    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer paymentStatus;
    
    /**
     * 支付成功数量
     */
    private Long successCount;
    
    /**
     * 支付成功总金额
     */
    private BigDecimal successAmount;
    
    /**
     * 退款数量
     */
    private Long refundCount;
    
    /**
     * 退款总金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 净收入（支付成功金额 - 退款金额）
     */
    private BigDecimal netRevenue;
}

