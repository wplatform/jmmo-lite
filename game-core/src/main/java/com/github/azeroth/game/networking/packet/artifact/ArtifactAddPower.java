package com.github.azeroth.game.networking.packet.artifact;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ArtifactAddPower extends ClientPacket {
    public ObjectGuid artifactGUID = ObjectGuid.EMPTY;
    public ObjectGuid forgeGUID = ObjectGuid.EMPTY;
    public Array<ArtifactPowerChoice> powerChoices = new Array<ArtifactPowerChoice>(1);

    public ArtifactAddPower(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        artifactGUID = this.readPackedGuid();
        forgeGUID = this.readPackedGuid();

        var powerCount = this.readUInt32();

        for (var i = 0; i < powerCount; ++i) {
            ArtifactPowerChoice artifactPowerChoice = new ArtifactPowerChoice();
            artifactPowerChoice.artifactPowerID = this.readUInt32();
            artifactPowerChoice.rank = this.readUInt8();
            powerChoices.set(i, artifactPowerChoice);
        }
    }

    public final static class ArtifactPowerChoice {
        public int artifactPowerID;
        public byte rank;

        public ArtifactPowerChoice clone() {
            ArtifactPowerChoice varCopy = new ArtifactPowerChoice();

            varCopy.artifactPowerID = this.artifactPowerID;
            varCopy.rank = this.rank;

            return varCopy;
        }
    }
}
