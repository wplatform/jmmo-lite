package com.github.azeroth.game.networking.packet.channel;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ChannelPlayerCommand extends ClientPacket {
    public String channelName;
    public String name;

    public ChannelPlayerCommand(WorldPacket packet) {
        super(packet);
        switch (GetOpcode()) {
            case ChatChannelBan:
            case ChatChannelInvite:
            case ChatChannelKick:
            case ChatChannelModerator:
            case ChatChannelSetOwner:
            case ChatChannelSilenceAll:
            case ChatChannelUnban:
            case ChatChannelUnmoderator:
            case ChatChannelUnsilenceAll:
                break;
            default:
                //ABORT();
                break;
        }
    }

    @Override
    public void read() {
        var channelNameLength = this.<Integer>readBit(7);
        var nameLength = this.<Integer>readBit(9);
        channelName = this.readString(channelNameLength);
        name = this.readString(nameLength);
    }
}
