package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.interfaces.*;

import java.util.ArrayList;


public class WorldObjectTypedList {
    private final ArrayList<Player> players = new ArrayList<>();
    private final ArrayList<Creature> creatures = new ArrayList<>();
    private final ArrayList<Corpse> corpses = new ArrayList<>();
    private final ArrayList<DynamicObject> dynamicObjects = new ArrayList<>();
    private final ArrayList<areaTrigger> areaTriggers = new ArrayList<>();
    private final ArrayList<sceneObject> sceneObjects = new ArrayList<>();
    private final ArrayList<conversation> conversations = new ArrayList<>();
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();
    private final ArrayList<WorldObject> worldObjects = new ArrayList<>();

    public final void insert(WorldObject obj) {
        if (obj == null) {
            Log.outWarn(LogFilter.Maps, String.format("Tried to insert null during %1$s to %2$s", "WorldObject", "WorldObjectTypedList"));
            Log.outWarn(LogFilter.Maps, Environment.StackTrace);

            return;
        }

        synchronized (worldObjects) {
            worldObjects.add(obj);

            switch (obj.getObjectTypeId()) {
                case Unit:
                    creatures.add((CREATURE) obj);

                    break;
                case Player:
                    players.add((player) obj);

                    break;
                case GameObject:
                    gameObjects.add((gameObject) obj);

                    break;
                case DynamicObject:
                    dynamicObjects.add((DynamicObject) obj);

                    break;
                case Corpse:
                    corpses.add((Corpse) obj);

                    break;
                case AreaTrigger:
                    areaTriggers.add((areaTrigger) obj);

                    break;
                case SceneObject:
                    sceneObjects.add((sceneObject) obj);

                    break;
                case Conversation:
                    conversations.add((conversation) obj);

                    break;
            }
        }
    }

    public final void remove(WorldObject obj) {
        if (obj == null) {
            Log.outWarn(LogFilter.Maps, String.format("Tried to remove null during %1$s to %2$s", "WorldObject", "WorldObjectTypedList"));
            Log.outWarn(LogFilter.Maps, Environment.StackTrace);

            return;
        }

        synchronized (worldObjects) {
            worldObjects.remove(obj);

            switch (obj.getObjectTypeId()) {
                case Unit:
                    creatures.remove((CREATURE) obj);

                    break;
                case Player:
                    players.remove((player) obj);

                    break;
                case GameObject:
                    gameObjects.remove((gameObject) obj);

                    break;
                case DynamicObject:
                    dynamicObjects.remove((DynamicObject) obj);

                    break;
                case Corpse:
                    corpses.remove((Corpse) obj);

                    break;
                case AreaTrigger:
                    areaTriggers.remove((areaTrigger) obj);

                    break;
                case SceneObject:
                    sceneObjects.remove((sceneObject) obj);

                    break;
                case Conversation:
                    conversations.remove((conversation) obj);

                    break;
            }
        }
    }

    public final void visit(IGridNotifier visitor) {
        if (visitor instanceof IGridNotifierGameObject go) {
            go.visit(gameObjects);
        }

        if (visitor instanceof IGridNotifierCreature cr) {
            cr.visit(creatures);
        }

        if (visitor instanceof IGridNotifierDynamicObject dyn) {
            dyn.visit(dynamicObjects);
        }

        if (visitor instanceof IGridNotifierCorpse cor) {
            cor.visit(corpses);
        }

        if (visitor instanceof IGridNotifierAreaTrigger at) {
            at.visit(areaTriggers);
        }

        if (visitor instanceof IGridNotifierSceneObject so) {
            so.visit(sceneObjects);
        }

        if (visitor instanceof IGridNotifierConversation conv) {
            conv.visit(conversations);
        }

        if (visitor instanceof IGridNotifierWorldObject wo) {
            wo.visit(worldObjects);
        }

        if (visitor instanceof IGridNotifierPlayer p) {
            p.visit(players);
        }
    }

    public final boolean contains(WorldObject obj) {
        synchronized (worldObjects) {
            return worldObjects.contains(obj);
        }
    }

    public final <T> int getCount() {
        synchronized (worldObjects) {
            switch (T.class.name) {
                case "Creature":
                    return creatures.size();
                case "Player":
                    return players.size();
                case "GameObject":
                    return gameObjects.size();
                case "DynamicObject":
                    return dynamicObjects.size();
                case "Corpse":
                    return corpses.size();
                case "AreaTrigger":
                    return areaTriggers.size();
                case "Conversation":
                    return conversations.size();
                case "SceneObject":
                    return sceneObjects.size();
            }
        }

        return 0;
    }
}
