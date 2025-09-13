package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ReorderCharacters extends ClientPacket {
    public ReorderInfo[] entries = new ReorderInfo[200];

    public ReorderCharacters(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var count = this.<Integer>readBit(9);

        for (var i = 0; i < count && i < WorldConfig.getIntValue(WorldCfg.CharactersPerRealm); ++i) {
            ReorderInfo reorderInfo = new ReorderInfo();
            reorderInfo.playerGUID = this.readPackedGuid();
            reorderInfo.newPosition = this.readUInt8();
            Entries[i] = reorderInfo;
        }
    }

    public final static class ReorderInfo {
        public ObjectGuid playerGUID = ObjectGuid.EMPTY;
        public byte newPosition;

        public ReorderInfo clone() {
            ReorderInfo varCopy = new ReorderInfo();

            varCopy.playerGUID = this.playerGUID;
            varCopy.newPosition = this.newPosition;

            return varCopy;
        }
    }
}
