package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.WorldPacket;

public class CharacterRenameRequest extends ClientPacket {
    public CharacterrenameInfo renameInfo;

    public CharacterRenameRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        renameInfo = new CharacterRenameInfo();
        renameInfo.guid = this.readPackedGuid();
        renameInfo.newName = this.readString(this.<Integer>readBit(6));
    }
}
