package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.interfaces.*;

import java.util.ArrayList;
import java.util.Objects;

public class UnitListSearcher implements IGridNotifierCreature, IGridNotifierPlayer {
    private final PhaseShift phaseShift;
    private final ArrayList<Unit> objects;
    private final ICheck<unit> check;

    public UnitListSearcher(WorldObject searcher, ArrayList<Unit> objects, ICheck<unit> check, GridType gridType) {
        phaseShift = searcher.getPhaseShift();
        objects = objects;
        check = check;
        setGridType(gridType);
    }

    public final GridType getGridType() {
        return gridType;
    }    private gridType gridType = getGridType().values()[0];

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Creature> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);

            if (creature.inSamePhase(phaseShift)) {
                if (check.invoke(creature)) {
                    Objects.add(creature);
                }
            }
        }
    }

    public final void visit(list<Player> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (player.inSamePhase(phaseShift)) {
                if (check.invoke(player)) {
                    Objects.add(player);
                }
            }
        }
    }




}
