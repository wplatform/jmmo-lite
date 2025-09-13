package com.github.azeroth.game.networking.packet.channel;


public class ChannelNotify extends ServerPacket {
    public String sender = "";
    public ObjectGuid senderGuid = ObjectGuid.EMPTY;
    public ObjectGuid senderAccountID = ObjectGuid.EMPTY;
    public ChatNotify type = ChatNotify.values()[0];
    public ChannelMemberFlags oldFlags = ChannelMemberFlags.values()[0];
    public ChannelMemberFlags newFlags = ChannelMemberFlags.values()[0];
    public String channel;
    public int senderVirtualRealm;
    public ObjectGuid targetGuid = ObjectGuid.EMPTY;
    public int targetVirtualRealm;
    public int chatChannelID;

    public ChannelNotify() {
        super(ServerOpcode.ChannelNotify);
    }

    @Override
    public void write() {
        this.writeBits(type, 6);
        this.writeBits(channel.getBytes().length, 7);
        this.writeBits(sender.getBytes().length, 6);

        this.writeGuid(senderGuid);
        this.writeGuid(senderAccountID);
        this.writeInt32(senderVirtualRealm);
        this.writeGuid(targetGuid);
        this.writeInt32(targetVirtualRealm);
        this.writeInt32(chatChannelID);

        if (type == ChatNotify.ModeChangeNotice) {
            this.writeInt8((byte) oldFlags.getValue());
            this.writeInt8((byte) newFlags.getValue());
        }

        this.writeString(channel);
        this.writeString(sender);
    }
}
