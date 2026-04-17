package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Service;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务Mapper接口
 */
@Mapper
public interface ServiceMapper {
    
    /**
     * 根据服务ID查询
     */
    Service selectById(@Param("serviceId") Long serviceId);
    
    /**
     * 根据服务类型ID查询服务列表
     */
    List<Service> selectByServiceTypeId(@Param("serviceTypeId") Long serviceTypeId);
    
    /**
     * 查询所有可用服务
     */
    List<Service> selectAvailableServices();
    
    /**
     * 插入服务
     */
    int insert(Service service);

    /**
     * 查询服务列表（后台管理可见全部）
     */
    List<Service> selectList(@Param("serviceTypeId") Long serviceTypeId,
                             @Param("mainCategory") String mainCategory,
                             @Param("availableStatus") Integer availableStatus);
    
    /**
     * 根据大类查询可用服务（前台使用）
     */
    List<Service> selectByMainCategory(@Param("mainCategory") String mainCategory);
    
    /**
     * 更新服务
     */
    int update(Service service);
    
    /**
     * 删除服务
     */
    int deleteById(@Param("serviceId") Long serviceId);
}

