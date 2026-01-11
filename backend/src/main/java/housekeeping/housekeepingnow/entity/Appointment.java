package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约记录实体类
 */
@Data
public class Appointment {
    /**
     * 预约ID
     */
    private Long appointmentId;
    
    /**
     * 顾客ID（关联customer_detail表）
     */
    private Long customerId;
    
    /**
     * 服务人员ID（关联staff表）
     */
    private Long staffId;
    
    /**
     * 服务ID（关联service表）
     */
    private Long serviceId;
    
    /**
     * 服务名称（查询时关联获取，不存储）
     */
    private String serviceName;
    
    /**
     * 服务人员名称（查询时关联获取，不存储）
     */
    private String staffName;
    
    /**
     * 支付状态（查询时关联获取，不存储）：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer paymentStatus;
    
    /**
     * 预约日期
     */
    private LocalDate appointmentDate;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 结束日期时间（支持跨天）
     */
    private LocalDateTime endDatetime;
    
    /**
     * 总时长（小时）
     */
    private BigDecimal totalDuration;
    
    /**
     * 总天数（按天结算时使用，不满一天按一天计算）
     */
    private Integer totalDays;
    
    /**
     * 结算方式：hourly-按小时，daily-按天，times-按次数
     */
    private String billingType;
    
    /**
     * 服务地址
     */
    private String serviceAddress;
    
    /**
     * 特殊要求
     */
    private String specialRequirements;
    
    /**
     * 状态：0-待确认，1-已确认，2-进行中，3-已完成，4-已取消，5-已拒绝
     */
    private Integer status;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

