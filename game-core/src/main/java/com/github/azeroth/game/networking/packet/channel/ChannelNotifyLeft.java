package com.github.azeroth.game.networking.packet.channel;


public class ChannelNotifyLeft extends ServerPacket {
    public String channel;
    public int chatChannelID;
    public boolean suspended;

    public ChannelNotifyLeft() {
        super(ServerOpcode.ChannelNotifyLeft);
    }

    @Override
    public void write() {
        this.writeBits(channel.getBytes().length, 7);
        this.writeBit(suspended);
        this.writeInt32(chatChannelID);
        this.writeString(channel);
    }
}
