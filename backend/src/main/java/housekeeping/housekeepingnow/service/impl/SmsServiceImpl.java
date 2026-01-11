package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.entity.SmsRecord;
import housekeeping.housekeepingnow.mapper.SmsRecordMapper;
import housekeeping.housekeepingnow.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SmsRecordMapper smsRecordMapper;

    @Override
    public SmsRecord sendVerificationCode(String phone) {
        String code = generateCode();
        String content = "您的验证码为：" + code + "，有效期5分钟。";
        SmsRecord record = new SmsRecord();
        record.setPhone(phone);
        record.setSmsType("verification");
        record.setContent(content);
        record.setCode(code);
        record.setSendStatus(1); // 先视为成功，后续接入真实网关时再调整
        record.setSendTime(LocalDateTime.now());
        record.setExpireTime(LocalDateTime.now().plusMinutes(5));
        smsRecordMapper.insert(record);
        return record;
    }

    @Override
    public SmsRecord sendSms(String phone, String type, String content) {
        SmsRecord record = new SmsRecord();
        record.setPhone(phone);
        record.setSmsType(type);
        record.setContent(content);
        record.setSendStatus(1);
        record.setSendTime(LocalDateTime.now());
        smsRecordMapper.insert(record);
        return record;
    }

    private String generateCode() {
        Random random = new Random();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }
}


