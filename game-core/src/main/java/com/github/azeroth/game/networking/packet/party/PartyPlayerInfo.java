package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

final class PartyPlayerInfo {
    public ObjectGuid GUID = ObjectGuid.EMPTY;
    public String name;
    public String voiceStateID; // same as bgs.protocol.club.v1.MemberVoiceState.id
    public byte subgroup;
    public byte class;
    public byte flags;
    public byte rolesAssigned;
    public byte factionGroup;
    public boolean fromSocialQueue;
    public boolean voiceChatSilenced;
    public boolean connected;

    public void write(WorldPacket data) {
        data.writeBits(name.getBytes().length, 6);
        data.writeBits(voiceStateID.getBytes().length + 1, 6);
        data.writeBit(connected);
        data.writeBit(voiceChatSilenced);
        data.writeBit(fromSocialQueue);
        data.writeGuid(GUID);
        data.writeInt8(subgroup);
        data.writeInt8(flags);
        data.writeInt8(rolesAssigned);
        data.writeInt8( class);
        data.writeInt8(factionGroup);
        data.writeString(name);

        if (!voiceStateID.isEmpty()) {
            data.writeString(voiceStateID);
        }
    }

    public PartyPlayerInfo clone() {
        PartyPlayerInfo varCopy = new PartyPlayerInfo();

        varCopy.GUID = this.GUID;
        varCopy.name = this.name;
        varCopy.voiceStateID = this.voiceStateID;
        varCopy.class = this. class ;
        varCopy.subgroup = this.subgroup;
        varCopy.flags = this.flags;
        varCopy.rolesAssigned = this.rolesAssigned;
        varCopy.factionGroup = this.factionGroup;
        varCopy.fromSocialQueue = this.fromSocialQueue;
        varCopy.voiceChatSilenced = this.voiceChatSilenced;
        varCopy.connected = this.connected;

        return varCopy;
    }
}
