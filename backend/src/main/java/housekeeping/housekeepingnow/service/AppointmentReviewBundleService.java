package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.dto.AppointmentReviewBundleDTO;

/**
 * 订单三方评价聚合服务
 */
public interface AppointmentReviewBundleService {

    AppointmentReviewBundleDTO getBundleByAppointmentId(Long appointmentId);
}

