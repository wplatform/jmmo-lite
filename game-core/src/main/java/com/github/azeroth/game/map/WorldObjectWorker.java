package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.interfaces.*;


public class WorldObjectWorker implements IGridNotifierPlayer, IGridNotifierCreature, IGridNotifierCorpse, IGridNotifierGameObject, IGridNotifierDynamicObject, IGridNotifierAreaTrigger, IGridNotifierSceneObject, IGridNotifierConversation {
    private final PhaseShift phaseShift;
    private final IDoWork<WorldObject> doWork;
    private GridMapTypemask mask = GridMapTypeMask.values()[0];    private gridType gridType = getGridType().values()[0];

    public WorldObjectWorker(WorldObject searcher, IDoWork<WorldObject> work, GridMapTypeMask mapTypeMask) {
        this(searcher, work, mapTypeMask, gridType.All);
    }

    public WorldObjectWorker(WorldObject searcher, IDoWork<WorldObject> work) {
        this(searcher, work, GridMapTypeMask.All, gridType.All);
    }

    public WorldObjectWorker(WorldObject searcher, IDoWork<WorldObject> work, GridMapTypeMask mapTypeMask, GridType gridType) {
        setMask(mapTypeMask);
        phaseShift = searcher.getPhaseShift();
        doWork = work;
        setGridType(gridType);
    }

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final GridMapTypeMask getMask() {
        return mask;
    }

    public final void setMask(GridMapTypeMask value) {
        mask = value;
    }

    public final void visit(list<areaTrigger> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.areaTrigger)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var areaTrigger = objs.get(i);

            if (areaTrigger.inSamePhase(phaseShift)) {
                doWork.invoke(areaTrigger);
            }
        }
    }

    public final void visit(list<conversation> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.conversation)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var conversation = objs.get(i);

            if (conversation.inSamePhase(phaseShift)) {
                doWork.invoke(conversation);
            }
        }
    }

    public final void visit(list<Corpse> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.Corpse)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var corpse = objs.get(i);

            if (corpse.inSamePhase(phaseShift)) {
                doWork.invoke(corpse);
            }
        }
    }

    public final void visit(list<Creature> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.CREATURE)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);

            if (creature.inSamePhase(phaseShift)) {
                doWork.invoke(creature);
            }
        }
    }

    public final void visit(list<DynamicObject> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.DynamicObject)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var dynamicObject = objs.get(i);

            if (dynamicObject.inSamePhase(phaseShift)) {
                doWork.invoke(dynamicObject);
            }
        }
    }

    public final void visit(list<GameObject> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.gameObject)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var gameObject = objs.get(i);

            if (gameObject.inSamePhase(phaseShift)) {
                doWork.invoke(gameObject);
            }
        }
    }

    public final void visit(list<Player> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.player)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (player.inSamePhase(phaseShift)) {
                doWork.invoke(player);
            }
        }
    }

    public final void visit(list<sceneObject> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.sceneObject)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var sceneObject = objs.get(i);

            if (sceneObject.inSamePhase(phaseShift)) {
                doWork.invoke(sceneObject);
            }
        }
    }


}
