package com.github.azeroth.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountInfo {
    private Integer accountId;
    private String accountName;
    private byte[] sessionKey;
    private String lastIp;
    private Short locked;
    private String lockCountry;
    private Integer expansion;
    private Long muteTime;
    private Integer clientBuild;
    private Integer locale;
    private Integer recruiter;
    private String os;
    private Integer timezoneOffset;
    private Integer bnetAccountId;
    private Integer securityLevel;
    private Boolean isBnetBanned;
    private Boolean isBanned;
    private Integer recruiterId;
    private String recruiterName;

}