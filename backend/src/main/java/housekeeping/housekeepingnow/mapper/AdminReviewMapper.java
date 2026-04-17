package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.dto.AdminReviewSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台-评价管理专用查询 Mapper
 */
@Mapper
public interface AdminReviewMapper {

    /**
     * 顾客评价列表（顾客 -> 服务人员）
     */
    List<AdminReviewSummaryDTO> selectCustomerReviews(@Param("staffId") Long staffId,
                                                     @Param("appointmentId") Long appointmentId);

    /**
     * 员工自评列表（服务人员 -> 自己）
     */
    List<AdminReviewSummaryDTO> selectStaffSelfReviews(@Param("staffId") Long staffId,
                                                      @Param("appointmentId") Long appointmentId);
}

