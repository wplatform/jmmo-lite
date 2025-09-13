package com.github.azeroth.game.networking.packet.equipment;


import com.github.azeroth.game.entity.player.EquipmentSetInfo;

import java.util.ArrayList;


public class LoadEquipmentSet extends ServerPacket {
    public ArrayList<EquipmentSetInfo.EquipmentsetData> setData = new ArrayList<EquipmentSetInfo.EquipmentSetData>();

    public LoadEquipmentSet() {
        super(ServerOpcode.LoadEquipmentSet);
    }

    @Override
    public void write() {
        this.writeInt32(setData.size());

        for (var equipSet : setData) {
            this.writeInt32(equipSet.getType().getValue());
            this.writeInt64(equipSet.getGuid());
            this.writeInt32(equipSet.getSetId());
            this.writeInt32(equipSet.getIgnoreMask());

            for (var i = 0; i < EquipmentSlot.End; ++i) {
                this.writeGuid(equipSet.getPieces()[i]);
                this.writeInt32(equipSet.getAppearances()[i]);
            }

            for (var id : equipSet.getEnchants()) {
                this.writeInt32(id);
            }

            this.writeInt32(equipSet.getSecondaryShoulderApparanceId());
            this.writeInt32(equipSet.getSecondaryShoulderSlot());
            this.writeInt32(equipSet.getSecondaryWeaponAppearanceId());
            this.writeInt32(equipSet.getSecondaryWeaponSlot());

            this.writeBit(equipSet.getAssignedSpecIndex() != -1);
            this.writeBits(equipSet.getSetName().getBytes().length, 8);
            this.writeBits(equipSet.getSetIcon().getBytes().length, 9);

            if (equipSet.getAssignedSpecIndex() != -1) {
                this.writeInt32(equipSet.getAssignedSpecIndex());
            }

            this.writeString(equipSet.getSetName());
            this.writeString(equipSet.getSetIcon());
        }
    }
}
