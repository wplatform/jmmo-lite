package com.github.azeroth.portal.realm;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RealmKey {

    final byte region;
    final byte site;
    final int realm;


    public static RealmKey createRealmKey(int region, int site, int realm) {
        return new RealmKey((byte) region, (byte) site, realm);
    }

    public static RealmKey createRealmKey(int realmAddress) {
        return new RealmKey((byte) ((realmAddress >> 24) & 0xFF), (byte) ((realmAddress >> 16) & 0xFF), (realmAddress & 0xFFFF));
    }

    public static String getAddressString(int region, int site, int realm) {
        return String.format("%d-%d-%d", region, site, realm);
    }

    public static String getSubRegionAddressString(int region, int site) {
        return String.format("%d-%d-0", region, site);
    }

    public String toAddressString() {
        return getAddressString(region, site, realm);
    }

    public String toSubRegionAddressString() {
        return getSubRegionAddressString(region, site);
    }

    public int getAddress() {
        return (region << 24) | (site << 16) | (realm & 0xffff);
    }
}
