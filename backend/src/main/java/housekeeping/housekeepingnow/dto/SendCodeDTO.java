package housekeeping.housekeepingnow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送验证码DTO
 */
@Data
public class SendCodeDTO {
    
    /**
     * 手机号或邮箱
     */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String account;
    
    /**
     * 类型：phone-手机号，email-邮箱
     */
    @NotBlank(message = "类型不能为空")
    private String type;
}

