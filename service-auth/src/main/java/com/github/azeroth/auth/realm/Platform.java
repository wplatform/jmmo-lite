package com.github.azeroth.auth.realm;

import com.github.azeroth.utils.Utils;

public enum Platform {

    WIN_X86     ("Win"),
    WIN_X64     ("Wn64"),
    WIN_ARM64   ("WinA"),
    MAC_X86     ("Mac"),
    MAC_X64     ("Mc64"),
    MAC_ARM64   ("MacA");
    public final int fourcc;
    Platform(String platform) {
        this.fourcc = Utils.fourCharValue(platform);
    }


    public static boolean isValid(String os) {
        int i = Utils.fourCharValue(os);
        return i == WIN_X86.fourcc || i == WIN_X64.fourcc || i == WIN_ARM64.fourcc ||
                i == MAC_X86.fourcc || i == MAC_X64.fourcc || i == MAC_ARM64.fourcc;
    }
}
