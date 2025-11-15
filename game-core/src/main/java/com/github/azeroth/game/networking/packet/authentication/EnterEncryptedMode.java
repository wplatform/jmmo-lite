package com.github.azeroth.game.networking.packet.authentication;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.utils.SecureUtils;

public class EnterEncryptedMode extends ServerPacket {


    private final byte[] ENABLE_ENCRYPTION_SEED = { 0x66, (byte) 0xBE, 0x29, 0x79, (byte) 0xEF, (byte) 0xF2, (byte) 0xD5, (byte) 0xB5,
            0x61, 0x53, (byte) 0xF6, 0x5F, 0x45, (byte) 0xAE, (byte) 0x81, (byte) 0xCB,
            0x32, (byte) 0xEC, (byte) 0x94, (byte) 0xEC, 0x75, (byte) 0xB3, 0x5F, 0x44, 0x6A, 0x63, 0x43, 0x67, 0x17, 0x20, 0x44, 0x34 };
    private final byte[] ENABLE_ENCRYPTION_CONTEXT = {(byte) 0xA7, 0x1F, (byte) 0xB6, (byte) 0x9B, (byte) 0xC9, 0x7C, (byte) 0xDD, (byte) 0x96, (byte) 0xE9,
            (byte) 0xBB, (byte) 0xB8, 0x21, 0x39, (byte) 0x8D, 0x5A, (byte) 0xD4};



    private final byte[] encryptionKey;
    private final boolean enabled;

    public EnterEncryptedMode(byte[] encryptionKey, boolean enabled) {
        super(ServerOpCode.SMSG_ENTER_ENCRYPTED_MODE);
        this.encryptionKey = encryptionKey;
        this.enabled = enabled;
    }

    @Override
    public void write() {
        byte[] toSign = SecureUtils.hmacSHA512(encryptionKey,
                new byte[]{(byte) (enabled ? 1 : 0)},
                ENABLE_ENCRYPTION_SEED);
        byte[] signInEd25519 = SecureUtils.signWithEd25519(toSign, ENABLE_ENCRYPTION_CONTEXT);
        writeBytes(signInEd25519);
        writeBit(enabled);
        flushBits();
    }
}
