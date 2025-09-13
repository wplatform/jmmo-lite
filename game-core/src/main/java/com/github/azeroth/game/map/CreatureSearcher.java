package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.interfaces.*;


public class CreatureSearcher implements IGridNotifierCreature {
    private final PhaseShift phaseShift;
    private final ICheck<Creature> check;
    private Creature object;

    public CreatureSearcher(WorldObject searcher, ICheck<Creature> check, GridType gridType) {
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
        // already found
        if (object) {
            return;
        }

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

    public final Creature getTarget() {
        return object;
    }


}
