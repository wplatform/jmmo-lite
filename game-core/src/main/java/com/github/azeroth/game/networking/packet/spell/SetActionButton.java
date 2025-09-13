package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetActionButton extends ClientPacket {
    public long action; // two packed values (action and type)
    public byte index;

    public SetActionButton(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        action = this.readUInt64();
        index = this.readUInt8();
    }

    public final int getButtonAction() {
        return (int) (action & 0x00FFFFFFFFFFFFFF);
    }

    public final int getButtonType() {
        return (int) ((action & 0xFF00000000000000) >>> 56);
    }
}
