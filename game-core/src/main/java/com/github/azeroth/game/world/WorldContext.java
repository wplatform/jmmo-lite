package com.github.azeroth.game.world;

import com.github.azeroth.common.Locale;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.GameTableManager;
import com.github.azeroth.game.ai.*;
import com.github.azeroth.game.battlefield.BattleFieldManager;
import com.github.azeroth.game.chat.LanguageManager;
import com.github.azeroth.game.condition.PlayerConditions;
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
import com.github.azeroth.game.movement.waypoint.WayPointManager;
import com.github.azeroth.game.pools.PoolManager;
import com.github.azeroth.game.pvp.OutdoorPvpManager;
import com.github.azeroth.game.script.ScriptManager;
import com.github.azeroth.game.spell.SpellManager;
import com.github.azeroth.game.world.setting.WorldSetting;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public interface WorldContext {

    default Locale getDbcLocale() {
        return getWorldSettings().dbcLocale;
    }

    default WorldSetting getWorldSettings() {
        return getBean(WorldSetting.class);
    }

    default GameTableManager getGameTableManager() {
        return getBean(GameTableManager.class);
    }

    default DbcObjectManager getDbcObjectManager() {
        return getBean(DbcObjectManager.class);
    }

    default ObjectManager getObjectManager() {
        return getBean(ObjectManager.class);
    }

    default PlayerConditions getConditionManager() {
        return getBean(PlayerConditions.class);
    }

    default SpellManager getSpellManager() {
        return getBean(SpellManager.class);
    }

    default TerrainManager getTerrainManager() {
        return getBean(TerrainManager.class);
    }

    default VMapManager getVMapManager() {
        return getBean(VMapManager.class);
    }

    default InstanceLockManager getInstanceLockManager() {
        return getBean(InstanceLockManager.class);
    }

    default BattleFieldManager getBattleFieldManager() {
        return getBean(BattleFieldManager.class);
    }

    default OutdoorPvpManager getOutdoorPvpManager() {
        return getBean(OutdoorPvpManager.class);
    }

    default DisableManager getDisableManager() {
        return getBean(DisableManager.class);
    }

    default MMapManager getMMapManager() {
        return getBean(MMapManager.class);
    }

    default WorldEventPublisher getWorldEventPublisher() {
        return getBean(WorldEventPublisher.class);
    }

    default ScheduledThreadPoolExecutor getScheduledExecutor() {
        return getBean(ScheduledThreadPoolExecutor.class);
    }

    default LfgManager getLfgManager() {
        return getBean(LfgManager.class);
    }

    default LanguageManager getLanguageManager() {
        return getBean(LanguageManager.class);
    }

    ExecutorService getTaskExecutor();

    default MapManager getMapManager() {
        return getBean(MapManager.class);
    }

    default PoolManager getPoolManager() {
        return getBean(PoolManager.class);
    }

    default TransportManager getTransportManager() {
        return getBean(TransportManager.class);
    }

    default WorldStateManager getWorldStateManager() {
        return getBean(WorldStateManager.class);
    }

    default OutdoorPvpManager getOutDoorPvpManager() {
        return getBean(OutdoorPvpManager.class);
    }

    default WayPointManager getWayPointManager() {
        return getBean(WayPointManager.class);
    }

    default ObjectAiFactory getObjectAiFactory() {
        return getBean(ObjectAiFactory.class);
    }

    default ScriptManager getScriptManager() {
        return getBean(ScriptManager.class);
    }


    // these functions return objects only if in map of specified object
    default WorldObject getWorldObject(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default GenericObject getObjectByTypeMask(WorldObject source, ObjectGuid guid, TypeMask mask) {
        return null;
    }

    default Corpse getCorpse(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default GameObject getGameObject(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Transport getTransport(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default DynamicObject getDynamicObject(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default AreaTrigger getAreaTrigger(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default SceneObject getSceneObject(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Conversation getConversation(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Unit getUnit(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Creature getCreature(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Pet getPet(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Player getPlayer(WorldObject source, ObjectGuid guid) {
        return null;
    }

    default Creature getCreatureOrPetOrVehicle(WorldObject source, ObjectGuid guid) {
        return null;
    }

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


    <T> T getBean(Class<T> beanClass);

}
