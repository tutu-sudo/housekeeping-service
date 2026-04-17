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
     * 个人简介（电子简历）
     */
    private String bio;
    
    /**
     * 工作经历文本（电子简历）
     */
    private String workExperienceText;
    
    /**
     * 专业技能文本（电子简历）
     */
    private String professionalSkills;
    
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
     * 工作状态：0-不可服务，1-正常，2-警告，3-黑名单
     */
    private Integer workStatus;
    
    /**
     * 验证状态：0-待审核，1-审核通过，2-审核驳回
     */
    private Integer verificationStatus;
    
    /**
     * 预约筛选结果中关联的主服务ID（非持久化字段，用于前端联动服务项目）
     */
    private Long serviceId;

    /**
     * 预约筛选结果中关联的主服务名称（非持久化字段，用于前端联动服务项目）
     */
    private String serviceName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

