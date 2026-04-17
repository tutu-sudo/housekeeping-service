package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 服务技能关联实体类
 */
@Data
public class StaffServiceSkill {
    /**
     * 关联ID
     */
    private Long skillId;
    
    /**
     * 服务人员ID（关联staff表）
     */
    private Long staffId;
    
    /**
     * 服务ID（关联service表）
     */
    private Long serviceId;
    
    /**
     * 熟练程度：1-初级，2-中级，3-高级
     */
    private Integer proficiencyLevel;
    
    /**
     * 证书链接
     */
    private String certificateUrl;

    /**
     * 证书审核状态：0-待审核，1-通过，2-驳回
     */
    private Integer certificateStatus;
    
    /**
     * 经验年限（年）
     */
    private Integer experienceYears;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

