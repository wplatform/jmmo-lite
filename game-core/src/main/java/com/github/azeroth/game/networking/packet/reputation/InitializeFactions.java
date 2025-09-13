package com.github.azeroth.game.networking.packet.reputation;


import com.github.azeroth.game.networking.ServerPacket;

public class InitializeFactions extends ServerPacket {

    private static final short FACTIONCOUNT = 443;
    public int[] factionStandings = new int[FactionCount];
    public boolean[] factionHasBonus = new boolean[FactionCount]; //@todo: implement faction bonus
    public ReputationFlags[] factionFlags = new ReputationFlags[FactionCount];

    public InitializeFactions() {
        super(ServerOpcode.InitializeFactions);
    }

    @Override
    public void write() {
        for (short i = 0; i < FACTIONCOUNT; ++i) {
            this.writeInt16((short) ((short) FactionFlags[i] & 0xFF));
            this.writeInt32(FactionStandings[i]);
        }

        for (short i = 0; i < FACTIONCOUNT; ++i) {
            this.writeBit(FactionHasBonus[i]);
        }

        this.flushBits();
    }
}
