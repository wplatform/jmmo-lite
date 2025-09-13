package com.github.azeroth.game.domain.map.model;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.MapDefine;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@NoArgsConstructor
public class WmoLiquid {
    private int tilesX;
    private int tilesY;
    private Vector3 corner;
    private int type;
    private float[] height;
    private byte[] flags;


    public WmoLiquid(int width, int height, Vector3 corner, int type) {
        tilesX = width;
        tilesY = height;
        this.corner = corner;
        this.type = type;

        if (width != 0 && height != 0) {
            this.height = new float[(width + 1) * (height + 1)];
            flags = new byte[width * height];
        } else {
            this.height = new float[1];
            flags = null;
        }
    }

    public static WmoLiquid readFromFile(ByteBuffer buffer) {
        WmoLiquid liquid = new WmoLiquid();

        liquid.tilesX = buffer.getInt();
        liquid.tilesY = buffer.getInt();
        liquid.corner = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        liquid.type = buffer.getInt();

        if (liquid.tilesX != 0 && liquid.tilesY != 0) {
            var size = (liquid.tilesX + 1) * (liquid.tilesY + 1);
            buffer.asFloatBuffer().get(liquid.height, 0, size);

            size = liquid.tilesX * liquid.tilesY;
            buffer.get(liquid.flags, 0, size);
        } else {
            liquid.height = new float[1];
            liquid.height[0] = buffer.getFloat();
        }

        return liquid;
    }

    public final boolean getLiquidHeight(Vector3 pos, Distance liqHeight) {
        // simple case
        if (flags == null) {
            liqHeight.distance = height[0];

            return true;
        }

        var tx_f = (pos.x - corner.y) / MapDefine.LIQUID_TILE_SIZE;
        var tx = Float.floatToIntBits(tx_f);

        if (tx_f < 0.0f || tx >= tilesX) {
            return false;
        }

        var ty_f = (pos.y - corner.y) / MapDefine.LIQUID_TILE_SIZE;
        var ty = (int) ty_f;

        if (ty_f < 0.0f || ty >= tilesY) {
            return false;
        }

        // check if tile shall be used for liquid level
        // checking for 0x08 *might* be enough, but disabled tiles always are 0x?F:
        if ((flags[tx + ty * tilesX] & 0x0F) == 0x0F) {
            return false;
        }

        // (dx, dy) coordinates inside tile, in [0, 1]^2
        var dx = tx_f - tx;
        var dy = ty_f - ty;

        /* Tesselate tile to two triangles (not sure if client does it exactly like this)

            ^ dy
            |
          1 x---------x (1, 1)
            | (b)   / |
            |     /   |
            |   /     |
            | /   (a) |
            x---------x---> dx
          0           1
        */

        var rowOffset = tilesX + 1;

        float sx, sy;
        if (dx > dy) // case (a)
        {
            sx = height[tx + 1 + ty * rowOffset] - height[tx + ty * rowOffset];
            sy = height[tx + 1 + (ty + 1) * rowOffset] - height[tx + 1 + ty * rowOffset];
        } else // case (b)
        {
            sx = height[tx + 1 + (ty + 1) * rowOffset] - height[tx + (ty + 1) * rowOffset];
            sy = height[tx + (ty + 1) * rowOffset] - height[tx + ty * rowOffset];
        }
        liqHeight.distance = height[tx + ty * rowOffset] + dx * sx + dy * sy;

        return true;
    }

    public final int getLiquidType() {
        return type;
    }
}
