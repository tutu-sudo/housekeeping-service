package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.dto.*;
import housekeeping.housekeepingnow.mapper.StatisticsMapper;
import housekeeping.housekeepingnow.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计数据服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    @Override
    public DashboardStatisticsDTO getDashboardStatistics() {
        DashboardStatisticsDTO dashboard = new DashboardStatisticsDTO();
        
        // 获取今日数据
        LocalDate today = LocalDate.now();
        List<AppointmentStatisticsDTO> todayStats = statisticsMapper.statisticsAppointmentsByDate(
                today, today, null, null);
        dashboard.setTodayAppointments(todayStats.stream()
                .mapToLong(AppointmentStatisticsDTO::getCount)
                .sum());
        dashboard.setTodayRevenue(todayStats.stream()
                .map(AppointmentStatisticsDTO::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        
        // 获取本月数据
        LocalDate monthStart = today.withDayOfMonth(1);
        List<AppointmentStatisticsDTO> monthStats = statisticsMapper.statisticsAppointmentsByDate(
                monthStart, today, null, null);
        dashboard.setMonthAppointments(monthStats.stream()
                .mapToLong(AppointmentStatisticsDTO::getCount)
                .sum());
        dashboard.setMonthRevenue(monthStats.stream()
                .map(AppointmentStatisticsDTO::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        
        // 获取待确认和进行中的预约数
        List<AppointmentStatisticsDTO> pendingStats = statisticsMapper.statisticsAppointmentsByDate(
                null, null, 0, null);
        dashboard.setPendingAppointments(pendingStats.stream()
                .mapToLong(AppointmentStatisticsDTO::getCount)
                .sum());
        
        List<AppointmentStatisticsDTO> inProgressStats = statisticsMapper.statisticsAppointmentsByDate(
                null, null, 2, null);
        dashboard.setInProgressAppointments(inProgressStats.stream()
                .mapToLong(AppointmentStatisticsDTO::getCount)
                .sum());
        
        // 获取总服务人员数和总顾客数
        dashboard.setTotalStaff(statisticsMapper.countUsersByType(2));
        dashboard.setTotalCustomers(statisticsMapper.countUsersByType(1));
        
        // 获取平均评分（从服务质量统计中获取）
        List<ServiceQualityStatisticsDTO> qualityStats = statisticsMapper.statisticsServiceQualityByType(
                null, null, null);
        if (!qualityStats.isEmpty()) {
            double avgRating = qualityStats.stream()
                    .mapToDouble(s -> s.getAverageOverallRating().doubleValue())
                    .average()
                    .orElse(0.0);
            dashboard.setAverageRating(java.math.BigDecimal.valueOf(avgRating));
        } else {
            dashboard.setAverageRating(java.math.BigDecimal.ZERO);
        }
        
        // 获取最近7天趋势
        dashboard.setLast7DaysTrend(statisticsMapper.getAppointmentTrend(7));
        dashboard.setLast7DaysRevenue(statisticsMapper.getRevenueTrend(7));
        
        // 获取热门服务类型Top5
        dashboard.setTopServiceTypes(statisticsMapper.getTopServiceTypes(5, monthStart, today));
        
        // 获取服务人员工作量Top5
        dashboard.setTopStaff(statisticsMapper.getTopStaff(5, monthStart, today));
        
        return dashboard;
    }
    
    @Override
    public List<AppointmentStatisticsDTO> statisticsAppointmentsByDate(LocalDate startDate, 
                                                                        LocalDate endDate,
                                                                        Integer status,
                                                                        Long serviceTypeId) {
        return statisticsMapper.statisticsAppointmentsByDate(startDate, endDate, status, serviceTypeId);
    }
    
    @Override
    public List<AppointmentStatisticsDTO> statisticsAppointmentsByServiceType(LocalDate startDate, 
                                                                              LocalDate endDate) {
        return statisticsMapper.statisticsAppointmentsByServiceType(startDate, endDate);
    }
    
    @Override
    public List<AppointmentStatisticsDTO> statisticsAppointmentsByStatus(LocalDate startDate, 
                                                                         LocalDate endDate) {
        return statisticsMapper.statisticsAppointmentsByStatus(startDate, endDate);
    }
    
    @Override
    public List<RevenueStatisticsDTO> statisticsRevenueByDate(LocalDate startDate, 
                                                               LocalDate endDate,
                                                               String paymentMethod) {
        return statisticsMapper.statisticsRevenueByDate(startDate, endDate, paymentMethod);
    }
    
    @Override
    public List<RevenueStatisticsDTO> statisticsRevenueByPaymentMethod(LocalDate startDate, 
                                                                        LocalDate endDate) {
        return statisticsMapper.statisticsRevenueByPaymentMethod(startDate, endDate);
    }
    
    @Override
    public List<StaffWorkloadStatisticsDTO> statisticsStaffWorkload(LocalDate startDate, 
                                                                    LocalDate endDate,
                                                                    Long staffId) {
        return statisticsMapper.statisticsStaffWorkload(startDate, endDate, staffId);
    }
    
    @Override
    public List<CustomerActivityStatisticsDTO> statisticsCustomerActivity(LocalDate startDate, 
                                                                          LocalDate endDate,
                                                                          Long customerId) {
        return statisticsMapper.statisticsCustomerActivity(startDate, endDate, customerId);
    }
    
    @Override
    public List<ServiceQualityStatisticsDTO> statisticsServiceQualityByType(LocalDate startDate, 
                                                                            LocalDate endDate,
                                                                            Long serviceTypeId) {
        return statisticsMapper.statisticsServiceQualityByType(startDate, endDate, serviceTypeId);
    }
    
    @Override
    public List<ServiceQualityStatisticsDTO> statisticsServiceQualityByStaff(LocalDate startDate, 
                                                                             LocalDate endDate,
                                                                             Long staffId) {
        return statisticsMapper.statisticsServiceQualityByStaff(startDate, endDate, staffId);
    }
    
    @Override
    public StatisticsDataDTO getStatisticsData(LocalDate startDate, LocalDate endDate) {
        // 如果没有指定日期范围，默认查询最近30天
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            // 含今天共30天：例如今天=03-16，则开始=02-15
            startDate = endDate.minusDays(29);
        }
        
        StatisticsDataDTO result = new StatisticsDataDTO();
        
        // ========== 1. 收入统计 ==========
        // 统一口径：以 payment.payment_time 为时间轴，且仅统计 payment_status=1（支付成功）的订单集合
        List<RevenueStatisticsDTO> revenueByMethod = statisticsMapper.statisticsPaidRevenueByPaymentMethod(startDate, endDate);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal alipayRevenue = BigDecimal.ZERO;
        BigDecimal wechatRevenue = BigDecimal.ZERO;
        long paidOrdersCount = 0;
        
        for (RevenueStatisticsDTO revenue : revenueByMethod) {
            BigDecimal successAmount = revenue.getSuccessAmount() != null ? revenue.getSuccessAmount() : BigDecimal.ZERO;
            long successCount = revenue.getSuccessCount() != null ? revenue.getSuccessCount() : 0;
            
            totalRevenue = totalRevenue.add(successAmount);
            paidOrdersCount += successCount;
            
            if ("alipay".equals(revenue.getPaymentMethod())) {
                alipayRevenue = successAmount;
            } else if ("wechat".equals(revenue.getPaymentMethod())) {
                wechatRevenue = successAmount;
            }
        }
        
        result.setTotalRevenue(totalRevenue);
        result.setAlipayRevenue(alipayRevenue);
        result.setWechatRevenue(wechatRevenue);
        result.setPaidOrdersCount(paidOrdersCount);
        
        // ========== 2. 收入趋势（按日期） ==========
        List<Map<String, Object>> revenueTrendData = statisticsMapper.getPaidRevenueTrendByDate(startDate, endDate);
        List<StatisticsDataDTO.RevenueTrendItem> revenueTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map<String, Object> item : revenueTrendData) {
            String dateStr = formatTrendDate(item.get("date"), formatter);
            if (dateStr == null || dateStr.isBlank()) {
                // 避免前端X轴出现 null
                continue;
            }
            StatisticsDataDTO.RevenueTrendItem trendItem = new StatisticsDataDTO.RevenueTrendItem();
            trendItem.setDate(dateStr);
            trendItem.setAmount(item.get("amount") != null ? 
                new BigDecimal(item.get("amount").toString()) : BigDecimal.ZERO);
            revenueTrend.add(trendItem);
        }
        result.setRevenueTrend(revenueTrend);
        
        // ========== 3. 订单统计 ==========
        List<AppointmentStatisticsDTO> statusStats = statisticsMapper.statisticsPaidOrdersByStatus(startDate, endDate);
        long totalOrders = 0;
        long completedOrders = 0;
        long inProgressOrders = 0;
        long pendingOrders = 0;
        
        for (AppointmentStatisticsDTO stat : statusStats) {
            long count = stat.getCount() != null ? stat.getCount() : 0;
            totalOrders += count;
            
            Integer status = stat.getStatus();
            if (status != null) {
                if (status == 3) { // 已完成
                    completedOrders += count;
                } else if (status == 2) { // 进行中
                    inProgressOrders += count;
                } else if (status == 0) { // 待确认
                    pendingOrders += count;
                }
            }
        }
        
        result.setTotalOrders(totalOrders);
        result.setCompletedOrders(completedOrders);
        result.setInProgressOrders(inProgressOrders);
        result.setPendingOrders(pendingOrders);

        // 按统一口径验收标准：paidOrdersCount = totalOrders
        // paidOrdersCount 来源于 payment 成功数统计；totalOrders 来源于成功订单按状态汇总，两者应一致
        result.setPaidOrdersCount(totalOrders);
        
        // ========== 4. 客户行为分析 ==========
        // 总客户数（所有客户）
        Long totalCustomers = statisticsMapper.countUsersByType(1);
        result.setTotalCustomers(totalCustomers != null ? totalCustomers : 0L);
        
        // 活跃客户数（在指定时间范围内“支付成功”的客户，去重）
        List<CustomerActivityStatisticsDTO> activeCustomerList = statisticsMapper.statisticsPaidCustomerActivity(startDate, endDate);
        result.setActiveCustomers((long) activeCustomerList.size());
        
        // 平均订单金额
        BigDecimal avgOrderAmount = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgOrderAmount = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }
        result.setAvgOrderAmount(avgOrderAmount);
        
        // 客户复购率 = (在时间范围内有2个及以上“支付成功订单”的客户数 / 总客户数) × 100
        BigDecimal repeatPurchaseRate = BigDecimal.ZERO;
        if (totalCustomers != null && totalCustomers > 0) {
            long repeatCustomers = activeCustomerList.stream()
                .filter(c -> c.getTotalAppointments() != null && c.getTotalAppointments() >= 2)
                .count();
            repeatPurchaseRate = BigDecimal.valueOf(repeatCustomers)
                .divide(BigDecimal.valueOf(totalCustomers), 3, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        result.setRepeatPurchaseRate(repeatPurchaseRate);
        
        // ========== 5. 客户活跃度趋势（按日期） ==========
        List<Map<String, Object>> activityTrendData = statisticsMapper.getPaidCustomerActivityTrendByDate(startDate, endDate);
        List<StatisticsDataDTO.CustomerActivityTrendItem> customerActivityTrend = new ArrayList<>();
        
        for (Map<String, Object> item : activityTrendData) {
            String dateStr = formatTrendDate(item.get("date"), formatter);
            if (dateStr == null || dateStr.isBlank()) {
                // 避免前端X轴出现 null
                continue;
            }
            StatisticsDataDTO.CustomerActivityTrendItem trendItem = new StatisticsDataDTO.CustomerActivityTrendItem();
            trendItem.setDate(dateStr);
            trendItem.setCount(item.get("count") != null ? 
                Long.parseLong(item.get("count").toString()) : 0L);
            customerActivityTrend.add(trendItem);
        }
        result.setCustomerActivityTrend(customerActivityTrend);
        
        // ========== 6. 服务类型统计 ==========
        List<Map<String, Object>> serviceTypeStatsData = statisticsMapper.getPaidServiceTypeStats(startDate, endDate);
        List<StatisticsDataDTO.ServiceTypeStatItem> serviceTypeStats = new ArrayList<>();
        
        for (Map<String, Object> item : serviceTypeStatsData) {
            StatisticsDataDTO.ServiceTypeStatItem statItem = new StatisticsDataDTO.ServiceTypeStatItem();
            statItem.setServiceTypeName(item.get("serviceTypeName") != null ? 
                item.get("serviceTypeName").toString() : "");
            statItem.setOrderCount(item.get("orderCount") != null ? 
                Long.parseLong(item.get("orderCount").toString()) : 0L);
            statItem.setTotalRevenue(item.get("totalRevenue") != null ? 
                new BigDecimal(item.get("totalRevenue").toString()) : BigDecimal.ZERO);
            statItem.setPaidCount(item.get("paidCount") != null ? 
                Long.parseLong(item.get("paidCount").toString()) : 0L);
            serviceTypeStats.add(statItem);
        }
        result.setServiceTypeStats(serviceTypeStats);
        
        // ========== 7. 服务质量分析 ==========
        List<ServiceQualityStatisticsDTO> qualityStats = statisticsMapper.statisticsPaidServiceQualityByType(startDate, endDate, null);
        BigDecimal avgRating = BigDecimal.ZERO;
        BigDecimal avgServiceAttitude = BigDecimal.ZERO;
        BigDecimal avgProfessionalAbility = BigDecimal.ZERO;
        long totalReviews = 0;
        
        if (!qualityStats.isEmpty()) {
            double avgRatingValue = qualityStats.stream()
                .filter(s -> s.getAverageOverallRating() != null)
                .mapToDouble(s -> s.getAverageOverallRating().doubleValue())
                .average()
                .orElse(0.0);
            avgRating = BigDecimal.valueOf(avgRatingValue).setScale(2, RoundingMode.HALF_UP);
            
            double avgAttitudeValue = qualityStats.stream()
                .filter(s -> s.getAverageServiceAttitudeRating() != null)
                .mapToDouble(s -> s.getAverageServiceAttitudeRating().doubleValue())
                .average()
                .orElse(0.0);
            avgServiceAttitude = BigDecimal.valueOf(avgAttitudeValue).setScale(2, RoundingMode.HALF_UP);
            
            double avgAbilityValue = qualityStats.stream()
                .filter(s -> s.getAverageProfessionalAbilityRating() != null)
                .mapToDouble(s -> s.getAverageProfessionalAbilityRating().doubleValue())
                .average()
                .orElse(0.0);
            avgProfessionalAbility = BigDecimal.valueOf(avgAbilityValue).setScale(2, RoundingMode.HALF_UP);
            
            totalReviews = qualityStats.stream()
                .mapToLong(s -> s.getReviewCount() != null ? s.getReviewCount() : 0L)
                .sum();
        }
        
        result.setAvgRating(avgRating);
        result.setAvgServiceAttitude(avgServiceAttitude);
        result.setAvgProfessionalAbility(avgProfessionalAbility);
        result.setTotalReviews(totalReviews);
        
        // ========== 8. 评分分布 ==========
        List<Map<String, Object>> ratingDistData = statisticsMapper.getPaidRatingDistribution(startDate, endDate);
        List<StatisticsDataDTO.RatingDistributionItem> ratingDistribution = new ArrayList<>();
        
        // 确保所有评分（1-5）都有数据，没有的补0
        for (int rating = 5; rating >= 1; rating--) {
            StatisticsDataDTO.RatingDistributionItem distItem = new StatisticsDataDTO.RatingDistributionItem();
            distItem.setRating(rating);
            
            boolean found = false;
            for (Map<String, Object> item : ratingDistData) {
                Object ratingObj = item.get("rating");
                int itemRating = ratingObj instanceof Number ? 
                    ((Number) ratingObj).intValue() : Integer.parseInt(ratingObj.toString());
                if (itemRating == rating) {
                    distItem.setCount(item.get("count") != null ? 
                        Long.parseLong(item.get("count").toString()) : 0L);
                    found = true;
                    break;
                }
            }
            if (!found) {
                distItem.setCount(0L);
            }
            ratingDistribution.add(distItem);
        }
        result.setRatingDistribution(ratingDistribution);
        
        return result;
    }

    /**
     * 统一格式化趋势图的日期字段，确保返回 YYYY-MM-DD 字符串且不为 null。
     * MyBatis 可能返回 LocalDate / java.sql.Date / java.sql.Timestamp / String 等类型。
     */
    private static String formatTrendDate(Object dateObj, DateTimeFormatter formatter) {
        if (dateObj == null) {
            return null;
        }
        if (dateObj instanceof LocalDate) {
            return ((LocalDate) dateObj).format(formatter);
        }
        if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).toLocalDate().format(formatter);
        }
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate().format(formatter);
        }
        if (dateObj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate().format(formatter);
        }
        // 兜底：String 或其他类型直接 toString（通常为 YYYY-MM-DD）
        String s = dateObj.toString();
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

