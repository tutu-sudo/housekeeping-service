package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 根据预约ID查询
     */
    Appointment getById(Long appointmentId);

    /**
     * 根据顾客ID查询预约列表
     */
    List<Appointment> listByCustomerId(Long customerId);

    /**
     * 根据服务人员ID查询预约列表
     */
    List<Appointment> listByStaffId(Long staffId);

    /**
     * 创建预约（包含时间冲突校验）
     */
    Appointment createAppointment(Appointment appointment);

    /**
     * 修改预约（包含时间冲突校验）
     */
    Appointment updateAppointment(Appointment appointment);

    /**
     * 更新预约状态
     */
    void updateStatus(Long appointmentId, Integer status);

    /**
     * 标记预约已支付（仅更新 paidTime，不改变预约状态）
     */
    void markPaid(Long appointmentId);

    /**
     * 校验预约是否允许支付（状态&时间窗口）
     * 用于支付前的业务校验：仅 status=1(待付款) 且 当前时间在 acceptTime+10 分钟内 才允许支付
     */
    void validatePayable(Long appointmentId);

    /**
     * 自动更新预约状态（定时任务调用）
     * 当预约时间开始后，将状态更新为"进行中"（2）
     * 当预约时间结束后，将状态更新为"已完成"（3）
     */
    void autoUpdateAppointmentStatus();

    /**
     * 删除10分钟未支付的预约（定时任务调用）
     */
    void deleteUnpaidAppointments();

    /**
     * 检查指定时间段是否与已有预约冲突
     */
    boolean hasTimeConflict(Long staffId, LocalDate appointmentDate,
                            LocalTime startTime, LocalTime endTime, Long excludeAppointmentId);

    /**
     * 后台-预约列表查询（可按顾客/服务人员/状态/日期范围过滤）
     */
    List<Appointment> listAppointments(Long customerId, Long staffId, Integer status,
                                       LocalDate startDate, LocalDate endDate);
}


