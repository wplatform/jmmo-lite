package com.github.azeroth.crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class GruntSRP6 extends SRP6 {
    public static final int EPHEMERAL_KEY_LENGTH = 32;
    public static final int SESSION_KEY_LENGTH = 40; // SHA1.DIGEST_LENGTH * 2

    // N - 模数，算法参数
    public static final BigInteger N = new BigInteger(1, new byte[]{
            (byte) 0x89, (byte) 0x4B, (byte) 0x64, (byte) 0x5E, (byte) 0x89, (byte) 0xE1, (byte) 0x53, (byte) 0x5B,
            (byte) 0xBD, (byte) 0xAD, (byte) 0x5B, (byte) 0x8B, (byte) 0x29, (byte) 0x06, (byte) 0x50, (byte) 0x53,
            (byte) 0x08, (byte) 0x01, (byte) 0xB1, (byte) 0x8E, (byte) 0xBF, (byte) 0xBF, (byte) 0xC5, (byte) 0xE8,
            (byte) 0x60, (byte) 0x72, (byte) 0xFD, (byte) 0x16, (byte) 0xBD, (byte) 0x18, (byte) 0xFF, (byte) 0x8C
    });

    // g - 生成器
    public static final BigInteger g = BigInteger.valueOf(7);

    // k = H(N | g)
    private static final BigInteger k;

    static {
        k = sha1Hash(bigIntegerToFixedBytes(N, 32), bigIntegerToFixedBytes(g, 1));
    }

    public GruntSRP6(String username, byte[] salt, BigInteger verifier) {
        super(getUsernameHash(username), salt, verifier, N, g, k);
    }

    public GruntSRP6() {
        super();
    }

    @Override
    public BigInteger getN() {
        return N;
    }

    @Override
    public BigInteger getg() {
        return g;
    }

    @Override
    public BigInteger calculateServerEvidence(BigInteger A, BigInteger clientM1, BigInteger K) {
        // M2 = H(A | M1 | K)
        return sha1Hash(
                bigIntegerToFixedBytes(A, EPHEMERAL_KEY_LENGTH),
                bigIntegerToFixedBytes(clientM1, 20), // SHA1.DIGEST_LENGTH
                bigIntegerToFixedBytes(K, SESSION_KEY_LENGTH)
        );
    }

    @Override
    protected BigInteger calculateX(String username, String password, byte[] salt) {
        // x = H(s | H(username | ":" | password))
        BigInteger innerHash = sha1Hash((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return sha1Hash(salt, innerHash.toByteArray());
    }

    @Override
    protected Optional<BigInteger> doVerifyClientEvidence(BigInteger A, BigInteger clientM1) {
        try {
            // 检查A是否为0模N
            if (A.mod(N).equals(BigInteger.ZERO)) {
                return Optional.empty();
            }

            // u = H(A | B)
            BigInteger u = sha1Hash(
                    bigIntegerToFixedBytes(A, EPHEMERAL_KEY_LENGTH),
                    bigIntegerToFixedBytes(B, EPHEMERAL_KEY_LENGTH)
            );

            // 检查u是否为0模N
            if (u.mod(N).equals(BigInteger.ZERO)) {
                return Optional.empty();
            }

            // S = (A * v^u) ^ b mod N
            BigInteger S = A.multiply(v.modPow(u, N)).modPow(b, N);

            // 将S转换为字节数组
            byte[] S_bytes = bigIntegerToFixedBytes(S, EPHEMERAL_KEY_LENGTH);

            // 计算会话密钥K = SHA1Interleave(S)
            byte[] K_bytes = sha1Interleave(S_bytes);
            BigInteger K = new BigInteger(1, K_bytes);

            // 计算NgHash = H(N) xor H(g)
            byte[] N_hash = bigIntegerToFixedBytes(sha1Hash(bigIntegerToFixedBytes(N, 32)), 20);
            byte[] g_hash = bigIntegerToFixedBytes(sha1Hash(bigIntegerToFixedBytes(g, 1)), 20);
            byte[] Ng_hash = new byte[20];
            for (int i = 0; i < 20; i++) {
                Ng_hash[i] = (byte) (N_hash[i] ^ g_hash[i]);
            }

            // 计算我们的M1
            BigInteger ourM = sha1Hash(
                    Ng_hash,
                    I.toByteArray(),
                    s,
                    bigIntegerToFixedBytes(A, EPHEMERAL_KEY_LENGTH),
                    bigIntegerToFixedBytes(B, EPHEMERAL_KEY_LENGTH),
                    K_bytes
            );

            // 比较客户端M1和我们计算的M1
            if (ourM.equals(clientM1)) {
                return Optional.of(K);
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // SHA1Interleave实现
    private static byte[] sha1Interleave(byte[] S) throws NoSuchAlgorithmException {
        int len = S.length / 2;
        byte[] buf0 = new byte[len];
        byte[] buf1 = new byte[len];

        // 将S分割成两个缓冲区
        for (int i = 0; i < len; i++) {
            buf0[i] = S[2 * i];
            buf1[i] = S[2 * i + 1];
        }

        // 找到第一个非零字节的位置
        int p = 0;
        while (p < S.length && S[p] == 0) {
            p++;
        }
        if (p % 2 == 1) {
            p++;
        }
        p /= 2;

        // 分别哈希两个半部分
        MessageDigest md0 = MessageDigest.getInstance("SHA-1");
        md0.update(buf0, p, len - p);
        byte[] hash0 = md0.digest();

        MessageDigest md1 = MessageDigest.getInstance("SHA-1");
        md1.update(buf1, p, len - p);
        byte[] hash1 = md1.digest();

        // 合并两个哈希结果
        byte[] K = new byte[40];
        for (int i = 0; i < 20; i++) {
            K[2 * i] = hash0[i];
            K[2 * i + 1] = hash1[i];
        }

        return K;
    }


    public static RegistrationData makeRegistrationData(String username, String password) {
        GruntSRP6 srp6 = new GruntSRP6();
        BigInteger bigInteger = srp6.calculateVerifier(username, password, srp6.s);
        return new RegistrationData(srp6.s, bigInteger.toByteArray());
    }
}