package com.github.azeroth.game.networking.packet.artifact;


import com.github.azeroth.game.networking.ServerPacket;

class ArtifactXpGain extends ServerPacket {
    public ObjectGuid artifactGUID = ObjectGuid.EMPTY;

    public long amount;

    public ArtifactXpGain() {
        super(ServerOpcode.ArtifactXpGain);
    }

    @Override
    public void write() {
        this.writeGuid(artifactGUID);
        this.writeInt64(amount);
    }
}
