package com.github.azeroth.game.networking.packet.channel;


public class ChannelNotifyJoined extends ServerPacket {
    public String channelWelcomeMsg = "";
    public int chatChannelID;
    public long instanceID;
    public channelFlags channelFlags = Framework.Constants.channelFlags.values()[0];
    public String channel = "";
    public ObjectGuid channelGUID = ObjectGuid.EMPTY;

    public ChannelNotifyJoined() {
        super(ServerOpcode.ChannelNotifyJoined);
    }

    @Override
    public void write() {
        this.writeBits(channel.getBytes().length, 7);
        this.writeBits(channelWelcomeMsg.getBytes().length, 11);
        this.writeInt32((int) channelFlags.getValue());
        this.writeInt32(chatChannelID);
        this.writeInt64(instanceID);
        this.writeGuid(channelGUID);
        this.writeString(channel);
        this.writeString(channelWelcomeMsg);
    }
}
