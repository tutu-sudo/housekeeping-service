package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务评价实体类
 */
@Data
public class Review {
    /**
     * 评价ID
     */
    private Long reviewId;
    
    /**
     * 预约ID（关联appointment表）
     */
    private Long appointmentId;
    
    /**
     * 总体评分（0-5分）
     */
    private BigDecimal overallRating;
    
    /**
     * 服务态度分（0-5分）
     */
    private BigDecimal serviceAttitudeRating;
    
    /**
     * 专业能力分（0-5分）
     */
    private BigDecimal professionalAbilityRating;
    
    /**
     * 服务质量分（0-5分）
     */
    private BigDecimal serviceQualityRating;
    
    /**
     * 评价内容
     */
    private String reviewContent;
    
    /**
     * 是否匿名：0-否，1-是
     */
    private Integer isAnonymous;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 顾客ID（通过 appointment 关联得到，review 表本身不存）
     */
    private Long customerId;

    /**
     * 顾客姓名（通过 appointment 关联得到，review 表本身不存）
     * 说明：历史数据可能为空（例如 appointment.customer_name 未回填）
     */
    private String customerName;

    /**
     * 是否屏蔽（仅用于接口返回，不对应 review 表实际字段）
     */
    private Boolean isMasked;
}

