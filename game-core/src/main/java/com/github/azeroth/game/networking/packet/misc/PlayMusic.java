package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlayMusic extends ServerPacket {
    private final int soundKitID;

    public PlayMusic(int soundKitID) {
        super(ServerOpCode.SMSG_PLAY_MUSIC);
        this.soundKitID = soundKitID;
    }

    @Override
    public void write() {
        this.writeInt32(soundKitID);
    }
}
