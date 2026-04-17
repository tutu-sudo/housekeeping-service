package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 顾客详细信息实体类
 */
@Data
public class CustomerDetail {
    /**
     * 顾客ID
     */
    private Long customerId;
    
    /**
     * 用户ID（关联user表）
     */
    private Long userId;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别：0-女，1-男
     */
    private Integer gender;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 联系偏好：phone-电话，email-邮件，sms-短信
     */
    private String contactPreference;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

