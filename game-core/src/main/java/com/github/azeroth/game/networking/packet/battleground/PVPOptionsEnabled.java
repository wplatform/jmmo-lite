package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class PVPOptionsEnabled extends ServerPacket {
    public boolean wargameArenas;
    public boolean ratedArenas;
    public boolean wargameBattlegrounds;
    public boolean arenaSkirmish;
    public boolean pugBattlegrounds;
    public boolean ratedBattlegrounds;

    public PVPOptionsEnabled() {
        super(ServerOpcode.PvpOptionsEnabled);
    }

    @Override
    public void write() {
        this.writeBit(ratedBattlegrounds);
        this.writeBit(pugBattlegrounds);
        this.writeBit(wargameBattlegrounds);
        this.writeBit(wargameArenas);
        this.writeBit(ratedArenas);
        this.writeBit(arenaSkirmish);
        this.flushBits();
    }
}
