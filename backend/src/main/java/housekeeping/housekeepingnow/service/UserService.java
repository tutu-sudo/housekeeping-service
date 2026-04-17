package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.dto.ChangePasswordDTO;
import housekeeping.housekeepingnow.dto.ForgotPasswordDTO;
import housekeeping.housekeepingnow.dto.LoginDTO;
import housekeeping.housekeepingnow.dto.RegisterDTO;
import housekeeping.housekeepingnow.dto.TokenDTO;
import housekeeping.housekeepingnow.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户登录
     */
    TokenDTO login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     */
    User register(RegisterDTO registerDTO);
    
    /**
     * 根据用户ID查询
     */
    User getById(Long userId);
    
    /**
     * 根据用户名查询
     */
    User getByUsername(String username);

    /**
     * 更新用户信息（仅用于内部服务，Controller 请做权限校验）
     */
    int updateUser(User user);
    
    /**
     * 刷新Token
     */
    TokenDTO refreshToken(String refreshToken);
    
    /**
     * 发送密码找回验证码（手机或邮箱）
     */
    void sendPasswordResetCode(String account, String type);
    
    /**
     * 验证密码找回验证码
     */
    void verifyPasswordResetCode(String account, String type, String code);
    
    /**
     * 重置密码
     */
    void resetPassword(ForgotPasswordDTO forgotPasswordDTO);

    /**
     * 登录态修改密码（需要旧密码）
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}

