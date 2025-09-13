package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SocketGems extends ClientPacket {
    public ObjectGuid itemGuid = ObjectGuid.EMPTY;
    public ObjectGuid[] gemItem = new ObjectGuid[ItemConst.MaxGemSockets];

    public SocketGems(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        itemGuid = this.readPackedGuid();

        for (var i = 0; i < ItemConst.MaxGemSockets; ++i) {
            GemItem[i] = this.readPackedGuid();
        }
    }
}
