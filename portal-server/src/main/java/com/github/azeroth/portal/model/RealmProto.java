package com.github.azeroth.portal.model;

import lombok.Data;

import java.util.List;

public class RealmProto {



    @Data
    public static class RealmListTicketIdentity {
        private int gameAccountID;
        private int gameAccountRegion;
    }

    @Data
    public static class ClientVersion {
        private int versionMajor;
        private int versionMinor;
        private int versionRevision;
        private int versionBuild;
    }

    @Data
    public static class ClientInformation {
        private int platform;
        private String buildVariant;
        private int type;
        private String timeZone;
        private long currentTime;
        private long textLocale;
        private long audioLocale;
        private long versionDataBuild;
        private ClientVersion version;

        private int[] secret;
        private int clientArch;
        private String systemVersion;
        private int platformType;
        private int systemArch;
    }

    @Data
    public static class RealmListTicketClientInformation {
        private ClientInformation info;
    }

    @Data
    public static class RealmCharacterCountEntry {
        private int wowRealmAddress;
        private int count;
    }

    @Data
    public static class RealmCharacterCountList {
        private List<RealmCharacterCountEntry> counts;
    }

    @Data
    public static class RealmEntry {
        private int wowRealmAddress;
        private int cfgTimezonesID;
        private int populationState;
        private int cfgCategoriesID;
        private ClientVersion version;
        private int cfgRealmsID;
        private int flags;
        private String name;
        private int cfgConfigsID;
        private int cfgLanguagesID;
    }

    @Data
    public static class RealmState {
        private RealmEntry update;
        private boolean deleting;
    }

    @Data
    public static class RealmListUpdates {
        private List<RealmState> updates;
    }

    @Data
    public static class IPAddress {
        private String ip;
        private int port;
    }

    @Data
    public static class RealmIPAddressFamily {
        private int family;
        List<IPAddress> addresses;
    }

    @Data
    public static class RealmListServerIPAddresses {
        List<RealmIPAddressFamily> families;
    }

    @Data
    public static class Bytes {
        private byte[] bytes;
    }


}
