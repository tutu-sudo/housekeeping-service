package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Schedule;
import housekeeping.housekeepingnow.mapper.ScheduleMapper;
import housekeeping.housekeepingnow.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public Schedule getById(Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }
        return schedule;
    }

    @Override
    public List<Schedule> listSchedules(Long staffId, LocalDate startDate, LocalDate endDate, Integer availableStatus) {
        return scheduleMapper.selectList(staffId, startDate, endDate, availableStatus);
    }

    @Override
    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        validateTimeRange(schedule.getStartTime(), schedule.getEndTime());
        if (schedule.getStaffId() == null || schedule.getWorkDate() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (schedule.getAvailableStatus() == null) {
            schedule.setAvailableStatus(1);
        }
        if (hasTimeConflict(schedule.getStaffId(), schedule.getWorkDate(),
                schedule.getStartTime(), schedule.getEndTime(), null)) {
            throw new BusinessException(ResultCode.SCHEDULE_TIME_CONFLICT);
        }
        scheduleMapper.insert(schedule);
        return scheduleMapper.selectById(schedule.getScheduleId());
    }

    @Override
    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        Schedule db = scheduleMapper.selectById(schedule.getScheduleId());
        if (db == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }

        LocalDate workDate = schedule.getWorkDate() != null ? schedule.getWorkDate() : db.getWorkDate();
        LocalTime start = schedule.getStartTime() != null ? schedule.getStartTime() : db.getStartTime();
        LocalTime end = schedule.getEndTime() != null ? schedule.getEndTime() : db.getEndTime();
        Long staffId = schedule.getStaffId() != null ? schedule.getStaffId() : db.getStaffId();

        if (staffId == null || workDate == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        validateTimeRange(start, end);

        if (hasTimeConflict(staffId, workDate, start, end, db.getScheduleId())) {
            throw new BusinessException(ResultCode.SCHEDULE_TIME_CONFLICT);
        }

        scheduleMapper.update(schedule);
        return scheduleMapper.selectById(db.getScheduleId());
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule db = scheduleMapper.selectById(scheduleId);
        if (db == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }
        scheduleMapper.deleteById(scheduleId);
    }

    @Override
    public boolean hasTimeConflict(Long staffId, LocalDate workDate, LocalTime startTime, LocalTime endTime, Long excludeScheduleId) {
        List<Schedule> conflicts = scheduleMapper.checkTimeConflict(staffId, workDate, startTime, endTime, excludeScheduleId);
        return conflicts != null && !conflicts.isEmpty();
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessException(ResultCode.SCHEDULE_TIME_CONFLICT);
        }
    }
}


