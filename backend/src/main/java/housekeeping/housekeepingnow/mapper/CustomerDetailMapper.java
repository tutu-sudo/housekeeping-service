package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.CustomerDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 顾客详细信息Mapper接口
 */
@Mapper
public interface CustomerDetailMapper {
    
    /**
     * 根据顾客ID查询
     */
    CustomerDetail selectById(@Param("customerId") Long customerId);
    
    /**
     * 根据用户ID查询
     */
    CustomerDetail selectByUserId(@Param("userId") Long userId);
    
    /**
     * 插入顾客信息
     */
    int insert(CustomerDetail customerDetail);
    
    /**
     * 更新顾客信息
     */
    int update(CustomerDetail customerDetail);
}

