package com.github.azeroth.game.networking.packet.channel;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class LeaveChannel extends ClientPacket {
    public int zoneChannelID;
    public String channelName;

    public leaveChannel(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        zoneChannelID = this.readInt32();
        channelName = this.readString(this.<Integer>readBit(7));
    }
}
