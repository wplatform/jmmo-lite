package com.github.azeroth.game.networking.packet.scene;

import com.github.azeroth.game.networking.ServerPacket;

public class PlayScene extends ServerPacket {
    public int sceneID;
    public int playbackFlags;
    public int sceneInstanceID;
    public int sceneScriptPackageID;
    public ObjectGuid transportGUID = ObjectGuid.EMPTY;
    public Position location;
    public boolean encrypted;

    public playScene() {
        super(ServerOpcode.PlayScene);
    }

    @Override
    public void write() {
        this.writeInt32(sceneID);
        this.writeInt32(playbackFlags);
        this.writeInt32(sceneInstanceID);
        this.writeInt32(sceneScriptPackageID);
        this.writeGuid(transportGUID);
        this.writeXYZO(location);
        this.writeBit(encrypted);
        this.flushBits();
    }
}
