package com.github.azeroth.game.networking.packet.character;


public class DeleteChar extends ServerPacket {
    public Responsecodes code = ResponseCodes.values()[0];

    public DeleteChar() {
        super(ServerOpcode.DeleteChar);
    }

    @Override
    public void write() {
        this.writeInt8((byte) code.getValue());
    }
}
