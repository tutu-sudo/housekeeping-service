package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务实体类
 */
@Data
public class Service {
    /**
     * 服务ID
     */
    private Long serviceId;
    
    /**
     * 服务类型ID（关联service_type表）
     */
    private Long serviceTypeId;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 预估时长（小时）
     */
    private Integer estimatedDuration;
    
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

