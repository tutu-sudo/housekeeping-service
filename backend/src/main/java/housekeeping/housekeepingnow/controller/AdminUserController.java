package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.annotation.RequireRole;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.dto.AdminNormalUserItemDTO;
import housekeeping.housekeepingnow.dto.AdminUserSearchItemDTO;
import housekeeping.housekeepingnow.dto.UserPromoteToStaffResultDTO;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.StaffServiceSkillMapper;
import housekeeping.housekeepingnow.mapper.StaffMapper;
import housekeeping.housekeepingnow.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import housekeeping.housekeepingnow.service.AppointmentService;

/**
 * 后台-用户管理（角色流转、用户搜索等）
 */
@Tag(name = "后台-用户管理接口", description = "管理员用于搜索用户、升级为服务人员、降级为普通用户等")
@RestController
@RequestMapping("/api/admin/users")
@RequireAuth
@RequireRole({3})
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private StaffServiceSkillMapper staffServiceSkillMapper;

    @Autowired
    private AppointmentService appointmentService;

    @Operation(summary = "按用户名/手机号搜索用户", description = "keyword 支持 username/phone 模糊匹配")
    @GetMapping("/search")
    public Result<List<AdminUserSearchItemDTO>> search(@RequestParam String keyword,
                                                       @RequestParam(required = false, defaultValue = "20") Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }
        return Result.success(userMapper.searchUsers(keyword, limit));
    }

    @Operation(summary = "分页查看普通用户", description = "可查看普通用户姓名(username)、手机号(phone)、邮箱(email)，支持关键字搜索")
    @GetMapping("/normal")
    public Result<List<AdminNormalUserItemDTO>> listNormalUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        int offset = (page - 1) * size;
        return Result.success(userMapper.selectNormalUsers(keyword, offset, size));
    }

    @Operation(summary = "禁用普通用户登录", description = "将普通用户 status 设置为 0，登录接口将拒绝该用户登录")
    @PostMapping("/{userId}/disable-login")
    public Result<Void> disableNormalUserLogin(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(user.getUserType())) {
            throw new BusinessException(ResultCode.OPERATION_NOT_ALLOWED, "仅允许禁用普通用户登录");
        }

        if (!Integer.valueOf(0).equals(user.getStatus())) {
            user.setStatus(0);
            userMapper.update(user);
        }
        return Result.success();
    }

    @Operation(summary = "启用普通用户登录", description = "将普通用户 status 设置为 1，恢复该用户登录能力")
    @PostMapping("/{userId}/enable-login")
    public Result<Void> enableNormalUserLogin(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(user.getUserType())) {
            throw new BusinessException(ResultCode.OPERATION_NOT_ALLOWED, "仅允许启用普通用户登录");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            user.setStatus(1);
            userMapper.update(user);
        }
        return Result.success();
    }

    @Operation(summary = "将用户升级为服务人员（待审核）",
            description = "做三件事：1) 校验用户存在；2) user.userType 设为2；3) 创建/复用 staff 档案并将 verificationStatus 设为0（待审核）")
    @PostMapping("/{userId}/promote-to-staff")
    @Transactional
    public Result<UserPromoteToStaffResultDTO> promoteToStaff(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 幂等：如果已是服务人员/管理员，不重复创建；管理员不允许直接降为服务人员（避免误操作）
        if (Integer.valueOf(3).equals(user.getUserType())) {
            throw new BusinessException(ResultCode.OPERATION_NOT_ALLOWED, "管理员账号不允许升级为服务人员");
        }

        // 角色切换：普通用户(1) -> 服务人员(2)
        if (!Integer.valueOf(2).equals(user.getUserType())) {
            user.setUserType(2);
            userMapper.update(user);
        }

        // 初始化/复用 staff 档案
        Staff staff = staffMapper.selectByUserId(userId);
        if (staff == null) {
            staff = new Staff();
            staff.setUserId(userId);
            // staff.name 在数据库里为 NOT NULL，初始化用 username（或兜底值），后续可在资料页完善
            String defaultName = (user.getUsername() != null && !user.getUsername().isBlank())
                    ? user.getUsername()
                    : "未填写";
            staff.setName(defaultName);
            // 默认待审核（0）+ 默认工作状态正常（1），避免空值
            staff.setVerificationStatus(0);
            staff.setWorkStatus(1);
            // hourly_rate 在部分库结构里可能为 NOT NULL 且 insert 显式传参，这里兜底为 0
            staff.setHourlyRate(BigDecimal.ZERO);
            try {
                staffMapper.insert(staff);
            } catch (DataIntegrityViolationException ex) {
                // 并发/幂等兜底：如果唯一键冲突（例如 uk_user_id），尝试回读并返回
                Staff existing = staffMapper.selectByUserId(userId);
                if (existing != null) {
                    staff = existing;
                } else {
                    throw ex;
                }
            }
        } else {
            // 确保升级后默认为待审核（未审核不能接单）
            if (staff.getVerificationStatus() == null || staff.getVerificationStatus() != 0) {
                staffMapper.updateVerificationStatus(staff.getStaffId(), 0);
            }
        }

        UserPromoteToStaffResultDTO dto = new UserPromoteToStaffResultDTO();
        dto.setStaffId(staff.getStaffId());
        dto.setVerificationStatus(0);
        return Result.success(dto);
    }

    @Operation(summary = "将家政服务人员开除为普通用户",
            description = "语义：user.userType 设为1；清空 staff_service_skill；若该 staff 无 appointment 则物理删除 staff；否则回退为不可服务（workStatus=0）且未通过审核（verificationStatus=2），避免外键 RESTRICT 导致删除失败")
    @PostMapping("/{userId}/demote-to-user")
    @Transactional
    public Result<Void> demoteToUser(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (Integer.valueOf(3).equals(user.getUserType())) {
            throw new BusinessException(ResultCode.OPERATION_NOT_ALLOWED, "管理员账号不允许降级为普通用户");
        }
        if (!Integer.valueOf(1).equals(user.getUserType())) {
            user.setUserType(1);
            userMapper.update(user);
        }

        // 开除员工语义：
        // 1) 清空该 staff 的技能关联（不依赖外键可删性）
        // 2) 尝试物理删除 staff 档案；若存在 appointment 等依赖，会因外键 ON DELETE RESTRICT 失败
        //    则回退为“不可服务/未通过审核”状态，避免破坏其它业务联表逻辑。
        Staff staff = staffMapper.selectByUserId(userId);
        if (staff != null) {
            Long staffId = staff.getStaffId();

            // 不管能否删除 staff，都先把其服务能力清掉
            staffServiceSkillMapper.deleteByStaffId(staffId);

            boolean hasAppointments = !appointmentService.listByStaffId(staffId).isEmpty();
            if (!hasAppointments) {
                staffMapper.deleteById(staffId);
            } else {
                // 存在 appointment：数据库外键 ON DELETE RESTRICT，会导致物理删除失败
                Staff update = new Staff();
                update.setStaffId(staffId);
                update.setWorkStatus(0); // 不可服务
                update.setVerificationStatus(2); // 审核驳回（等同未通过）
                staffMapper.update(update);
            }
        }

        return Result.success();
    }
}

