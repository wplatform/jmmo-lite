package com.github.azeroth.game.domain.misc;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaypointPath {
    public int id;
    public final ArrayList<WaypointNode> nodes = new ArrayList<>();
    public ArrayList<Pair<Integer, Integer>> continuousSegments;
    public WaypointMoveType moveType = WaypointMoveType.WALK;
    public EnumFlag<WaypointPathFlag> flags = EnumFlag.of(WaypointPathFlag.None);
    public Float velocity;


    public void buildSegments() {
        continuousSegments.add(Pair.of(0, 0));
        for (int i = 0; i < nodes.size(); ++i) {
            continuousSegments.getLast().second(continuousSegments.getLast().second() + 1);
            // split on delay
            if (i + 1 != nodes.size() && nodes.get(i).delay > 0)
                continuousSegments.add(Pair.of(i, 1));
        }
    }

}
