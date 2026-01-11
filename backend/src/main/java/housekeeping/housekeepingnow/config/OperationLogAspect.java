package housekeeping.housekeepingnow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import housekeeping.housekeepingnow.entity.OperationLog;
import housekeeping.housekeepingnow.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 简单的操作日志 AOP
 * 目前拦截 controller 包下所有公共接口方法，记录请求/响应状态
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("execution(public * housekeeping.housekeepingnow.controller..*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        OperationLog log = new OperationLog();
        log.setOperationTime(LocalDateTime.now());

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
            try {
                log.setRequestParams(objectMapper.writeValueAsString(request.getParameterMap()));
            } catch (Exception ignored) {
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.setOperationModule(signature.getDeclaringType().getSimpleName());
        log.setOperationType(detectOperationType(signature));

        try {
            Object result = joinPoint.proceed();
            log.setOperationStatus(1);
            return result;
        } catch (Throwable ex) {
            log.setOperationStatus(0);
            log.setErrorMessage(ex.getMessage());
            throw ex;
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (log.getOperationDesc() == null) {
                log.setOperationDesc("接口调用，用时 " + cost + " ms");
            }
            try {
                operationLogMapper.insert(log);
            } catch (Exception ignored) {
            }
        }
    }

    private String detectOperationType(MethodSignature signature) {
        String name = signature.getMethod().getName().toLowerCase();
        if (name.startsWith("get") || name.startsWith("list")) {
            return "query";
        } else if (name.startsWith("create") || name.startsWith("save") || name.startsWith("add")) {
            return "create";
        } else if (name.startsWith("update") || name.startsWith("edit") || name.startsWith("change")) {
            return "update";
        } else if (name.startsWith("delete") || name.startsWith("remove")) {
            return "delete";
        }
        return "other";
    }
}


