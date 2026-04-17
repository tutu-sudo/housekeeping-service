package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.*;
import housekeeping.housekeepingnow.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 后台-数据分析统计接口
 */
@Tag(name = "后台-数据分析统计接口")
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<DashboardStatisticsDTO> getDashboardStatistics() {
        return Result.success(statisticsService.getDashboardStatistics());
    }

    @Operation(summary = "按日期统计预约数据")
    @GetMapping("/appointments/date")
    public Result<List<AppointmentStatisticsDTO>> statisticsAppointmentsByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long serviceTypeId) {
        return Result.success(statisticsService.statisticsAppointmentsByDate(startDate, endDate, status, serviceTypeId));
    }

    @Operation(summary = "按服务类型统计预约数据")
    @GetMapping("/appointments/service-type")
    public Result<List<AppointmentStatisticsDTO>> statisticsAppointmentsByServiceType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.statisticsAppointmentsByServiceType(startDate, endDate));
    }

    @Operation(summary = "按状态统计预约数据")
    @GetMapping("/appointments/status")
    public Result<List<AppointmentStatisticsDTO>> statisticsAppointmentsByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.statisticsAppointmentsByStatus(startDate, endDate));
    }

    @Operation(summary = "按日期统计收入数据")
    @GetMapping("/revenue/date")
    public Result<List<RevenueStatisticsDTO>> statisticsRevenueByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String paymentMethod) {
        return Result.success(statisticsService.statisticsRevenueByDate(startDate, endDate, paymentMethod));
    }

    @Operation(summary = "按支付方式统计收入数据")
    @GetMapping("/revenue/payment-method")
    public Result<List<RevenueStatisticsDTO>> statisticsRevenueByPaymentMethod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.statisticsRevenueByPaymentMethod(startDate, endDate));
    }

    @Operation(summary = "统计服务人员工作量")
    @GetMapping("/staff/workload")
    public Result<List<StaffWorkloadStatisticsDTO>> statisticsStaffWorkload(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long staffId) {
        return Result.success(statisticsService.statisticsStaffWorkload(startDate, endDate, staffId));
    }

    @Operation(summary = "统计顾客活跃度")
    @GetMapping("/customers/activity")
    public Result<List<CustomerActivityStatisticsDTO>> statisticsCustomerActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long customerId) {
        return Result.success(statisticsService.statisticsCustomerActivity(startDate, endDate, customerId));
    }

    @Operation(summary = "统计服务质量（按服务类型）")
    @GetMapping("/quality/service-type")
    public Result<List<ServiceQualityStatisticsDTO>> statisticsServiceQualityByType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long serviceTypeId) {
        return Result.success(statisticsService.statisticsServiceQualityByType(startDate, endDate, serviceTypeId));
    }

    @Operation(summary = "统计服务质量（按服务人员）")
    @GetMapping("/quality/staff")
    public Result<List<ServiceQualityStatisticsDTO>> statisticsServiceQualityByStaff(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long staffId) {
        return Result.success(statisticsService.statisticsServiceQualityByStaff(startDate, endDate, staffId));
    }

    @Operation(summary = "获取统一统计数据（用于数据统计分析页面）", 
               description = "返回收入统计、收入趋势、订单统计、客户行为分析、客户活跃度趋势、服务类型统计、服务质量分析、评分分布等数据")
    @GetMapping
    public Result<StatisticsDataDTO> getStatisticsData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getStatisticsData(startDate, endDate));
    }
}

