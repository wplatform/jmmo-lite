package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.interfaces.*;


public class GameObjectSearcher implements IGridNotifierGameObject {
    private final PhaseShift phaseShift;
    private final ICheck<GameObject> check;
    private GameObject object;

    public GameObjectSearcher(WorldObject searcher, ICheck<GameObject> check, GridType gridType) {
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

    public final void visit(list<GameObject> objs) {
        // already found
        if (object) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var gameObject = objs.get(i);

            if (!gameObject.inSamePhase(phaseShift)) {
                continue;
            }

            if (check.invoke(gameObject)) {
                object = gameObject;

                return;
            }
        }
    }

    public final GameObject getTarget() {
        return object;
    }


}
