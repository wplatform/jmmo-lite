package com.github.azeroth.game.world.setting;

import lombok.Data;

@Data
public class MapSetting {

    public boolean mMapEnablePathFinding;
    public boolean vMapEnableLOS;
    public boolean vMapEnableHeight;
    public boolean vMapEnableIndoorCheck;
    public int mapUpdateThreads;
}
