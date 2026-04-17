package housekeeping.housekeepingnow.dto;

import lombok.Data;

/**
 * 后台-用户搜索返回项
 */
@Data
public class AdminUserSearchItemDTO {
    private Long userId;
    private String username;
    private String phone;
    private Integer userType;
}

