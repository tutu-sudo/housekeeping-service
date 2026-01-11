package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公司信息实体类
 */
@Data
public class Company {
    /**
     * 公司ID
     */
    private Long companyId;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 营业时间（如：09:00-18:00）
     */
    private String businessHours;
    
    /**
     * 服务城市（多个城市用逗号分隔）
     */
    private String serviceCities;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

