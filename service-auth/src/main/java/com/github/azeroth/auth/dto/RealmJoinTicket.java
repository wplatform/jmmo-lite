package com.github.azeroth.auth.dto;

import lombok.Data;

@Data
public class RealmJoinTicket {

    private String gameAccount;
    private int platform;
    private int type;
    private int clientArch;
}
