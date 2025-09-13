package game;

public enum WardenActions {
    Log,
    kick,
    Ban;

    public static final int SIZE = Integer.SIZE;

    public static WardenActions forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
