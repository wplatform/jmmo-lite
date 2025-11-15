package com.github.azeroth.auth.realm;

import com.github.azeroth.utils.Utils;

public enum Type {
    Retail("WoW"),
    RetailChina("WoWC"),
    Beta("WoWB"),
    BetaRelease("WoWE"),
    Ptr("WoWT"),
    PtrRelease("WoWR");
    public final int fourcc;

    Type(String type) {
        this.fourcc = Utils.fourCharValue(type);
    }

    public static boolean isValid(String type) {
        if (type.length() > 4)
            return false;
        int i = Utils.fourCharValue(type);
        return i == Retail.fourcc || i == RetailChina.fourcc || i == Beta.fourcc ||
                i == BetaRelease.fourcc || i == Ptr.fourcc || i == PtrRelease.fourcc;
    }
}
