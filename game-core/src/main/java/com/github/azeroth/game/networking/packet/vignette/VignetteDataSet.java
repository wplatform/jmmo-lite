package com.github.azeroth.game.networking.packet.vignette;

import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.Data;

import java.util.List;
@Data
public class VignetteDataSet {

    List<ObjectGuid> IDs;
    List<VignetteData> Data;

}
