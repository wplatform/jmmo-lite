package Globals;


import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.gobject.Transport;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.scene.SceneObject;
import com.github.azeroth.game.entity.unit.Unit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ObjectAccessor {
    private final Object _lockObject = new Object();
    private final HashMap<ObjectGuid, Player> _players = new HashMap<ObjectGuid, Player>();

    private ObjectAccessor() {
    }

    public static Corpse GetCorpse(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetCorpse(guid);
    }

    public static GameObject GetGameObject(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetGameObject(guid);
    }

    public static Transport GetTransport(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetTransport(guid);
    }

    public static Conversation GetConversation(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetConversation(guid);
    }

    public static Creature GetCreature(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetCreature(guid);
    }

    public static Pet GetPet(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetPet(guid);
    }

    public static Creature GetCreatureOrPetOrVehicle(WorldObject u, ObjectGuid guid) {
        if (guid.isPet()) {
            return GetPet(u, guid);
        }

        if (guid.isCreatureOrVehicle()) {
            return GetCreature(u, guid);
        }

        return null;
    }

    private static DynamicObject GetDynamicObject(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetDynamicObject(guid);
    }

    private static AreaTrigger GetAreaTrigger(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetAreaTrigger(guid);
    }

    private static SceneObject GetSceneObject(WorldObject u, ObjectGuid guid) {
        return u.getMap().GetSceneObject(guid);
    }

    public final WorldObject GetWorldObject(WorldObject p, ObjectGuid guid) {
        switch (guid.getHigh()) {
            case Player:
                return GetPlayer(p, guid);
            case Transport:
            case GameObject:
                return GetGameObject(p, guid);
            case Vehicle:
            case Creature:
                return GetCreature(p, guid);
            case Pet:
                return GetPet(p, guid);
            case DynamicObject:
                return GetDynamicObject(p, guid);
            case AreaTrigger:
                return GetAreaTrigger(p, guid);
            case Corpse:
                return GetCorpse(p, guid);
            case SceneObject:
                return GetSceneObject(p, guid);
            case Conversation:
                return GetConversation(p, guid);
            default:
                return null;
        }
    }

    public final WorldObject GetObjectByTypeMask(WorldObject p, ObjectGuid guid, TypeMask typemask) {
        switch (guid.getHigh()) {
            case Item:
                if (typemask.hasFlag(TypeMask.Item) && p.IsTypeId(TypeId.PLAYER)) {
                    return ((Player) p).GetItemByGuid(guid);
                }

                break;
            case Player:
                if (typemask.hasFlag(TypeMask.Player)) {
                    return GetPlayer(p, guid);
                }

                break;
            case Transport:
            case GameObject:
                if (typemask.hasFlag(TypeMask.GameObject)) {
                    return GetGameObject(p, guid);
                }

                break;
            case Creature:
            case Vehicle:
                if (typemask.hasFlag(TypeMask.Unit)) {
                    return GetCreature(p, guid);
                }

                break;
            case Pet:
                if (typemask.hasFlag(TypeMask.Unit)) {
                    return GetPet(p, guid);
                }

                break;
            case DynamicObject:
                if (typemask.hasFlag(TypeMask.DynamicObject)) {
                    return GetDynamicObject(p, guid);
                }

                break;
            case AreaTrigger:
                if (typemask.hasFlag(TypeMask.AreaTrigger)) {
                    return GetAreaTrigger(p, guid);
                }

                break;
            case SceneObject:
                if (typemask.hasFlag(TypeMask.SceneObject)) {
                    return GetSceneObject(p, guid);
                }

                break;
            case Conversation:
                if (typemask.hasFlag(TypeMask.Conversation)) {
                    return GetConversation(p, guid);
                }

                break;
            case Corpse:
                break;
        }

        return null;
    }

    public final Unit GetUnit(WorldObject u, ObjectGuid guid) {
        if (guid.isPlayer()) {
            return GetPlayer(u, guid);
        }

        if (guid.isPet()) {
            return GetPet(u, guid);
        }

        return GetCreature(u, guid);
    }

    public final Player FindPlayerByName(String name) {
        var player = PlayerNameMapHolder.Find(name);

        if (!player || !player.isInWorld()) {
            return null;
        }

        return player;
    }

    public final Player GetPlayer(Map m, ObjectGuid guid) {
        var player = _players.get(guid);

        if (player) {
            if (player.IsInWorld && player.Map == m) {
                return player;
            }
        }

        return null;
    }

    public final Player GetPlayer(WorldObject u, ObjectGuid guid) {
        return GetPlayer(u.getMap(), guid);
    }

    public final Player FindConnectedPlayerByName(String name) {
        return PlayerNameMapHolder.Find(name);
    }

    public final void SaveAllPlayers() {
        synchronized (_lockObject) {
            for (var pl : GetPlayers()) {
                pl.SaveToDB();
            }
        }
    }

    public final Collection<Player> GetPlayers() {
        synchronized (_lockObject) {
            return _players.values();
        }
    }

    public final void AddObject(Player obj) {
        synchronized (_lockObject) {
            PlayerNameMapHolder.Insert(obj);
            _players.put(obj.getGUID(), obj);
        }
    }

    // these functions return objects if found in whole world
    // ACCESS LIKE THAT IS NOT THREAD SAFE
    public final Player FindPlayer(ObjectGuid guid) {
        var player = FindConnectedPlayer(guid);

        return player && player.isInWorld() ? player : null;
    }

    public final Player FindPlayerByLowGUID(long lowguid) {
        var guid = ObjectGuid.Create(HighGuid.Player, lowguid);

        return FindPlayer(guid);
    }

    // this returns Player even if he is not in world, for example teleporting
    public final Player FindConnectedPlayer(ObjectGuid guid) {
        synchronized (_lockObject) {
            return _players.get(guid);
        }
    }

    public final void RemoveObject(Player obj) {
        synchronized (_lockObject) {
            PlayerNameMapHolder.Remove(obj);
            _players.remove(obj.getGUID());
        }
    }
}
