package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.StaffServiceSkill;

import java.util.List;

/**
 * 服务技能服务接口
 */
public interface StaffServiceSkillService {
    
    /**
     * 根据服务人员ID查询技能列表
     */
    List<StaffServiceSkill> getByStaffId(Long staffId);
    
    /**
     * 添加服务技能
     */
    StaffServiceSkill addSkill(StaffServiceSkill skill);
    
    /**
     * 更新服务技能
     */
    StaffServiceSkill updateSkill(StaffServiceSkill skill);
    
    /**
     * 删除服务技能
     */
    void deleteSkill(Long skillId);
    
    /**
     * 根据技能ID查询
     */
    StaffServiceSkill getById(Long skillId);
}

