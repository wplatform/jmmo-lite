package com.github.azeroth.game.domain.map;

import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.domain.map.enums.ZLiquidStatus;
import lombok.Data;


@Data
public class PositionFullTerrainStatus {
    private int areaId;
    private float floorZ;
    private boolean outdoors;
    private EnumFlag<ZLiquidStatus> liquidStatus;
    private WmoLocation wmoLocation;
    private LiquidData liquidInfo;
}
