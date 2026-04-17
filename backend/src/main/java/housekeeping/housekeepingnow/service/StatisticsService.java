package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计数据服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取仪表盘统计数据
     */
    DashboardStatisticsDTO getDashboardStatistics();
    
    /**
     * 按日期统计预约数据
     */
    List<AppointmentStatisticsDTO> statisticsAppointmentsByDate(LocalDate startDate, 
                                                                  LocalDate endDate,
                                                                  Integer status,
                                                                  Long serviceTypeId);
    
    /**
     * 按服务类型统计预约数据
     */
    List<AppointmentStatisticsDTO> statisticsAppointmentsByServiceType(LocalDate startDate, 
                                                                       LocalDate endDate);
    
    /**
     * 按状态统计预约数据
     */
    List<AppointmentStatisticsDTO> statisticsAppointmentsByStatus(LocalDate startDate, 
                                                                  LocalDate endDate);
    
    /**
     * 按日期统计收入数据
     */
    List<RevenueStatisticsDTO> statisticsRevenueByDate(LocalDate startDate, 
                                                        LocalDate endDate,
                                                        String paymentMethod);
    
    /**
     * 按支付方式统计收入数据
     */
    List<RevenueStatisticsDTO> statisticsRevenueByPaymentMethod(LocalDate startDate, 
                                                                  LocalDate endDate);
    
    /**
     * 统计服务人员工作量
     */
    List<StaffWorkloadStatisticsDTO> statisticsStaffWorkload(LocalDate startDate, 
                                                              LocalDate endDate,
                                                              Long staffId);
    
    /**
     * 统计顾客活跃度
     */
    List<CustomerActivityStatisticsDTO> statisticsCustomerActivity(LocalDate startDate, 
                                                                  LocalDate endDate,
                                                                  Long customerId);
    
    /**
     * 统计服务质量（按服务类型）
     */
    List<ServiceQualityStatisticsDTO> statisticsServiceQualityByType(LocalDate startDate, 
                                                                     LocalDate endDate,
                                                                     Long serviceTypeId);
    
    /**
     * 统计服务质量（按服务人员）
     */
    List<ServiceQualityStatisticsDTO> statisticsServiceQualityByStaff(LocalDate startDate, 
                                                                       LocalDate endDate,
                                                                       Long staffId);
    
    /**
     * 获取统一统计数据（用于数据统计分析页面）
     */
    StatisticsDataDTO getStatisticsData(LocalDate startDate, LocalDate endDate);
}

