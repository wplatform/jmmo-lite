package com.github.azeroth.game.networking.packet.pet;


import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PetRename extends ClientPacket {
    public PetrenameData renameData = new petRenameData();

    public PetRename(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        renameData.petGUID = this.readPackedGuid();
        renameData.petNumber = this.readInt32();

        var nameLen = this.<Integer>readBit(8);

        renameData.hasDeclinedNames = this.readBit();

        if (renameData.hasDeclinedNames) {
            renameData.declinedNames = new declinedName();
            var count = new int[SharedConst.MaxDeclinedNameCases];

            for (var i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                count[i] = this.<Integer>readBit(7);
            }

            for (var i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                renameData.declinedNames.name.charAt(i) = this.readString(count[i]);
            }
        }

        renameData.newName = this.readString(nameLen);
    }
}
