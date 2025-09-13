package com.github.azeroth.game.networking.packet.talent;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class UpdateTalentData extends ServerPacket {
    public TalentinfoUpdate info = new talentInfoUpdate();

    public UpdateTalentData() {
        super(ServerOpcode.UpdateTalentData);
    }

    @Override
    public void write() {
        this.writeInt8(info.activeGroup);
        this.writeInt32(info.primarySpecialization);
        this.writeInt32(info.talentGroups.size());

        for (var talentGroupInfo : info.talentGroups) {
            this.writeInt32(talentGroupInfo.specID);
            this.writeInt32(talentGroupInfo.talentIDs.size());
            this.writeInt32(talentGroupInfo.pvPTalents.size());

            for (var talentID : talentGroupInfo.talentIDs) {
                this.writeInt16(talentID);
            }

            for (var talent : talentGroupInfo.pvPTalents) {
                talent.write(this);
            }
        }
    }

    public static class TalentGroupInfo {
        public int specID;
        public ArrayList<SHORT> talentIDs = new ArrayList<>();
        public ArrayList<PvPTalent> pvPTalents = new ArrayList<>();
    }

    public static class TalentInfoUpdate {
        public byte activeGroup;
        public int primarySpecialization;
        public ArrayList<TalentGroupInfo> talentGroups = new ArrayList<>();
    }
}

//Structs

