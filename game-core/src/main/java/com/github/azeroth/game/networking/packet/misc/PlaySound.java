package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlaySound extends ServerPacket {
    public ObjectGuid sourceObjectGuid = ObjectGuid.EMPTY;
    public int soundKitID;
    public int broadcastTextID;

    public PlaySound(ObjectGuid sourceObjectGuid, int soundKitID, int broadcastTextId) {
        super(ServerOpCode.SMSG_PLAY_SOUND);
        this.sourceObjectGuid = sourceObjectGuid;
        this.soundKitID = soundKitID;
        this.broadcastTextID = broadcastTextId;
    }

    @Override
    public void write() {
        this.writeInt32(soundKitID);
        this.writeGuid(sourceObjectGuid);
        this.writeInt32(broadcastTextID);
    }
}
