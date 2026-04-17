package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Schedule;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.service.MessageService;
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

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserMapper userMapper;

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

        // 如果是“不可服务”时间（请假），给所有管理员发送站内消息进行提醒
        if (created.getAvailableStatus() != null && created.getAvailableStatus() == 0) {
            java.util.List<User> admins = userMapper.selectAdmins();
            if (admins != null && !admins.isEmpty()) {
                String title = "服务人员请假申请提醒";
                String content = String.format("服务人员【%s】(ID:%d) 申请在 %s %s-%s 不可服务，请在人员状态监控/排班管理中关注。",
                        staff.getName(),
                        staff.getStaffId(),
                        created.getWorkDate(),
                        created.getStartTime(),
                        created.getEndTime());
                for (User admin : admins) {
                    messageService.sendMessage(admin.getUserId(), "system", title, content, created.getScheduleId());
                }
            }
        }

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

