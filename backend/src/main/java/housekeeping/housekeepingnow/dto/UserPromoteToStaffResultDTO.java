package housekeeping.housekeepingnow.dto;

import lombok.Data;

/**
 * 后台-用户升级为服务人员结果
 */
@Data
public class UserPromoteToStaffResultDTO {
    private Long staffId;
    private Integer verificationStatus;
}

