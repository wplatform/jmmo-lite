package com.github.azeroth.game.world;

import com.github.azeroth.common.Locale;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.GameTableManager;
import com.github.azeroth.game.battlefield.BattleFieldManager;
import com.github.azeroth.game.chat.LanguageManager;
import com.github.azeroth.game.condition.ConditionManager;
import com.github.azeroth.game.condition.DisableManager;
import com.github.azeroth.game.domain.transport.TransportManager;
import com.github.azeroth.game.dungeonfinding.LfgManager;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.conversation.Conversation;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.gobject.Transport;
import com.github.azeroth.game.entity.object.GenericObject;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.object.enums.TypeMask;
import com.github.azeroth.game.entity.pet.Pet;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.scene.SceneObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.globals.ObjectManager;
import com.github.azeroth.game.event.WorldEventPublisher;
import com.github.azeroth.game.map.InstanceLockManager;
import com.github.azeroth.game.map.MMapManager;
import com.github.azeroth.game.map.MapManager;
import com.github.azeroth.game.map.TerrainManager;
import com.github.azeroth.game.map.collision.VMapManager;
import com.github.azeroth.game.pools.PoolManager;
import com.github.azeroth.game.pvp.OutdoorPvpManager;
import com.github.azeroth.game.spell.SpellManager;
import com.github.azeroth.game.world.setting.WorldSetting;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public interface WorldContext {

    Locale getDbcLocale();

    WorldSetting getWorldSettings();

    int getSetting(String name, int defaultValue);


    GameTableManager getGameTableManager();
    DbcObjectManager getDbcObjectManager();
    ObjectManager getObjectManager();
    ConditionManager getConditionManager();
    SpellManager getSpellManager();
    TerrainManager getTerrainManager();
    VMapManager getVMapManager();
    InstanceLockManager getInstanceLockManager();
    BattleFieldManager getBattleFieldManager();
    OutdoorPvpManager getOutdoorPvpManager();
    DisableManager getDisableManager();
    MMapManager getMMapManager();
    WorldEventPublisher getWorldEventPublisher();
    ScheduledThreadPoolExecutor getScheduledExecutor();
    LfgManager getLfgManager();
    LanguageManager getLanguageManager();

    ExecutorService getTaskExecutor();
    MapManager getMapManager();
    PoolManager getPoolManager();
    TransportManager getTransportManager();
    WorldStateManager getWorldStateManager();
    OutdoorPvpManager getOutDoorPvpManager();

    // these functions return objects only if in map of specified object
    WorldObject getWorldObject(WorldObject source, ObjectGuid guid);

    GenericObject getObjectByTypeMask(WorldObject source, ObjectGuid guid, TypeMask mask);

    Corpse getCorpse(WorldObject source, ObjectGuid guid);

    GameObject getGameObject(WorldObject source, ObjectGuid guid);

    Transport getTransport(WorldObject source, ObjectGuid guid);

    DynamicObject getDynamicObject(WorldObject source, ObjectGuid guid);

    AreaTrigger getAreaTrigger(WorldObject source, ObjectGuid guid);

    SceneObject getSceneObject(WorldObject source, ObjectGuid guid);

    Conversation getConversation(WorldObject source, ObjectGuid guid);

    Unit getUnit(WorldObject source, ObjectGuid guid);

    Creature getCreature(WorldObject source, ObjectGuid guid);

    Pet getPet(WorldObject source, ObjectGuid guid);

    Player getPlayer(WorldObject source, ObjectGuid guid);

    Creature getCreatureOrPetOrVehicle(WorldObject source, ObjectGuid guid);

    // these functions return objects if found in whole world
    // ACCESS LIKE THAT IS NOT THREAD SAFE
    Player findPlayer(ObjectGuid guid);

    Player findPlayerByName(String name);

    Player findPlayerByLowGUID(int entry);

    // this returns Player even if he is not in world, for example teleporting
    Player findConnectedPlayer(ObjectGuid guid);

    Player findConnectedPlayerByName(String name);

    // when using this, you must use the hashmapholder's lock
    Iterator<Player> getPlayers();

}
