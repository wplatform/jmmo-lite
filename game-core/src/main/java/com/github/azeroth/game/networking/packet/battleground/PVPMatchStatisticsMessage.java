package com.github.azeroth.game.networking.packet.battleground;


public class PVPMatchStatisticsMessage extends ServerPacket {
    public PVPMatchStatistics data;

    public PVPMatchStatisticsMessage() {
        super(ServerOpcode.PvpMatchStatistics);
    }

    @Override
    public void write() {
        data.write(this);
    }
}
