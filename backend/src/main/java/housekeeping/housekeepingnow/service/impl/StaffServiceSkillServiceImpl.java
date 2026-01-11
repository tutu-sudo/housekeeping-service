package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
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
        staffServiceSkillMapper.insert(skill);
        return staffServiceSkillMapper.selectByStaffIdAndServiceId(
                skill.getStaffId(), skill.getServiceId());
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
        staffServiceSkillMapper.update(skill);
        return staffServiceSkillMapper.selectById(skill.getSkillId());
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
}

