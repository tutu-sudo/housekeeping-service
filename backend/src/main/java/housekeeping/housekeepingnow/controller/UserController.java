package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.UserProfileDTO;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人信息相关接口
 */
@Tag(name = "用户接口", description = "用户个人信息查询与更新接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查询指定用户的基础信息（用户名、手机号、邮箱、头像）
     */
    @Operation(summary = "查询用户个人信息")
    @RequireAuth
    @GetMapping("/{userId}")
    public Result<UserProfileDTO> getUserProfile(@PathVariable Long userId,
                                                 HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Integer currentUserType = getCurrentUserType(request);

        // 仅允许本人或管理员查询
        if (!userId.equals(currentUserId) && (currentUserType == null || currentUserType != 3)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());

        return Result.success(dto);
    }

    /**
     * 更新指定用户的基础信息（用户名、手机号、邮箱、头像）
     */
    @Operation(summary = "更新用户个人信息")
    @RequireAuth
    @PutMapping("/{userId}")
    public Result<UserProfileDTO> updateUserProfile(@PathVariable Long userId,
                                                    @RequestBody UserProfileDTO profileDTO,
                                                    HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Integer currentUserType = getCurrentUserType(request);

        // 仅允许本人或管理员更新
        if (!userId.equals(currentUserId) && (currentUserType == null || currentUserType != 3)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (profileDTO.getUsername() != null) {
            user.setUsername(profileDTO.getUsername());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getAvatar() != null) {
            user.setAvatar(profileDTO.getAvatar());
        }

        userService.updateUser(user);

        UserProfileDTO result = new UserProfileDTO();
        result.setUserId(user.getUserId());
        result.setUsername(user.getUsername());
        result.setPhone(user.getPhone());
        result.setEmail(user.getEmail());
        result.setAvatar(user.getAvatar());

        return Result.success(result);
    }

    private static Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request != null ? request.getAttribute("userId") : null;
        if (userIdObj == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        try {
            return Long.valueOf(userIdObj.toString());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID格式错误");
        }
    }

    private static Integer getCurrentUserType(HttpServletRequest request) {
        Object typeObj = request != null ? request.getAttribute("userType") : null;
        if (typeObj == null) {
            return null;
        }
        if (typeObj instanceof Integer) {
            return (Integer) typeObj;
        }
        if (typeObj instanceof Number) {
            return ((Number) typeObj).intValue();
        }
        try {
            return Integer.valueOf(typeObj.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

