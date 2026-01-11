package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.mapper.StaffMapper;
import housekeeping.housekeepingnow.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffMapper staffMapper;

    @Override
    public Staff getById(Long staffId) {
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        return staff;
    }

    @Override
    public Staff getByUserId(Long userId) {
        System.out.println("=== StaffServiceImpl.getByUserId ===");
        System.out.println("查询参数 userId: " + userId);
        Staff staff = staffMapper.selectByUserId(userId);
        if (staff == null) {
            System.err.println("错误：未找到userId=" + userId + "对应的服务人员信息");
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        System.out.println("查询结果: staffId=" + staff.getStaffId() + ", userId=" + staff.getUserId() + ", name=" + staff.getName());
        System.out.println("=====================================");
        return staff;
    }

    @Override
    @Transactional
    public Staff updateStaff(Staff staff) {
        if (staff.getStaffId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Staff db = staffMapper.selectById(staff.getStaffId());
        if (db == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        staffMapper.update(staff);
        return staffMapper.selectById(staff.getStaffId());
    }

    @Override
    @Transactional
    public Staff updateBasicInfo(Long staffId, Staff staff) {
        Staff db = staffMapper.selectById(staffId);
        if (db == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        staff.setStaffId(staffId);
        staffMapper.update(staff);
        return staffMapper.selectById(staffId);
    }

    @Override
    @Transactional
    public Staff updateResume(Long staffId, String resume) {
        Staff db = staffMapper.selectById(staffId);
        if (db == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        Staff update = new Staff();
        update.setStaffId(staffId);
        update.setResume(resume);
        staffMapper.update(update);
        return staffMapper.selectById(staffId);
    }

    @Override
    @Transactional
    public Staff updateAvatar(Long staffId, String avatar) {
        Staff db = staffMapper.selectById(staffId);
        if (db == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        Staff update = new Staff();
        update.setStaffId(staffId);
        update.setAvatar(avatar);
        staffMapper.update(update);
        return staffMapper.selectById(staffId);
    }
}

