package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 服务质量统计数据传输对象
 */
@Data
public class ServiceQualityStatisticsDTO {
    /**
     * 服务类型ID
     */
    private Long serviceTypeId;
    
    /**
     * 服务类型名称
     */
    private String serviceTypeName;
    
    /**
     * 服务人员ID
     */
    private Long staffId;
    
    /**
     * 服务人员姓名
     */
    private String staffName;
    
    /**
     * 评价总数
     */
    private Long reviewCount;
    
    /**
     * 平均总体评分
     */
    private BigDecimal averageOverallRating;
    
    /**
     * 平均服务态度评分
     */
    private BigDecimal averageServiceAttitudeRating;
    
    /**
     * 平均专业能力评分
     */
    private BigDecimal averageProfessionalAbilityRating;
    
    /**
     * 平均服务质量评分
     */
    private BigDecimal averageServiceQualityRating;
    
    /**
     * 好评数量（评分>=4.5）
     */
    private Long goodReviewCount;
    
    /**
     * 中评数量（3.0<=评分<4.5）
     */
    private Long mediumReviewCount;
    
    /**
     * 差评数量（评分<3.0）
     */
    private Long badReviewCount;
    
    /**
     * 好评率
     */
    private BigDecimal goodReviewRate;
}

