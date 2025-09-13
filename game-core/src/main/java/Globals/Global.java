package Globals;

import com.github.azeroth.game.account.AccountManager;
import com.github.azeroth.game.account.BNetAccountManager;
import com.github.azeroth.game.achievement.AchievementGlobalMgr;
import com.github.azeroth.game.achievement.CriteriaManager;
import com.github.azeroth.game.ai.SmartAIManager;
import com.github.azeroth.game.arena.ArenaTeamManager;
import com.github.azeroth.game.battlefield.BattleFieldManager;
import com.github.azeroth.game.battleground.BattlegroundManager;
import com.github.azeroth.game.blackmarket.BlackMarketManager;
import com.github.azeroth.game.cache.CharacterCache;
import com.github.azeroth.game.chat.LanguageManager;
import com.github.azeroth.game.domain.transport.TransportManager;
import com.github.azeroth.game.dungeonfinding.LFGManager;
import com.github.azeroth.game.entity.player.PetitionManager;
import com.github.azeroth.game.entity.player.SocialManager;
import com.github.azeroth.game.garrison.GarrisonManager;
import com.github.azeroth.game.globals.ObjectManager;
import com.github.azeroth.game.group.GroupManager;
import com.github.azeroth.game.loot.LootItemStorage;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.collision.VMapManager;
import com.github.azeroth.game.pvp.OutdoorPvpManager;
import com.github.azeroth.game.scenario.ScenarioManager;
import com.github.azeroth.game.listener.ScriptManager;
import com.github.azeroth.game.service.WorldServiceManager;
import com.github.azeroth.game.supportsystem.SupportManager;
import game.*;


public final class Global {
    //Main
    public static ObjectAccessor getObjAccessor() {
        return ObjectAccessor.getInstance();
    }

    public static ObjectManager getObjectMgr() {
        return ObjectManager.getInstance();
    }

    public static WorldManager getWorldMgr() {
        return WorldManager.getInstance();
    }

    public static RealmManager getRealmMgr() {
        return RealmManager.Instance;
    }

    public static WorldServiceManager getServiceMgr() {
        return WorldServiceManager.getInstance();
    }

    //Guild
    public static PetitionManager getPetitionMgr() {
        return PetitionManager.getInstance();
    }

    public static GuildManager getGuildMgr() {
        return GuildManager.getInstance();
    }

    //Social
    public static CalendarManager getCalendarMgr() {
        return CalendarManager.getInstance();
    }

    public static SocialManager getSocialMgr() {
        return SocialManager.getInstance();
    }

    public static WhoListStorageManager getWhoListStorageMgr() {
        return WhoListStorageManager.getInstance();
    }

    //Scripts
    public static ScriptManager getScriptMgr() {
        return ScriptManager.getInstance();
    }

    public static SmartAIManager getSmartAIMgr() {
        return SmartAIManager.getInstance();
    }

    //Groups
    public static GroupManager getGroupMgr() {
        return GroupManager.getInstance();
    }

    public static LFGManager getLFGMgr() {
        return LFGManager.getInstance();
    }

    public static ArenaTeamManager getArenaTeamMgr() {
        return ArenaTeamManager.getInstance();
    }

    //Maps System
    public static TerrainManager getTerrainMgr() {
        return TerrainManager.getInstance();
    }

    public static MapManager getMapMgr() {
        return MapManager.getInstance();
    }

    public static MMapManager getMMapMgr() {
        return MMapManager.getInstance();
    }

    public static VMapManager getVMapMgr() {
        return VMapManager.getInstance();
    }

    public static WaypointManager getWaypointMgr() {
        return WaypointManager.getInstance();
    }

    public static TransportManager getTransportMgr() {
        return TransportManager.getInstance();
    }

    public static InstanceLockManager getInstanceLockMgr() {
        return InstanceLockManager.getInstance();
    }

    public static ScenarioManager getScenarioMgr() {
        return ScenarioManager.getInstance();
    }

    //PVP
    public static BattlegroundManager getBattlegroundMgr() {
        return BattlegroundManager.getInstance();
    }

    public static OutdoorPvpManager getOutdoorPvPMgr() {
        return OutdoorPvpManager.getInstance();
    }

    public static BattleFieldManager getBattleFieldMgr() {
        return BattleFieldManager.getInstance();
    }

    //Account
    public static AccountManager getAccountMgr() {
        return AccountManager.getInstance();
    }

    public static BNetAccountManager getBNetAccountMgr() {
        return BNetAccountManager.getInstance();
    }

    //Garrison
    public static GarrisonManager getGarrisonMgr() {
        return GarrisonManager.getInstance();
    }

    //Achievement
    public static AchievementGlobalMgr getAchievementMgr() {
        return AchievementGlobalMgr.getInstance();
    }

    public static CriteriaManager getCriteriaMgr() {
        return CriteriaManager.getInstance();
    }

    //DataStorage
    public static AreaTriggerDataStorage getAreaTriggerDataStorage() {
        return getAreaTriggerDataStorage().Instance;
    }

    public static CharacterTemplateDataStorage getCharacterTemplateDataStorage() {
        return getCharacterTemplateDataStorage().Instance;
    }

    public static ConversationDataStorage getConversationDataStorage() {
        return getConversationDataStorage().Instance;
    }

    public static CharacterCache getCharacterCacheStorage() {
        return CharacterCache.getInstance();
    }

    public static LootItemStorage getLootItemStorage() {
        return getLootItemStorage().Instance;
    }

    //Misc
    public static ConditionManager getConditionMgr() {
        return ConditionManager.getInstance();
    }

    public static DB2Manager getDB2Mgr() {
        return DB2Manager.getInstance();
    }

    public static DisableManager getDisableMgr() {
        return DisableManager.getInstance();
    }

    public static PoolManager getPoolMgr() {
        return PoolManager.getInstance();
    }

    public static QuestPoolManager getQuestPoolMgr() {
        return QuestPoolManager.getInstance();
    }

    public static WeatherManager getWeatherMgr() {
        return WeatherManager.getInstance();
    }

    public static GameEventManager getGameEventMgr() {
        return GameEventManager.getInstance();
    }

    public static LanguageManager getLanguageMgr() {
        return LanguageManager.getInstance();
    }

    public static CreatureTextManager getCreatureTextMgr() {
        return CreatureTextManager.getInstance();
    }

    public static AuctionManager getAuctionHouseMgr() {
        return AuctionManager.getInstance();
    }

    public static SpellManager getSpellMgr() {
        return SpellManager.getInstance();
    }

    public static SupportManager getSupportMgr() {
        return SupportManager.getInstance();
    }

    public static WardenCheckManager getWardenCheckMgr() {
        return WardenCheckManager.getInstance();
    }

    public static BlackMarketManager getBlackMarketMgr() {
        return BlackMarketManager.getInstance();
    }

    public static WorldStateManager getWorldStateMgr() {
        return WorldStateManager.getInstance();
    }
}
