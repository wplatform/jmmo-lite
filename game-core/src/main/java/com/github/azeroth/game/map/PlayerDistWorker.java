package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.interfaces.IGridNotifierPlayer;

import java.util.list;

public class PlayerDistWorker implements IGridNotifierPlayer {
    private final WorldObject searcher;
    private final float dist;
    private final IDoWork<Player> doWork;

    public PlayerDistWorker(WorldObject searcher, float dist, IDoWork<Player> work, GridType gridType) {
        searcher = searcher;
        dist = dist;
        doWork = work;
        setGridType(gridType);
    }

    public final GridType getGridType() {
        return gridType;
    }    private gridType gridType = getGridType().values()[0];

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Player> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (player.inSamePhase(searcher) && player.isWithinDist(searcher, dist)) {
                doWork.invoke(player);
            }
        }
    }




}
