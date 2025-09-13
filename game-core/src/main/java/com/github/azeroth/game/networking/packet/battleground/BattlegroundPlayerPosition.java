package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

public final class BattlegroundPlayerPosition {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public Vector2 pos;
    public byte iconID;
    public byte arenaSlot;

    public void write(WorldPacket data) {
        data.writeGuid(guid);
        data.writeVector2(pos);
        data.writeInt8(iconID);
        data.writeInt8(arenaSlot);
    }

    public BattlegroundPlayerPosition clone() {
        BattlegroundPlayerPosition varCopy = new BattlegroundPlayerPosition();

        varCopy.guid = this.guid;
        varCopy.pos = this.pos;
        varCopy.iconID = this.iconID;
        varCopy.arenaSlot = this.arenaSlot;

        return varCopy;
    }
}
