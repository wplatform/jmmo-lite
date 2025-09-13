package com.github.azeroth.game.networking.packet.artifact;

import com.github.azeroth.game.networking.ServerPacket;

public class ArtifactRespecPrompt extends ServerPacket {
    public ObjectGuid artifactGUID = ObjectGuid.EMPTY;
    public ObjectGuid npcGUID = ObjectGuid.EMPTY;

    public ArtifactRespecPrompt() {
        super(ServerOpcode.ArtifactRespecPrompt);
    }

    @Override
    public void write() {
        this.writeGuid(artifactGUID);
        this.writeGuid(npcGUID);
    }
}
