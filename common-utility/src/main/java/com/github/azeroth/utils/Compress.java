package com.github.azeroth.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compress {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static byte[] compress(byte[] data, int compressionLevel) {
        if (data == null || data.length == 0) {
            return data;
        }

        Deflater deflater = null;
        ByteArrayOutputStream bos = null;

        try {
            // 创建Deflater实例并设置压缩级别
            deflater = new Deflater(compressionLevel);
            deflater.setInput(data);
            deflater.finish();

            // 创建输出流和缓冲区
            bos = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            // 压缩数据
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                bos.write(buffer, 0, count);
            }

            return bos.toByteArray();
        } finally {
            // 确保资源正确释放
            if (deflater != null) {
                deflater.end();
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception ignored) {
                    // 忽略关闭异常
                }
            }
        }
    }


}
