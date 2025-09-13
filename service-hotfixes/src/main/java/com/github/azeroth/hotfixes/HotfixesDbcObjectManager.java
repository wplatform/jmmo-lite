package com.github.azeroth.hotfixes;


import com.github.azeroth.cache.*;
import com.github.azeroth.common.Locale;
import com.github.azeroth.common.*;
import com.github.azeroth.config.IllegalConfigException;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.DbcObjects;
import com.github.azeroth.dbc.db2.DB2FileException;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2EntityReader;
import com.github.azeroth.dbc.defines.*;

import com.github.azeroth.dbc.domain.*;
import com.github.azeroth.dbc.domain.ChrSpecialization;
import com.github.azeroth.dbc.domain.CreatureFamilyEntry;
import com.github.azeroth.dbc.model.*;
import com.github.azeroth.defines.*;
import com.github.azeroth.utils.MathUtil;
import com.github.azeroth.utils.StringUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static com.github.azeroth.common.Logs.SERVER_LOADING;
import static com.github.azeroth.dbc.defines.DbcDefine.WORLD_MAP_TRANSFORMS_FLAG_DUNGEON;
import static com.github.azeroth.defines.SharedDefine.*;

public class HotfixesDbcObjectManager implements DbcObjectManager {

    @Value("${worldserver.dbc.locale}")
    private Locale dbcLocale;

    @Value("${worldserver.datadir}")
    private String dataFolder;

    private final CacheProvider cacheProvider;

    private final EnumMap<DbcObjects, DbcEntityStore<? extends DbcEntity>> entityStoreMap = new EnumMap<>(DbcObjects.class);


    private Map<Long, Integer> hotfixData;
    private MapCache<Short, List<Short>> areaGroupMembers;
    private MapCache<Short, List<ArtifactPower>> artifactPowers;
    private MapCache<Short, Set<Short>> artifactPowerLinks;
    private MapCache<Pair<Short, Byte>, ArtifactPowerRank> artifactPowerRanks;
    private Set<Tuple<Byte, Byte, Byte>> characterFacialHairStyles;
    private MapCache<Tuple<Byte, Byte, CharBaseSectionVariation>, Set<CharSection>> charSections;
    private MapCache<Integer, CharStartOutfit> charStartOutfits;
    private final int[][] powersByClass = new int[PlayerClass.values().length][Power.MAX_POWERS.index];
    private final ChrSpecialization[][] chrSpecializationsByIndex = new ChrSpecialization[PlayerClass.values().length + 1][MAX_SPECIALIZATIONS];
    private MapCache<Integer, List<DBCPosition2D>> curvePoints;
    private MapCache<EmotesTextSoundKey, EmotesTextSound> emoteTextSounds;
    private Map<Integer, List<Integer>> factionTeams;
    private MapCache<Integer, Set<FriendshipRepReaction>> friendshipRepReactions;
    private MapCache<Integer, Heirloom> heirlooms;
    private Map<Integer,List<Integer>> glyphBindableSpells;
    private Map<Integer, List<ChrSpecialization>> glyphRequiredSpecs;
    private Map<Short, List<ItemBonus>> itemBonusLists;
    private Map<Short, Integer> itemLevelDeltaToBonusListContainer;
    private Map<Integer, Set<ItemBonusTreeNode>> itemBonusTrees;
    private Map<Integer, ItemChildEquipment> itemChildEquipment;
    private ItemClassEntry[] itemClassEntryByOldEnum = new ItemClassEntry[PlayerClass.values().length];
    private Set<Integer> itemsWithCurrencyCost;
    private Map<Integer, Set<ItemLimitCategoryCondition>> itemCategoryConditions;
    private Map<Short, Set<ItemLevelSelectorQuality>> itemLevelQualitySelectorQualities;
    private Map<Integer/*itemId | appearanceMod << 24*/, ItemModifiedAppearance> itemModifiedAppearancesByItem;
    private Map<Integer, Short> itemToBonusTree;
    private Map<Short, List<JournalEncounter>> journalEncountersByJournalInstance;
    private Map<Short, List<JournalEncounterItem>> itemsByJournalEncounter;
    private Map<Integer, JournalInstance> journalInstanceByMap;
    private Map<Integer, List<ItemSetSpell>> itemSetSpells;
    private Map<Integer, List<ItemSpecOverride>> itemSpecOverrides;

    private List<JournalTier> journalTiersByIndex;
    private MapCache<Integer, Map<Difficulty, MapDifficulty>> mapDifficulties;
    private MapCache<Integer, Map<Integer, PlayerCondition>> mapDifficultyConditions;
    private Map<Integer, Mount> mountsBySpellId;
    private Map<Short, Set<MountTypeXCapability>> mountCapabilitiesByType;
    private Map<Integer, List<MountXDisplay>> mountDisplays;
    private Map<Byte, List<List<NameGen>>> nameGenData;
    private Map<Locale, List<Pattern>> nameValidators;
    private Map<Integer, ParagonReputation> paragonReputations;

    private Map<Integer/*pathID*/, PathDb2> paths;
    private Map<Short, List<Integer>> phasesByGroup;
    private PowerType[] powerTypes;
    private Map<Integer, Byte> pvpItemBonus;

    private Map<Integer, List<PvpTalent>> pvpTalentsByPosition;
    private Map<Pair<Integer, Integer>, Integer> pvpRewardPack;
    private int[][] pvpTalentUnlock;
    private Map<Integer, List<QuestLineXQuest>> questsByQuestLine;

    private MapCache<Short, QuestPackage> questPackages;
    private Map<Integer, List<RewardPackXCurrencyType>> rewardPackCurrencyTypes;
    private Map<Integer, List<RewardPackXItem>> rewardPackItems;
    private Map<Integer, List<SkillLine>> skillLinesByParentSkillLine;
    private Map<Short, List<SkillLineAbility>> skillLineAbilitiesBySkillUpSkill;
    private Map<Integer, Short> ruleSetItemUpgrade;
    private Map<Short, List<SkillRaceClassInfo>> skillRaceClassInfoBySkill;
    private Map<Short, List<SpecializationSpell>> specializationSpellsBySpec;
    private Set<Byte> spellFamilyNames;

    private Map<Integer, List<SpellPower>> spellPowers;
    private Map<Integer, List<SpellPower>> spellPowerDifficulties;
    private Map<Short, List<SpellProcsPerMinuteMod>> spellProcsPerMinuteMods;
    private Map<Short, List<SpellVisualMissile>> spellVisualMissilesBySet;
    private Map<Integer, List<Talent>> talentsByPosition;

    @Getter
    private Map<Integer, List<TaxiPathNode>> taxiPathNodesByPath;
    private Map<Pair<Short, Short>, TaxiPath> taxiPaths;
    private Set<Integer> toys;

    private Map<Integer, List<TransmogSet>> transmogSetsByItemModifiedAppearance;
    private Map<Integer, List<TransmogSetItem>> transmogSetItemsByTransmogSet;
    private Map<Tuple<Short, Byte, Integer>, WmoAreaTable> wmoAreaTableLookup;
    private Map<Short, WorldMapArea> worldMapAreaByAreaID;
    private Map<Integer, WorldSafeLoc> worldSafeLocContainer;

    @Getter(onMethod_ = @Override)
    private TaxiMask taxiNodesMask;
    private TaxiMask oldContinentsNodesMask;
    private TaxiMask hordeTaxiNodesMask;
    private TaxiMask allianceTaxiNodesMask;

    public HotfixesDbcObjectManager(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }


    public void loadDbcObjects() {
        long oldMSTime = System.currentTimeMillis();
        loadEntityStores(DbcObjects.values());
        loadDbcRelationshipData();
        Logs.SERVER_LOADING.info(">> Initialized {} DB2 data stores in {} ms", DbcObjects.values().length, System.currentTimeMillis() - (oldMSTime));
    }



    void loadEntityStores(DbcObjects[] dbcObjects) {

        SERVER_LOADING.info("Using {} DBC Locale", dbcLocale.name());

        EnumSet<Locale> availableDb2Locales = EnumSet.noneOf(Locale.class);

        try (var listStream = Files.list(Paths.get(dataFolder, "dbc"))) {
            listStream.filter(Files::isDirectory)
                    .map(path -> Locale.valueOf(path.getFileName().toString()))
                    .forEach(availableDb2Locales::add);
        } catch (IllegalArgumentException e) {
            throw new IllegalConfigException(StringUtil.format("Incorrect DBC.Locale! Must ", Locale.none));
        } catch (IOException e) {
            SERVER_LOADING.error("Unable to load db2 files for {} locale specified in DBC.Locale config!", Paths.get(dataFolder).toAbsolutePath(), e);
            return;
        }

        if (!availableDb2Locales.contains(dbcLocale)) {
            SERVER_LOADING.error("Unable to load db2 files for {} locale specified in DBC.Locale config!", dbcLocale.name());
            return;
        }

        Arrays.stream(dbcObjects).forEach(dbc -> {
            DbcEntityStore<?> dbcEntityStore = loadDb2File(dbc.clazz(), availableDb2Locales);
            entityStoreMap.put(dbc, dbcEntityStore);
        });


        // Check loaded DB2 files proper version
        if (areaTable(9531) == null ||                   // last area added in 7.3.5 (25996)
                charTitle(522) == null ||                // last char title added in 7.3.5 (25996)
                gemProperty(3632) == null ||             // last gem property added in 7.3.5 (25996)
                item(157831) == null ||                  // last item added in 7.3.5 (25996)
                itemExtendedCost(6300) == null ||        // last item extended cost added in 7.3.5 (25996)
                map(1903) == null ||                     // last map added in 7.3.5 (25996)
                spell(263166) == null)                   // last spell added in 7.3.5 (25996)
        {
            throw new DB2FileException("You have _outdated_ DB2 files. Please extract correct versions from current using client.");
        }
    }


    private void loadDbcRelationshipData() {

        areaGroupMembers = cacheProvider.newGenericMapCache("areaGroupMemberCache", new TypeReference<>(){});
        for (var areaGroupMember : this.areaGroupMember()) {
            areaGroupMembers.compute(areaGroupMember.getAreaGroupID(), Functions.addToList(areaGroupMember.getAreaID()));
        }

        artifactPowers = cacheProvider.newGenericMapCache("artifactPowerCache", new TypeReference<>(){});
        for (var artifactPower : this.artifactPower()) {
            artifactPowers.compute(artifactPower.getArtifactID(), Functions.addToList(artifactPower));
        }

        artifactPowerLinks = cacheProvider.newGenericMapCache("artifactPowerLinkCache", new TypeReference<>(){});
        for (var artifactPowerLink : this.artifactPowerLink()) {
            artifactPowerLinks.compute(artifactPowerLink.getPowerA(), Functions.addToSet(artifactPowerLink.getPowerB()));
            artifactPowerLinks.compute(artifactPowerLink.getPowerB(), Functions.addToSet(artifactPowerLink.getPowerA()));
        }

        artifactPowerRanks = cacheProvider.newGenericMapCache("artifactPowerRankCache", new TypeReference<>(){});
        for (var artifactPowerRank : this.artifactPowerRank()) {
            artifactPowerRanks.put(Pair.of(artifactPowerRank.getArtifactPowerID(), artifactPowerRank.getRankIndex()), artifactPowerRank);
        }

        Assert.state(BATTLE_PET_SPECIES_MAX_ID >= this.battlePetSpecie().size(),
                "BATTLE_PET_SPECIES_MAX_ID {} must be equal to or greater than {}",
                BATTLE_PET_SPECIES_MAX_ID, this.battlePetSpecie().size());

        characterFacialHairStyles = new TreeSet<>();
        for (var charHairStyles : this.characterFacialHairStyle()) {
            characterFacialHairStyles.add(Tuple.of(charHairStyles.getRaceID(), charHairStyles.getSexID(), charHairStyles.getVariationID()));
        }

        CharSectionType[] charSectionTypes = CharSectionType.values();
        CharBaseSectionVariation[] variations = CharBaseSectionVariation.values();
        for (var charBaseSection : this.charBaseSection()) {
            Assert.state(charBaseSection.getResolutionVariationEnum() < charSectionTypes.length,
                    "CharSectionType length {} must be equal to or greater than {}",
                    charSectionTypes.length, charBaseSection.getResolutionVariationEnum());

            Assert.state(charBaseSection.getVariationEnum() < variations.length,
                    "CharBaseSectionVariation.length {} must be equal to or greater than {}",
                    variations.length, charBaseSection.getVariationEnum());

        }

        charSections = cacheProvider.newGenericMapCache("charSectionCache", new TypeReference<>(){});
        Map<Tuple<Byte, Byte, CharBaseSectionVariation>, Set<Pair<Byte, Byte>>> testDouble = new HashMap<>();
        for (var charSection : this.charSection()) {

            Assert.state(charSection.getBaseSection() < charSectionTypes.length,
                    "SECTION_TYPE_MAX {} must be equal to or greater than {}",
                    charSectionTypes.length, charSection.getBaseSection());

            Tuple<Byte, Byte, CharBaseSectionVariation> sectionKey = Tuple.of(charSection.getRaceID(), charSection.getSexID(), variations[charSection.getBaseSection()]);
            Pair<Byte, Byte> keyPair = Pair.of(charSection.getVariationIndex(), charSection.getColorIndex());
            testDouble.compute(sectionKey, Functions.addToSetIfAbsentThen(keyPair, () -> charSections.compute(sectionKey, Functions.addToSet(charSection))));

        }
        charStartOutfits = cacheProvider.newGenericMapCache("CharStartOutfitsCache", new TypeReference<>(){});
        for (var outfit : this.charStartOutfit()) {
            charStartOutfits.put(outfit.getRaceID() | (outfit.getClassID() << 8) | (outfit.getSexID() << 16), outfit);
        }

        for (int[] byClass : powersByClass)
            Arrays.fill(byClass, Power.MAX_POWERS.index);

        for (ChrClassesXPowerType power : chrClassesXPowerType()) {
            int index = 0;
            for (int j = 0; j < Power.MAX_POWERS.index; ++j)
                if (powersByClass[power.getClassID()][j] != Power.MAX_POWERS.index)
                    ++index;

            Assert.state(power.getClassID() < PlayerClass.values().length);
            Assert.state(power.getPowerType() < Power.MAX_POWERS.index);
            powersByClass[power.getClassID()][power.getPowerType()] = index;
        }


        for (ChrSpecialization chrSpec : chrSpecialization())
        {
            Assert.state(chrSpec.getClassID() < PlayerClass.values().length);
            Assert.state(chrSpec.getOrderIndex() < MAX_SPECIALIZATIONS);

            int storageIndex = chrSpec.getClassID();
            if (chrSpec.flags().hasFlag(ChrSpecializationFlag.PetOverrideSpec))
            {
                Assert.state(chrSpec.getClassID() != 0);
                storageIndex = PET_SPEC_OVERRIDE_CLASS_INDEX;
            }
            chrSpecializationsByIndex[storageIndex][chrSpec.getOrderIndex()] = chrSpec;
        }

        curvePoints = cacheProvider.newGenericMapCache("CurvePointsCache", new TypeReference<>(){});
        HashMap<Short /*curveID*/, List<CurvePoint>> unsortedPoints = new HashMap<>();
        for (CurvePoint curvePoint : curvePoint()) {
            if (curve(curvePoint.getCurveID()) != null) {
                unsortedPoints.compute(curvePoint.getCurveID(), Functions.addToList(curvePoint));
            }
        }
        unsortedPoints.forEach((k,v) -> {
            List<DBCPosition2D> list = v.stream().sorted().map(e -> new DBCPosition2D(e.getPosX(), e.getPosY())).toList();
            curvePoints.put(k.intValue(), list);
        });

        emoteTextSounds = cacheProvider.newGenericMapCache("EmoteTextSoundsCache", new TypeReference<>(){});
        for (EmotesTextSound item : emotesTextSound()) {
            EmotesTextSoundKey soundKey = EmotesTextSoundKey.of(item.getEmotesTextID(), item.getRaceID(), item.getSexID(), item.getClassID());
            emoteTextSounds.put(soundKey, item);
        }

        factionTeams = new HashMap<>();
        for (Faction  faction : faction()) {
            if (faction.getParentFactionID() > 0) {
                factionTeams.compute(faction.getParentFactionID().intValue(), Functions.addToList(faction.getId()));
            }
        }


        friendshipRepReactions = cacheProvider.newGenericMapCache("FriendshipRepReactionsCache", new TypeReference<>(){});
        for (FriendshipRepReaction friendshipRepReaction : friendshipRepReaction())
            friendshipRepReactions.compute(friendshipRepReaction.getFriendshipRepID().intValue(), Functions.addToSet(friendshipRepReaction));
        for (GameObjectDisplayInfo e : gameObjectDisplayInfo())
        {
            boolean updated = false;
            if (e.getGeoBoxMaxX() < e.getGeoBoxMinX()) {
                Float tmp = e.getGeoBoxMaxX();
                e.setGeoBoxMaxX(e.getGeoBoxMinX());
                e.setGeoBoxMinX(tmp);
                updated = true;
            }
            if (e.getGeoBoxMaxY() < e.getGeoBoxMinY()) {
                Float tmp = e.getGeoBoxMaxY();
                e.setGeoBoxMaxY(e.getGeoBoxMinY());
                e.setGeoBoxMinY(tmp);
                updated = true;
            }
            if (e.getGeoBoxMaxZ() < e.getGeoBoxMinZ()) {
                Float tmp = e.getGeoBoxMaxZ();
                e.setGeoBoxMaxZ(e.getGeoBoxMinZ());
                e.setGeoBoxMinZ(tmp);
                updated = true;
            }
            if(updated) {
                gameObjectDisplayInfo().append(e);
            }
        }

        heirlooms = cacheProvider.newGenericMapCache("HeirloomsCache", new TypeReference<>(){});
        for (Heirloom heirloom : heirloom())
            heirlooms.put(heirloom.getItemID(), heirloom);

        glyphBindableSpells = new HashMap<>();
        for (GlyphBindableSpell  e : glyphBindableSpell())
            glyphBindableSpells.compute(e.getGlyphPropertiesID().intValue(), Functions.addToList(e.getSpellID()));

        glyphRequiredSpecs = new HashMap<>();
        for (GlyphRequiredSpec e : glyphRequiredSpec())
            glyphRequiredSpecs.compute(e.getGlyphPropertiesID().intValue(), Functions.addToList(chrSpecialization(e.getChrSpecializationID())));

        itemBonusLists = cacheProvider.newGenericMapCache("ItemBonusListsCache", new TypeReference<>(){});
        for (ItemBonus bonus : itemBonus()) {
            itemBonusLists.compute(bonus.getParentItemBonusListID(),Functions.addToList(bonus));
        }

        itemLevelDeltaToBonusListContainer = cacheProvider.newGenericMapCache("ItemLevelDeltaToBonusListContainerCache", new TypeReference<>(){});
        for (ItemBonusListLevelDelta itemBonusListLevelDelta : itemBonusListLevelDelta()) {
            itemLevelDeltaToBonusListContainer.put(itemBonusListLevelDelta.getItemLevelDelta(), itemBonusListLevelDelta.getId());
        }

        itemBonusTrees = cacheProvider.newGenericMapCache("ItemBonusTreesCache", new TypeReference<>(){});
        for (ItemBonusTreeNode bonusTreeNode : itemBonusTreeNode()) {
            int bonusTreeId = bonusTreeNode.getParentItemBonusTreeID();
            while (bonusTreeNode != null) {
                itemBonusTrees.compute(bonusTreeId, Functions.addToSet(bonusTreeNode));
                bonusTreeNode = itemBonusTreeNode(bonusTreeNode.getChildItemBonusTreeID());
            }
        }

        itemChildEquipment = new HashMap<>();
        for (ItemChildEquipment e : itemChildEquipment()) {
            Assert.state(itemChildEquipment.get(e.getParentItemID()) == null, "Item must have max 1 child item.");
            itemChildEquipment.put(e.getParentItemID(), e);
        }

        for (ItemClassEntry itemClassEntry : itemClass()) {
            Assert.state(itemClassEntry.getClassID() < itemClassEntryByOldEnum.length);
            Assert.state(itemClassEntryByOldEnum[itemClassEntry.getClassID()] == null);
            itemClassEntryByOldEnum[itemClassEntry.getClassID()] = itemClassEntry;
        }

        itemsWithCurrencyCost = new HashSet<>();
        for (ItemCurrencyCost itemCurrencyCost : itemCurrencyCost()) {
            itemsWithCurrencyCost.add(itemCurrencyCost.getItemID());
        }

        itemCategoryConditions = new HashMap<>();
        for (ItemLimitCategoryCondition condition : itemLimitCategoryCondition()) {
            itemCategoryConditions.compute(condition.getParentItemLimitCategoryID(), Functions.addToSet(condition, ()->new HashSet<>(4)));
        }

        itemLevelQualitySelectorQualities = new HashMap<>(2);
        for (ItemLevelSelectorQuality itemLevelSelectorQuality : itemLevelSelectorQuality()) {
            itemLevelQualitySelectorQualities.compute(itemLevelSelectorQuality.getParentILSQualitySetID(),Functions.addToSet(itemLevelSelectorQuality));
        }

        itemModifiedAppearancesByItem = cacheProvider.newGenericMapCache("ItemModifiedAppearancesByItemCache", new TypeReference<>(){});
        for (ItemModifiedAppearance appearanceMod : itemModifiedAppearance()) {
            Assert.state(appearanceMod.getItemAppearanceID() <= 0xFFFFFF);
            itemModifiedAppearancesByItem.put(appearanceMod.getItemID() | (appearanceMod.getItemAppearanceModifierID() << 24), appearanceMod);
        }

        itemSetSpells = cacheProvider.newGenericMapCache("ItemSetSpellsCache", new TypeReference<>(){});
        for (ItemSetSpell  itemSetSpell : itemSetSpell()) {
            itemSetSpells.compute(itemSetSpell.getItemSetID().intValue(), Functions.addToList(itemSetSpell));
        }

        itemSpecOverrides = cacheProvider.newGenericMapCache("ItemSpecOverridesCache", new TypeReference<>(){});
        for (ItemSpecOverride e : itemSpecOverride()) {
            itemSpecOverrides.compute(e.getItemID(), Functions.addToList(e));
        }

        itemToBonusTree = cacheProvider.newGenericMapCache("ItemToBonusTreeCache", new TypeReference<>(){});
        for (ItemXBonusTree itemBonusTreeAssignment : itemXBonusTree()) {
            itemToBonusTree.put(itemBonusTreeAssignment.getItemID(), itemBonusTreeAssignment.getItemBonusTreeID());
        }
        journalEncountersByJournalInstance = cacheProvider.newGenericMapCache("JournalEncountersByJournalInstanceCache", new TypeReference<>(){});
        for (JournalEncounter journalEncounter : journalEncounter()) {
            journalEncountersByJournalInstance.compute(journalEncounter.getJournalInstanceID(), Functions.addToList(journalEncounter));
        }

        itemsByJournalEncounter = cacheProvider.newGenericMapCache("ItemsByJournalEncounterCache", new TypeReference<>(){});
        for (JournalEncounterItem journalEncounterItem : journalEncounterItem()) {
            itemsByJournalEncounter.compute(journalEncounterItem.getJournalEncounterID(), Functions.addToList(journalEncounterItem));
        }

        journalInstanceByMap = cacheProvider.newGenericMapCache("JournalInstanceByMapCache", new TypeReference<>(){});
        for (JournalInstance journalInstance : journalInstance()) {
            JournalInstance instance = journalInstanceByMap.get(journalInstance.getMapID().intValue());
            if (instance == null || journalInstance.getOrderIndex() > instance.getOrderIndex()) {
                journalInstanceByMap.put(journalInstance.getMapID().intValue(), journalInstance);
            }
        }


        journalTiersByIndex = new ArrayList<>(8);
        for (JournalTier  journalTier : journalTier()) {
            journalTiersByIndex.add(journalTier);
        }

        mapDifficulties = cacheProvider.newGenericMapCache("MapDifficultiesCache", new TypeReference<>(){});
        for (MapDifficulty  entry : mapDifficulty()) {
            mapDifficulties.compute(entry.getMapID().intValue(), Functions.addToMap(Difficulty.values()[entry.getDifficultyID()], entry));
        }

        List<MapDifficultyXCondition> allMapDifficultyConditions = new ArrayList<>(mapDifficultyXCondition().size());
        for (MapDifficultyXCondition  mapDifficultyCondition : mapDifficultyXCondition()) {
            allMapDifficultyConditions.add(mapDifficultyCondition);
        }
        Collections.sort(allMapDifficultyConditions);
        mapDifficultyConditions = cacheProvider.newGenericMapCache("MapDifficultyConditionsCache", new TypeReference<>(){});
        for (MapDifficultyXCondition  mapDifficultyCondition : allMapDifficultyConditions) {
            PlayerCondition playerCondition = playerCondition(mapDifficultyCondition.getPlayerConditionID());
            if (playerCondition != null) {
                mapDifficultyConditions.compute(mapDifficultyCondition.getMapDifficultyId(), Functions.addToMap(mapDifficultyCondition.getId(), playerCondition));
            }
        }

        mountsBySpellId = cacheProvider.newGenericMapCache("MountsBySpellIdCache", new TypeReference<>(){});
        for (Mount  mount : mount()) {
            mountsBySpellId.put(mount.getSourceSpellID(), mount);
        }

        mountCapabilitiesByType = new HashMap<>();
        for (MountTypeXCapability  mountTypeCapability : mountTypeXCapability()) {
            mountCapabilitiesByType.compute(mountTypeCapability.getMountTypeID(), Functions.addToSet(mountTypeCapability));
        }

        mountDisplays = new HashMap<>();
        for (MountXDisplay  mountDisplay : mountXDisplay()) {
            mountDisplays.compute(mountDisplay.getMountID(), Functions.addToList(mountDisplay));
        }

        nameGenData = cacheProvider.newGenericMapCache("NameGenDataCache", new TypeReference<>(){});
        for (NameGen  nameGen : nameGen()) {
            List<List<NameGen>> lists = nameGenData.get(nameGen.getRaceID());
            if(lists == null) {
                lists = List.of(new ArrayList<>(), new ArrayList<>());
            }
            lists.get(nameGen.getSex()).add(nameGen);
            nameGenData.put(nameGen.getRaceID(),lists);
        }

        nameValidators = cacheProvider.newGenericMapCache("NameValidatorsCache", new TypeReference<>() {});
        for (NamesProfanity namesProfanity : namesProfanity()) {
            Assert.state(namesProfanity.getLanguage() < Locale.values().length);

            if (namesProfanity.getLanguage() != -1)
                nameValidators.compute(Locale.values()[namesProfanity.getLanguage()], Functions.addToList(Pattern.compile(namesProfanity.getName())));
            else {
                for (Locale value : Locale.values()) {
                    if (value == Locale.none) {
                        continue;
                    }
                    nameValidators.compute(value, Functions.addToList(Pattern.compile(namesProfanity.getName())));
                }
            }
        }

        for (NamesReserved namesReserved : namesReserved()) {
            nameValidators.compute(Locale.none, Functions.addToList(Pattern.compile(namesReserved.getName())));
        }

        for (NamesReservedLocale namesReserved : namesReservedLocale()) {
            Assert.state((namesReserved.getLocaleMask() & -(1 << Locale.values().length)) == 0);

            for (Locale value : Locale.values()) {
                if (value == Locale.none) {
                    continue;
                }
                if ((namesReserved.getLocaleMask() & (1 << value.ordinal())) != 0)
                    nameValidators.compute(value, Functions.addToList(Pattern.compile(namesReserved.getName())));
            }
        }
        paragonReputations = new HashMap<>();
        for (ParagonReputation paragonReputation : paragonReputation()) {
            if (faction().contains(paragonReputation.getFactionID())) {
                paragonReputations.put(paragonReputation.getFactionID(), paragonReputation);
            }
        }

        paths =  cacheProvider.newGenericMapCache("PathsCache", new TypeReference<>() {});
        HashMap<Integer, PathDb2> tmp = new HashMap<>();
        HashMap<Integer /*pathID*/, List<PathNode>> unsortedNodes = new HashMap<>();
        for (PathNode pathNode : pathNode()){
            if (path().contains(pathNode.getPathID()) && location().contains(pathNode.getLocationID())) {
                unsortedNodes.compute(pathNode.getPathID(), Functions.addToList(pathNode));
            }
        }

        unsortedNodes.forEach((k,v) -> {
            PathDb2 pathDb2 = tmp.compute(k, Functions.ifAbsent(()-> new PathDb2(k, new ArrayList<>(), new ArrayList<>())));
            v.stream().sorted()
                    .map(e->location(e.getLocationID()))
                    .forEach(e->pathDb2.locations().add(new DBCPosition3D(e.getPosX(), e.getPosY(),e.getPosZ())));
        });


        for (PathProperty pathProperty : pathProperty()) {
            if (path().contains(pathProperty.getPathID())) {
                tmp.get(pathProperty.getPathID()).properties().add(pathProperty);
            }
        }
        paths.putAll(tmp);


        phasesByGroup = cacheProvider.newGenericMapCache("PhasesByGroupCache", new TypeReference<>() {});
        Map<Short, List<Integer>> tmpPhasesByGroup = new HashMap<>();
        for (PhaseXPhaseGroup group : phaseXPhaseGroup()) {
            Phase phase = phase(group.getPhaseID());
            if (phase != null) {
                tmpPhasesByGroup.compute(group.getPhaseGroupID(), Functions.addToList(phase.getId()));
            }
        }
        phasesByGroup.putAll(tmpPhasesByGroup);

        powerTypes = new PowerType[Power.MAX_POWERS.index];
        for (PowerType powerType : powerType())
        {
            Assert.state(powerType.getPowerTypeEnum() < Power.MAX_POWERS.index);
            Assert.state(powerTypes[powerType.getPowerTypeEnum()] == null);
            powerTypes[powerType.getPowerTypeEnum()] = powerType;
        }

        for (PvpDifficulty entry : pvpDifficulty())
        {
            Assert.state(entry.getRangeIndex() < BattlegroundBracketId.values().length,
                    "PvpDifficulty bracket {} exceeded max allowed second {}",
                    entry.getRangeIndex(), BattlegroundBracketId.values().length);
        }

        pvpItemBonus = cacheProvider.newGenericMapCache("PvpItemBonusCache", new TypeReference<>() {});
        for (PvpItem pvpItem : pvpItem()) {
            pvpItemBonus.put(pvpItem.getItemID(), pvpItem.getItemLevelDelta());
        }

        pvpRewardPack = new HashMap<>();
        for (PvpReward pvpReward : pvpReward()) {
            pvpRewardPack.put(Pair.of(pvpReward.getPrestigeLevel(), pvpReward.getHonorLevel()), pvpReward.getRewardPackID());
        }


        pvpTalentsByPosition = cacheProvider.newGenericMapCache("PvpTalentsByPositionCache", new TypeReference<>() {});
        for (PvpTalent talentInfo : pvpTalent())
        {
            Assert.state(talentInfo.getClassID() < PlayerClass.values().length);
            Assert.state(talentInfo.getTierID() < MAX_PVP_TALENT_TIERS, "MAX_PVP_TALENT_TIERS must be at least {}", talentInfo.getTierID() + 1);
            Assert.state(talentInfo.getColumnIndex() < MAX_PVP_TALENT_COLUMNS, "MAX_PVP_TALENT_COLUMNS must be at least {}", talentInfo.getColumnIndex() + 1);
            if (talentInfo.getClassID() == 0)
            {
                for (PlayerClass value : PlayerClass.values()) {
                    int id = value.ordinal() << 8 | talentInfo.getTierID() << 4 | talentInfo.getColumnIndex();
                    pvpTalentsByPosition.compute(id, Functions.addToList(talentInfo));
                }
            }
            else {
                int id = talentInfo.getClassID() << 8 | talentInfo.getTierID() << 4 | talentInfo.getColumnIndex();
                pvpTalentsByPosition.compute(id, Functions.addToList(talentInfo));
            }
        }
        pvpTalentUnlock = new int[MAX_PVP_TALENT_TIERS][MAX_PVP_TALENT_COLUMNS];
        for (PvpTalentUnlock talentUnlock : pvpTalentUnlock())
        {
            Assert.state(talentUnlock.getTierID() < MAX_PVP_TALENT_TIERS, "MAX_PVP_TALENT_TIERS must be at least {}", talentUnlock.getTierID() + 1);
            Assert.state(talentUnlock.getColumnIndex() < MAX_PVP_TALENT_COLUMNS, "MAX_PVP_TALENT_COLUMNS must be at least {}", talentUnlock.getColumnIndex() + 1);
            pvpTalentUnlock[talentUnlock.getTierID()][talentUnlock.getColumnIndex()] = talentUnlock.getHonorLevel();
        }

        questsByQuestLine = cacheProvider.newGenericMapCache("QuestsByQuestLineCache", new TypeReference<>(){});
        Map<Integer, List<QuestLineXQuest>> questsByQuestLineTmp = new HashMap<>();
        for (QuestLineXQuest questLineQuest : questLineXQuest()) {
            questsByQuestLineTmp.compute(questLineQuest.getQuestLineID(), Functions.addToList(questLineQuest));
        }

        questsByQuestLineTmp.forEach((k,v) -> {
            Collections.sort(v);
        });
        questsByQuestLine.putAll(questsByQuestLineTmp);

        questPackages = cacheProvider.newGenericMapCache("QuestPackagesCache", new TypeReference<>(){});
        Map<Short, QuestPackage> tmpQuestPackages = new HashMap<>();
        for (QuestPackageItem questPackageItem : questPackageItem()) {
            QuestPackage compute = tmpQuestPackages.compute(questPackageItem.getPackageID(), Functions.ifAbsent(QuestPackage::new));
            if (questPackageItem.getDisplayType() != QuestPackageFilter.UNMATCHED.ordinal())
                compute.first.add(questPackageItem);
            else
                compute.second.add(questPackageItem);
        }
        questPackages.putAll(tmpQuestPackages);

        rewardPackCurrencyTypes = cacheProvider.newGenericMapCache("RewardPackCurrencyTypesCache", new TypeReference<>(){});
        for (RewardPackXCurrencyType rewardPackXCurrencyType : rewardPackXCurrencyType()) {
            rewardPackCurrencyTypes.compute(rewardPackXCurrencyType.getRewardPackID(), Functions.addToList(rewardPackXCurrencyType));
        }

        rewardPackItems = cacheProvider.newGenericMapCache("RewardPackItemsCache", new TypeReference<>(){});
        for (RewardPackXItem rewardPackXItem : rewardPackXItem()) {
            rewardPackItems.compute(rewardPackXItem.getRewardPackID(), Functions.addToList(rewardPackXItem));
        }
        skillLinesByParentSkillLine = new HashMap<>();
        for (SkillLine skill : skillLine()) {
            if (skill.getParentSkillLineID() != 0) {
                skillLinesByParentSkillLine.compute(skill.getParentSkillLineID(), Functions.addToList(skill));
            }
        }

        skillLineAbilitiesBySkillUpSkill = cacheProvider.newGenericMapCache("SkillLineAbilitiesBySkillUpSkillCache", new TypeReference<>(){});
        for (SkillLineAbility skillLineAbility : skillLineAbility()) {
            skillLineAbilitiesBySkillUpSkill.compute(skillLineAbility.getSkillLine(), Functions.addToList(skillLineAbility));
        }

        ruleSetItemUpgrade = cacheProvider.newGenericMapCache("RuleSetItemUpgradeCache", new TypeReference<>(){});
        for (RuleSetItemUpgrade e : ruleSetItemUpgrade())
            ruleSetItemUpgrade.put(e.getItemID(), e.getItemUpgradeID());

        skillRaceClassInfoBySkill = cacheProvider.newGenericMapCache("SkillRaceClassInfoBySkillCache", new TypeReference<>(){});
        for (SkillRaceClassInfo entry : skillRaceClassInfo()) {
            if (skillLine().contains(entry.getSkillID())) {
                skillRaceClassInfoBySkill.compute(entry.getSkillID(), Functions.addToList(entry));
            }
        }

        specializationSpellsBySpec = cacheProvider.newGenericMapCache("SpecializationSpellsBySpecCache", new TypeReference<>(){});
        for (SpecializationSpell specSpells : specializationSpell()) {
            specializationSpellsBySpec.compute(specSpells.getSpecID(), Functions.addToList(specSpells));
        }


        spellFamilyNames = new HashSet<>();
        for (SpellClassOption classOption : spellClassOption()) {
            spellFamilyNames.add(classOption.getSpellClassSet());
        }

        spellPowerDifficulties = cacheProvider.newGenericMapCache("SpellPowerDifficultiesCache", new TypeReference<>(){});
        spellPowers = cacheProvider.newGenericMapCache("SpellPowersCache", new TypeReference<>(){});
        for (SpellPower power : spellPower()) {
            SpellPowerDifficulty powerDifficulty = spellPowerDifficulty(power.getId());
            if (powerDifficulty != null) {
                int id = power.getSpellID() << 16 | powerDifficulty.getDifficultyID() << 8 | powerDifficulty.getOrderIndex();
                spellPowerDifficulties.compute(id, Functions.addToList(power));
            } else {
                spellPowers.compute(power.getSpellID(), Functions.addToList(power));
            }
        }

        spellProcsPerMinuteMods = cacheProvider.newGenericMapCache("SpellProcsPerMinuteModsCache", new TypeReference<>(){});
        for (SpellProcsPerMinuteMod ppmMod : spellProcsPerMinuteMod()) {
            spellProcsPerMinuteMods.compute(ppmMod.getSpellProcsPerMinuteID(), Functions.addToList(ppmMod));
        }

        spellVisualMissilesBySet = cacheProvider.newGenericMapCache("SpellVisualMissilesBySetCache", new TypeReference<>(){});
        for (SpellVisualMissile spellVisualMissile : spellVisualMissile()) {
            spellVisualMissilesBySet.compute(spellVisualMissile.getSpellVisualMissileSetID(), Functions.addToList(spellVisualMissile));
        }

        talentsByPosition = cacheProvider.newGenericMapCache("TalentsByPositionCache", new TypeReference<>(){});
        for (Talent talentInfo : talent()) {
            Assert.state(talentInfo.getClassID() < PlayerClass.values().length);
            Assert.state(talentInfo.getTierID() < MAX_TALENT_TIERS, "MAX_TALENT_TIERS must be at least %u", talentInfo.getTierID() + 1);
            Assert.state(talentInfo.getColumnIndex() < MAX_TALENT_COLUMNS, "MAX_TALENT_COLUMNS must be at least %u", talentInfo.getColumnIndex() + 1);
            int id = talentInfo.getClassID() << 8 | talentInfo.getTierID() << 4 | talentInfo.getColumnIndex();
            talentsByPosition.compute(id, Functions.addToList(talentInfo));
        }

        taxiPaths = cacheProvider.newGenericMapCache("TaxiPathsCache", new TypeReference<>(){});
        for (TaxiPath entry : taxiPath()) {
            taxiPaths.put(Pair.of(entry.getFromTaxiNode(), entry.getToTaxiNode()), entry);
        }

        taxiPathNodesByPath = cacheProvider.newGenericMapCache("TaxiPathNodesByPathCache", new TypeReference<>(){});
        for (TaxiPathNode entry : taxiPathNode()) {
            //the max value of node index is 10100111(167)
            int id = entry.getPathID() << 10 | entry.getNodeIndex();
            taxiPathNodesByPath.compute(id, Functions.addToList(entry));
        }

        // Initialize global taxinodes mask
        // reinitialize internal storage for globals after loading TaxiNodes.db2
        taxiNodesMask = new TaxiMask();
        hordeTaxiNodesMask = new TaxiMask();
        allianceTaxiNodesMask = new TaxiMask();
        oldContinentsNodesMask = new TaxiMask();
        // include existed nodes that have at least single not spell base (scripted) path
        for (TaxiNode node : taxiNode()) {
            if (!node.isPartOfTaxiNetwork())
                continue;

            // valid taxi network node
            int field = (node.getId() - 1) / (8);
            byte subMask = (byte) (1 << ((node.getId() - 1) % (8)));

            byte currentValue = taxiNodesMask.get(field);
            taxiNodesMask.set(field, (byte)(currentValue | subMask));
            if (node.flags().hasFlag(TaxiNodeFlag.ShowOnHordeMap)) {
                currentValue = hordeTaxiNodesMask.get(field);
                hordeTaxiNodesMask.set(field, (byte)(currentValue | subMask));
            }
            if (node.flags().hasFlag(TaxiNodeFlag.ShowOnAllianceMap)) {
                currentValue = allianceTaxiNodesMask.get(field);
                allianceTaxiNodesMask.set(field, (byte)(currentValue | subMask));
            }

            int nodeMap = determinaAlternateMapPosition(node.getContinentID(), node.getPosX(), node.getPosY(), node.getPosZ(), null);
            if (nodeMap < 2) {
                currentValue = oldContinentsNodesMask.get(field);
                oldContinentsNodesMask.set(field, (byte)(currentValue | subMask));
            }
        }

        toys = new HashSet<>();
        for (Toy toy : toy()) {
            toys.add(toy.getItemID());
        }

        transmogSetsByItemModifiedAppearance = cacheProvider.newGenericMapCache("TransmogSetsByItemModifiedAppearanceCache", new TypeReference<>(){});
        transmogSetItemsByTransmogSet = cacheProvider.newGenericMapCache("TransmogSetItemsByTransmogSetCache", new TypeReference<>(){});
        for (TransmogSetItem transmogSetItem : transmogSetItem())
        {
            TransmogSet set = transmogSet(transmogSetItem.getTransmogSetID());
            if (set == null)
                continue;
            transmogSetsByItemModifiedAppearance.compute(transmogSetItem.getItemModifiedAppearanceID(), Functions.addToList(set));
            transmogSetItemsByTransmogSet.compute(transmogSetItem.getTransmogSetID(), Functions.addToList(transmogSetItem));
        }



        wmoAreaTableLookup = cacheProvider.newGenericMapCache("WmoAreaTableLookupCache", new TypeReference<>(){});
        for (WmoAreaTable entry : wmoAreaTable()) {
            wmoAreaTableLookup.put(Tuple.of(entry.getWmoID(), entry.getNameSetID(), entry.getWmoGroupID()), entry);
        }

        worldMapAreaByAreaID = cacheProvider.newGenericMapCache("worldMapAreaByAreaIDCache", new TypeReference<>(){});
        for (WorldMapArea worldMapArea : worldMapArea()) {
            worldMapAreaByAreaID.put(worldMapArea.getAreaID(), worldMapArea);
        }

        worldSafeLocContainer = cacheProvider.newGenericMapCache("WorldSafeLocContainerCache", new TypeReference<>(){});
        for (WorldSafeLoc safeLoc : worldSafeLoc()) {
            worldSafeLocContainer.put(safeLoc.getId(), safeLoc);
        }
    }


    private DbcEntityStore<?> loadDb2File(Class<? extends DbcEntity> clazz, Set<Locale> availableDb2Locales) {
        DbcEntityStore<? extends DbcEntity> dbcEntityStore = cacheProvider.newDbcEntityStore(clazz);
        String db2name = clazz.getAnnotation(Db2DataBind.class).name();
        if(dbcEntityStore.isEmpty()) {
            Db2EntityReader.read(dataFolder, dbcLocale, clazz).forEach(dbcEntityStore::append);
            SERVER_LOADING.info(">> Load {} rows from {}", dbcEntityStore.size(), db2name);
        } else {
            SERVER_LOADING.info(">> Reuse {} rows from {}", dbcEntityStore.size(), db2name);
        }
        return dbcEntityStore;
    }

    public <T extends DbcEntity> DbcEntityStore<T> getEntityStore(DbcObjects object) {
        @SuppressWarnings("unchecked")
        DbcEntityStore<T> objectStore = (DbcEntityStore<T>) entityStoreMap.get(object);
        return objectStore;
    }

    @Override
    public int getEmptyAnimStateID() {
        return this.animationData().size();
    }

    @Override
    public List<Short> getAreasForGroup(Short areaGroupId) {
        List<Short> values = areaGroupMembers.get(areaGroupId);
        return values == null ? List.of() : values;
    }

    @Override
    public boolean isInArea(Short objectAreaId, Short areaId) {
        do {
            if (Objects.equals(objectAreaId, areaId))
                return true;

            AreaTable objectArea = this.areaTable(objectAreaId.intValue());
            if (objectArea == null)
                break;

            objectAreaId = objectArea.getParentAreaID();
        } while (objectAreaId > 0);

        return false;
    }

    @Override
    public List<ArtifactPower> getArtifactPowers(Short artifactId) {
        var result = artifactPowers.get(artifactId);
        return result == null ? List.of() : result;
    }

    @Override
    public Set<Short> getArtifactPowerLinks(Short artifactPowerId) {
        var result = artifactPowerLinks.get(artifactPowerId);
        return result == null ? Set.of() : result;
    }

    @Override
    public ArtifactPowerRank getArtifactPowerRank(Short artifactPowerId, Byte rank) {
        return artifactPowerRanks.get(Pair.of(artifactPowerId, rank));
    }

    @Override
    public String getBroadcastTextValue(BroadcastText broadcastText) {
        return getBroadcastTextValue(broadcastText, dbcLocale, Gender.MALE, false);
    }

    @Override
    public String getBroadcastTextValue(BroadcastText broadcastText, Locale locale, Gender gender, boolean forceGender) {
        if ((gender == Gender.FEMALE || gender == Gender.NONE) && (forceGender || !StringUtils.hasText(broadcastText.getText1().get(DEFAULT_LOCALE)))) {
            if (!StringUtils.hasText(broadcastText.getText1().get(locale)))
                return broadcastText.getText1().get(locale);
            return broadcastText.getText1().get(DEFAULT_LOCALE);
        }

        if (!StringUtils.hasText(broadcastText.getText().get(locale))) {
            return broadcastText.getText().get(locale);
        }
        return broadcastText.getText().get(DEFAULT_LOCALE);
    }

    @Override
    public Integer getBroadcastTextDuration(Integer broadcastTextId) {
        return null;
    }

    @Override
    public Integer getBroadcastTextDuration(Integer broadcastTextId, Locale locale) {
        return null;
    }

    @Override
    public boolean hasCharacterFacialHairStyle(Byte race, Byte gender, Byte variationId) {
        return characterFacialHairStyles.contains(Tuple.of(race, gender, variationId));
    }

    @Override
    public boolean hasCharSections(Race race, Gender gender, CharBaseSectionVariation variation) {
        Set<CharSection> charSectionSet = charSections.get(Tuple.of(race, gender, variation));
        return charSectionSet != null && charSectionSet.isEmpty();

    }

    @Override
    public CharSection getCharSection(Byte race, Byte gender, CharBaseSectionVariation variation, Byte variationIndex, Byte colorIndex) {
        Tuple<Byte, Byte, CharBaseSectionVariation> key = Tuple.of(race, gender, variation);
        return charSections.get(key).stream()
                .filter(e -> Objects.equals(e.getVariationIndex(), variationIndex) && Objects.equals(e.getColorIndex(), colorIndex))
                .findFirst()
                .orElseThrow();

    }

    @Override
    public CharStartOutfit getCharStartOutfit(byte race, byte klass, byte gender) {
        return charStartOutfits.get(race | (klass << 8) | (gender << 16));
    }

    @Override
    public String getClassName(int klass, Locale locale) {
        ChrClass classEntry = this.chrClass(klass);
        if (classEntry == null) {
            return null;
        }
        return LocalizedString.get(classEntry.getName(), locale);

    }


    @Override
    public ChrSpecialization getChrSpecializationByIndex(PlayerClass playerClass, int index) {
        return chrSpecializationsByIndex[playerClass.ordinal()][index];
    }

    @Override
    public ChrSpecialization getDefaultChrSpecializationForClass(PlayerClass playerClass) {
        ChrSpecialization[] specializationsByIndex = chrSpecializationsByIndex[playerClass.ordinal()];
        for (ChrSpecialization chrSpec : specializationsByIndex) {
            if (chrSpec.flags().hasFlag(ChrSpecializationFlag.Recommended)) {
                return chrSpec;
            }
        }
        return null;
    }

    @Override
    public Integer getPowerIndexByClass(Power power, PlayerClass classId) {
        return powersByClass[classId.ordinal()][power.index];
    }



    @Override
    public String getChrRaceName(Race race, Locale locale) {
        ChrRace raceEntry = chrRace(race);
        if (raceEntry == null)
            return "";

        LocalizedString name = raceEntry.getName();
        if (!StringUtil.isEmpty(name.get(locale)))
            return name.get(locale);

        return name.get(DEFAULT_LOCALE);
    }

    @Override
    public Integer getRedirectedContentTuningId(Integer contentTuningId, Integer redirectFlag) {
        return 0;
    }

    @Override
    public boolean hasContentTuningLabel(Integer contentTuningId, Integer label) {
        return false;
    }

    @Override
    public String getCreatureFamilyPetName(CreatureFamily petFamily, Locale locale) {
        if (petFamily == null)
            return null;

        CreatureFamilyEntry entry = creatureFamily(petFamily);
        if (entry == null)
            return null;

        return entry.getName().get(locale);
    }

    @Override
    public Pair<Float, Float> getCurveXAxisRange(int curveId) {
        List<DBCPosition2D> dbcPosition2DS = curvePoints.get(curveId);
        if(dbcPosition2DS.isEmpty()) {
            return Pair.of(0.0f, 0.0f);
        }
        return Pair.of(dbcPosition2DS.getFirst().x, dbcPosition2DS.getLast().x);
    }


    private static CurveInterpolationMode determineCurveType(Curve curve, List<DBCPosition2D> points) {
        switch (curve.getType()) {
            case 1:
                return points.size() < 4 ? CurveInterpolationMode.Cosine : CurveInterpolationMode.CatmullRom;
            case 2: {
                switch (points.size()) {
                    case 1:
                        return CurveInterpolationMode.Constant;
                    case 2:
                        return CurveInterpolationMode.Linear;
                    case 3:
                        return CurveInterpolationMode.Bezier3;
                    case 4:
                        return CurveInterpolationMode.Bezier4;
                    default:
                        break;
                }
                return CurveInterpolationMode.Bezier;
            }
            case 3:
                return CurveInterpolationMode.Cosine;
            default:
                break;
        }

        return points.size() != 1 ? CurveInterpolationMode.Linear : CurveInterpolationMode.Constant;
    }


    @Override
    public float getCurveValueAt(Integer curveId, float x) {
        var points = curvePoints.get(curveId);
        if (points == null)
            return 0.0f;

        Curve curve = curve(curveId);
        if (points.isEmpty())
            return 0.0f;

        return getCurveValueAt(determineCurveType(curve, points), points, x);
    }

    @Override
    public float getCurveValueAt(CurveInterpolationMode mode, List<DBCPosition2D> points, float x) {
        switch (mode) {
            case Linear: {
                int pointIndex = 0;
                while (pointIndex < points.size() && points.get(pointIndex).x <= x)
                    ++pointIndex;
                if (pointIndex != 0)
                    return points.getFirst().y;
                if (pointIndex >= points.size())
                    return points.getLast().y;
                float xDiff = points.get(pointIndex).x - points.get(pointIndex - 1).x;
                if (xDiff == 0.0)
                    return points.get(pointIndex).y;
                return (((x - points.get(pointIndex - 1).x) / xDiff) * (points.get(pointIndex).y - points.get(pointIndex - 1).y)) + points.get(pointIndex - 1).y;
            }
            case Cosine: {
                int pointIndex = 0;
                while (pointIndex < points.size() && points.get(pointIndex).x <= x)
                    ++pointIndex;
                if (pointIndex != 0)
                    return points.getFirst().y;
                if (pointIndex >= points.size())
                    return points.getLast().y;
                float xDiff = points.get(pointIndex).x - points.get(pointIndex - 1).x;
                if (xDiff == 0.0)
                    return points.get(pointIndex).y;
                return ((points.get(pointIndex).y - points.get(pointIndex - 1).y) * (1.0f - (float) Math.cos((x - points.get(pointIndex - 1).x) / xDiff * MathUtil.PI)) * 0.5f) + points.get(pointIndex - 1).y;
            }
            case CatmullRom: {
                int pointIndex = 1;
                while (pointIndex < points.size() && points.get(pointIndex).x <= x)
                    ++pointIndex;
                if (pointIndex == 1)
                    return points.get(1).y;
                if (pointIndex >= points.size() - 1)
                    return points.get(points.size() - 2).y;
                float xDiff = points.get(pointIndex).x - points.get(pointIndex - 1).x;
                if (xDiff == 0.0)
                    return points.get(pointIndex).y;

                float mu = (x - points.get(pointIndex - 1).x) / xDiff;
                float a0 = -0.5f * points.get(pointIndex - 2).y + 1.5f * points.get(pointIndex - 1).y - 1.5f * points.get(pointIndex).y + 0.5f * points.get(pointIndex + 1).y;
                float a1 = points.get(pointIndex - 2).y - 2.5f * points.get(pointIndex - 1).y + 2.0f * points.get(pointIndex).y - 0.5f * points.get(pointIndex + 1).y;
                float a2 = -0.5f * points.get(pointIndex - 2).y + 0.5f * points.get(pointIndex).y;
                float a3 = points.get(pointIndex - 1).y;

                return a0 * mu * mu * mu + a1 * mu * mu + a2 * mu + a3;
            }
            case Bezier3: {
                float xDiff = points.get(2).x - points.getFirst().x;
                if (xDiff == 0.0)
                    return points.get(1).y;
                float mu = (x - points.getFirst().x) / xDiff;
                return ((1.0f - mu) * (1.0f - mu) * points.getFirst().y) + (1.0f - mu) * 2.0f * mu * points.get(1).y + mu * mu * points.get(2).y;
            }
            case Bezier4: {
                float xDiff = points.get(3).x - points.getFirst().x;
                if (xDiff == 0.0)
                    return points.get(1).y;
                float mu = (x - points.getFirst().x) / xDiff;
                return (1.0f - mu) * (1.0f - mu) * (1.0f - mu) * points.getFirst().y
                        + 3.0f * mu * (1.0f - mu) * (1.0f - mu) * points.get(1).y
                        + 3.0f * mu * mu * (1.0f - mu) * points.get(2).y
                        + mu * mu * mu * points.get(3).y;
            }
            case Bezier: {
                float xDiff = points.getLast().x - points.getFirst().x;
                if (xDiff == 0.0f)
                    return points.getLast().y;

                List<Float> tmp = new ArrayList<>(points.size());
                for (int i = 0; i < points.size(); ++i)
                    tmp.set(i, points.get(i).y);

                float mu = (x - points.getFirst().x) / xDiff;
                int i = points.size() - 1;
                while (i > 0) {
                    for (int k = 0; k < i; ++k) {
                        float val = tmp.get(k) + mu * (tmp.get(k + 1) - tmp.get(k));
                        tmp.set(k, val);
                    }
                    --i;
                }
                return tmp.getFirst();
            }
            case Constant:
                return points.getFirst().y;
            default:
                break;
        }

        return 0.0f;
    }

    @Override
    public EmotesTextSound getTextSoundEmoteFor(Integer emote, Byte race, Byte gender, Byte class_) {
        return null;
    }

    @Override
    public float evaluateExpectedStat(ExpectedStatType stat, Integer level, Integer expansion, Integer contentTuningId, PlayerClass unitClass, Integer mythicPlusMilestoneSeason) {
        return 0;
    }

    @Override
    public List<Integer> getFactionTeamList(Integer faction) {
        return List.of();
    }

    @Override
    public Set<FriendshipRepReaction> getFriendshipRepReactions(Integer friendshipRepID) {
        return Set.of();
    }

    @Override
    public Integer getGlobalCurveId(GlobalCurve globalCurveType) {
        return 0;
    }

    @Override
    public List<Integer> getGlyphBindableSpells(Integer glyphPropertiesId) {
        return List.of();
    }

    @Override
    public Heirloom getHeirloomByItemId(Integer itemId) {
        return null;
    }

    @Override
    public ItemChildEquipment getItemChildEquipment(Integer itemId) {
        return null;
    }

    @Override
    public ItemClassEntry getItemClassByOldEnum(Integer itemClass) {
        return null;
    }

    @Override
    public boolean hasItemCurrencyCost(Integer itemId) {
        return false;
    }

    @Override
    public List<ItemLimitCategoryCondition> getItemLimitCategoryConditions(Integer categoryId) {
        return List.of();
    }

    @Override
    public Integer getItemDisplayId(Integer itemId, Integer appearanceModId) {
        return 0;
    }

    @Override
    public ItemModifiedAppearance getItemModifiedAppearance(int itemId, short appearanceModId) {
        var result = itemModifiedAppearancesByItem.get(itemId | (appearanceModId << 24));
        if (result != null)
            return result;
        // Fall back to unmodified appearance
        if (appearanceModId != 0) {
            return itemModifiedAppearancesByItem.get(itemId);
        }

        return null;
    }

    @Override
    public ItemModifiedAppearance getDefaultItemModifiedAppearance(int itemId) {
        return itemModifiedAppearancesByItem.get(itemId);
    }

    @Override
    public List<ItemSetSpell> getItemSetSpells(Integer itemSetId) {
        return List.of();
    }

    @Override
    public JournalTier getJournalTier(Integer index) {
        return null;
    }

    @Override
    public List<ItemSpecOverride> getItemSpecOverrides(Integer itemId) {
        return List.of();
    }

    @Override
    public JournalInstance getJournalInstanceByMapId(Integer mapId) {
        return null;
    }

    @Override
    public List<JournalEncounterItem> getJournalItemsByEncounter(Integer encounterId) {
        return List.of();
    }

    @Override
    public List<JournalEncounter> getJournalEncounterByJournalInstanceId(Integer instanceId) {
        return List.of();
    }

    @Override
    public LfgDungeon getLfgDungeon(Integer mapId, Difficulty difficulty) {
        return null;
    }

    @Override
    public Integer getDefaultMapLight(Integer mapId) {
        return 0;
    }

    @Override
    public Integer getLiquidFlags(Integer liquidType) {
        LiquidType type = liquidType(liquidType);
        return type == null ? 0 : 1 << type.getSoundBank();
    }

    @Override
    public MapDifficulty getDefaultMapDifficulty(Integer mapId) {
        return getDefaultMapDifficulty(mapId, null);
    }

    @Override
    public MapDifficulty getDefaultMapDifficulty(Integer mapId, Difficulty difficulty) {
        Map<Difficulty, MapDifficulty> difficultiesForMap = mapDifficulties.get(mapId);
        if (difficultiesForMap == null || difficultiesForMap.isEmpty())
            return null;

        for (var pair : difficultiesForMap.entrySet()) {
            DifficultyEntry difficultyEntry = difficulty(pair.getKey().ordinal());
            if (difficultyEntry == null)
                continue;
            if (difficultyEntry.flags().hasFlag(DifficultyFlag.DEFAULT)) {
                return pair.getValue();
            }
        }

        var first = difficultiesForMap.entrySet().iterator().next();

        return first.getValue();
    }

    @Override
    public MapDifficulty getMapDifficultyData(Integer mapId, Difficulty difficulty) {
        var e = mapDifficulties.get(mapId);
        if (e == null)
            return null;

        return e.get(difficulty);
    }

    @Override
    public MapDifficulty getDownscaledMapDifficultyData(int mapId, Difficulty difficulty) {
        var diffEntry = difficulty(difficulty.ordinal());

        if (diffEntry == null) {
            return getDefaultMapDifficulty(mapId, difficulty);
        }

        var mapDiff = getMapDifficultyData(mapId, difficulty);
        while (mapDiff == null) {
            Difficulty tmpDiff = Difficulty.values()[diffEntry.getFallbackDifficultyID()];
            diffEntry = difficulty(diffEntry.getFallbackDifficultyID());

            if (diffEntry == null) {
                return getDefaultMapDifficulty(mapId, difficulty);
            }

            // pull new data
            // we are 10 normal or 25 normal
            mapDiff = getMapDifficultyData(mapId, tmpDiff);
        }


        return mapDiff;
    }

    @Override
    public String getNameGenEntry(Byte race, Byte gender) {
        return "";
    }

    @Override
    public Map<Integer, PlayerCondition> getMapDifficultyConditions(Integer mapDifficultyId) {
        return Map.of();
    }

    @Override
    public Mount getMount(Integer spellId) {
        return null;
    }

    @Override
    public Mount getMountById(int id) {
        return null;
    }

    @Override
    public Set<MountTypeXCapability> getMountCapabilities(Integer mountType) {
        return Set.of();
    }

    @Override
    public List<MountXDisplay> getMountDisplays(Integer mountId) {
        return List.of();
    }

    @Override
    public String getNameGen(Byte race, Byte gender) {
        return "";
    }

    @Override
    public ResponseCodes validateName(String name) {
        return null;
    }

    @Override
    public ResponseCodes validateName(String name, Locale locale) {
        return null;
    }

    @Override
    public Integer getNumTalentsAtLevel(Integer level, PlayerClass playerClass) {
        return 0;
    }

    @Override
    public ParagonReputation getParagonReputation(Integer factionId) {
        return null;
    }

    @Override
    public PathDb2 getPath(Integer pathId) {
        return null;
    }

    @Override
    public List<Integer> getPhasesForGroup(Integer group) {
        return List.of();
    }

    @Override
    public PowerType getPowerType(Power power) {
        return null;
    }

    @Override
    public PowerType getPowerTypeByName(String name) {
        return null;
    }

    @Override
    public Byte getPvpItemLevelBonus(Integer itemId) {
        return 0;
    }

    @Override
    public Byte getMaxPrestige() {
        return 0;
    }

    @Override
    public PvpDifficulty getBattlegroundBracketByLevel(Integer mapId, Integer level) {
        return null;
    }

    @Override
    public PvpDifficulty getBattlegroundBracketById(Integer mapId, BattlegroundBracketId id) {
        return null;
    }

    @Override
    public Integer getRewardPackIDForPvpRewardByHonorLevelAndPrestige(byte honorLevel, byte prestige) {
        return 0;
    }

    @Override
    public int getRequiredHonorLevelForPvpTalent(PvpTalent talentInfo) {
        Assert.state(talentInfo != null);
        return pvpTalentUnlock[talentInfo.getTierID()][talentInfo.getColumnIndex()];

    }

    @Override
    public List<PvpTalent> getPvpTalentsByPosition(PlayerClass class_, int tier, int column) {
        int id = class_.ordinal() << 8 | tier << 4 | column;
        return pvpTalentsByPosition.get(id);
    }

    @Override
    public List<QuestLineXQuest> getQuestsForQuestLine(Integer questLineId) {
        return List.of();
    }

    @Override
    public List<QuestPackageItem> getQuestPackageItems(Integer questPackageID) {


        return List.of();
    }

    @Override
    public List<QuestPackageItem> getQuestPackageItemsFallback(Integer questPackageID) {
        return List.of();
    }

    @Override
    public Integer getQuestUniqueBitFlag(Integer questId) {
        return 0;
    }

    @Override
    public List<RewardPackXCurrencyType> getRewardPackCurrencyTypesByRewardID(Integer rewardPackID) {
        return List.of();
    }

    @Override
    public List<RewardPackXItem> getRewardPackItemsByRewardID(Integer rewardPackID) {
        return List.of();
    }

    @Override
    public Integer getRulesetItemUpgrade(Integer itemId) {
        return 0;
    }

    @Override
    public SkillRaceClassInfo getSkillRaceClassInfo(Integer skill, Byte race, PlayerClass class_) {
        return null;
    }

    @Override
    public List<SkillLine> getSkillLinesForParentSkill(Integer parentSkillId) {
        return List.of();
    }

    @Override
    public List<SkillLineAbility> getSkillLineAbilitiesBySkill(Integer skillId) {
        return List.of();
    }

    @Override
    public SkillRaceClassInfo getSkillRaceClassInfo(Integer skill, Byte race, Byte class_) {
        return null;
    }

    @Override
    public List<SkillRaceClassInfo> getSkillRaceClassInfo(Integer skill) {
        return List.of();
    }

    @Override
    public boolean isValidSpellFamilyName(SpellFamilyName family) {
        return false;
    }

    @Override
    public List<SpellProcsPerMinuteMod> getSpellProcPerMinuteMods(Integer spellProcPerMinuteId) {
        return List.of();
    }

    @Override
    public List<SpellVisualMissile> getSpellVisualMissiles(Integer spellVisualMissileSetId) {
        return List.of();
    }

    @Override
    public TaxiPath getTaxiPath(Integer from, Integer to) {
        return null;
    }

    @Override
    public boolean isTotemCategoryCompatibleWith(Integer itemTotemCategoryId, Integer requiredTotemCategoryId, boolean requireAllTote) {
        return false;
    }

    @Override
    public boolean IsToyItem(Integer toy) {
        return false;
    }

    @Override
    public List<TransmogSet> getTransmogSetsForItemModifiedAppearance(Integer itemModifiedAppearanceId) {
        return List.of();
    }

    @Override
    public List<TransmogSetItem> getTransmogSetItems(Integer transmogSetId) {
        return List.of();
    }

    @Override
    public boolean getUiMapPosition(float x, float y, float z, Integer mapId, Integer areaId, Integer wmoDoodadPlacementId, Integer wmoGroupId, UiMapSystem system, boolean local) {
        return false;
    }

    public int determinaAlternateMapPosition(int mapId, float x, float y, float z, DBCPosition2D newPos /*= nullptr*/)
    {
        WorldMapTransform transformation = null;
        for (WorldMapTransform transform : worldMapTransform())
        {
            if (transform.getMapID() != mapId)
                continue;
            if (transform.getAreaID() == 0)
                continue;
            if ((transform.getFlags() & WORLD_MAP_TRANSFORMS_FLAG_DUNGEON) != 0)
                continue;
            if (transform.getRegionMinX() > x || transform.getRegionMaxX() < x)
                continue;
            if (transform.getRegionMinY() > y || transform.getRegionMaxY() < y)
                continue;
            if (transform.getRegionMinZ() > z || transform.getRegionMaxZ() < z)
                continue;

            if (transformation == null || transformation.getPriority() < transform.getPriority())
                transformation = transform;
        }

        if (transformation == null)
        {

            if (newPos != null)
            {
                newPos.x = x;
                newPos.y = y;
            }
            return mapId;
        }


        if (newPos ==null)
            return transformation.getNewMapID();

        if (Math.abs(transformation.getRegionScale() - 1.0f) > 0.001f)
        {
            x = (x - transformation.getRegionMinX()) * transformation.getRegionScale() + transformation.getRegionMinX();
            y = (y - transformation.getRegionMinY()) * transformation.getRegionScale() + transformation.getRegionMinY();
        }

        newPos.x = x + transformation.getRegionOffsetX();
        newPos.y = y + transformation.getRegionOffsetY();

        return transformation.getNewMapID();
    }

    @Override
    public DBCPosition2D zone2MapCoordinates(Integer areaId) {
        return null;
    }

    @Override
    public DBCPosition2D map2ZoneCoordinates(Integer areaId) {
        return null;
    }

    @Override
    public boolean isUiMapPhase(Integer phaseId) {
        return false;
    }

    @Override
    public WmoAreaTable getWMOAreaTable(Integer rootId, Integer adtId, Integer groupId) {
        return null;
    }

    @Override
    public Set<Integer> getPVPStatIDsForMap(Integer mapId) {
        return Set.of();
    }

    @Override
    public List<ItemEffect> getItemEffectsForItemId(Integer itemId) {
        return List.of();
    }

    @Override
    public WorldSafeLoc getWorldSafeLoc(int id) {
        return null;
    }

    @Override
    public void determineAlternateMapPosition(Integer mapId, float x, float y, float z) {

    }

    @Override
    public Set<Integer> getDefaultItemBonusTree(Integer itemId, ItemContext itemContext) {
        return Set.of();
    }

    @Override
    public List<ItemBonus> getItemBonusList(int bonusList) {
        return itemBonusLists.get((short)bonusList);
    }


}
