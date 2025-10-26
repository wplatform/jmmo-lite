package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class AreaSpiritHealerQuery extends ClientPacket {
    public ObjectGuid healerGuid;

    public AreaSpiritHealerQuery(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        healerGuid = this.readPackedGuid();
    }
}
