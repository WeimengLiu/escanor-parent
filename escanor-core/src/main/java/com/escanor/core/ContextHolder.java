package com.escanor.core;

import com.escanor.core.context.UserContext;

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
        return null != user.get() ? "" : user.get().getUsername();
    }

    public static void setUser(UserContext userContext) {
        user.set(userContext);
    }
}
