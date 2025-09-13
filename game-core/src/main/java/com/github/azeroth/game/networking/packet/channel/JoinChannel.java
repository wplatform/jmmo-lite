package com.github.azeroth.game.networking.packet.channel;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class JoinChannel extends ClientPacket {
    public String password;
    public String channelName;
    public boolean createVoiceSession;
    public int chatChannelId;
    public boolean internal;

    public joinChannel(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        chatChannelId = this.readInt32();
        createVoiceSession = this.readBit();
        internal = this.readBit();
        var channelLength = this.<Integer>readBit(7);
        var passwordLength = this.<Integer>readBit(7);
        channelName = this.readString(channelLength);
        password = this.readString(passwordLength);
    }
}
