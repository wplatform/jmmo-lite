package com.github.azeroth.world.server.traffic;

import com.github.azeroth.crypto.AesGcm;
import lombok.Getter;

import java.nio.ByteBuffer;

public class WorldPacketCrypt {

    @Getter
    private boolean initialized;
    private AesGcm serverEncrypt;
    private AesGcm clientDecrypt;
    private long clientCounter;
    private long serverCounter;

    public void initialize(byte[] key) {
        if (initialized)
            throw new IllegalStateException("PacketCrypt already initialized!");

        this.serverEncrypt = new AesGcm(key);
        this.clientDecrypt = new AesGcm(key);
        this.initialized = true;
    }

    public byte[] encrypt(byte[] data, byte[] tag) {
        byte[] encrypt;
        if (initialized) {
            byte[] iv = ByteBuffer.allocate(12).putLong(serverCounter).putInt(0x52565253).array();
            encrypt = serverEncrypt.encrypt(iv, data, tag);
        } else {
            encrypt = data;
        }
        ++serverCounter;
        return encrypt;
    }

    public byte[] decrypt(byte[] data, byte[] tag) {
        byte[] decrypt;
        if (initialized) {
            byte[] iv = ByteBuffer.allocate(12).putLong(clientCounter).putInt(0x544E4C43).array();
            decrypt = clientDecrypt.decrypt(iv, data, tag);
        } else {
            decrypt = data;
        }
        ++clientCounter;
        return decrypt;
    }


    public boolean PeekDecryptRecv(byte[] data, int length)
    {
        if (initialized)
        {
            WorldPacketCryptIV iv = new WorldPacketCryptIV(clientCounter, 0x544E4C43);
            if (!clientDecrypt.ProcessNoIntegrityCheck(iv.Value, data, length))
                return false;
        }

        return true;
    }


}
