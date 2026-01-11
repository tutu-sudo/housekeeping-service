package housekeeping.housekeepingnow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信发送记录实体
 */
@Data
public class SmsRecord {

    private Long smsId;

    private String phone;

    private String smsType;

    private String content;

    private String code;

    private Integer sendStatus;

    private LocalDateTime sendTime;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}


