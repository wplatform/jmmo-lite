package com.github.azeroth.game.networking.packet.social;

import com.github.azeroth.game.entity.player.SocialFlag;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SendContactList extends ClientPacket {
    public SocialFlag flags = SocialFlag.values()[0];

    public SendContactList(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        flags = SocialFlag.forValue(this.readUInt32());
    }
}

//Structs

