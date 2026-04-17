package housekeeping.housekeepingnow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录态修改密码DTO（需要旧密码）
 */
@Data
public class ChangePasswordDTO {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}

