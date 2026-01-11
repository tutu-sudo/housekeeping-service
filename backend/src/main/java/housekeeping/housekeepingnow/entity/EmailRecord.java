package housekeeping.housekeepingnow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件发送记录实体
 */
@Data
public class EmailRecord {

    private Long emailId;

    private String email;

    private String emailType;

    private String subject;

    private String content;

    /**
     * 验证码（仅验证码类型）
     */
    private String code;

    private Integer sendStatus;

    private LocalDateTime sendTime;

    /**
     * 过期时间（仅验证码类型）
     */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}


