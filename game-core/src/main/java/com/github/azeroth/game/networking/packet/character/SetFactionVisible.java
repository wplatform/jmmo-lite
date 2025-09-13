package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ServerPacket;

public class SetFactionVisible extends ServerPacket {
    public int factionIndex;

    public SetFactionVisible(boolean visible) {
        super(visible ? ServerOpcode.SetFactionVisible : ServerOpcode.SetFactionNotVisible);
    }

    @Override
    public void write() {
        this.writeInt32(factionIndex);
    }
}
