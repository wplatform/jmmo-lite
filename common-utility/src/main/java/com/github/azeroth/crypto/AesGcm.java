package com.github.azeroth.crypto;

import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

public class AesGcm {
    private static final int GCM_TAG_LENGTH = 12; // 12 bytes (96 bits) is recommended
    private static final int GCM_IV_LENGTH = 12;  // 12 bytes (96 bits) is recommended


    private final byte[] key;

    public AesGcm(byte[] key) {
        if (key == null || (key.length != 16 && key.length != 24 && key.length != 32)) {
            throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes");
        }
        this.key = key;
    }

    /**
     * Encrypts data using AES-GCM
     * @param iv initialization vector (must be 12 bytes)
     * @param data plaintext data to encrypt
     * @param tag output buffer for authentication tag (must be at least 12 bytes)
     * @return encrypted data
     * @throws RuntimeException if encryption fails
     */
    public byte[] encrypt(byte[] iv, byte[] data, byte[] tag) {
        try {
            validateParameters(iv, tag);

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // Encrypt the data
            byte[] encrypted = cipher.doFinal(data);

            // Extract the authentication tag (last 12 bytes of the encrypted data)
            System.arraycopy(encrypted, encrypted.length - GCM_TAG_LENGTH, tag, 0, GCM_TAG_LENGTH);

            // Return only the ciphertext (without the tag)
            byte[] ciphertext = new byte[encrypted.length - GCM_TAG_LENGTH];
            System.arraycopy(encrypted, 0, ciphertext, 0, ciphertext.length);

            return ciphertext;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts data using AES-GCM and verifies the authentication tag
     * @param iv initialization vector (must be 12 bytes)
     * @param data encrypted data to decrypt
     * @param tag authentication tag to verify
     * @return decrypted plaintext data
     * @throws RuntimeException if decryption or tag verification fails
     */
    public byte[] decrypt(byte[] iv, byte[] data, byte[] tag) {
        try {
            validateParameters(iv, tag);

            // Combine encrypted data and tag for decryption
            byte[] encryptedDataWithTag = new byte[data.length + GCM_TAG_LENGTH];
            System.arraycopy(data, 0, encryptedDataWithTag, 0, data.length);
            System.arraycopy(tag, 0, encryptedDataWithTag, data.length, GCM_TAG_LENGTH);

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // Decrypt and verify the data
            return cipher.doFinal(encryptedDataWithTag);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed or tag verification failed", e);
        }
    }


    public int decryptOpcode(byte[] iv, int opCode) {
        try {
            // Combine encrypted data and tag for decryption
            byte[] data = ByteBuffer.allocate(4).putInt(opCode).array();


            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // Decrypt and verify the data
            byte[] decrypted = cipher.doFinal(data);
            return ByteBuffer.wrap(decrypted).getInt();
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed or tag verification failed", e);
        }
    }


    private void validateParameters(byte[] iv, byte[] tag) {
        if (iv == null || iv.length != GCM_IV_LENGTH) {
            throw new IllegalArgumentException("IV must be 12 bytes");
        }
        if (tag == null || tag.length < GCM_TAG_LENGTH) {
            throw new IllegalArgumentException("Tag buffer must be at least 12 bytes");
        }
    }

}
