package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.interfaces.IGridNotifierPlayer;
import game.PhaseShift;

import java.util.list;


public class PlayerWorker implements IGridNotifierPlayer {
    private final PhaseShift phaseShift;
    private final tangible.Action1Param<Player> action;

    public PlayerWorker(WorldObject searcher, tangible.Action1Param<Player> action, GridType gridType) {
        phaseShift = searcher.getPhaseShift();
        action = action;
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Player> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (player.inSamePhase(phaseShift)) {
                action.invoke(player);
            }
        }
    }


}
