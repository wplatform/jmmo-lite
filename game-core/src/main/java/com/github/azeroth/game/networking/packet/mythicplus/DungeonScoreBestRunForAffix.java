package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

public class DungeonScoreBestRunForAffix {
    public int keystoneAffixID;
    public MythicPlusrun run = new mythicPlusRun();
    public float score;

    public final void write(WorldPacket data) {
        data.writeInt32(keystoneAffixID);
        data.writeFloat(score);
        run.write(data);
    }
}
