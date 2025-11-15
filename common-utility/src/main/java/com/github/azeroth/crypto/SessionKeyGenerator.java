package com.github.azeroth.crypto;

import com.github.azeroth.utils.SecureUtils;

public class SessionKeyGenerator {
    private final byte[] o0;
    private final byte[] o1;
    private final byte[] o2;
    private int o0Index;


    public SessionKeyGenerator(byte[] buffer) {

        int len = buffer.length;
        int halfLen = len / 2;

        this.o1 = SecureUtils.sha512(buffer, 0, halfLen);

        this.o2 = SecureUtils.sha512(buffer, halfLen, len - halfLen);

        this.o0 = SecureUtils.sha512(this.o1, /*this.o0,*/ this.o2);

        this.o0Index = 0;
    }


    public void generate(byte[] buffer, int size) {
        for (int i = 0; i < size; ++i) {
            if (o0Index == o0.length) {
                System.arraycopy(SecureUtils.sha512(o1, o0, o2), 0, o0, 0, o0.length);
                o0Index = 0;
            }
            buffer[i] = o0[o0Index++];
        }
    }
}
