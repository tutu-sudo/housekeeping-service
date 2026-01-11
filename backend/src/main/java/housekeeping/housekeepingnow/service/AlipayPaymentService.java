package housekeeping.housekeepingnow.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import housekeeping.housekeepingnow.config.AlipayConfig;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝支付服务
 */
@Slf4j
@Service
public class AlipayPaymentService {

    @Autowired
    private AlipayConfig alipayConfig;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建支付宝客户端
     */
    private AlipayClient createAlipayClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getPublicKey(),
                alipayConfig.getSignType()
        );
    }

    /**
     * 创建电脑网站支付（返回HTML表单）
     * 
     * @param appointmentId 预约ID
     * @return 支付页面HTML表单（POST方式）
     */
    public String createPagePay(Long appointmentId) throws AlipayApiException {
        log.info("开始创建支付宝支付页面，预约ID：{}", appointmentId);
        
        // 查询预约信息
        Appointment appointment;
        try {
            appointment = appointmentService.getById(appointmentId);
        } catch (Exception e) {
            log.error("查询预约失败，预约ID：{}", appointmentId, e);
            throw new RuntimeException("预约不存在或查询失败：" + e.getMessage());
        }
        
        if (appointment == null) {
            log.error("预约不存在，预约ID：{}", appointmentId);
            throw new RuntimeException("预约不存在");
        }
        
        // 检查订单金额
        if (appointment.getTotalAmount() == null || appointment.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            log.error("预约金额无效，预约ID：{}，金额：{}", appointmentId, appointment.getTotalAmount());
            throw new RuntimeException("订单总金额不能为空或0，请检查预约信息");
        }
        
        log.info("预约信息：预约ID={}，金额={}，状态={}", appointmentId, appointment.getTotalAmount(), appointment.getStatus());

        // 检查是否已有支付记录
        Payment existingPayment = null;
        try {
            existingPayment = paymentService.getByAppointmentId(appointmentId);
            // 如果已有支付记录且已支付成功，则不允许重复支付
            if (existingPayment != null && existingPayment.getPaymentStatus() != null && existingPayment.getPaymentStatus() == 1) {
                log.warn("该预约已支付成功，无法重复支付，预约ID：{}", appointmentId);
                throw new RuntimeException("该预约已支付成功，无法重复支付");
            }
        } catch (RuntimeException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            // 如果没有支付记录，则创建新的支付记录
            log.info("预约 {} 尚未创建支付记录，将创建新记录", appointmentId);
        }

        // 生成订单号（如果已有支付记录则使用支付ID，否则创建新的支付记录）
        String outTradeNo;
        Payment payment;
        if (existingPayment != null) {
            payment = existingPayment;
            outTradeNo = "APPT" + existingPayment.getPaymentId();
        } else {
            // 创建支付记录
            payment = new Payment();
            payment.setAppointmentId(appointmentId);
            payment.setPaymentAmount(appointment.getTotalAmount());
            payment.setPaymentMethod("alipay");
            payment.setPaymentStatus(0); // 待支付
            payment = paymentService.createPayment(payment); // 创建后会返回包含paymentId的对象
            outTradeNo = "APPT" + payment.getPaymentId();
        }

        // 创建支付宝客户端并调用API
        try {
            log.info("创建支付宝客户端，网关：{}，APPID：{}", alipayConfig.getGatewayUrl(), alipayConfig.getAppId());
            AlipayClient alipayClient = createAlipayClient();

            // 创建请求对象
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            // 设置异步通知地址
            String notifyUrl = alipayConfig.getNotifyUrl();
            String returnUrl = alipayConfig.getReturnUrl();
            log.info("设置回调地址，异步通知：{}，同步跳转：{}", notifyUrl, returnUrl);
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            // 创建业务参数模型
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(outTradeNo); // 商户订单号
            model.setTotalAmount(appointment.getTotalAmount().toString()); // 订单总金额
            model.setSubject("家政服务预约 - 订单号：" + appointmentId); // 订单标题
            model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 产品码（电脑网站支付固定值）
            model.setBody("家政服务预约，预约ID：" + appointmentId); // 订单描述（可选）

            // 如果是沙箱环境且配置了买家UID，可以设置buyerId（可选，仅用于测试）
            // model.setBuyerId(alipayConfig.getSandboxBuyerId());

            // 设置业务参数
            request.setBizModel(model);

            // 执行请求，获取响应（POST方式返回HTML表单）
            log.info("调用支付宝API创建支付页面，订单号：{}", outTradeNo);
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");

            if (response.isSuccess()) {
                log.info("支付宝支付页面创建成功，订单号：{}, 预约ID：{}", outTradeNo, appointmentId);
                return response.getBody();
            } else {
                log.error("支付宝支付页面创建失败，错误码：{}, 错误信息：{}，子错误码：{}，子错误信息：{}", 
                        response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                throw new RuntimeException("创建支付页面失败：" + response.getMsg() + 
                        (response.getSubMsg() != null ? "，" + response.getSubMsg() : ""));
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝API异常，预约ID：{}", appointmentId, e);
            throw new AlipayApiException("调用支付宝API失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("创建支付宝支付页面异常，预约ID：{}", appointmentId, e);
            throw new RuntimeException("创建支付页面异常：" + e.getMessage(), e);
        }
    }

    /**
     * 创建电脑网站支付（返回支付链接，GET方式）
     * 
     * @param appointmentId 预约ID
     * @return 支付页面URL
     */
    public String createPagePayUrl(Long appointmentId) throws AlipayApiException {
        log.info("开始创建支付宝支付链接，预约ID：{}", appointmentId);
        
        // 查询预约信息
        Appointment appointment;
        try {
            appointment = appointmentService.getById(appointmentId);
        } catch (Exception e) {
            log.error("查询预约失败，预约ID：{}", appointmentId, e);
            throw new RuntimeException("预约不存在或查询失败：" + e.getMessage());
        }
        
        if (appointment == null) {
            log.error("预约不存在，预约ID：{}", appointmentId);
            throw new RuntimeException("预约不存在");
        }
        
        // 检查订单金额
        if (appointment.getTotalAmount() == null || appointment.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            log.error("预约金额无效，预约ID：{}，金额：{}", appointmentId, appointment.getTotalAmount());
            throw new RuntimeException("订单总金额不能为空或0，请检查预约信息");
        }
        
        log.info("预约信息：预约ID={}，金额={}，状态={}", appointmentId, appointment.getTotalAmount(), appointment.getStatus());

        // 检查是否已有支付记录
        Payment existingPayment = null;
        try {
            existingPayment = paymentService.getByAppointmentId(appointmentId);
            if (existingPayment != null && existingPayment.getPaymentStatus() != null && existingPayment.getPaymentStatus() == 1) {
                log.warn("该预约已支付成功，无法重复支付，预约ID：{}", appointmentId);
                throw new RuntimeException("该预约已支付成功，无法重复支付");
            }
        } catch (RuntimeException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.info("预约 {} 尚未创建支付记录，将创建新记录", appointmentId);
        }

        // 生成订单号
        String outTradeNo;
        Payment payment;
        if (existingPayment != null) {
            payment = existingPayment;
            outTradeNo = "APPT" + existingPayment.getPaymentId();
        } else {
            // 创建支付记录
            payment = new Payment();
            payment.setAppointmentId(appointmentId);
            payment.setPaymentAmount(appointment.getTotalAmount());
            payment.setPaymentMethod("alipay");
            payment.setPaymentStatus(0);
            payment = paymentService.createPayment(payment); // 创建后会返回包含paymentId的对象
            outTradeNo = "APPT" + payment.getPaymentId();
        }

        // 创建支付宝客户端
        try {
            log.info("创建支付宝客户端，网关：{}，APPID：{}", alipayConfig.getGatewayUrl(), alipayConfig.getAppId());
            AlipayClient alipayClient = createAlipayClient();

            // 创建请求对象
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            String notifyUrl = alipayConfig.getNotifyUrl();
            String returnUrl = alipayConfig.getReturnUrl();
            log.info("设置回调地址，异步通知：{}，同步跳转：{}", notifyUrl, returnUrl);
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            // 创建业务参数模型
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(appointment.getTotalAmount().toString());
            model.setSubject("家政服务预约 - 订单号：" + appointmentId);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setBody("家政服务预约，预约ID：" + appointmentId);

            request.setBizModel(model);

            // GET方式返回支付链接
            log.info("调用支付宝API创建支付链接，订单号：{}", outTradeNo);
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");

            if (response.isSuccess()) {
                log.info("支付宝支付链接创建成功，订单号：{}, 预约ID：{}", outTradeNo, appointmentId);
                return response.getBody();
            } else {
                log.error("支付宝支付链接创建失败，错误码：{}, 错误信息：{}，子错误码：{}，子错误信息：{}", 
                        response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                throw new RuntimeException("创建支付链接失败：" + response.getMsg() + 
                        (response.getSubMsg() != null ? "，" + response.getSubMsg() : ""));
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝API异常，预约ID：{}", appointmentId, e);
            throw new AlipayApiException("调用支付宝API失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("创建支付宝支付链接异常，预约ID：{}", appointmentId, e);
            throw new RuntimeException("创建支付链接异常：" + e.getMessage(), e);
        }
    }

    /**
     * 处理支付宝异步通知（验签并更新支付状态）
     * 
     * @param params 支付宝回调参数
     * @return 是否处理成功
     */
    public boolean handleNotify(Map<String, String> params) {
        log.info("收到支付宝异步通知，参数：{}", params);

        try {
            // 解析订单号（格式：APPT + paymentId）
            String outTradeNo = params.get("out_trade_no");
            if (outTradeNo == null || !outTradeNo.startsWith("APPT")) {
                log.error("无效的订单号：{}", outTradeNo);
                return false;
            }

            // 提取支付ID
            Long paymentId = Long.parseLong(outTradeNo.substring(4));
            Payment payment = paymentService.getById(paymentId);

            // 获取交易状态
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no"); // 支付宝交易号

            // 处理不同的交易状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功
                // 如果当前状态是待支付（0）或支付失败（2），更新为支付成功（1）
                // 如果已经是支付成功（1），不再重复更新，但记录日志
                if (payment.getPaymentStatus() == null || payment.getPaymentStatus() == 0 || payment.getPaymentStatus() == 2) {
                    log.info("更新支付状态为成功，支付ID：{}, 当前状态：{}, 交易号：{}", 
                            paymentId, payment.getPaymentStatus(), tradeNo);
                    payment.setPaymentStatus(1); // 支付成功
                    payment.setTransactionId(tradeNo);
                    payment.setPaymentTime(LocalDateTime.now());
                    paymentService.updatePayment(payment);

                    // 更新预约状态为已确认
                    try {
                        appointmentService.updateStatus(payment.getAppointmentId(), 1);
                        log.info("支付成功，已更新预约状态，支付ID：{}, 预约ID：{}", 
                                paymentId, payment.getAppointmentId());
                    } catch (Exception e) {
                        log.error("更新预约状态失败，支付ID：{}, 预约ID：{}", 
                                paymentId, payment.getAppointmentId(), e);
                    }
                } else if (payment.getPaymentStatus() == 1) {
                    // 已经是支付成功状态，记录日志但不更新
                    log.info("支付状态已是成功，无需更新，支付ID：{}, 交易号：{}", paymentId, tradeNo);
                }
                return true;
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭
                // 只有待支付状态才更新为支付失败，已支付成功的订单不应该被关闭状态覆盖
                if (payment.getPaymentStatus() != null && payment.getPaymentStatus() == 0) {
                    log.info("交易关闭，更新支付状态为失败，支付ID：{}, 交易号：{}", paymentId, tradeNo);
                    payment.setPaymentStatus(2); // 支付失败
                    paymentService.updatePayment(payment);
                } else if (payment.getPaymentStatus() == 1) {
                    // 已支付成功的订单不应被关闭状态覆盖，记录警告日志
                    log.warn("收到交易关闭回调，但订单已支付成功，不更新状态。支付ID：{}, 交易号：{}", 
                            paymentId, tradeNo);
                }
                return true;
            }

            log.warn("未处理的交易状态：{}, 订单号：{}", tradeStatus, outTradeNo);
            return false;

        } catch (Exception e) {
            log.error("处理支付宝异步通知失败", e);
            return false;
        }
    }

    /**
     * 验证支付宝签名
     * 
     * @param params 参数Map
     * @return 签名是否有效
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("支付宝签名验证失败", e);
            return false;
        }
    }
}

