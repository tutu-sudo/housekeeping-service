package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.SmsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsRecordMapper {

    SmsRecord selectById(@Param("smsId") Long smsId);

    List<SmsRecord> selectByPhone(@Param("phone") String phone,
                                  @Param("smsType") String smsType);

    int insert(SmsRecord record);

    int updateStatus(@Param("smsId") Long smsId,
                     @Param("sendStatus") Integer sendStatus);
}


