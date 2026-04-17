package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.entity.EmailRecord;
import housekeeping.housekeepingnow.mapper.EmailRecordMapper;
import housekeeping.housekeepingnow.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailRecordMapper emailRecordMapper;

    @Override
    public EmailRecord sendEmail(String email, String type, String subject, String content) {
        EmailRecord record = new EmailRecord();
        record.setEmail(email);
        record.setEmailType(type);
        record.setSubject(subject);
        record.setContent(content);
        record.setSendStatus(1); // 先视为成功，后续接入真实网关
        record.setSendTime(LocalDateTime.now());
        emailRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public EmailRecord sendVerificationCode(String email) {
        String code = generateCode();
        String subject = "密码找回验证码";
        String content = "您的验证码为：" + code + "，有效期5分钟。请勿泄露给他人。";
        // 注意：当前为“模拟邮件发送”实现，仅写库不发真实邮件；联调阶段可从日志/数据库获取验证码
        log.debug("【联调】邮件验证码已生成（未真实发送），email={}, code={}, expireInMinutes=5", email, code);
        EmailRecord record = new EmailRecord();
        record.setEmail(email);
        record.setEmailType("verification");
        record.setSubject(subject);
        record.setContent(content);
        record.setCode(code);
        record.setSendStatus(1); // 先视为成功，后续接入真实网关
        record.setSendTime(LocalDateTime.now());
        record.setExpireTime(LocalDateTime.now().plusMinutes(5));
        emailRecordMapper.insert(record);
        return record;
    }
    
    private String generateCode() {
        Random random = new Random();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }
}


