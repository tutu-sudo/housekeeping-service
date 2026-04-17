package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘统计数据传输对象（汇总数据）
 */
@Data
public class DashboardStatisticsDTO {
    /**
     * 今日预约数
     */
    private Long todayAppointments;
    
    /**
     * 今日收入
     */
    private BigDecimal todayRevenue;
    
    /**
     * 本月预约数
     */
    private Long monthAppointments;
    
    /**
     * 本月收入
     */
    private BigDecimal monthRevenue;
    
    /**
     * 待确认预约数
     */
    private Long pendingAppointments;
    
    /**
     * 进行中预约数
     */
    private Long inProgressAppointments;
    
    /**
     * 总服务人员数
     */
    private Long totalStaff;
    
    /**
     * 总顾客数
     */
    private Long totalCustomers;
    
    /**
     * 平均评分
     */
    private BigDecimal averageRating;
    
    /**
     * 最近7天预约趋势（按日期）
     */
    private List<AppointmentStatisticsDTO> last7DaysTrend;
    
    /**
     * 最近7天收入趋势（按日期）
     */
    private List<RevenueStatisticsDTO> last7DaysRevenue;
    
    /**
     * 热门服务类型Top5
     */
    private List<AppointmentStatisticsDTO> topServiceTypes;
    
    /**
     * 服务人员工作量Top5
     */
    private List<StaffWorkloadStatisticsDTO> topStaff;
}

