package com.escanor.multidatasource.config;

import com.zaxxer.hikari.HikariConfig;

public class MyHikariConfig extends HikariConfig {

    public MyHikariConfig(){
        super();
    }

    private String[] supports = new String[0];

    public String[] getSupports() {
        return supports;
    }

    public void setSupports(String[] supports) {
        this.supports = supports;
    }
}
