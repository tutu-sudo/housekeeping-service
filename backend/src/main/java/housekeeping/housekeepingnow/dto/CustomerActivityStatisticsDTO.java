package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 顾客活跃度统计数据传输对象
 */
@Data
public class CustomerActivityStatisticsDTO {
    /**
     * 顾客ID
     */
    private Long customerId;
    
    /**
     * 顾客姓名
     */
    private String customerName;
    
    /**
     * 预约总数
     */
    private Long totalAppointments;
    
    /**
     * 已完成预约数
     */
    private Long completedAppointments;
    
    /**
     * 取消预约数
     */
    private Long cancelledAppointments;
    
    /**
     * 最近预约日期
     */
    private LocalDate lastAppointmentDate;
    
    /**
     * 首次预约日期
     */
    private LocalDate firstAppointmentDate;
    
    /**
     * 总消费金额
     */
    private java.math.BigDecimal totalSpent;
    
    /**
     * 活跃度等级（根据预约频率和金额计算）
     */
    private String activityLevel;
}

