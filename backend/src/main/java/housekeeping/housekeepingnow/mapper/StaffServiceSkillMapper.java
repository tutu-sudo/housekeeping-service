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

    /**
     * 根据服务人员ID删除全部技能关联（用于保持“一人一服务”场景）
     */
    int deleteByStaffId(@Param("staffId") Long staffId);

    /**
     * 按证书审核状态查询技能列表（用于管理员审核）
     */
    List<StaffServiceSkill> selectByCertificateStatus(@Param("certificateStatus") Integer certificateStatus,
                                                      @Param("staffId") Long staffId);

    /**
     * 更新证书审核状态
     */
    int updateCertificateStatus(@Param("skillId") Long skillId,
                                @Param("certificateStatus") Integer certificateStatus);
}

