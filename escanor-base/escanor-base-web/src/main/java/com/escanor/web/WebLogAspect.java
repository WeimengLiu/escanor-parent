package com.escanor.web;

import com.escanor.core.util.JSON;
import com.escanor.web.config.BaseWebProperties;
import com.escanor.web.util.IPUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Aspect
@Component
@Order(-5)
@ConditionalOnExpression("${escanor.web.logAspect:false}")
public class WebLogAspect {
    private final Log logger = LogFactory.getLog(this.getClass());
    @Autowired
    private BaseWebProperties baseWebProperties;
    private long start;

    @Pointcut("execution(public * com.escanor.*.controller..*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        if (!baseWebProperties.isLogAspect()) {
            return;
        }

        start = System.currentTimeMillis();
        logger.info("WebLogAspect.doBefore()");

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        logRequestDetails(joinPoint, request);
    }

    private void logRequestDetails(JoinPoint joinPoint, HttpServletRequest request) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("URL : ").append(request.getRequestURL().toString()).append("\n").append("HTTP_METHOD : ").append(request.getMethod()).append("\n").append("IP : ").append(IPUtil.getIp(request)).append("\n").append("CLASS_METHOD : ").append(joinPoint.getSignature().getDeclaringTypeName()).append(".").append(joinPoint.getSignature().getName()).append("\n").append("ARGS : ").append(JSON.toJSONString(joinPoint.getArgs())).append("\n").append("HEADER START:-----------------------\n");

        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String key = headers.nextElement();
            String value = request.getHeader(key);
            logMessage.append("Header : [key:").append(key).append("],[value:").append(value).append("]\n");
        }
        logMessage.append("HEADER END:-----------------------");

        logger.info(logMessage.toString());
    }

    @After("webLog()")
    public void doAfter(JoinPoint joinPoint) {
        long end = System.currentTimeMillis();
        logger.info("Method " + joinPoint.getSignature() + " took " + (end - start) + "ms!");
    }
}