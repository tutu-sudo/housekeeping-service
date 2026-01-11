package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务技能关联Mapper接口
 */
@Mapper
public interface StaffServiceSkillMapper {
    
    /**
     * 根据服务人员ID查询技能列表
     */
    List<StaffServiceSkill> selectByStaffId(@Param("staffId") Long staffId);
    
    /**
     * 根据服务ID查询服务人员列表
     */
    List<StaffServiceSkill> selectByServiceId(@Param("serviceId") Long serviceId);
    
    /**
     * 根据服务人员ID和服务ID查询
     */
    StaffServiceSkill selectByStaffIdAndServiceId(@Param("staffId") Long staffId, 
                                                   @Param("serviceId") Long serviceId);
    
    /**
     * 插入技能关联
     */
    int insert(StaffServiceSkill skill);
    
    /**
     * 更新技能关联
     */
    int update(StaffServiceSkill skill);
    
    /**
     * 根据技能ID查询
     */
    StaffServiceSkill selectById(@Param("skillId") Long skillId);
    
    /**
     * 删除技能关联
     */
    int deleteById(@Param("skillId") Long skillId);
}

