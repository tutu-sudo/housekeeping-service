package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.ChangePasswordDTO;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @Operation(summary = "登录态修改密码", description = "需要携带Bearer Token，并提供旧密码和新密码")
    @RequireAuth
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO,
                                       HttpServletRequest request) {
        Long userId = resolveUserId(request);
        userService.changePassword(userId, changePasswordDTO);
        return Result.success();
    }

    @Operation(summary = "发送密码找回验证码（兼容旧路径）",
            description = "兼容旧版前端接口：POST /api/auth/send-code。支持account/phone/email字段，type可选（不传则自动推断）。")
    @PostMapping("/send-code")
    public Result<Void> sendPasswordResetCodeCompat(@RequestBody(required = false) Map<String, Object> body,
                                                    @RequestParam(required = false) String account,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) String phone,
                                                    @RequestParam(required = false) String email) {
        String resolvedAccount = firstNonBlank(
                getBodyString(body, "account"),
                getBodyString(body, "phone"),
                getBodyString(body, "email"),
                account, phone, email
        );
        if (isBlank(resolvedAccount)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号或邮箱不能为空");
        }

        String resolvedType = firstNonBlank(getBodyString(body, "type"), type);
        if (isBlank(resolvedType)) {
            resolvedType = inferAccountType(resolvedAccount);
        }

        userService.sendPasswordResetCode(resolvedAccount, resolvedType);
        return Result.success();
    }




    @Operation(summary = "验证密码找回验证码（兼容旧路径）",
            description = "兼容旧版前端接口：POST /api/auth/verify-code。支持account/phone/email字段，type可选（不传则自动推断）。")
    @PostMapping("/verify-code")
    public Result<Void> verifyPasswordResetCodeCompat(@RequestBody(required = false) Map<String, Object> body,
                                                      @RequestParam(required = false) String account,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String code,
                                                      @RequestParam(required = false) String phone,
                                                      @RequestParam(required = false) String email) {
        String resolvedAccount = firstNonBlank(
                getBodyString(body, "account"),
                getBodyString(body, "phone"),
                getBodyString(body, "email"),
                account, phone, email
        );
        String resolvedCode = firstNonBlank(getBodyString(body, "code"), code);

        if (isBlank(resolvedAccount)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号或邮箱不能为空");
        }
        if (isBlank(resolvedCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码不能为空");
        }

        String resolvedType = firstNonBlank(getBodyString(body, "type"), type);
        if (isBlank(resolvedType)) {
            resolvedType = inferAccountType(resolvedAccount);
        }

        userService.verifyPasswordResetCode(resolvedAccount, resolvedType, resolvedCode);
        return Result.success();
    }




    @Operation(summary = "重置密码（兼容旧路径）",
            description = "兼容旧版前端接口：POST /api/auth/reset-password。支持account/phone/email字段，newPassword/password字段。")
    @PostMapping("/reset-password")
    public Result<Void> resetPasswordCompat(@RequestBody(required = false) Map<String, Object> body,
                                           @RequestParam(required = false) String account,
                                           @RequestParam(required = false) String code,
                                           @RequestParam(required = false) String newPassword,
                                           @RequestParam(required = false) String phone,
                                           @RequestParam(required = false) String email) {
        String resolvedAccount = firstNonBlank(
                getBodyString(body, "account"),
                getBodyString(body, "phone"),
                getBodyString(body, "email"),
                account, phone, email
        );
        String resolvedCode = firstNonBlank(getBodyString(body, "code"), code);
        String resolvedNewPassword = firstNonBlank(
                getBodyString(body, "newPassword"),
                getBodyString(body, "password"),
                newPassword
        );

        if (isBlank(resolvedAccount)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号或邮箱不能为空");
        }
        if (isBlank(resolvedCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码不能为空");
        }
        if (isBlank(resolvedNewPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "新密码不能为空");
        }

        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setAccount(resolvedAccount);
        dto.setCode(resolvedCode);
        dto.setNewPassword(resolvedNewPassword);

        userService.resetPassword(dto);
        return Result.success();
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

    private static String getBodyString(Map<String, Object> body, String key) {
        if (body == null || key == null) {
            return null;
        }
        Object v = body.get(key);
        if (v == null) {
            return null;
        }
        String s = v.toString();
        return isBlank(s) ? null : s.trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (!isBlank(v)) {
                return v.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }






    private static String inferAccountType(String account) {
        if (account != null && account.contains("@")) {
            return "email";
        }
        return "phone";
    }

    private static Long resolveUserId(HttpServletRequest request) {
        Object userIdObj = request != null ? request.getAttribute("userId") : null;
        if (userIdObj == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        try {
            if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            }
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            return Long.valueOf(userIdObj.toString());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID格式错误");
        }
    }
}

