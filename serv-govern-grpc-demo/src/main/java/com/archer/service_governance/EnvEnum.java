package com.archer.service_governance;

/**
 * 环境信息
 */
public enum EnvEnum {
    DEV("dev"), FAT("fat"), UAT("uat"), PROD("prod");

    private String envName;

    EnvEnum(String envName) {
        this.envName = envName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

}
