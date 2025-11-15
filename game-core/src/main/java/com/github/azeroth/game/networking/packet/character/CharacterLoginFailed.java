package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class CharacterLoginFailed extends ServerPacket {



    private final LoginFailureReason code;

    public CharacterLoginFailed(LoginFailureReason code) {
        super(ServerOpCode.SMSG_CHARACTER_LOGIN_FAILED);
        this.code = code;
    }

    @Override
    public void write() {
        this.writeInt8(code.code);
    }
}
