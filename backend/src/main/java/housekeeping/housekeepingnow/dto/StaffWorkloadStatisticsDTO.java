package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 服务人员工作量统计数据传输对象
 */
@Data
public class StaffWorkloadStatisticsDTO {
    /**
     * 服务人员ID
     */
    private Long staffId;
    
    /**
     * 服务人员姓名
     */
    private String staffName;
    
    /**
     * 完成预约数量
     */
    private Long completedCount;
    
    /**
     * 进行中预约数量
     */
    private Long inProgressCount;
    
    /**
     * 总预约数量
     */
    private Long totalCount;
    
    /**
     * 总工作时长（小时）
     */
    private BigDecimal totalWorkHours;
    
    /**
     * 总收入（从预约总金额计算）
     */
    private BigDecimal totalRevenue;
    
    /**
     * 平均评分
     */
    private BigDecimal averageRating;
    
    /**
     * 评价数量
     */
    private Long reviewCount;
}

