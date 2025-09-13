package com.github.azeroth.game.networking.packet.reputation;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class SetFactionStanding extends ServerPacket {
    public float bonusFromAchievementSystem;
    public ArrayList<factionStandingData> faction = new ArrayList<>();
    public boolean showVisual;

    public SetFactionStanding() {
        super(ServerOpcode.SetFactionStanding);
    }

    @Override
    public void write() {
        this.writeFloat(bonusFromAchievementSystem);

        this.writeInt32(faction.size());

        for (var factionStanding : faction) {
            factionStanding.write(this);
        }

        this.writeBit(showVisual);
        this.flushBits();
    }
}
