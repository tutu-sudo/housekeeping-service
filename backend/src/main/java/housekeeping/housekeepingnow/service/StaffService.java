package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Staff;

/**
 * 服务人员服务接口
 */
public interface StaffService {
    
    /**
     * 根据服务人员ID查询
     */
    Staff getById(Long staffId);
    
    /**
     * 根据用户ID查询
     */
    Staff getByUserId(Long userId);
    
    /**
     * 更新服务人员信息
     */
    Staff updateStaff(Staff staff);
    
    /**
     * 更新服务人员基本信息（姓名、性别、出生日期、籍贯、身份证号、工作经验等）
     */
    Staff updateBasicInfo(Long staffId, Staff staff);
    
    /**
     * 更新服务人员简历
     */
    Staff updateResume(Long staffId, String resume);
    
    /**
     * 更新服务人员头像
     */
    Staff updateAvatar(Long staffId, String avatar);
}

