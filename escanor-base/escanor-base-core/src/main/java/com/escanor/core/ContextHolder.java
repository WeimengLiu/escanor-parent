package com.escanor.core;

import com.escanor.core.context.UserContext;
import com.escanor.core.exception.ResponseException;
import org.springframework.util.StringUtils;

public class ContextHolder {

    private ContextHolder() {
        throw new IllegalStateException("Utility class");
    }

    static ThreadLocal<String> code = new ThreadLocal<>();

    static ThreadLocal<UserContext> user = new ThreadLocal<>();

    public static String getCode() {
        return code.get();
    }

    public static void setCode(String code) {
        ContextHolder.code.set(code);
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
