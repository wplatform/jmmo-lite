package com.github.azeroth.game.domain.map;


import lombok.Data;

@Data
public class AreaInfo {
    public float floorZ = MapDefine.VMAP_INVALID_HEIGHT_VALUE;

    public boolean result = false;

    //Area info
    public int adtId;
    public int rootId;
    public int groupId;
    public int flags;
    //liquid data
    public int liquidType;
    public float liquidLevel;
    public boolean liquidEnabled;

}
