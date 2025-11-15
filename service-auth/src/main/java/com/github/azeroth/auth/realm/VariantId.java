package com.github.azeroth.auth.realm;

import com.github.azeroth.utils.StringUtil;
import com.github.azeroth.utils.Utils;

public record VariantId(int platform, int arch, int type) {

    public static VariantId of(int platform, int arch, int type) {
        return new VariantId(platform, arch, type);
    }

    public static VariantId of(String platform, String arch, String type) {
        return new VariantId(Utils.fourCharValue(platform), Utils.fourCharValue(arch), Utils.fourCharValue(type));
    }

    @Override
    public String toString() {
        return StringUtil.format("{}-{}-{}", Utils.toFourChar(platform), Utils.toFourChar(arch), Utils.toFourChar(type));
    }
}
