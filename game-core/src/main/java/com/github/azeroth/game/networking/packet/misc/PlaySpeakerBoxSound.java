package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlaySpeakerBoxSound extends ServerPacket {
    public ObjectGuid sourceObjectGUID;
    public int soundKitID;

    public PlaySpeakerBoxSound(ObjectGuid sourceObjectGuid, int soundKitID) {
        super(ServerOpCode.SMSG_PLAY_SPEAKERBOT_SOUND);
        this.sourceObjectGUID = sourceObjectGuid;
        this.soundKitID = soundKitID;
    }

    @Override
    public void write() {
        this.writeGuid(sourceObjectGUID);
        this.writeInt32(soundKitID);
    }
}
