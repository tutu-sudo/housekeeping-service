package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务人员Mapper接口
 */
@Mapper
public interface StaffMapper {
    
    /**
     * 根据服务人员ID查询
     */
    Staff selectById(@Param("staffId") Long staffId);
    
    /**
     * 根据用户ID查询
     */
    Staff selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询服务人员列表（支持筛选）
     */
    List<Staff> selectList(@Param("gender") Integer gender,
                          @Param("origin") String origin,
                          @Param("minRating") java.math.BigDecimal minRating,
                          @Param("minWorkExperience") Integer minWorkExperience,
                          @Param("verificationStatus") Integer verificationStatus);
    
    /**
     * 插入服务人员信息
     */
    int insert(Staff staff);
    
    /**
     * 更新服务人员信息
     */
    int update(Staff staff);
    
    /**
     * 更新验证状态
     */
    int updateVerificationStatus(@Param("staffId") Long staffId, 
                                 @Param("verificationStatus") Integer verificationStatus);
}

