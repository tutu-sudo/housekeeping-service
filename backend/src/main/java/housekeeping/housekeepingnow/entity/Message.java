package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 站内信实体类
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 用户ID（关联user表）
     */
    private Long userId;
    
    /**
     * 消息类型：system-系统消息，order-订单消息，appointment-预约消息，review-评价消息
     */
    private String messageType;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 关联ID（如订单ID、预约ID等）
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}

