package com.github.azeroth.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


public class BNetV2SRP6 extends SRP6 {

    public static final BigInteger N = new BigInteger(1, new byte[256]); // 实际值应该从原始代码中获取
    public static final BigInteger g = BigInteger.valueOf(5);

    private final MessageDigest cryptoHash;

    public BNetV2SRP6(String username, byte[] salt, BigInteger verifier, MessageDigest hash) throws NoSuchAlgorithmException {
        super(username, salt, verifier, calculateK(hash));
        this.cryptoHash = hash;
    }

    public BNetV2SRP6() {
        super();
        try {
            this.cryptoHash = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize MessageDigest", e);
        }
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
    public byte getVersion() {
        return 2;
    }

    @Override
    public int getXIterations() {
        return 15000;
    }


    // 计算k = H(N | g)
    private static BigInteger calculateK(MessageDigest hash) throws NoSuchAlgorithmException {
        // 复制hash以避免修改原始对象
        MessageDigest md = MessageDigest.getInstance(hash.getAlgorithm());
        md.update(bigIntegerToFixedBytes(N, 256));
        md.update(bigIntegerToFixedBytes(g, 256));
        return new BigInteger(1, md.digest());
    }

    @Override
    protected BigInteger calculateU(BigInteger A) throws Exception {
        MessageDigest md = (MessageDigest) cryptoHash.clone();
        md.update(bigIntegerToFixedBytes(A, 256));
        md.update(bigIntegerToFixedBytes(B, 256));
        return new BigInteger(1, md.digest());
    }

    @Override
    protected BigInteger doCalculateEvidence(BigInteger[] bns) throws Exception {
        MessageDigest md = (MessageDigest) cryptoHash.clone();
        for (BigInteger bn : bns) {
            md.update(getBrokenEvidenceVector(bn));
        }
        return new BigInteger(1, md.digest());
    }

    // 获取证据向量
    protected static byte[] getBrokenEvidenceVector(BigInteger bn) {
        int bytes = (bn.bitLength() + 8) / 8;
        byte[] result = new byte[bytes];
        byte[] bnBytes = bn.toByteArray();

        // 如果bnBytes有符号位，跳过第一个字节
        int srcPos = (bnBytes[0] == 0) ? 1 : 0;
        int length = bnBytes.length - srcPos;

        // 确保result的长度足够
        if (result.length < length) {
            result = new byte[length];
        }

        // 复制字节到结果数组，确保大端序
        System.arraycopy(bnBytes, srcPos, result, result.length - length, length);
        return result;
    }

    @Override
    protected final Optional<BigInteger> doVerifyClientEvidence(BigInteger A, BigInteger clientM1) {
        try {
            BigInteger N = getN();
            // 检查A是否为0模N
            if (A.mod(N).equals(BigInteger.ZERO)) {
                return Optional.empty();
            }

            // 计算u
            BigInteger u = calculateU(A);

            // 检查u是否为0模N
            if (u.mod(N).equals(BigInteger.ZERO)) {
                return Optional.empty();
            }

            // 计算S = (A * v^u) ^ b mod N
            BigInteger S = A.multiply(v.modPow(u, N)).modPow(b, N);

            // 计算证据
            BigInteger[] evidenceBns = {A, B, S};
            BigInteger ourM = doCalculateEvidence(evidenceBns);

            // 比较客户端M1和我们计算的M1
            if (ourM.equals(clientM1)) {
                return Optional.of(S);
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    protected BigInteger calculateX(String username, String password, byte[] salt) throws Exception {
        // 使用PBKDF2-HMAC-SHA512
        PBEKeySpec spec = new PBEKeySpec(
                (username + ":" + password).toCharArray(),
                salt,
                getXIterations(),
                512
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] xBytes = factory.generateSecret(spec).getEncoded();

        // 处理最高位为1的情况
        if ((xBytes[0] & 0x80) != 0) {
            // 创建一个65字节的数组，最后一位为1
            byte[] fix = new byte[65];
            fix[64] = 1;
            BigInteger fixBn = new BigInteger(1, fix);
            return new BigInteger(1, xBytes).subtract(fixBn).mod(N.subtract(BigInteger.ONE));
        }

        return new BigInteger(1, xBytes).mod(N.subtract(BigInteger.ONE));
    }

    @Override
    public final BigInteger calculateServerEvidence(BigInteger A, BigInteger clientM1, BigInteger K) throws Exception {
        // 证据向量: A, clientM1, K
        BigInteger[] evidenceBns = {A, clientM1, K};
        return doCalculateEvidence(evidenceBns);
    }
}