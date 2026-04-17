package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Message;

import java.util.List;

/**
 * 站内信服务
 */
public interface MessageService {

    /**
     * 发送站内信
     */
    Message sendMessage(Long userId, String type, String title, String content, Long relatedId);

    /**
     * 查询用户消息
     */
    List<Message> listUserMessages(Long userId, Integer isRead);

    /**
     * 标记已读
     */
    void markAsRead(Long messageId);
}


