package com.escanor.core;

public class ContextHolder {
    static volatile int APP_READY_STATUS = 0;

    static ThreadLocal<String> CODE = new ThreadLocal<>();

    public static void setApplicationReady() {
        APP_READY_STATUS = 1;
    }

    public static boolean isApplicationReady() {
        return APP_READY_STATUS == 1;
    }

    public static String getCode() {
        return CODE.get();
    }

    public static void setCode(String bankCode) {
        CODE.set(bankCode);
    }
}
