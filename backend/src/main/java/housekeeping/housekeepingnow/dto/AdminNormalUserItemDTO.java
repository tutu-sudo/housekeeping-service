package housekeeping.housekeepingnow.dto;

import lombok.Data;

/**
 * 后台-普通用户列表返回项
 */
@Data
public class AdminNormalUserItemDTO {
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private Integer status;
}
