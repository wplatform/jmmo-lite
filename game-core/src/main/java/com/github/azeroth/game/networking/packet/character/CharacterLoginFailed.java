package com.github.azeroth.game.networking.packet.character;


public class CharacterLoginFailed extends ServerPacket {
    private final LoginFailureReason code;

    public CharacterLoginFailed(LoginFailureReason code) {
        super(ServerOpcode.CharacterLoginFailed);
        code = code;
    }

    @Override
    public void write() {
        this.writeInt8((byte) code.getValue());
    }
}
