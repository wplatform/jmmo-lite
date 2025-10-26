package com.github.azeroth.game.globals;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.github.azeroth.cache.CacheProvider;
import com.github.azeroth.cache.MapCache;
import com.github.azeroth.cache.TypeReference;
import com.github.azeroth.common.Locale;
import com.github.azeroth.common.*;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.defines.ItemSpecStat;
import com.github.azeroth.dbc.defines.PhaseUseFlag;
import com.github.azeroth.dbc.defines.TaxiNodeFlag;
import com.github.azeroth.dbc.domain.*;
import com.github.azeroth.dbc.domain.GameObjectEntry;
import com.github.azeroth.dbc.gtable.GameTable;
import com.github.azeroth.defines.QuestSort;
import com.github.azeroth.defines.SpellCategory;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.*;
import com.github.azeroth.game.condition.*;
import com.github.azeroth.game.domain.areatrigger.*;
import com.github.azeroth.game.domain.condition.ConditionSourceType;
import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.domain.creature.*;
import com.github.azeroth.game.domain.gobject.*;
import com.github.azeroth.game.domain.gossip.GossipMenuOption;
import com.github.azeroth.game.domain.gossip.GossipMenus;
import com.github.azeroth.game.domain.gossip.GossipOptionNpc;
import com.github.azeroth.game.domain.instance.EncounterState;
import com.github.azeroth.game.domain.misc.RaceUnlockRequirement;
import com.github.azeroth.game.domain.misc.*;
import com.github.azeroth.game.domain.object.ObjectDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.domain.phasing.TerrainSwapInfo;
import com.github.azeroth.game.domain.player.*;
import com.github.azeroth.game.domain.quest.*;
import com.github.azeroth.game.domain.reputation.RepRewardRate;
import com.github.azeroth.game.domain.reputation.RepSpilloverTemplate;
import com.github.azeroth.game.domain.reputation.ReputationOnKill;
import com.github.azeroth.game.domain.scene.SceneTemplate;
import com.github.azeroth.game.domain.script.ScriptCommand;
import com.github.azeroth.game.domain.script.ScriptInfo;
import com.github.azeroth.game.domain.script.ScriptsType;
import com.github.azeroth.game.domain.spawn.*;
import com.github.azeroth.game.domain.unit.*;
import com.github.azeroth.game.domain.vehicle.VehicleAccessory;
import com.github.azeroth.game.domain.vehicle.VehicleSeatAddon;
import com.github.azeroth.game.domain.vehicle.VehicleTemplate;
import com.github.azeroth.game.entity.creature.Trainer;
import com.github.azeroth.game.domain.creature.TrainerSpell;
import com.github.azeroth.game.entity.item.ItemSpecStats;
import com.github.azeroth.game.entity.item.ItemTemplate;
import com.github.azeroth.game.entity.item.enums.InventoryType;
import com.github.azeroth.game.entity.item.enums.ItemClass;
import com.github.azeroth.game.entity.item.enums.ItemFlagsCustom;
import com.github.azeroth.game.entity.item.enums.ItemSubclassConsumable;
import com.github.azeroth.game.entity.object.*;
import com.github.azeroth.game.domain.object.enums.HighGuid;
import com.github.azeroth.game.domain.object.enums.SummonerType;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.player.AccessRequirement;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.vehicle.Vehicle;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.enums.LoadResult;
import com.github.azeroth.game.domain.map.Coordinate;
import com.github.azeroth.game.domain.map.ZoneAndAreaId;
import com.github.azeroth.game.movement.MotionMaster;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.game.phasing.PhasingHandler;
import com.github.azeroth.game.quest.Quest;
import com.github.azeroth.game.repository.*;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.SpellManager;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.world.World;
import com.github.azeroth.game.world.setting.WorldSetting;
import com.github.azeroth.utils.MathUtil;
import com.github.azeroth.utils.RandomUtil;
import com.github.azeroth.utils.StringUtil;
import com.github.azeroth.utils.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.azeroth.game.domain.unit.UnitDefine.BASE_ATTACK_TIME;

/**
 * sObjectMgr->LoadTrinityStrings())
 * sObjectMgr->LoadInstanceTemplate();
 * sObjectMgr->LoadCreatureLocales();
 * sObjectMgr->LoadGameObjectLocales();
 * sObjectMgr->LoadQuestTemplateLocale();
 * sObjectMgr->LoadQuestOfferRewardLocale();
 * sObjectMgr->LoadQuestRequestItemsLocale();
 * sObjectMgr->LoadQuestObjectivesLocale();
 * sObjectMgr->LoadPageTextLocales();
 * sObjectMgr->LoadGossipMenuItemsLocales();
 * sObjectMgr->LoadPointOfInterestLocales();
 * sObjectMgr->LoadPageTexts();
 * sObjectMgr->LoadDestructibleHitpoints();
 * sObjectMgr->LoadGameObjectTemplate();
 * sObjectMgr->LoadGameObjectTemplateAddons();
 * sObjectMgr->LoadNPCText();
 * sObjectMgr->LoadItemTemplates();
 * sObjectMgr->LoadItemTemplateAddon();
 * sObjectMgr->LoadItemScriptNames();
 * sObjectMgr->LoadCreatureModelInfo();
 * sObjectMgr->LoadCreatureTemplates();
 * sObjectMgr->LoadEquipmentTemplates();
 * sObjectMgr->LoadCreatureTemplateAddons();
 * sObjectMgr->LoadCreatureTemplateDifficulty();
 * sObjectMgr->LoadCreatureTemplateSparring();
 * sObjectMgr->LoadReputationRewardRate();
 * sObjectMgr->LoadReputationOnKill();
 * sObjectMgr->LoadReputationSpilloverTemplate();
 * sObjectMgr->LoadPointsOfInterest();
 * sObjectMgr->LoadCreatureClassLevelStats();
 * sObjectMgr->LoadSpawnGroupTemplates();
 * sObjectMgr->LoadCreatures();
 * sObjectMgr->LoadTempSummons();                               // must be after LoadCreatureTemplates() and LoadGameObjectTemplates()
 * sObjectMgr->LoadCreatureAddons();                            // must be after LoadCreatureTemplates() and LoadCreatures()
 * sObjectMgr->LoadCreatureMovementOverrides();                 // must be after LoadCreatures()
 * sObjectMgr->LoadGameObjects();
 * sObjectMgr->LoadSpawnGroups();
 * sObjectMgr->LoadInstanceSpawnGroups();
 * sObjectMgr->LoadGameObjectAddons();                          // must be after LoadGameObjects()
 * sObjectMgr->LoadGameObjectOverrides();                       // must be after LoadGameObjects()
 * sObjectMgr->LoadGameObjectQuestItems();
 * sObjectMgr->LoadCreatureQuestItems();
 * sObjectMgr->LoadCreatureQuestCurrencies();
 * sObjectMgr->LoadLinkedRespawn();                             // must be after LoadCreatures(), LoadGameObjects()
 * sObjectMgr->LoadQuests();                                    // must be loaded after DBCs, creature_template, items, gameobject tables
 * sObjectMgr->LoadQuestPOI();
 * sObjectMgr->LoadQuestStartersAndEnders();                    // must be after quest load
 * sObjectMgr->LoadQuestGreetings();
 * sObjectMgr->LoadQuestGreetingLocales();
 * sObjectMgr->LoadCreatureSummonedData();                     // must be after LoadCreatureTemplates() and LoadQuests()
 * sObjectMgr->LoadNPCSpellClickSpells();
 * sObjectMgr->LoadVehicleTemplate();                          // must be after LoadCreatureTemplates()
 * sObjectMgr->LoadVehicleTemplateAccessories();                // must be after LoadCreatureTemplates() and LoadNPCSpellClickSpells()
 * sObjectMgr->LoadVehicleAccessories();                       // must be after LoadCreatureTemplates() and LoadNPCSpellClickSpells()
 * sObjectMgr->LoadVehicleSeatAddon();                         // must be after loading DBC
 * sObjectMgr->LoadWorldSafeLocs();                            // must be before LoadAreaTriggerTeleports and LoadGraveyardZones
 * sObjectMgr->LoadAreaTriggerTeleports();
 * sObjectMgr->LoadAreaTriggerPolygons();
 * sObjectMgr->LoadAccessRequirements();                        // must be after item template load
 * sObjectMgr->LoadQuestAreaTriggers();                         // must be after LoadQuests
 * sObjectMgr->LoadTavernAreaTriggers();
 * sObjectMgr->LoadAreaTriggerScripts();
 * sObjectMgr->LoadGraveyardZones();
 * sObjectMgr->LoadSceneTemplates();
 * sObjectMgr->LoadPlayerInfo();
 * sObjectMgr->LoadExplorationBaseXP();
 * sObjectMgr->LoadPetNames();
 * sObjectMgr->LoadPlayerChoices();
 * sObjectMgr->LoadPlayerChoicesLocale();
 * sObjectMgr->LoadUiMapQuestLines();
 * sObjectMgr->LoadUiMapQuests();
 * sObjectMgr->LoadJumpChargeParams();
 * sObjectMgr->LoadPetNumber();
 * sObjectMgr->LoadPetLevelInfo();
 * sObjectMgr->LoadMailLevelRewards();
 * sObjectMgr->LoadFishingBaseSkillLevel();
 * sObjectMgr->LoadSkillTiers();
 * sObjectMgr->LoadReservedPlayersNames();
 * sObjectMgr->LoadGameObjectForQuests();
 * sObjectMgr->LoadGameTele();
 * sObjectMgr->LoadTrainers();                                 // must be after load CreatureTemplate
 * sObjectMgr->LoadGossipMenu();
 * sObjectMgr->LoadGossipMenuItems();
 * sObjectMgr->LoadGossipMenuAddon();
 * sObjectMgr->LoadCreatureTemplateGossip();
 * sObjectMgr->LoadCreatureTrainers();                         // must be after LoadGossipMenuItems
 * sObjectMgr->LoadVendors();                                  // must be after load CreatureTemplate and ItemTemplate
 * sObjectMgr->LoadPhases();
 * sObjectMgr->LoadFactionChangeAchievements();
 * sObjectMgr->LoadFactionChangeSpells();
 * sObjectMgr->LoadFactionChangeQuests();
 * sObjectMgr->LoadFactionChangeItems();
 * sObjectMgr->LoadFactionChangeReputations();
 * sObjectMgr->LoadFactionChangeTitles();
 * sObjectMgr->LoadSpellScripts();                              // must be after load Creature/Gameobject(Template/Data)
 * sObjectMgr->LoadEventScripts();                              // must be after load Creature/Gameobject(Template/Data)
 * sObjectMgr->LoadSpellScriptNames();
 * sObjectMgr->LoadCreatureStaticFlagsOverride(); // must be after LoadCreatures
 * sObjectMgr->LoadRaceAndClassExpansionRequirements();
 * sObjectMgr->LoadPhaseNames();
 */
public final class ObjectManager {
    private static final float[] qualityMultipliers = new float[]{0.92f, 0.92f, 0.92f, 1.11f, 1.32f, 1.61f, 0.0f, 0.0f};
    private static final float[] armorMultipliers = new float[]{0.00f, 0.60f, 0.00f, 0.60f, 0.00f, 1.00f, 0.33f, 0.72f, 0.48f, 0.33f, 0.33f, 0.00f, 0.00f, 0.00f, 0.72f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 1.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f};
    private static final float[] weaponMultipliers = new float[]{0.91f, 1.00f, 1.00f, 1.00f, 0.91f, 1.00f, 1.00f, 0.91f, 1.00f, 1.00f, 1.00f, 0.00f, 0.00f, 0.66f, 0.00f, 0.66f, 0.00f, 0.00f, 1.00f, 0.66f, 0.66f};
    //Maps
    public final MapCache<Integer, GameTele> gameTeleStorage;
    //General
    private final MapCache<Integer, LocalizedString> messageTextStorage;
    private final MapCache<Integer, RepRewardRate> repRewardRateStorage;
    private final MapCache<Integer, ReputationOnKill> repOnKillStorage;
    private final MapCache<Integer, RepSpilloverTemplate> repSpilloverTemplateStorage;
    private final MapCache<Byte, List<MailLevelReward>> mailLevelRewardStorage;
    private final MapCache<Tuple<Integer, SummonerType, Short>, List<TempSummonData>> tempSummonDataStorage;
    private final MapCache<Integer, PlayerChoice> playerChoices;
    private final MapCache<Integer, PageText> pageTextStorage;
    private final HashMap<Integer, SceneTemplate> sceneTemplateStorage = new HashMap<Integer, SceneTemplate>();
    private final IntMap<JumpChargeParams> jumpChargeParams = new IntMap<>();
    private final MapCache<Integer, String> phaseNameStorage;
    private final EnumMap<Race, RaceUnlockRequirement> raceUnlockRequirementStorage = new EnumMap<>(Race.class);
    private final ArrayList<RaceClassAvailability> classExpansionRequirementStorage = new ArrayList<>();
    private final HashMap<Integer, String> realmNameStorage = new HashMap<>(2);
    //Quest
    private final MapCache<Integer, QuestTemplate> questTemplates;
    private final ArrayList<QuestTemplate> questTemplatesAutoPush = new ArrayList<>();
    private final MapCache<Integer, List<Integer>> goQuestRelations;
    private final MapCache<Integer, List<Integer>> goQuestInvolvedRelations;
    private final MapCache<Integer, List<Integer>> goQuestInvolvedRelationsReverse;
    private final MapCache<Integer, List<Integer>> creatureQuestRelations;
    private final MapCache<Integer, List<Integer>> creatureQuestInvolvedRelations;
    private final MapCache<Integer, List<Integer>> creatureQuestInvolvedRelationsReverse;
    private final Map<Integer, List<Integer>> exclusiveQuestGroups = new HashMap<>();
    private final MapCache<Integer, QuestPOIData> questPOIStorage;
    private final HashMap<Integer,Set<Integer>> questAreaTriggerStorage = new HashMap<>();
    private final HashMap<Integer, QuestObjective> questObjectives = new HashMap<Integer, QuestObjective>();
    private final MapCache<Integer, QuestGreeting> questGreetingStorage;
    //Scripts
    private final ScriptNameContainer scriptNamesStorage = new ScriptNameContainer();
    private final HashMap<Integer, List<Pair<Integer,Boolean>>> spellScriptsStorage = new HashMap<>();
    private final HashMap<Integer, Integer> areaTriggerScriptStorage = new HashMap<>();
    private final MapCache<Tuple<Integer,Difficulty, Integer>, GridSpawnData> mapGridSpawnDataStorage;
    private final MapCache<Pair<Integer,Difficulty>, GridSpawnData> transportMapSpawnDataStorage;
    private final MapCache<PersonalCellSpawnDataKey, GridSpawnData> mapPersonalSpawnDataStorage;
    private final HashMap<Integer, InstanceTemplate> instanceTemplateStorage = new HashMap<>();
    private final ArrayList<Short> transportMaps = new ArrayList<>();
    private final MapCache<Integer, SpawnGroupTemplateData> spawnGroupDataStorage;
    private final Map<Integer, List<SpawnMetadata>> spawnGroupMapStorage = new HashMap<>();
    private final Map<Integer, List<Integer>> spawnGroupsByMap = new HashMap<>();
    private final Map<Integer, List<InstanceSpawnGroup>> instanceSpawnGroupStorage = new HashMap<>();
    //Spells /Skills / Phases
    private final HashMap<Integer, PhaseInfoStruct> phaseInfoById = new HashMap<Integer, PhaseInfoStruct>();
    private final HashMap<Integer, TerrainSwapInfo> terrainSwapInfoById = new HashMap<Integer, TerrainSwapInfo>();
    private final Map<Integer, List<PhaseAreaInfo>> phaseInfoByArea = new HashMap<>();
    private final Map<Integer, List<TerrainSwapInfo>> terrainSwapInfoByMap = new HashMap<>();
    private final Map<Integer, List<SpellClickInfo>> spellClickInfoStorage = new HashMap<>();
    private final HashMap<Integer, Integer> fishingBaseForArea = new HashMap<Integer, Integer>();
    private final HashMap<Integer, SkillTiersEntry> skillTiers = new HashMap<Integer, SkillTiersEntry>();
    //Gossip
    private final MapCache<Integer, List<GossipMenus>> gossipMenusStorage;
    private final MapCache<Integer, List<GossipMenuOption>> gossipMenuItemsStorage;
    private final MapCache<Integer, GossipMenuAddon> gossipMenuAddonStorage;
    private final MapCache<Integer, PointOfInterest> pointsOfInterestStorage;
    //Creature
    private final MapCache<Integer, CreatureTemplate> creatureTemplateStorage;
    private final HashMap<Integer, CreatureModelInfo> creatureModelStorage = new HashMap<Integer, CreatureModelInfo>();
    private final MapCache<Integer, CreatureData> creatureDataStorage;
    private final MapCache<Pair<Integer, Difficulty>, List<Integer>> creatureQuestItemStorage;
    private final HashMap<Integer, CreatureMovementData> creatureMovementOverrides = new HashMap<>();
    private final MapCache<Integer, Map<Byte, EquipmentInfo>> equipmentInfoStorage;
    private final HashMap<ObjectGuid, ObjectGuid> linkedRespawnStorage = new HashMap<>();
    private final HashMap<Integer, CreatureBaseStats> creatureBaseStatsStorage = new HashMap<Integer, CreatureBaseStats>();
    private final HashMap<Integer, FormationInfo> creatureGroupMap = new HashMap<>();
    private final MapCache<Integer, VendorItemData> cacheVendorItemStorage;
    private final HashMap<Integer, Trainer> trainers = new HashMap<>();
    private final MapCache<Integer, NpcText> npcTextStorage;
    //GameObject
    private final HashMap<Integer, GameObjectTemplate> gameObjectTemplateStorage = new HashMap<Integer, GameObjectTemplate>();
    private final HashMap<Integer, GameObjectData> gameObjectDataStorage = new HashMap<>();
    private final MapCache<Integer, GameObjectTemplateAddon> gameObjectTemplateAddonStorage;
    private final MapCache<Integer, GameObjectOverride> gameObjectOverrideStorage;
    private final HashMap<Integer, GameObjectAddon> gameObjectAddonStorage = new HashMap<>();
    private final Map<Integer, List<Integer>> gameObjectQuestItemStorage = new HashMap<>();
    private final ArrayList<Integer> gameObjectForQuestStorage = new ArrayList<>();
    //Item
    private final HashMap<Integer, ItemTemplate> itemTemplateStorage = new HashMap<Integer, ItemTemplate>();
    //Player
    private final HashMap<Pair<Race, UnitClass>, PlayerInfo> playerInfo = new HashMap<>();
    //Pets
    private final MapCache<Integer, PetLevelInfo[]> petInfoStore;
    private final HashMap<Integer, List<String>> petHalfName0 = new HashMap<>();
    private final HashMap<Integer, List<String>> petHalfName1 = new HashMap<>();
    //Vehicles
    private final HashMap<Integer, VehicleTemplate> vehicleTemplateStore = new HashMap<>();
    private final HashMap<Integer, List<VehicleAccessory>> vehicleTemplateAccessoryStore = new HashMap<>();
    private final HashMap<Integer, List<VehicleAccessory>> vehicleAccessoryStore = new HashMap<>();
    private final HashMap<Integer, VehicleSeatAddon> vehicleSeatAddonStore = new HashMap<>();
    //Locales
    private final HashMap<Integer, GameObjectLocale> gameObjectLocaleStorage = new HashMap<Integer, GameObjectLocale>();

    private final Map<Tuple<Integer, Integer, Integer>, Integer> creatureDefaultTrainers = new HashMap<>();
    private final Set<Integer> tavernAreaTriggerStorage = new HashSet<>();

    private final HashMap<AreaTriggerId, AreaTriggerTemplate> areaTriggerTemplatesStorage = new HashMap<>();
    private final HashMap<Integer, AreaTriggerStruct> areaTriggerStorage = new HashMap<>();
    private final HashMap<Integer, AccessRequirement> accessRequirementStorage = new HashMap<>();

    private final Set<Integer> eventStorage = new HashSet<>();
    private final int[] baseXPTable = new int[SharedDefine.MAX_LEVEL];
    public HashMap<Integer, Map<Integer, ScriptInfo>> spellScripts = new HashMap<>();
    public HashMap<Integer, Map<Integer, ScriptInfo>> eventScripts = new HashMap<>();
    public HashMap<Integer, List<GraveYardData>> graveYardStorage = new HashMap<>();
    private final HashMap<Integer, WorldSafeLocEntry> worldSafeLocs = new HashMap<>();
    //Faction Change
    public HashMap<Integer, Integer> factionChangeAchievements = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> factionChangeItemsAllianceToHorde = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> factionChangeItemsHordeToAlliance = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> factionChangeQuests = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> factionChangeReputation = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> factionChangeSpells = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> factionChangeTitles = new HashMap<Integer, Integer>();

    private final HashMap<Integer, AreaTriggerSpawn> areaTriggerSpawnsBySpawnId = new HashMap<>();
    private final HashMap<AreaTriggerId, AreaTriggerTemplate> areaTriggerTemplateStore = new HashMap<>();
    private final HashMap<AreaTriggerId, AreaTriggerCreateProperty> areaTriggerCreateProperties = new HashMap<>();



    private World world;
    private CacheProvider cacheProvider;
    private DbcObjectManager dbcObjectManager;


    private WorldCrudRepository worldCrudRepo;
    private MiscRepository miscRepo;
    private CreatureRepository creatureRepo;
    private GameObjectRepository gameObjectRepo;
    private ReputationRepository reputationRepo;
    private AreaTriggerRepository areaTriggerRepo;
    private ConditionManager conditionManager;
    private QuestRepository questRepo;
    private ItemRepository itemRepo;
    private SpellManager spellManager;
    private DisableManager disableManager;
    private PlayerRepository playerRepo;
    private VehicleRepository vehicleRepo;
    // first free id for selected id type
    private int auctionId;
    private long equipmentSetGuid;
    private long mailId;
    private long creatureSpawnId;
    private long gameObjectSpawnId;
    private long voidItemId;
    private int[] playerXPperLevel;

    private ObjectManager() {
        this.messageTextStorage = cacheProvider.newGenericMapCache("LocalizedStringStorage", Integer.class, LocalizedString.class);
        this.gameTeleStorage = cacheProvider.newGenericMapCache("GameTeleportStorage", Integer.class, GameTele.class);
        this.tempSummonDataStorage = cacheProvider.newGenericMapCache("TempSummonDataStorage", new TypeReference<>() {});
        this.pageTextStorage = cacheProvider.newGenericMapCache("PageTextStorage", new TypeReference<>() {});
        this.mailLevelRewardStorage = cacheProvider.newGenericMapCache("MailLevelRewardStorage", new TypeReference<>() {});
        this.repOnKillStorage = cacheProvider.newGenericMapCache("RepOnKillStorage", new TypeReference<>() {});
        this.repRewardRateStorage = cacheProvider.newGenericMapCache("RepRewardRateStorage", new TypeReference<>() {});
        this.repSpilloverTemplateStorage = cacheProvider.newGenericMapCache("RepSpilloverTemplateStorage", new TypeReference<>() {});
        this.playerChoices = cacheProvider.newGenericMapCache("PlayerChoicesStorage", new TypeReference<>() {});
        this.gossipMenusStorage = cacheProvider.newGenericMapCache("GossipMenusStorage", new TypeReference<>() {});
        this.npcTextStorage = cacheProvider.newGenericMapCache("NpcTextStorage", new TypeReference<>() {});
        this.gossipMenuItemsStorage = cacheProvider.newGenericMapCache("GossipMenuItemsStorage", new TypeReference<>() {});
        this.gossipMenuAddonStorage = cacheProvider.newGenericMapCache("GossipMenuAddonStorage", new TypeReference<>() {});
        this.pointsOfInterestStorage = cacheProvider.newGenericMapCache("PointsOfInterestStorage", new TypeReference<>() {});
        this.questTemplates = cacheProvider.newGenericMapCache("QuestTemplateStorage", new TypeReference<>() {});
        this.goQuestRelations = cacheProvider.newGenericMapCache("GoQuestRelationsStorage", new TypeReference<>() {});
        this.goQuestInvolvedRelations = cacheProvider.newGenericMapCache("GoQuestInvolvedRelationsStorage", new TypeReference<>() {});
        this.goQuestInvolvedRelationsReverse = cacheProvider.newGenericMapCache("GoQuestInvolvedRelationsReverseStorage", new TypeReference<>() {});
        this.creatureQuestRelations = cacheProvider.newGenericMapCache("CreatureQuestRelationsStorage", new TypeReference<>() {});
        this.creatureQuestInvolvedRelations = cacheProvider.newGenericMapCache("CreatureQuestInvolvedRelationsStorage", new TypeReference<>() {});
        this.creatureQuestInvolvedRelationsReverse = cacheProvider.newGenericMapCache("CreatureQuestInvolvedRelationsReverseStorage", new TypeReference<>() {});
        this.questPOIStorage = cacheProvider.newGenericMapCache("QuestPOIStorageStorage", new TypeReference<>(){});
        this.questGreetingStorage = cacheProvider.newGenericMapCache("QuestGreetingStorage", new TypeReference<>(){});
        this.spawnGroupDataStorage = cacheProvider.newGenericMapCache("SpawnGroupDataStorage", new TypeReference<>(){});
        this.creatureTemplateStorage = cacheProvider.newGenericMapCache("CreatureTemplateStorage", new TypeReference<>(){});
        this.petInfoStore = cacheProvider.newGenericMapCache("PetInfoStorage", new TypeReference<>(){});
        this.phaseNameStorage = cacheProvider.newGenericMapCache("PhaseNameStorage", new TypeReference<>(){});
        this.mapGridSpawnDataStorage = cacheProvider.newGenericMapCache("MapGridSpawnDataStorage", new TypeReference<>(){});
        this.transportMapSpawnDataStorage = cacheProvider.newGenericMapCache("TransportMapSpawnDataStorage", new TypeReference<>(){});
        this.mapPersonalSpawnDataStorage = cacheProvider.newGenericMapCache("MapPersonalSpawnDataStorage", new TypeReference<>(){});
        this.cacheVendorItemStorage = cacheProvider.newGenericMapCache("CacheVendorItemStorage", new TypeReference<>(){});
        this.equipmentInfoStorage = cacheProvider.newGenericMapCache("EquipmentInfoStorage", new TypeReference<>(){});
        this.creatureDataStorage = cacheProvider.newGenericMapCache("CreatureDataStorage", new TypeReference<>(){});
        this.creatureQuestItemStorage = cacheProvider.newGenericMapCache("CreatureQuestItemStorage", new TypeReference<>(){});
        this.gameObjectTemplateAddonStorage = cacheProvider.newGenericMapCache("GameObjectTemplateAddonStorage", new TypeReference<>(){});

        this.gameObjectOverrideStorage = cacheProvider.newGenericMapCache("GameObjectOverrideStorage", new TypeReference<>(){});

    }

    //Static Methods
    public static String normalizePlayerName(String name) {
        return null;
    }

    public static ExtendedPlayerName extractExtendedPlayerName(String name) {
        var pos = name.indexOf('-');

        if (pos != -1) {
            return new ExtendedPlayerName(name.substring(0, pos), name.substring(pos + 1));
        } else {
            return new ExtendedPlayerName(name, "");
        }
    }

    public static CreatureModel chooseDisplayId(CreatureTemplate cinfo) {
        return chooseDisplayId(cinfo, null);
    }

    public static CreatureModel chooseDisplayId(CreatureTemplate cinfo, CreatureData data) {
        // Load creature model (display id)
        if (data != null && data.display != null) {
            return data.display;
        }

        if (!cinfo.flagsExtra.hasFlag(CreatureFlagExtra.TRIGGER)) {
            var model = cinfo.getRandomValidModel();

            if (model != null) {
                return model;
            }
        }

        // Triggers by default receive the invisible model
        return cinfo.getFirstInvisibleModel();
    }
    //General
    public boolean loadMessageText() {
        var time = System.currentTimeMillis();
        messageTextStorage.clear();


        try (Stream<SystemText> trinityString = miscRepo.streamAllMessageText()) {
            trinityString.forEach(e -> {
                LocalizedString string = new LocalizedString();
                Locale[] values = Locale.values();
                string.set(values[0], e.contentLoc1);
                string.set(values[1], e.contentLoc2);
                string.set(values[2], e.contentLoc3);
                string.set(values[3], e.contentLoc4);
                string.set(values[4], e.contentLoc5);
                string.set(values[5], e.contentLoc6);
                string.set(values[6], e.contentLoc7);
                string.set(values[7], e.contentLoc8);
                messageTextStorage.put(e.id, string);
            });
        }
        if (messageTextStorage.isEmpty()) {

            Logs.SERVER_LOADING.info(">> Loaded 0 trinity strings. DB table `trinity_string` is empty. You have imported an incorrect database for more info search for TCE00003 on forum.");


            return false;
        } else {
            Logs.SERVER_LOADING.info(">> Loaded {} trinity strings in {} ms", messageTextStorage.size(), System.currentTimeMillis() - time);

        }

        int count = 0;

        return true;
    }

    public void loadRaceAndClassExpansionRequirements() {
        var oldMSTime = System.currentTimeMillis();
        raceUnlockRequirementStorage.clear();

        try (Stream<RaceUnlockRequirement> raceUnlockRequirements = miscRepo.queryAllRaceUnlockRequirement()) {
            raceUnlockRequirements.forEach(e -> {
                var chrClass = dbcObjectManager.chrRace(e.raceId);
                if (chrClass == null) {
                    Logs.SQL.error("Race {} defined in `race_unlock_requirement` does not exists, skipped.", e.raceId);
                    return;
                }

                if (e.expansion >= Expansion.MAX_EXPANSION) {
                    Logs.SQL.error("Race {} defined in `race_unlock_requirement` has incorrect expansion {}, skipped.", e.raceId, e.expansion);
                    return;
                }

                if (e.achievementId != 0 && dbcObjectManager.achievement(e.achievementId) == null) {
                    Logs.SQL.error("Race {} defined in `race_unlock_requirement` has incorrect achievement {}, skipped.", e.raceId, e.achievementId);
                    return;
                }

                raceUnlockRequirementStorage.put(Race.values()[e.raceId], e);
            });
        }
        if (!raceUnlockRequirementStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded {} race expansion requirements in {} ms.", raceUnlockRequirementStorage.size(), System.currentTimeMillis() - oldMSTime);
        } else {
            Logs.SERVER_LOADING.info(">> Loaded 0 race expansion requirements. DB table `race_expansion_requirement` is empty.");
        }


        oldMSTime = System.currentTimeMillis();
        classExpansionRequirementStorage.clear();

        HashMap<Byte, Map<Byte, Pair<Byte, Byte>>> temp = new HashMap<>();
        var minRequirementForClass = new byte[UnitClass.values().length];
        try (Stream<ClassExpansionRequirement> raceUnlockRequirements = miscRepo.queryAllClassExpansionRequirement()) {
            raceUnlockRequirements.forEach(e -> {
                ChrClass classEntry = dbcObjectManager.chrClass(e.classID);
                if (classEntry == null) {
                    Logs.SQL.error("Class {} (race {}) defined in `class_expansion_requirement` does not exists, skipped.",
                            e.classID, e.raceID);
                    return;
                }

                ChrRace raceEntry = dbcObjectManager.chrRace(e.raceID);
                if (raceEntry == null) {
                    Logs.SQL.error("Race {} (class {}) defined in `class_expansion_requirement` does not exists, skipped.",
                            e.raceID, e.classID);
                    return;
                }

                if (e.activeExpansionLevel >= Expansion.MAX_EXPANSION) {
                    Logs.SQL.error("Class {} Race {} defined in `class_expansion_requirement` has incorrect ActiveExpansionLevel {}, skipped.",
                            e.classID, e.raceID, e.activeExpansionLevel);
                    return;
                }

                if (e.accountExpansionLevel >= Expansion.MAX_ACCOUNT_EXPANSION) {
                    Logs.SQL.error("Class {} Race {} defined in `class_expansion_requirement` has incorrect AccountExpansionLevel {}, skipped.",
                            e.classID, e.raceID, e.accountExpansionLevel);
                    return;
                }

                temp.compute(e.raceID, Functions.addToMap(e.classID, Pair.of(e.activeExpansionLevel, e.accountExpansionLevel)));
                minRequirementForClass[e.classID] = (byte) Math.min(minRequirementForClass[e.classID], e.activeExpansionLevel);
            });

        }


        for (var race : temp.entrySet()) {
            RaceClassAvailability raceClassAvailability = new RaceClassAvailability();
            raceClassAvailability.raceID = Race.values()[race.getKey()];

            for (var pair : race.getValue().entrySet()) {
                ClassAvailability classAvailability = new ClassAvailability();
                classAvailability.classID = UnitClass.values()[pair.getKey()];
                classAvailability.activeExpansionLevel = pair.getValue().first();
                classAvailability.accountExpansionLevel = pair.getValue().second();
                classAvailability.minActiveExpansionLevel = minRequirementForClass[pair.getKey()];

                raceClassAvailability.classes.add(classAvailability);
            }

            classExpansionRequirementStorage.add(raceClassAvailability);
        }

        if (!temp.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded {} class expansion requirements in {} ms.", temp.size(), System.currentTimeMillis() - oldMSTime);
        } else {
            Logs.SERVER_LOADING.info(">> Loaded 0 class expansion requirements. DB table `class_expansion_requirement` is empty.");
        }
    }

    public String getMessageText(int entry) {
        return getMessageText(entry, Locale.enUS);
    }

    public String getMessageText(int entry, Locale locale) {

        LocalizedString localizedString = messageTextStorage.get(entry);
        if (localizedString == null) {
            Logs.SQL.error("System text entry {} not found in DB.", entry);
            return "<error>";
        }
        return localizedString.get(locale);
    }

    public RaceUnlockRequirement getRaceUnlockRequirement(Race race) {
        return raceUnlockRequirementStorage.get(race);
    }

    public ArrayList<RaceClassAvailability> getClassExpansionRequirements() {
        return classExpansionRequirementStorage;
    }

    public ClassAvailability getClassExpansionRequirement(Race raceId, UnitClass classId) {
        return classExpansionRequirementStorage.stream()
                .filter(e -> e.raceID == raceId)
                .flatMap(e -> e.classes.stream())
                .filter(e -> e.classID == classId)
                .findFirst()
                .orElse(null);

    }

    public ClassAvailability getClassExpansionRequirementFallback(UnitClass classId) {
        return classExpansionRequirementStorage.stream()
                .flatMap(e -> e.classes.stream())
                .filter(e -> e.classID == classId)
                .findFirst()
                .orElse(null);
    }

    public PlayerChoice getPlayerChoice(int choiceId) {
        return playerChoices.get(choiceId);
    }


    //Gossip
    public void loadGossipMenu() {
        var oldMSTime = System.currentTimeMillis();

        gossipMenusStorage.clear();

        Map<Integer, List<GossipMenus>> tmp = new HashMap<>();
        AtomicInteger count = new AtomicInteger();
        try (var items = miscRepo.streamAllGossipMenu()) {
            items.forEach(e -> {
                if (getNpcText(e.textId) == null) {
                    Logs.SQL.error("Table gossip_menu: ID {} is using non-existing TextID {}", e.menuId, e.textId);
                    return;
                }
                tmp.compute(e.menuId, Functions.addToList(e));
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} gossip_menu IDs in {} ms", tmp.size(), count.get() - oldMSTime);

    }

    public void loadGossipMenuItems() {
        var oldMSTime = System.currentTimeMillis();

        Map<Integer, List<GossipMenuOption>> temp = new HashMap<>();
        AtomicInteger count = new AtomicInteger();
        gossipMenuItemsStorage.clear();
        try (var items = miscRepo.streamAllGossipMenuOption()) {
            items.forEach(e -> {
                if (e.optionNpc == null) {
                    Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} has unknown NPC option id {}. Replacing with GossipOptionNpc::None", e.menuId, e.orderIndex, e.optionNpc);
                    e.optionNpc = GossipOptionNpc.None;
                }

                if (e.optionBroadcastTextId != 0) {
                    BroadcastText broadcastText = dbcObjectManager.broadcastText(e.optionBroadcastTextId);
                    if (broadcastText != null) {
                        Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} has non-existing or incompatible OptionBroadcastTextID {}, ignoring.", e.menuId, e.orderIndex, e.optionBroadcastTextId);
                        e.optionBroadcastTextId = 0;
                    }
                }

                if (e.language != 0 && dbcObjectManager.language(e.language) != null) {
                    Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} use non-existing Language {}, ignoring", e.menuId, e.orderIndex, e.language);
                    e.language = 0;
                }

                if (e.actionMenuId != 0 && e.optionNpc != GossipOptionNpc.None) {
                    Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} can not use ActionMenuID for GossipOptionNpc different from GossipOptionNpc::None, ignoring", e.menuId, e.orderIndex);
                    e.actionMenuId = 0;
                }

                if (e.actionPoiId != 0) {
                    if (e.optionNpc != GossipOptionNpc.None) {
                        Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} can not use ActionPoiID for GossipOptionNpc different from GossipOptionNpc::None, ignoring", e.menuId, e.orderIndex);
                        e.actionPoiId = 0;
                    } else if (getPointOfInterest(e.actionPoiId) == null) {
                        Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} use non-existing ActionPoiID {}, ignoring", e.menuId, e.orderIndex, e.actionPoiId);
                        e.actionPoiId = 0;
                    }
                }


                if (e.boxBroadcastTextId != 0) {
                    if (dbcObjectManager.broadcastText(e.boxBroadcastTextId) == null) {
                        Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} has non-existing or incompatible BoxBroadcastTextID {}, ignoring.", e.menuId, e.orderIndex, e.boxBroadcastTextId);
                        e.boxBroadcastTextId = 0;
                    }
                }

                if (e.spellId != 0) {
                    if (spellManager.getSpellInfo(e.spellId, Difficulty.NONE) == null) {
                        Logs.SQL.error("Table `gossip_menu_option` for menu {}, id {} use non-existing Spell {}, ignoring",
                                e.menuId, e.orderIndex, e.spellId);
                        e.spellId = 0;
                    }
                }
                temp.compute(e.menuId, Functions.addToList(e));
                count.incrementAndGet();
            });
        }

        oldMSTime = System.currentTimeMillis();

        AtomicInteger countLocale = new AtomicInteger();
        try (var items = miscRepo.streamAllGossipMenuOptionLocale()) {
            items.forEach(e -> {
                if (e.locale == Locale.enUS) {
                    return;
                }
                GossipMenuOption gossipMenuOption = temp.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey() == e.menuId)
                        .flatMap(entry -> entry.getValue().stream())
                        .filter(option -> option.gossipOptionId == e.optionId).findFirst().orElse(null);
                if (gossipMenuOption == null) {
                    Logs.SQL.error("Table `gossip_menu_option_locale` for gossip_menu_option menuId {}, optionId {} has non-existing record, ignoring.", e.menuId, e.optionId);
                    return;
                }
                gossipMenuOption.boxText.set(e.locale, e.boxText);
                gossipMenuOption.optionText.set(e.locale, e.optionText);
                countLocale.getAndIncrement();
            });
        }

        gossipMenuItemsStorage.putAll(temp);
        Logs.SERVER_LOADING.error(">> Loaded {} gossip_menu_option entries in {} ms", count, System.currentTimeMillis() - oldMSTime);
        Logs.SERVER_LOADING.error(">> Loaded {} gossip_menu_option locale strings in {} ms", countLocale, System.currentTimeMillis() - oldMSTime);

    }

    public void loadGossipMenuAddon() {
        var oldMSTime = System.currentTimeMillis();

        gossipMenuAddonStorage.clear();

        try (var items = miscRepo.streamAllGossipMenuAddon()) {
            items.forEach(e -> {
                var entry = dbcObjectManager.faction(e.friendshipFactionId);

                if (entry != null && dbcObjectManager.friendshipRepReaction(entry.getFriendshipRepID()) == null) {
                    Logs.SQL.error("Table gossip_menu_addon: ID {} is using FriendshipFactionID {} referencing non-existing FriendshipRepID {}",
                            e.menuId, e.friendshipFactionId, entry.getFriendshipRepID());
                    e.friendshipFactionId = 0;
                } else {
                    Logs.SQL.error("Table gossip_menu_addon: ID {} is using non-existing FriendshipFactionID {}", e.menuId, e.friendshipFactionId);
                    e.friendshipFactionId = 0;
                }
            });
        }
        Logs.SQL.error(">> Loaded {} gossip_menu_addon IDs in {} ms", gossipMenuAddonStorage.size(), System.currentTimeMillis() - oldMSTime);

    }

    public void loadPointsOfInterest() {
        var oldMSTime = System.currentTimeMillis();

        pointsOfInterestStorage.clear(); // need for reload case
        HashMap<Integer, PointOfInterest> tmpPointsOfInterestStorage = new HashMap<>();
        try (var items = miscRepo.streamAllPointsOfInterest()) {
            items.forEach(e -> {
                if (!MapDefine.isValidMapCoordinate(e.positionX, e.positionY, e.positionZ)) {
                    Logs.SQL.error("Table `points_of_interest` (ID: {}) have invalid coordinates (PositionX: {} PositionY: {}, PositionZ: {}), ignored.",
                            e.id, e.positionX, e.positionY, e.positionZ);
                    return;
                }
                tmpPointsOfInterestStorage.put(e.id, e);
            });
        }
        Logs.SERVER_LOADING.error(">> Loaded {} Points of Interest definitions in {} ms", tmpPointsOfInterestStorage.size(), System.currentTimeMillis() - oldMSTime);


        oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var items = miscRepo.streamAllPointsOfInterestLocale()) {
            items.forEach(e -> {
                PointOfInterest pointOfInterest = tmpPointsOfInterestStorage.get(e.id);
                if(pointOfInterest == null) {
                    Logs.SQL.error("Table `points_of_interest_locale` is using non-existing points_of_interest id {}", e.id);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                pointOfInterest.name.set(locale, e.name);
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.error(">> Loaded {} points_of_interest locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);

        pointsOfInterestStorage.putAll(tmpPointsOfInterestStorage);


    }

    public List<GossipMenus> getGossipMenusMapBounds(int uiMenuId) {
        return gossipMenusStorage.get(uiMenuId);
    }

    public List<GossipMenuOption> getGossipMenuItemsMapBounds(int uiMenuId) {
        return gossipMenuItemsStorage.get(uiMenuId);
    }

    public GossipMenuAddon getGossipMenuAddon(int menuId) {
        return gossipMenuAddonStorage.get(menuId);
    }

    public PointOfInterest getPointOfInterest(int id) {
        return pointsOfInterestStorage.get(id);
    }

    public void loadGraveyardZones() {
        var oldMSTime = System.currentTimeMillis();
        graveYardStorage.clear(); // need for reload case
        AtomicInteger count = new AtomicInteger();
        try (var items = miscRepo.streamAllGraveyardZone()) {
            items.forEach(fields -> {
                count.getAndIncrement();
                WorldSafeLocEntry entry = getWorldSafeLoc(fields[0]);
                if (entry == null) {
                    Logs.SQL.error("Table `graveyard_zone` has a record for non-existing graveyard (WorldSafeLocsID: {}), skipped.", fields[0]);
                    return;
                }

                AreaTable areaEntry = dbcObjectManager.areaTable(fields[1]);
                if (areaEntry == null) {
                    Logs.SQL.error("Table `graveyard_zone` has a record for non-existing Zone (ID: {}), skipped.", fields[1]);
                    return;
                }

                if (!addGraveYardLink(fields[0], fields[1], Team.TEAM_OTHER, false))
                    Logs.SQL.error("Table `graveyard_zone` has a duplicate record for Graveyard (ID: {}) and Zone (ID: {}), skipped.", fields[0], fields[1]);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} graveyard-zone links in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }



    public WorldSafeLocEntry getDefaultGraveYard(Team team) {
        if (team == Team.HORDE) {
            return getWorldSafeLoc(10);
        } else if (team == Team.ALLIANCE) {
            return getWorldSafeLoc(4);
        } else {
            return null;
        }
    }

    public WorldSafeLocEntry getClosestGraveYard(WorldLocation location, Team team, WorldObject conditionObject) {
        var mapId = location.getMapId();

        // search for zone associated closest graveyard
        var zoneId = world.getTerrainManager().getZoneId(conditionObject != null ? conditionObject.getPhaseShift() : PhasingHandler.EMPTY_PHASE_SHIFT, mapId, location);

        if (zoneId == 0) {
            if (location.getZ() > -500) {
                Logs.MISC.error("ZoneId not found for map {} coords ({}, {}, {})", mapId, location.getX(), location.getY(), location.getZ());

                return getDefaultGraveYard(team);
            }
        }

        WorldSafeLocEntry  graveyard = getClosestGraveyardInZone(location, team, conditionObject, zoneId);
        AreaTable zoneEntry = Objects.requireNonNull(dbcObjectManager.areaTable(zoneId));
        AreaTable parentEntry = dbcObjectManager.areaTable(zoneEntry.getParentAreaID());

        while (graveyard == null && parentEntry != null)
        {
            graveyard = getClosestGraveyardInZone(location, team, conditionObject, parentEntry.getId());
            if (graveyard == null && parentEntry.getParentAreaID() != 0)
                parentEntry = dbcObjectManager.areaTable(parentEntry.getParentAreaID());
            else // nothing found, cant look further, give up.
                parentEntry = null;
        }

        return graveyard;
    }

    private WorldSafeLocEntry getClosestGraveyardInZone(WorldLocation location, Team team, WorldObject conditionObject, int zoneId) {
        // Simulate std. algorithm:
        //   found some graveyard associated to (ghost_zone, ghost_map)
        //
        //   if mapId == graveyard.mapId (ghost in plain zone or city or Battleground) and search graveyard at same map
        //     then check faction
        //   if mapId != graveyard.mapId (ghost in instance) and search any graveyard associated
        //     then check faction
        int mapId = location.getMapId();
        var range = graveYardStorage.get(zoneId);
        var mapEntry = dbcObjectManager.map(mapId);

        // not need to check validity of map object; MapId _MUST_ be valid here
        if (range.isEmpty() && !mapEntry.isBattlegroundOrArena()) {
            if (zoneId != 0) // zone == 0 can't be fixed, used by bliz for bugged zones
            {
                Logs.SQL.error("Table `game_graveyard_zone` incomplete: Zone {} Team {} does not have a linked graveyard.", zoneId, team);
            }

            return getDefaultGraveYard(team);
        }

        // at corpse map
        var foundNear = false;
        float distNear = 10000;
        WorldSafeLocEntry entryNear = null;

        // at entrance map for corpse map
        var foundEntr = false;
        float distEntr = 10000;
        WorldSafeLocEntry entryEntr = null;

        // some where other
        WorldSafeLocEntry entryFar = null;

        ConditionSourceInfo conditionSource = new ConditionSourceInfo(conditionObject);

        for (var data : range) {
            var entry = getWorldSafeLoc(data.safeLocId);

            if (entry == null) {
                Logs.SQL.error("Table `game_graveyard_zone` has record for not existing graveyard (WorldSafeLocs.dbc id) {}, skipped.", data.safeLocId);

                continue;
            }

            if (conditionObject != null) {

                if (!world.getConditionManager().isObjectMeetingNotGroupedConditions(ConditionSourceType.GRAVEYARD, data.safeLocId, conditionSource)) {
                    continue;
                }

                if (entry.loc.getMapId() == mapEntry.getParentMapID() && !conditionObject.getPhaseShift().hasVisibleMapId(entry.loc.getMapId())) {
                    continue;
                }
            } else if (data.team != 0) {
                continue;
            }



            // find now nearest graveyard at other map
            if (mapId != entry.loc.getMapId() && entry.loc.getMapId() != mapEntry.getParentMapID()) {
                // if find graveyard at different map from where entrance placed (or no entrance data), use any first
                if (mapEntry == null || mapEntry.getCorpseMapID() < 0 || mapEntry.getCorpseMapID() != entry.loc.getMapId() || (mapEntry.getCorpseX() == 0 && mapEntry.getCorpseY() == 0)) {
                    // not have any corrdinates for check distance anyway
                    entryFar = entry;

                    continue;
                }

                // at entrance map calculate distance (2D);
                var dist2 = (entry.loc.getX() - mapEntry.getCorpseX()) * (entry.loc.getX() - mapEntry.getCorpseX())
                        + (entry.loc.getY() - mapEntry.getCorpseY()) * (entry.loc.getY() - mapEntry.getCorpseY());

                if (foundEntr) {
                    if (dist2 < distEntr) {
                        distEntr = dist2;
                        entryEntr = entry;
                    }
                } else {
                    foundEntr = true;
                    distEntr = dist2;
                    entryEntr = entry;
                }
            }
            // find now nearest graveyard at same map
            else {
                var dist2 = (entry.loc.getX() - location.getX()) * (entry.loc.getX() - location.getX())
                        + (entry.loc.getY() - location.getY()) * (entry.loc.getY() - location.getY())
                        + (entry.loc.getZ() - location.getZ()) * (entry.loc.getZ() - location.getZ());

                if (foundNear) {
                    if (dist2 < distNear) {
                        distNear = dist2;
                        entryNear = entry;
                    }
                } else {
                    foundNear = true;
                    distNear = dist2;
                    entryNear = entry;
                }
            }
        }

        if (entryNear != null) {
            return entryNear;
        }

        if (entryEntr != null) {
            return entryEntr;
        }

        return entryFar;
    }


    public GraveYardData findGraveYardData(int id, int zoneId) {
        var range = graveYardStorage.get(zoneId);

        for (var data : range) {
            if (data.safeLocId == id) {
                return data;
            }
        }

        return null;
    }


    void loadWorldSafeLoc()
    {
        long oldMSTime = System.currentTimeMillis();

        try (var items = miscRepo.streamAllWorldSafeLoc()) {
            items.forEach(item -> {
                if (!world.getMapManager().isValidMapCoordinate(item.loc))
                {
                    Logs.SQL.error("World location (ID: {}) has a invalid position MapID: {} {}, skipped", item.id, item.loc.getMapId(), item.loc);
                    return;
                }

                if (item.transportSpawnId != null)
                {
                    if (null == world.getTransportManager().getTransportSpawn(item.transportSpawnId))
                    {
                        Logs.SQL.error("World location (ID: {}) has a invalid transportSpawnID {}, skipped.", item.id, item.transportSpawnId);
                        return;
                    }
                }

                worldSafeLocs.put(item.id, item);
            });
        }

        Logs.SQL.info(">> Loaded {} world locations {} ms", worldSafeLocs.size(), System.currentTimeMillis() - oldMSTime);
    }


    public WorldSafeLocEntry getWorldSafeLoc(int id) {
        return worldSafeLocs.get(id);
    }

    public boolean addGraveYardLink(int id, int zoneId, Team team) {
        return addGraveYardLink(id, zoneId, team, true);
    }

    public boolean addGraveYardLink(int id, int zoneId, Team team, boolean persist) {
        if (findGraveYardData(id, zoneId) != null) {
            return false;
        }

        // add link to loaded data
        GraveYardData data = new GraveYardData();
        data.safeLocId = id;
        data.team = team.value;

        graveYardStorage.compute(zoneId, Functions.addToList(data));

        // add link to DB
        if (persist) {
            miscRepo.insertGraveyardZone(id, zoneId);
        }
        if (team != Team.TEAM_OTHER)
        {
            //TODO save condition
        }
        return true;
    }

    //Scripts
    public void loadAreaTriggerScripts() {
        var oldMSTime = System.currentTimeMillis();

        areaTriggerScriptStorage.clear(); // need for reload case

        try(var items = areaTriggerRepo.streamAllAreaTriggerScripts()) {
            items.forEach(item -> {
                var id = (Integer) item[0];
                var scriptName = (String) item[0];
                var atEntry = dbcObjectManager.areaTrigger(id);
                if (atEntry == null) {
                    Logs.SQL.error("AreaTrigger (ID: {}) does not exist in `AreaTrigger.dbc`.", id);
                    return;
                }
                areaTriggerScriptStorage.put(id, getScriptId(scriptName));

            });
        }
        Logs.SQL.error(">> Loaded {} areatrigger scripts in {} ms", areaTriggerScriptStorage.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadSpellScripts() {
        loadScripts(ScriptsType.SPELL);

        // check ids
        for (var script : spellScripts.entrySet()) {
            var spellId = script.getKey() & 0x00FFFFFF;
            var spellInfo = world.getSpellManager().getSpellInfo(spellId, Difficulty.NONE);

            if (spellInfo == null) {
                Logs.SQL.error("Table `spell_scripts` has not existing spell (Id: {}) as script id", spellId);

                continue;
            }

            var spellEffIndex = (byte) ((script.getKey() >> 24) & 0x000000FF);

            if (spellEffIndex >= spellInfo.getEffects().size()) {
                Logs.SQL.error("Table `spell_scripts` has too high effect index {} for spell (Id: {}) as script id", spellEffIndex, spellId);

                continue;
            }
            var spellEffIndexEnum = SpellEffIndex.values()[spellEffIndex];
            //check for correct spellEffect
            if (spellInfo.getEffect(spellEffIndexEnum).effect == SpellEffectName.NONE
                    || (spellInfo.getEffect(spellEffIndexEnum).effect != SpellEffectName.SCRIPT_EFFECT && spellInfo.getEffect(spellEffIndexEnum).effect != SpellEffectName.DUMMY)) {
                Logs.SQL.error(String.format("Table `spell_scripts` - spell %1$s effect %2$s is not SPELL_EFFECT_SCRIPT_EFFECT or SPELL_EFFECT_DUMMY", spellId, spellEffIndex));
            }
        }
    }

    public void loadEventScripts() {

        // Set of valid events referenced in several sources
        LoadEventSet();

        // Deprecated
        loadScripts(ScriptsType.EVENT);

        // Then check if all scripts are in above list of possible script entries
        for (var script : eventScripts.entrySet()) {
            if(eventStorage.contains(script.getKey())) {
                Logs.SQL.error("Table `event_scripts` has script (Id: {}) not referring to any gameobject_template (type 3 data6 field, type 7 data3 field, type 10 data2 field, type 13 data2 field, type 50 data7 field), any taxi path node or any spell effect {}",
                        script.getKey(), SpellEffectName.SEND_EVENT);
            }
        }


    }

    private void LoadEventSet() {
        // Load all possible script entries from gameobjects
        for (var go : gameObjectTemplateStorage.entrySet()) {
            var eventId = go.getValue().getEventScriptId();
            if (eventId != 0) {
                eventStorage.add(eventId);
            }
        }

        // Load all possible script entries from spells
        for (var spellEntry : dbcObjectManager.spell()) {
            var spell = world.getSpellManager().getSpellInfo(spellEntry.getId(), Difficulty.NONE);
            if (spell != null) {
                for (var spellEffectInfo : spell.getEffects()) {
                    if (spellEffectInfo.isEffect(SpellEffectName.SEND_EVENT)) {
                        if (spellEffectInfo.miscValue != 0) {
                            eventStorage.add(spellEffectInfo.miscValue);
                        }
                    }
                }
            }
        }

        var taxiPathNodesByPath = dbcObjectManager.getTaxiPathNodesByPath();
        for (var integerListEntry : taxiPathNodesByPath.entrySet()) {
            var value = integerListEntry.getValue();
            for (TaxiPathNode node : value) {
                if (node.getArrivalEventID() != 0) {
                    eventStorage.add(node.getArrivalEventID());
                }

                if (node.getDepartureEventID() != 0) {
                    eventStorage.add(node.getDepartureEventID());
                }
            }

        }
    }

    public boolean registerSpellScript(int spellId, String scriptName) {
        var allRanks = false;

        if (spellId < 0) {
            allRanks = true;
            spellId = -spellId;
        }
        return false;
    }

    public void loadSpellScriptNames() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        spellScriptsStorage.clear(); // need for reload case

        try(var result = miscRepo.streamAllSpellScriptNames()) {

            result.forEach((row) -> {
                var spellId = (Integer) row[0];
                var scriptName = (String) row[1];


                boolean allRanks = false;
                if (spellId < 0)
                {
                    allRanks = true;
                    spellId = -spellId;
                }

                SpellInfo spellInfo = world.getSpellManager().getSpellInfo(spellId, Difficulty.NONE);
                if (spellInfo == null)
                {
                    Logs.SQL.error("Scriptname: `{}` spell (Id: {}) does not exist.", scriptName, spellId);
                    return;
                }

                if (allRanks)
                {
                    if (!spellInfo.isRanked())
                        Logs.SQL.error("Scriptname: `{}` spell (Id: {}) has no ranks of spell.", scriptName, spellId);

                    if (spellInfo.getFirstRankSpell().getId() != spellId)
                    {
                        Logs.SQL.error("Scriptname: `{}` spell (Id: {}) is not first rank of spell.", scriptName, spellId);
                        return;
                    }
                    while (spellInfo != null)
                    {
                        spellScriptsStorage.compute(spellInfo.getId(), Functions.addToList(new Pair<>(getScriptId(scriptName), true)));

                        spellInfo = spellInfo.getNextRankSpell();
                    }
                }
                else
                {
                    if (spellInfo.isRanked())
                        Logs.SQL.error("Scriptname: `{}` spell (Id: {}) is ranked spell. Perhaps not all ranks are assigned to this script.", scriptName, spellId);

                    spellScriptsStorage.compute(spellInfo.getId(), Functions.addToList(new Pair<>(getScriptId(scriptName), true)));
                }

                count.incrementAndGet();


            });

        }

        Logs.SERVER_LOADING.info(">> Loaded {} spell script names in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void validateSpellScripts() {
        var oldMSTime = System.currentTimeMillis();

        if (spellScriptsStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Validated 0 scripts.");

            return;
        }

        int count = 0;


        var iterator = spellScriptsStorage.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();

            var spellEntry = world.getSpellManager().getSpellInfo(entry.getKey(), Difficulty.NONE);
        }

        Logs.SERVER_LOADING.info(">> Validated {} scripts in {} ms", count, System.currentTimeMillis() - (oldMSTime));
    }

    public List<Pair<Integer,Boolean>> getSpellScriptsBounds(int spellId) {
        return spellScriptsStorage.get(spellId);
    }

    public ArrayList<String> getAllDBScriptNames() {
        return scriptNamesStorage.getAllDBScriptNames();
    }

    public String getScriptName(int id) {
        var entry = scriptNamesStorage.find(id);

        if (entry != null) {
            return entry.name;
        }

        return "";
    }

    public int getScriptId(String name) {
        return getScriptId(name, true);
    }

    public int getScriptId(String name, boolean isDatabaseBound) {
        if (StringUtil.isEmpty(name)) {
            return 0;
        }

        return scriptNamesStorage.insert(name, isDatabaseBound);
    }

    public int getAreaTriggerScriptIds(int triggerId) {
        return areaTriggerScriptStorage.get(triggerId);
    }

    //Creatures
    public void loadCreatureTemplates() {
        var time = System.currentTimeMillis();
        HashMap<Integer, CreatureTemplate> templateHashMap = new HashMap<>();
        try(var items = creatureRepo.streamCreatureTemplate(0, 1)) {
            items.forEach(creatureTemplate -> {
                creatureTemplate.scriptID = getScriptId(creatureTemplate.script);
                templateHashMap.put(creatureTemplate.entry, creatureTemplate);
            });
        }

        loadCreatureTemplateResistances(templateHashMap);
        loadCreatureTemplateSpells(templateHashMap);

        // We load the creature models after loading but before checking
        loadCreatureTemplateModels(templateHashMap);

        loadCreatureSummonedData(templateHashMap);

        loadCreatureLocales(templateHashMap);

        loadCreatureTemplateAddons(templateHashMap);
        loadCreatureTemplateSparring(templateHashMap);
        loadCreatureTemplateDifficulty(templateHashMap);

        // Checking needs to be done after loading because of the difficulty self referencing
        for (var template : templateHashMap.values()) {
            checkCreatureTemplate(template, templateHashMap);
        }
        creatureTemplateStorage.putAll(templateHashMap);
        Logs.SERVER_LOADING.info(">> Loaded {} creature definitions in {} ms", creatureTemplateStorage.size(), System.currentTimeMillis() - time);
    }



    public void checkCreatureTemplate(CreatureTemplate cInfo, HashMap<Integer, CreatureTemplate> templateHashMap) {
        if (cInfo == null) {
            return;
        }

        FactionTemplate factionTemplate = dbcObjectManager.factionTemplate(cInfo.faction);
        if (factionTemplate == null)
        {
            Logs.SQL.error("Creature (Entry: {}) has non-existing faction template ({}). This can lead to crashes, set to faction 35.", cInfo.entry, cInfo.faction);
            cInfo.faction = Objects.requireNonNull(dbcObjectManager.factionTemplate(35)).getId(); // this might seem stupid but all shit will would break if faction 35 did not exist
        }

        for (int k = 0; k < CreatureTemplate.MAX_KILL_CREDIT; ++k)
        {
            if (cInfo.killCredit[k] != 0)
            {
                if (templateHashMap.get(cInfo.killCredit[k]) == null)
                {
                    Logs.SQL.error("Creature (Entry: {}) lists non-existing creature entry {} in `KillCredit{}`.", cInfo.entry, cInfo.killCredit[k], k + 1);
                    cInfo.killCredit[k] = 0;
                }
            }
        }

        if (cInfo.models.isEmpty())
            Logs.SQL.error("Creature (Entry: {}) does not have any existing display id in creature_template_model.", cInfo.entry);

        if ((1 << cInfo.unitClass.ordinal()-1 & SharedDefine.CLASS_MASK_ALL_CREATURES) == 0)
        {
            Logs.SQL.error("Creature (Entry: {}) has invalid unit_class ({}) in creature_template. Set to 1 (UNIT_CLASS_WARRIOR).", cInfo.entry, cInfo.unitClass);
            cInfo.unitClass = UnitClass.WARRIOR;
        }



        if (cInfo.baseAttackTime == 0)
            cInfo.baseAttackTime  = BASE_ATTACK_TIME;

        if (cInfo.rangeAttackTime == 0)
            cInfo.rangeAttackTime = BASE_ATTACK_TIME;

        if (cInfo.speedWalk == 0.0f)
        {
            Logs.SQL.error("Creature (Entry: {}) has wrong value ({}) in speed_walk, set to 1.", cInfo.entry, cInfo.speedWalk);
            cInfo.speedWalk = 1.0f;
        }

        if (cInfo.speedRun == 0.0f)
        {
            Logs.SQL.error("Creature (Entry: {}) has wrong value ({}) in speed_run, set to 1.14286.", cInfo.entry, cInfo.speedRun);
            cInfo.speedRun = 1.14286f;
        }

        if (cInfo.type != null && dbcObjectManager.creatureType(cInfo.type) == null)
        {
            Logs.SQL.error("Creature (Entry: {}) has invalid creature type ({}) in `type`.", cInfo.entry, cInfo.type);
            cInfo.type = CreatureType.HUMANOID;
        }

        if (cInfo.family != null && dbcObjectManager.creatureFamily(cInfo.family) == null)
        {
            Logs.SQL.error("Creature (Entry: {}) has invalid creature family ({}) in `family`.", cInfo.entry, cInfo.family);
            cInfo.family = CreatureFamily.NONE;
        }

        checkCreatureMovement("creature_template_movement", cInfo.entry, cInfo.movement);

        if (cInfo.vehicleId != 0)
        {
            VehicleEntry vehId = dbcObjectManager.vehicle(cInfo.vehicleId);
            if (vehId == null)
            {
                Logs.SQL.error("Creature (Entry: {}) has a non-existing VehicleId ({}). This *WILL* cause the client to freeze!", cInfo.entry, cInfo.vehicleId);
                cInfo.vehicleId = 0;
            }
        }

        for (int j = 0; j < CreatureTemplate.MAX_CREATURE_SPELLS; ++j)
        {
            if (cInfo.spells[j] != 0 && world.getSpellManager().getSpellInfo(cInfo.spells[j], Difficulty.NONE) == null)
            {
                Logs.SQL.error("Creature (Entry: {}) has non-existing Spell{} ({}), set to 0.", cInfo.entry, j+1, cInfo.spells[j]);
                cInfo.spells[j] = 0;
            }
        }

        if (cInfo.movementType > MovementGeneratorType.RANDOM.ordinal())
        {
            Logs.SQL.error("Creature (Entry: {}) has wrong movement generator type ({}), ignored and set to IDLE.", cInfo.entry, cInfo.movementType);
            cInfo.movementType = MovementGeneratorType.IDLE.ordinal();
        }

        if (cInfo.requiredExpansion >= Expansion.MAX_EXPANSION)
        {
            Logs.SQL.error("Table `creature_template` lists creature (Entry: {}) with `RequiredExpansion` {}. Ignored and set to 0.", cInfo.entry, cInfo.requiredExpansion);
            cInfo.requiredExpansion = 0;
        }

        if (cInfo.flagsExtra.hasNotFlag(CreatureFlagExtra.DB_ALLOWED))
        {
            Logs.SQL.error("Table `creature_template` lists creature (Entry: {}) with disallowed `flags_extra` {}, removing incorrect flag.", cInfo.entry, cInfo.flagsExtra);
            cInfo.flagsExtra.removeNotFlag(CreatureFlagExtra.DB_ALLOWED);
        }

        if (cInfo.unitFlags.hasNotFlag(UnitFlag.ALLOWED))
        {
            Logs.SQL.error("Table `creature_template` lists creature (Entry: {}) with disallowed `unit_flags` {}, removing incorrect flag.", cInfo.entry, cInfo.unitFlags);
            cInfo.unitFlags.removeNotFlag(UnitFlag.ALLOWED);
        }

        if (cInfo.unitFlags2.hasNotFlag(UnitFlag2.ALLOWED))
        {
            Logs.SQL.error("Table `creature_template` lists creature (Entry: {}) with disallowed `unit_flags2` {}, removing incorrect flag.", cInfo.entry, cInfo.unitFlags2);
            cInfo.unitFlags2.removeNotFlag(UnitFlag2.ALLOWED);
        }

        if (cInfo.unitFlags3.hasNotFlag(UnitFlag3.ALLOWED))
        {
            Logs.SQL.error("Table `creature_template` lists creature (Entry: {}) with disallowed `unit_flags3` {}, removing incorrect flag.", cInfo.entry, cInfo.unitFlags3);
            cInfo.unitFlags3.removeNotFlag(UnitFlag3.ALLOWED);
        }

        if (!Utils.isArrayEmpty(cInfo.gossipMenuIds) && !cInfo.npcFlag.hasFlag(NPCFlag.GOSSIP))
            Logs.SQL.error("Creature (Entry: {}) has assigned gossip menu, but npcflag does not include UNIT_NPC_FLAG_GOSSIP.", cInfo.entry);
        else if (Utils.isArrayEmpty(cInfo.gossipMenuIds) && cInfo.npcFlag.hasFlag(NPCFlag.GOSSIP))
            Logs.SQL.error("Creature (Entry: {}) has npcflag UNIT_NPC_FLAG_GOSSIP, but gossip menu is unassigned.", cInfo.entry);
    }


    public void loadCreatureTemplateAddons(HashMap<Integer, CreatureTemplate> templateHashMap) {
        var time = System.currentTimeMillis();

        AtomicInteger count = new AtomicInteger();
        try(var items = creatureRepo.streamAllCreatureTemplateAddon()) {
            items.forEach(e -> {
                CreatureTemplate template = templateHashMap.get(e.entry);
                if (template == null) {
                    Logs.SQL.error("Creature template (Entry: {}) does not exist but has a record in `creature_template_addon`", e.entry);
                    return;
                }
                for (int aura : e.auras) {

                    SpellInfo spellInfo = spellManager.getSpellInfo(aura, Difficulty.NONE);

                    if (spellInfo == null) {
                        Logs.SQL.error("Creature (Entry: {}) has wrong spell '{}' defined in `auras` field in `creature_template_addon`.", e.entry, aura);
                        continue;
                    }

                    if (spellInfo.hasAura(AuraType.CONTROL_VEHICLE))
                        Logs.SQL.error("Creature (Entry: {}) has SPELL_AURA_CONTROL_VEHICLE aura {} defined in `auras` field in `creature_template_addon`.", e.entry, spellInfo.getId());


                    if (spellInfo.getDuration() > 0) {
                        Logs.SQL.error("Creature (Entry: {}) has temporary aura (spell {}) in `auras` field in `creature_template_addon`.", e.entry, spellInfo.getId());
                    }
                }

                if (e.mount != 0) {
                    if (!dbcObjectManager.creatureDisplayInfo().contains(e.mount)) {
                        Logs.SQL.error("Creature (Entry: {}) has invalid displayInfoId ({}) for mount defined in `creature_template_addon`", e.entry, e.mount);
                        e.mount = 0;
                    }
                }

                // PvPFlags don't need any checking for the time being since they cover the entire range of a byte
                if (!dbcObjectManager.emote().contains(e.emote)) {
                    Logs.SQL.error("Creature (Entry: {}) has invalid emote ({}) defined in `creature_template_addon`.", e.entry, e.emote);
                    e.emote = 0;
                }

                if (e.aiAnimKit != 0 && !dbcObjectManager.animKit().contains(e.aiAnimKit)) {
                    Logs.SQL.error("Creature (Entry: {}) has invalid aiAnimKit ({}) defined in `creature_template_addon`.", e.entry, e.aiAnimKit);
                    e.aiAnimKit = 0;
                }

                if (e.movementAnimKit != 0 && !dbcObjectManager.animKit().contains(e.movementAnimKit)) {
                    Logs.SQL.error("Creature (Entry: {}) has invalid movementAnimKit ({}) defined in `creature_template_addon`.", e.entry, e.movementAnimKit);
                    e.movementAnimKit = 0;
                }

                if (e.meleeAnimKit != 0 && !dbcObjectManager.animKit().contains(e.meleeAnimKit)) {
                    Logs.SQL.error("Creature (Entry: {}) has invalid meleeAnimKit ({}) defined in `creature_template_addon`.", e.entry, e.meleeAnimKit);
                    e.meleeAnimKit = 0;
                }

                template.creatureTemplateAddon = e;

                count.incrementAndGet();

            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature template addons in {} ms", count, System.currentTimeMillis() - time);
    }


    void loadCreatureTemplateSparring(HashMap<Integer, CreatureTemplate> templateHashMap) {
        long oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var items = creatureRepo.streamAllCreatureTemplateSparring()) {
            items.forEach(fields -> {
                Integer entry = (Integer) fields[0];
                Float noNPCDamageBelowHealthPct = (Float) fields[1];
                CreatureTemplate template = templateHashMap.get(entry);
                if (template == null) {
                    Logs.SQL.error("Creature template (Entry: {}) does not exist but has a record in `creature_template_sparring`", entry);
                    return;
                }
                if (noNPCDamageBelowHealthPct <= 0 || noNPCDamageBelowHealthPct > 100) {
                    Logs.SQL.error("Creature (Entry: {}) has invalid NoNPCDamageBelowHealthPct ({}) defined in `creature_template_sparring`. Skipping",
                            entry, noNPCDamageBelowHealthPct);
                    return;
                }
                count.incrementAndGet();

            });
        }
        Logs.SQL.error(">> Loaded {} creature template sparring rows in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    void loadCreatureTemplateDifficulty(HashMap<Integer, CreatureTemplate> templateHashMap) {
        long oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var items = creatureRepo.streamAllCreatureTemplateDifficulty()) {
            items.forEach(e -> {
                CreatureTemplate template = templateHashMap.get(e.entry);
                if (template == null) {
                    Logs.SQL.error("Creature template (Entry: {}) does not exist but has a record in `creature_template_sparring`", e.entry);
                    return;
                }
                e.damageModifier *= getDamageMod(template.classification);

                if (e.goldMin > e.goldMax) {
                    Logs.SQL.error("Table `creature_template_difficulty` lists creature (ID: {}) with `GoldMin` {} greater than `GoldMax` {}, setting `GoldMax` to {}.",
                            e.entry, e.goldMin, e.goldMax, e.goldMin);
                    e.goldMax = e.goldMin;
                }

                template.difficultyStore.put(e.difficulty, e);
                count.incrementAndGet();

            });
        }
        Logs.SQL.error(">> Loaded {} creature template difficulty data in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadCreatureAddons(Map<Integer, CreatureData> creatureData) {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var item = creatureRepo.streamAllCreatureAddon()) {
            item.forEach(e -> {
                var creData = creatureData.get(e.entry);
                if (creData == null)
                {
                    Logs.SQL.error("Creature (GUID: {}) does not exist but has a record in `creature_addon`", e.entry);
                    return;
                }

                if (creData.movementType == MovementGeneratorType.WAYPOINT.ordinal() && e.pathId == 0)
                {
                    creData.movementType = (byte) MovementGeneratorType.IDLE.ordinal();
                    Logs.SQL.error("Creature (GUID {}) has movement type set to WAYPOINT_MOTION_TYPE but no path assigned", e.entry);
                }

                for (int aura : e.auras) {

                    SpellInfo spellInfo = spellManager.getSpellInfo(aura, Difficulty.NONE);

                    if (spellInfo == null) {
                        Logs.SQL.error("Creature (GUID: {}) has wrong spell '{}' defined in `auras` field in `creature_addon`.", e.entry, aura);
                        continue;
                    }

                    if (spellInfo.hasAura(AuraType.CONTROL_VEHICLE))
                        Logs.SQL.error("Creature (GUID: {}) has SPELL_AURA_CONTROL_VEHICLE aura {} defined in `auras` field in `creature_template_addon`.", e.entry, spellInfo.getId());


                    if (spellInfo.getDuration() > 0) {
                        Logs.SQL.error("Creature (GUID: {}) has temporary aura (spell {}) in `auras` field in `creature_template_addon`.", e.entry, spellInfo.getId());
                    }
                }

                if (e.mount != 0) {
                    if (!dbcObjectManager.creatureDisplayInfo().contains(e.mount)) {
                        Logs.SQL.error("Creature (GUID: {}) has invalid displayInfoId ({}) for mount defined in `creature_template_addon`", e.entry, e.mount);
                        e.mount = 0;
                    }
                }

                // PvPFlags don't need any checking for the time being since they cover the entire range of a byte
                if (!dbcObjectManager.emote().contains(e.emote)) {
                    Logs.SQL.error("Creature (GUID: {}) has invalid emote ({}) defined in `creature_template_addon`.", e.entry, e.emote);
                    e.emote = 0;
                }

                if (e.aiAnimKit != 0 && !dbcObjectManager.animKit().contains(e.aiAnimKit)) {
                    Logs.SQL.error("Creature (GUID: {}) has invalid aiAnimKit ({}) defined in `creature_template_addon`.", e.entry, e.aiAnimKit);
                    e.aiAnimKit = 0;
                }

                if (e.movementAnimKit != 0 && !dbcObjectManager.animKit().contains(e.movementAnimKit)) {
                    Logs.SQL.error("Creature (GUID: {}) has invalid movementAnimKit ({}) defined in `creature_template_addon`.", e.entry, e.movementAnimKit);
                    e.movementAnimKit = 0;
                }

                if (e.meleeAnimKit != 0 && !dbcObjectManager.animKit().contains(e.meleeAnimKit)) {
                    Logs.SQL.error("Creature (GUID: {}) has invalid meleeAnimKit ({}) defined in `creature_template_addon`.", e.entry, e.meleeAnimKit);
                    e.meleeAnimKit = 0;
                }
                creData.creatureAddon = e;
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info( ">> Loaded {} creature addons in {} ms", count, System.currentTimeMillis() - oldMSTime);

    }

    public void loadCreatureQuestItems() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var item = creatureRepo.streamAllCreatureQuestItem()) {
            item.forEach(fields -> {

                Difficulty difficulty = Difficulty.values()[fields[1]];
                CreatureTemplate creatureInfo = getCreatureTemplate(fields[0]);
                if (creatureInfo == null) {
                    Logs.SQL.error("Table `creature_questitem` has data for nonexistent creature (entry: {}, difficulty: {}, idx: {}), skipped", fields[0], fields[1], fields[3]);
                    return;
                }
                ;
                ItemEntry db2Data = dbcObjectManager.item(fields[2]);
                if (db2Data == null) {
                    Logs.SQL.error("Table `creature_questitem` has nonexistent item (ID: {}) in creature (entry: {}, difficulty: {}, idx: {}), skipped", fields[2], fields[0], fields[1], fields[3]);
                    return;
                }
                creatureQuestItemStorage.compute(Pair.of(fields[1], difficulty), Functions.addToList(fields[1]));
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature quest items in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadEquipmentTemplates() {
        var time = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var item = creatureRepo.streamAllCreatureEquipTemplate()) {
            item.forEach(fields -> {

                if (getCreatureTemplate(fields[0]) == null) {
                    Logs.SQL.error("Creature template (CreatureID: {}) does not exist but has a record in `creature_equip_template`", fields[0]);
                    return;
                }

                byte id = (byte) fields[1];
                if (id == 0)
                {
                    Logs.SQL.error("Creature equipment template with id 0 found for creature {}, skipped.", fields[0]);
                    return;
                }

                EquipmentInfo equipmentInfo = new EquipmentInfo();

                for (var i = 0; i < UnitDefine.MAX_EQUIPMENT_ITEMS; ++i) {
                    equipmentInfo.items[i].itemId = fields[(2 + i * 3)];
                    equipmentInfo.items[i].appearanceModId = (short) fields[(3 + i * 3)];
                    equipmentInfo.items[i].itemVisual = (short) fields[(4 + i * 3)];

                    if (equipmentInfo.items[i].itemId == 0) {
                        continue;
                    }

                    var dbcItem = dbcObjectManager.item(equipmentInfo.items[i].itemId);

                    if (dbcItem == null) {
                        Logs.SQL.error("Unknown item (ID={}) in creature_equip_template.ItemID{} for CreatureID = {} and ID={}, forced to 0.", equipmentInfo.items[i].itemId, i + 1, fields[0], fields[1]);

                        equipmentInfo.items[i].itemId = 0;

                        continue;
                    }

                    if (dbcObjectManager.getItemModifiedAppearance(equipmentInfo.items[i].itemId, equipmentInfo.items[i].appearanceModId) == null) {
                        Logs.SQL.error("Unknown item appearance for (ID: {}, AppearanceModID: {}) pair in creature_equip_template.ItemID{} " +
                                        "creature_equip_template.AppearanceModID{} for CreatureID: {} and ID: {}, forced to default.",
                                equipmentInfo.items[i].itemId, equipmentInfo.items[i].appearanceModId, i + 1, i + 1, fields[0], fields[1]);

                        var defaultAppearance = dbcObjectManager.getDefaultItemModifiedAppearance(equipmentInfo.items[i].itemId);

                        if (defaultAppearance != null) {
                            equipmentInfo.items[i].appearanceModId = (short) defaultAppearance.getItemAppearanceModifierID();
                        } else {
                            equipmentInfo.items[i].appearanceModId = 0;
                        }

                        continue;
                    }

                    if (dbcItem.getInventoryType() != InventoryType.WEAPON.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.SHIELD.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.RANGED.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.TWO_HANDED_WEAPON.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.WEAPON_MAIN_HAND.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.WEAPON_OFFHAND.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.HOLDABLE.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.THROWN.ordinal()
                            && dbcItem.getInventoryType() != InventoryType.RANGEDRIGHT.ordinal()) {
                        Logs.SQL.error("Item (ID={}) in creature_equip_template.ItemID{} for CreatureID = {} and ID = {} is not equipable in a hand, forced to 0.",
                                equipmentInfo.items[i].itemId, i + 1, fields[0], fields[1]);

                        equipmentInfo.items[i].itemId = 0;
                    }
                }
                equipmentInfoStorage.compute(fields[0], Functions.addToMap(id, equipmentInfo));
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} equipment templates in {} ms", count, System.currentTimeMillis()- time);
    }

    public void loadCreatureMovementOverrides() {
        var oldMSTime = System.currentTimeMillis();
        creatureMovementOverrides.clear();
        try (var item = creatureRepo.streamAllCreatureMovementOverride()) {
            item.forEach(e -> {
                checkCreatureMovement("creature_movement_override", e.spawnId, e);
                creatureMovementOverrides.put(e.spawnId, e);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} movement overrides in {} ms", creatureMovementOverrides.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadCreatureClassLevelStats() {
        var time = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        creatureBaseStatsStorage.clear();
        try (var item = creatureRepo.streamAllCreatureClassLevelStats()) {
            item.forEach(e -> {
                if (((1 << (e.klass - 1)) & SharedDefine.CLASS_MASK_ALL_CREATURES) == 0) {
                    Logs.SQL.error("Creature base stats for level {} has invalid class {}", e.level, e.klass);
                }
                creatureBaseStatsStorage.put(e.level << 8 | e.klass, e);
                count.getAndIncrement();
            });
        }

        for (int unitLevel = 1; unitLevel <= SharedDefine.DEFAULT_MAX_LEVEL + 3; ++unitLevel) {
            for (int unitClass = 1; unitClass <= SharedDefine.MAX_UNIT_CLASSES; ++unitClass) {
                int unitClassMask = 1 << (unitClass - 1);
                if (creatureBaseStatsStorage.get(unitLevel << 8 | unitClassMask) == null)
                    Logs.SQL.error("Missing base stats for creature class {} level {}", unitClass, unitLevel);
            }
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature base stats in {} ms", count, System.currentTimeMillis() - time);
    }

    public void loadCreatureModelInfo() {
        var time = System.currentTimeMillis();
        // List of model FileDataIDs that the client treats as invisible stalker
        int[] triggerCreatureModelFileID = {124640, 124641, 124642, 343863, 439302};
        AtomicInteger count = new AtomicInteger();
        try (var item = creatureRepo.streamAllCreatureModelInfo()) {
            item.forEach(e -> {

                CreatureDisplayInfo creatureDisplay = dbcObjectManager.creatureDisplayInfo(e.displayId);
                if (creatureDisplay == null) {
                    Logs.SQL.error("Table `creature_model_info` has a non-existent DisplayID (ID: {}). Skipped.", e.displayId);
                    return;
                }

                if (e.displayIdOtherGender != 0 && !dbcObjectManager.creatureDisplayInfo().contains(e.displayIdOtherGender)) {
                    Logs.SQL.error("Table `creature_model_info` has a non-existent DisplayID_Other_Gender (ID: {}) being used by DisplayID (ID: {}).", e.displayIdOtherGender, e.displayId);
                    e.displayIdOtherGender = 0;
                }

                if (e.combatReach < 0.1f) {
                    e.combatReach = ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH;
                }

                CreatureModelData creatureModelData = dbcObjectManager.creatureModelData(creatureDisplay.getModelID());
                if (creatureModelData != null) {
                    if (IntStream.range(0, triggerCreatureModelFileID.length)
                            .anyMatch(i -> creatureModelData.getFileDataID() == triggerCreatureModelFileID[i])) {
                        e.isTrigger = true;
                    }
                }
                creatureModelStorage.put(e.displayId, e);
                count.incrementAndGet();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature model based info in {} ms", count, System.currentTimeMillis() - time);
    }


    public void loadLinkedRespawn() {
        var oldMSTime = System.currentTimeMillis();
        linkedRespawnStorage.clear();
        try(var item = creatureRepo.streamAllLinkedRespawn()) {
            item.forEach(fields -> {
                int guidLow = fields[0];
                int linkedGuidLow = fields[1];
                LinkedRespawnType linkType = LinkedRespawnType.values()[(byte) fields[2]];
                ObjectGuid guid = null, linkedGuid = null;
                boolean error = false;
                switch (linkType) {
                    case CREATURE_TO_CREATURE -> {
                        SpawnData slave = getCreatureData(guidLow);
                        if (slave == null) {
                            Logs.SQL.error("LinkedRespawn: Creature (guid) '{}' not found in creature table", guidLow);
                            error = true;
                            break;
                        }

                        SpawnData master = getCreatureData(linkedGuidLow);
                        if (master == null) {
                            Logs.SQL.error("LinkedRespawn: Creature (linkedGuid) '{}' not found in creature table", linkedGuidLow);
                            error = true;
                            break;
                        }

                        MapEntry map = dbcObjectManager.map(master.mapId);
                        if (map == null || !map.isInstanceable() || (master.mapId != slave.mapId)) {
                            Logs.SQL.error("LinkedRespawn: Creature '{}' linking to Creature '{}' on an unpermitted map.", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }

                        // they must have a possibility to meet (normal/heroic difficulty)
                        if (!Objects.equals(master.spawnDifficulties, slave.spawnDifficulties)) {
                            Logs.SQL.error("LinkedRespawn: Creature '{}' linking to Creature '{}' with not corresponding spawnMask", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }

                        guid = ObjectGuid.create(HighGuid.Creature, slave.mapId, slave.id, guidLow);
                        linkedGuid = ObjectGuid.create(HighGuid.Creature, slave.mapId, master.id, linkedGuidLow);
                    }
                    case CREATURE_TO_GO -> {
                        CreatureData slave = getCreatureData(guidLow);
                        if (slave == null) {
                            Logs.SQL.error("LinkedRespawn: Creature (guid) '{}' not found in creature table", guidLow);
                            error = true;
                            break;
                        }

                        GameObjectData master = getGameObjectData(linkedGuidLow);
                        if (master == null) {
                            Logs.SQL.error("LinkedRespawn: Gameobject (linkedGuid) '{}' not found in gameobject table", linkedGuidLow);
                            error = true;
                            break;
                        }

                        MapEntry map = dbcObjectManager.map(master.mapId);
                        if (map == null || !map.isInstanceable() || (master.mapId != slave.mapId)) {
                            Logs.SQL.error("LinkedRespawn: Creature '{}' linking to Gameobject '{}' on an unpermitted map.", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }

                        // they must have a possibility to meet (normal/heroic difficulty)
                        if (!Objects.equals(master.spawnDifficulties, slave.spawnDifficulties)) {
                            Logs.SQL.error("LinkedRespawn: Creature '{}' linking to Gameobject '{}' with not corresponding spawnMask", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }
                        guid = ObjectGuid.create(HighGuid.Creature, slave.mapId, slave.id, guidLow);
                        linkedGuid = ObjectGuid.create(HighGuid.GameObject, slave.mapId, master.id, linkedGuidLow);
                    }
                    case GO_TO_GO -> {
                        GameObjectData slave = getGameObjectData(guidLow);
                        if (slave == null) {
                            Logs.SQL.error("LinkedRespawn: Gameobject (guid) '{}' not found in gameobject table", guidLow);
                            error = true;
                            break;
                        }

                        GameObjectData master = getGameObjectData(linkedGuidLow);
                        if (master == null) {
                            Logs.SQL.error("LinkedRespawn: Gameobject (linkedGuid) '{}' not found in gameobject table", linkedGuidLow);
                            error = true;
                            break;
                        }

                        MapEntry map = dbcObjectManager.map(master.mapId);
                        if (map == null || !map.isInstanceable() || (master.mapId != slave.mapId)) {
                            Logs.SQL.error("LinkedRespawn: Gameobject '{}' linking to Gameobject '{}' on an unpermitted map.", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }

                        // they must have a possibility to meet (normal/heroic difficulty)
                        if (!Objects.equals(master.spawnDifficulties, slave.spawnDifficulties)) {
                            Logs.SQL.error("LinkedRespawn: Gameobject '{}' linking to Gameobject '{}' with not corresponding spawnMask", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }
                        guid = ObjectGuid.create(HighGuid.GameObject, slave.mapId, slave.id, guidLow);
                        linkedGuid = ObjectGuid.create(HighGuid.GameObject, slave.mapId, master.id, linkedGuidLow);
                    }
                    case GO_TO_CREATURE -> {
                        GameObjectData slave = getGameObjectData(guidLow);
                        if (slave == null) {
                            Logs.SQL.error("LinkedRespawn: Gameobject (guid) '{}' not found in gameobject table", guidLow);
                            error = true;
                            break;
                        }

                        CreatureData master = getCreatureData(linkedGuidLow);
                        if (master == null) {
                            Logs.SQL.error("LinkedRespawn: Creature (linkedGuid) '{}' not found in creature table", linkedGuidLow);
                            error = true;
                            break;
                        }

                        MapEntry map = dbcObjectManager.map(master.mapId);
                        if (map == null || !map.isInstanceable() || (master.mapId != slave.mapId)) {
                            Logs.SQL.error("LinkedRespawn: Gameobject '{}' linking to Creature '{}' on an unpermitted map.", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }

                        // they must have a possibility to meet (normal/heroic difficulty)
                        if (!Objects.equals(master.spawnDifficulties, slave.spawnDifficulties)) {
                            Logs.SQL.error("LinkedRespawn: Gameobject '{}' linking to Creature '{}' with not corresponding spawnMask", guidLow, linkedGuidLow);
                            error = true;
                            break;
                        }
                        guid = ObjectGuid.create(HighGuid.GameObject, slave.mapId, slave.id, guidLow);
                        linkedGuid = ObjectGuid.create(HighGuid.Creature, slave.mapId, master.id, linkedGuidLow);
                        break;
                    }
                }
                if (!error) {
                    linkedRespawnStorage.put(guid, linkedGuid);
                }

            });
        }


        Logs.SERVER_LOADING.info(">> Loaded {} linked respawns in {} ms", linkedRespawnStorage.size(), System.currentTimeMillis()- oldMSTime);
    }

    public void loadNPCText() {
        var oldMSTime = System.currentTimeMillis();

        npcTextStorage.clear();
        AtomicInteger count = new AtomicInteger();
        try (var items = miscRepo.streamAllNpcText()) {
            items.forEach(e -> {
                if (e.id == 0) {
                    Logs.SQL.error("Table `npc_text` has record with reserved id 0, ignore.");

                    return;
                }
                NpcTextData[] data = e.data;
                for (int i = 0; i < data.length; i++) {
                    NpcTextData datum = data[i];
                    if (datum.broadcastTextID != 0 && dbcObjectManager.broadcastText(datum.broadcastTextID) == null) {
                        Logs.SQL.error("NPCText (ID: {}) has a non-existing BroadcastText (ID: {}, Index: {})", e.data, datum.broadcastTextID, i);
                        datum.probability = 0.0f;
                        datum.broadcastTextID = 0;
                        return;
                    }
                    if (datum.probability > 0 && datum.broadcastTextID == 0) {
                        Logs.SQL.error("NPCText (ID: {}) has a probability (Index: {}) set, but no BroadcastTextID to go with it", e.id, i);
                        datum.probability = 0;
                        return;
                    }
                }
                float probabilitySum = Arrays.stream(data).map(datum -> datum.probability).reduce(0f, Float::sum);
                if (probabilitySum <= 0.0f) {
                    Logs.SQL.error("NPCText (ID: {}) has a probability sum 0, no text can be selected from it, skipped.", e.id);
                    return;
                }
                npcTextStorage.put(e.id, e);
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} npc texts in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadTrainers() {
        var oldMSTime = System.currentTimeMillis();

        // For reload case
        trainers.clear();
        Map<Integer, List<TrainerSpell>> spellsByTrainer = new HashMap<>();
        try(var items = creatureRepo.streamAllTrainerSpell()) {
            items.forEach(e -> {
                SpellInfo spellInfo = spellManager.getSpellInfo(e.spellId, Difficulty.NONE);
                if (spellInfo == null)
                {
                    Logs.SQL.error("Table `trainer_spell` references non-existing spell (SpellId: {}) for TrainerId {}, ignoring", e.spellId, e.trainerId);
                    return;
                }

                if (e.reqSkillLine != 0 && dbcObjectManager.skillLine(e.reqSkillLine) == null)
                {
                    Logs.SQL.error("Table `trainer_spell` references non-existing skill (ReqSkillLine: {}) for TrainerId {} and SpellId {}, ignoring",
                            e.reqSkillLine, e.trainerId, e.spellId);
                    return;
                }

                boolean allReqValid = true;
                for (int i = 0; i < e.reqAbility.length; ++i)
                {
                    int requiredSpell = e.reqAbility[i];
                    if (requiredSpell != 0 && world.getSpellManager().getSpellInfo(requiredSpell, Difficulty.NONE) == null)
                    {
                        Logs.SQL.error("Table `trainer_spell` references non-existing spell (ReqAbility{}: {}) for TrainerId {} and SpellId {}, ignoring",
                                i + 1, requiredSpell, e.trainerId, e.spellId);
                        allReqValid = false;
                    }
                }

                if (!allReqValid)
                    return;
                spellsByTrainer.compute(e.trainerId, Functions.addToList(e));

            });
        }

        try(var items = creatureRepo.streamAllTrainer()) {
            items.forEach(e -> {
                List<TrainerSpell> spellList = spellsByTrainer.get(e.id);
                List<TrainerSpell> spells = spellList == null ? Collections.emptyList() : spellList;
                spellsByTrainer.remove(e.id);
                trainers.put(e.id, new Trainer(e.id, e.type, e.greeting, spells));
            });
        }

        for (var unusedSpells : spellsByTrainer.entrySet()) {

            for (TrainerSpell unusedSpell : unusedSpells.getValue()) {
                Logs.SQL.error("Table `trainer_spell` references non-existing trainer (TrainerId: {}) for SpellId {}, ignoring", unusedSpells.getKey(), unusedSpell.spellId);

            }
        }

        try(var items = creatureRepo.streamAllTrainerLocale()) {
            items.forEach(fields -> {
                int trainerId = (Integer)fields[0];
                String localeName = (String)fields[1];
                String greeting = (String)fields[2];
                Trainer trainer = trainers.get(trainerId);
                trainer.addGreetingLocale(Locale.valueOf(localeName), greeting);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} Trainers in {} ms", trainers.size(), System.currentTimeMillis() - oldMSTime);

    }

    public void loadCreatureTrainers() {
        var oldMSTime = System.currentTimeMillis();

        creatureDefaultTrainers.clear();

        try(var items = creatureRepo.streamAllCreatureTrainer()) {
            items.forEach(fields -> {
                int creatureId = fields[0];
                int trainerId = fields[1];
                int gossipMenuId = fields[2];
                int gossipOptionId = fields[3];

                if (getCreatureTemplate(creatureId) == null)
                {
                    Logs.SQL.error("Table `creature_trainer` references non-existing creature template (CreatureID: {}), ignoring", creatureId);
                    return;
                }

                if (getTrainer(trainerId) == null)
                {
                    Logs.SQL.error("Table `creature_trainer` references non-existing trainer (TrainerID: {}) for CreatureID {} MenuID {} OptionID {}, ignoring",
                            trainerId, creatureId, gossipMenuId, gossipOptionId);
                    return;
                }

                if (gossipMenuId != 0 || gossipOptionId != 0)
                {
                    List<GossipMenuOption> gossipMenuItems = getGossipMenuItemsMapBounds(gossipMenuId);
                    Optional<GossipMenuOption> gossipMenuOption = gossipMenuItems.stream().filter(e -> e.orderIndex == gossipOptionId).findFirst();

                    if (gossipMenuOption.isEmpty())
                    {
                        Logs.SQL.error("Table `creature_trainer` references non-existing gossip menu option (MenuID {} OptionID {}) for CreatureID {} and TrainerID {}, ignoring",
                                gossipMenuId, gossipOptionId, creatureId, trainerId);
                        return;
                    }
                }

                creatureDefaultTrainers.put(Tuple.of(creatureId, gossipMenuId, gossipMenuId), trainerId);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} default trainers in {} ms", creatureDefaultTrainers.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadVendors() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        // For reload case
        cacheVendorItemStorage.clear();

        Map<Integer, VendorItemData> tmp = new HashMap<>();
        Set<Integer> skipVendors = new HashSet<>();

        try(var items = creatureRepo.streamAllNpcVendor()) {
            items.forEach(e-> {
                var vList = tmp.compute(e.entry, Functions.ifAbsent(VendorItemData::new));
                if(e.item < 0) {
                    count.addAndGet(loadReferenceVendor(e.entry, -e.item, skipVendors, vList));
                } else {
                    if (!isVendorItemValid(e.entry, e, skipVendors)) {
                        return;
                    }
                    vList.addItem(e);
                    count.getAndIncrement();
                }
            });
        }
        cacheVendorItemStorage.putAll(tmp);
        Logs.SERVER_LOADING.info(">> Loaded {} Vendors in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadCreatures() {
        var time = System.currentTimeMillis();
        Map<Integer, CreatureData> creatureData = new HashMap<>();
        // Build single time for check spawnmask
        HashMap<Short, Set<Difficulty>> spawnMasks = new HashMap<>();
        for (MapDifficulty mapDifficulty : dbcObjectManager.mapDifficulty())
            spawnMasks.compute(mapDifficulty.getMapID(), Functions.addToSet(mapDifficulty.getDifficulty()));

        PhaseShift phaseShift = new PhaseShift();
        try(var items = creatureRepo.streamAllCreature()) {
            items.forEach(data -> {
                var cInfo = getCreatureTemplate(data.id);

                if (cInfo == null) {
                    Logs.SQL.error("Table `creature` has creature (GUID: {}) with non existing creature entry {}, skipped.", data.spawnId, data.id);
                    return;
                }

                data.creatureTemplate = cInfo;
                // transport spawns default to compatibility group
                data.spawnGroupData = isTransportMap(data.mapId) ? getLegacySpawnGroup() : getDefaultSpawnGroup();

                MapEntry mapEntry = dbcObjectManager.map(data.mapId);
                if (mapEntry == null)
                {
                    Logs.SQL.error("Table `creature` has creature (GUID: {}) that spawned at nonexistent map (Id: {}), skipped.", data.spawnId, data.mapId);
                    return;
                }

                data.scriptId       = getScriptId(data.script);
                data.spawnDifficulties = parseSpawnDifficulties(data.spawnDifficultiesText, "creature", data.spawnId, data.mapId, spawnMasks.get(data.mapId));
                data.spawnGroupData = isTransportMap(data.mapId) ? getLegacySpawnGroup() : getDefaultSpawnGroup(); // transport spawns default to compatibility group



                if (world.getWorldSettings().creatureCheckInvalidPosition) {
                    if (world.getVMapManager().isMapLoadingEnabled() && !isTransportMap(data.mapId)) {
                        Coordinate gridCoord = MapDefine.computeGridCoordinate(data.positionX, data.positionY);
                        int gx = (MapDefine.MAX_NUMBER_OF_GRIDS - 1) - gridCoord.axisX();
                        int gy = (MapDefine.MAX_NUMBER_OF_GRIDS - 1) - gridCoord.axisY();

                        LoadResult result = world.getVMapManager().existsMap(data.mapId, gx, gy);
                        if (result != LoadResult.Success) {
                            Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {} MapID: {}) spawned on a possible invalid position ({}, {}, {})",
                                    data.spawnId, data.id, data.mapId, data.positionX, data.positionY, data.positionZ);
                        }
                    }
                }

                if (data.spawnDifficulties.isEmpty()) {
                    Logs.SQL.error("Table `creature` has creature (GUID: {}) that is not spawned in any difficulty, skipped.", data.spawnId);
                    return;
                }
                if (data.display != null) {
                    if (getCreatureModelInfo(data.display.creatureDisplayId) == null) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with invalid `modelid` {}, ignoring.", data.spawnId, data.id, data.display.creatureDisplayId);
                        data.display = null;
                    }
                }

                // -1 random, 0 no equipment
                if (data.equipmentId != 0)
                {
                    if (getEquipmentInfo(data.id, data.equipmentId) == null)
                    {
                        Logs.SQL.error("Table `creature` has creature (Entry: {}) with equipment_id {} not found in table `creature_equip_template`, set to no equipment.", data.id, data.equipmentId);
                        data.equipmentId = 0;
                    }
                }

                if (cInfo.flagsExtra.hasFlag(CreatureFlagExtra.INSTANCE_BIND)) {
                    if (!mapEntry.isDungeon())
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with `creature_template`.`flags_extra` including CREATURE_FLAG_EXTRA_INSTANCE_BIND but creature is not in instance.", data.spawnId, data.id);
                }

                if (data.movementType >= MovementGeneratorType.MAX_DB_MOTION_TYPE) {
                    Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with wrong movement generator type ({}), ignored and set to IDLE.", data.spawnId, data.id, data.movementType);
                    data.movementType = (byte) MovementGeneratorType.IDLE.ordinal();
                }

                if (data.wanderDistance < 0.0f) {
                    Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with `wander_distance`< 0, set to 0.", data.spawnId, data.id);
                    data.wanderDistance = 0.0f;
                } else if (data.movementType == MovementGeneratorType.RANDOM.ordinal()) {
                    if (MathUtil.fuzzyEq(data.wanderDistance, 0.0f)) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with `MovementType`=1 (random movement) but with `wander_distance`=0, replace by idle movement type (0).", data.spawnId, data.id);
                        data.movementType = (byte) MovementGeneratorType.IDLE.ordinal();
                    }
                } else if (data.movementType == MovementGeneratorType.IDLE.ordinal()) {
                    if (data.wanderDistance != 0.0f) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with `MovementType`=0 (idle) have `wander_distance`<>0, set to 0.", data.spawnId, data.id);
                        data.wanderDistance = 0.0f;
                    }
                }

                if (data.phaseUseFlags.hasNotFlag(PhaseUseFlag.ALL)) {
                    Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) has unknown `phaseUseFlags` set, removed unknown value.", data.spawnId, data.id);
                    data.phaseUseFlags.removeNotFlag(PhaseUseFlag.ALL);
                }

                if (data.phaseUseFlags.hasFlag(PhaseUseFlag.ALWAYS_VISIBLE, PhaseUseFlag.INVERSE)) {
                    Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) has both `phaseUseFlags` PHASE_USE_FLAGS_ALWAYS_VISIBLE and PHASE_USE_FLAGS_INVERSE,"
                            + " removing PHASE_USE_FLAGS_INVERSE.", data.spawnId, data.id);
                    data.phaseUseFlags.removeFlag(PhaseUseFlag.INVERSE);
                }

                if (data.phaseGroup != 0 && data.phaseId != 0) {
                    Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) with both `phaseid` and `phasegroup` set, `phasegroup` set to 0", data.spawnId, data.id);
                    data.phaseGroup = 0;
                }

                if (data.phaseId != 0) {
                    if (!dbcObjectManager.phase().contains(data.phaseId)) {
                        Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) with `phaseid` {} does not exist, set to 0", data.spawnId, data.id, data.phaseId);
                        data.phaseId = 0;
                    }
                }

                if (data.phaseGroup != 0) {
                    List<Integer> phasesForGroup = dbcObjectManager.getPhasesForGroup(data.phaseGroup);
                    if (phasesForGroup == null || phasesForGroup.isEmpty()) {
                        Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) with `phasegroup` {} does not exist, set to 0", data.spawnId, data.id, data.phaseGroup);
                        data.phaseGroup = 0;
                    }
                }

                if (data.terrainSwapMap != -1) {
                    MapEntry terrainSwapEntry = dbcObjectManager.map(data.terrainSwapMap);
                    if (terrainSwapEntry == null) {
                        Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) with `terrainSwapMap` {} does not exist, set to -1", data.spawnId, data.id, data.terrainSwapMap);
                        data.terrainSwapMap = -1;
                    } else if (terrainSwapEntry.getParentMapID() != (short) data.mapId) {
                        Logs.SQL.error("Table `creature` have creature (GUID: {} Entry: {}) with `terrainSwapMap` {} which cannot be used on spawn map, set to -1", data.spawnId, data.id, data.terrainSwapMap);
                        data.terrainSwapMap = -1;
                    }
                }

                if (data.unitFlags != null) {
                    if (data.unitFlags.hasNotFlag(UnitFlag.ALLOWED)) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with disallowed `unit_flags` {}, removing incorrect flag.", data.spawnId, data.id, data.unitFlags);
                        data.unitFlags.removeFlag(UnitFlag.ALLOWED);
                    }
                }

                if (data.unitFlags2 != null) {
                    if (data.unitFlags2.hasNotFlag(UnitFlag2.ALLOWED)) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with disallowed `unit_flags2` {}, removing incorrect flag.", data.spawnId, data.id, data.unitFlags2);
                        data.unitFlags2.removeFlag(UnitFlag2.ALLOWED);
                    }
                }

                if (data.unitFlags3 != null) {
                    if (data.unitFlags3.hasNotFlag(UnitFlag3.ALLOWED)) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with disallowed `unit_flags3` {}, removing incorrect flag.", data.spawnId, data.id, data.unitFlags3);
                        data.unitFlags3.removeNotFlag(UnitFlag3.ALLOWED);
                    }

                    if (data.unitFlags3.hasFlag(UnitFlag3.FAKE_DEAD) && (data.unitFlags == null || !(data.unitFlags.hasFlag(UnitFlag.IMMUNE_TO_PC, UnitFlag.IMMUNE_TO_NPC)))) {
                        Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) has UNIT_FLAG3_FAKE_DEAD set without IMMUNE_TO_PC | IMMUNE_TO_NPC, removing incorrect flag.", data.spawnId, data.id);
                        data.unitFlags3.removeFlag(UnitFlag3.FAKE_DEAD);
                    }
                }

                int healthPct = Math.clamp(data.curHealthPct, 1, 100);
                if (data.curHealthPct != healthPct) {
                    Logs.SQL.error("Table `creature` has creature (GUID: {} Entry: {}) with invalid `curHealthPct` {}, set to {}.", data.spawnId, data.id, data.curHealthPct, healthPct);
                    data.curHealthPct = healthPct;
                }

                if (world.getWorldSettings().calculateGameObjectZoneAreaData) {
                    PhasingHandler.initDbVisibleMapId(phaseShift, data.terrainSwapMap);
                    ZoneAndAreaId result = world.getTerrainManager().getZoneAndAreaId(phaseShift, data.mapId, data.positionX, data.positionY, data.positionZ);
                    if (result != null) {
                        creatureRepo.updateCreatureZoneAndAreaId(result.zoneId, result.areaId, data.spawnId);
                    }
                }
                // Add to grid if not managed by the game event
                if (data.gameEvent == 0)
                    addCreatureToGrid(data);

                creatureData.put(data.spawnId, data);
            });
        }

        loadCreatureAddons(creatureData);

        creatureData.forEach((k,v)-> {
            creatureDataStorage.put(k, v);
            // Add to grid if not managed by the game event
            if (v.eventEntry == 0) {
                addCreatureToGrid(v);
            }
        });



        Logs.SERVER_LOADING.info("Loaded {} creatures in {} ms", creatureDataStorage.size(), System.currentTimeMillis() - time);
    }

    public void loadCreatureFormations() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        Set<Integer> leaderSpawnIds = new HashSet<>();
        //Get group data
        try(var items = creatureRepo.streamAllCreatureFormations()) {
            items.forEach(data -> {
                if (getCreatureData(data.leaderSpawnId) == null)
                {
                    Logs.SQL.error("creature_formations table leader guid {} incorrect (not exist)", data.leaderSpawnId);
                    return;
                }

                if (getCreatureData(data.memberSpawnId) == null)
                {
                    Logs.SQL.error("creature_formations table member guid {} incorrect (not exist)", data.memberSpawnId);
                    return;
                }
                leaderSpawnIds.add(data.leaderSpawnId);
                //If creature is group leader we may skip loading of dist/angle
                if (data.leaderSpawnId == data.memberSpawnId)
                {
                    data.followDist             = 0.0f;
                    data.followAngle            = 0.0f;
                }

                creatureGroupMap.put(data.leaderSpawnId, data);
                count.getAndIncrement();
            });
        }

        for (var leaderSpawnId : leaderSpawnIds) {
            if (!creatureGroupMap.containsKey(leaderSpawnId)) {
                Logs.SQL.error("creature_formation contains leader spawn {} which is not included on its formation, removing", leaderSpawnId);
                creatureGroupMap.entrySet().removeIf(entry -> entry.getValue().leaderSpawnId == leaderSpawnId);
            }
        }

        Logs.SQL.info(">> Loaded {} creatures in formations in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }


    public GridSpawnData getCellPersonalObjectGuids(int mapId, Difficulty spawnMode, int cellId, int phaseId) {
        return mapPersonalSpawnDataStorage.get(PersonalCellSpawnDataKey.of(mapId, spawnMode, cellId, phaseId));
    }

    public void addCreatureToGrid(CreatureData data) {
        addSpawnDataToGrid(data);
    }

    public void removeCreatureFromGrid(CreatureData data) {
        removeSpawnDataFromGrid(data);
    }

    public List<Integer> getCreatureQuestItemList(int id, Difficulty difficulty) {
        return creatureQuestItemStorage.get(Pair.of(id, difficulty));
    }


    public CreatureTemplate getCreatureTemplate(int entry) {
        return creatureTemplateStorage.get(entry);
    }

    public int getCreatureDefaultTrainer(int creatureId) {
        return getCreatureTrainerForGossipOption(creatureId, 0, 0);
    }

    public int getCreatureTrainerForGossipOption(int creatureId, int gossipMenuId, int gossipOptionIndex) {
        return creatureDefaultTrainers.get(Tuple.of(creatureId, gossipMenuId, gossipOptionIndex));
    }



    public CreatureData getCreatureData(int spawnId) {
        return creatureDataStorage.get(spawnId);
    }

    public ObjectGuid getLinkedRespawnGuid(ObjectGuid spawnId) {
        var retGuid = linkedRespawnStorage.get(spawnId);

        if (retGuid.isEmpty()) {
            return ObjectGuid.EMPTY;
        }

        return retGuid;
    }

    public FormationInfo getFormationInfo(int spawnId) {
        return creatureGroupMap.get(spawnId);
    }

    public void addFormationMember(int spawnId, float followAng, float followDist, int leaderSpawnId, short groupAI) {
        FormationInfo member = new FormationInfo();
        member.leaderSpawnId = leaderSpawnId;
        member.followDist = followDist;
        member.followAngle = followAng;
        member.groupAI = EnumFlag.of(GroupAIFlag.class, groupAI);

        for (var i = 0; i < 2; ++i) {
            member.leaderWaypointIds[i] = 0;
        }

        creatureGroupMap.put(member.memberSpawnId, member);
    }

    public boolean setCreatureLinkedRespawn(int guidLow, int linkedGuidLow) {
        if (guidLow == 0) {
            return false;
        }

        var master = getCreatureData(guidLow);
        var guid = ObjectGuid.create(HighGuid.Creature, master.getMapId(), master.id, guidLow);

        if (linkedGuidLow == 0) // we're removing the linking
        {
            linkedRespawnStorage.remove(guid);
            creatureRepo.deleteLinkedRespawn(guidLow, LinkedRespawnType.CREATURE_TO_CREATURE.ordinal());
            return true;
        }

        var slave = getCreatureData(linkedGuidLow);

        if (slave == null) {
            Logs.SQL.error("Creature '{}' linking to non-existent creature '{}'.", guidLow, linkedGuidLow);

            return false;
        }



        var map = dbcObjectManager.map(master.getMapId());

        if (map == null || !map.isInstanceable() || (master.getMapId() != slave.getMapId())) {
            Logs.SQL.error("Creature '{}' linking to '{}' on an unpermitted map.", guidLow, linkedGuidLow);

            return false;
        }

        // they must have a possibility to meet (normal/heroic difficulty)
        if (!master.spawnDifficulties.containsAll(slave.spawnDifficulties)) {
            Logs.SQL.error("LinkedRespawn: Creature '{}' linking to '{}' with not corresponding spawnMask", guidLow, linkedGuidLow);

            return false;
        }

        var linkedGuid = ObjectGuid.create(HighGuid.Creature, slave.getMapId(), slave.id, linkedGuidLow);

        linkedRespawnStorage.put(guid, linkedGuid);

        creatureRepo.replaceLinkedRespawn(guidLow, linkedGuidLow, LinkedRespawnType.CREATURE_TO_CREATURE.ordinal());
        return true;
    }

    public CreatureData newOrExistCreatureData(int spawnId) {
        if (!creatureDataStorage.containsKey(spawnId)) {
            creatureDataStorage.put(spawnId, new CreatureData());
        }

        return creatureDataStorage.get(spawnId);
    }

    public void deleteCreatureData(int spawnId) {
        var data = getCreatureData(spawnId);

        if (data != null) {
            removeCreatureFromGrid(data);
            onDeleteSpawnData(data);
        }

        creatureDataStorage.remove(spawnId);
    }

    public CreatureBaseStats getCreatureBaseStats(int level, int unitClass) {
        int id = level << 8 | unitClass;
        var stats = creatureBaseStatsStorage.get(id);

        if (stats != null) {
            return stats;
        }

        return null;
    }

    public CreatureModelInfo getCreatureModelRandomGender(final CreatureModel model, CreatureTemplate creatureTemplate) {
        var modelInfo = getCreatureModelInfo(model.creatureDisplayId);

        if (modelInfo == null) {
            return null;
        }

        // If a model for another gender exists, 50% chance to use it
        if (modelInfo.displayIdOtherGender != 0 && RandomUtil.randomInt(0, 2) == 0) {
            var minfoTmp = getCreatureModelInfo(modelInfo.displayIdOtherGender);

            if (minfoTmp == null) {
                Logs.SQL.error("Model (Entry: {}) has modelid_other_gender {} not found in table `creature_model_info`. ", model.creatureDisplayId, modelInfo.displayIdOtherGender);
            } else {
                // DisplayID changed
                model.creatureDisplayId = modelInfo.displayIdOtherGender;
                if (creatureTemplate != null) {
                    creatureTemplate.models.stream()
                            .filter(templateModel -> templateModel.creatureDisplayId == modelInfo.displayIdOtherGender)
                            .findFirst().ifPresent(e -> {
                        model.creatureDisplayId = e.creatureDisplayId;
                        model.probability = e.probability;
                        model.displayScale = e.displayScale;
                    });
                }

                return minfoTmp;
            }
        }

        return modelInfo;
    }

    public CreatureModelInfo getCreatureModelInfo(int modelId) {
        return creatureModelStorage.get(modelId);
    }


    public NpcText getNpcText(int textId) {
        return npcTextStorage.get(textId);
    }

    //GameObjects
    public void loadGameObjectTemplate() {
        var time = System.currentTimeMillis();
        for (GameObjectEntry db2go : dbcObjectManager.gameObject()) {
            GameObjectTemplate go = new GameObjectTemplate();

            go.entry = db2go.getId();
            go.type = GameObjectType.values()[db2go.getTypeID()];
            go.displayId = db2go.getDisplayID();
            go.name = db2go.getName();
            go.size = db2go.getScale();

            int[] propValues = db2go.getPropValues();
            System.arraycopy(propValues, 0, go.raw, 0, propValues.length);

            go.requiredLevel = 0;
            go.scriptId = 0;

            gameObjectTemplateStorage.put(db2go.getId(), go);
        }

        try(var items = gameObjectRepo.streamAllGameObjectTemplate()) {
            items.forEach(got -> {
                switch (got.type) {
                    case DOOR: //0
                        if (got.door.open != 0) {
                            checkGOLockId(got, got.door.open, 1);
                        }

                        checkGONoDamageImmuneId(got, got.door.noDamageImmune, 3);

                        break;
                    case BUTTON: //1
                        if (got.button.open != 0) {
                            checkGOLockId(got, got.button.open, 1);
                        }

                        checkGONoDamageImmuneId(got, got.button.noDamageImmune, 4);

                        break;
                    case QUEST_GIVER: //2
                        if (got.questGiver.open != 0) {
                            checkGOLockId(got, got.questGiver.open, 0);
                        }

                        checkGONoDamageImmuneId(got, got.questGiver.noDamageImmune, 5);

                        break;
                    case CHEST: //3
                        if (got.chest.open != 0) {
                            checkGOLockId(got, got.chest.open, 0);
                        }

                        checkGOConsumable(got, got.chest.consumable, 3);

                        if (got.chest.linkedTrap != 0) // linked trap
                        {
                            checkGOLinkedTrapId(got, got.chest.linkedTrap, 7);
                        }

                        break;
                    case TRAP: //6
                        if (got.trap.open != 0) {
                            checkGOLockId(got, got.trap.open, 0);
                        }

                        break;
                    case CHAIR: //7

                        if(checkAndFixGOChairHeightId(got, got.chair.chairheight, 1)) {
                            got.chair.chairheight = 0;
                        }
                        break;
                    case SPELL_FOCUS: //8
                        if (got.spellFocus.spellFocusType != 0) {
                            if (!dbcObjectManager.spellFocusObject().contains(got.spellFocus.spellFocusType)) {
                                Logs.SQL.error("GameObject (Entry: {} GoType: {}) have data0={} but SpellFocus (Id: {}) not exist.", got.entry, got.type, got.spellFocus.spellFocusType, got.spellFocus.spellFocusType);
                            }
                        }

                        if (got.spellFocus.linkedTrap != 0) // linked trap
                        {
                            checkGOLinkedTrapId(got, got.spellFocus.linkedTrap, 2);
                        }

                        break;
                    case GOOBER: //10
                        if (got.goober.open != 0) {
                            checkGOLockId(got, got.goober.open, 0);
                        }

                        checkGOConsumable(got, got.goober.consumable, 3);

                        if (got.goober.pageID != 0) // pageId
                        {
                            if (getPageText(got.goober.pageID) == null) {
                                Logs.SQL.error("GameObject (Entry: {} GoType: {}) have data7={} but PageText (Entry {}) not exist.", got.entry, got.type, got.goober.pageID, got.goober.pageID);
                            }
                        }

                        checkGONoDamageImmuneId(got, got.goober.noDamageImmune, 11);

                        if (got.goober.linkedTrap != 0) // linked trap
                        {
                            checkGOLinkedTrapId(got, got.goober.linkedTrap, 12);
                        }

                        break;
                    case AREA_DAMAGE: //12
                        if (got.areaDamage.open != 0) {
                            checkGOLockId(got, got.areaDamage.open, 0);
                        }

                        break;
                    case CAMERA: //13
                        if (got.camera.open != 0) {
                            checkGOLockId(got, got.camera.open, 0);
                        }

                        break;
                    case MAP_OBJ_TRANSPORT: //15
                    {
                        if (got.moTransport.taxiPathID != 0) {

                            List<TaxiPathNode> taxiPathNodes = dbcObjectManager.getTaxiPathNodesByPath().get(got.moTransport.taxiPathID);
                            if (taxiPathNodes == null || taxiPathNodes.isEmpty()) {
                                Logs.SQL.error("GameObject (Entry: {} GoType: {}) have data0={} but TaxiPath (Id: {}) not exist.", got.entry, got.type, got.moTransport.taxiPathID, got.moTransport.taxiPathID);
                            }
                        }

                        var transportMap = got.moTransport.spawnMap;

                        if (transportMap != 0) {
                            transportMaps.add((short) transportMap);
                        }

                        break;
                    }
                    case SPELL_CASTER: //22
                        // always must have spell
                        checkGOSpellId(got, got.spellCaster.spell, 0);

                        break;
                    case FLAG_STAND: //24
                        if (got.flagStand.open != 0) {
                            checkGOLockId(got, got.flagStand.open, 0);
                        }

                        checkGONoDamageImmuneId(got, got.flagStand.noDamageImmune, 5);

                        break;
                    case FISHING_HOLE: //25
                        if (got.fishingHole.open != 0) {
                            checkGOLockId(got, got.fishingHole.open, 4);
                        }

                        break;
                    case FLAG_DROP: //26
                        if (got.flagDrop.open != 0) {
                            checkGOLockId(got, got.flagDrop.open, 0);
                        }

                        checkGONoDamageImmuneId(got, got.flagDrop.noDamageImmune, 3);

                        break;
                    case BARBER_CHAIR: //32

                        checkAndFixGOChairHeightId(got, got.barberChair.chairheight, 0);

                        if (got.barberChair.sitAnimKit != 0 && dbcObjectManager.animKit(got.barberChair.sitAnimKit) == null) {
                            Logs.SQL.error("GameObject (Entry: {} GoType: {}) have data0={} but SpellFocus (Id: {}) not exist.", got.entry, got.type, got.barberChair.sitAnimKit, got.barberChair.sitAnimKit);

                            got.barberChair.sitAnimKit = 0;
                        }

                        break;
                    case GARRISON_BUILDING: {
                        var transportMap = got.garrisonBuilding.spawnMap;

                        if (transportMap != 0) {
                            transportMaps.add((short) transportMap);
                        }
                    }

                    break;
                    case GATHERING_NODE:
                        if (got.gatheringNode.open != 0) {
                            checkGOLockId(got, got.gatheringNode.open, 0);
                        }

                        if (got.gatheringNode.linkedTrap != 0) {
                            checkGOLinkedTrapId(got, got.gatheringNode.linkedTrap, 20);
                        }

                        break;
                }
            });
        };

        Logs.SERVER_LOADING.info(">> Loaded {} game object templates in {} ms", gameObjectTemplateStorage.size(), System.currentTimeMillis() - time);


    }

    public void loadGameObjectTemplateAddons() {
        var oldMSTime = System.currentTimeMillis();

        AtomicInteger count = new AtomicInteger();

        try(var items = gameObjectRepo.streamAllGameObjectTemplateAddons()) {
            items.forEach(gameObjectAddon -> {
                int entry = gameObjectAddon.entry;

                GameObjectTemplate got = getGameObjectTemplate(entry);

                int[] artKits = gameObjectAddon.artKits;
                for (int i = 0; i < artKits.length; i++) {
                    int artKit = artKits[i];
                    if (artKit == 0)
                        continue;

                    if (!dbcObjectManager.gameObjectArtKit().contains(artKit)) {
                        Logs.SQL.error("GameObject (Entry: {}) has invalid `artkit{}` ({}) defined, set to zero instead.", entry, i, artKit);
                        artKits[i] = 0;
                    }

                }

                // checks
                if (gameObjectAddon.faction != 0 && !dbcObjectManager.factionTemplate().contains(gameObjectAddon.faction)) {
                    Logs.SQL.error("GameObject (Entry: {}) has invalid faction ({}) defined in `gameobject_template_addon`.", entry, gameObjectAddon.faction);
                }

                if (gameObjectAddon.maxgold > 0) {
                    switch (got.type) {
                        case CHEST:
                        case FISHING_HOLE:
                            break;
                        default:
                            Logs.SQL.error("GameObject (Entry {} GoType: {}) cannot be looted but has maxgold set in `gameobject_template_addon`.", entry, got.type);

                            break;
                    }
                }

                if (gameObjectAddon.worldEffectId != 0 && !dbcObjectManager.worldEffect().contains(gameObjectAddon.worldEffectId)) {
                    Logs.SQL.error("GameObject (Entry: {}) has invalid WorldEffectID ({}) defined in `gameobject_template_addon`, set to 0.", entry, gameObjectAddon.worldEffectId);
                    gameObjectAddon.worldEffectId = 0;
                }

                if (gameObjectAddon.aiAnimKitId != 0 && !dbcObjectManager.animKit().contains(gameObjectAddon.aiAnimKitId)) {
                    Logs.SQL.error("GameObject (Entry: {}) has invalid AIAnimKitID ({}) defined in `gameobject_template_addon`, set to 0.", entry, gameObjectAddon.aiAnimKitId);
                    gameObjectAddon.aiAnimKitId = 0;
                }

                gameObjectTemplateAddonStorage.put(entry, gameObjectAddon);
                count.incrementAndGet();
            });

            Logs.SERVER_LOADING.info(">> Loaded {} game object template addons in {} ms", count, System.currentTimeMillis() - oldMSTime);
        }

    }

    public void loadGameObjectOverrides() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = gameObjectRepo.streamAllGameObjectOverrides()) {
            items.forEach(gameObjectOverride -> {
                var goData = getGameObjectData(gameObjectOverride.spawnId);

                if (goData == null) {
                    Logs.SQL.error("GameObject (SpawnId: {}) does not exist but has a record in `gameobject_overrides`", gameObjectOverride.spawnId);
                    return;
                }

                gameObjectOverrideStorage.put(gameObjectOverride.spawnId, gameObjectOverride);

                if (gameObjectOverride.faction != 0 && !dbcObjectManager.factionTemplate().contains(gameObjectOverride.faction)) {
                    Logs.SQL.error(String.format("GameObject (SpawnId: %1$s) has invalid faction (%2$s) defined in `gameobject_overrides`.", gameObjectOverride.spawnId, gameObjectOverride.faction));
                }

                count.incrementAndGet();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} gameobject faction and flags overrides in {} ms", count, System.currentTimeMillis() - (oldMSTime));
    }

    public void loadGameObjects() {
        var time = System.currentTimeMillis();

        AtomicInteger count = new AtomicInteger();
        // build single time for check spawnmask
        HashMap<Integer, Set<Difficulty>> spawnMasks = new HashMap<>();
        for (MapDifficulty mapDifficulty : dbcObjectManager.mapDifficulty())
            spawnMasks.compute(mapDifficulty.getMapID().intValue(), Functions.addToSet(mapDifficulty.getDifficulty()));

        PhaseShift phaseShift = new PhaseShift();

        try(var items = gameObjectRepo.streamAllGameObject()) {
            items.forEach(data -> {
                var gInfo = getGameObjectTemplate(data.id);

                if (gInfo == null) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {}) with non existing gameobject entry {}, skipped.", data.spawnId, data.id);

                    return;
                }

                if (gInfo.displayId == 0) {
                    switch (gInfo.type) {
                        case TRAP:
                        case SPELL_FOCUS:
                            break;
                        default:
                            Logs.SQL.error("Gameobject (GUID: {} Entry {} GoType: {}) doesn't have a displayId ({}), not loaded.", data.spawnId, data.id, gInfo.type, gInfo.displayId);

                            break;
                    }
                }

                if (gInfo.displayId != 0 && !dbcObjectManager.gameObjectDisplayInfo().contains(gInfo.displayId)) {
                    Logs.SQL.error("Gameobject (GUID: {} Entry {} GoType: {}) has an invalid displayId ({}), not loaded.", data.spawnId, data.id, gInfo.type, gInfo.displayId);

                    return;
                }

                data.setSpawnGroupData(isTransportMap(data.getMapId()) ? getLegacySpawnGroup() : getDefaultSpawnGroup()); // transport spawns default to compatibility group

                var mapEntry = dbcObjectManager.map(data.getMapId());

                if (mapEntry == null) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) spawned on a non-existed map (Id: {}), skip", data.spawnId, data.id, data.getMapId());

                    return;
                }

                if(world.getWorldSettings().gameObjectCheckInvalidPosition) {
                    if (world.getVMapManager().isMapLoadingEnabled() && !isTransportMap(data.mapId))
                    {
                        Coordinate gridCoord = MapDefine.computeGridCoordinate(data.positionX, data.positionY);
                        int gx = (MapDefine.MAX_NUMBER_OF_GRIDS - 1) - gridCoord.axisX();
                        int gy = (MapDefine.MAX_NUMBER_OF_GRIDS - 1) - gridCoord.axisY();

                        LoadResult result = world.getVMapManager().existsMap(data.mapId, gx, gy);
                        if (result != LoadResult.Success) {
                            Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {} MapID: {}) spawned on a possible invalid position ({})",
                                    data.spawnId, data.id, data.mapId, data.positionString());
                        }
                    }
                }

                if (data.spawnTimeSecs == 0 && gInfo.isDespawnAtAction()) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with `spawntimesecs` (0) value, but the gameobejct is marked as despawnable at action.", data.spawnId, data.id);
                }


                data.artKit = 0;



                if (data.goState == null) {
                    if (gInfo.type != GameObjectType.TRANSPORT || data.goState.value > GOState.TRANSPORT_ACTIVE.value + SharedDefine.MAX_GO_STATE_TRANSPORT_STOP_FRAMES) {
                        Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid `state` ({}) value, skip", data.spawnId, data.id, data.goState);

                        return;
                    }
                }


                data.spawnDifficulties = parseSpawnDifficulties(data.spawnDifficultiesText, "gameobject", data.id, data.getMapId(), spawnMasks.get(data.getMapId()));

                if (data.spawnDifficulties.isEmpty()) {
                    Logs.SQL.error(String.format("Table `creature` has creature (GUID: %1$s) that is not spawned in any difficulty, skipped.", data.id));

                    return;
                }


                if (data.phaseUseFlags.hasNotFlag(PhaseUseFlag.ALL)) {
                    Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) has unknown `phaseUseFlags` set, removed unknown second.", data.spawnId, data.id);
                    data.phaseUseFlags.removeNotFlag(PhaseUseFlag.ALL);
                }

                if (data.phaseUseFlags.hasFlag(PhaseUseFlag.ALWAYS_VISIBLE) && data.phaseUseFlags.hasFlag(PhaseUseFlag.INVERSE)) {
                    Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) has both `phaseUseFlags` PHASE_USE_FLAGS_ALWAYS_VISIBLE and PHASE_USE_FLAGS_INVERSE," + " removing PHASE_USE_FLAGS_INVERSE.", data.id, data.id);

                    data.phaseUseFlags.removeFlag(PhaseUseFlag.INVERSE);
                }

                if (data.phaseGroup != 0 && data.phaseId != 0) {
                    Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) with both `phaseid` and `phasegroup` set, `phasegroup` set to 0", data.spawnId, data.id);
                    data.phaseGroup = 0;
                }

                if (data.phaseId != 0) {
                    if (!dbcObjectManager.phase().contains(data.phaseId)) {
                        Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) with `phaseid` {} does not exist, set to 0", data.spawnId, data.id, data.phaseId);
                        data.phaseId = 0;
                    }
                }

                if (data.phaseGroup != 0) {
                    if (dbcObjectManager.getPhasesForGroup(data.phaseGroup).isEmpty()) {
                        Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) with `phaseGroup` {} does not exist, set to 0", data.spawnId, data.id, data.phaseGroup);
                        data.phaseGroup = 0;
                    }
                }



                if (data.terrainSwapMap != -1) {
                    var terrainSwapEntry = dbcObjectManager.map(data.terrainSwapMap);

                    if (terrainSwapEntry == null) {
                        Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) with `terrainSwapMap` {} does not exist, set to -1", data.spawnId, data.id, data.terrainSwapMap);
                        data.terrainSwapMap = -1;
                    } else if (terrainSwapEntry.getParentMapID() != data.getMapId()) {
                        Logs.SQL.error("Table `gameobject` have gameobject (GUID: {} Entry: {}) with `terrainSwapMap` {} which cannot be used on spawn map, set to -1", data.spawnId, data.id, data.terrainSwapMap);
                        data.terrainSwapMap = -1;
                    }
                }

                data.scriptId = getScriptId(data.script);

                if (data.rotation.x < -1.0f || data.rotation.x > 1.0f) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid rotationX ({}) second, skip", data.spawnId, data.id, data.rotation.x);

                    return;
                }

                if (data.rotation.y < -1.0f || data.rotation.y > 1.0f) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid rotationY ({}) second, skip", data.spawnId, data.id, data.rotation.y);

                    return;
                }

                if (data.rotation.z < -1.0f || data.rotation.z > 1.0f) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid rotationZ ({}) second, skip", data.spawnId, data.id, data.rotation.z);

                    return;
                }

                if (data.rotation.w < -1.0f || data.rotation.w > 1.0f) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid rotationW ({}) second, skip", data.spawnId, data.id, data.rotation.w);

                    return;
                }

                if (!world.getMapManager().isValidMapCoordinate(data.getMapId(), data.positionX, data.positionY, data.positionZ)) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid coordinates, skip", data.spawnId, data.id);

                    return;
                }


                if (!data.rotation.isUnit()) {
                    Logs.SQL.error("Table `gameobject` has gameobject (GUID: {} Entry: {}) with invalid rotation quaternion (non-unit), defaulting to orientation on Z axis only", data.spawnId, data.id);
                    data.rotation.setEulerAngles(data.positionO, 0.0f, 0.0f);
                }


                if (world.getWorldSettings().calculateGameObjectZoneAreaData) {
                    PhasingHandler.initDbVisibleMapId(phaseShift, data.terrainSwapMap);
                    ZoneAndAreaId result = world.getTerrainManager().getZoneAndAreaId(phaseShift, data.mapId, data.positionX, data.positionY, data.positionZ);
                    if (result != null) {
                        gameObjectRepo.updateGameObjectZoneAndAreaId(result.zoneId, result.areaId, data.spawnId);
                    }
                }

                // if not this is to be managed by GameEvent System
                if (data.gameEvent == 0) {
                    addGameObjectToGrid(data);
                }

                gameObjectDataStorage.put(data.spawnId, data);
                count.incrementAndGet();
            });
            Logs.SERVER_LOADING.info(">> Loaded {} gameobjects in {} ms", count, System.currentTimeMillis() - time);
        }

    }

    public void loadGameObjectAddons() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        gameObjectAddonStorage.clear();

        try(var items = gameObjectRepo.streamAllGameObjectAddons()) {
            items.forEach(data -> {

                var goData = getGameObjectData(data.guid);
                if (goData == null)
                {
                    Logs.SQL.error("GameObject (GUID: {}) does not exist but has a record in `gameobject_addon`", data.guid);
                    return;
                }

                if (data.invisibilityType.ordinal() > 0 && data.invisibilityValue == 0)
                {
                    Logs.SQL.error("GameObject (GUID: {}) has InvisibilityType set but has no InvisibilityValue in `gameobject_addon`, set to 1", data.guid);
                    data.invisibilityValue = 1;
                }

                if (!data.parentRotation.isUnit())
                {
                    Logs.SQL.error("GameObject (GUID: {}) has invalid parent rotation in `gameobject_addon`, set to default", data.guid);
                    data.parentRotation = new QuaternionData();
                }

                if (data.worldEffectID != 0 && !dbcObjectManager.worldEffect().contains(data.worldEffectID))
                {
                    Logs.SQL.error("GameObject (GUID: {}) has invalid WorldEffectID ({}) in `gameobject_addon`, set to 0.", data.guid, data.worldEffectID);
                    data.worldEffectID = 0;
                }

                if (data.aiAnimKitID != 0 && !dbcObjectManager.animKit().contains(data.aiAnimKitID))
                {
                    Logs.SQL.error("GameObject (GUID: {}) has invalid AIAnimKitID ({}) in `gameobject_addon`, set to 0.", data.guid, data.aiAnimKitID);
                    data.aiAnimKitID = 0;
                }


                gameObjectAddonStorage.put(data.guid, data);
                count.incrementAndGet();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} gameobject addons in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadGameObjectQuestItems() {
        var oldMSTime = System.currentTimeMillis();
        var count = new AtomicInteger();

        try(var items = gameObjectRepo.streamAllGameObjectQuestItem()) {
            items.forEach(fields -> {

                int entry = fields[0];
                int item  = fields[1];
                int idx   = fields[2];


                GameObjectTemplate goInfo = getGameObjectTemplate(entry);
                if (goInfo == null)
                {
                    Logs.SQL.error("Table `gameobject_questitem` has data for nonexistent gameobject (entry: {}, idx: {}), skipped", entry, idx);
                    return;
                };



                if (!dbcObjectManager.item().contains(item))
                {
                    Logs.SQL.error("Table `gameobject_questitem` has nonexistent item (ID: {}) in gameobject (entry: {}, idx: {}), skipped", item, entry, idx);
                    return;
                };
                gameObjectQuestItemStorage.compute(entry, Functions.addToList(item));
                count.incrementAndGet();

            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} gameobject quest items in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadGameObjectForQuests() {
        var oldMSTime = System.currentTimeMillis();

        gameObjectForQuestStorage.clear(); // need for reload case

        if (getGameObjectTemplates().isEmpty()) {
            Logs.SERVER_LOADING.info("Loaded 0 GameObjects for quests");

            return;
        }

        int count = 0;

        // collect GO entries for GO that must activated
        for (var pair : getGameObjectTemplates().entrySet()) {
            switch (pair.getValue().type) {
                case GameObjectType.QUEST_GIVER:
                    break;
                case GameObjectType.CHEST: {
                    // scan GO chest with loot including quest items
                    // find quest loot for GO
                    if (pair.getValue().chest.questID != 0 || LootStorage.GAMEOBJECT.haveQuestLootFor(pair.getValue().chest.chestLoot) || LootStorage.GAMEOBJECT.haveQuestLootFor(pair.getValue().chest.chestPersonalLoot) || LootStorage.GAMEOBJECT.haveQuestLootFor(pair.getValue().chest.chestPushLoot)) {
                        break;
                    }

                    continue;
                }
                case GameObjectType.GENERIC: {
                    if (pair.getValue().generic.questID > 0) //quests objects
                    {
                        break;
                    }

                    continue;
                }
                case GameObjectType.GOOBER: {
                    if (pair.getValue().goober.questID > 0) //quests objects
                    {
                        break;
                    }

                    continue;
                }
                case GameObjectType.GATHERING_NODE: {
                    // scan GO chest with loot including quest items
                    // find quest loot for GO
                    if (LootStorage.GAMEOBJECT.haveQuestLootFor(pair.getValue().gatheringNode.chestLoot)) {
                        break;
                    }

                    continue;
                }
                default:
                    continue;
            }

            gameObjectForQuestStorage.add(pair.getValue().entry);
            ++count;
        }

        Logs.SERVER_LOADING.info(">> Loaded {} GameObjects for quests in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void addGameObjectToGrid(GameObjectData data) {
        addSpawnDataToGrid(data);
    }

    public void removeGameObjectFromGrid(GameObjectData data) {
        removeSpawnDataFromGrid(data);
    }

    public GameObjectAddon getGameObjectAddon(int lowguid) {
        return gameObjectAddonStorage.get(lowguid);
    }

    public List<Integer> getGameObjectQuestItemList(int id) {
        return gameObjectQuestItemStorage.get(id);
    }

    public HashMap<Integer, GameObjectData> getAllGameObjectData() {
        return gameObjectDataStorage;
    }

    public GameObjectData getGameObjectData(int spawnId) {
        return gameObjectDataStorage.get(spawnId);
    }

    public void deleteGameObjectData(int spawnId) {
        var data = getGameObjectData(spawnId);

        if (data != null) {
            removeGameObjectFromGrid(data);
            onDeleteSpawnData(data);
        }

        gameObjectDataStorage.remove(spawnId);
    }

    public GameObjectData newOrExistGameObjectData(int spawnId) {
        if (!gameObjectDataStorage.containsKey(spawnId)) {
            gameObjectDataStorage.put(spawnId, new GameObjectData());
        }

        return gameObjectDataStorage.get(spawnId);
    }

    public GameObjectTemplate getGameObjectTemplate(int entry) {
        return gameObjectTemplateStorage.get(entry);
    }

    public GameObjectTemplateAddon getGameObjectTemplateAddon(int entry) {
        return gameObjectTemplateAddonStorage.get(entry);
    }

    public GameObjectOverride getGameObjectOverride(int spawnId) {
        return gameObjectOverrideStorage.get(spawnId);
    }

    public HashMap<Integer, GameObjectTemplate> getGameObjectTemplates() {
        return gameObjectTemplateStorage;
    }

    public boolean isGameObjectForQuests(int entry) {
        return gameObjectForQuestStorage.contains(entry);
    }

    //Items
    public void loadItemTemplates() {
        var oldMSTime = System.currentTimeMillis();
        int sparseCount = 0;

        for (var sparse : dbcObjectManager.itemSparse()) {
            var db2Data = dbcObjectManager.item(sparse.getId());

            if (db2Data == null) {
                continue;
            }

            var itemTemplate = new ItemTemplate(db2Data, sparse);
            ItemClass classID = ItemClass.values()[db2Data.getClassID()];
            InventoryType inventoryType = InventoryType.values()[sparse.getInventoryType()];
            int maxDurability = fillMaxDurability(classID, db2Data.getSubclassID(), inventoryType, ItemQuality.values()[sparse.getOverallQualityID()], sparse.getItemLevel());
            itemTemplate.setMaxDurability(maxDurability);

            var itemSpecOverrides = dbcObjectManager.getItemSpecOverrides(sparse.getId());

            if (itemSpecOverrides != null) {
                for (var itemSpecOverride : itemSpecOverrides) {
                    var specialization = dbcObjectManager.chrSpecialization(itemSpecOverride.getSpecID());

                    if (specialization != null) {
                        itemTemplate.setItemSpecClassMask(itemTemplate.getItemSpecClassMask() | 1 << (specialization.getClassID() - 1));
                        itemTemplate.getSpecializations()[0].set(ItemTemplate.calculateItemSpecBit(specialization), true);

                        itemTemplate.getSpecializations()[1].or(itemTemplate.getSpecializations()[0]);
                        itemTemplate.getSpecializations()[2].or(itemTemplate.getSpecializations()[0]);
                    }
                }
            } else {
                ItemSpecStats itemSpecStats = new ItemSpecStats(db2Data, sparse, dbcObjectManager);
                for (var itemSpec : dbcObjectManager.itemSpec()) {


                    if (itemSpecStats.itemType != itemSpec.getItemType()) {
                        continue;
                    }

                    ItemSpecStat primaryStat = ItemSpecStat.values()[itemSpec.getPrimaryStat()];
                    ItemSpecStat secondaryStat = ItemSpecStat.values()[itemSpec.getSecondaryStat()];
                    var hasPrimary = primaryStat == ItemSpecStat.NONE;
                    var hasSecondary = secondaryStat == ItemSpecStat.NONE;

                    for (int i = 0; i < itemSpecStats.itemSpecStatCount; ++i) {
                        if (itemSpecStats.itemSpecStatTypes[i] == primaryStat) {
                            hasPrimary = true;
                        }

                        if (itemSpecStats.itemSpecStatTypes[i] == secondaryStat) {
                            hasSecondary = true;
                        }
                    }

                    if (!hasPrimary || !hasSecondary) {
                        continue;
                    }

                    var specialization = dbcObjectManager.chrSpecialization(itemSpec.getSpecializationID());

                    if (specialization != null) {
                        if (((1 << (specialization.getClassID() - 1)) & sparse.getAllowableClass()) != 0) {
                            itemTemplate.setItemSpecClassMask(itemTemplate.getItemSpecClassMask() | 1 << (specialization.getClassID() - 1));
                            var specBit = ItemTemplate.calculateItemSpecBit(specialization);
                            itemTemplate.getSpecializations()[0].set(specBit, true);

                            if (itemSpec.getMaxLevel() > 40) {
                                itemTemplate.getSpecializations()[1].set(specBit, true);
                            }

                            if (itemSpec.getMaxLevel() >= 110) {
                                itemTemplate.getSpecializations()[2].set(specBit, true);
                            }
                        }
                    }
                }
            }

            // Items that have no specializations set can be used by everyone
            for (var specs : itemTemplate.getSpecializations()) {
                if (specs.isEmpty()) {
                    specs.set(0, specs.size(), true);
                }
            }

            ++sparseCount;
            itemTemplateStorage.put(sparse.getId(), itemTemplate);
        }

        // Load item effects (spells)
        for (var effectEntry : dbcObjectManager.itemEffect()) {
            var item = itemTemplateStorage.get(effectEntry.getParentItemID());

            if (item != null) {
                item.getEffects().add(effectEntry);
            }
        }

        Logs.SERVER_LOADING.info(">> Loaded {} item templates in {} ms", sparseCount, System.currentTimeMillis() - oldMSTime);
    }

    public void loadItemTemplateAddon() {
        var time = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = itemRepo.streamAllItemTemplateAddons()) {
            items.forEach(fields -> {
                var itemId = fields[0];
                var itemTemplate = getItemTemplate(itemId);

                if (itemTemplate == null) {
                    Logs.SQL.error("Item {} specified in `item_template_addon` does not exist, skipped.", itemId);

                    return;
                }

                var minMoneyLoot = fields[3];
                var maxMoneyLoot = fields[4];

                if (minMoneyLoot > maxMoneyLoot) {
                    Logs.SQL.error("Minimum money loot specified in `item_template_addon` for item {} was greater than maximum amount, swapping.", itemId);
                    var temp = minMoneyLoot;
                    minMoneyLoot = maxMoneyLoot;
                    maxMoneyLoot = temp;
                }
                itemTemplate.setFlagsCu(EnumFlag.of(ItemFlagsCustom.class, fields[1]));
                itemTemplate.setFoodType(fields[2]);
                itemTemplate.setMinMoneyLoot(minMoneyLoot);
                itemTemplate.setMaxMoneyLoot(maxMoneyLoot);
                itemTemplate.setSpellPPMRate(fields[5]);
                itemTemplate.setRandomBonusListTemplateId(fields[6]);
                count.incrementAndGet();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} item addon templates in {} ms", count, System.currentTimeMillis() - (time));
    }

    public void loadItemScriptNames() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();

        try (var items = itemRepo.streamAllItemScriptNames()) {
            items.forEach(fields -> {
                var itemId = (Integer) fields[0];
                var scriptName = (String) fields[1];

                if (getItemTemplate(itemId) == null) {
                    Logs.SQL.error("Item {} specified in `item_script_names` does not exist, skipped.", itemId);
                    return;
                }

                itemTemplateStorage.get(itemId).setScriptId(getScriptId(scriptName));
                count.incrementAndGet();
            });
        }

        Logs.SERVER_LOADING.info("Loaded {} item script names in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public ItemTemplate getItemTemplate(int itemId) {
        return itemTemplateStorage.get(itemId);
    }

    public HashMap<Integer, ItemTemplate> getItemTemplates() {
        return itemTemplateStorage;
    }

    public Trainer getTrainer(int trainerId) {
        return trainers.get(trainerId);
    }

    public void addVendorItem(int entry, VendorItem vItem) {
        addVendorItem(entry, vItem, true);
    }

    public void addVendorItem(int entry, VendorItem vItem, boolean persist) {
        var vList = cacheVendorItemStorage.get(entry);
        vList.addItem(vItem);

        if (persist) {
            creatureRepo.insertNpcVendor(entry, vItem.item, vItem.maxcount, vItem.incrtime, vItem.extendedCost, vItem.type.ordinal());
        }
    }

    public boolean removeVendorItem(int entry, int item, ItemVendorType type) {
        return removeVendorItem(entry, item, type, true);
    }

    public boolean removeVendorItem(int entry, int item, ItemVendorType type, boolean persist) {
        var iter = cacheVendorItemStorage.get(entry);

        if (iter == null) {
            return false;
        }

        if (!iter.removeItem(item, type)) {
            return false;
        }

        if (persist) {
            creatureRepo.deleteNpcVendor(entry, item, type.ordinal());
        }

        return true;
    }

    public boolean isVendorItemValid(int vendorentry, VendorItem vItem, Player player, Set<Integer> skipvendors) {
        return isVendorItemValid(vendorentry, vItem, player, skipvendors, 0);
    }

    public boolean isVendorItemValid(int vendorentry, VendorItem vItem, Set<Integer> skipvendors) {
        return isVendorItemValid(vendorentry, vItem, null, skipvendors, 0);
    }

    public boolean isVendorItemValid(int vendorentry, VendorItem vItem) {
        return isVendorItemValid(vendorentry, vItem, null, null, 0);
    }

    public boolean isVendorItemValid(int vendorEntry, VendorItem vItem, Player player, Set<Integer> skipVendors, int orNpcFlag) {
        var cInfo = getCreatureTemplate(vendorEntry);

        if (cInfo == null) {
            if (player != null) {
                player.sendSysMessage(SysMessage.LANG_COMMAND_VENDORSELECTION);
            } else {
                Logs.SQL.error("Table `(game_event_)npc_vendor` has data for nonexistent creature template (Entry: {}), ignore", vendorEntry);
            }
            return false;
        }

        if (!EnumFlag.of(NPCFlag.class, orNpcFlag).addFlag(cInfo.npcFlag).hasFlag(NPCFlag.VENDOR)) {
            if (skipVendors == null || skipVendors.isEmpty()) {
                if (player != null) {
                    player.sendSysMessage(SysMessage.LANG_COMMAND_VENDORSELECTION);
                } else {
                    Logs.SQL.error("Table `(game_event_)npc_vendor` has data for creature template (Entry: {}) without vendor flag, ignore", vendorEntry);
                }

                if (skipVendors != null) {
                    skipVendors.add(vendorEntry);
                }
            }

            return false;
        }

        if ((vItem.type == ItemVendorType.ITEM && getItemTemplate(vItem.item) == null)
                || (vItem.type == ItemVendorType.CURRENCY && dbcObjectManager.currencyType(vItem.item) == null)) {
            if (player != null) {
                player.sendSysMessage(SysMessage.LANG_ITEM_NOT_FOUND, vItem.item, vItem.type);
            } else {
                Logs.SQL.error("Table `(game_event_)npc_vendor` for Vendor (Entry: {}) have in item list non-existed item ({}, type {}), ignore", vendorEntry, vItem.item, vItem.type);
            }

            return false;
        }

        if (vItem.playerConditionId != 0 && !dbcObjectManager.playerCondition().contains(vItem.playerConditionId)) {
            Logs.SQL.error("Table `(game_event_)npc_vendor` has Item (Entry: {}) with invalid PlayerConditionId ({}) for vendor ({}), ignore", vItem.item, vItem.playerConditionId, vendorEntry);

            return false;
        }

        if (vItem.extendedCost != 0 && !dbcObjectManager.itemExtendedCost().contains(vItem.extendedCost)) {
            if (player != null) {
                player.sendSysMessage(SysMessage.LANG_EXTENDED_COST_NOT_EXIST, vItem.extendedCost);
            } else {
                Logs.SQL.error("Table `(game_event_)npc_vendor` has Item (Entry: {}) with wrong ExtendedCost ({}) for vendor ({}), ignore", vItem.item, vItem.extendedCost, vendorEntry);
            }

            return false;
        }

        if (vItem.type == ItemVendorType.ITEM) // not applicable to currencies
        {
            if (vItem.maxcount > 0 && vItem.incrtime == 0) {
                if (player != null) {
                    player.sendSysMessage("MaxCount != 0 ({}) but IncrTime == 0", vItem.maxcount);
                } else {
                    Logs.SQL.error("Table `(game_event_)npc_vendor` has `maxcount` ({}) for item {} of vendor (Entry: {}) but `incrtime`=0, ignore", vItem.maxcount, vItem.item, vendorEntry);
                }

                return false;
            } else if (vItem.maxcount == 0 && vItem.incrtime > 0) {
                if (player != null) {
                    player.sendSysMessage("MaxCount == 0 but IncrTime<>= 0");
                } else {
                    Logs.SQL.error("Table `(game_event_)npc_vendor` has `maxcount`=0 for item {} of vendor (Entry: {}) but `incrtime`<>0, ignore", vItem.item, vendorEntry);
                }

                return false;
            }

            for (var bonusListId : vItem.bonusListIDs) {
                if (dbcObjectManager.getItemBonusList(bonusListId).isEmpty()) {
                    Logs.SQL.error("Table `(game_event_)npc_vendor` have Item (Entry: {}) with invalid bonus {} for vendor ({}), ignore", vItem.item, bonusListId, vendorEntry);

                    return false;
                }
            }
        }

        var vItems = getNpcVendorItemList(vendorEntry);

        if (vItems == null) {
            return true; // later checks for non-empty lists
        }

        if (vItems.findItemCostPair(vItem.item, vItem.extendedCost, vItem.type) != null) {
            if (player != null) {
                player.sendSysMessage(SysMessage.LANG_ITEM_ALREADY_IN_LIST, vItem.item, vItem.extendedCost, vItem.type);
            } else {
                Logs.SQL.error("Table `npc_vendor` has duplicate items {} (with extended cost {}, type {}) for vendor (Entry: {}), ignoring", vItem.item, vItem.extendedCost, vItem.type, vendorEntry);
            }

            return false;
        }

        if (vItem.type == ItemVendorType.CURRENCY && vItem.maxcount == 0) {
            Logs.SQL.error("Table `(game_event_)npc_vendor` have Item (Entry: {}, type: {}) with missing maxcount for vendor ({}), ignore", vItem.item, vItem.type, vendorEntry);

            return false;
        }

        return true;
    }

    public VendorItemData getNpcVendorItemList(int entry) {
        return cacheVendorItemStorage.get(entry);
    }

    public CreatureMovementData getCreatureMovementOverride(int spawnId) {
        return creatureMovementOverrides.get(spawnId);
    }


    public EquipmentInfo getEquipmentInfo(int entry, byte id) {
        var equip = equipmentInfoStorage.get(entry);

        if (equip.isEmpty()) {
            return null;
        }

        if (id == -1) {
            Byte random = RandomUtil.random(equip.keySet().stream().toList());
            return equip.get(random);
        } else {
            return equip.get(id);
        }
    }

    //Maps
    public void loadInstanceTemplate() {
        var oldMSTime = System.currentTimeMillis();

        try (var instanceTemplates = miscRepo.streamAllInstanceTemplate()) {
            instanceTemplates.forEach(e -> {
                if (!world.getMapManager().isValidMap(e.map)) {
                    Logs.SQL.error("ObjectMgr::LoadInstanceTemplate: bad mapid {} for template!", e.map);
                    return;
                }
                instanceTemplateStorage.put(e.map, e);
            });
        }
        if (instanceTemplateStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 instance templates. DB table `instance_template` is empty!");

        } else {
            Logs.SERVER_LOADING.info(">> Loaded {} instance templates in {} ms", instanceTemplateStorage.size(),
                    System.currentTimeMillis() - oldMSTime);

        }
    }

    public void loadGameTeleport() {
        var oldMSTime = System.currentTimeMillis();

        gameTeleStorage.clear();

        try (var gameTeleports = miscRepo.streamAllGameTeleport()) {
            gameTeleports.forEach(gt -> {
                if (!world.getMapManager().isValidMapCoordinate(gt.mapId, gt.posX, gt.posY, gt.posZ, gt.orientation)) {
                    Logs.SQL.error("Wrong position for id {} (name: {}) in `game_tele` table, ignoring.", gt.id, gt.name);
                    return;
                }
                gameTeleStorage.put(gt.id, gt);
            });
        }
        if (gameTeleStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 GameTeleports. DB table `game_tele` is empty!");

        } else {
            Logs.SERVER_LOADING.info(">> Loaded {} GameTeleports in {} ms", gameTeleStorage.size(),
                    System.currentTimeMillis() - oldMSTime);
        }
    }


    public void loadAreaTriggerTemplates() {
        var oldMSTime = System.currentTimeMillis();

        areaTriggerTemplatesStorage.clear(); // need for reload case

        Map<AreaTriggerId, List<Vector2>> verticesByCreateProperties = new HashMap<>();
        Map<AreaTriggerId, List<Vector2>> verticesTargetByCreateProperties = new HashMap<>();
        Map<AreaTriggerId, List<Position>> splinesByCreateProperties = new HashMap<>();
        Map<AreaTriggerId, List<AreaTriggerAction>> actionsByAreaTrigger = new HashMap<>();


        try (var items = areaTriggerRepo.streamAllAreaTriggerTemplateActions()) {
            items.forEach(data -> {

                if (data.actionType == null) {
                    Logs.SQL.error("Table `areatrigger_template_actions` has invalid ActionType {} for AreaTriggerId ({}, {}) and Param {}",
                            data.actionType, data.id, data.id.isServerSide, data.param);
                    return;

                }

                if (data.targetType == null) {
                    Logs.SQL.error("Table `areatrigger_template_actions` has invalid TargetType {} for AreaTriggerId ({}, {}) and Param {}",
                            data.targetType, data.id, data.id.isServerSide, data.param);
                    return;
                }

                if (data.actionType == AreaTriggerActionType.TELEPORT) {
                    if (!dbcObjectManager.worldSafeLoc().contains(data.param)) {
                        Logs.SQL.error("Table `areatrigger_template_actions` has invalid entry for AreaTriggerId ({},{}) with TargetType=Teleport and Param ({}) not a valid world safe loc entry",
                                data.id, data.id.isServerSide, data.param);
                        return;
                    }
                }
                actionsByAreaTrigger.compute(data.id, Functions.addToList(data));
            });
        }


        try (var items = areaTriggerRepo.streamAllAreaTriggerCreatePropertiesPolygonVertex()) {
            items.forEach(field -> {

                AreaTriggerId id = new AreaTriggerId((Integer) field[0], (Boolean) field[1]);
                verticesByCreateProperties.compute(id, Functions.addToList(new Vector2((Float) field[3], (Float) field[4])));

                if (field[5] != null && field[6] != null)
                    verticesTargetByCreateProperties.compute(id, Functions.addToList(new Vector2((Float) field[5], (Float) field[6])));
                else if (field[5] != null || field[6] != null)
                    Logs.SQL.error("Table `areatrigger_create_properties_polygon_vertex` has listed invalid target vertices (AreaTriggerCreatePropertiesId: (Id: {}, IsCustom: {}), Index: {}).",
                            id.id, id.isServerSide, field[2]);

            });
        }

        try (var items = areaTriggerRepo.streamAllAreaTriggerCreatePropertiesSplinePoint()) {
            items.forEach(field -> {
                AreaTriggerId id = new AreaTriggerId((Integer) field[0], (Boolean) field[1]);
                splinesByCreateProperties.compute(id, Functions.addToList(new Position((Float) field[2], (Float) field[3], (Float) field[4])));
            });
        }


        try (var items = areaTriggerRepo.streamAllAreaTriggerTemplate()) {
            items.forEach(item -> {
                areaTriggerTemplatesStorage.put(item.id, item);
            });
        }


        try (var items = areaTriggerRepo.streamAllAreaTriggerCreateProperties()) {
            items.forEach(item -> {

                AreaTriggerTemplate template = areaTriggerTemplatesStorage.get(item.templateId);


                if (null != template) {

                    Logs.SQL.error("Table `areatrigger_create_properties` references invalid AreaTrigger (Id: {}, IsCustom: {}) for AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {})",
                            item.id, item.id.isServerSide, item.templateId, item.templateId.isServerSide);
                    return;
                }


                BiConsumer<Integer, Runnable> curveCheck = (cure, rester) -> {
                    if (cure != 0 && !dbcObjectManager.curve().contains(cure)) {
                        Logs.SQL.error("Table `areatrigger_create_properties` has listed AreaTrigger (Id: {}, IsCustom: {}) for AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with invalid #Curve ({}), set to 0!",
                                item.id, item.id.isServerSide, item.templateId, item.templateId.isServerSide, cure);
                        rester.run();
                    }
                };

                curveCheck.accept(item.MoveCurveId, () -> item.MoveCurveId = 0);
                curveCheck.accept(item.ScaleCurveId, () -> item.ScaleCurveId = 0);
                curveCheck.accept(item.MorphCurveId, () -> item.MorphCurveId = 0);
                curveCheck.accept(item.FacingCurveId, () -> item.FacingCurveId = 0);

                if (item.SpellForVisuals != null) {
                    if (null == spellManager.getSpellInfo(item.SpellForVisuals, Difficulty.NONE)) {
                        Logs.SQL.error("Table `areatrigger_create_properties` has AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with invalid SpellForVisual {}, set to none.", item.id, item.id.isServerSide, item.SpellForVisuals);
                        item.SpellForVisuals = null;
                    }
                }

                item.ScriptId = getScriptId(item.scriptName);


                if (item.Shape.Type == AreaTriggerShapeType.Polygon) {
                    if (item.Shape.PolygonDatas.Height <= 0.0f) {
                        item.Shape.PolygonDatas.Height = 1.0f;
                        if (item.Shape.PolygonDatas.HeightTarget <= 0.0f)
                            item.Shape.PolygonDatas.HeightTarget = 1.0f;
                    }
                }


                item.Shape.PolygonVertices = verticesByCreateProperties.get(item.id);
                item.Shape.PolygonVerticesTarget = verticesTargetByCreateProperties.get(item.id);

                if (!item.Shape.PolygonVerticesTarget.isEmpty() && item.Shape.PolygonVertices.size() != item.Shape.PolygonVerticesTarget.size()) {

                    Logs.SQL.error("Table `areatrigger_create_properties_polygon_vertex` has invalid target vertices, either all or none vertices must have a corresponding target vertex (AreaTriggerCreatePropertiesId: (Id: {}, IsCustom: {})).",
                            item.id, item.id.isServerSide);
                    item.Shape.PolygonVerticesTarget.clear();
                }

                item.SplinePoints = splinesByCreateProperties.get(item.id);

                areaTriggerCreateProperties.put(item.id, item);

            });
        }


        var rs = worldCrudRepo.queryForRowSet("SELECT AreaTriggerCreatePropertiesId, IsCustom, StartDelay, CircleRadius, BlendFromRadius, InitialAngle, ZOffset, CounterClockwise, CanLoop FROM `areatrigger_create_properties_orbit`");
        while (rs.next()) {

            AreaTriggerId areaTriggerId = new AreaTriggerId(rs.getInt(1), rs.getBoolean(2));

            AreaTriggerCreateProperty createProperty = areaTriggerCreateProperties.get(areaTriggerId);
            if (createProperty == null) {
                Logs.SQL.error("Table `areatrigger_create_properties_orbit` reference invalid AreaTriggerCreatePropertiesId: (Id: {}, IsCustom: {})",
                        areaTriggerId.id, areaTriggerId.isServerSide);
                continue;
            }

            AreaTriggerOrbitInfo data = new AreaTriggerOrbitInfo();
            data.startDelay = rs.getInt(3);
            data.radius = rs.getFloat(4);
            data.blendFromRadius = rs.getFloat(5);
            data.initialAngle = rs.getFloat(6);
            data.ZOffset = rs.getFloat(7);
            data.counterClockwise = rs.getBoolean(8);
            data.canLoop = rs.getBoolean(9);

            createProperty.OrbitInfo = data;
        }

        Logs.SERVER_LOADING.info(">> Loaded {} spell areatrigger templates in {} ms.", areaTriggerTemplatesStorage.size(), System.currentTimeMillis() - oldMSTime);
    }


    void loadAreaTriggerSpawns() {
        // build single time for check spawnmask
        Map<Integer, Set<Difficulty>> spawnMasks = new HashMap<>();
        for (var mapDifficulty : dbcObjectManager.mapDifficulty())
            spawnMasks.compute(Integer.valueOf(mapDifficulty.getMapID()), Functions.addToSet(mapDifficulty.getDifficulty()));

        long oldMSTime = System.currentTimeMillis();
        //                                                                            0        1                              2         3      4                  5     6     7     8            9              10       11          12
        var rs = worldCrudRepo.queryForRowSet("SELECT SpawnId, AreaTriggerCreatePropertiesId, IsCustom, MapId, SpawnDifficulties, PosX, PosY, PosZ, Orientation, PhaseUseFlags, PhaseId, PhaseGroup, ScriptName FROM `areatrigger`");
        while (rs.next()) {
            var spawnId = rs.getInt(1);
            var createPropertiesId = new AreaTriggerId(rs.getInt(2), rs.getBoolean(3));

            var location = new WorldLocation(rs.getInt(4), rs.getFloat(6), rs.getFloat(7), rs.getFloat(8), rs.getFloat(9));

            AreaTriggerCreateProperty createProperties = areaTriggerCreateProperties.get(createPropertiesId);
            if (null == createProperties) {
                Logs.SQL.error("Table `areatrigger` has listed AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) that doesn't exist for SpawnId {}",
                        createPropertiesId.id, createPropertiesId.isServerSide, spawnId);
                continue;
            }

            if (!createProperties.Flags.equals(AreaTriggerCreatePropertiesFlag.NONE)) {
                Logs.SQL.error("Table `areatrigger` has listed AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with non - zero flags",
                        createPropertiesId.id, createPropertiesId.isServerSide);
                continue;
            }

            if (createProperties.ScaleCurveId != 0 || createProperties.MorphCurveId != 0 || createProperties.FacingCurveId != 0 || createProperties.MoveCurveId != 0) {
                Logs.SQL.error("Table `areatrigger` has listed AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with curve values",
                        createPropertiesId.id, createPropertiesId.isServerSide);
                continue;
            }

            if (createProperties.TimeToTargetScale != 0) {
                Logs.SQL.error("Table `areatrigger` has listed AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with time to target values",
                        createPropertiesId.id, createPropertiesId.isServerSide);
                continue;
            }

            if (createProperties.OrbitInfo != null) {
                Logs.SQL.error("Table `areatrigger` has listed AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with orbit info",
                        createPropertiesId.id, createPropertiesId.isServerSide);
                continue;
            }

            if (createProperties.HasSplines()) {
                Logs.SQL.error("Table `areatrigger` has listed AreaTriggerCreatePropertiesId (Id: {}, IsCustom: {}) with splines",
                        createPropertiesId.id, createPropertiesId.isServerSide);
                continue;
            }

            if (!world.getMapManager().isValidMapCoordinate(location)) {
                Logs.SQL.error("Table `areatrigger` has listed an invalid position: SpawnId: {}, MapId {}, Position {{}}",
                        spawnId, location.getMapId(), location.toString());
                continue;
            }

            var difficulties = parseSpawnDifficulties(rs.getString(5), "areatrigger", spawnId, location.getMapId(), spawnMasks.get(location.getMapId()));
            if (difficulties.isEmpty()) {
                Logs.SQL.debug("Table `areatrigger` has areatrigger (GUID: {}) that is not spawned in any difficulty, skipped.", spawnId);
                continue;
            }

            AreaTriggerSpawn spawn = new AreaTriggerSpawn();
            spawn.spawnId = spawnId;
            spawn.mapId = location.getMapId();
            spawn.triggerId = createPropertiesId;
            spawn.positionX = location.getX();
            spawn.positionY = location.getY();
            spawn.positionZ = location.getZ();
            spawn.positionO = location.getO();

            spawn.phaseUseFlags = EnumFlag.of(PhaseUseFlag.class, rs.getByte(10));
            spawn.phaseId = rs.getInt(11);
            spawn.phaseGroup = rs.getInt(12);

            spawn.scriptId = getScriptId(rs.getString(13));
            spawn.spawnGroupData = getLegacySpawnGroup();

            // Add the trigger to a map::cell map, which is later used by GridLoader to query

            // if not this is to be managed by GameEvent System
            if (spawn.gameEvent == 0) {
                addSpawnDataToGrid(spawn);
            }

            areaTriggerSpawnsBySpawnId.put(spawn.spawnId, spawn);

        }
        Logs.SERVER_LOADING.info("Loaded {} areatrigger spawns in {} ms.", areaTriggerSpawnsBySpawnId.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadAreaTriggerTeleports() {
        var oldMSTime = System.currentTimeMillis();

        areaTriggerStorage.clear(); // need for reload case


        try (var items = areaTriggerRepo.streamAllAreaTriggerTeleport()) {
            items.forEach(fields -> {
                int Trigger_ID = fields[0];
                int PortLocID  = fields[1];

                WorldSafeLocEntry portLoc = getWorldSafeLoc(PortLocID);
                if (null == portLoc)
                {
                    Logs.SQL.error("Area Trigger (ID: {}) has a non-existing Port Loc (ID: {}) in WorldSafeLocs.dbc, skipped", Trigger_ID, PortLocID);
                    return;
                }

                AreaTriggerStruct at = new AreaTriggerStruct();

                at.target_mapId       = portLoc.loc.getMapId();
                at.target_X           = portLoc.loc.getX();
                at.target_Y           = portLoc.loc.getY();
                at.target_Z           = portLoc.loc.getZ();
                at.target_Orientation = portLoc.loc.getO();

                AreaTriggerEntry atEntry = dbcObjectManager.areaTrigger(Trigger_ID);
                if (null == atEntry)
                {
                    Logs.SQL.error("Area Trigger (ID: {}) does not exist in AreaTrigger.dbc.", Trigger_ID);
                    return;
                }

                areaTriggerStorage.put(Trigger_ID, at);
            });
        }
    }

    public void loadAccessRequirements() {
        Logs.SERVER_LOADING.info("Loading access requirement definitions...");
        long oldMSTime = System.currentTimeMillis();

        accessRequirementStorage.clear();

        AtomicInteger count = new AtomicInteger();
        try (var result = playerRepo.streamAllAccessRequirements()) {

            result.forEach(item -> {
                if (dbcObjectManager.map().contains(item.getMapId()))
                {
                    Logs.SQL.error("Map {} referenced in `access_requirement` does not exist, skipped.", item.getMapId());
                    return;
                }

                if (null == dbcObjectManager.getMapDifficultyData(item.getMapId(), item.getDifficulty()))
                {
                    Logs.SQL.error("Map {} referenced in `access_requirement` does not have difficulty {}, skipped", item.getMapId(), item.getDifficulty());
                    return;
                }
                //the type of mapId is short
                int requirementId = item.getMapId() << 8 | item.getDifficulty().ordinal();

                if (item.getItem() != 0) {
                    var pProto = getItemTemplate(item.getItem());
                    if (pProto == null) {
                        Logs.SQL.error("Key item {} does not exist for map {} difficulty {}, removing key requirement.", item.getItem(), item.getMapId(), item.getDifficulty());
                        item.setItem(0);
                    }
                }

                if (item.getItem2() != 0) {
                    var pProto = getItemTemplate(item.getItem2());
                    if (pProto == null) {
                        Logs.SQL.error("Second item {} does not exist for map {} difficulty {}, removing key requirement.", item.getItem2(),  item.getMapId(), item.getDifficulty());
                        item.setItem2(0);
                    }
                }

                if (item.getQuestDoneA() != 0) {
                    if (getQuestTemplate(item.getQuestDoneA()) == null) {
                        Logs.SQL.error("Required Alliance Quest {} not exist for map {} difficulty {}, remove quest done requirement.", item.getQuestDoneA(),  item.getMapId(), item.getDifficulty());
                        item.setQuestDoneA(0);
                    }
                }

                if (item.getQuestDoneH() != 0) {
                    if (getQuestTemplate(item.getQuestDoneH()) == null) {
                        Logs.SQL.error("Required Horde Quest {} not exist for map {} difficulty {}, remove quest done requirement.", item.getQuestDoneH(),  item.getMapId(), item.getDifficulty());
                        item.setQuestDoneH(0);
                    }
                }

                if (item.getCompletedAchievement() != 0) {
                    if (!dbcObjectManager.achievement().contains(item.getCompletedAchievement())) {
                        Logs.SQL.error("Required Achievement {} not exist for map {} difficulty {}, remove quest done requirement.", item.getCompletedAchievement(),  item.getMapId(), item.getDifficulty());
                        item.setCompletedAchievement(0);
                    }
                }

                accessRequirementStorage.put(requirementId, item);
                count.incrementAndGet();
            });

        }

        Logs.SERVER_LOADING.info(">> Loaded {} access requirement definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }


    public void loadSpawnGroupTemplates() {
        var oldMSTime = System.currentTimeMillis();

        try(var items = miscRepo.streamAllSpawnGroupTemplate()) {
            items.forEach(e -> {
                if (e.flags.hasNotFlag(SpawnGroupFlag.ALL))
                {
                    int orig = e.flags.getFlag();
                    e.flags.removeNotFlag(SpawnGroupFlag.ALL);
                    Logs.SQL.error("Invalid spawn group flag {} on group ID {} ({}), reduced to valid flag {}.", e.flags, e.groupId, e.name, orig);
                }
                if (e.flags.hasFlag(SpawnGroupFlag.SYSTEM, SpawnGroupFlag.MANUAL_SPAWN))
                {

                    e.flags.removeNotFlag(SpawnGroupFlag.MANUAL_SPAWN);
                    Logs.SQL.error("System spawn group {} ({}) has invalid manual spawn flag. Ignored.", e.groupId, e.name);
                }
                spawnGroupDataStorage.put(e.groupId, e);
            });
        }



        if (!spawnGroupDataStorage.containsKey(0)) {
            Logs.SQL.error("Default spawn group (index 0) is missing from DB! Manually inserted.");
            SpawnGroupTemplateData data = new SpawnGroupTemplateData();
            data.groupId = 0;
            data.name = "Default Group";
            data.mapId = 0;
            data.flags = EnumFlag.of(SpawnGroupFlag.SYSTEM);
            spawnGroupDataStorage.put(0, data);
        }

        if (!spawnGroupDataStorage.containsKey(1)) {
            Logs.SQL.error("Default legacy spawn group (index 1) is missing from DB! Manually inserted.");
            SpawnGroupTemplateData data = new SpawnGroupTemplateData();
            data.groupId = 0;
            data.name = "Legacy Group";
            data.mapId = 0;
            data.flags = EnumFlag.of(SpawnGroupFlag.SYSTEM , SpawnGroupFlag.COMPATIBILITY_MODE);
            spawnGroupDataStorage.put(1, data);
        }

        Logs.SERVER_LOADING.info(">> Loaded {} spawn group templates in {} ms", spawnGroupDataStorage.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadSpawnGroups() {
        var oldMSTime = System.currentTimeMillis();


        AtomicInteger count = new AtomicInteger();
        try (var result = miscRepo.streamAllSpawnGroups()) {

            result.forEach(fields -> {
                int groupId = fields[0];
                SpawnObjectType spawnType = SpawnObjectType.values()[fields[1]];

                if (!SpawnData.typeIsValid(spawnType)) {
                    Logs.SQL.error("Spawn data with invalid type {} listed for spawn group {}. Skipped.", spawnType, groupId);
                    return;
                }
                int spawnId = fields[2];
                SpawnMetadata data = getSpawnMetadata(spawnType, spawnId);

                if (data == null) {
                    Logs.SQL.error("Spawn data with ID ({},{}) not found, but is listed as a member of spawn group {}!", spawnType, spawnId, groupId);
                    return;
                } else if (data.spawnGroupData.groupId == 0) {
                    Logs.SQL.error("Spawn with ID ({},{}) is listed as a member of spawn group {}, but is already a member of spawn group {}. Skipping.", spawnType, spawnId, groupId, data.spawnGroupData.groupId);
                    return;
                }
                if (!spawnGroupDataStorage.containsKey(groupId)) {
                    Logs.SQL.error("Spawn group {} assigned to spawn ID ({},{}) but group is found!", groupId, spawnType, spawnId);
                } else {
                    SpawnGroupTemplateData groupTemplate = spawnGroupDataStorage.get(groupId);
                    if (groupTemplate.mapId == MapDefine.SPAWN_GROUP_MAP_UNSET) {
                        groupTemplate.mapId = data.mapId;
                        spawnGroupsByMap.compute(data.mapId, Functions.addToList(groupId));
                    } else if (groupTemplate.mapId != data.mapId && !(groupTemplate.flags.hasFlag(SpawnGroupFlag.SYSTEM))) {
                        Logs.SQL.error("Spawn group {} has map ID {}, but spawn ({},{}) has map id {} - spawn NOT added to group!", groupId, groupTemplate.mapId, spawnType, spawnId, data.mapId);
                        return;
                    }
                    data.spawnGroupData = groupTemplate;
                    if (!groupTemplate.flags.hasFlag(SpawnGroupFlag.SYSTEM)) {
                        spawnGroupMapStorage.compute(groupId, Functions.addToList(data));
                    }
                    count.incrementAndGet();
                }

            });

            Logs.SERVER_LOADING.info(">> Loaded {} spawn group members in {} ms", count, System.currentTimeMillis() - oldMSTime);


        }
    }

    public void loadInstanceSpawnGroups() {
        Logs.SERVER_LOADING.info("Loading instance spawn groups...");
        long oldMSTime = System.currentTimeMillis();

        AtomicInteger count = new AtomicInteger();
        try (var result = miscRepo.streamAllInstanceSpawnGroups()) {

            result.forEach (item -> {
                int instanceMapId = item.getInstanceMapId();
                int bossStateId = item.getBossStateId();
                int bossStates = item.getBossStates();
                int spawnGroupId = item.getSpawnGroupId();


                var spawnGroupTemplate = spawnGroupDataStorage.get(spawnGroupId);

                if (spawnGroupTemplate == null || spawnGroupTemplate.flags.hasFlag(SpawnGroupFlag.SYSTEM)) {
                    Logs.SQL.error("Invalid spawn group {} specified for instance {}. Skipped.", spawnGroupId, bossStateId);
                    return;
                }

                if (spawnGroupTemplate.mapId != instanceMapId) {
                    Logs.SQL.error("Instance spawn group {} specified for instance {} has spawns on a different map {}. Skipped.",
                            spawnGroupId, instanceMapId, instanceMapId);
                    return;
                }



                byte ALL_STATES = (byte) ((1 << EncounterState.TO_BE_DECIDED.ordinal()) - 1);

                if ((bossStates & ~ALL_STATES) != 0) {
                    item.setBossStates((byte) (bossStates & ALL_STATES));
                    Logs.SQL.error("Instance spawn group ({},{}) had invalid boss state mask {} - truncated to {}.", instanceMapId, spawnGroupId, bossStates, item.getBossStates());
                }


                var flags = item.getFlags();
                if ((flags.hasNotFlag(InstanceSpawnGroup.Flag.FLAG_ALL))) {
                    var oldFlags = EnumFlag.of(flags);
                    flags.removeNotFlag(InstanceSpawnGroup.Flag.FLAG_ALL);
                    Logs.SQL.error("Instance spawn group ({},{}) had invalid flags {} - truncated to {}.", instanceMapId, spawnGroupId, oldFlags, flags);
                }

                if (flags.hasFlag(InstanceSpawnGroup.Flag.FLAG_ALLIANCE_ONLY) && flags.hasFlag(InstanceSpawnGroup.Flag.FLAG_HORDE_ONLY)) {

                    flags.removeFlag(InstanceSpawnGroup.Flag.FLAG_ALL, InstanceSpawnGroup.Flag.FLAG_HORDE_ONLY);
                    Logs.SQL.error("Instance spawn group ({},{}) FLAG_ALLIANCE_ONLY and FLAG_HORDE_ONLY may not be used together in a single entry - truncated to {}.", instanceMapId, spawnGroupId, flags);
                }

                instanceSpawnGroupStorage.compute(instanceMapId, Functions.addToList(item));
                count.incrementAndGet();
            });
        }

        Logs.SERVER_LOADING.info("Loaded {} instance spawn groups in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public HashMap<Integer, InstanceTemplate> getInstanceTemplates() {
        return instanceTemplateStorage;
    }

    public InstanceTemplate getInstanceTemplate(int mapID) {
        return instanceTemplateStorage.get(mapID);
    }

    public GameTele getGameTele(int id) {
        return gameTeleStorage.get(id);
    }

    public GameTele getGameTele(String name) {


        // Alternative first GameTele what contains wnameLow as substring in case no GameTele location found
        GameTele alt = null;


        for (var tele : gameTeleStorage.values()) {

            if (StringUtil.containsIgnoreCase(tele.name, name)) {
                return tele;
            } else if (StringUtil.containsIgnoreCase(tele.name, name)) {
                alt = tele;
            }
        }
        return alt;
    }

    public GameTele getGameTeleExactName(String name) {
        return gameTeleStorage.values()
                .stream()
                .filter(tele -> StringUtil.equalsIgnoreCase(tele.name, name))
                .findFirst()
                .orElse(null);
    }

    public boolean addGameTele(GameTele tele) {
        // find max id
        Integer maxId = gameTeleStorage.keySet().stream().max(Integer::compare).orElse(0);
        // use next
        tele.id = ++maxId;
        gameTeleStorage.put(maxId, tele);
        miscRepo.addGameTele(tele);

        return true;
    }

    public boolean deleteGameTele(String name) {
        for (var entry : gameTeleStorage.entrySet()) {
            if (StringUtil.equalsIgnoreCase(entry.getValue().name, name)) {
                gameTeleStorage.remove(entry.getKey());
                miscRepo.deleteGameTele(entry.getValue());
                return true;
            }
        }
        return false;
    }


    public boolean isTransportMap(int mapId) {
        return transportMaps.contains((short) mapId);
    }

    public SpawnGroupTemplateData getSpawnGroupData(int groupId) {
        return spawnGroupDataStorage.get(groupId);
    }

    public SpawnGroupTemplateData getSpawnGroupData(SpawnObjectType type, int spawnId) {
        var data = getSpawnMetadata(type, spawnId);

        return data != null ? data.getSpawnGroupData() : null;
    }

    public SpawnGroupTemplateData getDefaultSpawnGroup() {
        return spawnGroupDataStorage.get(0);
    }

    public SpawnGroupTemplateData getLegacySpawnGroup() {
        return spawnGroupDataStorage.get(1);
    }

    public List<SpawnMetadata> getSpawnMetadataForGroup(int groupId) {
        return spawnGroupMapStorage.get(groupId);
    }

    public List<Integer> getSpawnGroupsForMap(int mapId) {
        return spawnGroupsByMap.get(mapId);
    }

    AreaTriggerSpawn getAreaTriggerSpawn(int spawnId)
    {
        return areaTriggerSpawnsBySpawnId.get(spawnId);
    }

    public SpawnMetadata getSpawnMetadata(SpawnObjectType type, int spawnId) {
        if (SpawnMetadata.typeHasData(type)) {
            return getSpawnData(type, spawnId);
        } else {
            return null;
        }
    }

    public SpawnData getSpawnData(SpawnObjectType type, int spawnId) {
        if (!SpawnMetadata.typeHasData(type)) {
            return null;
        }

        return switch (type) {
            case CREATURE -> getCreatureData(spawnId);
            case GAME_OBJECT -> getGameObjectData(spawnId);
            case AREA_TRIGGER -> getAreaTriggerSpawn(spawnId);
            default -> null;
        };
    }


    //Player
    public void loadPlayerInfo() {
        var oldMSTime = System.currentTimeMillis();
        try(var items = playerRepo.streamsAllPlayerCreateInfo()) {
            items.forEach(e -> {
                ChrRace chrRace = dbcObjectManager.chrRace(e.race);
                if (chrRace == null) {
                    Logs.SQL.error("Wrong race {} in `playercreateinfo` table, ignoring.", e.race.ordinal());

                    return;
                }

                if (!dbcObjectManager.chrClass().contains(e.playClass.ordinal())) {
                    Logs.SQL.error("Wrong class {} in `playercreateinfo` table, ignoring.", e.playClass);

                    return;
                }

                // accept DB data only for valid position (and non instanceable)
                if (!world.getMapManager().isValidMapCoordinate(e.mapId, e.x, e.y, e.z, e.o)) {
                    Logs.SQL.error("Wrong home position for class {} race {} pair in `playercreateinfo` table, ignoring.", e.playClass, e.race);

                    return;
                }

                if (dbcObjectManager.map(e.mapId).isInstanceable()) {
                    Logs.SQL.error("Home position in instanceable map for class {} race {} pair in `playercreateinfo` table, ignoring.", e.playClass, e.race);

                    return;
                }
                e.displayIdForMale = chrRace.getMaleDisplayId();
                e.displayIdFemale = chrRace.getFemaleDisplayId();


                if (e.introMovieId != null && !dbcObjectManager.movie().contains(e.introMovieId)) {

                    Logs.SQL.error("Invalid intro movie id {} for class {} race {} pair in `playercreateinfo` table, ignoring.",
                            e.introMovieId, e.playClass, e.race);
                    e.introMovieId = null;
                }
                playerInfo.put(Pair.of(e.race, e.playClass), e);
            });


            Logs.SERVER_LOADING.info(">> Loaded {} player create definitions in {} ms", playerInfo.size(), System.currentTimeMillis() - (oldMSTime));

        }


        oldMSTime = System.currentTimeMillis();
        // Load playercreate items
        Logs.SERVER_LOADING.info("Loading Player Create Items data...");

        for (var chrOutFitItem : dbcObjectManager.charStartOutfit()) {
            List<ItemTemplate> items = Arrays.stream(chrOutFitItem.getItems())
                    .filter(itemId -> itemId != 0)
                    .mapToObj(this::getItemTemplate)
                    .filter(Objects::nonNull)
                    .toList();

            Gender gender = Gender.valueOf(chrOutFitItem.getSexID());
            Race race = Race.values()[chrOutFitItem.getRaceID()];
            UnitClass klass = UnitClass.values()[chrOutFitItem.getClassID()];
            PlayerInfo info = playerInfo.get(Pair.of(race, klass));

            if (items.isEmpty() || info == null) {
                continue;
            }

            for (ItemTemplate itemTemplate : items) {
                // BuyCount by default
                int count = itemTemplate.getBuyCount();
                // special amount for food/drink
                if (itemTemplate.getItemClass() == ItemClass.CONSUMABLE
                        && itemTemplate.getSubClass() == ItemSubclassConsumable.FOOD_DRINK) {
                    if (!itemTemplate.getEffects().isEmpty()) {
                        Short spellCategoryID = itemTemplate.getEffects().getFirst().getSpellCategoryID();
                        count = switch (SpellCategory.valueOf(spellCategoryID)) {
                            case FOOD -> klass == UnitClass.DEATH_KNIGHT ? 10 : 4;
                            case DRINK -> 2;
                        };
                    }
                    if (itemTemplate.getMaxStackSize() < count)
                        count = itemTemplate.getMaxStackSize();
                }
                switch (gender) {
                    case MALE -> info.itemsForMale.add(new PlayerCreateInfoItem(itemTemplate.getId(), count));
                    case FEMALE -> info.itemsForFemale.add(new PlayerCreateInfoItem(itemTemplate.getId(), count));
                }
            }
        }

        Logs.SERVER_LOADING.info("Loading Player Create Items Override data...");
        AtomicInteger count = new AtomicInteger();
        try(var items = playerRepo.streamsAllPlayerCreateInfoItems()) {
            items.forEach(fields -> {
                if (fields[0] >= Race.values().length) {
                    Logs.SQL.error("Wrong race {} in `playercreateinfo_item` table, ignoring.", fields[0]);
                    return;
                }

                if (fields[1] >= UnitClass.values().length) {
                    Logs.SQL.error("Wrong class {} in `playercreateinfo_item` table, ignoring.", fields[1]);
                    return;
                }

                Race race = Race.values()[fields[0]];
                UnitClass klass = UnitClass.values()[fields[1]];

                if (getItemTemplate(fields[2]) == null) {
                    Logs.SQL.error("Item id {} (race {} class {}) in `playercreateinfo_item` table but it does not exist, ignoring.", fields[2], race, klass);
                    return;
                }

                if (fields[3] < 1)
                {
                    Logs.SQL.error("Item id {} (class {} race {}) have amount == 0 in `playercreateinfo_item` table, ignoring.", fields[2], race, klass);
                    return;
                }

                if (race == Race.NONE || klass == UnitClass.NONE) {
                    Set<Race> racesNeedAdd = race == Race.NONE ? EnumSet.allOf(Race.class) : Set.of(race);
                    Set<UnitClass> klassNeedAdd = klass == UnitClass.NONE ? EnumSet.allOf(UnitClass.class) : Set.of(klass);
                    for (Race aRace : racesNeedAdd) {
                        for (UnitClass aClass : klassNeedAdd) {
                            playerCreateInfoAddItemHelper(aRace, aClass, fields[2], fields[3]);
                        }
                    }
                } else {
                    playerCreateInfoAddItemHelper(race, klass, fields[2], fields[3]);
                }
                count.getAndIncrement();
            });

        }

        Logs.SERVER_LOADING.info(">> Loaded {} custom player create items in {} ms", count, System.currentTimeMillis() - oldMSTime);


        // Load playercreate skills
        Logs.SERVER_LOADING.info("Loading Player Create Skill data...");

        oldMSTime = System.currentTimeMillis();
        for (var rcInfo : dbcObjectManager.skillRaceClassInfo()) {
            if(rcInfo.getAvailability() != 1) {
                continue;
            }
            for (Race race : Race.values()) {
                for (UnitClass klass : UnitClass.values()) {
                    if (race == Race.NONE || klass == UnitClass.NONE) {
                        continue;
                    }
                    if (!rcInfo.raceMask().hasRace(race) || !rcInfo.classMask().hasPlayerClass(klass)) {
                        continue;
                    }
                    PlayerInfo info = playerInfo.get(Pair.of(race, klass));
                    info.skills.add(rcInfo);
                }
            }
        }

        Logs.SERVER_LOADING.info(">> Loaded player create skills in {} ms", System.currentTimeMillis() - oldMSTime);

        oldMSTime = System.currentTimeMillis();
        count.set(0);
        // Load playercreate custom spells
        Logs.SERVER_LOADING.info("Loading Player Create Custom Spell data...");

        try (var items = playerRepo.streamsAllPlayerCreateInfoSpellCustom()) {
            items.forEach(fields -> {
                RaceMask raceMask = fields.raceMask();
                PlayerClassMask playerClassMask = fields.classMask();
                if (!raceMask.isEmpty() && !RaceMask.ALL_PLAYABLE.hasRaceMask(raceMask)) {
                    Logs.SQL.error("Wrong race mask {} in `playercreateinfo_spell_custom` table, ignoring.", raceMask.getRawValue());
                    return;
                }

                if (!playerClassMask.isEmpty() && !PlayerClassMask.ALL_PLAYABLE.hasPlayerClassMask(playerClassMask)) {
                    Logs.SQL.error("Wrong class mask {} in `playercreateinfo_spell_custom` table, ignoring.", playerClassMask);
                    return;
                }
                for (Race race : Race.values()) {
                    for (UnitClass klass : UnitClass.values()) {
                        if (race == Race.NONE || klass == UnitClass.NONE) {
                            continue;
                        }
                        if ((!raceMask.isEmpty() && !raceMask.hasRace(race))
                                || (!playerClassMask.isEmpty() && !playerClassMask.hasPlayerClass(klass))) {
                            continue;
                        }
                        PlayerInfo info = playerInfo.get(Pair.of(race, klass));
                        info.customSpells.add(fields.spell);
                    }
                }
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} custom player create spells in {} ms", count, System.currentTimeMillis() - oldMSTime);

        // Load playercreate cast spell
        Logs.SERVER_LOADING.info("Loading Player Create Cast Spell data...");
        oldMSTime = System.currentTimeMillis();
        count.set(0);

        try (var items = playerRepo.streamsAllPlayerCreateInfoCastSpell()) {
            items.forEach(fields -> {
                RaceMask raceMask = fields.raceMask();
                PlayerClassMask playerClassMask = fields.classMask();
                if (!raceMask.isEmpty() && !RaceMask.ALL_PLAYABLE.hasRaceMask(raceMask)) {
                    Logs.SQL.error("Wrong race mask {} in `playercreateinfo_cast_spell` table, ignoring.", raceMask.getRawValue());
                    return;
                }

                if (!playerClassMask.isEmpty() && !PlayerClassMask.ALL_PLAYABLE.hasPlayerClassMask(playerClassMask)) {
                    Logs.SQL.error("Wrong class mask {} in `playercreateinfo_cast_spell` table, ignoring.", playerClassMask);
                    return;
                }
                for (Race race : Race.values()) {
                    for (UnitClass klass : UnitClass.values()) {
                        if (race == Race.NONE || klass == UnitClass.NONE) {
                            continue;
                        }
                        if ((!raceMask.isEmpty() && !raceMask.hasRace(race))
                                || (!playerClassMask.isEmpty() && !playerClassMask.hasPlayerClass(klass))) {
                            continue;
                        }
                        PlayerInfo info = playerInfo.get(Pair.of(race, klass));
                        info.castSpells.add(fields.spell);
                    }
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} custom player create spells in {} ms", count, System.currentTimeMillis() - oldMSTime);

        // Load playercreate actions
        oldMSTime = System.currentTimeMillis();
        count.set(0);
        Logs.SERVER_LOADING.info("Loading Player Create Action data...");

        try (var items = playerRepo.streamsAllPlayerCreateInfoAction()) {
            items.forEach(fields -> {
                if (fields[0] >= Race.values().length) {
                    Logs.SQL.error("Wrong race {} in `playercreateinfo_action` table, ignoring.", fields[0]);
                    return;
                }

                if (fields[1] >= UnitClass.values().length) {
                    Logs.SQL.error("Wrong class {} in `playercreateinfo_action` table, ignoring.", fields[1]);
                    return;
                }

                Race race = Race.values()[fields[0]];
                UnitClass klass = UnitClass.values()[fields[1]];
                PlayerInfo info = playerInfo.get(Pair.of(race, klass));
                info.actions.add(new PlayerCreateInfoAction((byte) fields[2], (byte) fields[3], fields[4]));
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} player create actions in {} ms", count, System.currentTimeMillis() - oldMSTime);

        oldMSTime = System.currentTimeMillis();
        count.set(0);
        // Loading levels data (class/race dependent)
        Logs.SERVER_LOADING.info("Loading Player Create Level Stats data...");

        int[][] raceStatModifiers = new int[Race.values().length][SharedDefine.MAX_STATS];
        try (var items = playerRepo.streamsAllPlayerRaceStats()) {
            items.forEach(fields -> {
                if (fields[0] >= Race.values().length) {
                    Logs.SQL.error("Wrong race {} in `player_racestats` table, ignoring.", fields[0]);
                    return;
                }

                Race race = Race.values()[fields[0]];
                System.arraycopy(fields, 1, raceStatModifiers[race.ordinal()], 0, SharedDefine.MAX_STATS);
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} player create race stats in {} ms", count, System.currentTimeMillis() - oldMSTime);
        oldMSTime = System.currentTimeMillis();
        count.set(0);
        try (var items = playerRepo.streamsAllPlayerClassLevelStats()) {
            items.forEach(fields -> {
                if (fields[0] >= UnitClass.values().length) {
                    Logs.SQL.error("Wrong class {} in `playercreateinfo_action` table, ignoring.", fields[0]);
                    return;
                }

                int level = fields[1];
                if (level > world.getWorldSettings().maxPlayerLevel) {
                    if (level > SharedDefine.STRONG_MAX_LEVEL)        // hardcoded level maximum
                        Logs.SQL.error("Wrong (> {}) level {} in `player_classlevelstats` table, ignoring.", SharedDefine.STRONG_MAX_LEVEL, level);
                    else
                        Logs.SQL.error("Unused (> MaxPlayerLevel in worldserver.conf) level {} in `player_classlevelstats` table, ignoring.", level);

                    return;
                }

                UnitClass klass = UnitClass.values()[fields[0]];

                for (int race = 1; race < raceStatModifiers.length; race++) {
                    PlayerInfo info = playerInfo.get(Pair.of(Race.values()[race], klass));
                    if (info == null) {
                        continue;
                    }
                    if (info.levelInfo == null) {
                        info.levelInfo = new PlayerLevelInfo[world.getWorldSettings().maxPlayerLevel];
                    }
                    PlayerLevelInfo levelInfo = info.levelInfo[level - 1];
                    for (int i = 0; i < SharedDefine.MAX_STATS; ++i) {
                        levelInfo.stats[i] = fields[i + 2] + raceStatModifiers[race][i];
                    }
                }
                count.getAndIncrement();
            });
        }

        for (Race race : Race.values()) {
            for (UnitClass klass : UnitClass.values()) {
                if (race == Race.NONE || klass == UnitClass.NONE) {
                    continue;
                }
                PlayerInfo info = playerInfo.get(Pair.of(race, klass));
                if(info == null) {
                    continue;
                }

                // skip expansion races if not playing with expansion
                if (world.getWorldSettings().expansion.ordinal() < Expansion.THE_BURNING_CRUSADE.ordinal() && (race == Race.BLOOD_ELF || race == Race.DRAENEI))
                    continue;

                // skip expansion classes if not playing with expansion
                if (world.getWorldSettings().expansion.ordinal() < Expansion.WRATH_OF_THE_LICH_KING.ordinal() && klass == UnitClass.DEATH_KNIGHT)
                    continue;

                // skip expansion races if not playing with expansion
                if (world.getWorldSettings().expansion.ordinal() < Expansion.CATACLYSM.ordinal() && (race == Race.GOBLIN || race == Race.WORGEN))
                    continue;

                if (world.getWorldSettings().expansion.ordinal() < Expansion.MISTS_OF_PANDARIA.ordinal() && (race == Race.PANDAREN_NEUTRAL || race == Race.PANDAREN_HORDE || race == Race.PANDAREN_ALLIANCE))
                    continue;

                if (world.getWorldSettings().expansion.ordinal() < Expansion.LEGION.ordinal() && klass == UnitClass.DEMON_HUNTER)
                    continue;


                // fatal error if no level 1 data
                if (info.levelInfo == null || info.levelInfo[0].stats[0] == 0)
                {
                    Logs.SQL.error("Race {} Class {} Level 1 does not have stats data!", race, klass);
                    throw new IllegalStateException(StringUtil.format("Race {} Class {} Level 1 does not have stats data!", race, klass));
                }

                // fill level gaps
                for (int level = 1; level < world.getWorldSettings().maxPlayerLevel; ++level)
                {
                    if (info.levelInfo[level].stats[0] == 0)
                    {
                        Logs.SQL.error("Race {} Class {} Level {} does not have stats data. Using stats data of level {}.", race, klass, level + 1, level);
                        info.levelInfo[level] = info.levelInfo[level - 1];
                    }
                }
            }
        }

        Logs.SERVER_LOADING.error(">> Loaded {} level stats definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);


        oldMSTime = System.currentTimeMillis();
        count.set(0);
        // Loading xp per level data
        Logs.SERVER_LOADING.info("Loading Player Create XP data...");

        {
            GameTable<GtXp> xp = world.getGameTableManager().xp();

            playerXPperLevel = new int[xp.rowCount()];
            // load the DBC's levels at first...
            for (int level = 1; level < xp.rowCount(); ++level) {
                playerXPperLevel[level] = (int) xp.getRow(level).total;
            }

            try (var items = playerRepo.streamsAllPlayerXoForLevel()) {
                items.forEach(fields -> {
                    if (fields[0] >= world.getWorldSettings().maxPlayerLevel) {
                        // hardcoded level maximum
                        if (fields[0] > SharedDefine.STRONG_MAX_LEVEL)
                            Logs.SQL.error("Wrong (> {}) level {} in `player_xp_for_level` table, ignoring.", SharedDefine.STRONG_MAX_LEVEL, fields[0]);
                        else {
                            Logs.MISC.error("Unused (> MaxPlayerLevel in worldserver.conf) level {} in `player_xp_for_level` table, ignoring.", fields[0]);
                            // make result loading percent "expected" correct in case disabled detail mode for example.
                            count.getAndIncrement();
                        }
                        return;
                    }
                    //PlayerXPperLevel
                    playerXPperLevel[fields[0]] = fields[1];
                    count.getAndIncrement();
                });
            }

            // fill level gaps - only accounting levels > MAX_LEVEL
            for (int level = 1; level < world.getWorldSettings().maxPlayerLevel; ++level)
            {
                if (playerXPperLevel[level] == 0)
                {
                    Logs.SQL.error("Level {} does not have XP for level data. Using data of level [{}] + 12000.", level + 1, level);
                    playerXPperLevel[level] = playerXPperLevel[level - 1] + 12000;
                }
            }
            Logs.SERVER_LOADING.info(">> Loaded {} xp for level definition(s) from database in {} ms", count, System.currentTimeMillis() - oldMSTime);
        }
    }

    public PlayerInfo getPlayerInfo(Race raceId, UnitClass classId) {
        return playerInfo.get(Pair.of(raceId, classId));
    }

    public int getPlayerClassLevelInfo(UnitClass _class, int level) {
        if (level < 1 || _class == null)
            throw new IllegalArgumentException();

        if (level > world.getWorldSettings().maxPlayerLevel)
            level = world.getWorldSettings().maxPlayerLevel;

        GtBaseMP mp = world.getGameTableManager().baseMP().getRow(level);
        if (mp == null) {
            throw new NullPointerException(StringUtil.format("Tried to get non-existant Class-Level combination data for base hp/mp. Class {} Level {}", _class, level));
        }
        return (int)world.getGameTableManager().getBaseMPValueForClass(level, _class);
    }

    public PlayerLevelInfo getPlayerLevelInfo(Race race, UnitClass _class, int level) {
        Objects.requireNonNull(race);
        Objects.requireNonNull(_class);

        PlayerInfo info = playerInfo.get(Pair.of(race, _class));


        if (level <= world.getWorldSettings().maxPlayerLevel) {
            return info.levelInfo[level - 1];
        } else {
            return buildPlayerLevelInfo(race, _class, level);
        }
    }

    //Pets
    public void loadPetLevelInfo() {
        var oldMSTime = System.currentTimeMillis();
        int maxPlayerLevel = world.getWorldSettings().maxPlayerLevel;
        AtomicInteger count = new AtomicInteger();
        HashMap<Integer, PetLevelInfo[]> tmp = new HashMap<>();
        try(var items = playerRepo.streamsAllPetLevelStats()) {
            items.forEach(fields -> {
                if (getCreatureTemplate(fields[0]) == null) {
                    Logs.SQL.error("Wrong creature id {} in `pet_levelstats` table, ignoring.", fields[0]);
                    return;
                }
                int currentLevel = fields[1];

                if (currentLevel > maxPlayerLevel) {
                    if (currentLevel > SharedDefine.STRONG_MAX_LEVEL)        // hardcoded level maximum
                        Logs.SQL.error("Wrong (> {}) level {} in `pet_levelstats` table, ignoring.", SharedDefine.STRONG_MAX_LEVEL, currentLevel);
                    else {
                        Logs.MISC.error("Unused (> MaxPlayerLevel in worldserver.conf) level {} in `pet_levelstats` table, ignoring.", currentLevel);
                        count.incrementAndGet();                                // make result loading percent "expected" correct in case disabled detail mode for example.
                    }
                    return;
                } else if (currentLevel < 1) {
                    Logs.SQL.error("Wrong (<1) level {} in `pet_levelstats` table, ignoring.", currentLevel);
                    return;
                }
                var pInfoMapEntry = tmp.compute(fields[0], Functions.ifAbsent(()-> new PetLevelInfo[maxPlayerLevel]));

                PetLevelInfo pLevelInfo = new PetLevelInfo();
                pLevelInfo.health = fields[2];
                pLevelInfo.mana = fields[3];
                pLevelInfo.armor = fields[9];

                System.arraycopy(fields, 4, pLevelInfo.stats, 0, SharedDefine.MAX_STATS);

                pInfoMapEntry[currentLevel - 1] = pLevelInfo;
                count.getAndIncrement();
            });
        }


        // Fill gaps and check integrity
        for (var map : tmp.entrySet()) {
            var pInfo = map.getValue();

            // fatal error if no level 1 data
            if (pInfo == null || pInfo[0].health == 0) {
                Logs.SQL.error("Creature {} does not have pet stats data for Level 1!", map.getKey());
                throw new IllegalArgumentException("Creature " + map.getKey() + " does not have pet stats data for Level 1!");
            }

            // fill level gaps
            for (byte level = 1; level < maxPlayerLevel; ++level) {
                if (pInfo[level].health == 0) {
                    Logs.SQL.error("Creature {} has no data for Level {} pet stats data, using data of Level {}.", map.getKey(), level + 1, level);
                    pInfo[level] = pInfo[level - 1];
                }
            }
        }

        petInfoStore.putAll(tmp);

        Logs.SERVER_LOADING.info(">> Loaded {} level pet stats definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadPetNames() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = playerRepo.streamsAllPetNameGeneration()) {
            items.forEach(field->{
                if (field.half != 0) {
                    petHalfName1.compute(field.entry, Functions.addToList(field.word));
                } else {
                    petHalfName0.compute(field.entry, Functions.addToList(field.word));
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} pet name parts in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }


    public PetLevelInfo getPetLevelInfo(int creatureId, int level) {
        int maxPlayerLevel = world.getWorldSettings().maxPlayerLevel;
        if (level > maxPlayerLevel) {
            level = maxPlayerLevel;
        }

        var petinfo = petInfoStore.get(creatureId);

        if (petinfo == null) {
            return null;
        }

        return petinfo[level - 1]; // data for level 1 stored in [0] array element, ...
    }

    public String generatePetName(int entry) {
        var list0 = petHalfName0.get(entry);
        var list1 = petHalfName1.get(entry);

        if (list0.isEmpty() || list1.isEmpty()) {
            var cinfo = getCreatureTemplate(entry);

            if (cinfo == null) {
                return "";
            }

            var petName = dbcObjectManager.getCreatureFamilyPetName(cinfo.family, world.getWorldSettings().dbcLocale);

            if (!StringUtil.isEmpty(petName)) {
                return petName;
            } else {
                return cinfo.name.get(world.getWorldSettings().dbcLocale);
            }
        }
        return RandomUtil.random(list0) + RandomUtil.random(list1);

    }

    //Faction Change
    public void loadFactionChangeAchievements() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = miscRepo.streamAllPlayerFactionChangeAchievement()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.achievement().contains(fields[0])) {
                    Logs.SQL.error("Achievement {} (alliance_id) referenced in `player_factionchange_achievement` does not exist, pair skipped!", fields[0]);
                } else if (!dbcObjectManager.achievement().contains(fields[1])) {
                    Logs.SQL.error("Achievement {} (horde_id) referenced in `player_factionchange_achievement` does not exist, pair skipped!", fields[1]);
                } else {
                    factionChangeAchievements.put(fields[0], fields[1]);
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} faction change achievement pairs in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadFactionChangeItems() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = miscRepo.streamAllPlayerFactionChangeItem()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.item().contains(fields[0])) {
                    Logs.SQL.error("Item {} (alliance_id) referenced in `player_factionchange_items` does not exist, pair skipped!", fields[0]);
                } else if (!dbcObjectManager.item().contains(fields[1])) {
                    Logs.SQL.error("Item {} (horde_id) referenced in `player_factionchange_items` does not exist, pair skipped!", fields[1]);
                } else {
                    factionChangeItemsAllianceToHorde.put(fields[0], fields[1]);
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} faction change achievement pairs in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadFactionChangeQuests() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = miscRepo.streamAllPlayerFactionChangeQuests()) {
            items.forEach(fields -> {
                if (!questTemplates.containsKey(fields[0])) {
                    Logs.SQL.error("Quest {} (alliance_id) referenced in `player_factionchange_quests` does not exist, pair skipped!", fields[0]);
                } else if (!questTemplates.containsKey(fields[1])) {
                    Logs.SQL.error("Quest {} (horde_id) referenced in `player_factionchange_quests` does not exist, pair skipped!", fields[1]);
                } else {
                    factionChangeQuests.put(fields[0], fields[1]);
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} faction change quest pairs in {} ms.", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadFactionChangeReputations() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = miscRepo.streamAllPlayerFactionChangeReputations()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.faction().contains(fields[0])) {
                    Logs.SQL.error("Reputation {} (alliance_id) referenced in `player_factionchange_reputations` does not exist, pair skipped!", fields[0]);
                } else if (!dbcObjectManager.faction().contains(fields[1])) {
                    Logs.SQL.error("Reputation {} (horde_id) referenced in `player_factionchange_reputations` does not exist, pair skipped!", fields[1]);
                } else {
                    factionChangeReputation.put(fields[0], fields[1]);
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} faction change reputation pairs in {} ms", count, System.currentTimeMillis() - oldMSTime);

    }

    public void loadFactionChangeSpells() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = miscRepo.streamAllPlayerFactionChangeSpells()) {
            items.forEach(fields -> {
                if (!spellManager.hasSpellInfo(fields[0], Difficulty.NONE)) {
                    Logs.SQL.error("Spell {} (alliance_id) referenced in `player_factionchange_spells` does not exist, pair skipped!", fields[0]);
                } else if (!spellManager.hasSpellInfo(fields[1])) {
                    Logs.SQL.error("Spell {} (horde_id) referenced in `player_factionchange_spells` does not exist, pair skipped!", fields[1]);
                } else {
                    factionChangeSpells.put(fields[0], fields[1]);
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} faction change spell pairs in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadFactionChangeTitles() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = miscRepo.streamAllPlayerFactionChangeTitles()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.charTitle().contains(fields[0])) {
                    Logs.SQL.error("Title {} (alliance_id) referenced in `player_factionchange_title` does not exist, pair skipped!", fields[0]);
                } else if (!dbcObjectManager.charTitle().contains(fields[1])) {
                    Logs.SQL.error("Title {} (horde_id) referenced in `player_factionchange_title` does not exist, pair skipped!", fields[1]);
                } else {
                    factionChangeTitles.put(fields[0], fields[1]);
                }
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} faction change title pairs in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    //Quests
    public void loadQuests() {
        var oldMSTime = System.currentTimeMillis();

        // For reload case
        questTemplates.clear();
        questTemplatesAutoPush.clear();
        questObjectives.clear();
        exclusiveQuestGroups.clear();

        // create multimap previous quest for each existed quest
        // some quests can have many previous maps set by NextQuestId in previous quest
        // for example set of race quests can lead to single not race specific quest
        Map<Integer, QuestTemplate> tmp = new HashMap<>();
        try (var items = questRepo.streamAllQuestTemplate()) {
            items.forEach(questTemplate -> {
                tmp.put(questTemplate.id, questTemplate);
                if (questTemplate.isAutoPush()) {
                    questTemplatesAutoPush.add(questTemplate);
                }
            });
        }

        // Load `quest_reward_choice_items`
        try (var items = questRepo.streamAllQuestRewardChoiceItems()) {
            items.forEach(e -> {
                QuestTemplate questTemplate = tmp.get(e[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_reward_choice_items` has data for quest {} but such quest does not exist", e[0]);
                    return;
                }
                for (int i = 0; i < QuestTemplate.QUEST_REWARD_CHOICES_COUNT; ++i) {
                    int type = e[i + 1];
                    if (type > LootItemType.values().length) {
                        Logs.SQL.error("Table `quest_reward_choice_items` field index {} has worry item type {}. ", i + 1, e[0]);
                        continue;
                    }
                    questTemplate.rewardChoiceItemType[i] = LootItemType.values()[type];
                }
            });
        }


        // Load `quest_reward_display_spell`
        try (var items = questRepo.streamAllQuestRewardDisplaySpell()) {
            items.forEach(e -> {
                int spellId = e[1];
                int playerConditionId = e[2];
                int type = e[3];
                QuestTemplate questTemplate = tmp.get(e[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_reward_display_spell` has data for quest {} but such quest does not exist", e[0]);
                    return;
                }
                if (spellManager.getSpellInfo(e[2], Difficulty.NONE) == null) {
                    Logs.SQL.error("Table `quest_reward_display_spell` has non-existing Spell ({}) set for quest {}. Skipped.", spellId, e[0]);
                    return;
                }

                if (playerConditionId != 0 && dbcObjectManager.playerCondition(playerConditionId) != null) {
                    if (conditionManager.hasConditionsForNotGroupedEntry(ConditionSourceType.PLAYER_CONDITION, playerConditionId)) {
                        Logs.SQL.error("Table `quest_reward_display_spell` has serverside PlayerCondition ({}) set for quest {} and spell {} without conditions. Set to 0.", playerConditionId, e[0], spellId);
                        playerConditionId = 0;
                    }
                }

                if (type >= QuestCompleteSpellType.values().length) {
                    Logs.SQL.error("Table `quest_reward_display_spell` invalid type second ({}) set for quest {} and spell {}. Set to 0.", type, e[0], spellId);
                    type = QuestCompleteSpellType.LegacyBehavior.ordinal();
                }
                questTemplate.rewardDisplaySpell.add(new QuestRewardDisplaySpell(spellId, playerConditionId, QuestCompleteSpellType.values()[type]));
            });
        }

        // Load `quest_details`
        try (var items = questRepo.streamAllQuestDetails()) {
            items.forEach(fields -> {
                QuestTemplate questTemplate = tmp.get(fields[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_details` has data for quest {} but such quest does not exist", fields[0]);
                    return;
                }
                for (int i = 0; i < QuestTemplate.QUEST_EMOTE_COUNT; ++i) {
                    if (dbcObjectManager.emote(fields[1 + i]) == null) {
                        Logs.SQL.error("Table `quest_details` has non-existing Emote{} ({}) set for quest {}. Skipped.", 1 + i, fields[1 + i], fields[0]);
                        return;
                    }

                    questTemplate.detailsEmote[i] = fields[1 + i];
                }

                System.arraycopy(fields, 5, questTemplate.detailsEmoteDelay, 0, QuestTemplate.QUEST_EMOTE_COUNT);
            });
        }


        // Load `quest_request_items`
        try (var items = questRepo.streamAllQuestRequestItems()) {
            items.forEach(fields -> {
                QuestTemplate questTemplate = tmp.get((Integer) fields[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_request_items` has data for quest {} but such quest does not exist", fields[0]);
                    return;
                }
                questTemplate.emoteOnComplete = (Integer) fields[1];
                questTemplate.emoteOnIncomplete = (Integer) fields[2];

                if (dbcObjectManager.emote(questTemplate.emoteOnComplete) == null)
                    Logs.SQL.error("Table `quest_request_items` has non-existing EmoteOnComplete ({}) set for quest {}.", questTemplate.emoteOnComplete, fields[0]);

                if (dbcObjectManager.emote(questTemplate.emoteOnIncomplete) == null)
                    Logs.SQL.error("Table `quest_request_items` has non-existing EmoteOnIncomplete ({}) set for quest {}.", questTemplate.emoteOnIncomplete, fields[0]);

                questTemplate.emoteOnCompleteDelay = (Integer) fields[3];
                questTemplate.emoteOnIncompleteDelay = (Integer) fields[4];
                questTemplate.requestItemsText.set(Locale.enUS, (String) fields[5]);
            });
        }

        // Load `quest_offer_reward`
        try (var items = questRepo.streamAllQuestOfferReward()) {
            items.forEach(fields -> {
                QuestTemplate questTemplate = tmp.get((Integer) fields[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_offer_reward` has data for quest {} but such quest does not exist", fields[0]);
                    return;
                }
                for (int i = 0; i < QuestTemplate.QUEST_EMOTE_COUNT; ++i) {
                    if (dbcObjectManager.emote((Integer) fields[1 + i]) == null) {
                        Logs.SQL.error("Table `quest_offer_reward` has non-existing Emote{} ({}) set for quest {}. Skipped.", 1 + i, fields[1 + i], fields[0]);
                        return;
                    }

                    questTemplate.offerRewardEmote[i] = (Integer) fields[1 + i];
                }

                for (int i = 0; i < QuestTemplate.QUEST_EMOTE_COUNT; ++i)
                    questTemplate.offerRewardEmoteDelay[i] = (Integer) fields[5 + i];

                questTemplate.offerRewardText.set(Locale.enUS, (String) fields[9]);
            });
        }

        // Load `quest_template_addon`
        try (var items = questRepo.streamAllQuestTemplateAddon()) {
            items.forEach(fields -> {
                QuestTemplate questTemplate = tmp.get((Integer) fields[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_template_addon` has data for quest {} but such quest does not exist", fields[0]);
                    return;
                }
                questTemplate.maxLevel = (Short) fields[1];
                questTemplate.allowableClasses = (Integer) fields[2];
                questTemplate.sourceSpellID = (Integer) fields[3];
                questTemplate.prevQuestId = (Integer) fields[4];
                questTemplate.nextQuestId = (Integer) fields[5];
                questTemplate.exclusiveGroup = (Integer) fields[6];
                questTemplate.breadcrumbForQuestId = (Integer) fields[7];
                questTemplate.rewardMailTemplateId = (Integer) fields[8];
                questTemplate.rewardMailDelay = (Integer) fields[9];
                questTemplate.requiredSkillId = (Integer) fields[10];
                questTemplate.requiredSkillPoints = (Integer) fields[11];
                questTemplate.requiredMinRepFaction = (Integer) fields[12];
                questTemplate.requiredMaxRepFaction = (Integer) fields[13];
                questTemplate.requiredMinRepValue = (Integer) fields[14];
                questTemplate.requiredMaxRepValue = (Integer) fields[15];
                questTemplate.sourceItemIdCount = (Integer) fields[16];
                questTemplate.specialFlags.setFlags((Integer) fields[17]);
                questTemplate.scriptId = getScriptId((String) fields[18]);

                if (questTemplate.specialFlags.hasFlag(QuestSpecialFlag.AUTO_ACCEPT))
                    questTemplate.flags.addFlag(QuestFlag.AUTO_ACCEPT);
            });
        }


        // Load `quest_mail_sender`
        try (var items = questRepo.streamAllQuestMailSender()) {
            items.forEach(fields -> {
                QuestTemplate questTemplate = tmp.get(fields[0]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_mail_sender` has data for quest {} but such quest does not exist", fields[0]);
                    return;
                }
                questTemplate.rewardMailSenderEntry = fields[1];
            });
        }


        // Load `quest_objectives`
        Map<Integer, QuestObjective> tmpObjectives = new HashMap<>();
        try (var items = questRepo.streamAllQuestObjectives()) {
            items.forEach(e -> {
                QuestTemplate questTemplate = tmp.get(e.questID);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_objectives` has data for quest {} but such quest does not exist", e.questID);
                    return;
                }
                tmpObjectives.put(e.objectID, e);
                questTemplate.objectives.add(e);
                questTemplate.usedQuestObjectiveTypes.set(e.type.ordinal(), true);
            });
        }


        // Load `quest_visual_effect` join table with quest_objectives because visual effects are based on objective ID (core stores objectives by their index in quest)
        try (var items = questRepo.streamAllQuestVisualEffect()) {
            items.forEach(fields -> {
                QuestTemplate questTemplate = tmp.get(fields[2]);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_visual_effect` has data for quest {} but such quest does not exist", fields[0]);
                    return;
                }

                int objID = fields[1];

                for (QuestObjective obj : questTemplate.objectives) {
                    if (obj.id == objID) {
                        int effectIndex = fields[3];
                        obj.visualEffects[effectIndex] = fields[4];
                        break;
                    }
                }

            });
        }

        // Load `quest_template_locale`
        try (var items = questRepo.streamAllQuestTemplateLocale()) {
            items.forEach(e -> {
                QuestTemplate questTemplate = tmp.get(e.id);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_template_locale` has data for quest {} but such quest does not exist", e.id);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                questTemplate.logTitle.set(locale, e.logTitle);
                questTemplate.logDescription.set(locale, e.logDescription);
                questTemplate.areaDescription.set(locale, e.areaDescription);
                questTemplate.questDescription.set(locale, e.questDescription);
                questTemplate.questCompletionLog.set(locale, e.questCompletionLog);
                questTemplate.portraitGiverName.set(locale, e.portraitGiverName);
                questTemplate.portraitGiverText.set(locale, e.portraitGiverText);
                questTemplate.portraitTurnInName.set(locale, e.portraitTurnInName);
                questTemplate.portraitTurnInText.set(locale, e.portraitTurnInText);
            });
        }

        // Load `quest_offer_reward_locale`
        try (var items = questRepo.streamAllQuestOfferRewardLocale()) {
            items.forEach(e -> {
                QuestTemplate questTemplate = tmp.get(e.id);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_offer_reward_locale` has data for quest {} but such quest does not exist", e.id);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                questTemplate.offerRewardText.set(locale, e.rewardText);
            });
        }

        // Load `quest_objectives_locale`
        try (var items = questRepo.streamAllQuestObjectivesLocale()) {
            items.forEach(e -> {
                QuestObjective questObjective = tmpObjectives.get(e.id);
                if (questObjective == null) {
                    Logs.SQL.error("Table `quest_objectives_locale` has data for quest objective {} but such quest objective does not exist", e.id);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                questObjective.description.set(locale, e.description);
            });
        }

        // Load `quest_request_items_locale`
        try (var items = questRepo.streamAllQuestRequestItemsLocale()) {
            items.forEach(e -> {
                QuestTemplate questTemplate = tmp.get(e.id);
                if (questTemplate == null) {
                    Logs.SQL.error("Table `quest_request_items_locale` has data for quest {} but such quest does not exist", e.id);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                questTemplate.requestItemsText.set(locale, e.completionText);
            });
        }


        HashMap<Integer, Integer> usedMailTemplates = new HashMap<Integer, Integer>();

        // Post processing
        for (var qinfo : tmp.values()) {
            // skip post-loading checks for disabled quests
            if (disableManager.isDisabledFor(DisableType.QUEST, qinfo.id, null)) {
                continue;
            }

            // additional quest integrity checks (GO, creaturetemplate and itemtemplate must be loaded already)

            if (qinfo.type == null) {
                Logs.SQL.error("Quest {} has `type` = {}, expected values are 0, 1 or 2.", qinfo.id, qinfo.type);
            }

            if (qinfo.specialFlags.hasNotFlag(QuestSpecialFlag.DB_ALLOWED)) {
                Logs.SQL.error("Quest {} has `SpecialFlags` = {} > max allowed second. Correct `SpecialFlags` to second <= {}", qinfo.id, qinfo.specialFlags, QuestSpecialFlag.DB_ALLOWED);

                qinfo.specialFlags.removeNotFlag(QuestSpecialFlag.DB_ALLOWED);
            }

            if (qinfo.flags.hasFlag(QuestFlag.DAILY) && qinfo.flags.hasFlag(QuestFlag.WEEKLY)) {
                Logs.SQL.error("Weekly Quest {} is marked as daily quest in `Flags`, removed daily flag.", qinfo.id);
                qinfo.flags.removeNotFlag(QuestFlag.DAILY);
            }

            if (qinfo.flags.hasFlag(QuestFlag.DAILY)) {
                if (!qinfo.specialFlags.hasFlag(QuestSpecialFlag.REPEATABLE)) {
                    Logs.SQL.error("Daily Quest {} not marked as repeatable in `SpecialFlags`, added.", qinfo.id);
                    qinfo.specialFlags.addFlag(QuestSpecialFlag.REPEATABLE);
                }
            }

            if (qinfo.flags.hasFlag(QuestFlag.WEEKLY)) {
                if (!qinfo.specialFlags.hasFlag(QuestSpecialFlag.REPEATABLE)) {
                    Logs.SQL.error("Weekly Quest {} not marked as repeatable in `SpecialFlags`, added.", qinfo.id);
                    qinfo.specialFlags.addFlag(QuestSpecialFlag.REPEATABLE);
                }
            }

            if (qinfo.specialFlags.hasFlag(QuestSpecialFlag.MONTHLY)) {
                if (!qinfo.specialFlags.hasFlag(QuestSpecialFlag.REPEATABLE)) {
                    Logs.SQL.error("Monthly quest {} not marked as repeatable in `SpecialFlags`, added.", qinfo.id);
                    qinfo.specialFlags.addFlag(QuestSpecialFlag.REPEATABLE);
                }
            }


            if ((qinfo.flags.hasFlag(QuestFlag.TRACKING_EVENT))) {
                // at auto-reward can be rewarded only RewardChoiceItemId[0]
                for (var j = 1; j < qinfo.rewardChoiceItemId.length; ++j) {
                    var id = qinfo.rewardChoiceItemId[j];

                    if (id != 0) {
                        Logs.SQL.error("Quest {} has `RewardChoiceItemId{}` = {} but item from `RewardChoiceItemId{}` can't be rewarded with quest flag QUEST_FLAGS_TRACKING.", qinfo.id, j + 1, id, j + 1);
                    }
                    // no changes, quest ignore this data
                }
            }


            // client quest log visual (area case)
            if (qinfo.questSortID > 0) {
                if (dbcObjectManager.areaTrigger(qinfo.questSortID) != null) {
                    Logs.SQL.error("Quest {} has `ZoneOrSort` = {} (zone case) but zone with this id does not exist.", qinfo.id, qinfo.questSortID);
                }
            }

            // no changes, quest not dependent from this second but can have problems at client
            // client quest log visual (sort case)
            if (qinfo.questSortID < 0) {
                var qSort = dbcObjectManager.areaTrigger( -qinfo.questSortID);

                if (qSort == null) {
                    Logs.SQL.error("Quest {} has `ZoneOrSort` = {} (sort case) but quest sort with this id does not exist.", qinfo.id, qinfo.questSortID);
                }

                // no changes, quest not dependent from this second but can have problems at client (note some may be 0, we must allow this so no check)
                //check for proper RequiredSkillId second (skill case)
                var skillid = SharedDefine.skillByQuestSort(QuestSort.valueOf(-qinfo.questSortID));

                if (skillid != SkillType.NONE) {
                    if (qinfo.requiredSkillId != skillid.value) {
                        Logs.SQL.error("Quest {} has `ZoneOrSort` = {} but `RequiredSkillId` does not have a corresponding second ({}).", qinfo.id, qinfo.questSortID, skillid);
                    }
                }
                //override, and force proper second here?
            }

            // allowableClasses, can be 0/CLASSMASK_ALL_PLAYABLE to allow any class
            if (qinfo.allowableClasses != 0) {
                if (!PlayerClassMask.ALL_PLAYABLE.hasPlayerClassMask(qinfo.allowableClasses)) {
                    Logs.SQL.error("Quest {} does not contain any playable classes in `RequiredClasses` ({}), second set to 0 (all classes).", qinfo.id, qinfo.allowableClasses);
                    qinfo.allowableClasses = 0;
                }
            }

            // allowableRaces, can be -1/RACEMASK_ALL_PLAYABLE to allow any race
            if (qinfo.allowableRaces.getRawValue() != -1) {
                if (qinfo.allowableRaces.getRawValue() > 0 && !qinfo.allowableRaces.hasRaceMask(RaceMask.ALL_PLAYABLE)) {
                    Logs.SQL.error("Quest {} does not contain any playable races in `RequiredRaces` ({}), second set to 0 (all races).", qinfo.id, qinfo.allowableRaces);
                    qinfo.allowableRaces.setRawValue(-1);
                }
            }

            // requiredSkillId, can be 0
            if (qinfo.requiredSkillId != 0) {
                if (!dbcObjectManager.skillLine().contains(qinfo.requiredSkillId)) {
                    Logs.SQL.error("Quest {} has `RequiredSkillId` = {} but this skill does not exist", qinfo.id, qinfo.requiredSkillId);
                }
            }

            if (qinfo.requiredSkillPoints != 0) {
                if (qinfo.requiredSkillPoints > world.getWorldSettings().getConfigMaxSkillValue()) {
                    Logs.SQL.error("Quest {} has `RequiredSkillPoints` = {} but max possible skill is {}, quest can't be done.", qinfo.id, qinfo.requiredSkillPoints, world.getWorldSettings().getConfigMaxSkillValue());
                }
            }
            // no changes, quest can't be done for this requirement
            // else Skill quests can have 0 skill level, this is ok

            if (qinfo.requiredMinRepFaction != 0 && !dbcObjectManager.faction().contains(qinfo.requiredMinRepFaction)) {
                Logs.SQL.error("Quest {} has `RequiredMinRepFaction` = {} but faction template {} does not exist, quest can't be done.", qinfo.id, qinfo.requiredMinRepFaction, qinfo.requiredMinRepFaction);
            }

            // no changes, quest can't be done for this requirement
            if (qinfo.requiredMaxRepFaction != 0 && !dbcObjectManager.faction().contains(qinfo.requiredMaxRepFaction)) {
                Logs.SQL.error("Quest {} has `RequiredMaxRepFaction` = {} but faction template {} does not exist, quest can't be done.", qinfo.id, qinfo.requiredMaxRepFaction, qinfo.requiredMaxRepFaction);
            }

            // no changes, quest can't be done for this requirement
            if (qinfo.requiredMinRepValue > SharedDefine.REPUTATION_CAP) {
                Logs.SQL.error("Quest {} has `RequiredMinRepValue` = {} but max reputation is {}, quest can't be done.", qinfo.id, qinfo.requiredMinRepValue, SharedDefine.REPUTATION_CAP);
            }

            // no changes, quest can't be done for this requirement
            if (qinfo.requiredMinRepValue != 0 && qinfo.requiredMaxRepValue != 0 && qinfo.requiredMaxRepValue <= qinfo.requiredMinRepValue) {
                Logs.SQL.error("Quest {} has `RequiredMaxRepValue` = {} and `RequiredMinRepValue` = {}, quest can't be done.", qinfo.id, qinfo.requiredMaxRepValue, qinfo.requiredMinRepValue);
            }

            // no changes, quest can't be done for this requirement
            if (qinfo.requiredMinRepFaction == 0 && qinfo.requiredMinRepValue != 0) {
                Logs.SQL.error("Quest {} has `RequiredMinRepValue` = {} but `RequiredMinRepFaction` is 0, second has no effect", qinfo.id, qinfo.requiredMinRepValue);
            }

            // warning
            if (qinfo.requiredMaxRepFaction == 0 && qinfo.requiredMaxRepValue != 0) {
                Logs.SQL.error("Quest {} has `RequiredMaxRepValue` = {} but `RequiredMaxRepFaction` is 0, second has no effect", qinfo.id, qinfo.requiredMaxRepValue);
            }

            // warning
            if (qinfo.rewardTitleId != 0 && !dbcObjectManager.charTitle().contains(qinfo.rewardTitleId)) {
                Logs.SQL.error("Quest {} has `RewardTitleId` = {} but CharTitle Id {} does not exist, quest can't be rewarded with title.", qinfo.id, qinfo.rewardTitleId, qinfo.rewardTitleId);

                qinfo.rewardTitleId = 0;
                // quest can't reward this title
            }

            if (qinfo.sourceItemId != 0) {
                if (getItemTemplate(qinfo.sourceItemId) == null) {
                    Logs.SQL.error("Quest {} has `SourceItemId` = {} but item with entry {} does not exist, quest can't be done.", qinfo.id, qinfo.sourceItemId, qinfo.sourceItemId);

                    qinfo.sourceItemId = 0; // quest can't be done for this requirement
                } else if (qinfo.sourceItemIdCount == 0) {
                    Logs.SQL.error("Quest {} has `StartItem` = {} but `ProvidedItemCount` = 0, set to 1 but need fix in DB.", qinfo.id, qinfo.sourceItemId);
                    qinfo.sourceItemIdCount = 1; // update to 1 for allow quest work for backward compatibility with DB
                }
            } else if (qinfo.sourceItemIdCount > 0) {
                Logs.SQL.error("Quest {} has `SourceItemId` = 0 but `SourceItemIdCount` = {}, useless second.", qinfo.id, qinfo.sourceItemIdCount);

                qinfo.sourceItemIdCount = 0; // no quest work changes in fact
            }

            if (qinfo.sourceSpellID != 0) {
                var spellInfo = spellManager.getSpellInfo(qinfo.sourceSpellID, Difficulty.NONE);

                if (spellInfo == null) {
                    Logs.SQL.error("Quest {} has `SourceSpellid` = {} but spell {} doesn't exist, quest can't be done.", qinfo.id, qinfo.sourceSpellID, qinfo.sourceSpellID);

                    qinfo.sourceSpellID = 0; // quest can't be done for this requirement
                } else if (!spellManager.isSpellValid(spellInfo)) {
                    Logs.SQL.error("Quest {} has `SourceSpellid` = {} but spell {} is broken, quest can't be done.", qinfo.id, qinfo.sourceSpellID, qinfo.sourceSpellID);

                    qinfo.sourceSpellID = 0; // quest can't be done for this requirement
                }
            }

            for (var obj : qinfo.objectives) {
                // Store objective for lookup by id
                questObjectives.put(obj.id, obj);

                // Check storage index for objectives which store data
                if (obj.isStoringValue() && obj.storageIndex < 0) {
                    Logs.SQL.error("Quest {} objective {} has invalid storageIndex = {} for objective type {}", qinfo.id, obj.id, obj.storageIndex, obj.type);
                }

                switch (obj.type) {
                    case ITEM:
                        if (getItemTemplate(obj.objectID) == null) {
                            Logs.SQL.error("Quest {} objective {} has non existing item entry {}, quest can't be done.",
                                    qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case MONSTER, TALK_TO:
                        if (getCreatureTemplate(obj.objectID) == null) {
                            Logs.SQL.error("Quest {} objective {} has non existing creature entry {}, quest can't be done.",
                                    qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case GAME_OBJECT:
                        if (getGameObjectTemplate(obj.objectID) == null) {
                            Logs.SQL.error("Quest {} objective {} has non existing gameobject entry {}, quest can't be done.", qinfo.id, obj.id, obj.objectID);

                        }

                        break;

                    case MIN_REPUTATION:
                    case MAX_REPUTATION:
                    case INCREASE_REPUTATION:
                        if (!dbcObjectManager.faction().contains(obj.objectID)) {
                            Logs.SQL.error("Quest {} objective {} has non existing faction id {}", qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case PLAYER_KILLS:
                        if (obj.amount <= 0) {
                            Logs.SQL.error("Quest {} objective {} has invalid player kills count {}", qinfo.id, obj.id, obj.amount);
                        }

                        break;
                    case CURRENCY:
                    case HAVE_CURRENCY:
                    case OBTAIN_CURRENCY:
                        if (!dbcObjectManager.currencyType().contains(obj.objectID)) {
                            Logs.SQL.error("Quest {} objective {} has non existing currency {}", qinfo.id, obj.id, obj.objectID);
                        }

                        if (obj.amount <= 0) {
                            Logs.SQL.error("Quest {} objective {} has invalid currency amount {}", qinfo.id, obj.id, obj.amount);
                        }

                        break;
                    case LEARN_SPELL:
                        if (!spellManager.hasSpellInfo(obj.objectID, Difficulty.NONE)) {
                            Logs.SQL.error("Quest {} objective {} has non existing spell id {}", qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case WIN_PET_BATTLE_AGAINST_NPC:
                        if (obj.objectID != 0 && getCreatureTemplate(obj.objectID) == null) {
                            Logs.SQL.error("Quest {} objective {} has non existing creature entry {}, quest can't be done.", qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case DEFEAT_BATTLE_PET:
                        if (!dbcObjectManager.battlePetSpecie().contains(obj.objectID)) {
                            Logs.SQL.error("Quest {} objective {} has non existing battlepet species id {}", qinfo.id, obj.id, obj.objectID);

                        }

                        break;
                    case CRITERIA_TREE:
                        if (!dbcObjectManager.criteriaTree().contains(obj.objectID)) {
                            Logs.SQL.error("Quest {} objective {} has non existing criteria tree id {}", qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case AREA_TRIGGER:
                        if (!dbcObjectManager.areaTrigger().contains(obj.objectID) && obj.objectID != -1) {
                            Logs.SQL.error("Quest {} objective {} has non existing areaTrigger.db2 id {}", qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case AREA_TRIGGER_ENTER:
                    case AREA_TRIGGER_EXIT:
                        if (!areaTriggerTemplateStore.containsKey(new AreaTriggerId(obj.objectID, false))
                                && !areaTriggerTemplateStore.containsKey(new AreaTriggerId(obj.objectID, true))) {
                            Logs.SQL.error("Quest {} objective {} has non existing areatrigger id {}", qinfo.id, obj.id, obj.objectID);
                        }

                        break;
                    case MONEY:
                    case WIN_PVP_PET_BATTLES:
                    case PROGRESS_BAR:
                        break;
                    default:
                        Logs.SQL.error("Quest {} objective {} has unhandled type {}", qinfo.id, obj.id, obj.type);

                        break;
                }

                if (obj.flags.hasFlag(QuestObjectiveFlag.SEQUENCED)) {
                    qinfo.specialFlags.set(QuestSpecialFlag.SEQUENCED_OBJECTIVES);
                }
            }

            for (var j = 0; j < QuestTemplate.QUEST_ITEM_DROP_COUNT; j++) {
                var id = qinfo.itemDrop[j];

                if (id != 0) {
                    if (getItemTemplate(id) == null) {
                        Logs.SQL.error("Quest {} has `RequiredSourceItemId{}` = {} but item with entry {} does not exist, quest can't be done.", qinfo.id, j + 1, id, id);
                    }
                    // no changes, quest can't be done for this requirement
                } else {
                    if (qinfo.itemDropQuantity[j] > 0) {
                        Logs.SQL.error("Quest {} has `RequiredSourceItemId{}` = 0 but `RequiredSourceItemCount{}` = {}.", qinfo.id, j + 1, j+1,qinfo.itemDropQuantity[j]);
                    }
                    // no changes, quest ignore this data
                }
            }

            for (var j = 0; j < QuestTemplate.QUEST_REWARD_CHOICES_COUNT; ++j) {
                var id = qinfo.rewardChoiceItemId[j];

                if (id != 0) {
                    switch (qinfo.rewardChoiceItemType[j]) {
                        case ITEM:
                            if (getItemTemplate(id) == null) {
                                Logs.SQL.error("Quest {} has `RewardItemId{}` = {} but item with entry {} does not exist, quest will not reward this item.",
                                        qinfo.id, j+1, id, id);
                                qinfo.rewardChoiceItemId[j] = 0; // no changes, quest will not reward this
                            }

                            break;
                        case CURRENCY:
                            if (!dbcObjectManager.currencyType().contains(id)) {
                                Logs.SQL.error("Quest {} has `RewardChoiceItemId{}` = {} but currency with id {} does not exist, quest will not reward this currency.",
                                        qinfo.id, j + 1, id, id);
                                qinfo.rewardChoiceItemId[j] = 0;          // no changes, quest will not reward this
                            }

                            break;
                        default:
                            Logs.SQL.error("Quest {} has `RewardChoiceItemType{}` = {} but it is not a valid item type, reward removed.",
                                    qinfo.id, j + 1, qinfo.rewardChoiceItemType[j]);
                            qinfo.rewardChoiceItemId[j] = 0;

                            break;
                    }

                    if (qinfo.rewardChoiceItemCount[j] == 0) {
                        Logs.SQL.error("Quest {} has `RewardChoiceItemId{}` = {} but `RewardChoiceItemCount{}` = 0.", qinfo.id, j + 1, id, j + 1);
                    }
                } else if (qinfo.rewardChoiceItemCount[j] > 0) {
                    Logs.SQL.error("Quest {} has `RewardChoiceItemId{}` = 0 but `RewardChoiceItemCount{}` = {}.", qinfo.id, j + 1, j + 1, qinfo.rewardChoiceItemCount[j]);
                    // no changes, quest ignore this data
                }
            }

            for (var j = 0; j < QuestTemplate.QUEST_REWARD_ITEM_COUNT; ++j) {
                var id = qinfo.rewardItemId[j];

                if (id != 0) {
                    if (getItemTemplate(id) == null) {
                        Logs.SQL.error("Quest {} has `RewardItemId{}` = {} but item with entry {} does not exist, quest will not reward this item.", qinfo.id, j + 1, id, id);

                        qinfo.rewardItemId[j] = 0; // no changes, quest will not reward this item
                    }

                    if (qinfo.rewardItemCount[j] == 0) {
                        Logs.SQL.error("Quest {} has `RewardItemId{}` = {} but `RewardItemIdCount{}` = 0, quest will not reward this item.", qinfo.id, j + 1, id, j + 1);
                    }
                    // no changes
                } else if (qinfo.rewardItemCount[j] > 0) {
                    Logs.SQL.error("Quest {} has `RewardItemId{}` = 0 but `RewardItemIdCount{}` = {}.", qinfo.id, j + 1, j + 1, qinfo.rewardItemCount[j]);
                    // no changes, quest ignore this data
                }
            }

            for (var j = 0; j < QuestTemplate.QUEST_REWARD_REPUTATIONS_COUNT; ++j) {
                if (qinfo.rewardFactionId[j] != 0) {
                    if (Math.abs(qinfo.rewardFactionValue[j]) > 9) {
                        Logs.SQL.error("Quest {} has RewardFactionValueId{} = {}. That is outside the range of valid values (-9 to 9).", qinfo.id, j + 1, qinfo.rewardFactionValue[j]);
                    }

                    if (!dbcObjectManager.faction().contains(qinfo.rewardFactionId[j])) {
                        Logs.SQL.error("Quest {} has `RewardFactionId{}` = {} but raw faction (faction.dbc) {} does not exist, quest will not reward reputation for this faction.", qinfo.id, j + 1, qinfo.rewardFactionId[j], qinfo.rewardFactionId[j]);

                        qinfo.rewardFactionId[j] = 0; // quest will not reward this
                    }
                } else if (qinfo.rewardFactionOverride[j] != 0) {
                    Logs.SQL.error("Quest {} has `RewardFactionId{}` = 0 but `RewardFactionValueIdOverride{}` = {}.", qinfo.id, j + 1, j + 1, qinfo.rewardFactionOverride[j]);
                    // no changes, quest ignore this data
                }
            }

            if (qinfo.rewardSpell > 0) {
                var spellInfo = spellManager.getSpellInfo(qinfo.rewardSpell, Difficulty.NONE);

                if (spellInfo == null) {
                    Logs.SQL.error("Quest {} has `RewardSpellCast` = {} but spell {} does not exist, quest will not have a spell reward.", qinfo.id, qinfo.rewardSpell, qinfo.rewardSpell);

                    qinfo.rewardSpell = 0; // no spell will be casted on player
                } else if (!spellManager.isSpellValid(spellInfo)) {
                    Logs.SQL.error("Quest {} has `RewardSpellCast` = {} but spell {} is broken, quest will not have a spell reward.", qinfo.id, qinfo.rewardSpell, qinfo.rewardSpell);

                    qinfo.rewardSpell = 0; // no spell will be casted on player
                }
            }

            if (qinfo.rewardMailTemplateId != 0) {
                if (!dbcObjectManager.mailTemplate().contains(qinfo.rewardMailTemplateId)) {
                    Logs.SQL.error("Quest {} has `RewardMailTemplateId` = {} but mail template {} does not exist, quest will not have a mail reward.", qinfo.id, qinfo.rewardMailTemplateId, qinfo.rewardMailTemplateId);

                    qinfo.rewardMailTemplateId = 0; // no mail will send to player
                    qinfo.rewardMailDelay = 0; // no mail will send to player
                    qinfo.rewardMailSenderEntry = 0;
                } else if (usedMailTemplates.containsKey(qinfo.rewardMailTemplateId)) {
                    var usedId = usedMailTemplates.get(qinfo.rewardMailTemplateId);

                    Logs.SQL.error("Quest {} has `RewardMailTemplateId` = {} but mail template  {} already used for quest {}, quest will not have a mail reward.", qinfo.id, qinfo.rewardMailTemplateId, qinfo.rewardMailTemplateId, usedId);

                    qinfo.rewardMailTemplateId = 0; // no mail will send to player
                    qinfo.rewardMailDelay = 0; // no mail will send to player
                    qinfo.rewardMailSenderEntry = 0;
                } else {
                    usedMailTemplates.put(qinfo.rewardMailTemplateId, qinfo.id);
                }
            }

            if (qinfo.nextQuestInChain != 0) {
                if (!questTemplates.containsKey(qinfo.nextQuestInChain)) {
                    Logs.SQL.error("Quest {} has `NextQuestIdChain` = {} but quest {} does not exist, quest chain will not work.", qinfo.id, qinfo.nextQuestInChain, qinfo.nextQuestInChain);

                    qinfo.nextQuestInChain = 0;
                }
            }

            for (var j = 0; j < QuestTemplate.QUEST_REWARD_CURRENCY_COUNT; ++j) {
                if (qinfo.rewardCurrencyId[j] != 0) {
                    if (qinfo.rewardCurrencyCount[j] == 0) {
                        Logs.SQL.error("Quest {} has `RewardCurrencyId{}` = {} but `RewardCurrencyCount{}` = 0, quest can't be done.", qinfo.id, j + 1, qinfo.rewardCurrencyId[j], j + 1);
                    }

                    // no changes, quest can't be done for this requirement
                    if (!dbcObjectManager.currencyType().contains(qinfo.rewardCurrencyId[j])) {
                        Logs.SQL.error("Quest {} has `RewardCurrencyId{}` = {} but currency with entry {} does not exist, quest can't be done.", qinfo.id, j + 1, qinfo.rewardCurrencyId[j], qinfo.rewardCurrencyId[j]);

                        qinfo.rewardCurrencyCount[j] = 0; // prevent incorrect work of quest
                    }
                } else if (qinfo.rewardCurrencyCount[j] > 0) {
                    Logs.SQL.error("Quest {} has `RewardCurrencyId{}` = 0 but `RewardCurrencyCount{}` = {}, quest can't be done.", qinfo.id, j + 1, j + 1, qinfo.rewardCurrencyCount[j]);

                    qinfo.rewardCurrencyCount[j] = 0; // prevent incorrect work of quest
                }
            }

            if (qinfo.soundAccept != 0) {
                if (!dbcObjectManager.soundKit().contains(qinfo.soundAccept)) {
                    Logs.SQL.error("Quest {} has `SoundAccept` = {} but sound {} does not exist, set to 0.", qinfo.id, qinfo.soundAccept, qinfo.soundAccept);

                    qinfo.soundAccept = 0; // no sound will be played
                }
            }

            if (qinfo.soundTurnIn != 0) {
                if (!dbcObjectManager.soundKit().contains(qinfo.soundTurnIn)) {
                    Logs.SQL.error("Quest {} has `SoundTurnIn` = {} but sound {} does not exist, set to 0.", qinfo.id, qinfo.soundTurnIn, qinfo.soundTurnIn);

                    qinfo.soundTurnIn = 0; // no sound will be played
                }
            }

            if (qinfo.rewardSkillId > 0) {
                if (!dbcObjectManager.skillLine().contains(qinfo.rewardSkillId)) {
                    Logs.SQL.error("Quest {} has `RewardSkillId` = {} but this skill does not exist", qinfo.id, qinfo.rewardSkillId);
                }

                if (qinfo.rewardSkillPoints == 0) {
                    Logs.SQL.error("Quest {} has `RewardSkillId` = {} but `RewardSkillPoints` is 0", qinfo.id, qinfo.rewardSkillId);
                }
            }

            if (qinfo.rewardSkillPoints != 0) {
                if (qinfo.rewardSkillPoints > world.getWorldSettings().getConfigMaxSkillValue()) {
                    Logs.SQL.error("Quest {} has `RewardSkillPoints` = {} but max possible skill is {}, quest can't be done.", qinfo.id, qinfo.rewardSkillPoints, world.getWorldSettings().getConfigMaxSkillValue());
                }

                // no changes, quest can't be done for this requirement
                if (qinfo.rewardSkillId == 0) {
                    Logs.SQL.error("Quest {} has `RewardSkillPoints` = {} but `RewardSkillId` is 0", qinfo.id, qinfo.rewardSkillPoints);
                }
            }

            // fill additional data stores
            var prevQuestId = Math.abs(qinfo.prevQuestId);

            if (prevQuestId != 0) {
                var prevQuestItr = tmp.get(prevQuestId);

                if (prevQuestItr == null) {
                    Logs.SQL.error("Quest {} has PrevQuestId {}, but no such quest", qinfo.id, qinfo.prevQuestId);
                } else if (prevQuestItr.breadcrumbForQuestId != 0) {
                    Logs.SQL.error("Quest {} should not be unlocked by breadcrumb quest {}", qinfo.id, prevQuestId);
                } else if (qinfo.prevQuestId > 0) {
                    qinfo.dependentPreviousQuests.add(prevQuestId);
                }
            }

            if (qinfo.nextQuestId != 0) {
                var nextquest = tmp.get(qinfo.nextQuestId);

                if (nextquest == null) {
                    Logs.SQL.error("Quest {} has NextQuestId {}, but no such quest", qinfo.id, qinfo.nextQuestId);
                } else {
                    nextquest.dependentPreviousQuests.add(qinfo.id);
                }
            }

            var breadcrumbForQuestId = Math.abs(qinfo.breadcrumbForQuestId);

            if (breadcrumbForQuestId != 0) {
                if (!tmp.containsKey(breadcrumbForQuestId)) {
                    Logs.SQL.error(String.format("Quest %1$s is a breadcrumb for quest %2$s, but no such quest exists", qinfo.id, breadcrumbForQuestId));
                    qinfo.breadcrumbForQuestId = 0;
                }

                if (qinfo.nextQuestId != 0) {
                    Logs.SQL.error(String.format("Quest %1$s is a breadcrumb, should not unlock quest %2$s", qinfo.id, qinfo.nextQuestId));
                }
            }

            if (qinfo.exclusiveGroup != 0) {
                exclusiveQuestGroups.compute(qinfo.exclusiveGroup, Functions.addToList(qinfo.id));
            }
        }

        for (var questPair : tmp.entrySet()) {
            // skip post-loading checks for disabled quests
            if (disableManager.isDisabledFor(DisableType.QUEST, questPair.getKey(), null)) {
                continue;
            }

            var qinfo = questPair.getValue();
            var qid = qinfo.id;
            var breadcrumbForQuestId = Math.abs(qinfo.breadcrumbForQuestId);
            ArrayList<Integer> questSet = new ArrayList<>();

            while (breadcrumbForQuestId != 0) {
                //a previously visited quest was found as a breadcrumb quest
                //breadcrumb loop found!
                if (questSet.contains(qinfo.id)) {
                    Logs.SQL.error(String.format("Breadcrumb quests %1$s and %2$s are in a loop", qid, breadcrumbForQuestId));
                    qinfo.breadcrumbForQuestId = 0;

                    break;
                }

                questSet.add(qinfo.id);

                qinfo = tmp.get(breadcrumbForQuestId);

                //every quest has a list of every breadcrumb towards it
                qinfo.dependentBreadcrumbQuests.add(qid);

                breadcrumbForQuestId = Math.abs(qinfo.breadcrumbForQuestId);
            }
        }

        // don't check spells with SPELL_EFFECT_QUEST_COMPLETE, a lot of invalid db2 data


        // Make all paragon reward quests repeatable
        for (var paragonReputation : dbcObjectManager.paragonReputation()) {
            var quest = tmp.get(paragonReputation.getQuestID());

            if (quest != null) {
                quest.specialFlags.set(QuestSpecialFlag.REPEATABLE);
            }
        }

        questTemplates.putAll(tmp);

        Logs.SERVER_LOADING.info("Loaded {} quests definitions in {} ms", tmp.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadQuestStartersAndEnders() {
        long oldMSTime = System.currentTimeMillis();
        Logs.SERVER_LOADING.info("Loading GO Start Quest data...");
        AtomicInteger count = new AtomicInteger();
        try (var items = questRepo.streamAllGameObjectQuestStarter()) {
            items.forEach(fields -> {
                if (!questTemplates.containsKey(fields[1])) {
                    Logs.SQL.error("Table `gameobject_queststarter`: Quest {} listed for entry {} does not exist.", fields[1], fields[0]);
                    return;
                }
                GameObjectTemplate gameObjectTemplate = gameObjectTemplateStorage.get(fields[1]);
                if (gameObjectTemplate == null) {
                    Logs.SQL.error("Table `gameobject_queststarter` has data for nonexistent gameobject entry ({}) and existed quest {}", fields[0], fields[1]);
                    return;
                } else if (gameObjectTemplate.type != GameObjectType.QUEST_GIVER) {
                    Logs.SQL.error("Table `gameobject_queststarter` has data gameobject entry ({}) for quest {}, but GO is not GAMEOBJECT_TYPE_QUESTGIVER", fields[0], fields[1]);
                    return;
                }

                this.goQuestRelations.compute(fields[0], Functions.addToList(fields[1]));
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info("Loaded {} quest relations from `gameobject_queststarter` in {} ms", count, System.currentTimeMillis() - (oldMSTime));
        oldMSTime = System.currentTimeMillis();
        count.set(0);

        Logs.SERVER_LOADING.info("Loading GO End Quest data...");
        try (var items = questRepo.streamAllGameObjectQuestEnder()) {
            items.forEach(fields -> {
                if (!questTemplates.containsKey(fields[1])) {
                    Logs.SQL.error("Table `gameobject_questender`: Quest {} listed for entry {} does not exist.", fields[1], fields[0]);
                    return;
                }
                GameObjectTemplate gameObjectTemplate = gameObjectTemplateStorage.get(fields[1]);
                if (gameObjectTemplate == null) {
                    Logs.SQL.error("Table `gameobject_questender` has data for nonexistent gameobject entry ({}) and existed quest {}.", fields[0], fields[1]);
                    return;
                } else if (gameObjectTemplate.type != GameObjectType.QUEST_GIVER) {
                    Logs.SQL.error("Table `gameobject_questender` has data gameobject entry ({}) for quest {}, but GO is not GAMEOBJECT_TYPE_QUESTGIVER.", fields[0], fields[1]);
                    return;
                }
                this.creatureQuestRelations.compute(fields[0], Functions.addToList(fields[1]));
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info("Loaded {} quest relations from `gameobject_questender` in {} ms", count, System.currentTimeMillis() - (oldMSTime));
        oldMSTime = System.currentTimeMillis();
        count.set(0);

        Logs.SERVER_LOADING.info("Loading Creature Start Quest data...");
        try (var items = questRepo.streamAllCreatureQuestStarter()) {
            items.forEach(fields -> {
                if (!questTemplates.containsKey(fields[1])) {
                    Logs.SQL.error("Table `creature_queststarter`: Quest {} listed for entry {} does not exist.", fields[1], fields[0]);
                    return;
                }

                CreatureTemplate creatureTemplate = creatureTemplateStorage.get(fields[1]);
                if (creatureTemplate == null) {
                    Logs.SQL.error("Table `creature_queststarter` has data for nonexistent creature entry ({}) and existed quest {}", fields[0], fields[1]);
                    return;
                } else if (!creatureTemplate.npcFlag.hasFlag(NPCFlag.QUEST_GIVER)) {
                    Logs.SQL.error("Table `creature_queststarter` has data creature entry ({}) for quest {}, but npcflag does not include UNIT_NPC_FLAG_QUESTGIVER", fields[0], fields[1]);
                    return;
                }

                this.creatureQuestRelations.compute(fields[0], Functions.addToList(fields[1]));
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info("Loaded {} quest relations from `creature_queststarter` in {} ms", count, System.currentTimeMillis() - (oldMSTime));
        oldMSTime = System.currentTimeMillis();
        count.set(0);

        Logs.SERVER_LOADING.info("Loading Creature End Quest data...");
        try (var items = questRepo.streamAllCreatureQuestEnder()) {
            items.forEach(fields -> {
                if (!questTemplates.containsKey(fields[1])) {
                    Logs.SQL.error("Table `gameobject_questender`: Quest {} listed for entry {} does not exist.", fields[1], fields[0]);
                    return;
                }
                CreatureTemplate creatureTemplate = creatureTemplateStorage.get(fields[1]);
                if (creatureTemplate == null) {
                    Logs.SQL.error("Table `creature_questender` has data for nonexistent creature entry ({}) and existed quest {}", fields[0], fields[1]);
                    return;
                } else if (!creatureTemplate.npcFlag.hasFlag(NPCFlag.QUEST_GIVER)) {
                    Logs.SQL.error("Table `creature_questender` has data creature entry ({}) for quest {}, but npcflag does not include UNIT_NPC_FLAG_QUESTGIVER", fields[0], fields[1]);
                    return;
                }

                this.creatureQuestInvolvedRelations.compute(fields[0], Functions.addToList(fields[1]));
                this.creatureQuestInvolvedRelationsReverse.compute(fields[1], Functions.addToList(fields[0]));
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info("Loaded {} quest relations from `gameobject_questender` in {} ms", count, System.currentTimeMillis() - (oldMSTime));

    }

    public void loadQuestPOI() {
        var oldMSTime = System.currentTimeMillis();

        questPOIStorage.clear(); // need for reload case
        HashMap<Integer, HashMap<Integer, List<QuestPOIBlobPoint>>> allPoints = new HashMap<>();
        try (var items = questRepo.streamAllQuestPoiPoints()) {
            items.forEach(e -> {
                allPoints.compute(e.questId, Functions.ifAbsent(HashMap::new)).compute(e.idx1, Functions.addToList(e));
            });
        }


        try (var items = questRepo.streamAllQuestPoi()) {
            items.forEach(e -> {
                if (getQuestTemplate(e.questID) == null) {
                    Logs.SQL.error("`quest_poi` quest id ({}) Idx1 ({}) does not exist in `quest_template`", e.questID, e.idx1);
                    return;
                }
                var blobs = allPoints.get(e.questID);
                if (blobs != null) {
                    var points = blobs.get(e.idx1);

                    if (!points.isEmpty()) {
                        if (!questPOIStorage.containsKey(e.questID)) {
                            questPOIStorage.put(e.questID, new QuestPOIData(e.questID));
                        }
                        e.points = points;
                        var poiData = questPOIStorage.get(e.questID);
                        poiData.questID = e.questID;

                        poiData.blobs.add(e);
                    }
                }
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} quest POI definitions in {} ms", questPOIStorage.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadQuestAreaTriggers() {
        var oldMSTime = System.currentTimeMillis();

        questAreaTriggerStorage.clear(); // need for reload case
        AtomicInteger count = new AtomicInteger();
        try (var items = questRepo.streamAllAreaTriggerInvolvedRelation()) {
            items.forEach(fields -> {
                AreaTriggerEntry atEntry = dbcObjectManager.areaTrigger(fields[0]);
                if (atEntry == null)
                {
                    Logs.SQL.error("Area trigger (ID:{}) does not exist in `AreaTrigger.dbc`.", fields[0]);
                    return;
                }

                Quest quest = getQuestTemplate(fields[1]);

                if (quest == null)
                {
                    Logs.SQL.error("Table `areatrigger_involvedrelation` has record (id: {}) for not existing quest {}", fields[0], fields[1]);
                    return;
                }

                if (!quest.hasFlag(QuestFlag.COMPLETION_AREA_TRIGGER) && !quest.hasQuestObjectiveType(QuestObjectiveType.AREA_TRIGGER))
                {
                    Logs.SQL.error("Table `areatrigger_involvedrelation` has record (id: {}) for not quest {}, but quest not have flag QUEST_FLAGS_COMPLETION_AREA_TRIGGER and no objective with type QUEST_OBJECTIVE_AREATRIGGER. Trigger is obsolete, skipped.", fields[0], fields[1]);
                    return;
                }

                questAreaTriggerStorage.compute(fields[0], Functions.addToSet(fields[1]));
                count.getAndIncrement();
            });
        }

        for (var pair : questObjectives.entrySet()) {
            var objective = pair.getValue();

            if (objective.type == QuestObjectiveType.AREA_TRIGGER) {
                questAreaTriggerStorage.compute(objective.objectID, Functions.addToSet(objective.questID));
            }
        }
        Logs.SERVER_LOADING.info("Loaded {} quest trigger points in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadQuestGreetings() {
        var oldMSTime = System.currentTimeMillis();
        HashMap<Integer, QuestGreeting> tmp = new HashMap<>();
        AtomicInteger count = new AtomicInteger();
        try (var items = questRepo.streamAllQuestGreeting()) {
            items.forEach(e -> {
                switch (e.type) {
                    case 0: // Creature
                        if (getCreatureTemplate(e.id) == null) {
                            Logs.SQL.error("Table `quest_greeting`: creature template entry {} does not exist.", e.id);
                            return;
                        }

                        break;
                    case 1: // GameObject
                        if (getGameObjectTemplate(e.id) == null) {
                            Logs.SQL.error("Table `quest_greeting`: gameobject template entry {} does not exist.", e.id);
                            return;
                        }
                        break;
                    default:
                        return;
                }
                count.getAndIncrement();
                tmp.put((e.id << 1 | e.type), e);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} Quest Greeting locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);
        count.set(0);
        oldMSTime = System.currentTimeMillis();
        try(var items = questRepo.streamAllQuestGreetingLocale()) {
            items.forEach(e -> {
                QuestGreeting questGreeting = tmp.get(e.id << 1 | e.type);
                if(questGreeting == null) {
                    Logs.SQL.error("Table `quest_greeting_locale`: quest greeting entry {} type {} does not exist.", e.id, e.type);
                    return;
                }
                questGreeting.text.set(Locale.values()[e.locale], e.greeting);
            });
        }
        questGreetingStorage.putAll(tmp);
        Logs.SERVER_LOADING.info(">> Loaded {} Quest Greeting locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public Quest getQuestTemplate(int questId) {
        QuestTemplate questTemplate = questTemplates.get(questId);
        if(questTemplate == null) {
            return null;
        }
        return new Quest(questTemplate, world.getWorldSettings(), dbcObjectManager);
    }




    public Map<Integer, List<Integer>> getGOQuestRelationMapHACK() {
        return goQuestRelations;
    }

    public QuestRelationResult getGOQuestRelations(int entry) {
        return getQuestRelationsFrom(goQuestRelations, entry, true);
    }

    public QuestRelationResult getGOQuestInvolvedRelations(int entry) {
        return getQuestRelationsFrom(goQuestInvolvedRelations, entry, false);
    }

    public List<Integer> getGOQuestInvolvedRelationReverseBounds(int questId) {
        return goQuestInvolvedRelationsReverse.get(questId);
    }


    public QuestRelationResult getCreatureQuestRelations(int entry) {
        return getQuestRelationsFrom(creatureQuestRelations, entry, true);
    }

    public QuestRelationResult getCreatureQuestInvolvedRelations(int entry) {
        return getQuestRelationsFrom(creatureQuestInvolvedRelations, entry, false);
    }

    public List<Integer> getCreatureQuestInvolvedRelationReverseBounds(int questId) {
        return creatureQuestInvolvedRelationsReverse.get(questId);
    }

    public QuestPOIData getQuestPOIData(int questId) {
        return questPOIStorage.get(questId);
    }

    public QuestObjective getQuestObjective(int questObjectiveId) {
        return questObjectives.get(questObjectiveId);
    }

    public Set<Integer> getQuestsForAreaTrigger(int triggerId) {
        return questAreaTriggerStorage.get(triggerId);
    }

    public QuestGreeting getQuestGreeting(TypeId type, int id) {
        byte typeIndex;

        if (type == TypeId.UNIT) {
            typeIndex = 0;
        } else if (type == TypeId.GAME_OBJECT) {
            typeIndex = 1;
        } else {
            return null;
        }

        return questGreetingStorage.get((id << 1) | typeIndex);
    }


    public List<Integer> getExclusiveQuestGroupBounds(int exclusiveGroupId) {
        return exclusiveQuestGroups.get(exclusiveGroupId);
    }

    //Spells /Skills / Phases
    public void loadPhases() {
        for (var phase : dbcObjectManager.phase()) {
            phaseInfoById.put(phase.getId(), new PhaseInfoStruct(phase.getId()));
        }

        for (var map : dbcObjectManager.map()) {
            if (map.getParentMapID() != -1) {
                terrainSwapInfoById.put(map.getId(), new TerrainSwapInfo(map.getId()));
            }
        }

        Logs.SERVER_LOADING.info("Loading Terrain World Map definitions...");
        loadTerrainWorldMaps();

        Logs.SERVER_LOADING.info("Loading Terrain Swap Default definitions...");
        loadTerrainSwapDefaults();

        Logs.SERVER_LOADING.info("Loading Phase Area definitions...");
        loadAreaPhases();
    }

    public void unloadPhaseConditions() {

        phaseInfoByArea.forEach((k,v) -> v.forEach(value -> value.conditions.clear()));
    }

    public void loadNPCSpellClickSpells() {
        var oldMSTime = System.currentTimeMillis();

        spellClickInfoStorage.clear();
        AtomicInteger count = new AtomicInteger();
        try(var items = creatureRepo.streamAllNpcSpellClickSpells()) {
            items.forEach(fields -> {
                CreatureTemplate cInfo = getCreatureTemplate(fields[0]);
                if (cInfo == null)
                {
                    Logs.SQL.error("Table npc_spellclick_spells references unknown creature_template {}. Skipping entry.", fields[0]);
                    return;
                }

                SpellInfo spellinfo = spellManager.getSpellInfo(fields[1], Difficulty.NONE);
                if (spellinfo == null)
                {
                    Logs.SQL.error("Table npc_spellclick_spells creature: {} references unknown spellid {}. Skipping entry.", fields[0], fields[1]);
                    return;
                }

                if (fields[3] > SpellClickUserType.values().length)
                    Logs.SQL.error("Table npc_spellclick_spells creature: {} references unknown user type {}. Skipping entry.", fields[0], fields[3]);

                spellClickInfoStorage.compute(fields[1], Functions.addToList(new SpellClickInfo(fields[1], (byte) fields[2], SpellClickUserType.values()[fields[3]])));
                count.getAndIncrement();
            });
        }


        // all spellclick data loaded, now we check if there are creatures with NPC_FLAG_SPELLCLICK but with no data
        // NOTE: It *CAN* be the other way around: no spellclick flag but with spellclick data, in case of creature-only vehicle accessories
        var ctc = creatureTemplateStorage.values();

        for (var kv : creatureTemplateStorage.entrySet()) {
            var creature = kv.getValue();
            if (creature.npcFlag.hasFlag(NPCFlag.SPELL_CLICK) && !spellClickInfoStorage.containsKey(kv.getKey())) {
                Logs.SQL.error("npc_spellclick_spells: Creature template {} has UNIT_NPC_FLAG_SPELLCLICK but no data in spellclick table! Removing flag", kv.getKey());
                creature.npcFlag.removeFlag(NPCFlag.SPELL_CLICK);
                creatureTemplateStorage.put(kv.getKey(), creature);
            }
        }

        Logs.SERVER_LOADING.info(">> Loaded {} spellclick definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadFishingBaseSkillLevel() {
        var oldMSTime = System.currentTimeMillis();

        fishingBaseForArea.clear(); // for reload case

        try(var items = miscRepo.streamAllSkillFishingBaseLevel()) {
            items.forEach(fields -> {
                var fArea = dbcObjectManager.areaTable(fields[0]);
                if (fArea == null)
                {
                    Logs.SQL.error("AreaId {} defined in `skill_fishing_base_level` does not exist", fields[0]);
                    return;
                }
                fishingBaseForArea.put(fields[0], fields[1]);
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} areas for fishing base skill level in {} ms", fishingBaseForArea.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadSkillTiers() {
        var oldMSTime = System.currentTimeMillis();

        skillTiers.clear();

        try(var items = miscRepo.streamAllSkillTiers()) {
            items.forEach(fields -> {
                SkillTiersEntry tier = skillTiers.get(fields[0]);
                System.arraycopy(fields, 1, tier.value, 0, SkillTiersEntry.MAX_SKILL_STEP);
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} skill max values in {} ms", skillTiers.size(), System.currentTimeMillis() - oldMSTime);
    }

    public PhaseInfoStruct getPhaseInfo(int phaseId) {
        return phaseInfoById.get(phaseId);
    }

    public List<PhaseAreaInfo> getPhasesForArea(int areaId) {
        return phaseInfoByArea.get(areaId);
    }

    public TerrainSwapInfo getTerrainSwapInfo(int terrainSwapId) {
        return terrainSwapInfoById.get(terrainSwapId);
    }

    public List<SpellClickInfo> getSpellClickInfoMapBounds(int creature_id) {
        return spellClickInfoStorage.get(creature_id);
    }

    public int getFishingBaseSkillLevel(int entry) {
        return fishingBaseForArea.get(entry);
    }

    public SkillTiersEntry getSkillTier(int skillTierId) {
        return skillTiers.get(skillTierId);
    }


    //Locales
    public void loadCreatureLocales(HashMap<Integer, CreatureTemplate> templateHashMap) {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = creatureRepo.streamAllCreatureTemplateLocale()) {
            items.forEach(e -> {
                CreatureTemplate creatureTemplate = templateHashMap.get(e.entry);
                if (creatureTemplate == null)
                {
                    Logs.SQL.error("Creature template (Entry: {}) does not exist but has a record in `creature_template_locale`", e.entry);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                creatureTemplate.name.set(locale, e.name);
                creatureTemplate.femaleName.set(locale, e.nameAlt);
                creatureTemplate.subName.set(locale, e.title);
                creatureTemplate.titleAlt.set(locale, e.titleAlt);
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public void loadGameObjectLocales(HashMap<Integer, GameObjectTemplate> templateHashMap) {
        var oldMSTime = System.currentTimeMillis();

        gameObjectLocaleStorage.clear(); // need for reload case

        AtomicInteger count = new AtomicInteger();
        try(var items = gameObjectRepo.streamAllGameObjectTemplateLocales()) {
            items.forEach(e -> {
                GameObjectTemplate gameObjectTemplate = templateHashMap.get(e.entry);
                if (gameObjectTemplate == null)
                {
                    Logs.SQL.error("GameObject template (Entry: {}) does not exist but has a record in `gameobject_template_locale`", e.entry);
                    return;
                }
                Locale locale = Locale.values()[e.locale];
                gameObjectTemplate.name.set(locale, e.name);
                gameObjectTemplate.castBarCaption.set(locale, e.castBarCaption);
                gameObjectTemplate.unk1.set(locale, e.unk1);
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} gameobject_template_locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }




    public void loadQuestGreetingLocales() {
        var oldMSTime = System.currentTimeMillis();

    }



    public GameObjectLocale getGameObjectLocale(int entry) {
        return gameObjectLocaleStorage.get(entry);
    }




    //General
    public void loadReputationRewardRate() {
        var oldMSTime = System.currentTimeMillis();

        repRewardRateStorage.clear(); // for reload case

        try (var reputationRewardRates = reputationRepo.streamReputationRewardRate()) {
            reputationRewardRates.forEach(e -> {
                Faction faction = dbcObjectManager.faction(e.factionId);
                if (faction == null) {
                    Logs.SQL.error("Faction (faction.dbc) {} does not exist but is used in `reputation_reward_rate`", e.factionId);
                    return;
                }

                if (e.questRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has quest_rate with invalid rate {}, skipping data for faction {}", e.questRate, e.factionId);
                    return;
                }

                if (e.questDailyRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has quest_daily_rate with invalid rate {}, skipping data for faction {}", e.questDailyRate, e.factionId);
                    return;
                }

                if (e.questWeeklyRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has quest_weekly_rate with invalid rate {}, skipping data for faction {}", e.questWeeklyRate, e.factionId);
                    return;
                }

                if (e.questMonthlyRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has quest_monthly_rate with invalid rate {}, skipping data for faction {}", e.questMonthlyRate, e.factionId);
                    return;
                }

                if (e.questRepeatableRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has quest_repeatable_rate with invalid rate {}, skipping data for faction {}", e.questRepeatableRate, e.factionId);
                    return;
                }

                if (e.creatureRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has creature_rate with invalid rate {}, skipping data for faction {}", e.creatureRate, e.factionId);
                    return;
                }

                if (e.spellRate < 0.0f) {
                    Logs.SQL.error("Table reputation_reward_rate has spell_rate with invalid rate {}, skipping data for faction {}", e.spellRate, e.factionId);
                    return;
                }

                repRewardRateStorage.put(e.factionId, e);
            });
        }

        if (!repRewardRateStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded {} reputation_reward_rate in {} ms", repRewardRateStorage.size(), System.currentTimeMillis() - oldMSTime);
        } else {
            Logs.SERVER_LOADING.info(">> Loaded `reputation_reward_rate`, table is empty!");
        }
    }

    public void loadReputationOnKill() {
        var oldMSTime = System.currentTimeMillis();

        // For reload case
        repOnKillStorage.clear();

        try (var reputationOnKills = reputationRepo.streamAllCreatureOnKillReputation()) {
            reputationOnKills.forEach(e -> {
                if (getCreatureTemplate(e.creatureId) == null) {
                    Logs.SQL.error("Table `creature_onkill_reputation` has data for nonexistent creature entry ({}), skipped", e.creatureId);
                    return;
                }
                if (e.repFaction1 != 0 && dbcObjectManager.faction(e.repFaction1) == null) {
                    Logs.SQL.error("Faction (faction.dbc) {} does not exist but is used in `creature_onkill_reputation`", e.repFaction1);
                    return;
                }
                if (e.repFaction2 != 0 && dbcObjectManager.faction(e.repFaction2) == null) {
                    Logs.SQL.error("Faction (faction.dbc) {} does not exist but is used in `creature_onkill_reputation`", e.repFaction2);
                    return;
                }
                repOnKillStorage.put(e.creatureId, e);
            });
        }

        if (!repOnKillStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded {} creature award reputation definitions in {} ms", repOnKillStorage.size(), System.currentTimeMillis() - oldMSTime);
        } else {
            Logs.SERVER_LOADING.info(">> Loaded 0 creature award reputation definitions. DB table `creature_onkill_reputation` is empty.");
        }
    }

    public void loadReputationSpilloverTemplate() {
        var oldMSTime = System.currentTimeMillis();

        repSpilloverTemplateStorage.clear(); // for reload case

        try (var result = reputationRepo.streamAllReputationSpilloverTemplate()) {
            result.forEach(e -> {
                var faction = dbcObjectManager.faction(e.factionId);
                if (faction == null) {
                    Logs.SQL.error("Faction (faction.dbc) {} does not exist but is used in `reputation_spillover_template`", e.factionId);
                    return;
                }

                if (faction.getParentFactionID() == 0) {
                    Logs.SQL.error("Faction (faction.dbc) {} in `reputation_spillover_template` does not belong to any team, skipping", e.factionId);
                    return;
                }

                boolean invalidSpilloverFaction = false;
                for (int i = 0; i < SharedDefine.MAX_SPILLOVER_FACTIONS; ++i) {
                    if (e.faction[i] != 0) {
                        Faction factionSpillover = dbcObjectManager.faction(e.faction[i]);

                        if (factionSpillover == null) {
                            Logs.SQL.error("Spillover faction (faction.dbc) {} does not exist but is used in `reputation_spillover_template` for faction {}, skipping", e.faction[i], e.factionId);
                            invalidSpilloverFaction = true;
                            break;
                        }

                        if (!factionSpillover.canHaveReputation()) {
                            Logs.SQL.error("Spillover faction (faction.dbc) {} for faction {} in `reputation_spillover_template` can not be listed for client, and then useless, skipping", e.faction[i], e.factionId);
                            invalidSpilloverFaction = true;
                            break;
                        }

                        if (e.factionRank[i] >= SharedDefine.MAX_REPUTATION_RANK) {
                            Logs.SQL.error("Rank {} used in `reputation_spillover_template` for spillover faction {} is not valid, skipping", e.factionRank[i], e.faction[i]);
                            invalidSpilloverFaction = true;
                            break;
                        }
                    }
                }
                if (invalidSpilloverFaction)
                    return;
                repSpilloverTemplateStorage.put(e.factionId, e);
            });
        }
        ;

        if (!repSpilloverTemplateStorage.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded {} reputation_spillover_template in {} ms", repSpilloverTemplateStorage.size(), System.currentTimeMillis() - oldMSTime);
        } else {
            Logs.SERVER_LOADING.info(">> Loaded `reputation_spillover_template`, table is empty.");
        }

    }

    public void loadTavernAreaTriggers() {
        var oldMSTime = System.currentTimeMillis();

        tavernAreaTriggerStorage.clear(); // need for reload case

        List<Integer> result = miscRepo.queryAllTavernAreaTriggers();


        if (result.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 tavern triggers. DB table `areatrigger_tavern` is empty.");

            return;
        }


        tavernAreaTriggerStorage.addAll(result);

        Logs.SERVER_LOADING.info(">> Loaded {} tavern triggers in {} ms", result.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadMailLevelRewards() {
        var oldMSTime = System.currentTimeMillis();

        mailLevelRewardStorage.clear(); // for reload case

        Map<Byte, List<MailLevelReward>> tmp = new HashMap<>();
        try (var mailLevelRewards = miscRepo.streamsAllMailLevelReward()) {
            mailLevelRewards.forEach(e -> {
                if (e.level > SharedDefine.MAX_LEVEL) {
                    Logs.SQL.error("Table `mail_level_reward` has data for level {} that more supported by client ({}), ignoring.", e.level, SharedDefine.MAX_LEVEL);
                    return;
                }

                if (!RaceMask.ALL_PLAYABLE.hasRaceMask(e.raceMask)) {
                    Logs.SQL.error("Table `mail_level_reward` has raceMask ({}) for level {} that not include any player races, ignoring.", e.raceMask, e.level);
                    return;
                }

                if (dbcObjectManager.mailTemplate(e.mailTemplateId) == null) {
                    Logs.SQL.error("Table `mail_level_reward` has invalid mailTemplateId ({}) for level {} that invalid not include any player races, ignoring.", e.mailTemplateId, e.level);
                    return;
                }

                if (getCreatureTemplate(e.senderEntry) == null) {
                    Logs.SQL.error("Table `mail_level_reward` has nonexistent sender creature entry ({}) for level {} that invalid not include any player races, ignoring.", e.senderEntry, e.level);
                    return;
                }
                tmp.compute(e.level, Functions.addToList(e));
            });
        }

        if (tmp.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 level dependent mail rewards. DB table `mail_level_reward` is empty.");
            return;
        } else {
            mailLevelRewardStorage.putAll(tmp);
            Logs.SERVER_LOADING.info(">> Loaded {} level dependent mail rewards in {} ms", tmp.size(), System.currentTimeMillis() - oldMSTime);

        }
    }

    public void loadExplorationBaseXP() {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var explorationBaseXps = miscRepo.streamsAllExplorationBaseXp()) {
            explorationBaseXps.forEach(e -> {
                if (e.id > 0 && e.id < baseXPTable.length) {
                    baseXPTable[e.id] = e.basexp;
                } else {
                    Logs.SQL.error("Table `exploration_basexp` has worry level {} the level must be 0 - {}, skipped.", e.id, baseXPTable.length);
                }
                count.getAndIncrement();
            });
        }

        if (count.get() == 0) {
            Logs.SERVER_LOADING.info(">> Loaded 0 BaseXP definitions. DB table `exploration_basexp` is empty.");
        } else {
            Logs.SERVER_LOADING.info(">> Loaded {} BaseXP definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);
        }

    }

    public void loadTempSummons() {
        var oldMSTime = System.currentTimeMillis();

        // needed for reload case
        tempSummonDataStorage.clear();

        Map<Tuple<Integer, SummonerType, Short>, List<TempSummonData>> tmp = new HashMap<>();
        AtomicInteger count = new AtomicInteger();
        try (var creatureSummonGroupStream = creatureRepo.streamsAllTempSummon()) {
            creatureSummonGroupStream.forEach(e -> {
                SummonerType[] values = SummonerType.values();
                if (e.summonerType == null) {
                    Logs.SQL.error("Table `creature_summon_groups` has unhandled summoner type {} for summoner {}, skipped.", e.summonerType, e.summonerId);
                    return;
                }
                switch (e.summonerType) {
                    case CREATURE:
                        if (getCreatureTemplate(e.summonerId) == null) {
                            Logs.SQL.error("Table `creature_summon_groups` has summoner with non existing entry {} for creature summoner type, skipped.", e.summonerId);

                            return;
                        }

                        break;
                    case GAME_OBJECT:
                        if (getGameObjectTemplate(e.summonerId) == null) {
                            Logs.SQL.error("Table `creature_summon_groups` has summoner with non existing entry {} for gameobject summoner type, skipped.", e.summonerId);

                            return;
                        }

                        break;
                    case MAP:
                        if (dbcObjectManager.map(e.summonerId) == null) {
                            Logs.SQL.error("Table `creature_summon_groups` has summoner with non existing entry {} for map summoner type, skipped.", e.summonerId);

                            return;
                        }
                }

                if (e.summonType > TempSummonType.values().length) {
                    Logs.SQL.error("Table `creature_summon_groups` has unhandled temp summon type {} in group [Summoner ID: {}, Summoner Type: {}, Group ID: {}] for creature entry {}, skipped.", e.summonType, e.summonerId, e.summonerType, e.groupId, e.entry);
                    return;
                }

                var key = Tuple.of(e.summonerId, e.summonerType, e.groupId);
                tmp.compute(key, Functions.addToList(e));
                count.getAndIncrement();
            });
            tempSummonDataStorage.putAll(tmp);
            Logs.SERVER_LOADING.info(">> Loaded {} temp summons in {} ms", count, System.currentTimeMillis() - oldMSTime);
        }
    }

    public void loadPageTexts() {
        var oldMSTime = System.currentTimeMillis();
        Map<Integer, PageText> tmp = new HashMap<>();
        try (var pageTexts = miscRepo.streamAllPageText()) {
            pageTexts.forEach(e -> tmp.put(e.id, e));
        }
        for (var pair : tmp.entrySet()) {
            if (pair.getValue().nextPageID != 0) {
                if (!tmp.containsKey(pair.getValue().nextPageID)) {
                    Logs.SQL.error("Page text (ID: {}) has non-existing `NextPageID` ({})", pair.getKey(), pair.getValue().nextPageID);
                }
            }
        }
        if (tmp.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 page texts. DB table `page_text` is empty!");
        } else {
            pageTextStorage.putAll(tmp);
            Logs.SERVER_LOADING.info(">> Loaded {} page texts in {} ms", tmp.size(), System.currentTimeMillis() - oldMSTime);
        }
        oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var item = miscRepo.streamAllPageTextLocale()) {
            item.forEach(e -> {
                PageText pageText = tmp.get(e.id);
                if(pageText == null) {
                    Logs.SQL.error("Table `page_text_locale` has non-existing `page_text` ({})", e.id);
                    return;
                }
                pageText.text.set(Locale.values()[e.locale], e.text);
                count.getAndIncrement();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} PageText locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);

    }



    public void loadSceneTemplates() {
        var oldMSTime = System.currentTimeMillis();
        sceneTemplateStorage.clear();

        try (var item = miscRepo.streamAllSceneTemplates()) {
            item.forEach(e -> {
                sceneTemplateStorage.put(e.sceneId, e);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} scene templates in {} ms.", sceneTemplateStorage.size(), System.currentTimeMillis() - oldMSTime);
    }

    public void loadPlayerChoices() {
        var oldMSTime = System.currentTimeMillis();
        playerChoices.clear();
        HashMap<Integer, PlayerChoice> tmp = new HashMap<>();
        AtomicInteger responseCount = new AtomicInteger();
        AtomicInteger rewardCount = new AtomicInteger();
        AtomicInteger itemRewardCount = new AtomicInteger();
        AtomicInteger currencyRewardCount = new AtomicInteger();
        AtomicInteger factionRewardCount = new AtomicInteger();
        AtomicInteger itemChoiceRewardCount = new AtomicInteger();
        AtomicInteger mawPowersCount = new AtomicInteger();
        try (var items = playerRepo.streamsAllPlayerChoice()) {
            items.forEach(e -> tmp.put(e.choiceId, e));
        }

        try (var items = playerRepo.streamsAllPlayerChoiceResponse()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseID);

                    return;
                }
                playerChoice.responses.add(e);
                responseCount.getAndIncrement();
            });
        }
        try (var items = playerRepo.streamsAllPlayerChoiceResponseReward()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseId);

                    return;
                }

                var anyMatch = playerChoice.responses.stream().filter(response -> response.responseID == e.responseId).findFirst();
                if (anyMatch.isEmpty()) {
                    Logs.SQL.error("Table `playerchoice_response_reward` references non-existing ResponseId: {} for ChoiceId {}, skipped", e.responseId, e.choiceId);
                    return;
                }

                if (e.titleID != 0 && dbcObjectManager.charTitle(e.titleID) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward` references non-existing Title {} for ChoiceId {}, ResponseId: {}, set to 0",
                            e.titleID, e.choiceId, e.responseId);
                    e.titleID = 0;
                }

                if (e.packageID != 0 && dbcObjectManager.getQuestPackageItems(e.packageID) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward` references non-existing QuestPackage {} for ChoiceId {}, ResponseId: {}, set to 0",
                            e.titleID, e.choiceId, e.responseId);
                    e.packageID = 0;
                }

                if (e.skillLineID != 0 && dbcObjectManager.skillLine(e.skillLineID) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward` references non-existing SkillLine {} for ChoiceId {}, ResponseId: {}, set to 0",
                            e.titleID, e.choiceId, e.responseId);
                    e.skillLineID = 0;
                    e.skillPointCount = 0;
                }
                anyMatch.get().reward = e;
                rewardCount.getAndIncrement();
            });
        }

        try (var items = playerRepo.streamsAllPlayerChoiceResponseRewardItem()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseId);
                    return;
                }
                var anyMatch = playerChoice.responses.stream().filter(response -> response.responseID == e.responseId).findFirst();
                if (anyMatch.isEmpty()) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item` references non-existing ResponseId: {} for ChoiceId {}, skipped", e.responseId, e.choiceId);
                    return;
                }


                if (anyMatch.get().reward == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item` references non-existing player choice reward for ChoiceId {}, ResponseId: {}, skipped",
                            e.choiceId, e.responseId);
                    return;
                }

                if (getItemTemplate(e.itemId) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item` references non-existing item {} for ChoiceId {}, ResponseId: {}, skipped",
                            e.itemId, e.choiceId, e.responseId);
                    return;
                }
                anyMatch.get().reward.items.add(e);
                itemRewardCount.getAndIncrement();
            });
        }

        try (var items = playerRepo.streamsAllPlayerChoiceResponseRewardCurrency()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_currency` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseId);
                    return;
                }
                var anyMatch = playerChoice.responses.stream().filter(response -> response.responseID == e.responseId).findFirst();
                if (anyMatch.isEmpty()) {
                    Logs.SQL.error("Table `playerchoice_response_reward_currency` references non-existing ResponseId: {} for ChoiceId {}, skipped", e.responseId, e.choiceId);
                    return;
                }


                if (anyMatch.get().reward == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_currency` references non-existing player choice reward for ChoiceId {}, ResponseId: {}, skipped",
                            e.choiceId, e.responseId);
                    return;
                }

                if (dbcObjectManager.currencyType(e.currencyId) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_currency` references non-existing currency {} for ChoiceId {}, ResponseId: {}, skipped",
                            e.currencyId, e.choiceId, e.responseId);
                    return;
                }
                anyMatch.get().reward.currencies.add(e);
                currencyRewardCount.getAndIncrement();
            });
        }

        try (var items = playerRepo.streamsAllPlayerChoiceResponseRewardFaction()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_faction` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseId);
                    return;
                }
                var anyMatch = playerChoice.responses.stream().filter(response -> response.responseID == e.responseId).findFirst();
                if (anyMatch.isEmpty()) {
                    Logs.SQL.error("Table `playerchoice_response_reward_faction` references non-existing ResponseId: {} for ChoiceId {}, skipped", e.responseId, e.choiceId);
                    return;
                }


                if (anyMatch.get().reward == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_faction` references non-existing player choice reward for ChoiceId {}, ResponseId: {}, skipped",
                            e.choiceId, e.responseId);
                    return;
                }

                if (dbcObjectManager.faction(e.factionId) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_faction` references non-existing faction {} for ChoiceId {}, ResponseId: {}, skipped",
                            e.factionId, e.choiceId, e.responseId);
                    return;
                }
                anyMatch.get().reward.factions.add(e);
                factionRewardCount.getAndIncrement();
            });
        }

        try (var items = playerRepo.streamsAllPlayerChoiceResponseRewardItemChoice()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item_choice` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseId);
                    return;
                }
                var anyMatch = playerChoice.responses.stream().filter(response -> response.responseID == e.responseId).findFirst();
                if (anyMatch.isEmpty()) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item_choice` references non-existing ResponseId: {} for ChoiceId {}, skipped", e.responseId, e.choiceId);
                    return;
                }


                if (anyMatch.get().reward == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item_choice` references non-existing player choice reward for ChoiceId {}, ResponseId: {}, skipped",
                            e.choiceId, e.responseId);
                    return;
                }

                if (getItemTemplate(e.itemId) == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item_choice` references non-existing item {} for ChoiceId {}, ResponseId: {}, skipped",
                            e.itemId, e.choiceId, e.responseId);
                    return;
                }
                anyMatch.get().reward.itemChoices.add(e);
                itemChoiceRewardCount.getAndIncrement();
            });
        }

        try (var items = playerRepo.streamsAllPlayerChoiceResponseRewardMawPower()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = tmp.get(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_maw_power` references non-existing ChoiceId: {} (ResponseId: {}), skipped", e.choiceId, e.responseId);
                    return;
                }
                var anyMatch = playerChoice.responses.stream().filter(response -> response.responseID == e.responseId).findFirst();
                if (anyMatch.isEmpty()) {
                    Logs.SQL.error("Table `playerchoice_response_maw_power` references non-existing ResponseId: {} for ChoiceId {}, skipped", e.responseId, e.choiceId);
                    return;
                }


                if (anyMatch.get().reward == null) {
                    Logs.SQL.error("Table `playerchoice_response_reward_item_choice` references non-existing player choice reward for ChoiceId {}, ResponseId: {}, skipped",
                            e.choiceId, e.responseId);
                    return;
                }


                anyMatch.get().mawPower = e;
                mawPowersCount.getAndIncrement();
            });
        }

        playerChoices.putAll(tmp);
        Logs.SERVER_LOADING.info(">> Loaded {} player choices, {} responses, {} rewards, {} item rewards, {} currency rewards, {} faction rewards, {} item choice rewards and {} maw powers in {} ms.",
                tmp.size(), responseCount, rewardCount, itemRewardCount, currencyRewardCount, factionRewardCount, itemChoiceRewardCount, mawPowersCount, System.currentTimeMillis() - oldMSTime);
    }

    public void loadPlayerChoicesLocale() {
        var oldMSTime = System.currentTimeMillis();

        // need for reload case
        AtomicInteger count = new AtomicInteger();
        try (var items = playerRepo.streamsAllPlayerChoiceLocale()) {
            items.forEach(e->{
                PlayerChoice playerChoice = getPlayerChoice(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_locale` references non-existing ChoiceId: {} for locale {}, skipped", e.choiceId, e.locale);
                    return;
                }
                if (e.locale > Locale.values().length) {
                    Logs.SQL.error("Table `playerchoice_locale` has worry locale {}, skipped", e.locale);
                    return;
                }
                playerChoice.question.set(Locale.values()[e.locale], e.question);
                playerChoices.put(e.choiceId, playerChoice);
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.error(">> Loaded {} Player Choice locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);

        oldMSTime = System.currentTimeMillis();
        count.set(0);
        try (var items = playerRepo.streamsAllPlayerChoiceResponseLocale()) {
            items.forEach(e -> {
                PlayerChoice playerChoice = getPlayerChoice(e.choiceId);
                if (playerChoice == null) {
                    Logs.SQL.error("Table `playerchoice_response_locale` references non-existing ChoiceId: {} for locale {}, skipped", e.choiceId, e.locale);
                    return;
                }
                if (e.locale > Locale.values().length) {
                    Logs.SQL.error("Table `playerchoice_response_locale` has worry locale {}, skipped", e.locale);
                    return;
                }
                PlayerChoiceResponse response = playerChoice.getResponse(e.responseId);
                if (response == null) {
                    Logs.SQL.error("Table `playerchoice_response_locale` references non-existing ResponseId: {} for locale {}, skipped", e.responseId, e.locale);
                    return;
                }

                Locale locale = Locale.values()[e.locale];
                response.answer.set(locale, e.answer);
                response.buttonTooltip.set(locale, e.buttonTooltip);
                response.confirmation.set(locale, e.confirmation);
                response.header.set(locale, e.header);
                response.description.set(locale, e.description);
                response.subHeader.set(locale, e.subHeader);
                playerChoices.put(e.choiceId, playerChoice);
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} Player Choice Response locale strings in {} ms", count, System.currentTimeMillis() - oldMSTime);

    }


    public void loadJumpChargeParams() {
        var oldMSTime = System.currentTimeMillis();

        // need for reload case
        jumpChargeParams.clear();

        var result = miscRepo.queryAllJumpChargeParams();

        if (result.isEmpty()) {
            return;
        }

        result.forEach(e -> {

            if (e.speed <= 0.0f) {
                Logs.SQL.error("Table `jump_charge_params` uses invalid speed {} for id {}, set to default charge speed {}.",
                        e.speed, e.id, MotionMaster.SPEED_CHARGE);
                e.speed = MotionMaster.SPEED_CHARGE;
            }

            if (e.jumpGravity <= 0.0f) {
                Logs.SQL.error("Table `jump_charge_params` uses invalid jump gravity {} for id {}, set to default {}.",
                        e.jumpGravity, e.id, MotionMaster.GRAVITY);
                e.jumpGravity = MotionMaster.GRAVITY;
            }

            if (e.spellVisualId != null) {

                if (dbcObjectManager.spellVisual(e.spellVisualId) == null)
                    Logs.SQL.error("Table `jump_charge_params` references non-existing SpellVisual: {} for id {}, ignored.",
                            e.spellVisualId, e.id);
                else
                    e.spellVisualId = null;

            }

            if (e.progressCurveId != null) {
                if (dbcObjectManager.curve(e.progressCurveId) == null)
                    Logs.SQL.error("Table `jump_charge_params` references non-existing progress Curve: {} for id {}, ignored.",
                            e.progressCurveId, e.id);
                else
                    e.parabolicCurveId = null;
            }

            if (e.parabolicCurveId != null) {
                if (dbcObjectManager.curve(e.parabolicCurveId) == null)
                    Logs.SQL.error("Table `jump_charge_params` references non-existing parabolic Curve: {} for id {}, ignored.",
                            e.parabolicCurveId, e.id);
                else
                    e.parabolicCurveId = null;
            }

            jumpChargeParams.put(e.id, e);
        });
        Logs.SERVER_LOADING.info(">> Loaded {} Player Choice locale strings in {} ms", jumpChargeParams.size, System.currentTimeMillis() - oldMSTime);
    }

    public void loadPhaseNames() {
        var oldMSTime = System.currentTimeMillis();
        phaseNameStorage.clear();
        try(var items = miscRepo.streamAllPhaseName()) {
            items.forEach(fields -> {
                phaseNameStorage.put((Integer) fields[0], fields[1].toString());
            });
        }
        Logs.SERVER_LOADING.info(String.format("Loaded %1$s phase names in %2$s ms.", phaseNameStorage.size(), System.currentTimeMillis() - oldMSTime));
    }

    public MailLevelReward getMailLevelReward(int level, long raceMask) {
        var mailList = mailLevelRewardStorage.get((byte) level);

        if (mailList.isEmpty()) {
            return null;
        }

        for (var mailReward : mailList) {
            if ((mailReward.raceMask & raceMask) != 0) {
                return mailReward;
            }
        }

        return null;
    }

    public RepRewardRate getRepRewardRate(int factionId) {
        return repRewardRateStorage.get(factionId);
    }

    public RepSpilloverTemplate getRepSpillover(int factionId) {
        return repSpilloverTemplateStorage.get(factionId);
    }

    public ReputationOnKill getReputationOnKilEntry(int id) {
        return repOnKillStorage.get(id);
    }


    public int getBaseXP(int level) {
        return level > 0 && level < baseXPTable.length ? baseXPTable[level] : 0;
    }

    public int getXPForLevel(int level) {
        if (level < playerXPperLevel.length) {
            return playerXPperLevel[level];
        }
        return 0;
    }


    public GridSpawnData getGridObjectGuids(int mapId, Difficulty difficulty, int gridId) {
        var key = Tuple.of(mapId, difficulty, gridId);
        return mapGridSpawnDataStorage.get(key);
    }


    public PageText getPageText(int pageEntry) {
        return pageTextStorage.get(pageEntry);
    }

    public int getNearestTaxiNode(float x, float y, float z, int mapId, Team team) {
        var found = false;
        float dist = 10000;
        int id = 0;

        Function<TaxiNode, Boolean> isVisibleForFaction = (node) -> {
            return switch (team) {
                case HORDE -> node.flags().hasFlag(TaxiNodeFlag.ShowOnHordeMap);
                case ALLIANCE -> node.flags().hasFlag(TaxiNodeFlag.ShowOnAllianceMap);
                default -> false;
            };
        };

        for (TaxiNode node : dbcObjectManager.taxiNode()) {
            if (node.getContinentID() != mapId || !isVisibleForFaction.apply(node) || node.flags().hasFlag(TaxiNodeFlag.IgnoreForFindNearest))
                continue;

            int field = ((node.getId() - 1) / 8);
            int subMask = 1 << ((node.getId() - 1) % 8);

            // skip not taxi network nodes
            if ((dbcObjectManager.getTaxiNodesMask().get(field) & subMask) == 0)
                continue;

            float dist2 = (node.getPosX() - x) * (node.getPosX() - x) + (node.getPosY() - y) * (node.getPosY() - y) + (node.getPosZ() - z) * (node.getPosZ() - z);
            if (found) {
                if (dist2 < dist) {
                    dist = dist2;
                    id = node.getId();
                }
            } else {
                found = true;
                dist = dist2;
                id = node.getId();
            }
        }

        return id;
    }

    public int getTaxiMountDisplayId(int id, Team team) {
        return getTaxiMountDisplayId(id, team, false);
    }

    public int getTaxiMountDisplayId(int id, Team team, boolean allowed_alt_team) {
        CreatureModel mountModel = new CreatureModel();
        CreatureTemplate mount_info = null;

        // select mount creature id
        var node = dbcObjectManager.taxiNode(id);

        if (node != null) {
            int mount_entry;

            if (team == Team.ALLIANCE) {
                mount_entry = node.getMountCreatureID2();
            } else {
                mount_entry = node.getMountCreatureID1();
            }

            // Fix for Alliance not being able to use Acherus taxi
            // only one mount type for both sides
            if (mount_entry == 0 && allowed_alt_team) {
                // Simply reverse the selection. At least one team in theory should have a valid mount ID to choose.
                mount_entry = team == Team.ALLIANCE ? node.getMountCreatureID1() : node.getMountCreatureID2();
            }

            mount_info = getCreatureTemplate(mount_entry);

            if (mount_info != null) {
                var model = mount_info.getRandomValidModel();

                if (model == null) {
                    Logs.SQL.error(String.format("No displayid found for the taxi mount with the entry %1$s! Can't load it!", mount_entry));

                    return 0;
                }

                mountModel = model;
            }
        }

        // minfo is not actually used but the mount_id was updated
        getCreatureModelRandomGender(mountModel, mount_info);

        return mountModel.creatureDisplayId;
    }

    public AreaTriggerStruct getAreaTrigger(int trigger) {
        return areaTriggerStorage.get(trigger);
    }

    public AccessRequirement getAccessRequirement(int mapId, Difficulty difficulty) {
        int id = mapId << 8 | difficulty.ordinal();
        return accessRequirementStorage.get(id);
    }

    public boolean isTavernAreaTrigger(int Trigger_ID) {
        return tavernAreaTriggerStorage.contains(Trigger_ID);
    }

    public AreaTriggerStruct getGoBackTrigger(int map) {
        Integer parentId = null;
        var mapEntry = dbcObjectManager.map(map);
        if (mapEntry == null || mapEntry.getCorpseMapID() < 0) {
            return null;
        }

        if (mapEntry.isDungeon()) {
            var iTemplate = getInstanceTemplate(map);

            if (iTemplate != null) {
                parentId = iTemplate.parent;
            }
        }

        var entrance_map = (parentId == null ? (int) mapEntry.getCorpseMapID() : parentId);

        for (var pair : areaTriggerStorage.entrySet()) {
            if (pair.getValue().target_mapId == entrance_map) {
                var atEntry = dbcObjectManager.areaTrigger(pair.getKey());

                if (atEntry != null && atEntry.getContinentID() == map) {
                    return pair.getValue();
                }
            }
        }

        return null;
    }

    public AreaTriggerStruct getMapEntranceTrigger(int Map) {
        for (var pair : areaTriggerStorage.entrySet()) {
            if (pair.getValue().target_mapId == Map) {
                var atEntry = dbcObjectManager.areaTrigger(pair.getKey());

                if (atEntry != null) {
                    return pair.getValue();
                }
            }
        }

        return null;
    }

    public SceneTemplate getSceneTemplate(int sceneId) {
        return sceneTemplateStorage.get(sceneId);
    }

    public List<TempSummonData> getSummonGroup(int summonerId, SummonerType summonerType, byte group) {
        var key = Tuple.of(summonerId, summonerType, group);

        return tempSummonDataStorage.get(key);
    }


    public JumpChargeParams getJumpChargeParams(int id) {
        return jumpChargeParams.get(id);
    }

    public String getPhaseName(int phaseId) {
        return phaseNameStorage.get(phaseId);
    }

    //Vehicles
    private void loadVehicleTemplate() {
        Logs.SERVER_LOADING.info("Loading vehicle templates...");
        long oldMSTime = System.currentTimeMillis();

        vehicleTemplateStore.clear();

        try (var result = vehicleRepo.streamAllVehicleTemplates()) {

            result.forEach(item -> {
                if (getCreatureTemplate(item.getCreatureId()) == null) {
                    Logs.SQL.error("Table `vehicle_template`: Vehicle {} does not exist.", item.getCreatureId());
                    return;
                }
                vehicleTemplateStore.put(item.getCreatureId(), item);
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} Vehicle Template entries in {} ms", vehicleTemplateStore.size(), System.currentTimeMillis() - oldMSTime);
    }

    private void loadVehicleTemplateAccessories() {
        Logs.SERVER_LOADING.info("Loading vehicle template accessories...");
        long oldMSTime = System.currentTimeMillis();

        vehicleTemplateAccessoryStore.clear(); // needed for reload case

        AtomicInteger count = new AtomicInteger();

        try (var result = vehicleRepo.streamAllVehicleTemplateAccessories()) {

            result.forEach(item -> {

                if (null == getCreatureTemplate(item.getEntry())) {
                    Logs.SQL.error("Table `vehicle_template_accessory`: creature template entry {} does not exist.", item.getEntry());
                    return;
                }

                if (null == getCreatureTemplate(item.getAccessoryEntry())) {
                    Logs.SQL.error("Table `vehicle_template_accessory`: Accessory {} does not exist.", item.getAccessoryEntry());
                    return;
                }

                if (spellClickInfoStorage.get(item.getEntry()) == null) {
                    Logs.SQL.error("Table `vehicle_template_accessory`: creature template entry {} has no data in npc_spellclick_spells", item.getEntry());
                    return;
                }
                vehicleTemplateAccessoryStore.compute(item.getEntry(), Functions.addToList(item));
                count.incrementAndGet();
            });


        }

        Logs.SERVER_LOADING.info(">> Loaded {} Vehicle Template Accessories in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    private void loadVehicleAccessories() {
        Logs.SERVER_LOADING.info("Loading vehicle accessories...");
        long oldMSTime = System.currentTimeMillis();

        vehicleAccessoryStore.clear(); // needed for reload case

        AtomicInteger count = new AtomicInteger();

        try (var result = vehicleRepo.streamAllVehicleAccessories()) {
            result.forEach(item ->{
                if (getCreatureTemplate(item.getAccessoryEntry()) == null) {
                    Logs.SQL.error("Table `vehicle_accessory`: Accessory {} does not exist.", item.getAccessoryEntry());
                    return;
                }

                if (item.getRideSpellID() != null && null == spellManager.getSpellInfo(item.getRideSpellID(), Difficulty.NONE)) {
                    Logs.SQL.error("Table `vehicle_accessory`: rideSpellId {} does not exist for guid {}.", item.getRideSpellID(), item.getEntry());
                    return;
                }

                vehicleAccessoryStore.compute(item.getEntry(), Functions.addToList(item));
                count.incrementAndGet();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} Vehicle Accessories in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    private void loadVehicleSeatAddon() {
        Logs.SERVER_LOADING.info("Loading vehicle seat addons...");
        long oldMSTime = System.currentTimeMillis();

        vehicleSeatAddonStore.clear(); // needed for reload case

        AtomicInteger count = new AtomicInteger();

        try (var result = vehicleRepo.streamAllVehicleSeatAddons()) {
            result.forEach(item -> {

                if (!dbcObjectManager.vehicleSeat().contains(item.getSeatEntry())) {
                    Logs.SQL.error("Table `vehicle_seat_addon`: SeatID: {} does not exist in VehicleSeat.dbc. Skipping entry.", item.getSeatEntry());
                    return;
                }

                // Sanitizing values
                if (item.getSeatOrientation() > (float) Math.PI * 2) {
                    Logs.SQL.error("Table `vehicle_seat_addon`: SeatID: {} is using invalid angle offset value ({}). Set Value to 0.", item.getSeatEntry(), item.getSeatOrientation());
                    item.setSeatOrientation(0.0f);
                }

                vehicleSeatAddonStore.put(item.getSeatEntry(), item);
                count.incrementAndGet();
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} Vehicle Seat Addon entries in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    public VehicleTemplate getVehicleTemplate(Vehicle veh) {
        return vehicleTemplateStore.get(veh.getCreatureEntry());
    }

    public List<VehicleAccessory> getVehicleAccessoryList(Vehicle veh) {
        var cre = veh.getBase().toCreature();

        if (cre != null) {
            // Give preference to GUID-based accessories
            var list = vehicleAccessoryStore.get(cre.getSpawnId());

            if (!list.isEmpty()) {
                return list;
            }
        }
        // Otherwise return entry-based
        return vehicleTemplateAccessoryStore.get(veh.getCreatureEntry());
    }

    public VehicleSeatAddon getVehicleSeatAddon(int seatId) {
        return vehicleSeatAddonStore.get(seatId);
    }


    private String getScriptCommandName(ScriptCommand command) {
        return switch (command) {
            case TALK -> "SCRIPT_COMMAND_TALK";
            case EMOTE -> "SCRIPT_COMMAND_EMOTE";
            case FIELD_SET_DEPRECATED -> "SCRIPT_COMMAND_FIELD_SET_DEPRECATED";
            case MOVE_TO -> "SCRIPT_COMMAND_MOVE_TO";
            case FLAG_SET_DEPRECATED -> "SCRIPT_COMMAND_FLAG_SET_DEPRECATED";
            case FLAG_REMOVE_DEPRECATED -> "SCRIPT_COMMAND_FLAG_REMOVE_DEPRECATED";
            case TELEPORT_TO -> "SCRIPT_COMMAND_TELEPORT_TO";
            case QUEST_EXPLORED -> "SCRIPT_COMMAND_QUEST_EXPLORED";
            case KILL_CREDIT -> "SCRIPT_COMMAND_KILL_CREDIT";
            case RESPAWN_GAMEOBJECT -> "SCRIPT_COMMAND_RESPAWN_GAMEOBJECT";
            case TEMP_SUMMON_CREATURE -> "SCRIPT_COMMAND_TEMP_SUMMON_CREATURE";
            case OPEN_DOOR -> "SCRIPT_COMMAND_OPEN_DOOR";
            case CLOSE_DOOR -> "SCRIPT_COMMAND_CLOSE_DOOR";
            case ACTIVATE_OBJECT -> "SCRIPT_COMMAND_ACTIVATE_OBJECT";
            case REMOVE_AURA -> "SCRIPT_COMMAND_REMOVE_AURA";
            case CAST_SPELL -> "SCRIPT_COMMAND_CAST_SPELL";
            case PLAY_SOUND -> "SCRIPT_COMMAND_PLAY_SOUND";
            case CREATE_ITEM -> "SCRIPT_COMMAND_CREATE_ITEM";
            case DESPAWN_SELF -> "SCRIPT_COMMAND_DESPAWN_SELF";
            case LOAD_PATH -> "SCRIPT_COMMAND_LOAD_PATH";
            case CALLSCRIPT_TO_UNIT -> "SCRIPT_COMMAND_CALLSCRIPT_TO_UNIT";
            case KILL -> "SCRIPT_COMMAND_KILL";
            // TrinityCore only
            case ORIENTATION -> "SCRIPT_COMMAND_ORIENTATION";
            case EQUIP -> "SCRIPT_COMMAND_EQUIP";
            case MODEL -> "SCRIPT_COMMAND_MODEL";
            case CLOSE_GOSSIP -> "SCRIPT_COMMAND_CLOSE_GOSSIP";
            case PLAYMOVIE -> "SCRIPT_COMMAND_PLAYMOVIE";
            case MOVEMENT -> "SCRIPT_COMMAND_MOVEMENT";
            case PLAY_ANIMKIT -> "SCRIPT_COMMAND_PLAY_ANIMKIT";

        };
    }


    private void loadScripts(ScriptsType type) {
        long oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        boolean isSpellType = type == ScriptsType.SPELL;
        var scripts = isSpellType ? spellScripts : eventScripts;
        String tableName = isSpellType ? "spell_scripts" : "event_scripts";
        try (var result = isSpellType ? miscRepo.streamAllSpellScripts() : miscRepo.streamAllEventScripts()) {

            result.forEach(fields -> {
                ScriptInfo tmp = new ScriptInfo();

                tmp.type = type;
                tmp.id = (Integer) fields[0];
                if (isSpellType)
                    tmp.id |= (Byte) fields[10] << 24;
                tmp.delay = (Integer) fields[1];
                tmp.command = ScriptCommand.values()[(Integer) fields[2]];
                tmp.raw.nData[0] = (Integer) fields[3];
                tmp.raw.nData[1] = (Integer) fields[4];
                tmp.raw.nData[2] = (Integer) fields[5];
                tmp.raw.fData[0] = (Float) fields[6];
                tmp.raw.fData[1] = (Float) fields[7];
                tmp.raw.fData[2] = (Float) fields[8];
                tmp.raw.fData[3] = (Float) fields[9];

                // generic command args check
                switch (tmp.command) {
                    case TALK: {
                        if (ChatMsg.WHISPER.compareTo(tmp.talk.chatType) < 0 && tmp.talk.chatType != ChatMsg.RAID_BOSS_WHISPER) {

                            Logs.SQL.error("Table `{}` has invalid talk type (datalong = {}) in SCRIPT_COMMAND_TALK for script id {}",
                                    tableName, tmp.talk.chatType, tmp.id);
                            return;
                        }
                        if (!dbcObjectManager.broadcastText().contains(tmp.talk.textID)) {
                            Logs.SQL.error("Table `{}` has invalid talk text id (dataint = {}) in SCRIPT_COMMAND_TALK for script id {}",
                                    tableName, tmp.talk.textID, tmp.id);
                            return;
                        }

                        break;
                    }

                    case EMOTE: {
                        if (dbcObjectManager.emote().contains(tmp.emote.emoteID)) {
                            Logs.SQL.error("Table `{}` has invalid emote id (datalong = {}) in SCRIPT_COMMAND_EMOTE for script id {}",
                                    tableName, tmp.emote.emoteID, tmp.id);
                            return;
                        }
                        break;
                    }

                    case TELEPORT_TO: {
                        if (!dbcObjectManager.map().contains(tmp.teleportTo.mapID)) {
                            Logs.SQL.error("Table `{}` has invalid map (Id: {}) in SCRIPT_COMMAND_TELEPORT_TO for script id {}",
                                    tableName, tmp.teleportTo.mapID, tmp.id);
                            return;
                        }

                        if (!MapDefine.isValidMapCoordinate(tmp.teleportTo.destX, tmp.teleportTo.destY, tmp.teleportTo.destZ, tmp.teleportTo.orientation)) {
                            Logs.SQL.error("Table `{}` has invalid coordinates (X: {} Y: {} Z: {} O: {}) in SCRIPT_COMMAND_TELEPORT_TO for script id {}",
                                    tableName, tmp.teleportTo.destX, tmp.teleportTo.destY, tmp.teleportTo.destZ, tmp.teleportTo.orientation, tmp.id);
                            return;
                        }
                        break;
                    }

                    case QUEST_EXPLORED: {
                        Quest quest = getQuestTemplate(tmp.questExplored.questID);
                        if (quest == null) {
                            Logs.SQL.error("Table `{}` has invalid quest (ID: {}) in SCRIPT_COMMAND_QUEST_EXPLORED in `datalong` for script id {}",
                                    tableName, tmp.questExplored.questID, tmp.id);
                            return;
                        }

                        if (!quest.hasFlag(QuestFlag.COMPLETION_EVENT) && !quest.hasFlag(QuestFlag.COMPLETION_AREA_TRIGGER)) {
                            Logs.SQL.error("Table `{}` has quest (ID: {}) in SCRIPT_COMMAND_QUEST_EXPLORED in `datalong` for script id {}, but quest not have QUEST_FLAGS_COMPLETION_EVENT or QUEST_FLAGS_COMPLETION_AREA_TRIGGER in quest flags. Script command will do nothing.",
                                    tableName, tmp.questExplored.questID, tmp.id);
                            return;
                        }

                        if (tmp.questExplored.distance > ObjectDefine.DEFAULT_VISIBILITY_DISTANCE) {
                            Logs.SQL.error("Table `{}` has too large distance ({}) for exploring objective complete in `datalong2` in SCRIPT_COMMAND_QUEST_EXPLORED in `datalong` for script id {}",
                                    tableName, tmp.questExplored.distance, tmp.id);
                            return;
                        }

                        if (tmp.questExplored.distance != 0 && tmp.questExplored.distance > ObjectDefine.DEFAULT_VISIBILITY_DISTANCE) {
                            Logs.SQL.error("Table `{}` has too large distance ({}) for exploring objective complete in `datalong2` in SCRIPT_COMMAND_QUEST_EXPLORED in `datalong` for script id {}, max distance is {} or 0 for disable distance check",
                                    tableName, tmp.questExplored.distance, tmp.id, ObjectDefine.DEFAULT_VISIBILITY_DISTANCE);
                            return;
                        }

                        if (tmp.questExplored.distance != 0 && tmp.questExplored.distance < ObjectDefine.INTERACTION_DISTANCE) {
                            Logs.SQL.error("Table `{}` has too small distance ({}) for exploring objective complete in `datalong2` in SCRIPT_COMMAND_QUEST_EXPLORED in `datalong` for script id {}, min distance is {} or 0 for disable distance check",
                                    tableName, tmp.questExplored.distance, tmp.id, ObjectDefine.INTERACTION_DISTANCE);
                            return;
                        }

                        break;
                    }

                    case KILL_CREDIT: {
                        if (getCreatureTemplate(tmp.killCredit.creatureEntry) == null) {
                            Logs.SQL.error("Table `{}` has invalid creature (Entry: {}) in SCRIPT_COMMAND_KILL_CREDIT for script id {}",
                                    tableName, tmp.killCredit.creatureEntry, tmp.id);
                            return;
                        }
                        break;
                    }

                    case RESPAWN_GAMEOBJECT: {
                        GameObjectData data = getGameObjectData(tmp.respawnGameObject.goGuid);
                        if (data == null) {
                            Logs.SQL.error("Table `{}` has invalid gameobject (GUID: {}) in SCRIPT_COMMAND_RESPAWN_GAMEOBJECT for script id {}",
                                    tableName, tmp.respawnGameObject.goGuid, tmp.id);
                            return;
                        }

                        GameObjectTemplate info = getGameObjectTemplate(data.id);
                        if (info == null) {
                            Logs.SQL.error("Table `{}` has gameobject with invalid entry (GUID: {} Entry: {}) in SCRIPT_COMMAND_RESPAWN_GAMEOBJECT for script id {}",
                                    tableName, tmp.respawnGameObject.goGuid, data.id, tmp.id);
                            return;
                        }

                        if (Set.of(GameObjectType.FISHING_NODE, GameObjectType.DOOR, GameObjectType.BUTTON, GameObjectType.TRAP).contains(info.type)) {
                            Logs.SQL.error("Table `{}` has gameobject type ({}) unsupported by command SCRIPT_COMMAND_RESPAWN_GAMEOBJECT for script id {}",
                                    tableName, info.entry, tmp.id);
                            return;
                        }
                        break;
                    }

                    case TEMP_SUMMON_CREATURE: {
                        if (!MapDefine.isValidMapCoordinate(tmp.tempSummonCreature.posX, tmp.tempSummonCreature.posY, tmp.tempSummonCreature.posZ, tmp.tempSummonCreature.orientation)) {
                            Logs.SQL.error("Table `{}` has invalid coordinates (X: {} Y: {} Z: {} O: {}) in SCRIPT_COMMAND_TEMP_SUMMON_CREATURE for script id {}",
                                    tableName, tmp.tempSummonCreature.posX, tmp.tempSummonCreature.posY, tmp.tempSummonCreature.posZ, tmp.tempSummonCreature.orientation, tmp.id);
                            return;
                        }

                        if (getCreatureTemplate(tmp.tempSummonCreature.creatureEntry) == null) {
                            Logs.SQL.error("Table `{}` has invalid creature (Entry: {}) in SCRIPT_COMMAND_TEMP_SUMMON_CREATURE for script id {}",
                                    tableName, tmp.tempSummonCreature.creatureEntry, tmp.id);
                            return;
                        }
                        break;
                    }

                    case OPEN_DOOR:
                    case CLOSE_DOOR: {
                        GameObjectData data = getGameObjectData(tmp.toggleDoor.goGuid);
                        if (data == null) {
                            Logs.SQL.error("Table `{}` has invalid gameobject (GUID: {}) in {} for script id {}",
                                    tableName, tmp.toggleDoor.goGuid, getScriptCommandName(tmp.command), tmp.id);
                            return;
                        }

                        GameObjectTemplate info = getGameObjectTemplate(data.id);
                        if (info == null) {
                            Logs.SQL.error("Table `{}` has gameobject with invalid entry (GUID: {} Entry: {}) in {} for script id {}",
                                    tableName, tmp.toggleDoor.goGuid, data.id, getScriptCommandName(tmp.command), tmp.id);
                            return;
                        }

                        if (info.type != GameObjectType.DOOR) {
                            Logs.SQL.error("Table `{}` has gameobject type ({}) unsupported by command {} for script id {}",
                                    tableName, info.entry, getScriptCommandName(tmp.command), tmp.id);
                            return;
                        }

                        break;
                    }

                    case REMOVE_AURA: {
                        if (null == spellManager.getSpellInfo(tmp.removeAura.spellID, Difficulty.NONE)) {
                            Logs.SQL.error("Table `{}` using non-existent spell (id: {}) in SCRIPT_COMMAND_REMOVE_AURA for script id {}",
                                    tableName, tmp.removeAura.spellID, tmp.id);
                            return;
                        }
                        if ((tmp.removeAura.flags & ~0x1) == 0)                    // 1 bits (0, 1)
                        {
                            Logs.SQL.error("Table `{}` using unknown flags in datalong2 ({}) in SCRIPT_COMMAND_REMOVE_AURA for script id {}",
                                    tableName, tmp.removeAura.flags, tmp.id);
                            return;
                        }
                        break;
                    }

                    case CAST_SPELL: {
                        if (null == spellManager.getSpellInfo(tmp.castSpell.spellID, Difficulty.NONE)) {
                            Logs.SQL.error("Table `{}` using non-existent spell (id: {}) in SCRIPT_COMMAND_CAST_SPELL for script id {}",
                                    tableName, tmp.castSpell.spellID, tmp.id);
                            return;
                        }
                        if (tmp.castSpell.flags > 4)                      // targeting type
                        {
                            Logs.SQL.error("Table `{}` using unknown target in datalong2 ({}) in SCRIPT_COMMAND_CAST_SPELL for script id {}",
                                    tableName, tmp.castSpell.flags, tmp.id);
                            return;
                        }
                        if (tmp.castSpell.flags != 4 && (tmp.castSpell.creatureEntry & ~0x1) == 0)                      // 1 bit (0, 1)
                        {
                            Logs.SQL.error("Table `{}` using unknown flags in dataint ({}) in SCRIPT_COMMAND_CAST_SPELL for script id {}",
                                    tableName, tmp.castSpell.creatureEntry, tmp.id);
                            return;
                        } else if (tmp.castSpell.flags == 4 && null == getCreatureTemplate(tmp.castSpell.creatureEntry)) {
                            Logs.SQL.error("Table `{}` using invalid creature entry in dataint ({}) in SCRIPT_COMMAND_CAST_SPELL for script id {}",
                                    tableName, tmp.castSpell.creatureEntry, tmp.id);
                            return;
                        }
                        break;
                    }

                    case CREATE_ITEM: {
                        if (null == getItemTemplate(tmp.createItem.itemEntry)) {
                            Logs.SQL.error("Table `{}` has nonexistent item (entry: {}) in SCRIPT_COMMAND_CREATE_ITEM for script id {}",
                                    tableName, tmp.createItem.itemEntry, tmp.id);
                            return;
                        }
                        if (tmp.createItem.amount == 0) {
                            Logs.SQL.error("Table `{}` SCRIPT_COMMAND_CREATE_ITEM but amount is {} for script id {}",
                                    tableName, tmp.createItem.amount, tmp.id);
                            return;
                        }
                        break;
                    }
                    case PLAY_ANIMKIT: {
                        if (!dbcObjectManager.animKit().contains(tmp.playAnimKit.animKitID)) {
                            Logs.SQL.error("Table `{}` has invalid AnimKid id (datalong = {}) in SCRIPT_COMMAND_PLAY_ANIMKIT for script id {}",
                                    tableName, tmp.playAnimKit.animKitID, tmp.id);
                            return;
                        }
                        break;
                    }
                    case FIELD_SET_DEPRECATED:
                    case FLAG_SET_DEPRECATED:
                    case FLAG_REMOVE_DEPRECATED: {
                        Logs.SQL.error("Table `{}` uses deprecated direct updatefield modify command {} for script id {}", tableName, getScriptCommandName(tmp.command), tmp.id);
                        return;
                    }
                    default:
                        break;
                }

                scripts.compute(tmp.id, Functions.addToMap(tmp.delay, tmp));

                count.incrementAndGet();

            });

        }
        Logs.SERVER_LOADING.info(">> Loaded {} script definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);

    }




    private boolean isScriptDatabaseBound(int id) {
        var entry = scriptNamesStorage.find(id);

        if (entry != null) {
            return entry.isScriptDatabaseBound;
        }

        return false;
    }

    private void loadCreatureTemplateResistances(HashMap<Integer, CreatureTemplate> templateHashMap) {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = creatureRepo.streamAllCreatureTemplateResistance()) {
            items.forEach(fields -> {
                if (fields[1] == 0 || fields[1] >= SpellSchool.values().length)
                {
                    Logs.SQL.error("creature_template_resistance has resistance definitions for creature {} but this school {} doesn't exist", fields[0], fields[1]);
                    return;
                }
                CreatureTemplate creatureTemplate = templateHashMap.get(fields[0]);
                if (creatureTemplate == null)
                {
                    Logs.SQL.error("creature_template_resistance has resistance definitions for creature {} but this creature doesn't exist", fields[0]);
                    return;
                }
                creatureTemplate.resistance[fields[1]] = fields[2];
                count.incrementAndGet();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature template resistances in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    private void loadCreatureTemplateSpells(HashMap<Integer, CreatureTemplate> templateHashMap) {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = creatureRepo.streamAllCreatureTemplateSpell()) {
            items.forEach(fields -> {
                if (fields[1] >= CreatureTemplate.MAX_CREATURE_SPELLS)
                {
                    Logs.SQL.error("creature_template_spell has spell definitions for creature {} with a incorrect index {}", fields[0], fields[1]);
                    return;
                }
                CreatureTemplate creatureTemplate = templateHashMap.get(fields[0]);
                if (creatureTemplate == null)
                {
                    Logs.SQL.error("creature_template_spell has spell definitions for creature {} but this creature doesn't exist", fields[0]);
                    return;
                }
                creatureTemplate.spells[fields[1]] = fields[2];
                count.incrementAndGet();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature template spells in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    private void loadCreatureTemplateModels(HashMap<Integer, CreatureTemplate> templateHashMap) {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try(var items = creatureRepo.streamAllCreatureTemplateModel()) {
            items.forEach(fields -> {
                var creatureId = (Integer)fields[0];
                var creatureDisplayId = (Integer)fields[1];
                var displayScale = (Float)fields[2];
                var probability = (Float) fields[3];
                CreatureTemplate creatureTemplate = templateHashMap.get(creatureId);
                if (creatureTemplate == null)
                {
                    Logs.SQL.error("Creature template (Entry: {}) does not exist but has a record in `creature_template_model`", creatureId);
                    return;
                }

                CreatureDisplayInfo  displayEntry = dbcObjectManager.creatureDisplayInfo(creatureDisplayId);
                if (displayEntry == null)
                {
                    Logs.SQL.error("Creature (Entry: {}) lists non-existing CreatureDisplayID id ({}), this can crash the client.", creatureId, creatureDisplayId);
                    return;
                }

                CreatureModelInfo modelInfo = getCreatureModelInfo(creatureDisplayId);
                if (modelInfo == null)
                    Logs.SQL.error("No model data exist for `CreatureDisplayID` = {} listed by creature (Entry: {}).", creatureDisplayId, creatureId);

                if (displayScale <= 0.0f)
                    displayScale = 1.0f;

                creatureTemplate.models.add(new CreatureModel(creatureDisplayId, displayScale, probability));
                creatureTemplate.modelInfos.add(modelInfo);

                count.incrementAndGet();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} creature template models in {} ms", count, System.currentTimeMillis() - oldMSTime);

    }

    private void loadCreatureSummonedData(HashMap<Integer, CreatureTemplate> templateHashMap) {
        var oldMSTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger();
        try (var items = creatureRepo.streamAllCreatureSummonedData()) {
            items.forEach(fields -> {

                CreatureTemplate creatureTemplate = templateHashMap.get(fields.creatureID);
                if (creatureTemplate == null) {
                    Logs.SQL.error("Table `creature_summoned_data` references non-existing creature {}, skipped", fields.creatureID);
                    return;
                }

                if (fields.creatureIdVisibleToSummoner != null) {
                    if (templateHashMap.get(fields.creatureIdVisibleToSummoner) == null) {
                        Logs.SQL.error("Table `creature_summoned_data` references non-existing creature {} in CreatureIDVisibleToSummoner for creature {}, set to 0",
                                fields.creatureIdVisibleToSummoner, fields.creatureID);
                        fields.creatureIdVisibleToSummoner = 0;
                    }
                }

                if (fields.groundMountDisplayId == null) {
                    if (!dbcObjectManager.creatureDisplayInfo().contains(fields.groundMountDisplayId)) {
                        Logs.SQL.error("Table `creature_summoned_data` references non-existing display id {} in GroundMountDisplayID for creature {}, set to 0",
                                fields.groundMountDisplayId, fields.creatureID);
                        fields.groundMountDisplayId = 0;
                    }
                }

                if (fields.flyingMountDisplayId == null) {
                    if (!dbcObjectManager.creatureDisplayInfo().contains(fields.flyingMountDisplayId)) {
                        Logs.SQL.error("Table `creature_summoned_data` references non-existing display id {} in FlyingMountDisplayID for creature {}, set to 0",
                                fields.flyingMountDisplayId, fields.creatureID);
                        fields.flyingMountDisplayId = 0;
                    }
                }

                creatureTemplate.summonedData = fields;
            });
        }

        Logs.SERVER_LOADING.info(">> Loaded {} creature summoned data definitions in {} ms", count, System.currentTimeMillis() - oldMSTime);
    }

    private void checkCreatureMovement(String table, long id, CreatureMovementData creatureMovement) {
        if (creatureMovement.ground == null) {
            Logs.SQL.error("`{}`.`Ground` wrong value ({}) for Id {}, setting to Run.", table, creatureMovement.ground, id);
            creatureMovement.ground = CreatureGroundMovementType.Run;
        }

        if (creatureMovement.flight == null) {
            Logs.SQL.error("`{}`.`Flight` wrong value ({}) for Id {}, setting to None.", table, creatureMovement.flight, id);
            creatureMovement.flight = CreatureFlightMovementType.None;
        }

        if (creatureMovement.chase == null) {
            Logs.SQL.error("`{}`.`Chase` wrong value ({}) for Id {}, setting to Run.", table, creatureMovement.chase, id);
            creatureMovement.chase = CreatureChaseMovementType.Run;
        }

        if (creatureMovement.random == null) {
            Logs.SQL.error("`{}`.`Random` wrong value ({}) for Id {}, setting to Walk.", table, creatureMovement.random, id);
            creatureMovement.random = CreatureRandomMovementType.Walk;
        }
    }

    private int loadReferenceVendor(int vendor, int item, Set<Integer> skipVendors, VendorItemData vList) {
        AtomicInteger count = new AtomicInteger();
        creatureRepo.queryNpcVendorByEntry(item).forEach(e->{
            if(e.item < 0) {
                count.addAndGet(loadReferenceVendor(vendor, -item, skipVendors, vList));
            } else {
                if (!isVendorItemValid( vendor, e, null, skipVendors)) {
                    return;
                }
                vList.addItem(e);
                count.getAndIncrement();
            }
        });
        return count.get();
    }

    private void addSpawnDataToGrid(SpawnData data) {
        var gridId = MapDefine.computeGridCoordinate(data.positionX, data.positionY).getId();
        var isPersonalPhase = PhasingHandler.isPersonalPhase(data.phaseId);

        if (!isPersonalPhase) {
            for (var difficulty : data.spawnDifficulties) {
                var key = Tuple.of(data.getMapId(), difficulty, gridId);
                GridSpawnData gridSpawnData = mapGridSpawnDataStorage.compute(key, Functions.ifAbsent(GridSpawnData::new));
                gridSpawnData.addSpawn(data);
            }
        } else {
            for (var difficulty : data.spawnDifficulties) {
                PersonalCellSpawnDataKey key = PersonalCellSpawnDataKey.of(data.mapId, difficulty, gridId, data.phaseId);
                GridSpawnData gridSpawnData = mapPersonalSpawnDataStorage.compute(key, Functions.ifAbsent(GridSpawnData::new));
                gridSpawnData.addSpawn(data);

            }
        }
    }

    private void removeSpawnDataFromGrid(SpawnData data) {
        var gridId = MapDefine.computeGridCoordinate(data.positionX, data.positionY).getId();
        var isPersonalPhase = PhasingHandler.isPersonalPhase(data.phaseId);

        if (!isPersonalPhase) {
            for (var difficulty : data.spawnDifficulties) {
                var key = Tuple.of(data.getMapId(), difficulty, gridId);
                GridSpawnData gridSpawnData = mapGridSpawnDataStorage.get(key);
                if (gridSpawnData == null) {
                    continue;
                }
                gridSpawnData.removeSpawn(data);
                mapGridSpawnDataStorage.put(key, gridSpawnData);
            }
        } else {
            for (var difficulty : data.spawnDifficulties) {
                PersonalCellSpawnDataKey key = PersonalCellSpawnDataKey.of(data.mapId, difficulty, gridId, data.phaseId);
                GridSpawnData gridSpawnData = mapPersonalSpawnDataStorage.get(key);
                if (gridSpawnData == null) {
                    continue;
                }
                gridSpawnData.removeSpawn(data);
                mapPersonalSpawnDataStorage.put(key, gridSpawnData);
            }
        }
    }


    private void checkGOLockId(GameObjectTemplate goInfo, int dataN, int N) {
        if (dbcObjectManager.lock().contains(dataN)) {
            return;
        }

        Logs.SQL.error("Gameobject (Entry: {} GoType: {}) have data{}={} but lock (Id: {}) not found.", goInfo.entry, goInfo.type, N, goInfo.door.open, goInfo.door.open);
    }

    private void checkGOLinkedTrapId(GameObjectTemplate goInfo, int dataN, int N) {
        var trapInfo = getGameObjectTemplate(dataN);

        if (trapInfo != null) {
            if (trapInfo.type != GameObjectType.TRAP) {
                Logs.SQL.error("Gameobject (Entry: {} GoType: {}) have data{}={} but GO (Entry {}) have not GAMEOBJECT_TYPE_TRAP ({}) type.", goInfo.entry, goInfo.type, N, dataN, dataN, GameObjectType.TRAP.ordinal());
            }
        }
    }

    private void checkGOSpellId(GameObjectTemplate goInfo, int dataN, int N) {
        if (world.getSpellManager().hasSpellInfo(dataN, Difficulty.NONE)) {
            return;
        }

        Logs.SQL.error("Gameobject (Entry: {} GoType: {}) have data{}={} but Spell (Entry {}) not exist.", goInfo.entry, goInfo.type, N, dataN, dataN);
    }

    private boolean checkAndFixGOChairHeightId(GameObjectTemplate goInfo, int dataN, int N) {
        int bound = UnitStandStateType.STATE_SIT_HIGH_CHAIR.ordinal() - UnitStandStateType.STATE_SIT_LOW_CHAIR.ordinal();
        if (dataN <= bound) {
            return true;
        }

        Logs.SQL.error("Gameobject (Entry: {} GoType: {}) have data{}={} but correct chair height in range 0..{}.", goInfo.entry, goInfo.type, N, dataN, dataN);

        // prevent client and server unexpected work
        return false;
    }

    private void checkGONoDamageImmuneId(GameObjectTemplate goTemplate, int dataN, int N) {
        // 0/1 correct values
        if (dataN <= 1) {
            return;
        }
        Logs.SQL.error("Gameobject (Entry: {} GoType: {}) have data{}={} but expected boolean (0/1) noDamageImmune field value.", goTemplate.entry, goTemplate.type, N, dataN);
    }

    private void checkGOConsumable(GameObjectTemplate goInfo, int dataN, int N) {
        // 0/1 correct values
        if (dataN <= 1) {
            return;
        }
        Logs.SQL.error("Gameobject (Entry: {} GoType: {}) have data{}={} but expected boolean (0/1) consumable field value.", goInfo.entry, goInfo.type, N, dataN);
    }

    private Set<Difficulty> parseSpawnDifficulties(String difficultyString, String table, long spawnId, int mapId, Set<Difficulty> mapDifficulties) {
        Set<Difficulty> difficulties = EnumSet.noneOf(Difficulty.class);


        var isTransportMap = isTransportMap(mapId);

        int[] ints = StringUtil.distinctSplitInts(difficultyString, ",");

        for (int difficultyId : ints) {
            Difficulty difficulty = difficultyId > Difficulty.values().length || difficultyId < 0 ? Difficulty.NONE : Difficulty.values()[difficultyId];

            if (difficulty != Difficulty.NONE && dbcObjectManager.difficulty(difficultyId) == null) {
                Logs.SQL.error("Table `{}` has {} (GUID: {}) with non invalid difficulty id {}, skipped.", table, table, spawnId, difficulty);
                continue;
            }

            if (!isTransportMap && mapDifficulties.contains(difficulty)) {
                Logs.SQL.error("Table `{}` has {} (GUID: {}) has unsupported difficulty {} for map (Id: {}).", table, table, spawnId, difficulty, mapId);
                continue;
            }
            difficulties.add(difficulty);
        }
        return difficulties;
    }

    private int fillMaxDurability(ItemClass itemClass, int itemSubClass, InventoryType inventoryType, ItemQuality quality, int itemLevel) {
        if (itemClass != ItemClass.ARMOR && itemClass != ItemClass.WEAPON) {
            return 0;
        }

        var levelPenalty = 1.0f;

        if (itemLevel <= 28) {
            levelPenalty = 0.966f - (28 - itemLevel) / 54.0f;
        }

        if (itemClass == ItemClass.ARMOR) {
            if (InventoryType.ROBE.compareTo(inventoryType) < 0) {
                return 0;
            }

            return 5 * (int) (Math.rint(25.0f * qualityMultipliers[quality.ordinal()] * armorMultipliers[inventoryType.ordinal()] * levelPenalty));
        }

        return 5 * (int) (Math.rint(18.0f * qualityMultipliers[quality.ordinal()] * weaponMultipliers[itemSubClass] * levelPenalty));
    }

    private void onDeleteSpawnData(SpawnData data) {
        var templateIt = spawnGroupDataStorage.get(data.getSpawnGroupData().groupId);

        if (templateIt.flags.hasFlag(SpawnGroupFlag.SYSTEM)) // system groups don't store their members in the map
        {
            return;
        }

        var spawnDatas = spawnGroupMapStorage.get(data.getSpawnGroupData().groupId);

        for (var it : spawnDatas) {
            if (it != data) {
                continue;
            }

            spawnGroupMapStorage.remove(data.getSpawnGroupData().groupId);

            return;
        }

        Assert.fail("Spawn data ({},{}) being removed is member of spawn group %u, but not actually listed in the lookup table for that group!",
                data.type, data.spawnId, data.spawnGroupData.groupId);


    }

    private void playerCreateInfoAddItemHelper(Race race, UnitClass class_, int itemId, int count) {
        if(race == Race.NONE || class_ == UnitClass.NONE) {
            return;
        }
        PlayerInfo info = playerInfo.get(Pair.of(race, class_));
        if (info == null) {
            return;
        }

        if (count > 0) {
            info.items.add(new PlayerCreateInfoItem(itemId, count));
        } else {
            if (count < -1) {
                Logs.SQL.error("Invalid count {} specified on item {} be removed from original player create info (use -1)!", count, itemId);
            }
            boolean removedF = info.itemsForFemale.removeIf(item -> item.itemId == itemId);
            boolean removedM = info.itemsForMale.removeIf(item -> item.itemId == itemId);
            if(!removedF && !removedM) {
                Logs.SQL.error("Item {} specified to be removed from original create info not found in db2!", itemId);
            }
        }
    }

    private PlayerLevelInfo buildPlayerLevelInfo(Race race, UnitClass _class, int level) {
        // base data (last known level)
        var info = playerInfo.get(Pair.of(race, _class)).levelInfo[world.getWorldSettings().maxPlayerLevel - 1];

        for (var lvl = world.getWorldSettings().maxPlayerLevel - 1; lvl < level; ++lvl) {
            switch (_class) {
                case WARRIOR:
                    info.stats[0] += (lvl > 23 ? 2 : (lvl > 1 ? 1 : 0));
                    info.stats[1] += (lvl > 23 ? 2 : (lvl > 1 ? 1 : 0));
                    info.stats[2] += (lvl > 36 ? 1 : (lvl > 6 && (lvl % 2) != 0 ? 1 : 0));
                    info.stats[3] += (lvl > 9 && (lvl % 2) == 0 ? 1 : 0);

                    break;
                case PALADIN:
                    info.stats[0] += (lvl > 3 ? 1 : 0);
                    info.stats[1] += (lvl > 33 ? 2 : (lvl > 1 ? 1 : 0));
                    info.stats[2] += (lvl > 38 ? 1 : (lvl > 7 && (lvl % 2) == 0 ? 1 : 0));
                    info.stats[3] += (lvl > 6 && (lvl % 2) != 0 ? 1 : 0);

                    break;
                case DEMON_HUNTER:
                    info.stats[0] += (lvl > 4 ? 1 : 0);
                    info.stats[1] += (lvl > 4 ? 1 : 0);
                    info.stats[2] += (lvl > 33 ? 2 : (lvl > 1 ? 1 : 0));
                    info.stats[3] += (lvl > 8 && (lvl % 2) != 0 ? 1 : 0);

                    break;
                case ROGUE:
                    info.stats[0] += (lvl > 5 ? 1 : 0);
                    info.stats[1] += (lvl > 4 ? 1 : 0);
                    info.stats[2] += (lvl > 16 ? 2 : (lvl > 1 ? 1 : 0));
                    info.stats[3] += (lvl > 8 && (lvl % 2) == 0 ? 1 : 0);

                    break;
                case PRIEST:
                    info.stats[0] += (lvl > 9 && (lvl % 2) == 0 ? 1 : 0);
                    info.stats[1] += (lvl > 5 ? 1 : 0);
                    info.stats[2] += (lvl > 38 ? 1 : (lvl > 8 && (lvl % 2) != 0 ? 1 : 0));
                    info.stats[3] += (lvl > 22 ? 2 : (lvl > 1 ? 1 : 0));

                    break;
                case SHAMAN:
                    info.stats[0] += (lvl > 34 ? 1 : (lvl > 6 && (lvl % 2) != 0 ? 1 : 0));
                    info.stats[1] += (lvl > 4 ? 1 : 0);
                    info.stats[2] += (lvl > 7 && (lvl % 2) == 0 ? 1 : 0);
                    info.stats[3] += (lvl > 5 ? 1 : 0);

                    break;
                case MAGE:
                    info.stats[0] += (lvl > 9 && (lvl % 2) == 0 ? 1 : 0);
                    info.stats[1] += (lvl > 5 ? 1 : 0);
                    info.stats[2] += (lvl > 9 && (lvl % 2) == 0 ? 1 : 0);
                    info.stats[3] += (lvl > 24 ? 2 : (lvl > 1 ? 1 : 0));

                    break;
                case WARLOCK:
                    info.stats[0] += (lvl > 9 && (lvl % 2) == 0 ? 1 : 0);
                    info.stats[1] += (lvl > 38 ? 2 : (lvl > 3 ? 1 : 0));
                    info.stats[2] += (lvl > 9 && (lvl % 2) == 0 ? 1 : 0);
                    info.stats[3] += (lvl > 33 ? 2 : (lvl > 2 ? 1 : 0));

                    break;
                case DRUID:
                    info.stats[0] += (lvl > 38 ? 2 : (lvl > 6 && (lvl % 2) != 0 ? 1 : 0));
                    info.stats[1] += (lvl > 32 ? 2 : (lvl > 4 ? 1 : 0));
                    info.stats[2] += (lvl > 38 ? 2 : (lvl > 8 && (lvl % 2) != 0 ? 1 : 0));
                    info.stats[3] += (lvl > 38 ? 3 : (lvl > 4 ? 1 : 0));

                    break;
            }
        }

        return info;
    }


    private QuestRelationResult getQuestRelationsFrom(Map<Integer, List<Integer>> map, int key, boolean onlyActive) {
        return new QuestRelationResult(map.get(key), onlyActive);
    }

    private void loadTerrainWorldMaps() {
        var oldMSTime = System.currentTimeMillis();

        try (var items = miscRepo.streamAllTerrainWorldMap()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.map().contains(fields[0])) {
                    Logs.SQL.error("Map {} defined in `terrain_swap_defaults` does not exist, skipped.", fields[0]);
                    return;
                }
                //TODO figure out this data structs
                if (!dbcObjectManager.isUiMapPhase(fields[1])) {
                    Logs.SQL.error("WorldMapArea {} defined in `terrain_worldmap` does not exist, skipped.", fields[1]);
                    return;
                }
                var terrainSwapInfo = terrainSwapInfoById.compute(fields[1], Functions.ifAbsent(()-> new TerrainSwapInfo(fields[1])));
                terrainSwapInfoByMap.compute(fields[0], Functions.addToList(terrainSwapInfo));
                terrainSwapInfo.uiMapPhaseIDs.add(fields[1]);
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} terrain world maps in {} ms.", terrainSwapInfoById.size(), System.currentTimeMillis() - oldMSTime);
    }

    private void loadTerrainSwapDefaults() {
        var oldMSTime = System.currentTimeMillis();
        try (var items = miscRepo.streamAllTerrainSwapDefaults()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.map().contains(fields[0])) {
                    Logs.SQL.error("Map {} defined in `terrain_swap_defaults` does not exist, skipped.", fields[0]);
                    return;
                }

                if (!dbcObjectManager.map().contains(fields[1])) {
                    Logs.SQL.error("TerrainSwapMap {} defined in `terrain_swap_defaults` does not exist, skipped.", fields[1]);
                    return;
                }
                var terrainSwapInfo = terrainSwapInfoById.compute(fields[1], Functions.ifAbsent(()-> new TerrainSwapInfo(fields[1])));
                terrainSwapInfoByMap.compute(fields[0], Functions.addToList(terrainSwapInfo));
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} terrain swap defaults in {} ms.", terrainSwapInfoById.size(), System.currentTimeMillis() - oldMSTime);
    }

    private void loadAreaPhases() {
        var oldMSTime = System.currentTimeMillis();

        AtomicInteger count = new AtomicInteger();
        try (var items = miscRepo.streamAllPhaseArea()) {
            items.forEach(fields -> {
                if (!dbcObjectManager.areaTrigger().contains(fields[0])) {
                    Logs.SQL.error("Area {} defined in `phase_area` does not exist, skipped.", fields[0]);
                    return;
                }

                if (!dbcObjectManager.phase().contains(fields[1])) {
                    Logs.SQL.error("Phase {} defined in `phase_area` does not exist, skipped.", fields[1]);
                    return;
                }
                PhaseInfoStruct phaseInfoStruct = phaseInfoById.compute(fields[1], Functions.ifAbsent(() -> new PhaseInfoStruct(fields[1])));
                phaseInfoStruct.areas.add(fields[0]);
                phaseInfoByArea.compute(fields[0], Functions.addToList(new PhaseAreaInfo(phaseInfoStruct)));
                count.getAndIncrement();
            });
        }
        phaseInfoByArea.forEach((k, v) -> {
            int parentAreaId = k;

            for (PhaseAreaInfo phase : v) {
                do {
                    var area = dbcObjectManager.areaTable(parentAreaId);
                    if (area == null) {
                        break;
                    }
                    parentAreaId = area.getParentAreaID();
                    if (parentAreaId == 0) {
                        break;
                    }

                    var parentAreaPhases = phaseInfoByArea.get(parentAreaId);

                    for (var parentAreaPhase : parentAreaPhases) {
                        if (parentAreaPhase.phaseInfo.id == phase.phaseInfo.id) {
                            parentAreaPhase.subAreaExclusions.add(k);
                        }
                    }
                } while (true);
            }
        });

        Logs.SERVER_LOADING.info(">> Loaded {} phase areas in {} ms.", count, System.currentTimeMillis() - oldMSTime);
    }




    private float getDamageMod(CreatureClassification rank) {
        WorldSetting worldSettings = world.getWorldSettings();
        // define rates for each elite rank
        return switch (rank) {
            case Normal -> worldSettings.rate.creatureDamageNormal;
            case Elite -> worldSettings.rate.creatureDamageElite;
            case RareElite -> worldSettings.rate.creatureDamageRareElite;
            case Obsolete -> worldSettings.rate.creatureDamageObsolete;
            case Trivial -> worldSettings.rate.creatureDamageTrivial;
            case MinusMob -> worldSettings.rate.creatureDamageMinusMob;
            case Rare -> worldSettings.rate.creatureDamageRare;
        };
    }
}
