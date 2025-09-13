package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.interfaces.*;


public class GameObjectWorker implements IGridNotifierGameObject {
    private final PhaseShift phaseShift;
    private final IDoWork<GameObject> doWork;

    public GameObjectWorker(WorldObject searcher, IDoWork<GameObject> work, GridType gridType) {
        phaseShift = searcher.getPhaseShift();
        doWork = work;
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<GameObject> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var gameObject = objs.get(i);

            if (gameObject.inSamePhase(phaseShift)) {
                doWork.invoke(gameObject);
            }
        }
    }


}
