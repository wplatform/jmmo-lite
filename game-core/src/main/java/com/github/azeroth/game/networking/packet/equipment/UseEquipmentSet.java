package com.github.azeroth.game.networking.packet.equipment;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class UseEquipmentSet extends ClientPacket {
    public invUpdate inv = new invUpdate();
    public EquipmentSetItem[] items = new EquipmentSetItem[EquipmentSlot.End];
    public long GUID; //Set Identifier

    public UseEquipmentSet(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);

        for (byte i = 0; i < EquipmentSlot.End; ++i) {
            Items[i].item = this.readPackedGuid();
            Items[i].containerSlot = this.readUInt8();
            Items[i].slot = this.readUInt8();
        }

        GUID = this.readUInt64();
    }

    public final static class EquipmentSetItem {
        public ObjectGuid item = ObjectGuid.EMPTY;
        public byte containerSlot;
        public byte slot;

        public EquipmentSetItem clone() {
            EquipmentSetItem varCopy = new EquipmentSetItem();

            varCopy.item = this.item;
            varCopy.containerSlot = this.containerSlot;
            varCopy.slot = this.slot;

            return varCopy;
        }
    }
}
