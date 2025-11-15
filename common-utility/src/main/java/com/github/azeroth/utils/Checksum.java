package com.github.azeroth.utils;

import java.util.zip.Adler32;

public class Checksum {

    public final Adler32 INSTANCE = new Adler32();

    public int adler32(int adler, byte[] data, int offset, int len) {
        INSTANCE.reset();
        INSTANCE.update(adler);
        INSTANCE.update(data, offset, len);
        return (int) INSTANCE.getValue();
    }
}
