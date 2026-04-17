package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.ReviewExtra;

import java.util.List;

public interface ReviewExtraService {

    ReviewExtra getById(Long reviewExtraId);

    ReviewExtra getByAppointmentIdAndRole(Long appointmentId, Integer reviewerRole);

    List<ReviewExtra> listByAppointmentId(Long appointmentId);

    List<ReviewExtra> listByReviewerUserId(Long reviewerUserId);

    ReviewExtra createReviewExtra(ReviewExtra reviewExtra);
}

