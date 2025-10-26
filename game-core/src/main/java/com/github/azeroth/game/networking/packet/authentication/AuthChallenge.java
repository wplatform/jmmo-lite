package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class AuthChallenge extends ServerPacket {
    public byte[] challenge;
    public byte[] dosChallenge; // Encryption seeds
    public byte dosZeroBits;

    public AuthChallenge() {
        super(ServerOpCode.SMSG_AUTH_CHALLENGE);
    }

    @Override
    public void write() {
        this.writeBytes(dosChallenge);
        this.writeBytes(challenge);
        this.writeInt8(dosZeroBits);
    }
}
