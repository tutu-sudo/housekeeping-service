package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import housekeeping.housekeepingnow.service.StaffService;
import housekeeping.housekeepingnow.service.StaffServiceSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务技能接口控制器
 */
@Tag(name = "服务技能接口", description = "服务人员技能管理接口")
@RestController
@RequestMapping("/api/staff/skills")
@RequireAuth
@RequireRole({2}) // 仅服务人员可访问
public class StaffServiceSkillController {

    @Autowired
    private StaffServiceSkillService staffServiceSkillService;
    
    @Autowired
    private StaffService staffService;

    @Operation(summary = "获取当前登录服务人员的技能列表")
    @GetMapping
    public Result<List<StaffServiceSkill>> getSkills(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        List<StaffServiceSkill> skills = staffServiceSkillService.getByStaffId(staff.getStaffId());
        return Result.success(skills);
    }

    @Operation(summary = "添加服务技能")
    @PostMapping
    public Result<StaffServiceSkill> addSkill(@RequestBody StaffServiceSkill skill, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        skill.setStaffId(staff.getStaffId());
        StaffServiceSkill added = staffServiceSkillService.addSkill(skill);
        return Result.success(added);
    }

    @Operation(summary = "更新服务技能")
    @PutMapping("/{skillId}")
    public Result<StaffServiceSkill> updateSkill(@PathVariable Long skillId, 
                                                  @RequestBody StaffServiceSkill skill,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        // 验证技能属于当前服务人员
        StaffServiceSkill existing = staffServiceSkillService.getById(skillId);
        if (!existing.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        skill.setSkillId(skillId);
        skill.setStaffId(staff.getStaffId());
        StaffServiceSkill updated = staffServiceSkillService.updateSkill(skill);
        return Result.success(updated);
    }

    @Operation(summary = "删除服务技能")
    @DeleteMapping("/{skillId}")
    public Result<Void> deleteSkill(@PathVariable Long skillId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var staff = staffService.getByUserId(userId);
        // 验证技能属于当前服务人员
        StaffServiceSkill existing = staffServiceSkillService.getById(skillId);
        if (!existing.getStaffId().equals(staff.getStaffId())) {
            throw new housekeeping.housekeepingnow.common.exception.BusinessException(
                    housekeeping.housekeepingnow.common.enums.ResultCode.PERMISSION_DENIED);
        }
        staffServiceSkillService.deleteSkill(skillId);
        return Result.success();
    }
}

