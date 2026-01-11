package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 员工排班实体类
 */
@Data
public class Schedule {
    /**
     * 排班ID
     */
    private Long scheduleId;
    
    /**
     * 服务人员ID（关联staff表）
     */
    private Long staffId;
    
    /**
     * 工作日期
     */
    private LocalDate workDate;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 可用状态：0-不可用，1-可用
     */
    private Integer availableStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

