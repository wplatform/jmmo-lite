package com.github.azeroth.game.networking.packet.warden;

public enum WardenOpcodes {
    // client.Server
    CmsgModuleMissing(0),
    CmsgModuleOk(1),
    CmsgCheatChecksResult(2),
    CmsgMemChecksResult(3), // Only Sent If Mem_Check Bytes Doesn'T Match
    CmsgHashResult(4),
    CmsgModuleFailed(5), // This Is Sent When Client Failed To Load Uploaded Module Due To Cache Fail

    // Server.Client
    SmsgModuleUse(0),
    SmsgModuleCache(1),
    SmsgCheatChecksRequest(2),
    SmsgModuleInitialize(3),
    SmsgMemChecksRequest(4), // Byte Len; While (!Eof) { Byte unk(1); Byte index(++); String module(Can Be 0); Int offset; Byte Len; Byte[] Bytes_To_Compare[Len]; }
    SmsgHashRequest(5);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, WardenOpcodes> mappings;
    private int intValue;

    private WardenOpcodes(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, WardenOpcodes> getMappings() {
        if (mappings == null) {
            synchronized (WardenOpcodes.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, WardenOpcodes>();
                }
            }
        }
        return mappings;
    }

    public static WardenOpcodes forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
