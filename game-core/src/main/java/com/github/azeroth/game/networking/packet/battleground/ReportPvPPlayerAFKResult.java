package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class ReportPvPPlayerAFKResult extends ServerPacket {
    public ObjectGuid offender = ObjectGuid.EMPTY;
    public byte numPlayersIHaveReported = 0;
    public byte numBlackMarksOnOffender = 0;
    public resultCode result = resultCode.GenericFailure;
    public ReportPvPPlayerAFKResult() {
        super(ServerOpcode.ReportPvpPlayerAfkResult);
    }

    @Override
    public void write() {
        this.writeGuid(offender);
        this.writeInt8((byte) result.getValue());
        this.writeInt8(numBlackMarksOnOffender);
        this.writeInt8(numPlayersIHaveReported);
    }

    public enum ResultCode {
        success(0),
        GenericFailure(1), // there are more error codes but they are impossible to receive without modifying the client
        AFKSystemEnabled(5),
        AFKSystemDisabled(6);

        public static final int SIZE = Integer.SIZE;
        private static java.util.HashMap<Integer, resultCode> mappings;
        private int intValue;

        private resultCode(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static java.util.HashMap<Integer, resultCode> getMappings() {
            if (mappings == null) {
                synchronized (resultCode.class) {
                    if (mappings == null) {
                        mappings = new java.util.HashMap<Integer, resultCode>();
                    }
                }
            }
            return mappings;
        }

        public static ResultCode forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }
}
