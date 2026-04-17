package housekeeping.housekeepingnow.dto;

import lombok.Data;

/**
 * 用户个人信息 DTO（用于个人中心页面）
 */
@Data
public class UserProfileDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像 URL
     */
    private String avatar;
}

