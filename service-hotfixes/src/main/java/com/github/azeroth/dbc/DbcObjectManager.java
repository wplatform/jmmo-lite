package com.github.azeroth.dbc;

import com.github.azeroth.cache.DbcEntityStore;
import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.Locale;
import com.github.azeroth.common.Pair;
import com.github.azeroth.dbc.defines.*;
import com.github.azeroth.dbc.domain.*;
import com.github.azeroth.dbc.domain.ArtifactCategory;
import com.github.azeroth.dbc.domain.ChrSpecialization;
import com.github.azeroth.dbc.domain.CreatureFamilyEntry;
import com.github.azeroth.dbc.domain.CreatureTypeEntry;
import com.github.azeroth.dbc.domain.Emote;
import com.github.azeroth.dbc.domain.Language;
import com.github.azeroth.dbc.domain.QuestInfo;
import com.github.azeroth.dbc.domain.QuestSort;
import com.github.azeroth.dbc.domain.SpellCategory;
import com.github.azeroth.dbc.domain.SpellVisualKit;
import com.github.azeroth.dbc.domain.TotemCategory;
import com.github.azeroth.dbc.model.DBCPosition2D;
import com.github.azeroth.dbc.model.PathDb2;

import com.github.azeroth.dbc.model.TaxiMask;
import com.github.azeroth.defines.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DbcObjectManager {

    default DbcEntityStore<Achievement> achievement() {
        return getEntityStore(DbcObjects.Achievement);
    }

    default Achievement achievement(int id) {
        return achievement().get(id);
    }

    default DbcEntityStore<AchievementCategory> achievementCategory() {
        return getEntityStore(DbcObjects.AchievementCategory);
    }

    default AchievementCategory achievementCategory(int id) {
        return achievementCategory().get(id);
    }

    default DbcEntityStore<AdventureJournal> adventureJournal() {
        return getEntityStore(DbcObjects.AdventureJournal);
    }

    default AdventureJournal adventureJournal(int id) {
        return adventureJournal().get(id);
    }

    default DbcEntityStore<AdventureMapPoi> adventureMapPoi() {
        return getEntityStore(DbcObjects.AdventureMapPoi);
    }

    default AdventureMapPoi adventureMapPoi(int id) {
        return adventureMapPoi().get(id);
    }

    default DbcEntityStore<AnimationData> animationData() {
        return getEntityStore(DbcObjects.AnimationData);
    }

    default AnimationData animationData(int id) {
        return animationData().get(id);
    }

    default DbcEntityStore<AnimKit> animKit() {
        return getEntityStore(DbcObjects.AnimKit);
    }

    default AnimKit animKit(int id) {
        return animKit().get(id);
    }

    default DbcEntityStore<AreaGroupMember> areaGroupMember() {
        return getEntityStore(DbcObjects.AreaGroupMember);
    }

    default AreaGroupMember areaGroupMember(int id) {
        return areaGroupMember().get(id);
    }

    default DbcEntityStore<AreaTable> areaTable() {
        return getEntityStore(DbcObjects.AreaTable);
    }

    default AreaTable areaTable(int id) {
        return areaTable().get(id);
    }

    default DbcEntityStore<AreaTriggerEntry> areaTrigger() {
        return getEntityStore(DbcObjects.AreaTrigger);
    }

    default AreaTriggerEntry areaTrigger(int id) {
        return areaTrigger().get(id);
    }

    default DbcEntityStore<AreaTriggerActionSet> areaTriggerActionSet() {
        return getEntityStore(DbcObjects.AreaTriggerActionSet);
    }

    default AreaTriggerActionSet areaTriggerActionSet(int id) {
        return areaTriggerActionSet().get(id);
    }

    default DbcEntityStore<ArmorLocation> armorLocation() {
        return getEntityStore(DbcObjects.ArmorLocation);
    }

    default ArmorLocation armorLocation(int id) {
        return armorLocation().get(id);
    }

    default DbcEntityStore<Artifact> artifact() {
        return getEntityStore(DbcObjects.Artifact);
    }

    default Artifact artifact(int id) {
        return artifact().get(id);
    }

    default DbcEntityStore<ArtifactAppearance> artifactAppearance() {
        return getEntityStore(DbcObjects.ArtifactAppearance);
    }

    default ArtifactAppearance artifactAppearance(int id) {
        return artifactAppearance().get(id);
    }

    default DbcEntityStore<ArtifactAppearanceSet> artifactAppearanceSet() {
        return getEntityStore(DbcObjects.ArtifactAppearanceSet);
    }

    default ArtifactAppearanceSet artifactAppearanceSet(int id) {
        return artifactAppearanceSet().get(id);
    }

    default DbcEntityStore<ArtifactCategory> artifactCategory() {
        return getEntityStore(DbcObjects.ArtifactCategory);
    }

    default ArtifactCategory artifactCategory(int id) {
        return artifactCategory().get(id);
    }

    default DbcEntityStore<ArtifactPower> artifactPower() {
        return getEntityStore(DbcObjects.ArtifactPower);
    }

    default ArtifactPower artifactPower(int id) {
        return artifactPower().get(id);
    }

    default DbcEntityStore<ArtifactPowerLink> artifactPowerLink() {
        return getEntityStore(DbcObjects.ArtifactPowerLink);
    }

    default ArtifactPowerLink artifactPowerLink(int id) {
        return artifactPowerLink().get(id);
    }

    default DbcEntityStore<ArtifactPowerPicker> artifactPowerPicker() {
        return getEntityStore(DbcObjects.ArtifactPowerPicker);
    }

    default ArtifactPowerPicker artifactPowerPicker(int id) {
        return artifactPowerPicker().get(id);
    }

    default DbcEntityStore<ArtifactPowerRank> artifactPowerRank() {
        return getEntityStore(DbcObjects.ArtifactPowerRank);
    }

    default ArtifactPowerRank artifactPowerRank(int id) {
        return artifactPowerRank().get(id);
    }

    default DbcEntityStore<ArtifactQuestXp> artifactQuestXp() {
        return getEntityStore(DbcObjects.ArtifactQuestXp);
    }

    default ArtifactQuestXp artifactQuestXp(int id) {
        return artifactQuestXp().get(id);
    }

    default DbcEntityStore<ArtifactTier> artifactTier() {
        return getEntityStore(DbcObjects.ArtifactTier);
    }

    default ArtifactTier artifactTier(int id) {
        return artifactTier().get(id);
    }

    default DbcEntityStore<ArtifactUnlock> artifactUnlock() {
        return getEntityStore(DbcObjects.ArtifactUnlock);
    }

    default ArtifactUnlock artifactUnlock(int id) {
        return artifactUnlock().get(id);
    }

    default DbcEntityStore<AuctionHouse> auctionHouse() {
        return getEntityStore(DbcObjects.AuctionHouse);
    }

    default AuctionHouse auctionHouse(int id) {
        return auctionHouse().get(id);
    }

    default DbcEntityStore<BankBagSlotPrice> bankBagSlotPrice() {
        return getEntityStore(DbcObjects.BankBagSlotPrice);
    }

    default BankBagSlotPrice bankBagSlotPrice(int id) {
        return bankBagSlotPrice().get(id);
    }

    default DbcEntityStore<BannedAddon> bannedAddon() {
        return getEntityStore(DbcObjects.BannedAddon);
    }

    default BannedAddon bannedAddon(int id) {
        return bannedAddon().get(id);
    }

    default DbcEntityStore<BarberShopStyle> barberShopStyle() {
        return getEntityStore(DbcObjects.BarberShopStyle);
    }

    default BarberShopStyle barberShopStyle(int id) {
        return barberShopStyle().get(id);
    }

    default DbcEntityStore<BattlePetAbility> battlePetAbility() {
        return getEntityStore(DbcObjects.BattlePetAbility);
    }

    default BattlePetAbility battlePetAbility(int id) {
        return battlePetAbility().get(id);
    }

    default DbcEntityStore<BattlePetBreedQuality> battlePetBreedQuality() {
        return getEntityStore(DbcObjects.BattlePetBreedQuality);
    }

    default BattlePetBreedQuality battlePetBreedQuality(int id) {
        return battlePetBreedQuality().get(id);
    }

    default DbcEntityStore<BattlePetBreedState> battlePetBreedState() {
        return getEntityStore(DbcObjects.BattlePetBreedState);
    }

    default BattlePetBreedState battlePetBreedState(int id) {
        return battlePetBreedState().get(id);
    }

    default DbcEntityStore<BattlePetSpecie> battlePetSpecie() {
        return getEntityStore(DbcObjects.BattlePetSpecie);
    }

    default BattlePetSpecie battlePetSpecie(int id) {
        return battlePetSpecie().get(id);
    }

    default DbcEntityStore<BattlePetSpeciesState> battlePetSpeciesState() {
        return getEntityStore(DbcObjects.BattlePetSpeciesState);
    }

    default BattlePetSpeciesState battlePetSpeciesState(int id) {
        return battlePetSpeciesState().get(id);
    }

    default DbcEntityStore<BattleMasterList> battleMasterList() {
        return getEntityStore(DbcObjects.BattleMasterList);
    }

    default BattleMasterList battleMasterList(int id) {
        return battleMasterList().get(id);
    }

    default DbcEntityStore<BroadcastText> broadcastText() {
        return getEntityStore(DbcObjects.BroadcastText);
    }

    default BroadcastText broadcastText(int id) {
        return broadcastText().get(id);
    }

    default DbcEntityStore<CfgCategory> cfgCategory() {
        return getEntityStore(DbcObjects.CfgCategory);
    }

    default CfgCategory cfgCategory(int id) {
        return cfgCategory().get(id);
    }

    default DbcEntityStore<CfgRegion> cfgRegion() {
        return getEntityStore(DbcObjects.CfgRegion);
    }

    default CfgRegion cfgRegion(int id) {
        return cfgRegion().get(id);
    }

    default DbcEntityStore<CharacterFacialHairStyle> characterFacialHairStyle() {
        return getEntityStore(DbcObjects.CharacterFacialHairStyle);
    }

    default CharacterFacialHairStyle characterFacialHairStyle(int id) {
        return characterFacialHairStyle().get(id);
    }

    default DbcEntityStore<CharBaseSection> charBaseSection() {
        return getEntityStore(DbcObjects.CharBaseSection);
    }

    default CharBaseSection charBaseSection(int id) {
        return charBaseSection().get(id);
    }

    default DbcEntityStore<CharSection> charSection() {
        return getEntityStore(DbcObjects.CharSection);
    }

    default CharSection charSection(int id) {
        return charSection().get(id);
    }

    default DbcEntityStore<CharStartOutfit> charStartOutfit() {
        return getEntityStore(DbcObjects.CharStartOutfit);
    }

    default CharStartOutfit charStartOutfit(int id) {
        return charStartOutfit().get(id);
    }

    default DbcEntityStore<CharTitle> charTitle() {
        return getEntityStore(DbcObjects.CharTitle);
    }

    default CharTitle charTitle(int id) {
        return charTitle().get(id);
    }

    default DbcEntityStore<ChatChannel> chatChannel() {
        return getEntityStore(DbcObjects.ChatChannel);
    }

    default ChatChannel chatChannel(int id) {
        return chatChannel().get(id);
    }

    default DbcEntityStore<ChrClass> chrClass() {
        return getEntityStore(DbcObjects.ChrClass);
    }

    default ChrClass chrClass(int id) {
        return chrClass().get(id);
    }

    default DbcEntityStore<ChrClassesXPowerType> chrClassesXPowerType() {
        return getEntityStore(DbcObjects.ChrClassesXPowerType);
    }

    default ChrClassesXPowerType chrClassesXPowerType(int id) {
        return chrClassesXPowerType().get(id);
    }

    default DbcEntityStore<ChrRace> chrRace() {
        return getEntityStore(DbcObjects.ChrRace);
    }

    default ChrRace chrRace(int id) {
        return chrRace().get(id);
    }

    default ChrRace chrRace(Race race) {
        return chrRace().get(race.ordinal());
    }

    default DbcEntityStore<ChrSpecialization> chrSpecialization() {
        return getEntityStore(DbcObjects.ChrSpecialization);
    }

    default ChrSpecialization chrSpecialization(int id) {
        return chrSpecialization().get(id);
    }

    default DbcEntityStore<CinematicCamera> cinematicCamera() {
        return getEntityStore(DbcObjects.CinematicCamera);
    }

    default CinematicCamera cinematicCamera(int id) {
        return cinematicCamera().get(id);
    }

    default DbcEntityStore<CinematicSequence> cinematicSequence() {
        return getEntityStore(DbcObjects.CinematicSequence);
    }

    default CinematicSequence cinematicSequence(int id) {
        return cinematicSequence().get(id);
    }

    default DbcEntityStore<ConversationLine> conversationLine() {
        return getEntityStore(DbcObjects.ConversationLine);
    }

    default ConversationLine conversationLine(int id) {
        return conversationLine().get(id);
    }

    default DbcEntityStore<CreatureDisplayInfo> creatureDisplayInfo() {
        return getEntityStore(DbcObjects.CreatureDisplayInfo);
    }

    default CreatureDisplayInfo creatureDisplayInfo(int id) {
        return creatureDisplayInfo().get(id);
    }

    default DbcEntityStore<CreatureDisplayInfoExtra> creatureDisplayInfoExtra() {
        return getEntityStore(DbcObjects.CreatureDisplayInfoExtra);
    }

    default CreatureDisplayInfoExtra creatureDisplayInfoExtra(int id) {
        return creatureDisplayInfoExtra().get(id);
    }

    default DbcEntityStore<CreatureFamilyEntry> creatureFamily() {
        return getEntityStore(DbcObjects.CreatureFamily);
    }

    default CreatureFamilyEntry creatureFamily(CreatureFamily id) {
        return creatureFamily().get(id.ordinal());
    }

    default DbcEntityStore<CreatureModelData> creatureModelData() {
        return getEntityStore(DbcObjects.CreatureModelData);
    }

    default CreatureModelData creatureModelData(int id) {
        return creatureModelData().get(id);
    }

    default DbcEntityStore<CreatureTypeEntry> creatureType() {
        return getEntityStore(DbcObjects.CreatureType);
    }

    default CreatureTypeEntry creatureType(CreatureType id) {
        return creatureType().get(id.ordinal());
    }

    default DbcEntityStore<CriteriaEntity> criteria() {
        return getEntityStore(DbcObjects.Criteria);
    }

    default CriteriaEntity criteria(int id) {
        return criteria().get(id);
    }

    default DbcEntityStore<CriteriaTree> criteriaTree() {
        return getEntityStore(DbcObjects.CriteriaTree);
    }

    default CriteriaTree criteriaTree(int id) {
        return criteriaTree().get(id);
    }

    default DbcEntityStore<CurrencyType> currencyType() {
        return getEntityStore(DbcObjects.CurrencyType);
    }

    default CurrencyType currencyType(int id) {
        return currencyType().get(id);
    }

    default DbcEntityStore<Curve> curve() {
        return getEntityStore(DbcObjects.Curve);
    }

    default Curve curve(int id) {
        return curve().get(id);
    }

    default DbcEntityStore<CurvePoint> curvePoint() {
        return getEntityStore(DbcObjects.CurvePoint);
    }

    default CurvePoint curvePoint(int id) {
        return curvePoint().get(id);
    }

    default DbcEntityStore<DestructibleModelData> destructibleModelData() {
        return getEntityStore(DbcObjects.DestructibleModelData);
    }

    default DestructibleModelData destructibleModelData(int id) {
        return destructibleModelData().get(id);
    }

    default DbcEntityStore<DifficultyEntry> difficulty() {
        return getEntityStore(DbcObjects.Difficulty);
    }

    default DifficultyEntry difficulty(int id) {
        return difficulty().get(id);
    }

    default DbcEntityStore<DungeonEncounter> dungeonEncounter() {
        return getEntityStore(DbcObjects.DungeonEncounter);
    }

    default DungeonEncounter dungeonEncounter(int id) {
        return dungeonEncounter().get(id);
    }

    default DbcEntityStore<DurabilityCost> durabilityCost() {
        return getEntityStore(DbcObjects.DurabilityCost);
    }

    default DurabilityCost durabilityCost(int id) {
        return durabilityCost().get(id);
    }

    default DbcEntityStore<DurabilityQuality> durabilityQuality() {
        return getEntityStore(DbcObjects.DurabilityQuality);
    }

    default DurabilityQuality durabilityQuality(int id) {
        return durabilityQuality().get(id);
    }

    default DbcEntityStore<Emote> emote() {
        return getEntityStore(DbcObjects.Emote);
    }

    default Emote emote(int id) {
        return emote().get(id);
    }

    default DbcEntityStore<EmotesText> emotesText() {
        return getEntityStore(DbcObjects.EmotesText);
    }

    default EmotesText emotesText(int id) {
        return emotesText().get(id);
    }

    default DbcEntityStore<EmotesTextSound> emotesTextSound() {
        return getEntityStore(DbcObjects.EmotesTextSound);
    }

    default EmotesTextSound emotesTextSound(int id) {
        return emotesTextSound().get(id);
    }

    default DbcEntityStore<Faction> faction() {
        return getEntityStore(DbcObjects.Faction);
    }

    default Faction faction(int id) {
        return faction().get(id);
    }

    default DbcEntityStore<FactionTemplate> factionTemplate() {
        return getEntityStore(DbcObjects.FactionTemplate);
    }

    default FactionTemplate factionTemplate(int id) {
        return factionTemplate().get(id);
    }

    default DbcEntityStore<GameObjectArtKit> gameObjectArtKit() {
        return getEntityStore(DbcObjects.GameObjectArtKit);
    }

    default GameObjectArtKit gameObjectArtKit(int id) {
        return gameObjectArtKit().get(id);
    }

    default DbcEntityStore<FriendshipRepReaction> friendshipRepReaction() {
        return getEntityStore(DbcObjects.FriendshipRepReaction);
    }

    default FriendshipRepReaction friendshipRepReaction(int id) {
        return friendshipRepReaction().get(id);
    }

    default DbcEntityStore<FriendshipReputation> friendshipReputation() {
        return getEntityStore(DbcObjects.FriendshipReputation);
    }

    default FriendshipReputation friendshipReputation(int id) {
        return friendshipReputation().get(id);
    }

    default DbcEntityStore<GameObjectDisplayInfo> gameObjectDisplayInfo() {
        return getEntityStore(DbcObjects.GameObjectDisplayInfo);
    }

    default GameObjectDisplayInfo gameObjectDisplayInfo(int id) {
        return gameObjectDisplayInfo().get(id);
    }

    default DbcEntityStore<GameObjectEntry> gameObject() {
        return getEntityStore(DbcObjects.GameObject);
    }

    default GameObjectEntry gameObject(int id) {
        return gameObject().get(id);
    }

    default DbcEntityStore<GarrAbility> garrAbility() {
        return getEntityStore(DbcObjects.GarrAbility);
    }

    default GarrAbility garrAbility(int id) {
        return garrAbility().get(id);
    }

    default DbcEntityStore<GarrBuilding> garrBuilding() {
        return getEntityStore(DbcObjects.GarrBuilding);
    }

    default GarrBuilding garrBuilding(int id) {
        return garrBuilding().get(id);
    }

    default DbcEntityStore<GarrBuildingPlotInst> garrBuildingPlotInst() {
        return getEntityStore(DbcObjects.GarrBuildingPlotInst);
    }

    default GarrBuildingPlotInst garrBuildingPlotInst(int id) {
        return garrBuildingPlotInst().get(id);
    }

    default DbcEntityStore<GarrClassSpec> garrClassSpec() {
        return getEntityStore(DbcObjects.GarrClassSpec);
    }

    default GarrClassSpec garrClassSpec(int id) {
        return garrClassSpec().get(id);
    }

    default DbcEntityStore<GarrFollower> garrFollower() {
        return getEntityStore(DbcObjects.GarrFollower);
    }

    default GarrFollower garrFollower(int id) {
        return garrFollower().get(id);
    }

    default DbcEntityStore<GarrFollowerXAbility> garrFollowerXAbility() {
        return getEntityStore(DbcObjects.GarrFollowerXAbility);
    }

    default GarrFollowerXAbility garrFollowerXAbility(int id) {
        return garrFollowerXAbility().get(id);
    }

    default DbcEntityStore<GarrPlot> garrPlot() {
        return getEntityStore(DbcObjects.GarrPlot);
    }

    default GarrPlot garrPlot(int id) {
        return garrPlot().get(id);
    }

    default DbcEntityStore<GarrPlotBuilding> garrPlotBuilding() {
        return getEntityStore(DbcObjects.GarrPlotBuilding);
    }

    default GarrPlotBuilding garrPlotBuilding(int id) {
        return garrPlotBuilding().get(id);
    }

    default DbcEntityStore<GarrPlotInstance> garrPlotInstance() {
        return getEntityStore(DbcObjects.GarrPlotInstance);
    }

    default GarrPlotInstance garrPlotInstance(int id) {
        return garrPlotInstance().get(id);
    }

    default DbcEntityStore<GarrSiteLevel> garrSiteLevel() {
        return getEntityStore(DbcObjects.GarrSiteLevel);
    }

    default GarrSiteLevel garrSiteLevel(int id) {
        return garrSiteLevel().get(id);
    }

    default DbcEntityStore<GarrSiteLevelPlotInst> garrSiteLevelPlotInst() {
        return getEntityStore(DbcObjects.GarrSiteLevelPlotInst);
    }

    default GarrSiteLevelPlotInst garrSiteLevelPlotInst(int id) {
        return garrSiteLevelPlotInst().get(id);
    }

    default DbcEntityStore<GarrTalentTree> garrTalentTree() {
        return getEntityStore(DbcObjects.GarrTalentTree);
    }

    default GarrTalentTree garrTalentTree(int id) {
        return garrTalentTree().get(id);
    }

    default DbcEntityStore<GemProperty> gemProperty() {
        return getEntityStore(DbcObjects.GemProperty);
    }

    default GemProperty gemProperty(int id) {
        return gemProperty().get(id);
    }

    default DbcEntityStore<GlyphBindableSpell> glyphBindableSpell() {
        return getEntityStore(DbcObjects.GlyphBindableSpell);
    }

    default GlyphBindableSpell glyphBindableSpell(int id) {
        return glyphBindableSpell().get(id);
    }

    default DbcEntityStore<GlyphProperty> glyphProperty() {
        return getEntityStore(DbcObjects.GlyphProperty);
    }

    default GlyphProperty glyphProperty(int id) {
        return glyphProperty().get(id);
    }

    default DbcEntityStore<GlyphRequiredSpec> glyphRequiredSpec() {
        return getEntityStore(DbcObjects.GlyphRequiredSpec);
    }

    default GlyphRequiredSpec glyphRequiredSpec(int id) {
        return glyphRequiredSpec().get(id);
    }

    default DbcEntityStore<GuildColorBackground> guildColorBackground() {
        return getEntityStore(DbcObjects.GuildColorBackground);
    }

    default GuildColorBackground guildColorBackground(int id) {
        return guildColorBackground().get(id);
    }

    default DbcEntityStore<GuildColorBorder> guildColorBorder() {
        return getEntityStore(DbcObjects.GuildColorBorder);
    }

    default GuildColorBorder guildColorBorder(int id) {
        return guildColorBorder().get(id);
    }

    default DbcEntityStore<GuildColorEmblem> guildColorEmblem() {
        return getEntityStore(DbcObjects.GuildColorEmblem);
    }

    default GuildColorEmblem guildColorEmblem(int id) {
        return guildColorEmblem().get(id);
    }

    default DbcEntityStore<GuildPerkSpell> guildPerkSpell() {
        return getEntityStore(DbcObjects.GuildPerkSpell);
    }

    default GuildPerkSpell guildPerkSpell(int id) {
        return guildPerkSpell().get(id);
    }

    default DbcEntityStore<Heirloom> heirloom() {
        return getEntityStore(DbcObjects.Heirloom);
    }

    default Heirloom heirloom(int id) {
        return heirloom().get(id);
    }

    default DbcEntityStore<Holiday> holiday() {
        return getEntityStore(DbcObjects.Holiday);
    }

    default Holiday holiday(int id) {
        return holiday().get(id);
    }

    default DbcEntityStore<ImportPriceArmor> importPriceArmor() {
        return getEntityStore(DbcObjects.ImportPriceArmor);
    }

    default ImportPriceArmor importPriceArmor(int id) {
        return importPriceArmor().get(id);
    }

    default DbcEntityStore<ImportPriceQuality> importPriceQuality() {
        return getEntityStore(DbcObjects.ImportPriceQuality);
    }

    default ImportPriceQuality importPriceQuality(int id) {
        return importPriceQuality().get(id);
    }

    default DbcEntityStore<ImportPriceShield> importPriceShield() {
        return getEntityStore(DbcObjects.ImportPriceShield);
    }

    default ImportPriceShield importPriceShield(int id) {
        return importPriceShield().get(id);
    }

    default DbcEntityStore<ImportPriceWeapon> importPriceWeapon() {
        return getEntityStore(DbcObjects.ImportPriceWeapon);
    }

    default ImportPriceWeapon importPriceWeapon(int id) {
        return importPriceWeapon().get(id);
    }

    default DbcEntityStore<ItemAppearance> itemAppearance() {
        return getEntityStore(DbcObjects.ItemAppearance);
    }

    default ItemAppearance itemAppearance(int id) {
        return itemAppearance().get(id);
    }

    default DbcEntityStore<ItemArmorQuality> itemArmorQuality() {
        return getEntityStore(DbcObjects.ItemArmorQuality);
    }

    default ItemArmorQuality itemArmorQuality(int id) {
        return itemArmorQuality().get(id);
    }

    default DbcEntityStore<ItemArmorShield> itemArmorShield() {
        return getEntityStore(DbcObjects.ItemArmorShield);
    }

    default ItemArmorShield itemArmorShield(int id) {
        return itemArmorShield().get(id);
    }

    default DbcEntityStore<ItemArmorTotal> itemArmorTotal() {
        return getEntityStore(DbcObjects.ItemArmorTotal);
    }

    default ItemArmorTotal itemArmorTotal(int id) {
        return itemArmorTotal().get(id);
    }

    default DbcEntityStore<ItemBagFamily> itemBagFamily() {
        return getEntityStore(DbcObjects.ItemBagFamily);
    }

    default ItemBagFamily itemBagFamily(int id) {
        return itemBagFamily().get(id);
    }

    default DbcEntityStore<ItemBonus> itemBonus() {
        return getEntityStore(DbcObjects.ItemBonus);
    }

    default ItemBonus itemBonus(int id) {
        return itemBonus().get(id);
    }

    default DbcEntityStore<ItemBonusListLevelDelta> itemBonusListLevelDelta() {
        return getEntityStore(DbcObjects.ItemBonusListLevelDelta);
    }

    default ItemBonusListLevelDelta itemBonusListLevelDelta(int id) {
        return itemBonusListLevelDelta().get(id);
    }

    default DbcEntityStore<ItemBonusTreeNode> itemBonusTreeNode() {
        return getEntityStore(DbcObjects.ItemBonusTreeNode);
    }

    default ItemBonusTreeNode itemBonusTreeNode(int id) {
        return itemBonusTreeNode().get(id);
    }

    default DbcEntityStore<ItemChildEquipment> itemChildEquipment() {
        return getEntityStore(DbcObjects.ItemChildEquipment);
    }

    default ItemChildEquipment itemChildEquipment(int id) {
        return itemChildEquipment().get(id);
    }

    default DbcEntityStore<ItemClassEntry> itemClass() {
        return getEntityStore(DbcObjects.ItemClass);
    }

    default ItemClassEntry itemClass(int id) {
        return itemClass().get(id);
    }

    default DbcEntityStore<ItemContextPickerEntry> itemContextPickerEntry() {
        return getEntityStore(DbcObjects.ItemContextPickerEntry);
    }

    default ItemContextPickerEntry itemContextPickerEntry(int id) {
        return itemContextPickerEntry().get(id);
    }

    default DbcEntityStore<ItemCurrencyCost> itemCurrencyCost() {
        return getEntityStore(DbcObjects.ItemCurrencyCost);
    }

    default ItemCurrencyCost itemCurrencyCost(int id) {
        return itemCurrencyCost().get(id);
    }

    default DbcEntityStore<ItemDamageAmmo> itemDamageAmmo() {
        return getEntityStore(DbcObjects.ItemDamageAmmo);
    }

    default ItemDamageAmmo itemDamageAmmo(int id) {
        return itemDamageAmmo().get(id);
    }

    default DbcEntityStore<ItemDamageOneHand> itemDamageOneHand() {
        return getEntityStore(DbcObjects.ItemDamageOneHand);
    }

    default ItemDamageOneHand itemDamageOneHand(int id) {
        return itemDamageOneHand().get(id);
    }

    default DbcEntityStore<ItemDamageOneHandCaster> itemDamageOneHandCaster() {
        return getEntityStore(DbcObjects.ItemDamageOneHandCaster);
    }

    default ItemDamageOneHandCaster itemDamageOneHandCaster(int id) {
        return itemDamageOneHandCaster().get(id);
    }

    default DbcEntityStore<ItemDamageTwoHand> itemDamageTwoHand() {
        return getEntityStore(DbcObjects.ItemDamageTwoHand);
    }

    default ItemDamageTwoHand itemDamageTwoHand(int id) {
        return itemDamageTwoHand().get(id);
    }

    default DbcEntityStore<ItemDamageTwoHandCaster> itemDamageTwoHandCaster() {
        return getEntityStore(DbcObjects.ItemDamageTwoHandCaster);
    }

    default ItemDamageTwoHandCaster itemDamageTwoHandCaster(int id) {
        return itemDamageTwoHandCaster().get(id);
    }

    default DbcEntityStore<ItemDisenchantLoot> itemDisenchantLoot() {
        return getEntityStore(DbcObjects.ItemDisenchantLoot);
    }

    default ItemDisenchantLoot itemDisenchantLoot(int id) {
        return itemDisenchantLoot().get(id);
    }

    default DbcEntityStore<ItemEffect> itemEffect() {
        return getEntityStore(DbcObjects.ItemEffect);
    }

    default ItemEffect itemEffect(int id) {
        return itemEffect().get(id);
    }

    default DbcEntityStore<ItemEntry> item() {
        return getEntityStore(DbcObjects.Item);
    }

    default ItemEntry item(int id) {
        return item().get(id);
    }

    default DbcEntityStore<ItemExtendedCost> itemExtendedCost() {
        return getEntityStore(DbcObjects.ItemExtendedCost);
    }

    default ItemExtendedCost itemExtendedCost(int id) {
        return itemExtendedCost().get(id);
    }

    default DbcEntityStore<ItemLevelSelector> itemLevelSelector() {
        return getEntityStore(DbcObjects.ItemLevelSelector);
    }

    default ItemLevelSelector itemLevelSelector(int id) {
        return itemLevelSelector().get(id);
    }

    default DbcEntityStore<ItemLevelSelectorQuality> itemLevelSelectorQuality() {
        return getEntityStore(DbcObjects.ItemLevelSelectorQuality);
    }

    default ItemLevelSelectorQuality itemLevelSelectorQuality(int id) {
        return itemLevelSelectorQuality().get(id);
    }

    default DbcEntityStore<ItemLevelSelectorQualitySet> itemLevelSelectorQualitySet() {
        return getEntityStore(DbcObjects.ItemLevelSelectorQualitySet);
    }

    default ItemLevelSelectorQualitySet itemLevelSelectorQualitySet(int id) {
        return itemLevelSelectorQualitySet().get(id);
    }

    default DbcEntityStore<ItemLimitCategory> itemLimitCategory() {
        return getEntityStore(DbcObjects.ItemLimitCategory);
    }

    default ItemLimitCategory itemLimitCategory(int id) {
        return itemLimitCategory().get(id);
    }

    default DbcEntityStore<ItemLimitCategoryCondition> itemLimitCategoryCondition() {
        return getEntityStore(DbcObjects.ItemLimitCategoryCondition);
    }

    default ItemLimitCategoryCondition itemLimitCategoryCondition(int id) {
        return itemLimitCategoryCondition().get(id);
    }

    default DbcEntityStore<ItemModifiedAppearance> itemModifiedAppearance() {
        return getEntityStore(DbcObjects.ItemModifiedAppearance);
    }

    default ItemModifiedAppearance itemModifiedAppearance(int id) {
        return itemModifiedAppearance().get(id);
    }

    default DbcEntityStore<ItemModifiedAppearanceExtra> itemModifiedAppearanceExtra() {
        return getEntityStore(DbcObjects.ItemModifiedAppearanceExtra);
    }

    default ItemModifiedAppearanceExtra itemModifiedAppearanceExtra(int id) {
        return itemModifiedAppearanceExtra().get(id);
    }

    default DbcEntityStore<ItemNameDescription> itemNameDescription() {
        return getEntityStore(DbcObjects.ItemNameDescription);
    }

    default ItemNameDescription itemNameDescription(int id) {
        return itemNameDescription().get(id);
    }

    default DbcEntityStore<ItemPriceBase> itemPriceBase() {
        return getEntityStore(DbcObjects.ItemPriceBase);
    }

    default ItemPriceBase itemPriceBase(int id) {
        return itemPriceBase().get(id);
    }

    default DbcEntityStore<ItemRandomProperty> itemRandomProperty() {
        return getEntityStore(DbcObjects.ItemRandomPropertie);
    }

    default ItemRandomProperty itemRandomProperty(int id) {
        return itemRandomProperty().get(id);
    }

    default DbcEntityStore<ItemRandomSuffix> itemRandomSuffix() {
        return getEntityStore(DbcObjects.ItemRandomSuffix);
    }

    default ItemRandomSuffix itemRandomSuffix(int id) {
        return itemRandomSuffix().get(id);
    }

    default DbcEntityStore<ItemSearchName> itemSearchName() {
        return getEntityStore(DbcObjects.ItemSearchName);
    }

    default ItemSearchName itemSearchName(int id) {
        return itemSearchName().get(id);
    }

    default DbcEntityStore<ItemSet> itemSet() {
        return getEntityStore(DbcObjects.ItemSet);
    }

    default ItemSet itemSet(int id) {
        return itemSet().get(id);
    }

    default DbcEntityStore<ItemSetSpell> itemSetSpell() {
        return getEntityStore(DbcObjects.ItemSetSpell);
    }

    default ItemSetSpell itemSetSpell(int id) {
        return itemSetSpell().get(id);
    }

    default DbcEntityStore<ItemSparse> itemSparse() {
        return getEntityStore(DbcObjects.ItemSparse);
    }

    default ItemSparse itemSparse(int id) {
        return itemSparse().get(id);
    }

    default DbcEntityStore<ItemSpec> itemSpec() {
        return getEntityStore(DbcObjects.ItemSpec);
    }

    default ItemSpec itemSpec(int id) {
        return itemSpec().get(id);
    }

    default DbcEntityStore<ItemSpecOverride> itemSpecOverride() {
        return getEntityStore(DbcObjects.ItemSpecOverride);
    }

    default ItemSpecOverride itemSpecOverride(int id) {
        return itemSpecOverride().get(id);
    }

    default DbcEntityStore<ItemUpgrade> itemUpgrade() {
        return getEntityStore(DbcObjects.ItemUpgrade);
    }

    default ItemUpgrade itemUpgrade(int id) {
        return itemUpgrade().get(id);
    }

    default DbcEntityStore<ItemXBonusTree> itemXBonusTree() {
        return getEntityStore(DbcObjects.ItemXBonusTree);
    }

    default ItemXBonusTree itemXBonusTree(int id) {
        return itemXBonusTree().get(id);
    }

    default DbcEntityStore<JournalEncounter> journalEncounter() {
        return getEntityStore(DbcObjects.JournalEncounter);
    }

    default JournalEncounter journalEncounter(int id) {
        return journalEncounter().get(id);
    }

    default DbcEntityStore<JournalEncounterSection> journalEncounterSection() {
        return getEntityStore(DbcObjects.JournalEncounterSection);
    }

    default JournalEncounterSection journalEncounterSection(int id) {
        return journalEncounterSection().get(id);
    }

    default DbcEntityStore<JournalEncounterItem> journalEncounterItem() {
        return getEntityStore(DbcObjects.JournalEncounterItem);
    }

    default JournalEncounterItem journalEncounterItem(int id) {
        return journalEncounterItem().get(id);
    }

    default DbcEntityStore<JournalInstance> journalInstance() {
        return getEntityStore(DbcObjects.JournalInstance);
    }

    default JournalInstance journalInstance(int id) {
        return journalInstance().get(id);
    }

    default DbcEntityStore<JournalTier> journalTier() {
        return getEntityStore(DbcObjects.JournalTier);
    }

    default JournalTier journalTier(int id) {
        return journalTier().get(id);
    }

    default DbcEntityStore<Keychain> keychain() {
        return getEntityStore(DbcObjects.Keychain);
    }

    default Keychain keychain(int id) {
        return keychain().get(id);
    }

    default DbcEntityStore<KeystoneAffix> keystoneAffix() {
        return getEntityStore(DbcObjects.KeystoneAffix);
    }

    default KeystoneAffix keystoneAffix(int id) {
        return keystoneAffix().get(id);
    }

    default DbcEntityStore<LanguageWord> languageWord() {
        return getEntityStore(DbcObjects.LanguageWord);
    }

    default LanguageWord languageWord(int id) {
        return languageWord().get(id);
    }

    default DbcEntityStore<Language> language() {
        return getEntityStore(DbcObjects.Language);
    }

    default Language language(int id) {
        return language().get(id);
    }

    default DbcEntityStore<LfgDungeon> lfgDungeon() {
        return getEntityStore(DbcObjects.LfgDungeon);
    }

    default LfgDungeon lfgDungeon(int id) {
        return lfgDungeon().get(id);
    }

    default DbcEntityStore<Light> light() {
        return getEntityStore(DbcObjects.Light);
    }

    default Light light(int id) {
        return light().get(id);
    }

    default DbcEntityStore<LiquidType> liquidType() {
        return getEntityStore(DbcObjects.LiquidType);
    }

    default LiquidType liquidType(int id) {
        return liquidType().get(id);
    }

    default DbcEntityStore<Location> location() {
        return getEntityStore(DbcObjects.Location);
    }

    default Location location(int id) {
        return location().get(id);
    }

    default DbcEntityStore<Lock> lock() {
        return getEntityStore(DbcObjects.Lock);
    }

    default Lock lock(int id) {
        return lock().get(id);
    }

    default DbcEntityStore<MailTemplate> mailTemplate() {
        return getEntityStore(DbcObjects.MailTemplate);
    }

    default MailTemplate mailTemplate(int id) {
        return mailTemplate().get(id);
    }

    default DbcEntityStore<MapEntry> map() {
        return getEntityStore(DbcObjects.Map);
    }

    default MapEntry map(int id) {
        return map().get(id);
    }

    default DbcEntityStore<MapChallengeMode> mapChallengeMode() {
        return getEntityStore(DbcObjects.MapChallengeMode);
    }

    default MapChallengeMode mapChallengeMode(int id) {
        return mapChallengeMode().get(id);
    }

    default DbcEntityStore<MapDifficulty> mapDifficulty() {
        return getEntityStore(DbcObjects.MapDifficulty);
    }

    default MapDifficulty mapDifficulty(int id) {
        return mapDifficulty().get(id);
    }

    default DbcEntityStore<MapDifficultyXCondition> mapDifficultyXCondition() {
        return getEntityStore(DbcObjects.MapDifficultyXCondition);
    }

    default MapDifficultyXCondition mapDifficultyXCondition(int id) {
        return mapDifficultyXCondition().get(id);
    }

    default DbcEntityStore<ModifierTree> modifierTree() {
        return getEntityStore(DbcObjects.ModifierTree);
    }

    default ModifierTree modifierTree(int id) {
        return modifierTree().get(id);
    }

    default DbcEntityStore<MountCapability> mountCapability() {
        return getEntityStore(DbcObjects.MountCapability);
    }

    default MountCapability mountCapability(int id) {
        return mountCapability().get(id);
    }

    default DbcEntityStore<Mount> mount() {
        return getEntityStore(DbcObjects.Mount);
    }

    default Mount mount(int id) {
        return mount().get(id);
    }

    default DbcEntityStore<MountTypeXCapability> mountTypeXCapability() {
        return getEntityStore(DbcObjects.MountTypeXCapability);
    }

    default MountTypeXCapability mountTypeXCapability(int id) {
        return mountTypeXCapability().get(id);
    }

    default DbcEntityStore<MountXDisplay> mountXDisplay() {
        return getEntityStore(DbcObjects.MountXDisplay);
    }

    default MountXDisplay mountXDisplay(int id) {
        return mountXDisplay().get(id);
    }

    default DbcEntityStore<Movie> movie() {
        return getEntityStore(DbcObjects.Movie);
    }

    default Movie movie(int id) {
        return movie().get(id);
    }

    default DbcEntityStore<NameGen> nameGen() {
        return getEntityStore(DbcObjects.NameGen);
    }

    default NameGen nameGen(int id) {
        return nameGen().get(id);
    }

    default DbcEntityStore<NamesProfanity> namesProfanity() {
        return getEntityStore(DbcObjects.NamesProfanity);
    }

    default NamesProfanity namesProfanity(int id) {
        return namesProfanity().get(id);
    }

    default DbcEntityStore<NamesReserved> namesReserved() {
        return getEntityStore(DbcObjects.NamesReserved);
    }

    default NamesReserved namesReserved(int id) {
        return namesReserved().get(id);
    }

    default DbcEntityStore<NamesReservedLocale> namesReservedLocale() {
        return getEntityStore(DbcObjects.NamesReservedLocale);
    }

    default NamesReservedLocale namesReservedLocale(int id) {
        return namesReservedLocale().get(id);
    }

    default DbcEntityStore<OverrideSpellData> overrideSpellData() {
        return getEntityStore(DbcObjects.OverrideSpellData);
    }

    default OverrideSpellData overrideSpellData(int id) {
        return overrideSpellData().get(id);
    }

    default DbcEntityStore<ParagonReputation> paragonReputation() {
        return getEntityStore(DbcObjects.ParagonReputation);
    }

    default ParagonReputation paragonReputation(int id) {
        return paragonReputation().get(id);
    }

    default DbcEntityStore<Path> path() {
        return getEntityStore(DbcObjects.Path);
    }

    default Path path(int id) {
        return path().get(id);
    }

    default DbcEntityStore<PathNode> pathNode() {
        return getEntityStore(DbcObjects.PathNode);
    }

    default PathNode pathNode(int id) {
        return pathNode().get(id);
    }

    default DbcEntityStore<PathProperty> pathProperty() {
        return getEntityStore(DbcObjects.PathProperty);
    }

    default PathProperty pathProperty(int id) {
        return pathProperty().get(id);
    }

    default DbcEntityStore<Phase> phase() {
        return getEntityStore(DbcObjects.Phase);
    }

    default Phase phase(int id) {
        return phase().get(id);
    }

    default DbcEntityStore<PhaseXPhaseGroup> phaseXPhaseGroup() {
        return getEntityStore(DbcObjects.PhaseXPhaseGroup);
    }

    default PhaseXPhaseGroup phaseXPhaseGroup(int id) {
        return phaseXPhaseGroup().get(id);
    }

    default DbcEntityStore<PlayerCondition> playerCondition() {
        return getEntityStore(DbcObjects.PlayerCondition);
    }

    default PlayerCondition playerCondition(int id) {
        return playerCondition().get(id);
    }

    default DbcEntityStore<PowerDisplay> powerDisplay() {
        return getEntityStore(DbcObjects.PowerDisplay);
    }

    default PowerDisplay powerDisplay(int id) {
        return powerDisplay().get(id);
    }

    default DbcEntityStore<PowerType> powerType() {
        return getEntityStore(DbcObjects.PowerType);
    }

    default PowerType powerType(int id) {
        return powerType().get(id);
    }

    default DbcEntityStore<PrestigeLevelInfo> prestigeLevelInfo() {
        return getEntityStore(DbcObjects.PrestigeLevelInfo);
    }

    default PrestigeLevelInfo prestigeLevelInfo(int id) {
        return prestigeLevelInfo().get(id);
    }

    default DbcEntityStore<PvpDifficulty> pvpDifficulty() {
        return getEntityStore(DbcObjects.PvpDifficulty);
    }

    default PvpDifficulty pvpDifficulty(int id) {
        return pvpDifficulty().get(id);
    }

    default DbcEntityStore<PvpItem> pvpItem() {
        return getEntityStore(DbcObjects.PvpItem);
    }

    default PvpItem pvpItem(int id) {
        return pvpItem().get(id);
    }

    default DbcEntityStore<PvpReward> pvpReward() {
        return getEntityStore(DbcObjects.PvpReward);
    }

    default PvpReward pvpReward(int id) {
        return pvpReward().get(id);
    }

    default DbcEntityStore<PvpTalent> pvpTalent() {
        return getEntityStore(DbcObjects.PvpTalent);
    }

    default PvpTalent pvpTalent(int id) {
        return pvpTalent().get(id);
    }

    default DbcEntityStore<PvpTalentUnlock> pvpTalentUnlock() {
        return getEntityStore(DbcObjects.PvpTalentUnlock);
    }

    default PvpTalentUnlock pvpTalentUnlock(int id) {
        return pvpTalentUnlock().get(id);
    }

    default DbcEntityStore<QuestFactionReward> questFactionReward() {
        return getEntityStore(DbcObjects.QuestFactionReward);
    }

    default QuestFactionReward questFactionReward(int id) {
        return questFactionReward().get(id);
    }

    default DbcEntityStore<QuestLineXQuest> questLineXQuest() {
        return getEntityStore(DbcObjects.QuestLineXQuest);
    }

    default QuestLineXQuest questLineXQuest(int id) {
        return questLineXQuest().get(id);
    }

    default DbcEntityStore<QuestInfo> questInfo() {
        return getEntityStore(DbcObjects.QuestInfo);
    }

    default QuestInfo questInfo(int id) {
        return questInfo().get(id);
    }

    default DbcEntityStore<QuestMoneyReward> questMoneyReward() {
        return getEntityStore(DbcObjects.QuestMoneyReward);
    }

    default QuestMoneyReward questMoneyReward(int id) {
        return questMoneyReward().get(id);
    }

    default DbcEntityStore<QuestPackageItem> questPackageItem() {
        return getEntityStore(DbcObjects.QuestPackageItem);
    }

    default QuestPackageItem questPackageItem(int id) {
        return questPackageItem().get(id);
    }

    default DbcEntityStore<QuestSort> questSort() {
        return getEntityStore(DbcObjects.QuestSort);
    }

    default QuestSort questSort(int id) {
        return questSort().get(id);
    }

    default DbcEntityStore<QuestV2> questV2() {
        return getEntityStore(DbcObjects.QuestV2);
    }

    default QuestV2 questV2(int id) {
        return questV2().get(id);
    }

    default DbcEntityStore<QuestXp> questXp() {
        return getEntityStore(DbcObjects.QuestXp);
    }

    default QuestXp questXp(int id) {
        return questXp().get(id);
    }

    default DbcEntityStore<RandPropPoint> randPropPoint() {
        return getEntityStore(DbcObjects.RandPropPoint);
    }

    default RandPropPoint randPropPoint(int id) {
        return randPropPoint().get(id);
    }

    default DbcEntityStore<RewardPack> rewardPack() {
        return getEntityStore(DbcObjects.RewardPack);
    }

    default RewardPack rewardPack(int id) {
        return rewardPack().get(id);
    }

    default DbcEntityStore<RewardPackXCurrencyType> rewardPackXCurrencyType() {
        return getEntityStore(DbcObjects.RewardPackXCurrencyType);
    }

    default RewardPackXCurrencyType rewardPackXCurrencyType(int id) {
        return rewardPackXCurrencyType().get(id);
    }

    default DbcEntityStore<RewardPackXItem> rewardPackXItem() {
        return getEntityStore(DbcObjects.RewardPackXItem);
    }

    default RewardPackXItem rewardPackXItem(int id) {
        return rewardPackXItem().get(id);
    }

    default DbcEntityStore<RuleSetItemUpgrade> ruleSetItemUpgrade() {
        return getEntityStore(DbcObjects.RulesetItemUpgrade);
    }

    default RuleSetItemUpgrade ruleSetItemUpgrade(int id) {
        return ruleSetItemUpgrade().get(id);
    }

    default DbcEntityStore<SandboxScaling> sandboxScaling() {
        return getEntityStore(DbcObjects.SandboxScaling);
    }

    default SandboxScaling sandboxScaling(int id) {
        return sandboxScaling().get(id);
    }

    default DbcEntityStore<ScalingStatDistribution> scalingStatDistribution() {
        return getEntityStore(DbcObjects.ScalingStatDistribution);
    }

    default ScalingStatDistribution scalingStatDistribution(int id) {
        return scalingStatDistribution().get(id);
    }

    default DbcEntityStore<Scenario> scenario() {
        return getEntityStore(DbcObjects.Scenario);
    }

    default Scenario scenario(int id) {
        return scenario().get(id);
    }

    default DbcEntityStore<ScenarioStep> scenarioStep() {
        return getEntityStore(DbcObjects.ScenarioStep);
    }

    default ScenarioStep scenarioStep(int id) {
        return scenarioStep().get(id);
    }

    default DbcEntityStore<SceneScript> sceneScript() {
        return getEntityStore(DbcObjects.SceneScript);
    }

    default SceneScript sceneScript(int id) {
        return sceneScript().get(id);
    }

    default DbcEntityStore<SceneScriptGlobalText> sceneScriptGlobalText() {
        return getEntityStore(DbcObjects.SceneScriptGlobalText);
    }

    default SceneScriptGlobalText sceneScriptGlobalText(int id) {
        return sceneScriptGlobalText().get(id);
    }

    default DbcEntityStore<SceneScriptPackage> sceneScriptPackage() {
        return getEntityStore(DbcObjects.SceneScriptPackage);
    }

    default SceneScriptPackage sceneScriptPackage(int id) {
        return sceneScriptPackage().get(id);
    }

    default DbcEntityStore<SceneScriptText> sceneScriptText() {
        return getEntityStore(DbcObjects.SceneScriptText);
    }

    default SceneScriptText sceneScriptText(int id) {
        return sceneScriptText().get(id);
    }

    default DbcEntityStore<ServerMessage> serverMessage() {
        return getEntityStore(DbcObjects.ServerMessages);
    }

    default ServerMessage serverMessage(int id) {
        return serverMessage().get(id);
    }

    default DbcEntityStore<SkillLine> skillLine() {
        return getEntityStore(DbcObjects.SkillLine);
    }

    default SkillLine skillLine(int id) {
        return skillLine().get(id);
    }

    default DbcEntityStore<SkillLineAbility> skillLineAbility() {
        return getEntityStore(DbcObjects.SkillLineAbility);
    }

    default SkillLineAbility skillLineAbility(int id) {
        return skillLineAbility().get(id);
    }

    default DbcEntityStore<SkillRaceClassInfo> skillRaceClassInfo() {
        return getEntityStore(DbcObjects.SkillRaceClassInfo);
    }

    default SkillRaceClassInfo skillRaceClassInfo(int id) {
        return skillRaceClassInfo().get(id);
    }

    default DbcEntityStore<SoundKit> soundKit() {
        return getEntityStore(DbcObjects.SoundKit);
    }

    default SoundKit soundKit(int id) {
        return soundKit().get(id);
    }

    default DbcEntityStore<SpecializationSpell> specializationSpell() {
        return getEntityStore(DbcObjects.SpecializationSpell);
    }

    default SpecializationSpell specializationSpell(int id) {
        return specializationSpell().get(id);
    }

    default DbcEntityStore<SpellEntity> spell() {
        return getEntityStore(DbcObjects.Spell);
    }

    default SpellEntity spell(int id) {
        return spell().get(id);
    }

    default DbcEntityStore<SpellAuraOption> spellAuraOption() {
        return getEntityStore(DbcObjects.SpellAuraOption);
    }

    default SpellAuraOption spellAuraOption(int id) {
        return spellAuraOption().get(id);
    }

    default DbcEntityStore<SpellAuraRestriction> spellAuraRestriction() {
        return getEntityStore(DbcObjects.SpellAuraRestriction);
    }

    default SpellAuraRestriction spellAuraRestriction(int id) {
        return spellAuraRestriction().get(id);
    }

    default DbcEntityStore<SpellCastTime> spellCastTime() {
        return getEntityStore(DbcObjects.SpellCastTime);
    }

    default SpellCastTime spellCastTime(int id) {
        return spellCastTime().get(id);
    }

    default DbcEntityStore<SpellCastingRequirement> spellCastingRequirement() {
        return getEntityStore(DbcObjects.SpellCastingRequirement);
    }

    default SpellCastingRequirement spellCastingRequirement(int id) {
        return spellCastingRequirement().get(id);
    }

    default DbcEntityStore<SpellCategories> spellCategories() {
        return getEntityStore(DbcObjects.SpellCategories);
    }

    default SpellCategories spellCategories(int id) {
        return spellCategories().get(id);
    }

    default DbcEntityStore<SpellCategory> spellCategory() {
        return getEntityStore(DbcObjects.SpellCategory);
    }

    default SpellCategory spellCategory(int id) {
        return spellCategory().get(id);
    }

    default DbcEntityStore<SpellClassOption> spellClassOption() {
        return getEntityStore(DbcObjects.SpellClassOption);
    }

    default SpellClassOption spellClassOption(int id) {
        return spellClassOption().get(id);
    }

    default DbcEntityStore<SpellCooldown> spellCooldown() {
        return getEntityStore(DbcObjects.SpellCooldown);
    }

    default SpellCooldown spellCooldown(int id) {
        return spellCooldown().get(id);
    }

    default DbcEntityStore<SpellDuration> spellDuration() {
        return getEntityStore(DbcObjects.SpellDuration);
    }

    default SpellDuration spellDuration(int id) {
        return spellDuration().get(id);
    }

    default DbcEntityStore<SpellEffect> spellEffect() {
        return getEntityStore(DbcObjects.SpellEffect);
    }

    default SpellEffect spellEffect(int id) {
        return spellEffect().get(id);
    }

    default DbcEntityStore<SpellEquippedItem> spellEquippedItem() {
        return getEntityStore(DbcObjects.SpellEquippedItem);
    }

    default SpellEquippedItem spellEquippedItem(int id) {
        return spellEquippedItem().get(id);
    }

    default DbcEntityStore<SpellFocusObject> spellFocusObject() {
        return getEntityStore(DbcObjects.SpellFocusObject);
    }

    default SpellFocusObject spellFocusObject(int id) {
        return spellFocusObject().get(id);
    }

    default DbcEntityStore<SpellInterrupt> spellInterrupt() {
        return getEntityStore(DbcObjects.SpellInterrupt);
    }

    default SpellInterrupt spellInterrupt(int id) {
        return spellInterrupt().get(id);
    }

    default DbcEntityStore<SpellItemEnchantment> spellItemEnchantment() {
        return getEntityStore(DbcObjects.SpellItemEnchantment);
    }

    default SpellItemEnchantment spellItemEnchantment(int id) {
        return spellItemEnchantment().get(id);
    }

    default DbcEntityStore<SpellItemEnchantmentCondition> spellItemEnchantmentCondition() {
        return getEntityStore(DbcObjects.SpellItemEnchantmentCondition);
    }

    default SpellItemEnchantmentCondition spellItemEnchantmentCondition(int id) {
        return spellItemEnchantmentCondition().get(id);
    }

    default DbcEntityStore<SpellKeyboundOverride> spellKeyboundOverride() {
        return getEntityStore(DbcObjects.SpellKeyboundOverride);
    }

    default SpellKeyboundOverride spellKeyboundOverride(int id) {
        return spellKeyboundOverride().get(id);
    }

    default DbcEntityStore<SpellLabel> spellLabel() {
        return getEntityStore(DbcObjects.SpellLabel);
    }

    default SpellLabel spellLabel(int id) {
        return spellLabel().get(id);
    }

    default DbcEntityStore<SpellLearnSpell> spellLearnSpell() {
        return getEntityStore(DbcObjects.SpellLearnSpell);
    }

    default SpellLearnSpell spellLearnSpell(int id) {
        return spellLearnSpell().get(id);
    }

    default DbcEntityStore<SpellLevel> spellLevel() {
        return getEntityStore(DbcObjects.SpellLevel);
    }

    default SpellLevel spellLevel(int id) {
        return spellLevel().get(id);
    }

    default DbcEntityStore<SpellMisc> spellMisc() {
        return getEntityStore(DbcObjects.SpellMisc);
    }

    default SpellMisc spellMisc(int id) {
        return spellMisc().get(id);
    }

    default DbcEntityStore<SpellPower> spellPower() {
        return getEntityStore(DbcObjects.SpellPower);
    }

    default SpellPower spellPower(int id) {
        return spellPower().get(id);
    }

    default DbcEntityStore<SpellPowerDifficulty> spellPowerDifficulty() {
        return getEntityStore(DbcObjects.SpellPowerDifficulty);
    }

    default SpellPowerDifficulty spellPowerDifficulty(int id) {
        return spellPowerDifficulty().get(id);
    }

    default DbcEntityStore<SpellProcsPerMinute> spellProcsPerMinute() {
        return getEntityStore(DbcObjects.SpellProcsPerMinute);
    }

    default SpellProcsPerMinute spellProcsPerMinute(int id) {
        return spellProcsPerMinute().get(id);
    }

    default DbcEntityStore<SpellProcsPerMinuteMod> spellProcsPerMinuteMod() {
        return getEntityStore(DbcObjects.SpellProcsPerMinuteMod);
    }

    default SpellProcsPerMinuteMod spellProcsPerMinuteMod(int id) {
        return spellProcsPerMinuteMod().get(id);
    }

    default DbcEntityStore<SpellRadius> spellRadius() {
        return getEntityStore(DbcObjects.SpellRadiu);
    }

    default SpellRadius spellRadius(int id) {
        return spellRadius().get(id);
    }

    default DbcEntityStore<SpellRange> spellRange() {
        return getEntityStore(DbcObjects.SpellRange);
    }

    default SpellRange spellRange(int id) {
        return spellRange().get(id);
    }

    default DbcEntityStore<SpellReagent> spellReagent() {
        return getEntityStore(DbcObjects.SpellReagent);
    }

    default SpellReagent spellReagent(int id) {
        return spellReagent().get(id);
    }

    default DbcEntityStore<SpellScaling> spellScaling() {
        return getEntityStore(DbcObjects.SpellScaling);
    }

    default SpellScaling spellScaling(int id) {
        return spellScaling().get(id);
    }

    default DbcEntityStore<SpellShapeshift> spellShapeshift() {
        return getEntityStore(DbcObjects.SpellShapeshift);
    }

    default SpellShapeshift spellShapeshift(int id) {
        return spellShapeshift().get(id);
    }

    default DbcEntityStore<SpellShapeshiftForm> spellShapeshiftForm() {
        return getEntityStore(DbcObjects.SpellShapeshiftForm);
    }

    default SpellShapeshiftForm spellShapeshiftForm(int id) {
        return spellShapeshiftForm().get(id);
    }

    default DbcEntityStore<SpellTargetRestriction> spellTargetRestriction() {
        return getEntityStore(DbcObjects.SpellTargetRestriction);
    }

    default SpellTargetRestriction spellTargetRestriction(int id) {
        return spellTargetRestriction().get(id);
    }

    default DbcEntityStore<SpellTotem> spellTotem() {
        return getEntityStore(DbcObjects.SpellTotem);
    }

    default SpellTotem spellTotem(int id) {
        return spellTotem().get(id);
    }

    default DbcEntityStore<SpellVisual> spellVisual() {
        return getEntityStore(DbcObjects.SpellVisual);
    }

    default SpellVisual spellVisual(int id) {
        return spellVisual().get(id);
    }

    default DbcEntityStore<SpellVisualEffectName> spellVisualEffectName() {
        return getEntityStore(DbcObjects.SpellVisualEffectName);
    }

    default SpellVisualEffectName spellVisualEffectName(int id) {
        return spellVisualEffectName().get(id);
    }

    default DbcEntityStore<SpellVisualMissile> spellVisualMissile() {
        return getEntityStore(DbcObjects.SpellVisualMissile);
    }

    default SpellVisualMissile spellVisualMissile(int id) {
        return spellVisualMissile().get(id);
    }

    default DbcEntityStore<SpellVisualKit> spellVisualKit() {
        return getEntityStore(DbcObjects.SpellVisualKit);
    }

    default SpellVisualKit spellVisualKit(int id) {
        return spellVisualKit().get(id);
    }

    default DbcEntityStore<SpellXSpellVisual> spellXSpellVisual() {
        return getEntityStore(DbcObjects.SpellXSpellVisual);
    }

    default SpellXSpellVisual spellXSpellVisual(int id) {
        return spellXSpellVisual().get(id);
    }

    default DbcEntityStore<SummonProperty> summonProperty() {
        return getEntityStore(DbcObjects.SummonProperty);
    }

    default SummonProperty summonProperty(int id) {
        return summonProperty().get(id);
    }

    default DbcEntityStore<TactKey> tactKey() {
        return getEntityStore(DbcObjects.TactKey);
    }

    default TactKey tactKey(int id) {
        return tactKey().get(id);
    }

    default DbcEntityStore<Talent> talent() {
        return getEntityStore(DbcObjects.Talent);
    }

    default Talent talent(int id) {
        return talent().get(id);
    }

    default DbcEntityStore<TaxiNode> taxiNode() {
        return getEntityStore(DbcObjects.TaxiNode);
    }

    default TaxiNode taxiNode(int id) {
        return taxiNode().get(id);
    }

    default DbcEntityStore<TaxiPath> taxiPath() {
        return getEntityStore(DbcObjects.TaxiPath);
    }

    default TaxiPath taxiPath(int id) {
        return taxiPath().get(id);
    }

    default DbcEntityStore<TaxiPathNode> taxiPathNode() {
        return getEntityStore(DbcObjects.TaxiPathNode);
    }

    default TaxiPathNode taxiPathNode(int id) {
        return taxiPathNode().get(id);
    }

    default DbcEntityStore<TotemCategory> totemCategory() {
        return getEntityStore(DbcObjects.TotemCategory);
    }

    default TotemCategory totemCategory(int id) {
        return totemCategory().get(id);
    }

    default DbcEntityStore<Toy> toy() {
        return getEntityStore(DbcObjects.Toy);
    }

    default Toy toy(int id) {
        return toy().get(id);
    }

    default DbcEntityStore<TransmogHoliday> transmogHoliday() {
        return getEntityStore(DbcObjects.TransmogHoliday);
    }

    default TransmogHoliday transmogHoliday(int id) {
        return transmogHoliday().get(id);
    }

    default DbcEntityStore<TransmogSet> transmogSet() {
        return getEntityStore(DbcObjects.TransmogSet);
    }

    default TransmogSet transmogSet(int id) {
        return transmogSet().get(id);
    }

    default DbcEntityStore<TransmogSetGroup> transmogSetGroup() {
        return getEntityStore(DbcObjects.TransmogSetGroup);
    }

    default TransmogSetGroup transmogSetGroup(int id) {
        return transmogSetGroup().get(id);
    }

    default DbcEntityStore<TransmogSetItem> transmogSetItem() {
        return getEntityStore(DbcObjects.TransmogSetItem);
    }

    default TransmogSetItem transmogSetItem(int id) {
        return transmogSetItem().get(id);
    }

    default DbcEntityStore<TransportAnimation> transportAnimation() {
        return getEntityStore(DbcObjects.TransportAnimation);
    }

    default TransportAnimation transportAnimation(int id) {
        return transportAnimation().get(id);
    }

    default DbcEntityStore<TransportRotation> transportRotation() {
        return getEntityStore(DbcObjects.TransportRotation);
    }

    default TransportRotation transportRotation(int id) {
        return transportRotation().get(id);
    }

    default DbcEntityStore<UnitCondition> unitCondition() {
        return getEntityStore(DbcObjects.UnitCondition);
    }

    default UnitCondition unitCondition(int id) {
        return unitCondition().get(id);
    }

    default DbcEntityStore<UnitPowerBar> unitPowerBar() {
        return getEntityStore(DbcObjects.UnitPowerBar);
    }

    default UnitPowerBar unitPowerBar(int id) {
        return unitPowerBar().get(id);
    }

    default DbcEntityStore<VehicleEntry> vehicle() {
        return getEntityStore(DbcObjects.Vehicle);
    }

    default VehicleEntry vehicle(int id) {
        return vehicle().get(id);
    }

    default DbcEntityStore<VehicleSeat> vehicleSeat() {
        return getEntityStore(DbcObjects.VehicleSeat);
    }

    default VehicleSeat vehicleSeat(int id) {
        return vehicleSeat().get(id);
    }

    default DbcEntityStore<Vignette> vignette() {
        return getEntityStore(DbcObjects.Vignette);
    }

    default Vignette vignette(int id) {
        return vignette().get(id);
    }

    default DbcEntityStore<WmoAreaTable> wmoAreaTable() {
        return getEntityStore(DbcObjects.WmoAreaTable);
    }

    default WmoAreaTable wmoAreaTable(int id) {
        return wmoAreaTable().get(id);
    }

    default DbcEntityStore<WorldEffect> worldEffect() {
        return getEntityStore(DbcObjects.WorldEffect);
    }

    default WorldEffect worldEffect(int id) {
        return worldEffect().get(id);
    }

    default DbcEntityStore<WorldMapArea> worldMapArea() {
        return getEntityStore(DbcObjects.WorldMapArea);
    }

    default WorldMapArea worldMapArea(int id) {
        return worldMapArea().get(id);
    }

    default DbcEntityStore<WorldMapOverlay> worldMapOverlay() {
        return getEntityStore(DbcObjects.WorldMapOverlay);
    }

    default WorldMapOverlay worldMapOverlay(int id) {
        return worldMapOverlay().get(id);
    }

    default DbcEntityStore<WorldMapTransform> worldMapTransform() {
        return getEntityStore(DbcObjects.WorldMapTransform);
    }

    default WorldMapTransform worldMapTransform(int id) {
        return worldMapTransform().get(id);
    }

    default DbcEntityStore<WorldSafeLoc> worldSafeLoc() {
        return getEntityStore(DbcObjects.WorldSafeLoc);
    }

    default WorldSafeLoc worldSafeLoc(int id) {
        return worldSafeLoc().get(id);
    }

    default DbcEntityStore<WorldStateExpression> worldStateExpression() {
        return getEntityStore(DbcObjects.WorldStateExpression);
    }

    default WorldStateExpression worldStateExpression(int id) {
        return worldStateExpression().get(id);
    }

    <T extends DbcEntity> DbcEntityStore<T> getEntityStore(DbcObjects object);


    int getEmptyAnimStateID();

    List<Short> getAreasForGroup(Short areaGroupId);

    boolean isInArea(Short objectAreaId, Short areaId);

    List<ArtifactPower> getArtifactPowers(Short artifactId);

    Set<Short> getArtifactPowerLinks(Short artifactPowerId);

    ArtifactPowerRank getArtifactPowerRank(Short artifactPowerId, Byte rank);

    String getBroadcastTextValue(BroadcastText broadcastText);

    String getBroadcastTextValue(BroadcastText broadcastText, Locale locale, Gender gender, boolean forceGender);

    Integer getBroadcastTextDuration(Integer broadcastTextId);

    Integer getBroadcastTextDuration(Integer broadcastTextId, Locale locale);

    boolean hasCharacterFacialHairStyle(Byte race, Byte gender, Byte variationId);

    boolean hasCharSections(Race race, Gender gender, CharBaseSectionVariation variation);

    CharSection getCharSection(Byte race, Byte gender, CharBaseSectionVariation variation, Byte variationIndex, Byte colorIndex);

    CharStartOutfit getCharStartOutfit(byte race, byte klass, byte gender);


    String getClassName(int klass, Locale locale);

    ChrSpecialization getChrSpecializationByIndex(UnitClass klass, int index);

    ChrSpecialization getDefaultChrSpecializationForClass(UnitClass klass);

    Integer getPowerIndexByClass(Power power, UnitClass classId);


    String getChrRaceName(Race race, Locale locale);

    Integer getRedirectedContentTuningId(Integer contentTuningId, Integer redirectFlag);

    boolean hasContentTuningLabel(Integer contentTuningId, Integer label);

    String getCreatureFamilyPetName(CreatureFamily petFamily, Locale locale);

    Pair<Float, Float> getCurveXAxisRange(int curveId);

    float getCurveValueAt(Integer curveId, float x);

    float getCurveValueAt(CurveInterpolationMode mode, List<DBCPosition2D> points, float x);

    EmotesTextSound getTextSoundEmoteFor(Integer emote, Byte race, Byte gender, Byte class_);

    float evaluateExpectedStat(ExpectedStatType stat, Integer level, Integer expansion, Integer contentTuningId, UnitClass unitClass, Integer mythicPlusMilestoneSeason);

    List<Integer> getFactionTeamList(Integer faction);

    Set<FriendshipRepReaction> getFriendshipRepReactions(Integer friendshipRepID);

    Integer getGlobalCurveId(GlobalCurve globalCurveType);

    List<Integer> getGlyphBindableSpells(Integer glyphPropertiesId);

    Heirloom getHeirloomByItemId(Integer itemId);

    ItemChildEquipment getItemChildEquipment(Integer itemId);

    ItemClassEntry getItemClassByOldEnum(Integer itemClass);

    boolean hasItemCurrencyCost(Integer itemId);

    List<ItemLimitCategoryCondition> getItemLimitCategoryConditions(Integer categoryId);

    Integer getItemDisplayId(Integer itemId, Integer appearanceModId);

    ItemModifiedAppearance getItemModifiedAppearance(int itemId, short appearanceModId);

    ItemModifiedAppearance getDefaultItemModifiedAppearance(int itemId);

    List<ItemSetSpell> getItemSetSpells(Integer itemSetId);

    JournalTier getJournalTier(Integer index);

    List<ItemSpecOverride> getItemSpecOverrides(Integer itemId);

    JournalInstance getJournalInstanceByMapId(Integer mapId);

    List<JournalEncounterItem> getJournalItemsByEncounter(Integer encounterId);

    List<JournalEncounter> getJournalEncounterByJournalInstanceId(Integer instanceId);

    LfgDungeon getLfgDungeon(Integer mapId, Difficulty Difficulty);

    Integer getDefaultMapLight(Integer mapId);

    Integer getLiquidFlags(Integer liquidType);

    MapDifficulty getDefaultMapDifficulty(Integer mapId);

    MapDifficulty getDefaultMapDifficulty(Integer mapId, Difficulty difficulty);

    MapDifficulty getMapDifficultyData(Integer mapId, Difficulty difficulty);

    MapDifficulty getDownscaledMapDifficultyData(int mapId, Difficulty difficulty);

    String getNameGenEntry(Byte race, Byte gender);

    Map<Integer, PlayerCondition> getMapDifficultyConditions(Integer mapDifficultyId);

    Mount getMount(Integer spellId);

    Mount getMountById(int id);

    Set<MountTypeXCapability> getMountCapabilities(Integer mountType);

    List<MountXDisplay> getMountDisplays(Integer mountId);

    String getNameGen(Byte race, Byte gender);

    ResponseCodes validateName(String name);

    ResponseCodes validateName(String name, Locale locale);

    Integer getNumTalentsAtLevel(Integer level, UnitClass klass);

    ParagonReputation getParagonReputation(Integer factionId);

    PathDb2 getPath(Integer pathId);

    List<Integer> getPhasesForGroup(Integer group);

    PowerType getPowerType(Power power);

    PowerType getPowerTypeByName(String name);

    Byte getPvpItemLevelBonus(Integer itemId);

    Byte getMaxPrestige();

    PvpDifficulty getBattlegroundBracketByLevel(Integer mapId, Integer level);

    PvpDifficulty getBattlegroundBracketById(Integer mapId, BattlegroundBracketId id);

    Integer getRewardPackIDForPvpRewardByHonorLevelAndPrestige(byte honorLevel, byte prestige);

    int getRequiredHonorLevelForPvpTalent(PvpTalent talentInfo);

    List<PvpTalent> getPvpTalentsByPosition(UnitClass class_, int tier, int column);


    List<QuestLineXQuest> getQuestsForQuestLine(Integer questLineId);

    List<QuestPackageItem> getQuestPackageItems(Integer questPackageID);

    List<QuestPackageItem> getQuestPackageItemsFallback(Integer questPackageID);

    Integer getQuestUniqueBitFlag(Integer questId);

    List<RewardPackXCurrencyType> getRewardPackCurrencyTypesByRewardID(Integer rewardPackID);

    List<RewardPackXItem> getRewardPackItemsByRewardID(Integer rewardPackID);

    Integer getRulesetItemUpgrade(Integer itemId);

    SkillRaceClassInfo getSkillRaceClassInfo(Integer skill, Byte race, UnitClass class_);


    List<SkillLine> getSkillLinesForParentSkill(Integer parentSkillId);

    List<SkillLineAbility> getSkillLineAbilitiesBySkill(Integer skillId);

    SkillRaceClassInfo getSkillRaceClassInfo(Integer skill, Byte race, Byte class_);

    List<SkillRaceClassInfo> getSkillRaceClassInfo(Integer skill);

    boolean isValidSpellFamilyName(SpellFamilyName family);

    List<SpellProcsPerMinuteMod> getSpellProcPerMinuteMods(Integer spellProcPerMinuteId);

    List<SpellVisualMissile> getSpellVisualMissiles(Integer spellVisualMissileSetId);

    TaxiPath getTaxiPath(Integer from, Integer to);

    boolean isTotemCategoryCompatibleWith(Integer itemTotemCategoryId, Integer requiredTotemCategoryId, boolean requireAllTote);

    boolean IsToyItem(Integer toy);

    List<TransmogSet> getTransmogSetsForItemModifiedAppearance(Integer itemModifiedAppearanceId);

    List<TransmogSetItem> getTransmogSetItems(Integer transmogSetId);

    boolean getUiMapPosition(float x, float y, float z, Integer mapId, Integer areaId, Integer wmoDoodadPlacementId, Integer wmoGroupId, UiMapSystem system, boolean local);

    DBCPosition2D zone2MapCoordinates(Integer areaId);

    DBCPosition2D map2ZoneCoordinates(Integer areaId);

    boolean isUiMapPhase(Integer phaseId);

    WmoAreaTable getWMOAreaTable(Integer rootId, Integer adtId, Integer groupId);

    Set<Integer> getPVPStatIDsForMap(Integer mapId);

    List<ItemEffect> getItemEffectsForItemId(Integer itemId);

    WorldSafeLoc getWorldSafeLoc(int id);

    void determineAlternateMapPosition(Integer mapId, float x, float y, float z);

    Set<Integer> getDefaultItemBonusTree(Integer itemId, ItemContext itemContext);


    List<ItemBonus> getItemBonusList(int bonusList);


    Map<Integer, List<TaxiPathNode>> getTaxiPathNodesByPath();

    TaxiMask getTaxiNodesMask();
}
