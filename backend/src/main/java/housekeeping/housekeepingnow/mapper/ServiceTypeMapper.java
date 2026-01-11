package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.ServiceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务类型Mapper接口
 */
@Mapper
public interface ServiceTypeMapper {
    
    /**
     * 查询所有服务类型
     */
    List<ServiceType> selectAll();
    
    /**
     * 根据ID查询
     */
    ServiceType selectById(@Param("serviceTypeId") Long serviceTypeId);
    
    /**
     * 插入服务类型
     */
    int insert(ServiceType serviceType);
    
    /**
     * 更新服务类型
     */
    int update(ServiceType serviceType);
    
    /**
     * 删除服务类型
     */
    int deleteById(@Param("serviceTypeId") Long serviceTypeId);
}

