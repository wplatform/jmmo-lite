package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ServerPacket;

public class SummonRequest extends ServerPacket {
    public ObjectGuid summonerGUID = ObjectGuid.EMPTY;
    public int summonerVirtualRealmAddress;
    public int areaID;
    public Summonreason reason = SummonReason.values()[0];
    public boolean skipStartingArea;
    public SummonRequest() {
        super(ServerOpcode.SummonRequest);
    }

    @Override
    public void write() {
        this.writeGuid(summonerGUID);
        this.writeInt32(summonerVirtualRealmAddress);
        this.writeInt32(areaID);
        this.writeInt8((byte) reason.getValue());
        this.writeBit(skipStartingArea);
        this.flushBits();
    }

    public enum SummonReason {
        spell(0),
        Scenario(1);

        public static final int SIZE = Integer.SIZE;
        private static java.util.HashMap<Integer, SummonReason> mappings;
        private int intValue;

        private SummonReason(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static java.util.HashMap<Integer, SummonReason> getMappings() {
            if (mappings == null) {
                synchronized (SummonReason.class) {
                    if (mappings == null) {
                        mappings = new java.util.HashMap<Integer, SummonReason>();
                    }
                }
            }
            return mappings;
        }

        public static SummonReason forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }
}
