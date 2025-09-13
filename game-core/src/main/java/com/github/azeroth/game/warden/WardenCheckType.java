package game;

public enum WardenCheckType {
    NONE(0),
    Timing(87), // nyi
    Driver(113), // uint seed + byte[20] SHA1 + byte driverNameIndex (check to ensure driver isn't loaded)
    Proc(126), // nyi
    LuaEval(139), // evaluate arbitrary Lua check
    Mpq(152), // get hash of MPQ file (to check it is not modified)
    PageA(178), // scans all pages for specified SHA1 hash
    PageB(191), // scans only pages starts with MZ+PE headers for specified hash
    module(217), // check to make sure module isn't injected
    Mem(243); // retrieve specific memory

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, WardenCheckType> mappings;
    private int intValue;

    private WardenCheckType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, WardenCheckType> getMappings() {
        if (mappings == null) {
            synchronized (WardenCheckType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, WardenCheckType>();
                }
            }
        }
        return mappings;
    }

    public static WardenCheckType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
