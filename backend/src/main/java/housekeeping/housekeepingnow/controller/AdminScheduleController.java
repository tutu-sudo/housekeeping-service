package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Schedule;
import housekeeping.housekeepingnow.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 后台-排班管理
 */
@Tag(name = "后台-排班管理接口")
@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Operation(summary = "排班详情")
    @GetMapping("/{scheduleId}")
    public Result<Schedule> getById(@PathVariable Long scheduleId) {
        return Result.success(scheduleService.getById(scheduleId));
    }

    @Operation(summary = "排班列表")
    @GetMapping
    public Result<List<Schedule>> list(@RequestParam(required = false) Long staffId,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       @RequestParam(required = false) Integer availableStatus) {
        return Result.success(scheduleService.listSchedules(staffId, startDate, endDate, availableStatus));
    }

    @Operation(summary = "新增排班")
    @PostMapping
    public Result<Schedule> create(@RequestBody Schedule schedule) {
        return Result.success(scheduleService.createSchedule(schedule));
    }

    @Operation(summary = "更新排班")
    @PutMapping("/{scheduleId}")
    public Result<Schedule> update(@PathVariable Long scheduleId,
                                   @RequestBody Schedule schedule) {
        schedule.setScheduleId(scheduleId);
        return Result.success(scheduleService.updateSchedule(schedule));
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/{scheduleId}")
    public Result<Void> delete(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return Result.success();
    }
}


