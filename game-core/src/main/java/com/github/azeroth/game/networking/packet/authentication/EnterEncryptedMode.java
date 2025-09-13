package com.github.azeroth.game.networking.packet.authentication;


import Framework.Cryptography.*;
import Framework.Cryptography.Ed25519.*;
import com.github.azeroth.game.networking.ServerPacket;

public class EnterEncryptedMode extends ServerPacket {
    private static final byte[] expandedPrivateKey;

    private static final byte[] ENABLEENCRYPTIONSEED = {(byte) 0x90, (byte) 0x9C, (byte) 0xD0, 0x50, 0x5A, 0x2C, 0x14, (byte) 0xDD, 0x5C, 0x2C, (byte) 0xC0, 0x64, 0x14, (byte) 0xF3, (byte) 0xFE, (byte) 0xC9};

    private static final byte[] ENABLEENCRYPTIONCONTEXT = {(byte) 0xA7, 0x1F, (byte) 0xB6, (byte) 0x9B, (byte) 0xC9, 0x7C, (byte) 0xDD, (byte) 0x96, (byte) 0xE9, (byte) 0xBB, (byte) 0xB8, 0x21, 0x39, (byte) 0x8D, 0x5A, (byte) 0xD4};

    private static final byte[] ENTERENCRYPTEDMODEPRIVATEKEY = {0x08, (byte) 0xBD, (byte) 0xC7, (byte) 0xA3, (byte) 0xCC, (byte) 0xC3, 0x4F, 0x3F, 0x6A, 0x0B, (byte) 0xFF, (byte) 0xCF, 0x31, (byte) 0xC1, (byte) 0xB6, (byte) 0x97, 0x69, 0x1E, 0x72, (byte) 0x9A, 0x0A, (byte) 0xAB, 0x2C, 0x77, (byte) 0xC3, 0x6F, (byte) 0x8A, (byte) 0xE7, 0x5A, (byte) 0x9A, (byte) 0xA7, (byte) 0xC9};

    static {
        expandedPrivateKey = Ed25519.ExpandedPrivateKeyFromSeed(ENTERENCRYPTEDMODEPRIVATEKEY);
    }

    private final byte[] encryptionKey;
    private final boolean enabled;

    public EnterEncryptedMode(byte[] encryptionKey, boolean enabled) {
        super(ServerOpcode.EnterEncryptedMode);
        encryptionKey = encryptionKey;
        enabled = enabled;
    }

    @Override
    public void write() {
        HmacSha256 toSign = new HmacSha256(encryptionKey);
        toSign.process(BitConverter.GetBytes(enabled), 1);
        toSign.finish(ENABLEENCRYPTIONSEED, 16);

        this.writeBytes(Ed25519.Sign(toSign.digest, expandedPrivateKey, 0, ENABLEENCRYPTIONCONTEXT));
        this.writeBit(enabled);
        this.flushBits();
    }
}
