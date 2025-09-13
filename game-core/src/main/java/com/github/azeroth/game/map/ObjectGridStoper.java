package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.map.interfaces.IGridNotifierCreature;

import java.util.list;


class ObjectGridStoper implements IGridNotifierCreature {
    public ObjectGridStoper(GridType gridType) {
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Creature> objs) {
        // stop any fights at grid de-activation and remove dynobjects/areatriggers created at cast by creatures
        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);
            creature.removeAllDynObjects();
            creature.removeAllAreaTriggers();

            if (creature.isInCombat()) {
                creature.combatStop();
            }
        }
    }


}
