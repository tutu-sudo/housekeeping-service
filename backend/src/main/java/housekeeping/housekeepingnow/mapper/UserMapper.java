package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据用户ID查询
     */
    User selectById(@Param("userId") Long userId);
    
    /**
     * 根据用户名查询
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据手机号查询
     */
    User selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据邮箱查询
     */
    java.util.List<User> selectByEmail(@Param("email") String email);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户
     */
    int update(User user);
    
    /**
     * 根据用户ID删除
     */
    int deleteById(@Param("userId") Long userId);

    /**
     * 查询所有管理员用户
     */
    java.util.List<User> selectAdmins();

    /**
     * 后台-按关键字搜索用户（username/phone 模糊匹配）
     */
    java.util.List<housekeeping.housekeepingnow.dto.AdminUserSearchItemDTO> searchUsers(@Param("keyword") String keyword,
                                                                                       @Param("limit") Integer limit);

    /**
     * 后台-分页查询普通用户（user_type=1）
     */
    java.util.List<housekeeping.housekeepingnow.dto.AdminNormalUserItemDTO> selectNormalUsers(@Param("keyword") String keyword,
                                                                                              @Param("offset") Integer offset,
                                                                                              @Param("size") Integer size);
}

