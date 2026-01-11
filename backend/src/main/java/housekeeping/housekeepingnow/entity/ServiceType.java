package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务类型实体类
 */
@Data
public class ServiceType {
    /**
     * 服务类型ID
     */
    private Long serviceTypeId;
    
    /**
     * 类型名称
     */
    private String typeName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 基础价格
     */
    private BigDecimal basePrice;
    
    /**
     * 最小时长（小时）
     */
    private Integer minDuration;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

