package com.github.azeroth.game.networking.packet.petition;


public class PetitionRenameGuildResponse extends ServerPacket {
    public ObjectGuid petitionGuid = ObjectGuid.EMPTY;
    public String newGuildName;

    public PetitionRenameGuildResponse() {
        super(ServerOpcode.PetitionRenameGuildResponse);
    }

    @Override
    public void write() {
        this.writeGuid(petitionGuid);

        this.writeBits(newGuildName.getBytes().length, 7);
        this.flushBits();

        this.writeString(newGuildName);
    }
}
