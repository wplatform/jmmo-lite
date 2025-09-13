package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class PVPMatchInitialize extends ServerPacket {
    public int mapID;
    public Matchstate state = MatchState.inactive;
    public long startTime;
    public int duration;
    public RatedMatchdeserterPenalty deserterPenalty;
    public byte arenaFaction;
    public int battlemasterListID;
    public boolean registered;
    public boolean affectsRating;
    public PVPMatchInitialize() {
        super(ServerOpcode.PvpMatchInitialize);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeInt8((byte) state.getValue());
        this.writeInt64(startTime);
        this.writeInt32(duration);
        this.writeInt8(arenaFaction);
        this.writeInt32(battlemasterListID);
        this.writeBit(registered);
        this.writeBit(affectsRating);
        this.writeBit(deserterPenalty != null);
        this.flushBits();

        if (deserterPenalty != null) {
            deserterPenalty.write(this);
        }
    }

    public enum MatchState {
        inProgress(1),
        Complete(3),
        inactive(4);

        public static final int SIZE = Integer.SIZE;
        private static java.util.HashMap<Integer, MatchState> mappings;
        private int intValue;

        private MatchState(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static java.util.HashMap<Integer, MatchState> getMappings() {
            if (mappings == null) {
                synchronized (MatchState.class) {
                    if (mappings == null) {
                        mappings = new java.util.HashMap<Integer, MatchState>();
                    }
                }
            }
            return mappings;
        }

        public static MatchState forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }
}
