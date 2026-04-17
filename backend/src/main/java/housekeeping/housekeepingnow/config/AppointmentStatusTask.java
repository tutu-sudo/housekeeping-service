package housekeeping.housekeepingnow.config;

import housekeeping.housekeepingnow.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约状态自动更新定时任务
 * 功能：定期检查预约时间，自动更新预约状态
 * - 当预约时间开始后，将状态更新为"进行中"（2）
 * - 当预约时间结束后，将状态更新为"已完成"（3）
 */
@Slf4j
@Component
public class AppointmentStatusTask {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 自动更新预约状态
     * 每5分钟执行一次
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoUpdateAppointmentStatus() {
        try {
            log.debug("开始执行预约状态自动更新任务");
            appointmentService.autoUpdateAppointmentStatus();
            log.debug("预约状态自动更新任务执行完成");
        } catch (Exception e) {
            log.error("预约状态自动更新任务执行失败", e);
        }
    }

    /**
     * 删除10分钟未支付的预约
     * 每1分钟执行一次
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void deleteUnpaidAppointments() {
        try {
            log.debug("开始执行删除未支付订单任务");
            appointmentService.deleteUnpaidAppointments();
            log.debug("删除未支付订单任务执行完成");
        } catch (Exception e) {
            log.error("删除未支付订单任务执行失败", e);
        }
    }
}

