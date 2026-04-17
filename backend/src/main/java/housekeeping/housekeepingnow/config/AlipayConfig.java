package housekeeping.housekeepingnow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayConfig {
    
    /**
     * 应用ID（APPID）
     */
    private String appId;
    
    /**
     * 应用私钥
     */
    private String privateKey;
    
    /**
     * 支付宝公钥
     */
    private String publicKey;
    
    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;
    
    /**
     * 支付结果异步通知地址
     */
    private String notifyUrl;
    
    /**
     * 支付结果同步跳转地址（支付完成后跳转）
     */
    private String returnUrl;

    /**
     * 前端支付成功落地页（后端同步回调处理完后跳转到该地址）
     * 例如：http://localhost:3000/#/payment/success
     */
    private String frontendSuccessUrl;

    /**
     * 前端支付失败落地页（后端同步回调处理完后跳转到该地址）
     * 例如：http://localhost:3000/#/payment/fail
     */
    private String frontendFailUrl;
    
    /**
     * 沙箱买家UID（用于测试）
     */
    private String sandboxBuyerId;
    
    /**
     * 签名方式（固定为RSA2）
     */
    private String signType = "RSA2";
    
    /**
     * 字符编码格式（固定为UTF-8）
     */
    private String charset = "UTF-8";
    
    /**
     * 数据格式（固定为JSON）
     */
    private String format = "JSON";
}

