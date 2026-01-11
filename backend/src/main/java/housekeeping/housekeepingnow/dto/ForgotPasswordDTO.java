package housekeeping.housekeepingnow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码DTO
 */
@Data
public class ForgotPasswordDTO {
    
    /**
     * 手机号或邮箱
     */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String account;
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}

