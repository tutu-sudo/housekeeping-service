package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 排班管理服务接口
 */
public interface ScheduleService {

    /**
     * 根据ID查询排班
     */
    Schedule getById(Long scheduleId);

    /**
     * 排班列表（可按人员、日期范围、状态筛选）
     */
    List<Schedule> listSchedules(Long staffId, LocalDate startDate, LocalDate endDate, Integer availableStatus);

    /**
     * 新增排班（校验时间冲突）
     */
    Schedule createSchedule(Schedule schedule);

    /**
     * 更新排班（校验时间冲突）
     */
    Schedule updateSchedule(Schedule schedule);

    /**
     * 删除排班
     */
    void deleteSchedule(Long scheduleId);

    /**
     * 校验排班时间冲突
     */
    boolean hasTimeConflict(Long staffId, LocalDate workDate, LocalTime startTime,
                            LocalTime endTime, Long excludeScheduleId);
}


