package com.github.azeroth.game.networking.packet.achievement;


public class RespondInspectAchievements extends ServerPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public allAchievements data = new allAchievements();

    public RespondInspectAchievements() {
        super(ServerOpcode.RespondInspectAchievements);
    }

    @Override
    public void write() {
        this.writeGuid(player);
        data.write(this);
    }
}
