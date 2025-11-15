package com.github.azeroth.auth.realm;

import com.github.azeroth.utils.Utils;

public enum Arch {
    X86("x86"),
    X64("x64"),
    ARM32("A32"),
    Arm64("A64"),
    WA32("WA32");
    public final int fourcc;

    Arch(String arch) {
        this.fourcc = Utils.fourCharValue(arch);
    }

    public static boolean isValid(String arch) {
        if (arch.length() > 4)
            return false;
        int i = Utils.fourCharValue(arch);
        return i == X86.fourcc || i == X64.fourcc || i == ARM32.fourcc || i == Arm64.fourcc || i == WA32.fourcc;
    }
}
