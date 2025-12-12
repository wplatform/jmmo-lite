package com.github.azeroth.game.domain.map.model;

public class OffMeshData {

    public final static int SIZE = 44;

    public int mapId;
    public int tileX;
    public int tileY;
    public float[] from = new float[3];
    public float[] to = new float[3];
    public float radius;
    public byte connectionFlags;
    public byte areaId;
    public short flags;

}
