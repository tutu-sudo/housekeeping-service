package housekeeping.housekeepingnow.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 后台-服务人员信息DTO（用于人员审核/人员状态管理列表与详情）
 * 聚合 staff + user(phone/email) 字段，便于前端直接展示。
 */
@Data
public class AdminStaffDTO {
    // ===== staff 表字段 =====
    private Long staffId;
    private Long userId;
    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private String origin;
    private String idCard;
    private String avatar;
    private String resume;
    private String bio;
    private String workExperienceText;
    private String professionalSkills;
    private Integer workExperience;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer workStatus;
    private Integer verificationStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== user 表字段（补齐手机号/邮箱）=====
    private String phone;
    private String email;
}

