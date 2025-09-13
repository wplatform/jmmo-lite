package com.github.azeroth.game.networking.packet.channel;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ChannelPassword extends ClientPacket {
    public String channelName;
    public String password;

    public ChannelPassword(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var channelNameLength = this.<Integer>readBit(7);
        var passwordLength = this.<Integer>readBit(7);
        channelName = this.readString(channelNameLength);
        password = this.readString(passwordLength);
    }
}
