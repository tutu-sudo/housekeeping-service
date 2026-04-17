package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.AdminStaffDTO;
import housekeeping.housekeepingnow.dto.StaffProfileDTO;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.StaffMapper;
import housekeeping.housekeepingnow.mapper.StaffServiceSkillMapper;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-人员管理模块接口
 */
@Tag(name = "后台-人员管理接口", description = "服务人员审核、证书审核、人员状态管理")
@RestController
@RequestMapping("/api/admin/staff")
@RequireAuth
@RequireRole({3}) // 仅管理员可访问
public class AdminStaffController {

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StaffServiceSkillMapper staffServiceSkillMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private housekeeping.housekeepingnow.service.StaffService staffService;

    @Autowired
    private housekeeping.housekeepingnow.service.UserService userService;

    // ========== 1. 服务人员审核 ==========

    @Operation(summary = "服务人员审核列表", description = "按验证状态筛选服务人员，verificationStatus: 0-待审核,1-通过,2-驳回")
    @GetMapping("/applications")
    public Result<List<AdminStaffDTO>> listApplications(@RequestParam(required = false) Integer verificationStatus) {
        // 后台列表需要展示 phone/email，使用聚合查询
        List<AdminStaffDTO> list = staffMapper.selectAdminList(verificationStatus, null);
        return Result.success(list);
    }

    @Operation(summary = "服务人员详情（后台）", description = "返回服务人员详细信息，并补齐手机号/邮箱")
    @GetMapping("/{staffId}")
    public Result<AdminStaffDTO> getStaffDetail(@PathVariable Long staffId) {
        AdminStaffDTO dto = staffMapper.selectAdminById(staffId);
        if (dto == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        return Result.success(dto);
    }

    @Operation(summary = "更新服务人员基础信息（后台填写资料）",
            description = "管理员在后台直接维护服务人员档案：姓名、性别、身份证、籍贯、工作经验、时薪等，并选择唯一的具体服务；" +
                    "如需更新手机号/邮箱，可通过用户管理接口调整。服务选择会同步写入 staff_service_skill 表（保持一人一服务）。")
    @PutMapping("/{staffId}")
    public Result<StaffProfileDTO> updateStaffProfile(@PathVariable Long staffId,
                                                      @RequestBody StaffProfileDTO profileDTO) {
        Staff current = staffService.getById(staffId);
        User user = userService.getById(current.getUserId());

        // 构造部分更新的 Staff
        Staff update = new Staff();
        update.setStaffId(staffId);
        if (profileDTO.getName() != null) update.setName(profileDTO.getName());
        if (profileDTO.getGender() != null) update.setGender(profileDTO.getGender());
        if (profileDTO.getBirthDate() != null) update.setBirthDate(profileDTO.getBirthDate());
        if (profileDTO.getOrigin() != null) update.setOrigin(profileDTO.getOrigin());
        if (profileDTO.getIdCard() != null) update.setIdCard(profileDTO.getIdCard());
        if (profileDTO.getResume() != null) update.setResume(profileDTO.getResume());
        if (profileDTO.getBio() != null) update.setBio(profileDTO.getBio());
        if (profileDTO.getWorkExperienceText() != null) update.setWorkExperienceText(profileDTO.getWorkExperienceText());
        if (profileDTO.getProfessionalSkills() != null) update.setProfessionalSkills(profileDTO.getProfessionalSkills());
        if (profileDTO.getWorkExperience() != null) update.setWorkExperience(profileDTO.getWorkExperience());
        if (profileDTO.getHourlyRate() != null) update.setHourlyRate(profileDTO.getHourlyRate());

        Staff updatedStaff = staffService.updateStaff(update);

        // 同步唯一服务（一人一服务）：先删除该 staff 的所有技能记录，再插入一条新的
        if (profileDTO.getServiceId() != null) {
            Long serviceId = profileDTO.getServiceId();
            // 删除旧记录
            staffServiceSkillMapper.deleteByStaffId(staffId);
            // 插入新记录
            StaffServiceSkill skill = new StaffServiceSkill();
            skill.setStaffId(staffId);
            skill.setServiceId(serviceId);
            // 默认设置：初级/已通过证书/经验年限来自 workExperience（没有则 0）
            skill.setProficiencyLevel(1);
            Integer years = profileDTO.getWorkExperience() != null ? profileDTO.getWorkExperience() : 0;
            skill.setExperienceYears(years);
            skill.setCertificateStatus(1);
            staffServiceSkillMapper.insert(skill);
        }

        // 返回统一的资料 DTO（包含用户基础信息 + staff 信息）
        StaffProfileDTO result = StaffProfileDTO.from(user, updatedStaff);
        return Result.success(result);
    }

    @Operation(summary = "审核通过服务人员", description = "将用户从普通用户升级为服务人员账号")
    @PostMapping("/{staffId}/approve")
    public Result<Void> approveStaff(@PathVariable Long staffId,
                                     @RequestParam(required = false) String remark) {
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        User user = userMapper.selectById(staff.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 更新staff审核状态
        staffMapper.updateVerificationStatus(staffId, 1);
        // 将账号类型改为服务人员
        user.setUserType(2);
        userMapper.update(user);

        String title = "服务人员审核通过";
        String content = "您的服务人员申请已通过审核，现在可以使用家政服务人员端功能。" +
                (remark != null ? (" 备注：" + remark) : "");
        messageService.sendMessage(user.getUserId(), "system", title, content, staffId);
        return Result.success();
    }

    @Operation(summary = "审核驳回服务人员", description = "审核不通过时保留普通用户身份")
    @PostMapping("/{staffId}/reject")
    public Result<Void> rejectStaff(@PathVariable Long staffId,
                                    @RequestParam(required = false) String reason) {
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        User user = userMapper.selectById(staff.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        staffMapper.updateVerificationStatus(staffId, 2);

        String title = "服务人员审核未通过";
        String content = "您的服务人员申请未通过审核。" +
                (reason != null ? (" 原因：" + reason) : "");
        messageService.sendMessage(user.getUserId(), "system", title, content, staffId);
        return Result.success();
    }

    // ========== 2. 证书审核 ==========

    @Operation(summary = "待审核证书列表", description = "certificateStatus: 0-待审核,1-通过,2-驳回")
    @GetMapping("/certificates")
    public Result<List<StaffServiceSkill>> listCertificates(@RequestParam(required = false) Integer status,
                                                            @RequestParam(required = false) Long staffId) {
        List<StaffServiceSkill> list =
                staffServiceSkillMapper.selectByCertificateStatus(status, staffId);
        return Result.success(list);
    }

    @Operation(summary = "证书审核通过")
    @PostMapping("/certificates/{skillId}/approve")
    public Result<Void> approveCertificate(@PathVariable Long skillId,
                                           @RequestParam(required = false) String remark) {
        StaffServiceSkill skill = staffServiceSkillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.SERVICE_SKILL_NOT_FOUND);
        }
        staffServiceSkillMapper.updateCertificateStatus(skillId, 1);
        Staff staff = staffMapper.selectById(skill.getStaffId());
        if (staff != null) {
            User user = userMapper.selectById(staff.getUserId());
            if (user != null) {
                String title = "技能证书审核通过";
                String content = "您上传的技能证书已通过审核，可以正常展示。" +
                        (remark != null ? (" 备注：" + remark) : "");
                messageService.sendMessage(user.getUserId(), "system", title, content, skillId);
            }
        }
        return Result.success();
    }

    @Operation(summary = "证书审核驳回")
    @PostMapping("/certificates/{skillId}/reject")
    public Result<Void> rejectCertificate(@PathVariable Long skillId,
                                          @RequestParam(required = false) String reason) {
        StaffServiceSkill skill = staffServiceSkillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.SERVICE_SKILL_NOT_FOUND);
        }
        staffServiceSkillMapper.updateCertificateStatus(skillId, 2);
        Staff staff = staffMapper.selectById(skill.getStaffId());
        if (staff != null) {
            User user = userMapper.selectById(staff.getUserId());
            if (user != null) {
                String title = "技能证书审核未通过";
                String content = "您上传的技能证书未通过审核，请根据提示重新上传。" +
                        (reason != null ? (" 原因：" + reason) : "");
                messageService.sendMessage(user.getUserId(), "system", title, content, skillId);
            }
        }
        return Result.success();
    }

    // ========== 3. 人员状态管理 ==========

    @Operation(summary = "服务人员状态列表", description = "可按验证状态和工作状态筛选")
    @GetMapping
    public Result<List<AdminStaffDTO>> listStaff(@RequestParam(required = false) Integer verificationStatus,
                                         @RequestParam(required = false) Integer workStatus) {
        List<AdminStaffDTO> list = staffMapper.selectAdminList(verificationStatus, workStatus);
        return Result.success(list);
    }

    @Operation(summary = "修改服务人员工作状态",
            description = "workStatus: 0-不可服务,1-正常,2-警告,3-黑名单；黑名单或不可服务会同时禁用账号")
    @PatchMapping("/{staffId}/work-status")
    public Result<Void> changeWorkStatus(@PathVariable Long staffId,
                                         @RequestParam Integer workStatus,
                                         @RequestParam(required = false) String reason) {
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        User user = userMapper.selectById(staff.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Staff update = new Staff();
        update.setStaffId(staffId);
        update.setWorkStatus(workStatus);
        staffMapper.update(update);

        String title = "人员状态变更通知";
        String content = "您的服务人员状态已被管理员调整，当前状态码：" + workStatus +
                (reason != null ? ("，原因：" + reason) : "");
        messageService.sendMessage(user.getUserId(), "system", title, content, staffId);
        return Result.success();
    }
}

