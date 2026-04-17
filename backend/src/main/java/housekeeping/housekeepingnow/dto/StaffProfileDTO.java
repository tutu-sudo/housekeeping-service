package housekeeping.housekeepingnow.dto;

import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 服务人员个人资料DTO
 * 包含用户信息和服务人员详细信息
 */
@Data
public class StaffProfileDTO {
    // 用户基本信息
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String avatar; // 用户头像（优先使用staff表的avatar）
    
    // 服务人员详细信息
    private Long staffId;
    private String name;
    private Integer gender; // 0-女，1-男
    private LocalDate birthDate;
    private String origin; // 籍贯
    private String idCard; // 身份证号
    private String resume; // 简历（兼容字段）
    
    // 电子简历字段
    private String bio; // 个人简介（电子简历）
    private String workExperienceText; // 工作经历文本（电子简历）
    private String professionalSkills; // 专业技能文本（电子简历）
    
    private Integer workExperience; // 工作经验（年）
    private BigDecimal hourlyRate; // 时薪
    private BigDecimal rating; // 综合评分
    private Integer verificationStatus; // 验证状态：0-待审核，1-审核通过，2-审核驳回
    /**
     * 详情页选择的具体服务ID（service_id，一人一服务）
     */
    private Long serviceId;
    
    /**
     * 从User和Staff构建DTO
     */
    public static StaffProfileDTO from(User user, Staff staff) {
        StaffProfileDTO dto = new StaffProfileDTO();
        if (user != null) {
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setAvatar(user.getAvatar());
        }
        if (staff != null) {
            dto.setStaffId(staff.getStaffId());
            dto.setName(staff.getName());
            dto.setGender(staff.getGender());
            dto.setBirthDate(staff.getBirthDate());
            dto.setOrigin(staff.getOrigin());
            dto.setIdCard(staff.getIdCard());
            dto.setResume(staff.getResume());
            dto.setBio(staff.getBio());
            dto.setWorkExperienceText(staff.getWorkExperienceText());
            dto.setProfessionalSkills(staff.getProfessionalSkills());
            dto.setWorkExperience(staff.getWorkExperience());
            dto.setHourlyRate(staff.getHourlyRate());
            dto.setRating(staff.getRating());
            dto.setVerificationStatus(staff.getVerificationStatus());
            // 优先使用staff表的avatar
            if (staff.getAvatar() != null) {
                dto.setAvatar(staff.getAvatar());
            }
        }
        return dto;
    }
}

