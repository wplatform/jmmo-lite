package com.github.azeroth.portal.realm;

import lombok.Data;

import java.net.InetAddress;

@Data
public class Realm {

    public static final int[] CONFIG_ID_BY_TYPE = new int[] { //size MAX_CLIENT_REALM_TYPE
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
    };

    public static final int REALM_TYPE_NORMAL       = 0;
    public static final int REALM_TYPE_PVP          = 1;
    public static final int REALM_TYPE_NORMAL2      = 4;
    public static final int REALM_TYPE_RP           = 6;
    public static final int REALM_TYPE_RPPVP        = 8;
    public static final int MAX_CLIENT_REALM_TYPE   = 14;
    public static final int REALM_TYPE_FFA_PVP      = 16;

    public static final int REALM_FLAG_NONE             = 0x00;
    public static final int REALM_FLAG_VERSION_MISMATCH = 0x01;
    public static final int REALM_FLAG_OFFLINE          = 0x02;
    public static final int REALM_FLAG_SPECIFY_BUILD    = 0x04;
    public static final int REALM_FLAG_UNK1             = 0x08;
    public static final int REALM_FLAG_UNK2             = 0x10;
    public static final int REALM_FLAG_RECOMMENDED      = 0x20;
    public static final int REALM_FLAG_NEW              = 0x40;
    public static final int REALM_FLAG_FULL             = 0x80;


    private RealmKey id;
    private int build;
    private InetAddress externalAddress;
    private InetAddress localAddress;
    private InetAddress localSubnetMask;
    private int port;
    private String name;
    private String normalizedName;
    private int type;
    private int flags;
    private int timezone;
    private int allowedSecurityLevel;
    private float populationLevel;
}
