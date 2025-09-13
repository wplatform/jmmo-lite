package com.github.azeroth.game.networking.packet.channel;

import com.github.azeroth.game.networking.ServerPacket;

public class UserlistAdd extends ServerPacket {
    public ObjectGuid addedUserGUID = ObjectGuid.EMPTY;
    public channelFlags channelFlags = Framework.Constants.channelFlags.values()[0];
    public ChannelMemberFlags userFlags = ChannelMemberFlags.values()[0];
    public int channelID;
    public String channelName;

    public UserlistAdd() {
        super(ServerOpcode.UserlistAdd);
    }

    @Override
    public void write() {
        this.writeGuid(addedUserGUID);
        this.writeInt8((byte) userFlags.getValue());
        this.writeInt32((int) channelFlags.getValue());
        this.writeInt32(channelID);

        this.writeBits(channelName.getBytes().length, 7);
        this.flushBits();
        this.writeString(channelName);
    }
}
