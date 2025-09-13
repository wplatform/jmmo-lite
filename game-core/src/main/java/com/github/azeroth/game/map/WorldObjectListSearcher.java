package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.interfaces.*;

import java.util.ArrayList;
import java.util.Objects;


public class WorldObjectListSearcher implements IGridNotifierPlayer, IGridNotifierCreature, IGridNotifierCorpse, IGridNotifierGameObject, IGridNotifierDynamicObject, IGridNotifierAreaTrigger, IGridNotifierSceneObject, IGridNotifierConversation {
    private final ArrayList<WorldObject> objects;
    private final PhaseShift phaseShift;
    private final ICheck<WorldObject> check;
    private GridMapTypemask mask = GridMapTypeMask.values()[0];    private gridType gridType = getGridType().values()[0];

    public WorldObjectListSearcher(WorldObject searcher, ArrayList<WorldObject> objects, ICheck<WorldObject> check, GridMapTypeMask mapTypeMask) {
        this(searcher, objects, check, mapTypeMask, gridType.All);
    }

    public WorldObjectListSearcher(WorldObject searcher, ArrayList<WorldObject> objects, ICheck<WorldObject> check) {
        this(searcher, objects, check, GridMapTypeMask.All, gridType.All);
    }

    public WorldObjectListSearcher(WorldObject searcher, ArrayList<WorldObject> objects, ICheck<WorldObject> check, GridMapTypeMask mapTypeMask, GridType gridType) {
        setMask(mapTypeMask);
        phaseShift = searcher.getPhaseShift();
        objects = objects;
        check = check;
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

            if (areaTrigger.inSamePhase(phaseShift) && check.invoke(areaTrigger)) {
                Objects.add(areaTrigger);
            }
        }
    }

    public final void visit(list<conversation> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.conversation)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var conversation = objs.get(i);

            if (conversation.inSamePhase(phaseShift) && check.invoke(conversation)) {
                Objects.add(conversation);
            }
        }
    }

    public final void visit(list<Corpse> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.Corpse)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var corpse = objs.get(i);

            if (corpse.inSamePhase(phaseShift) && check.invoke(corpse)) {
                Objects.add(corpse);
            }
        }
    }

    public final void visit(list<Creature> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.CREATURE)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);

            if (creature.inSamePhase(phaseShift) && check.invoke(creature)) {
                Objects.add(creature);
            }
        }
    }

    public final void visit(list<DynamicObject> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.DynamicObject)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var dynamicObject = objs.get(i);

            if (dynamicObject.inSamePhase(phaseShift) && check.invoke(dynamicObject)) {
                Objects.add(dynamicObject);
            }
        }
    }

    public final void visit(list<GameObject> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.gameObject)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var gameObject = objs.get(i);

            if (gameObject.inSamePhase(phaseShift) && check.invoke(gameObject)) {
                Objects.add(gameObject);
            }
        }
    }

    public final void visit(list<Player> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.player)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);

            if (player.inSamePhase(phaseShift) && check.invoke(player)) {
                Objects.add(player);
            }
        }
    }

    public final void visit(list<sceneObject> objs) {
        if (!getMask().hasFlag(GridMapTypeMask.conversation)) {
            return;
        }

        for (var i = 0; i < objs.size(); ++i) {
            var sceneObject = objs.get(i);

            if (check.invoke(sceneObject)) {
                Objects.add(sceneObject);
            }
        }
    }


}
