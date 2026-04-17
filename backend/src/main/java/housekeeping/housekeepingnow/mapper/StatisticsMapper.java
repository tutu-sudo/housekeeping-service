package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计数据Mapper接口
 */
@Mapper
public interface StatisticsMapper {
    
    /**
     * 按日期统计预约数据
     */
    List<AppointmentStatisticsDTO> statisticsAppointmentsByDate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Integer status,
            @Param("serviceTypeId") Long serviceTypeId);
    
    /**
     * 按服务类型统计预约数据
     */
    List<AppointmentStatisticsDTO> statisticsAppointmentsByServiceType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 按状态统计预约数据
     */
    List<AppointmentStatisticsDTO> statisticsAppointmentsByStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 按日期统计收入数据
     */
    List<RevenueStatisticsDTO> statisticsRevenueByDate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("paymentMethod") String paymentMethod);
    
    /**
     * 按支付方式统计收入数据
     */
    List<RevenueStatisticsDTO> statisticsRevenueByPaymentMethod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 统计服务人员工作量
     */
    List<StaffWorkloadStatisticsDTO> statisticsStaffWorkload(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("staffId") Long staffId);
    
    /**
     * 统计顾客活跃度
     */
    List<CustomerActivityStatisticsDTO> statisticsCustomerActivity(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("customerId") Long customerId);
    
    /**
     * 统计服务质量（按服务类型）
     */
    List<ServiceQualityStatisticsDTO> statisticsServiceQualityByType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("serviceTypeId") Long serviceTypeId);
    
    /**
     * 统计服务质量（按服务人员）
     */
    List<ServiceQualityStatisticsDTO> statisticsServiceQualityByStaff(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("staffId") Long staffId);
    
    /**
     * 获取仪表盘统计数据
     */
    DashboardStatisticsDTO getDashboardStatistics();
    
    /**
     * 获取最近N天预约趋势
     */
    List<AppointmentStatisticsDTO> getAppointmentTrend(@Param("days") Integer days);
    
    /**
     * 获取最近N天收入趋势
     */
    List<RevenueStatisticsDTO> getRevenueTrend(@Param("days") Integer days);
    
    /**
     * 获取热门服务类型TopN
     */
    List<AppointmentStatisticsDTO> getTopServiceTypes(@Param("limit") Integer limit,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    /**
     * 获取服务人员工作量TopN
     */
    List<StaffWorkloadStatisticsDTO> getTopStaff(@Param("limit") Integer limit,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    
    /**
     * 根据用户类型统计用户数量
     */
    Long countUsersByType(@Param("userType") Integer userType);
    
    /**
     * 获取收入趋势数据（按日期）
     */
    List<java.util.Map<String, Object>> getRevenueTrendByDate(@Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);
    
    /**
     * 获取客户活跃度趋势数据（按日期）
     */
    List<java.util.Map<String, Object>> getCustomerActivityTrendByDate(@Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);
    
    /**
     * 获取服务类型统计数据
     */
    List<java.util.Map<String, Object>> getServiceTypeStats(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
    
    /**
     * 获取评分分布数据
     */
    List<java.util.Map<String, Object>> getRatingDistribution(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    // =========================
    // 统一统计页（/api/admin/statistics）专用：以 payment_time 为时间轴，且仅统计 payment_status=1（支付成功）的订单集合
    // =========================

    /**
     * 按支付方式统计支付成功收入（用于统一统计页）
     */
    List<RevenueStatisticsDTO> statisticsPaidRevenueByPaymentMethod(@Param("startDate") LocalDate startDate,
                                                                    @Param("endDate") LocalDate endDate);

    /**
     * 按日期统计支付成功收入趋势（用于统一统计页）
     */
    List<java.util.Map<String, Object>> getPaidRevenueTrendByDate(@Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    /**
     * 按预约状态统计“已支付成功订单”数量（用于统一统计页）
     */
    List<AppointmentStatisticsDTO> statisticsPaidOrdersByStatus(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    /**
     * 统计时间范围内支付成功的顾客活跃度（按顾客聚合，返回每个顾客支付成功订单数）
     */
    List<CustomerActivityStatisticsDTO> statisticsPaidCustomerActivity(@Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    /**
     * 获取支付成功客户活跃度趋势（按支付时间日期分组，顾客去重）
     */
    List<java.util.Map<String, Object>> getPaidCustomerActivityTrendByDate(@Param("startDate") LocalDate startDate,
                                                                           @Param("endDate") LocalDate endDate);

    /**
     * 获取服务类型统计数据（按支付成功订单集合聚合）
     */
    List<java.util.Map<String, Object>> getPaidServiceTypeStats(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    /**
     * 统计服务质量（按服务类型）- 仅统计支付成功订单集合（按支付时间过滤）
     */
    List<ServiceQualityStatisticsDTO> statisticsPaidServiceQualityByType(@Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate,
                                                                         @Param("serviceTypeId") Long serviceTypeId);

    /**
     * 获取评分分布数据 - 仅统计支付成功订单集合（按支付时间过滤）
     */
    List<java.util.Map<String, Object>> getPaidRatingDistribution(@Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);
}

