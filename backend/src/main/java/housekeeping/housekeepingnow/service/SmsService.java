package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.SmsRecord;

/**
 * 短信服务（仅做记录 + 调用外部网关的占位）
 */
public interface SmsService {

    /**
     * 发送短信验证码
     */
    SmsRecord sendVerificationCode(String phone);

    /**
     * 发送普通短信
     */
    SmsRecord sendSms(String phone, String type, String content);
}


