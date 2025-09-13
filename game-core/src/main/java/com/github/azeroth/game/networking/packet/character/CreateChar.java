package com.github.azeroth.game.networking.packet.character;


public class CreateChar extends ServerPacket {
    public Responsecodes code = ResponseCodes.values()[0];
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public CreateChar() {
        super(ServerOpcode.CreateChar);
    }

    @Override
    public void write() {
        this.writeInt8((byte) code.getValue());
        this.writeGuid(guid);
    }
}
