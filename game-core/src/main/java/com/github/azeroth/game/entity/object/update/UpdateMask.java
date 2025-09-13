package com.github.azeroth.game.entity.object.update;

import java.util.Arrays;




public class UpdateMask {
    private final int blockCount;
    private final int blocksMaskCount;

    private final int[] blocks;

    private final int[] blocksMask;

    public final boolean get(int index) {
        int blockIndex = getBlockIndex(index);
        int blockFlag = getBlockFlag(index);
        return (blocks[blockIndex] & blockFlag) != 0;
    }


    public UpdateMask(int bits) {
        this(bits, null);
    }


    public UpdateMask(int bits, int[] input) {
        blockCount = (bits + 31) / 32;
        blocksMaskCount = (blockCount + 31) / 32;

        blocks = new int[blockCount];

        blocksMask = new int[blocksMaskCount];

        if (input != null) {
            var block = 0;

            for (; block < input.length; ++block) {
                if ((blocks[block] = input[block]) != 0) {

                    blocksMask[getBlockIndex(block)] |= (int)getBlockFlag(block);
                }
            }

            for (; block < blockCount; ++block) {
                blocks[block] = 0;
            }
        }
    }


    public final int getBlocksMask(int index) {
        return blocksMask[index];
    }


    public final int getBlock(int index) {
        return blocks[index] ;
    }

    public final boolean isAnySet() {
        return Arrays.stream(blocksMask).anyMatch(blockMask -> blockMask != 0);
    }

    public final void reset(int index) {
        var blockIndex = getBlockIndex(index);

        if ((blocks[blockIndex] &= ~(int)getBlockFlag(index)) == 0) {
            blocksMask[getBlockIndex(blockIndex)] &= ~(int)getBlockFlag(blockIndex);
        }
    }

    public final void resetAll() {
        Arrays.fill(blocks, 0, blocks.length, 0);
        Arrays.fill(blocksMask, 0, blocksMask.length, 0);
    }

    public final void set(int index) {
        var blockIndex = getBlockIndex(index);
        blocks[blockIndex] |= getBlockFlag(index);
        blocksMask[getBlockIndex(blockIndex)] |= getBlockFlag(blockIndex);
    }

    public final void setAll() {
        for (var i = 0; i < blocksMaskCount; ++i) {
            blocksMask[i] = 0xFFFFFFFF;
        }

        for (var i = 0; i < blockCount; ++i) {
            blocks[i] = 0xFFFFFFFF;
        }

        if ((blocksMaskCount % 32) != 0) {
            var unused = 32 - (blocksMaskCount % 32);
            blocksMask[blocksMaskCount - 1] &= (0xFFFFFFFF >>> unused);
        }

        if ((blockCount % 32) != 0) {
            var unused = 32 - (blockCount % 32);
            blocks[blockCount - 1] &= (0xFFFFFFFF >>> unused);
        }
    }

    public final void and(UpdateMask right) {
        for (var i = 0; i < blocksMaskCount; ++i) {
            blocksMask[i] &= right.blocksMask[i];
        }

        for (var i = 0; i < blockCount; ++i) {
            if ((blocks[i] &= right.blocks[i]) == 0) {
                blocksMask[getBlockIndex(i)] &= ~(int)getBlockFlag(i);
            }
        }
    }

    public final void or(UpdateMask right) {
        for (var i = 0; i < blocksMaskCount; ++i) {
            blocksMask[i] |= right.blocksMask[i];
        }

        for (var i = 0; i < blockCount; ++i) {
            blocks[i] |= right.blocks[i];
        }
    }

    public static UpdateMask opBitwiseAnd(UpdateMask left, UpdateMask right) {
        var result = left;
        result.and(right);

        return result;
    }

    public static UpdateMask opBitwiseOr(UpdateMask left, UpdateMask right) {
        var result = left;
        result.or(right);

        return result;
    }

    //helpers
    public static int getBlockIndex(int bit) {
        return bit / 32;
    }

    public static int getBlockFlag(int bit) {
        return 1 << (bit % 32);
    }
}