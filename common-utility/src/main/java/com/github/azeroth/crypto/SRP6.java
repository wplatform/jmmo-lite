package com.github.azeroth.crypto;

import com.github.azeroth.utils.RandomUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

public abstract class SRP6 {
    public static final int SALT_LENGTH = 32;

    protected final byte[] s; // salt value
    protected final BigInteger I; // username hash
    protected final BigInteger b; // server random number
    protected final BigInteger v; // verifier
    protected final BigInteger B; // server public key
    protected boolean used = false; // instance can only be used once


    public SRP6(BigInteger i, byte[] salt, BigInteger verifier, BigInteger N, BigInteger g, BigInteger k) {
        this.I = i;
        this.s = salt;
        this.v = verifier;
        this.b = calculatePrivateB(N);
        // B = k*v + g^b mod N
        this.B = k.multiply(v).add(g.modPow(b, N)).mod(N);
    }

    // 用于注册的构造函数
    public SRP6() {
        this.s = RandomUtil.randomBytes(SALT_LENGTH);
        this.I = BigInteger.ZERO;
        this.b = BigInteger.ZERO;
        this.v = BigInteger.ZERO;
        this.B = BigInteger.ZERO;
    }

    public abstract BigInteger getN();
    public abstract BigInteger getg();

    // 验证客户端证据
    public Optional<BigInteger> verifyClientEvidence(BigInteger A, BigInteger clientM1) {
        if (used) {
            return Optional.empty();
        }
        used = true;
        return doVerifyClientEvidence(A, clientM1);
    }

    // 计算服务器证据
    public abstract BigInteger calculateServerEvidence(BigInteger A, BigInteger clientM1, BigInteger K) throws Exception;

    // 检查凭证
    public boolean checkCredentials(String username, String password) {
        BigInteger x = calculateX(username, password, s);
        BigInteger vCalculated = getg().modPow(x, getN());
        return v.equals(vCalculated);
    }

    // 计算私钥b
    protected static BigInteger calculatePrivateB(BigInteger N) {
        SecureRandom random = new SecureRandom();
        int numBytes = (N.bitLength() + 7) / 8;
        byte[] bytes = new byte[numBytes];
        BigInteger b;
        do {
            random.nextBytes(bytes);
            b = new BigInteger(1, bytes);
        } while (b.equals(BigInteger.ZERO));
        return b;
    }

    // 计算公钥B
    protected BigInteger calculatePublicB(BigInteger N, BigInteger g, BigInteger k) {
        return k.multiply(v).add(g.modPow(b, N)).mod(N);
    }

    // 计算x值（子类实现）
    protected abstract BigInteger calculateX(String username, String password, byte[] salt ) ;

    // 计算验证器v
    protected BigInteger calculateVerifier(String username, String password, byte[] salt)  {
        BigInteger x = calculateX(username, password, salt);
        return getg().modPow(x, getN());
    }

    // 验证客户端证据（子类实现）
    protected abstract Optional<BigInteger> doVerifyClientEvidence(BigInteger A, BigInteger clientM1);

    // 辅助类：表示键值对
    // 获取盐值
    public byte[] gets() {
        return Arrays.copyOf(s, s.length);
    }

    // 获取公钥B
    public BigInteger getB() {
        return B;
    }

    // SHA1哈希辅助方法
    protected static BigInteger sha1Hash(byte[]... data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (byte[] bytes : data) {
                md.update(bytes);
            }
            return new BigInteger(1, md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize SHA-1 digest", e);
        }
    }

    // SHA256哈希辅助方法
    protected static BigInteger sha256Hash(byte[]... data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (byte[] bytes : data) {
                md.update(bytes);
            }
            return new BigInteger(1, md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize SHA-256 digest", e);
        }

    }

    // 将BigInteger转换为固定长度的字节数组
    protected static byte[] bigIntegerToFixedBytes(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }
        byte[] result = new byte[length];
        if (bytes.length > length) {
            // 如果字节数超过长度，取最后length个字节
            System.arraycopy(bytes, bytes.length - length, result, 0, length);
        } else {
            // 如果字节数不足，前面补0
            System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
        }
        return result;
    }

    // 获取用户名哈希
    protected static BigInteger getUsernameHash(String username) {
        return sha1Hash(username.toUpperCase().getBytes(StandardCharsets.UTF_8));
    }

    public record RegistrationData(byte[] salt, byte[] verifier) {}
}
