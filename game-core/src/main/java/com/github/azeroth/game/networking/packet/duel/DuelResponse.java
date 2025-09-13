package com.github.azeroth.game.networking.packet.duel;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class DuelResponse extends ClientPacket {
    public ObjectGuid arbiterGUID = ObjectGuid.EMPTY;
    public boolean accepted;
    public boolean forfeited;

    public DuelResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        arbiterGUID = this.readPackedGuid();
        accepted = this.readBit();
        forfeited = this.readBit();
    }
}
