package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.EmailRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmailRecordMapper {

    EmailRecord selectById(@Param("emailId") Long emailId);

    List<EmailRecord> selectByEmail(@Param("email") String email,
                                    @Param("emailType") String emailType);

    int insert(EmailRecord record);

    int updateStatus(@Param("emailId") Long emailId,
                     @Param("sendStatus") Integer sendStatus);
}


