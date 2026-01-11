package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Schedule;
import housekeeping.housekeepingnow.service.ScheduleService;
import housekeeping.housekeepingnow.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 服务人员日程管理接口控制器
 */
@Tag(name = "服务人员日程管理接口", description = "服务人员可服务时间管理接口")
@RestController
@RequestMapping("/api/staff/schedules")
@RequireAuth
@RequireRole({2}) // 仅服务人员可访问
public class StaffScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private StaffService staffService;

    @Operation(summary = "获取当前登录服务人员的日程列表")
    @GetMapping
    public Result<List<Schedule>> listSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer availableStatus,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        List<Schedule> schedules = scheduleService.listSchedules(staff.getStaffId(), startDate, endDate, availableStatus);
        return Result.success(schedules);
    }

    @Operation(summary = "获取日程详情")
    @GetMapping("/{scheduleId}")
    public Result<Schedule> getById(@PathVariable Long scheduleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        Schedule schedule = scheduleService.getById(scheduleId);
        // 验证日程属于当前服务人员
        if (!schedule.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        return Result.success(schedule);
    }

    @Operation(summary = "添加可服务时间")
    @PostMapping
    public Result<Schedule> createSchedule(@RequestBody Schedule schedule, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        schedule.setStaffId(staff.getStaffId());
        Schedule created = scheduleService.createSchedule(schedule);
        return Result.success(created);
    }

    @Operation(summary = "更新可服务时间")
    @PutMapping("/{scheduleId}")
    public Result<Schedule> updateSchedule(@PathVariable Long scheduleId,
                                            @RequestBody Schedule schedule,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        // 验证日程属于当前服务人员
        Schedule existing = scheduleService.getById(scheduleId);
        if (!existing.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        schedule.setScheduleId(scheduleId);
        schedule.setStaffId(staff.getStaffId());
        Schedule updated = scheduleService.updateSchedule(schedule);
        return Result.success(updated);
    }

    @Operation(summary = "删除可服务时间")
    @DeleteMapping("/{scheduleId}")
    public Result<Void> deleteSchedule(@PathVariable Long scheduleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        // 验证日程属于当前服务人员
        Schedule existing = scheduleService.getById(scheduleId);
        if (!existing.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        scheduleService.deleteSchedule(scheduleId);
        return Result.success();
    }
}

