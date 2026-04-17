package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.entity.SmsRecord;
import housekeeping.housekeepingnow.mapper.SmsRecordMapper;
import housekeeping.housekeepingnow.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SmsRecordMapper smsRecordMapper;

    @Override
    public SmsRecord sendVerificationCode(String phone) {
        String code = generateCode();
        String content = "您的验证码为：" + code + "，有效期5分钟。";
        // 注意：当前为“模拟短信发送”实现，仅写库不发真实短信；联调阶段可从日志/数据库获取验证码
        log.debug("【联调】短信验证码已生成（未真实发送），phone={}, code={}, expireInMinutes=5", phone, code);
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


