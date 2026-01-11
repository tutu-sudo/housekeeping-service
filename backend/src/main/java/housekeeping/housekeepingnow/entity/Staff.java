package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家政服务人员实体类
 */
@Data
public class Staff {
    /**
     * 服务人员ID
     */
    private Long staffId;
    
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
     * 籍贯
     */
    private String origin;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 简历
     */
    private String resume;
    
    /**
     * 工作经验（年）
     */
    private Integer workExperience;
    
    /**
     * 时薪
     */
    private BigDecimal hourlyRate;
    
    /**
     * 综合评分（0-5分）
     */
    private BigDecimal rating;
    
    /**
     * 验证状态：0-待审核，1-审核通过，2-审核驳回
     */
    private Integer verificationStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

