package com.github.azeroth.game.networking.packet.battlenet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ChangeRealmTicket extends ClientPacket {
    public int token;
    public Array<Byte> secret = new Array<Byte>(32);

    public ChangeRealmTicket(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        token = this.readUInt32();

        for (var i = 0; i < secret.GetLimit(); ++i) {
            secret.set(i, this.readUInt8());
        }
    }
}
