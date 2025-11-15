package com.github.azeroth.auth.realm;

import com.github.azeroth.utils.Utils;

public enum PlatformType {

    Windows("Win"),
    macOS("Mac");
    public final int fourcc;

    PlatformType(String platformType) {
        this.fourcc = Utils.fourCharValue(platformType);
    }

    public static boolean isValid(String platformType) {
        if (platformType.length() > 4)
            return false;
        int i = Utils.fourCharValue(platformType);
        return i == Windows.fourcc || i == macOS.fourcc;
    }
}
