package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.EmailRecord;

/**
 * 邮件服务（仅做记录 + 调用外部网关的占位）
 */
public interface EmailService {

    /**
     * 发送邮件
     */
    EmailRecord sendEmail(String email, String type, String subject, String content);
    
    /**
     * 发送验证码邮件
     */
    EmailRecord sendVerificationCode(String email);
}


