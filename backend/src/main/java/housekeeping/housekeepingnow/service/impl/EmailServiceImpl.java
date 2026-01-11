package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.entity.EmailRecord;
import housekeeping.housekeepingnow.mapper.EmailRecordMapper;
import housekeeping.housekeepingnow.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
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


