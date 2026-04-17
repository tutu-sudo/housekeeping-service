package housekeeping.housekeepingnow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 服务人员详情DTO（用于客户端查看）
 * 包含服务人员基本信息和电子简历
 */
@Data
public class StaffDetailDTO {
    // 基本信息
    private Long staffId;
    private String name;
    private String avatar;
    private Integer gender; // 0-女，1-男
    private LocalDate birthDate;
    private Integer workExperience; // 工作经验（年）
    private String origin; // 籍贯
    private BigDecimal rating; // 综合评分
    
    // ========== 电子简历相关字段 ==========
    /**
     * 个人简介（优先使用bio，如果没有则使用resume）
     */
    private String bio;
    
    /**
     * 工作经历文本
     */
    private String workExperienceText;
    
    /**
     * 专业技能文本
     */
    private String professionalSkills;
    
    // ========== 兼容字段（前端会自动适配）==========
    /**
     * 个人简介的备用字段（兼容resume）
     */
    private String personalIntroduction;
    
    /**
     * 个人简介的备用字段（兼容resume）
     */
    private String resume;
    
    /**
     * 工作经历的备用字段（JSON字段名：work_experience_text）
     */
    @JsonProperty("work_experience_text")
    private String workExperienceTextSnake;
    
    /**
     * 专业技能的备用字段
     */
    private String skills;
    
    /**
     * 从User和Staff构建DTO
     */
    public static StaffDetailDTO from(User user, Staff staff) {
        StaffDetailDTO dto = new StaffDetailDTO();
        if (staff != null) {
            dto.setStaffId(staff.getStaffId());
            dto.setName(staff.getName());
            dto.setGender(staff.getGender());
            dto.setBirthDate(staff.getBirthDate());
            dto.setWorkExperience(staff.getWorkExperience());
            dto.setOrigin(staff.getOrigin());
            dto.setRating(staff.getRating());
            
            // 优先使用staff表的avatar
            if (staff.getAvatar() != null) {
                dto.setAvatar(staff.getAvatar());
            } else if (user != null && user.getAvatar() != null) {
                dto.setAvatar(user.getAvatar());
            }
            
            // 电子简历字段
            // 优先使用bio，如果没有则使用resume
            String bioValue = staff.getBio();
            if (bioValue == null || bioValue.trim().isEmpty()) {
                bioValue = staff.getResume();
            }
            dto.setBio(bioValue);
            dto.setPersonalIntroduction(bioValue); // 兼容字段
            dto.setResume(bioValue); // 兼容字段
            
            dto.setWorkExperienceText(staff.getWorkExperienceText());
            dto.setWorkExperienceTextSnake(staff.getWorkExperienceText()); // 兼容字段（JSON: work_experience_text）
            
            dto.setProfessionalSkills(staff.getProfessionalSkills());
            dto.setSkills(staff.getProfessionalSkills()); // 兼容字段
        }
        return dto;
    }
}
