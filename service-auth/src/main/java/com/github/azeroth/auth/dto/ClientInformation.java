package com.github.azeroth.auth.dto;

import lombok.Data;

@Data
public class ClientInformation {
    private int platform;
    private String buildVariant;
    private int type;
    private String timeZone;
    private long currentTime;
    private int textLocale;
    private int audioLocale;
    private int versionDataBuild;
    private ClientVersion version;
    private byte[] secret;
    private int clientArch;
    private String systemVersion;
    private int platformType;
    private int systemArch;
}
