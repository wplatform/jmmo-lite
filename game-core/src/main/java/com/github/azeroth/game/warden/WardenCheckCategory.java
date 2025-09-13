package game;

public enum WardenCheckCategory {
    Inject(0), // checks that test whether the client's execution has been interfered with
    Lua(1), // checks that test whether the lua sandbox has been modified
    Modded(2), // checks that test whether the client has been modified

    max(3); // SKIP

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, WardenCheckCategory> mappings;
    private int intValue;

    private WardenCheckCategory(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, WardenCheckCategory> getMappings() {
        if (mappings == null) {
            synchronized (WardenCheckCategory.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, WardenCheckCategory>();
                }
            }
        }
        return mappings;
    }

    public static WardenCheckCategory forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
