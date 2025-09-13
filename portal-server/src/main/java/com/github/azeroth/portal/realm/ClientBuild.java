package com.github.azeroth.portal.realm;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ClientBuild {


    public static final Set<String> PLATFORM_TYPE = Set.of("Win", "Mac");
    public static final Set<String> PLATFORM = Set.of("Win_x86", "Win_x64", "Win_arm64", "Mac_x86", "Mac_x64", "Mac_arm64");
    public static final Set<String> ARCH = Set.of("x86", "x64", "A32", "A64", "WA32");
    public static final Set<String> TYPE = Set.of("WoW", "WoWC", "WoWB", "WoWE", "WoWT", "WoWR");


    public static final int HOTFIX_VERSION_LENGTH = 4;
    public static final int AUTH_SEED_LENGTH = 16;

    private int build;
    private int majorVersion;
    private int minorVersion;
    private int bugfixVersion;
    private char[] hotfixVersion;
    private List<AuthKey> AuthKeys;

    public record VariantId(String platform, String arch, String type) {
    }

    public record AuthKey(VariantId variant, byte[] key) {
    }


}


