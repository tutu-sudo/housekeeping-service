package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.entity.Message;
import housekeeping.housekeepingnow.mapper.MessageMapper;
import housekeeping.housekeepingnow.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Message sendMessage(Long userId, String type, String title, String content, Long relatedId) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setMessageType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setRelatedId(relatedId);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        return messageMapper.selectById(msg.getMessageId());
    }

    @Override
    public List<Message> listUserMessages(Long userId, Integer isRead) {
        return messageMapper.selectByUser(userId, isRead);
    }

    @Override
    public void markAsRead(Long messageId) {
        messageMapper.markAsRead(messageId);
    }
}


