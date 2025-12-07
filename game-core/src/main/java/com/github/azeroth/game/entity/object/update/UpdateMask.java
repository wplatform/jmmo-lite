package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class UpdateMask {
    private UpdateMask() { }

    // Block type in original C++ was a 32-bit unsigned integer
    public static final int BLOCK_BITS = Integer.SIZE; // 32

    /**
     * Get number of 32-bit blocks required to hold bitCount bits.
     */
    public static int getBlockCount(int bitCount) {
        return (bitCount + BLOCK_BITS - 1) / BLOCK_BITS;
    }

    /**
     * Encodes dynamic field change type into the blockCount value similar to C++ implementation.
     * The original expression uses integer math to yield 0 unless updateType indicates VALUES.
     *
     * @param blockCount raw block count
     * @param changeType change type enum
     * @param updateType update type (same semantics as C++ caller)
     * @return encoded value combining blockCount and change marker
     */
    public static int encodeDynamicFieldChangeType(int blockCount, DynFieldChangeType changeType, ObjectUpdateType updateType) {
        int changeVal = changeType.getValue();
        // replicate: ((changeType & VALUE_AND_SIZE_CHANGED) * ((3 - updateType) / 3))
        int mask = changeVal & DynFieldChangeType.VALUE_AND_SIZE_CHANGED.getValue();
        int divisor = (3 - updateType.ordinal()) / 3; // integer division: yields 0 unless updateType == 0
        return blockCount | (mask * divisor);
    }

    /**
     * Set a single bit (bitIndex) in the given int[] bitfield array.
     * This mirrors the template SetUpdateBit for 32-bit blocks in C++.
     */
    public static void setUpdateBit(WorldPacket data, int bitBlockPos, int bitIndex) {
        final int bitsPerBlock = Integer.SIZE;
        int updatePos = bitBlockPos + (bitIndex / bitsPerBlock);
        int offset = bitIndex % bitsPerBlock;
        ByteBuf content = data.content();
        content.setInt(updatePos, content.getInt(updatePos) | (1 << offset));
    }

}
