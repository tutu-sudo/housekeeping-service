package housekeeping.housekeepingnow.common.enums;

/**
 * 统一错误代码枚举
 * 
 * @author housekeeping
 */
public enum ResultCode {
    
    // ========== 通用错误码 ==========
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // ========== 用户相关错误码 1000-1099 ==========
    USER_NOT_FOUND(1000, "用户不存在"),
    USER_ALREADY_EXISTS(1001, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_LOCKED(1004, "用户已被锁定"),
    PASSWORD_ERROR(1005, "密码错误"),
    OLD_PASSWORD_ERROR(1006, "原密码错误"),
    PASSWORD_TOO_SIMPLE(1007, "密码过于简单"),
    
    // ========== 认证相关错误码 1100-1199 ==========
    TOKEN_INVALID(1100, "Token无效"),
    TOKEN_EXPIRED(1101, "Token已过期"),
    TOKEN_MISSING(1102, "Token缺失"),
    REFRESH_TOKEN_INVALID(1103, "刷新Token无效"),
    REFRESH_TOKEN_EXPIRED(1104, "刷新Token已过期"),
    LOGIN_REQUIRED(1105, "请先登录"),
    PERMISSION_DENIED(1106, "权限不足"),
    
    // ========== 验证码相关错误码 1200-1299 ==========
    CAPTCHA_ERROR(1200, "验证码错误"),
    CAPTCHA_EXPIRED(1201, "验证码已过期"),
    CAPTCHA_SEND_FAILED(1202, "验证码发送失败"),
    SMS_SEND_FAILED(1203, "短信发送失败"),
    EMAIL_SEND_FAILED(1204, "邮件发送失败"),
    
    // ========== 服务相关错误码 2000-2099 ==========
    SERVICE_NOT_FOUND(2000, "服务不存在"),
    SERVICE_DISABLED(2001, "服务已停用"),
    SERVICE_TYPE_NOT_FOUND(2002, "服务类型不存在"),
    SERVICE_SKILL_NOT_FOUND(2003, "服务技能不存在"),
    SERVICE_SKILL_ALREADY_EXISTS(2004, "服务技能已存在"),
    
    // ========== 服务人员相关错误码 2100-2199 ==========
    STAFF_NOT_FOUND(2100, "服务人员不存在"),
    STAFF_NOT_VERIFIED(2101, "服务人员未通过审核"),
    STAFF_DISABLED(2102, "服务人员已停用"),
    STAFF_ALREADY_EXISTS(2103, "服务人员已存在"),
    CERTIFICATE_NOT_FOUND(2104, "证书不存在"),
    CERTIFICATE_UPLOAD_FAILED(2105, "证书上传失败"),
    
    // ========== 预约相关错误码 3000-3099 ==========
    APPOINTMENT_NOT_FOUND(3000, "预约记录不存在"),
    APPOINTMENT_ALREADY_EXISTS(3001, "预约记录已存在"),
    APPOINTMENT_TIME_CONFLICT(3002, "预约时间冲突"),
    APPOINTMENT_STATUS_ERROR(3003, "预约状态错误"),
    APPOINTMENT_CANNOT_CANCEL(3004, "当前状态无法取消预约"),
    APPOINTMENT_CANNOT_MODIFY(3005, "当前状态无法修改预约"),
    APPOINTMENT_TIME_INVALID(3006, "预约时间无效"),
    
    // ========== 订单相关错误码 3100-3199 ==========
    ORDER_NOT_FOUND(3100, "订单不存在"),
    ORDER_STATUS_ERROR(3101, "订单状态错误"),
    ORDER_ALREADY_PAID(3102, "订单已支付"),
    ORDER_NOT_PAID(3103, "订单未支付"),
    ORDER_AMOUNT_ERROR(3104, "订单金额错误"),
    ORDER_CANNOT_CANCEL(3105, "当前状态无法取消订单"),
    
    // ========== 支付相关错误码 3200-3299 ==========
    PAYMENT_NOT_FOUND(3200, "支付记录不存在"),
    PAYMENT_FAILED(3201, "支付失败"),
    PAYMENT_ALREADY_PAID(3202, "订单已支付"),
    PAYMENT_METHOD_NOT_SUPPORTED(3203, "不支持的支付方式"),
    PAYMENT_AMOUNT_ERROR(3204, "支付金额错误"),
    PAYMENT_SIGN_ERROR(3205, "支付签名错误"),
    PAYMENT_CALLBACK_ERROR(3206, "支付回调处理失败"),
    
    // ========== 评价相关错误码 3300-3399 ==========
    REVIEW_NOT_FOUND(3300, "评价不存在"),
    REVIEW_ALREADY_EXISTS(3301, "该订单已评价"),
    REVIEW_CANNOT_MODIFY(3302, "评价无法修改"),
    REVIEW_SCORE_INVALID(3303, "评分无效"),
    
    // ========== 文件相关错误码 4000-4099 ==========
    FILE_NOT_FOUND(4000, "文件不存在"),
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4002, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),
    FILE_DELETE_FAILED(4004, "文件删除失败"),
    
    // ========== 排班相关错误码 5000-5099 ==========
    SCHEDULE_NOT_FOUND(5000, "排班记录不存在"),
    SCHEDULE_TIME_CONFLICT(5001, "排班时间冲突"),
    SCHEDULE_CANNOT_MODIFY(5002, "排班无法修改"),
    
    // ========== 公司信息相关错误码 6000-6099 ==========
    COMPANY_NOT_FOUND(6000, "公司信息不存在"),
    
    // ========== 其他业务错误码 9000-9999 ==========
    DATA_NOT_FOUND(9000, "数据不存在"),
    DATA_ALREADY_EXISTS(9001, "数据已存在"),
    OPERATION_FAILED(9002, "操作失败"),
    OPERATION_NOT_ALLOWED(9003, "操作不允许"),
    SYSTEM_BUSY(9004, "系统繁忙，请稍后重试"),
    REQUEST_TOO_FREQUENT(9005, "请求过于频繁"),
    ;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    /**
     * 构造函数
     */
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取错误码
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 获取错误信息
     */
    public String getMessage() {
        return message;
    }
}

