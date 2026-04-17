package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.StaffDetailDTO;
import housekeeping.housekeepingnow.dto.StaffSkillDTO;
import housekeeping.housekeepingnow.entity.Service;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.ServiceMapper;
import housekeeping.housekeepingnow.service.StaffService;
import housekeeping.housekeepingnow.service.StaffServiceSkillService;
import housekeeping.housekeepingnow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公开服务人员接口控制器
 * 用于客户端查看服务人员详情和技能列表（可选token，公开接口）
 */
@Tag(name = "公开服务人员接口", description = "客户端查看服务人员详情和技能列表的公开接口")
@RestController
@RequestMapping("/api/staff")
public class PublicStaffController {

    @Autowired
    private StaffService staffService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StaffServiceSkillService staffServiceSkillService;
    
    @Autowired
    private ServiceMapper serviceMapper;

    /**
     * 获取服务人员详细信息（公开接口，可选token）
     * 用于客户端查看服务人员的电子简历等信息
     */
    @Operation(summary = "获取服务人员详细信息", description = "用于获取服务人员的基本信息、电子简历等，公开接口，可选token")
    @GetMapping("/{staffId}")
    public Result<StaffDetailDTO> getStaffDetail(@PathVariable Long staffId) {
        // 获取服务人员信息
        Staff staff = staffService.getById(staffId);
        
        // 获取关联的用户信息
        User user = null;
        if (staff.getUserId() != null) {
            try {
                user = userService.getById(staff.getUserId());
            } catch (Exception e) {
                // 如果获取用户信息失败，继续使用staff信息
            }
        }
        
        // 构建DTO
        StaffDetailDTO dto = StaffDetailDTO.from(user, staff);
        
        return Result.success(dto);
    }

    /**
     * 获取服务人员技能列表（包含证书）（公开接口，可选token）
     * 用于客户端查看服务人员的技能和证书信息
     * 只返回已审核通过的技能（certificateStatus=1）
     */
    @Operation(summary = "获取服务人员技能列表", description = "用于获取服务人员的技能列表，包含已审核通过的证书，公开接口，可选token")
    @GetMapping("/{staffId}/skills")
    public Result<List<StaffSkillDTO>> getStaffSkills(@PathVariable Long staffId) {
        // 获取服务人员的所有技能
        List<StaffServiceSkill> skills = staffServiceSkillService.getByStaffId(staffId);
        
        // 过滤只返回已审核通过的技能（certificateStatus=1）
        // 注意：根据联调说明，前端只显示status=1的技能
        List<StaffSkillDTO> skillDTOs = skills.stream()
                .filter(skill -> skill.getCertificateStatus() != null && skill.getCertificateStatus() == 1)
                .map(skill -> {
                    // 获取服务信息
                    Service service = null;
                    if (skill.getServiceId() != null) {
                        try {
                            service = serviceMapper.selectById(skill.getServiceId());
                        } catch (Exception e) {
                            // 如果获取服务信息失败，继续处理
                        }
                    }
                    return StaffSkillDTO.from(skill, service);
                })
                .collect(Collectors.toList());
        
        return Result.success(skillDTOs);
    }
}
