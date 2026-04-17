package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.StaffProfileDTO;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.service.StaffService;
import housekeeping.housekeepingnow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 服务人员接口控制器
 */
@Tag(name = "服务人员接口", description = "服务人员个人资料管理接口")
@RestController
@RequestMapping("/api/staff")
@RequireAuth
@RequireRole({2}) // 仅服务人员可访问
public class StaffController {

    @Autowired
    private StaffService staffService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "获取当前登录服务人员的个人资料")
    @GetMapping("/profile")
    public Result<StaffProfileDTO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        
        // 调试日志
        System.out.println("=== StaffController.getProfile ===");
        System.out.println("从Token解析的userId: " + userId);
        System.out.println("从Token解析的username: " + username);
        
        User user = userService.getById(userId);
        if (user == null) {
            System.err.println("错误：未找到userId=" + userId + "的用户");
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.USER_NOT_FOUND);
        }
        System.out.println("查询到的User: userId=" + user.getUserId() + ", username=" + user.getUsername());
        
        Staff staff = staffService.getByUserId(userId);
        if (staff == null) {
            System.err.println("错误：未找到userId=" + userId + "对应的服务人员信息");
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.STAFF_NOT_FOUND);
        }
        System.out.println("查询到的Staff: staffId=" + staff.getStaffId() + ", userId=" + staff.getUserId() + ", name=" + staff.getName());
        
        StaffProfileDTO profile = StaffProfileDTO.from(user, staff);
        System.out.println("返回的Profile: userId=" + profile.getUserId() + ", staffId=" + profile.getStaffId() + ", name=" + profile.getName());
        System.out.println("===================================");
        
        return Result.success(profile);
    }

    @Operation(summary = "更新服务人员基本信息")
    @PutMapping("/profile/basic")
    @Transactional
    public Result<StaffProfileDTO> updateBasicInfo(@RequestBody StaffProfileDTO profileDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Staff currentStaff = staffService.getByUserId(userId);
        
        // 更新Staff信息
        Staff staff = new Staff();
        staff.setStaffId(currentStaff.getStaffId());
        if (profileDTO.getName() != null) staff.setName(profileDTO.getName());
        if (profileDTO.getGender() != null) staff.setGender(profileDTO.getGender());
        if (profileDTO.getBirthDate() != null) staff.setBirthDate(profileDTO.getBirthDate());
        if (profileDTO.getOrigin() != null) staff.setOrigin(profileDTO.getOrigin());
        if (profileDTO.getIdCard() != null) staff.setIdCard(profileDTO.getIdCard());
        if (profileDTO.getWorkExperience() != null) staff.setWorkExperience(profileDTO.getWorkExperience());
        Staff updatedStaff = staffService.updateBasicInfo(currentStaff.getStaffId(), staff);
        
        // 更新User信息（手机号、邮箱）
        User user = new User();
        user.setUserId(userId);
        if (profileDTO.getPhone() != null) user.setPhone(profileDTO.getPhone());
        if (profileDTO.getEmail() != null) user.setEmail(profileDTO.getEmail());
        userMapper.update(user);
        User updatedUser = userService.getById(userId);
        
        StaffProfileDTO result = StaffProfileDTO.from(updatedUser, updatedStaff);
        return Result.success(result);
    }

    @Operation(summary = "更新服务人员简历", description = "更新电子简历，包括个人简介(bio)、工作经历(workExperienceText)、专业技能(professionalSkills)")
    @PutMapping("/profile/resume")
    @Transactional
    public Result<StaffProfileDTO> updateResume(@RequestBody StaffProfileDTO profileDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Staff currentStaff = staffService.getByUserId(userId);
        
        // 更新电子简历字段
        Staff staff = new Staff();
        staff.setStaffId(currentStaff.getStaffId());
        
        // 支持 bio, workExperienceText, professionalSkills 字段
        if (profileDTO.getBio() != null) {
            staff.setBio(profileDTO.getBio());
        }
        if (profileDTO.getWorkExperienceText() != null) {
            staff.setWorkExperienceText(profileDTO.getWorkExperienceText());
        }
        if (profileDTO.getProfessionalSkills() != null) {
            staff.setProfessionalSkills(profileDTO.getProfessionalSkills());
        }
        
        // 兼容旧的 resume 字段（如果前端只传了 resume，则更新到 bio）
        if (profileDTO.getResume() != null && profileDTO.getBio() == null) {
            staff.setBio(profileDTO.getResume());
        }
        
        Staff updated = staffService.updateElectronicResume(currentStaff.getStaffId(), staff);
        User user = userService.getById(userId);
        StaffProfileDTO result = StaffProfileDTO.from(user, updated);
        return Result.success(result);
    }

    @Operation(summary = "更新服务人员头像")
    @PutMapping("/profile/avatar")
    @Transactional
    public Result<StaffProfileDTO> updateAvatar(@RequestBody StaffProfileDTO profileDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Staff currentStaff = staffService.getByUserId(userId);
        Staff updated = staffService.updateAvatar(currentStaff.getStaffId(), profileDTO.getAvatar());
        
        // 同时更新User表的avatar
        User user = new User();
        user.setUserId(userId);
        user.setAvatar(profileDTO.getAvatar());
        userMapper.update(user);
        User updatedUser = userService.getById(userId);
        
        StaffProfileDTO result = StaffProfileDTO.from(updatedUser, updated);
        return Result.success(result);
    }
}

