package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.interfaces.*;


public class UnitSearcher implements IGridNotifierPlayer, IGridNotifierCreature {
    private final PhaseShift phaseShift;
    private final ICheck<unit> check;
    private Unit object;

    public UnitSearcher(WorldObject searcher, ICheck<unit> check, GridType gridType) {
        phaseShift = searcher.getPhaseShift();
        check = check;
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Creature> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);

            if (!creature.inSamePhase(phaseShift)) {
                continue;
            }

            if (check.invoke(creature)) {
                object = creature;

                return;
            }
        }
    }

    public final void visit(list<Player> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (!player.inSamePhase(phaseShift)) {
                continue;
            }

            if (check.invoke(player)) {
                object = player;

                return;
            }
        }
    }

    public final Unit getTarget() {
        return object;
    }


}
