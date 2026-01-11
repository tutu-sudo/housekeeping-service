package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 后台-预约/订单管理
 */
@Tag(name = "后台-预约/订单管理接口")
@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Operation(summary = "预约详情")
    @GetMapping("/{appointmentId}")
    public Result<Appointment> getDetail(@PathVariable Long appointmentId) {
        return Result.success(appointmentService.getById(appointmentId));
    }

    @Operation(summary = "预约列表")
    @GetMapping
    public Result<List<Appointment>> list(@RequestParam(required = false) Long customerId,
                                          @RequestParam(required = false) Long staffId,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(appointmentService.listAppointments(customerId, staffId, status, startDate, endDate));
    }

    @Operation(summary = "修改预约状态（审核/流转）")
    @PatchMapping("/{appointmentId}/status")
    public Result<Void> changeStatus(@PathVariable Long appointmentId,
                                     @RequestParam Integer status) {
        appointmentService.updateStatus(appointmentId, status);
        return Result.success();
    }
}


