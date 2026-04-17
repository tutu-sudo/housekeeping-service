package housekeeping.housekeepingnow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预约统计数据传输对象
 */
@Data
public class AppointmentStatisticsDTO {
    /**
     * 日期（用于按日期统计）
     */
    private LocalDate date;
    
    /**
     * 服务类型ID
     */
    private Long serviceTypeId;
    
    /**
     * 服务类型名称
     */
    private String serviceTypeName;
    
    /**
     * 预约状态
     */
    private Integer status;
    
    /**
     * 预约数量
     */
    private Long count;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 总时长（小时）
     */
    private BigDecimal totalDuration;
}

