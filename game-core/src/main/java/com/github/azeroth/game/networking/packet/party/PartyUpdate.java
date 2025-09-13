package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class PartyUpdate extends ServerPacket {
    public GroupFlags partyFlags = GroupFlags.values()[0];
    public byte partyIndex;
    public GroupType partyType = GroupType.values()[0];

    public ObjectGuid partyGUID = ObjectGuid.EMPTY;
    public ObjectGuid leaderGUID = ObjectGuid.EMPTY;
    public byte leaderFactionGroup;

    public int myIndex;
    public int sequenceNum;

    public ArrayList<PartyPlayerInfo> playerList = new ArrayList<>();

    public PartyLFGInfo lfgInfos = null;
    public PartylootSettings lootSettings = null;
    public PartydifficultySettings difficultySettings = null;

    public PartyUpdate() {
        super(ServerOpcode.PartyUpdate);
    }

    @Override
    public void write() {
        this.writeInt16((short) partyFlags.getValue());
        this.writeInt8(partyIndex);
        this.writeInt8((byte) partyType.getValue());
        this.writeInt32(myIndex);
        this.writeGuid(partyGUID);
        this.writeInt32(sequenceNum);
        this.writeGuid(leaderGUID);
        this.writeInt8(leaderFactionGroup);
        this.writeInt32(playerList.size());
        this.writeBit(lfgInfos != null);
        this.writeBit(lootSettings != null);
        this.writeBit(difficultySettings != null);
        this.flushBits();

        for (var playerInfo : playerList) {
            playerInfo.write(this);
        }

        if (lootSettings != null) {
            lootSettings.getValue().write(this);
        }

        if (difficultySettings != null) {
            difficultySettings.getValue().write(this);
        }

        if (lfgInfos != null) {
            lfgInfos.getValue().write(this);
        }
    }
}
