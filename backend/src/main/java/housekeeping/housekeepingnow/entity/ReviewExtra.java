package housekeeping.housekeepingnow.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扩展评价实体类（服务人员自评 / 管理员评价）
 */
@Data
public class ReviewExtra {
    /**
     * 扩展评价ID
     */
    private Long reviewExtraId;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 评价者角色：2-服务人员，3-管理员
     */
    private Integer reviewerRole;

    /**
     * 评价对象：1-顾客，2-服务人员
     */
    private Integer reviewTarget;

    /**
     * 评价者userId（用于审计）
     */
    private Long reviewerUserId;

    /**
     * 总体评分（0-5分）
     */
    private BigDecimal overallRating;

    /**
     * 评价内容
     */
    private String reviewContent;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

