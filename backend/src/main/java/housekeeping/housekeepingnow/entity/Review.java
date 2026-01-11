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
}

