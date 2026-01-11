package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.ForgotPasswordDTO;
import housekeeping.housekeepingnow.dto.LoginDTO;
import housekeeping.housekeepingnow.dto.RegisterDTO;
import housekeeping.housekeepingnow.dto.SendCodeDTO;
import housekeeping.housekeepingnow.dto.TokenDTO;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证接口", description = "用户登录、注册、密码找回等认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    public Result<TokenDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        TokenDTO tokenDTO = userService.login(loginDTO);
        return Result.success(tokenDTO);
    }
    
    @Operation(summary = "用户注册", description = "新用户注册")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        return Result.success(user);
    }
    
    @Operation(summary = "刷新Token", description = "使用刷新Token获取新的访问Token")
    @PostMapping("/refresh")
    public Result<TokenDTO> refreshToken(@RequestParam String refreshToken) {
        TokenDTO tokenDTO = userService.refreshToken(refreshToken);
        return Result.success(tokenDTO);
    }
    
    @Operation(summary = "发送密码找回验证码", description = "发送手机或邮箱验证码用于密码找回")
    @PostMapping("/forgot-password/send-code")
    public Result<Void> sendPasswordResetCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        userService.sendPasswordResetCode(sendCodeDTO.getAccount(), sendCodeDTO.getType());
        return Result.success();
    }
    
    @Operation(summary = "验证密码找回验证码", description = "验证手机或邮箱验证码")
    @PostMapping("/forgot-password/verify-code")
    public Result<Void> verifyPasswordResetCode(@RequestParam String account,
                                                @RequestParam String type,
                                                @RequestParam String code) {
        userService.verifyPasswordResetCode(account, type, code);
        return Result.success();
    }
    
    @Operation(summary = "重置密码", description = "通过验证码重置密码")
    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        userService.resetPassword(forgotPasswordDTO);
        return Result.success();
    }
}

