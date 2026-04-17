package housekeeping.housekeepingnow.config;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * JWT拦截器
 * 用于验证JWT Token并检查用户权限
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    // 白名单路径（不需要认证的接口）
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password/send-code",
            "/api/auth/forgot-password/verify-code",
            "/api/auth/forgot-password/reset",
            "/swagger-ui",
            "/v3/api-docs",
            "/doc.html",
            "/error"
    };
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是HandlerMethod，直接放行（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String requestPath = request.getRequestURI();
        
        // 检查白名单
        if (isWhiteList(requestPath)) {
            return true;
        }
        
        // 检查是否需要认证
        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        if (requireAuth == null) {
            requireAuth = handlerMethod.getBeanType().getAnnotation(RequireAuth.class);
        }
        
        // 检查是否需要特定角色
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        
        // 如果接口标记了@RequireAuth或@RequireRole，或者路径是/admin/*，则需要认证
        boolean needAuth = requireAuth != null || requireRole != null || requestPath.startsWith("/api/admin/");
        
        if (!needAuth) {
            return true;
        }
        
        // 获取Token
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.TOKEN_MISSING);
        }
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        
        // 从Token中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        
        // 调试日志
        System.out.println("=== JwtInterceptor.preHandle ===");
        System.out.println("请求路径: " + requestPath);
        System.out.println("从Token解析: userId=" + userId + ", userType=" + userType + ", username=" + username);
        
        if (userId == null || userType == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        
        // 验证用户是否存在且状态正常
        User user = userMapper.selectById(userId);
        if (user == null) {
            System.err.println("错误：Token中的userId=" + userId + "在数据库中不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        System.out.println("验证通过: userId=" + user.getUserId() + ", username=" + user.getUsername());
        System.out.println("=================================");
        
        // 检查角色权限
        if (requireRole != null) {
            int[] allowedTypes = requireRole.value();
            boolean hasPermission = Arrays.stream(allowedTypes).anyMatch(type -> type == userType);
            if (!hasPermission) {
                throw new BusinessException(ResultCode.PERMISSION_DENIED);
            }
        }
        
        // 后台管理接口需要管理员权限
        if (requestPath.startsWith("/api/admin/") && userType != 3) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        
        // 将用户信息存储到request中，供后续使用
        request.setAttribute("userId", userId);
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("userType", userType);
        
        return true;
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 也支持从query参数获取
        return request.getParameter("token");
    }
    
    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        for (String whitePath : WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }
}

