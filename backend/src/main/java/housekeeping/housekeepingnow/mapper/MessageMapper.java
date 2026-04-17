package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    Message selectById(@Param("messageId") Long messageId);

    List<Message> selectByUser(@Param("userId") Long userId,
                               @Param("isRead") Integer isRead);

    int insert(Message message);

    int markAsRead(@Param("messageId") Long messageId);
}


