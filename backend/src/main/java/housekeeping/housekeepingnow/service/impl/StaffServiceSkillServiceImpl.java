package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.StaffMapper;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.mapper.StaffServiceSkillMapper;
import housekeeping.housekeepingnow.service.StaffServiceSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffServiceSkillServiceImpl implements StaffServiceSkillService {

    @Autowired
    private StaffServiceSkillMapper staffServiceSkillMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private housekeeping.housekeepingnow.service.MessageService messageService;

    @Override
    public List<StaffServiceSkill> getByStaffId(Long staffId) {
        return staffServiceSkillMapper.selectByStaffId(staffId);
    }

    @Override
    @Transactional
    public StaffServiceSkill addSkill(StaffServiceSkill skill) {
        if (skill.getStaffId() == null || skill.getServiceId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        // 检查是否已存在相同的技能
        StaffServiceSkill existing = staffServiceSkillMapper.selectByStaffIdAndServiceId(
                skill.getStaffId(), skill.getServiceId());
        if (existing != null) {
            throw new BusinessException(ResultCode.SERVICE_SKILL_ALREADY_EXISTS);
        }
        if (skill.getProficiencyLevel() == null) {
            skill.setProficiencyLevel(1); // 默认初级
        }
        if (skill.getExperienceYears() == null) {
            skill.setExperienceYears(0);
        }
        // 新增证书或技能时，证书状态默认待审核
        if (skill.getCertificateUrl() != null && !skill.getCertificateUrl().isEmpty()) {
            skill.setCertificateStatus(0);
        }
        staffServiceSkillMapper.insert(skill);
        StaffServiceSkill inserted = staffServiceSkillMapper.selectByStaffIdAndServiceId(
                skill.getStaffId(), skill.getServiceId());
        // 如果有证书，给管理员发送审核通知
        if (inserted.getCertificateUrl() != null && !inserted.getCertificateUrl().isEmpty()) {
            notifyAdminsCertificatePending(inserted);
        }
        return inserted;
    }

    @Override
    @Transactional
    public StaffServiceSkill updateSkill(StaffServiceSkill skill) {
        if (skill.getSkillId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        StaffServiceSkill db = staffServiceSkillMapper.selectById(skill.getSkillId());
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_SKILL_NOT_FOUND);
        }
        // 如果证书有新增或变更，置为待审核
        if (skill.getCertificateUrl() != null &&
                (db.getCertificateUrl() == null || !db.getCertificateUrl().equals(skill.getCertificateUrl()))) {
            skill.setCertificateStatus(0);
        }
        staffServiceSkillMapper.update(skill);
        StaffServiceSkill updated = staffServiceSkillMapper.selectById(skill.getSkillId());
        if (updated.getCertificateUrl() != null && !updated.getCertificateUrl().isEmpty()
                && (db.getCertificateUrl() == null || !db.getCertificateUrl().equals(updated.getCertificateUrl()))) {
            notifyAdminsCertificatePending(updated);
        }
        return updated;
    }

    @Override
    @Transactional
    public void deleteSkill(Long skillId) {
        StaffServiceSkill skill = staffServiceSkillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.SERVICE_SKILL_NOT_FOUND);
        }
        staffServiceSkillMapper.deleteById(skillId);
    }

    @Override
    public StaffServiceSkill getById(Long skillId) {
        StaffServiceSkill skill = staffServiceSkillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ResultCode.SERVICE_SKILL_NOT_FOUND);
        }
        return skill;
    }

    private void notifyAdminsCertificatePending(StaffServiceSkill skill) {
        Staff staff = staffMapper.selectById(skill.getStaffId());
        if (staff == null) {
            return;
        }
        java.util.List<User> admins = userMapper.selectAdmins();
        if (admins == null || admins.isEmpty()) {
            return;
        }
        String title = "新的技能证书待审核";
        String content = String.format("服务人员【%s】(ID:%d) 上传或更新了服务(ID:%d)的技能证书，请尽快审核。",
                staff.getName(), staff.getStaffId(), skill.getServiceId());
        for (User admin : admins) {
            messageService.sendMessage(admin.getUserId(), "system", title, content, skill.getSkillId());
        }
    }
}

