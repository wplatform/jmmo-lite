package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ServerPacket;

public class PetNameInvalid extends ServerPacket {
    public PetrenameData renameData = new petRenameData();
    public PetNameInvalidReason result = PetNameInvalidReason.values()[0];

    public PetNameInvalid() {
        super(ServerOpcode.PetNameInvalid);
    }

    @Override
    public void write() {
        this.writeInt8((byte) result.getValue());
        this.writeGuid(renameData.petGUID);
        this.writeInt32(renameData.petNumber);

        this.writeInt8((byte) renameData.newName.getBytes().length);

        this.writeBit(renameData.hasDeclinedNames);

        if (renameData.hasDeclinedNames) {
            for (var i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                this.writeBits(renameData.declinedNames.name.charAt(i).getBytes().length, 7);
            }

            for (var i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                this.writeString(renameData.declinedNames.name.charAt(i));
            }
        }

        this.writeString(renameData.newName);
    }
}
