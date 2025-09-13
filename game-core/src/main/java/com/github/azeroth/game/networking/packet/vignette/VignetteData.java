package com.github.azeroth.game.networking.packet.vignette;

import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.Data;

@Data
public class VignetteData {
    public ObjectGuid ObjGUID;
    public Vector3 position;
    public int vignetteID = 0;
    public int ZoneID = 0;
    public int WMOGroupID = 0;
    public int WMODoodadPlacementID = 0;
    float HealthPercent = 1.0f;
    short RecommendedGroupSizeMin = 0;
    short RecommendedGroupSizeMax = 0;

}
