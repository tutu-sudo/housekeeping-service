package housekeeping.housekeepingnow.dto;

import housekeeping.housekeepingnow.entity.Service;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import lombok.Data;

/**
 * 服务人员技能DTO（用于客户端查看）
 * 包含技能信息和服务详情
 */
@Data
public class StaffSkillDTO {
    private Long skillId;
    private Long staffId;
    private Long serviceId;
    private String serviceName; // 服务名称
    private Integer proficiencyLevel; // 熟练程度：1-初级，2-中级，3-高级
    private Integer experienceYears; // 经验年限（年）
    private String certificateUrl; // 证书URL（如果已审核通过且有证书）
    private Integer status; // 审核状态（1=已通过，前端只显示已通过的技能）
    
    /**
     * 服务详情对象（可选）
     */
    private ServiceInfo service;
    
    /**
     * 服务信息内部类
     */
    @Data
    public static class ServiceInfo {
        private Long serviceId;
        private String serviceName;
        private java.math.BigDecimal price;
        
        public static ServiceInfo from(Service service) {
            if (service == null) {
                return null;
            }
            ServiceInfo info = new ServiceInfo();
            info.setServiceId(service.getServiceId());
            info.setServiceName(service.getServiceName());
            info.setPrice(service.getPrice());
            return info;
        }
    }
    
    /**
     * 从StaffServiceSkill和Service构建DTO
     */
    public static StaffSkillDTO from(StaffServiceSkill skill, Service service) {
        StaffSkillDTO dto = new StaffSkillDTO();
        if (skill != null) {
            dto.setSkillId(skill.getSkillId());
            dto.setStaffId(skill.getStaffId());
            dto.setServiceId(skill.getServiceId());
            dto.setProficiencyLevel(skill.getProficiencyLevel());
            dto.setExperienceYears(skill.getExperienceYears());
            dto.setStatus(skill.getCertificateStatus()); // 使用certificateStatus作为status
            
            // 只有审核通过的技能才返回证书URL
            if (skill.getCertificateStatus() != null && skill.getCertificateStatus() == 1) {
                dto.setCertificateUrl(skill.getCertificateUrl());
            } else {
                dto.setCertificateUrl(null);
            }
            
            // 设置服务名称
            if (service != null) {
                dto.setServiceName(service.getServiceName());
                dto.setService(ServiceInfo.from(service));
            }
        }
        return dto;
    }
}
