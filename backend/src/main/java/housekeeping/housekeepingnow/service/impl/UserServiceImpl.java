package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.dto.ForgotPasswordDTO;
import housekeeping.housekeepingnow.dto.LoginDTO;
import housekeeping.housekeepingnow.dto.RegisterDTO;
import housekeeping.housekeepingnow.dto.TokenDTO;
import housekeeping.housekeepingnow.entity.EmailRecord;
import housekeeping.housekeepingnow.entity.SmsRecord;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.EmailRecordMapper;
import housekeeping.housekeepingnow.mapper.SmsRecordMapper;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.service.EmailService;
import housekeeping.housekeepingnow.service.SmsService;
import housekeeping.housekeepingnow.service.UserService;
import housekeeping.housekeepingnow.util.JwtUtil;
import housekeeping.housekeepingnow.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsRecordMapper smsRecordMapper;
    
    @Autowired
    private EmailRecordMapper emailRecordMapper;
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    @Override
    public TokenDTO login(LoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 调试日志
        System.out.println("=== UserServiceImpl.login ===");
        System.out.println("登录用户名: " + loginDTO.getUsername());
        System.out.println("查询到的User: userId=" + user.getUserId() + ", username=" + user.getUsername() + ", userType=" + user.getUserType());
        
        // 验证密码
        if (!passwordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getUserType());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername(), user.getUserType());
        
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setRefreshToken(refreshToken);
        tokenDTO.setUserId(user.getUserId());
        tokenDTO.setUsername(user.getUsername());
        tokenDTO.setUserType(user.getUserType());
        
        System.out.println("返回TokenDTO: userId=" + tokenDTO.getUserId() + ", username=" + tokenDTO.getUsername());
        System.out.println("==============================");
        
        return tokenDTO;
    }
    
    @Override
    @Transactional
    public User register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        // 检查手机号是否已存在
        existUser = userMapper.selectByPhone(registerDTO.getPhone());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordUtil.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setUserType(registerDTO.getUserType() != null ? registerDTO.getUserType() : 1);
        user.setStatus(1);
        
        userMapper.insert(user);
        
        return user;
    }
    
    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    @Override
    public TokenDTO refreshToken(String refreshToken) {
        // 验证刷新Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Integer userType = jwtUtil.getUserTypeFromToken(refreshToken);
        
        // 生成新的Token
        String newToken = jwtUtil.generateToken(userId, username, userType);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, userType);
        
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(newToken);
        tokenDTO.setRefreshToken(newRefreshToken);
        tokenDTO.setUserId(userId);
        tokenDTO.setUsername(username);
        tokenDTO.setUserType(userType);
        
        return tokenDTO;
    }
    
    @Override
    public void sendPasswordResetCode(String account, String type) {
        User user = null;
        
        // 根据类型查找用户
        if ("phone".equals(type)) {
            user = userMapper.selectByPhone(account);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            // 发送短信验证码
            smsService.sendVerificationCode(account);
        } else if ("email".equals(type)) {
            // 验证邮箱格式
            if (!EMAIL_PATTERN.matcher(account).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            // 根据邮箱查找用户
            List<User> users = userMapper.selectByEmail(account);
            if (users == null || users.isEmpty()) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            user = users.get(0);
            // 发送邮件验证码
            emailService.sendVerificationCode(account);
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
    
    @Override
    public void verifyPasswordResetCode(String account, String type, String code) {
        if ("phone".equals(type)) {
            // 查询最近的短信验证码
            List<SmsRecord> records = smsRecordMapper.selectByPhone(account, "verification");
            if (records == null || records.isEmpty()) {
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
            
            SmsRecord latestRecord = records.get(0);
            
            // 检查验证码是否匹配
            if (!code.equals(latestRecord.getCode())) {
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
            
            // 检查验证码是否过期
            if (latestRecord.getExpireTime() != null && latestRecord.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
            }
            
            // 检查发送状态
            if (latestRecord.getSendStatus() != 1) {
                throw new BusinessException(ResultCode.CAPTCHA_SEND_FAILED);
            }
        } else if ("email".equals(type)) {
            // 查询最近的邮件验证码
            List<EmailRecord> records = emailRecordMapper.selectByEmail(account, "verification");
            if (records == null || records.isEmpty()) {
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
            
            EmailRecord latestRecord = records.get(0);
            
            // 检查验证码是否匹配
            if (!code.equals(latestRecord.getCode())) {
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
            
            // 检查验证码是否过期
            if (latestRecord.getExpireTime() != null && latestRecord.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
            }
            
            // 检查发送状态
            if (latestRecord.getSendStatus() != 1) {
                throw new BusinessException(ResultCode.EMAIL_SEND_FAILED);
            }
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
    
    @Override
    @Transactional
    public void resetPassword(ForgotPasswordDTO forgotPasswordDTO) {
        // 先验证验证码
        String account = forgotPasswordDTO.getAccount();
        String type = determineAccountType(account);
        verifyPasswordResetCode(account, type, forgotPasswordDTO.getCode());
        
        // 查找用户
        User user = null;
        if ("phone".equals(type)) {
            user = userMapper.selectByPhone(account);
        } else {
            List<User> users = userMapper.selectByEmail(account);
            if (users != null && !users.isEmpty()) {
                user = users.get(0);
            }
        }
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 更新密码
        user.setPassword(passwordUtil.encode(forgotPasswordDTO.getNewPassword()));
        userMapper.update(user);
    }
    
    /**
     * 判断账号类型（手机号或邮箱）
     */
    private String determineAccountType(String account) {
        if (EMAIL_PATTERN.matcher(account).matches()) {
            return "email";
        } else {
            return "phone";
        }
    }
}

