package com.archer.service_governance.enums;

import org.apache.commons.lang3.StringUtils;

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

    public static EnvEnum getEnvEnum(String envName) {
        EnvEnum res = EnvEnum.DEV;
        if (StringUtils.isBlank(envName)) {
            return res;
        }
        for (EnvEnum envEnum : EnvEnum.values()) {
            if (StringUtils.equalsIgnoreCase(envEnum.envName, envName)) {
                return envEnum;
            }
        }
        return res;
    }
}
