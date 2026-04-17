package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.dto.AppointmentReviewBundleDTO;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.Review;
import housekeeping.housekeepingnow.entity.ReviewExtra;
import housekeeping.housekeepingnow.mapper.AppointmentMapper;
import housekeeping.housekeepingnow.mapper.ReviewExtraMapper;
import housekeeping.housekeepingnow.mapper.ReviewMapper;
import housekeeping.housekeepingnow.service.AppointmentReviewBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentReviewBundleServiceImpl implements AppointmentReviewBundleService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewExtraMapper reviewExtraMapper;

    @Override
    public AppointmentReviewBundleDTO getBundleByAppointmentId(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        AppointmentReviewBundleDTO dto = new AppointmentReviewBundleDTO();
        dto.setAppointmentId(appointmentId);

        // 顾客评价（可能为空）
        Review customer = reviewMapper.selectByAppointmentId(appointmentId);
        dto.setCustomerReview(customer);

        // 服务人员自评（reviewerRole=2，reviewTarget=2，可能为空）
        ReviewExtra staffSelf = reviewExtraMapper.selectByAppointmentIdAndRoleAndTarget(appointmentId, 2, 2);
        dto.setStaffSelfReview(staffSelf);

        // 管理员对顾客的评价（reviewerRole=3, reviewTarget=1，可能为空）
        ReviewExtra adminCustomer = reviewExtraMapper.selectByAppointmentIdAndRoleAndTarget(appointmentId, 3, 1);
        dto.setAdminReviewForCustomer(adminCustomer);

        // 管理员对服务人员的评价（reviewerRole=3, reviewTarget=2，可能为空）
        ReviewExtra adminStaff = reviewExtraMapper.selectByAppointmentIdAndRoleAndTarget(appointmentId, 3, 2);
        dto.setAdminReviewForStaff(adminStaff);

        return dto;
    }
}

