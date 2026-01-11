package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价Mapper接口
 */
@Mapper
public interface ReviewMapper {
    
    /**
     * 根据评价ID查询
     */
    Review selectById(@Param("reviewId") Long reviewId);
    
    /**
     * 根据预约ID查询评价
     */
    Review selectByAppointmentId(@Param("appointmentId") Long appointmentId);
    
    /**
     * 根据服务人员ID查询评价列表
     */
    List<Review> selectByStaffId(@Param("staffId") Long staffId);
    
    /**
     * 根据顾客ID查询评价列表（通过预约关联查询）
     */
    List<Review> selectByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 插入评价
     */
    int insert(Review review);
    
    /**
     * 更新评价
     */
    int update(Review review);

    /**
     * 评价列表查询（后台）
     */
    java.util.List<Review> selectList(@Param("staffId") Long staffId,
                                      @Param("appointmentId") Long appointmentId);
}

