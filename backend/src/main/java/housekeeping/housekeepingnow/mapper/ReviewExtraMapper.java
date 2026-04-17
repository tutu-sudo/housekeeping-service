package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.ReviewExtra;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewExtraMapper {

    ReviewExtra selectById(@Param("reviewExtraId") Long reviewExtraId);

    ReviewExtra selectByAppointmentIdAndRoleAndTarget(@Param("appointmentId") Long appointmentId,
                                                      @Param("reviewerRole") Integer reviewerRole,
                                                      @Param("reviewTarget") Integer reviewTarget);

    List<ReviewExtra> selectByAppointmentId(@Param("appointmentId") Long appointmentId);

    List<ReviewExtra> selectByReviewerUserId(@Param("reviewerUserId") Long reviewerUserId);

    int insert(ReviewExtra reviewExtra);
}

