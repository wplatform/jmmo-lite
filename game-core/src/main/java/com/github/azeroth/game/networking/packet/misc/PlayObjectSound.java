package com.github.azeroth.game.networking.packet.misc;

import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlayObjectSound extends ServerPacket {
    public ObjectGuid targetObjectGUID = ObjectGuid.EMPTY;
    public ObjectGuid sourceObjectGUID = ObjectGuid.EMPTY;
    public int soundKitID;
    public Vector3 position;
    public int broadcastTextID;

    public PlayObjectSound() {
        super(ServerOpCode.SMSG_PLAY_OBJECT_SOUND);
    }

    @Override
    public void write() {
        this.writeInt32(soundKitID);
        this.writeGuid(sourceObjectGUID);
        this.writeGuid(targetObjectGUID);
        this.writeVector3(position);
        this.writeInt32(broadcastTextID);
    }
}
