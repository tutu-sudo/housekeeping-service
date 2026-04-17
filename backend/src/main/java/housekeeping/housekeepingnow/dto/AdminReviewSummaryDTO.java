package housekeeping.housekeepingnow.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台-评价管理列表统一返回结构
 */
@Data
public class AdminReviewSummaryDTO {

    /**
     * 评价ID（顾客评价为 review_id，自评为 review_extra_id）
     */
    private Long reviewId;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 服务人员ID
     */
    private Long staffId;

    /**
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 顾客ID
     */
    private Long customerId;

    /**
     * 顾客姓名
     */
    private String customerName;

    /**
     * 总体评分（0-5分）
     */
    private BigDecimal overallRating;

    /**
     * 服务态度评分（顾客评价有，自评为 null）
     */
    private BigDecimal serviceAttitudeRating;

    /**
     * 专业能力评分（顾客评价有，自评为 null）
     */
    private BigDecimal professionalAbilityRating;

    /**
     * 服务质量评分（顾客评价有，自评为 null）
     */
    private BigDecimal serviceQualityRating;

    /**
     * 评价内容
     */
    private String reviewContent;

    /**
     * 是否屏蔽（true-已屏蔽，false-可展示）
     */
    private Boolean isMasked;

    /**
     * 是否匿名：0-否，1-是（自评统一返回 0）
     */
    private Integer isAnonymous;

    /**
     * 评价时间
     */
    private LocalDateTime createTime;

    /**
     * 评价来源：1-顾客评价，2-员工自评，3-管理员评价（预留）
     */
    private Integer reviewerRole;

    /**
     * 评价对象：1-顾客，2-服务人员
     */
    private Integer reviewTarget;
}

