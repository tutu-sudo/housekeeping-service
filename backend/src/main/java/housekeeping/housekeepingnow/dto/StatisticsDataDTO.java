package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 统一统计数据DTO（用于数据统计分析页面）
 */
@Data
public class StatisticsDataDTO {
    
    // ========== 收入统计 ==========
    /**
     * 总收入（元）
     */
    private BigDecimal totalRevenue;
    
    /**
     * 支付宝收入（元）
     */
    private BigDecimal alipayRevenue;
    
    /**
     * 微信支付收入（元）
     */
    private BigDecimal wechatRevenue;
    
    /**
     * 支付成功订单数
     */
    private Long paidOrdersCount;
    
    // ========== 收入趋势（按日期） ==========
    /**
     * 收入趋势数据
     */
    private List<RevenueTrendItem> revenueTrend;
    
    /**
     * 收入趋势项
     */
    @Data
    public static class RevenueTrendItem {
        /**
         * 日期（YYYY-MM-DD）
         */
        private String date;
        
        /**
         * 当日收入（元）
         */
        private BigDecimal amount;
    }
    
    // ========== 订单统计 ==========
    /**
     * 总订单数
     */
    private Long totalOrders;
    
    /**
     * 已完成订单数
     */
    private Long completedOrders;
    
    /**
     * 进行中订单数
     */
    private Long inProgressOrders;
    
    /**
     * 待确认订单数
     */
    private Long pendingOrders;
    
    // ========== 客户行为分析 ==========
    /**
     * 总客户数
     */
    private Long totalCustomers;
    
    /**
     * 活跃客户数（在指定时间范围内有订单的客户）
     */
    private Long activeCustomers;
    
    /**
     * 平均订单金额（元）
     */
    private BigDecimal avgOrderAmount;
    
    /**
     * 客户复购率（百分比，如 52.3 表示 52.3%）
     */
    private BigDecimal repeatPurchaseRate;
    
    // ========== 客户活跃度趋势（按日期） ==========
    /**
     * 客户活跃度趋势数据
     */
    private List<CustomerActivityTrendItem> customerActivityTrend;
    
    /**
     * 客户活跃度趋势项
     */
    @Data
    public static class CustomerActivityTrendItem {
        /**
         * 日期（YYYY-MM-DD）
         */
        private String date;
        
        /**
         * 当日活跃客户数
         */
        private Long count;
    }
    
    // ========== 服务类型统计（用于服务统计分析页面） ==========
    /**
     * 服务类型统计数据
     */
    private List<ServiceTypeStatItem> serviceTypeStats;
    
    /**
     * 服务类型统计项
     */
    @Data
    public static class ServiceTypeStatItem {
        /**
         * 服务类型名称
         */
        private String serviceTypeName;
        
        /**
         * 订单数量
         */
        private Long orderCount;
        
        /**
         * 总收入（元）
         */
        private BigDecimal totalRevenue;
        
        /**
         * 已支付订单数
         */
        private Long paidCount;
    }
    
    // ========== 服务质量分析（用于服务统计分析页面） ==========
    /**
     * 平均评分（1-5分）
     */
    private BigDecimal avgRating;
    
    /**
     * 总评价数
     */
    private Long totalReviews;
    
    /**
     * 平均服务态度分（1-5分）
     */
    private BigDecimal avgServiceAttitude;
    
    /**
     * 平均专业能力分（1-5分）
     */
    private BigDecimal avgProfessionalAbility;
    
    // ========== 评分分布（用于服务统计分析页面） ==========
    /**
     * 评分分布数据
     */
    private List<RatingDistributionItem> ratingDistribution;
    
    /**
     * 评分分布项
     */
    @Data
    public static class RatingDistributionItem {
        /**
         * 评分（1-5）
         */
        private Integer rating;
        
        /**
         * 该评分的评价数量
         */
        private Long count;
    }
}
