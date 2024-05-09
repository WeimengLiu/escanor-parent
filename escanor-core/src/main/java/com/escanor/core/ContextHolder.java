package com.escanor.core;

import com.escanor.core.context.UserContext;
import com.escanor.core.exception.ResponseException;
import org.springframework.util.StringUtils;

public class ContextHolder {
    static volatile int APP_READY_STATUS = 0;

    static ThreadLocal<String> CODE = new ThreadLocal<>();

    static ThreadLocal<UserContext> user = new ThreadLocal<>();

    public static void setApplicationReady() {
        APP_READY_STATUS = 1;
    }

    public static boolean isApplicationReady() {
        return APP_READY_STATUS == 1;
    }

    public static String getCode() {
        return CODE.get();
    }

    public static void setCode(String code) {
        CODE.set(code);
    }

    public static String getUsername() {
        UserContext userContext = user.get();
        if (null == userContext || !StringUtils.hasText(userContext.getUsername())) {
            throw new ResponseException("用户上下文为空");
        }
        return userContext.getUsername();
    }

    public static void setUser(UserContext userContext) {
        user.set(userContext);
    }

    public static void cleanContext() {
        user.remove();
    }
}
