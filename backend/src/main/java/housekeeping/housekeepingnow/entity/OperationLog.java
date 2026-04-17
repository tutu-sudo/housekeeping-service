package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 业务操作日志实体类
 */
@Data
public class OperationLog {
    /**
     * 日志ID
     */
    private Long logId;
    
    /**
     * 操作用户ID
     */
    private Long userId;
    
    /**
     * 操作类型：create-创建，update-更新，delete-删除，login-登录，logout-登出
     */
    private String operationType;
    
    /**
     * 操作模块：user-用户，service-服务，appointment-预约，order-订单，payment-支付等
     */
    private String operationModule;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 请求方法：GET，POST，PUT，DELETE
     */
    private String requestMethod;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer operationStatus;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
}

