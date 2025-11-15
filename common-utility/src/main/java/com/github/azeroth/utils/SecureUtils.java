package com.github.azeroth.utils;

import com.github.azeroth.crypto.KeysDefine;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.IntStream;

public class SecureUtils {


    private static final PrivateKey ED25519_PRIVATE_KEY;
    private static final PrivateKey RSA_PRIVATE_KEY;

    static {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(KeysDefine.ENTER_ENCRYPTED_MODE_PRIVATE_KEY);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            ED25519_PRIVATE_KEY = keyFactory.generatePrivate(keySpec);

            byte[] decode = Base64.getDecoder().decode(KeysDefine.RSA_PRIVATE_KEY);
            PKCS8EncodedKeySpec rsaKeySpec = new PKCS8EncodedKeySpec(decode);
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
            RSA_PRIVATE_KEY = rsaKeyFactory.generatePrivate(rsaKeySpec);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String bytesToHexString(byte[] src) {
        Objects.requireNonNull(src);
        StringBuilder stringBuilder = new StringBuilder(src.length * 2);
        IntStream.range(0, src.length).map(i -> src[i] & 0xFF).mapToObj(Integer::toHexString).forEach(hv -> {
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        });
        return stringBuilder.toString();
    }

    public static byte[] hexStringToByteArray(String hexString) {
        Objects.requireNonNull(hexString);
        if ((hexString.length() & 1) == 1) {
            throw new IllegalArgumentException("hexString must have even number of character");
        }

        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
        return result;
    }



    public static byte[] sha256(byte[] source, int offset, int length) {
        Objects.requireNonNull(source);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(source, offset, length);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }

    }


    public static byte[] sha256(byte[]... sources) {
        Objects.requireNonNull(sources);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            for (byte[] src : sources) {
                messageDigest.update(src);
            }
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] sha512(byte[]... sources) {
        Objects.requireNonNull(sources);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
            for (byte[] src : sources) {
                messageDigest.update(src);
            }
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha512(byte[] source, int offset, int length) {
        Objects.requireNonNull(source);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(source, offset, length);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }



    public static byte[] hmacSHA512(byte[] key, byte[]... sources) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA512");
            sha512_HMAC.init(secret_key);
            for (byte[] src : sources) {
                sha512_HMAC.update(src);
            }
            return sha512_HMAC.doFinal();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] signWithEd25519(byte[] data, byte[] context) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(ED25519_PRIVATE_KEY);
            signature.update(data);
            signature.update(context);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("Ed25519 Signing failed", e);
        }
    }


    public static byte[] signWithRsa(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(RSA_PRIVATE_KEY);
            signature.update(hash);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("SHA256withRSA Signing failed", e);
        }
    }

}
