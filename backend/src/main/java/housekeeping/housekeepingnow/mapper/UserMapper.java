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
}

