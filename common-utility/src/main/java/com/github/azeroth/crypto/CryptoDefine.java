package com.github.azeroth.crypto;

public interface CryptoDefine {
    // Session key length. it is sha1 hash length * 2
    byte SESSION_KEY_LENGTH = 40;
    byte SERVER_CHALLENGE_LENGTH = 32;
    byte ENCRYPT_KEY_LENGTH = 32;
}
