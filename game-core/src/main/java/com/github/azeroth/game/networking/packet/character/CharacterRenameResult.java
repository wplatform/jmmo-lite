package com.github.azeroth.game.networking.packet.character;


public class CharacterRenameResult extends ServerPacket {
    public String name;
    public ResponseCodes result = ResponseCodes.values()[0];
    public ObjectGuid UUID = null;

    public CharacterRenameResult() {
        super(ServerOpcode.CharacterRenameResult);
    }

    @Override
    public void write() {
        this.writeInt8((byte) result.getValue());
        this.writeBit(system.guid.HasValue);
        this.writeBits(name.getBytes().length, 6);
        this.flushBits();

        if (system.guid.HasValue) {
            this.writeGuid(system.guid.value);
        }

        this.writeString(name);
    }
}
