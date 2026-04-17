package housekeeping.housekeepingnow.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要特定角色注解
 * 标记在Controller方法上，表示该接口需要特定用户类型才能访问
 * userTypes: 1-顾客，2-服务人员，3-管理员
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 允许的用户类型数组
     */
    int[] value();
}

