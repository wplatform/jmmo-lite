package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PartyInviteClient extends ClientPacket {

    public byte partyIndex;

    public int proposedRoles;
    public String targetName;
    public String targetRealm;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public PartyInviteClient(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();

        var targetNameLen = this.<Integer>readBit(9);
        var targetRealmLen = this.<Integer>readBit(9);

        proposedRoles = this.readUInt32();
        targetGUID = this.readPackedGuid();

        targetName = this.readString(targetNameLen);
        targetRealm = this.readString(targetRealmLen);
    }
}
