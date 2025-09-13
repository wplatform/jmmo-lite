package com.github.azeroth.game.entity.item;


import com.github.azeroth.dbc.defines.ItemContext;
import com.github.azeroth.game.entity.ArtifactPower;
import com.github.azeroth.game.entity.SocketedGem;
import com.github.azeroth.game.entity.UpdateMask;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.object.update.ItemData;
import com.github.azeroth.game.entity.object.update.UpdateData;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.listener.interfaces.iitem.IItemOnExpire;
import com.github.azeroth.game.spell.SpellInfo;
import game.ConditionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.locale;


public class Item extends WorldObject {
    public static int[] ITEMTRANSMOGRIFICATIONSLOTS = {-1, EquipmentSlot.Head, -1, EquipmentSlot.Shoulders, EquipmentSlot.Shirt, EquipmentSlot.chest, EquipmentSlot.Waist, EquipmentSlot.Legs, EquipmentSlot.Feet, EquipmentSlot.Wrist, EquipmentSlot.Hands, -1, -1, -1, EquipmentSlot.OffHand, EquipmentSlot.MainHand, EquipmentSlot.Cloak, EquipmentSlot.MainHand, -1, EquipmentSlot.Tabard, EquipmentSlot.chest, EquipmentSlot.MainHand, EquipmentSlot.MainHand, EquipmentSlot.OffHand, -1, -1, EquipmentSlot.MainHand, -1, -1, -1, -1, -1, -1, -1, -1};

    private final HashMap<Integer, SHORT> artifactPowerIdToIndex = new HashMap<Integer, SHORT>();
    private final Array<Integer> gemScalingLevels = new Array<Integer>(ItemConst.MaxGemSockets);

    private ItemUpdateState updateState = ItemUpdateState.values()[0];
    private int paidExtendedCost;
    private long paidMoney;
    private ObjectGuid refundRecipient = ObjectGuid.EMPTY;
    private byte slot;
    private Bag container;
    private int queuePos;
    private String text;
    private boolean mbInTrade;
    private long lastPlayedTimeUpdate;
    private ArrayList<ObjectGuid> allowedGuiDs = new ArrayList<>();
    private int randomBonusListId; // store separately to easily find which bonus list is the one randomly given for stat rerolling
    private ObjectGuid childItem = ObjectGuid.EMPTY;

    private ItemData itemData;
    private boolean lootGenerated;
    private Loot loot;
    private com.github.azeroth.game.entity.item.BonusData bonusData;

    public item() {
        super(false);
        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.item.getValue()));
        setObjectTypeId(TypeId.item);

        setItemData(new itemData());

        updateState = ItemUpdateState.New;
        queuePos = -1;
        lastPlayedTimeUpdate = gameTime.GetGameTime();
    }

    public static void deleteFromDB(SQLTransaction trans, long itemGuid) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_GEMS);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_TRANSMOG);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_ARTIFACT);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_ARTIFACT_POWERS);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_MODIFIERS);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GIFT);
        stmt.AddValue(0, itemGuid);
        DB.characters.ExecuteOrAppend(trans, stmt);
    }

    public static void deleteFromInventoryDB(SQLTransaction trans, long itemGuid) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_INVENTORY_BY_ITEM);
        stmt.AddValue(0, itemGuid);
        trans.append(stmt);
    }

    public static void removeItemFromUpdateQueueOf(Item item, Player player) {
        if (!item.isInUpdateQueue()) {
            return;
        }

        if (ObjectGuid.opNotEquals(player.getGUID(), item.getOwnerGUID())) {
            Log.outError(LogFilter.player, "Item.RemoveFromUpdateQueueOf - Owner's guid ({0}) and player's guid ({1}) don't match!", item.getOwnerGUID().toString(), player.getGUID().toString());

            return;
        }

        if (player.getItemUpdateQueueBlocked()) {
            return;
        }

        player.getItemUpdateQueue().set(item.queuePos, null);
        item.queuePos = -1;
    }

    public static Item createItem(int item, int count, ItemContext context) {
        return createItem(item, count, context, null);
    }

    public static Item createItem(int item, int count, ItemContext context, Player player) {
        if (count < 1) {
            return null; //don't create item at zero count
        }

        var pProto = global.getObjectMgr().getItemTemplate(item);

        if (pProto != null) {
            if (count > pProto.getMaxStackSize()) {
                count = pProto.getMaxStackSize();
            }

            var pItem = newItemOrBag(pProto);

            if (pItem.create(global.getObjectMgr().getGenerator(HighGuid.Item).generate(), item, context, player)) {
                pItem.setCount(count);

                return pItem;
            }
        }

        return null;
    }

    public static boolean canTransmogrifyItemWithItem(Item item, ItemModifiedAppearanceRecord itemModifiedAppearance) {
        var source = global.getObjectMgr().getItemTemplate(itemModifiedAppearance.itemID); // source
        var target = item.getTemplate(); // dest

        if (source == null || target == null) {
            return false;
        }

        if (itemModifiedAppearance == item.getItemModifiedAppearance()) {
            return false;
        }

        if (!item.isValidTransmogrificationTarget()) {
            return false;
        }

        if (source.getClass() != target.getClass()) {
            return false;
        }

        if (source.getInventoryType() == inventoryType.bag || source.getInventoryType() == inventoryType.Relic || source.getInventoryType() == inventoryType.Finger || source.getInventoryType() == inventoryType.Trinket || source.getInventoryType() == inventoryType.ammo || source.getInventoryType() == inventoryType.Quiver) {
            return false;
        }

        if (source.getSubClass() != target.getSubClass()) {
            switch (source.getClass()) {
                case Weapon:
                    if (getTransmogrificationWeaponCategory(source) != getTransmogrificationWeaponCategory(target)) {
                        return false;
                    }

                    break;
                case Armor:
                    if (source.getSubClass() != ItemSubClassArmor.COSMETIC.getValue()) {
                        return false;
                    }

                    if (source.getInventoryType() != target.getInventoryType()) {
                        if (ItemTransmogrificationSlots[source.getInventoryType().getValue()] != ItemTransmogrificationSlots[target.getInventoryType().getValue()]) {
                            return false;
                        }
                    }

                    break;
                default:
                    return false;
            }
        }

        return true;
    }

    public static int getSellPrice(ItemTemplate proto, int quality, int itemLevel) {
        if (proto.hasFlag(ItemFlags2.OverrideGoldCost)) {
            return proto.getSellPrice();
        }

        boolean standardPrice;
        tangible.OutObject<Boolean> tempOut_standardPrice = new tangible.OutObject<Boolean>();
        var cost = getBuyPrice(proto, quality, itemLevel, tempOut_standardPrice);
        standardPrice = tempOut_standardPrice.outArgValue;

        if (standardPrice) {
            var classEntry = global.getDB2Mgr().GetItemClassByOldEnum(proto.getClass());

            if (classEntry != null) {
                var buyCount = Math.max(proto.getBuyCount(), 1);

                return (int) (cost * classEntry.PriceModifier / buyCount);
            }

            return 0;
        } else {
            return proto.getSellPrice();
        }
    }

    public static int getItemLevel(ItemTemplate itemTemplate, BonusData bonusData, int level, int fixedLevel, int minItemLevel, int minItemLevelCutoff, int maxItemLevel, boolean pvpBonus, int azeriteLevel) {
        if (itemTemplate == null) {
            return 1;
        }

        var itemLevel = itemTemplate.getBaseItemLevel();
        var azeriteLevelInfo = CliDB.AzeriteLevelInfoStorage.get(azeriteLevel);

        if (azeriteLevelInfo != null) {
            itemLevel = azeriteLevelInfo.itemLevel;
        }

        if (bonusData.playerLevelToItemLevelCurveId != 0) {
            if (fixedLevel != 0) {
                level = fixedLevel;
            } else {
                var levels = global.getDB2Mgr().GetContentTuningData(bonusData.contentTuningId, 0, true);

                if (levels != null) {
                    level = (int) Math.min(Math.max((short) level, levels.getValue().minLevel), levels.getValue().maxLevel);
                }
            }

            itemLevel = (int) global.getDB2Mgr().GetCurveValueAt(bonusData.playerLevelToItemLevelCurveId, level);
        }

        itemLevel += (int) bonusData.itemLevelBonus;

        for (int i = 0; i < ItemConst.MaxGemSockets; ++i) {
            itemLevel += bonusData.GemItemLevelBonus[i];
        }

        var itemLevelBeforeUpgrades = itemLevel;

        if (pvpBonus) {
            itemLevel += global.getDB2Mgr().GetPvpItemLevelBonus(itemTemplate.getId());
        }

        if (itemTemplate.getInventoryType() != inventoryType.NonEquip) {
            if (minItemLevel != 0 && (minItemLevelCutoff == 0 || itemLevelBeforeUpgrades >= minItemLevelCutoff) && itemLevel < minItemLevel) {
                itemLevel = minItemLevel;
            }

            if (maxItemLevel != 0 && itemLevel > maxItemLevel) {
                itemLevel = maxItemLevel;
            }
        }

        return Math.min(Math.max(itemLevel, 1), 1300);
    }

    public static ItemDisenchantLootRecord getDisenchantLoot(ItemTemplate itemTemplate, int quality, int itemLevel) {
        if (itemTemplate.hasFlag(ItemFlags.Conjured) || itemTemplate.hasFlag(ItemFlags.NoDisenchant) || itemTemplate.getBonding() == ItemBondingType.Quest) {
            return null;
        }

        if (itemTemplate.getArea(0) != 0 || itemTemplate.getArea(1) != 0 || itemTemplate.getMap() != 0 || itemTemplate.getMaxStackSize() > 1) {
            return null;
        }

        if (getSellPrice(itemTemplate, quality, itemLevel) == 0 && !global.getDB2Mgr().HasItemCurrencyCost(itemTemplate.getId())) {
            return null;
        }

        var itemClass = (byte) itemTemplate.getClass().getValue();
        var itemSubClass = itemTemplate.getSubClass();
        var expansion = itemTemplate.getRequiredExpansion();

        for (var disenchant : CliDB.ItemDisenchantLootStorage.values()) {
            if (disenchant.class != itemClass) {
                continue;
            }

            if (disenchant.subclass >= 0 && itemSubClass != 0) {
                continue;
            }

            if (disenchant.quality != quality) {
                continue;
            }

            if (disenchant.minLevel > itemLevel || disenchant.maxLevel < itemLevel) {
                continue;
            }

            if (disenchant.ExpansionID != -2 && disenchant.ExpansionID != expansion) {
                continue;
            }

            return disenchant;
        }

        return null;
    }

    public static Item newItemOrBag(ItemTemplate proto) {
        if (proto.getInventoryType() == inventoryType.bag) {
            return new bag();
        }

        if (global.getDB2Mgr().IsAzeriteItem(proto.getId())) {
            return new azeriteItem();
        }

        if (global.getDB2Mgr().GetAzeriteEmpoweredItem(proto.getId()) != null) {
            return new azeriteEmpoweredItem();
        }

        return new item();
    }

    public static void addItemsSetItem(Player player, Item item) {
        var proto = item.getTemplate();
        var setid = proto.getItemSet();

        var set = CliDB.ItemSetStorage.get(setid);

        if (set == null) {
            Logs.SQL.error("Item set {0} for item (id {1}) not found, mods not applied.", setid, proto.getId());

            return;
        }

        if (set.RequiredSkill != 0 && player.getSkillValue(SkillType.forValue(set.RequiredSkill)).getValue() < set.RequiredSkillRank) {
            return;
        }

        if (set.SetFlags.hasFlag(ItemSetFlags.LegacyInactive)) {
            return;
        }

        // Check player level for heirlooms
        if (global.getDB2Mgr().GetHeirloomByItemId(item.getEntry()) != null) {
            if (item.getBonusData().playerLevelToItemLevelCurveId != 0) {
                var maxLevel = (int) global.getDB2Mgr().GetCurveXAxisRange(item.getBonusData().playerLevelToItemLevelCurveId).item2;

                var contentTuning = global.getDB2Mgr().GetContentTuningData(item.getBonusData().contentTuningId, player.getPlayerData().ctrOptions.getValue().contentTuningConditionMask, true);

                if (contentTuning != null) {
                    maxLevel = Math.min(maxLevel, (int) contentTuning.getValue().maxLevel);
                }

                if (player.getLevel() > maxLevel) {
                    return;
                }
            }
        }

        ItemSetEffect eff = null;

        for (var x = 0; x < player.getItemSetEff().size(); ++x) {
            if ((player.getItemSetEff().get(x) == null ? null : player.getItemSetEff().get(x).getItemSetId()) == setid) {
                eff = player.getItemSetEff().get(x);

                break;
            }
        }

        if (eff == null) {
            eff = new ItemSetEffect();
            eff.setItemSetId(setid);

            var x = 0;

            for (; x < player.getItemSetEff().size(); ++x) {
                if (player.getItemSetEff().get(x) == null) {
                    break;
                }
            }

            if (x < player.getItemSetEff().size()) {
                player.getItemSetEff().set(x, eff);
            } else {
                player.getItemSetEff().add(eff);
            }
        }

        eff.getEquippedItems().add(item);

        var itemSetSpells = global.getDB2Mgr().GetItemSetSpells(setid);

        for (var itemSetSpell : itemSetSpells) {
            //not enough for  spell
            if (itemSetSpell.threshold > eff.getEquippedItems().size()) {
                continue;
            }

            if (eff.getSetBonuses().contains(itemSetSpell)) {
                continue;
            }

            var spellInfo = global.getSpellMgr().getSpellInfo(itemSetSpell.spellID, Difficulty.NONE);

            if (spellInfo == null) {
                Log.outError(LogFilter.player, "WORLD: unknown spell id {0} in items set {1} effects", itemSetSpell.spellID, setid);

                continue;
            }

            eff.getSetBonuses().add(itemSetSpell);

            // spell cast only if fit form requirement, in other case will cast at form change
            if (itemSetSpell.ChrSpecID == 0 || itemSetSpell.ChrSpecID == player.getPrimarySpecialization()) {
                player.applyEquipSpell(spellInfo, null, true);
            }
        }
    }

    public static void removeItemsSetItem(Player player, Item item) {
        var setid = item.getTemplate().getItemSet();

        var set = CliDB.ItemSetStorage.get(setid);

        if (set == null) {
            Logs.SQL.error(String.format("Item set %1$s for item %2$s not found, mods not removed.", setid, item.getEntry()));

            return;
        }

        ItemSetEffect eff = null;
        var setindex = 0;

        for (; setindex < player.getItemSetEff().size(); setindex++) {
            if (player.getItemSetEff().get(setindex) != null && player.getItemSetEff().get(setindex).getItemSetId() == setid) {
                eff = player.getItemSetEff().get(setindex);

                break;
            }
        }

        // can be in case now enough skill requirement for set appling but set has been appliend when skill requirement not enough
        if (eff == null) {
            return;
        }

        eff.getEquippedItems().remove(item);

        var itemSetSpells = global.getDB2Mgr().GetItemSetSpells(setid);

        for (var itemSetSpell : itemSetSpells) {
            // enough for spell
            if (itemSetSpell.threshold <= eff.getEquippedItems().size()) {
                continue;
            }

            if (!eff.getSetBonuses().contains(itemSetSpell)) {
                continue;
            }

            player.applyEquipSpell(global.getSpellMgr().getSpellInfo(itemSetSpell.spellID, Difficulty.NONE), null, false);
            eff.getSetBonuses().remove(itemSetSpell);
        }

        if (eff.getEquippedItems().isEmpty()) //all items of a set were removed
        {
            player.getItemSetEff().set(setindex, null);
        }
    }

    //Static
    public static boolean itemCanGoIntoBag(ItemTemplate pProto, ItemTemplate pBagProto) {
        if (pProto == null || pBagProto == null) {
            return false;
        }

        switch (pBagProto.getClass()) {
            case Container:
                switch (ItemSubClassContainer.forValue(pBagProto.getSubClass())) {
                    case Container:
                        return true;
                    case SoulContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.SoulShards.getValue())) {
                            return false;
                        }

                        return true;
                    case HerbContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.Herbs.getValue())) {
                            return false;
                        }

                        return true;
                    case EnchantingContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.EnchantingSupp.getValue())) {
                            return false;
                        }

                        return true;
                    case MiningContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.MiningSupp.getValue())) {
                            return false;
                        }

                        return true;
                    case EngineeringContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.EngineeringSupp.getValue())) {
                            return false;
                        }

                        return true;
                    case GemContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.gems.getValue())) {
                            return false;
                        }

                        return true;
                    case LeatherworkingContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.LeatherworkingSupp.getValue())) {
                            return false;
                        }

                        return true;
                    case InscriptionContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.InscriptionSupp.getValue())) {
                            return false;
                        }

                        return true;
                    case TackleContainer:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.FishingSupp.getValue())) {
                            return false;
                        }

                        return true;
                    case CookingContainer:
                        if (!pProto.getBagFamily().hasFlag(BagFamilyMask.CookingSupp)) {
                            return false;
                        }

                        return true;
                    case ReagentContainer:
                        return pProto.isCraftingReagent();
                    default:
                        return false;
                }
                //can remove?
            case Quiver:
                switch (ItemSubClassQuiver.forValue(pBagProto.getSubClass())) {
                    case Quiver:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.Arrows.getValue())) {
                            return false;
                        }

                        return true;
                    case AmmoPouch:
                        if (!(boolean) (pProto.getBagFamily().getValue() & BagFamilyMask.Bullets.getValue())) {
                            return false;
                        }

                        return true;
                    default:
                        return false;
                }
        }

        return false;
    }

    public static int itemSubClassToDurabilityMultiplierId(ItemClass itemClass, int itemSubClass) {
        switch (itemClass) {
            case Weapon:
                return itemSubClass;
            case Armor:
                return itemSubClass + 21;
        }

        return 0;
    }

    private static void addItemToUpdateQueueOf(Item item, Player player) {
        if (item.isInUpdateQueue()) {
            return;
        }

        if (ObjectGuid.opNotEquals(player.getGUID(), item.getOwnerGUID())) {
            Log.outError(LogFilter.player, "Item.AddToUpdateQueueOf - Owner's guid ({0}) and player's guid ({1}) don't match!", item.getOwnerGUID(), player.getGUID().toString());

            return;
        }

        if (player.getItemUpdateQueueBlocked()) {
            return;
        }

        player.getItemUpdateQueue().add(item);
        item.queuePos = player.getItemUpdateQueue().size() - 1;
    }

    private static boolean hasStats(ItemInstance itemInstance, BonusData bonus) {
        for (byte i = 0; i < ItemConst.MaxStats; ++i) {
            if (bonus.StatPercentEditor[i] != 0) {
                return true;
            }
        }

        return false;
    }

    private static ItemTransmogrificationWeaponCategory getTransmogrificationWeaponCategory(ItemTemplate proto) {
        if (proto.getClass() == itemClass.Weapon) {
            switch (ItemSubClassWeapon.forValue(proto.getSubClass())) {
                case Axe2:
                case Mace2:
                case Sword2:
                case Staff:
                case Polearm:
                    return ItemTransmogrificationWeaponCategory.Melee2H;
                case Bow:
                case Gun:
                case Crossbow:
                    return ItemTransmogrificationWeaponCategory.Ranged;
                case Axe:
                case Mace:
                case Sword:
                case Warglaives:
                    return ItemTransmogrificationWeaponCategory.AxeMaceSword1H;
                case Dagger:
                    return ItemTransmogrificationWeaponCategory.Dagger;
                case Fist:
                    return ItemTransmogrificationWeaponCategory.Fist;
                default:
                    break;
            }
        }

        return ItemTransmogrificationWeaponCategory.Invalid;
    }

    private static int getBuyPrice(ItemTemplate proto, int quality, int itemLevel, tangible.OutObject<Boolean> standardPrice) {
        standardPrice.outArgValue = true;

        if (proto.hasFlag(ItemFlags2.OverrideGoldCost)) {
            return proto.getBuyPrice();
        }

        var qualityPrice = CliDB.ImportPriceQualityStorage.get(quality + 1);

        if (qualityPrice == null) {
            return 0;
        }

        var basePrice = CliDB.ItemPriceBaseStorage.get(proto.getBaseItemLevel());

        if (basePrice == null) {
            return 0;
        }

        var qualityFactor = qualityPrice.data;
        float baseFactor;

        var inventoryType = proto.getInventoryType();

        if (inventoryType == inventoryType.Weapon || inventoryType == inventoryType.Weapon2Hand || inventoryType == inventoryType.WeaponMainhand || inventoryType == inventoryType.WeaponOffhand || inventoryType == inventoryType.Ranged || inventoryType == inventoryType.Thrown || inventoryType == inventoryType.RangedRight) {
            baseFactor = basePrice.Weapon;
        } else {
            baseFactor = basePrice.armor;
        }

        if (inventoryType == inventoryType.Robe) {
            inventoryType = inventoryType.chest;
        }

        if (proto.getClass() == itemClass.Gem && proto.getSubClass() == ItemSubClassGem.ArtifactRelic.getValue()) {
            inventoryType = inventoryType.Weapon;
            baseFactor = basePrice.Weapon / 3.0f;
        }


        var typeFactor = 0.0f;
        byte weapType = -1;

        switch (inventoryType) {
            case Head:
            case Neck:
            case Shoulders:
            case Chest:
            case Waist:
            case Legs:
            case Feet:
            case Wrists:
            case Hands:
            case Finger:
            case Trinket:
            case Cloak:
            case Holdable: {
                var armorPrice = CliDB.ImportPriceArmorStorage.get(inventoryType);

                if (armorPrice == null) {
                    return 0;
                }

                switch (ItemSubClassArmor.forValue(proto.getSubClass())) {
                    case Miscellaneous:
                    case Cloth:
                        typeFactor = armorPrice.ClothModifier;

                        break;
                    case Leather:
                        typeFactor = armorPrice.LeatherModifier;

                        break;
                    case Mail:
                        typeFactor = armorPrice.ChainModifier;

                        break;
                    case Plate:
                        typeFactor = armorPrice.PlateModifier;

                        break;
                    default:
                        typeFactor = 1.0f;

                        break;
                }

                break;
            }
            case Shield: {
                var shieldPrice = CliDB.ImportPriceShieldStorage.get(2); // it only has two rows, it's unclear which is the one used

                if (shieldPrice == null) {
                    return 0;
                }

                typeFactor = shieldPrice.data;

                break;
            }
            case WeaponMainhand:
                weapType = 0;

                break;
            case WeaponOffhand:
                weapType = 1;

                break;
            case Weapon:
                weapType = 2;

                break;
            case Weapon2Hand:
                weapType = 3;

                break;
            case Ranged:
            case RangedRight:
            case Relic:
                weapType = 4;

                break;
            default:
                return proto.getBuyPrice();
        }

        if (weapType != -1) {
            var weaponPrice = CliDB.ImportPriceWeaponStorage.get(weapType + 1);

            if (weaponPrice == null) {
                return 0;
            }

            typeFactor = weaponPrice.data;
        }

        standardPrice.outArgValue = false;

        return (int) (proto.getPriceVariance() * typeFactor * baseFactor * qualityFactor * proto.getPriceRandomValue());
    }

    public final ItemData getItemData() {
        return itemData;
    }

    public final void setItemData(ItemData value) {
        itemData = value;
    }

    public final boolean getLootGenerated() {
        return lootGenerated;
    }

    public final void setLootGenerated(boolean value) {
        lootGenerated = value;
    }

    public final Loot getLoot() {
        return loot;
    }

    public final void setLoot(Loot value) {
        loot = value;
    }

    public final BonusData getBonusData() {
        return bonusData;
    }

    public final void setBonusData(BonusData value) {
        bonusData = value;
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return getItemData().owner;
    }

    public final void setOwnerGUID(ObjectGuid guid) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().owner), guid);
    }

    @Override
    public Player getOwnerUnit() {
        return global.getObjAccessor().findPlayer(getOwnerGUID());
    }

    public final ItemTemplate getTemplate() {
        return global.getObjectMgr().getItemTemplate(getEntry());
    }

    public final byte getBagSlot() {
        return container != null ? container.getSlot() : InventorySlots.Bag0;
    }

    public final boolean isEquipped() {
        return !isInBag() && (slot < EquipmentSlot.End || (slot >= ProfessionSlots.start && slot < ProfessionSlots.End));
    }

    public final SkillType getSkill() {
        var proto = getTemplate();

        return proto.getSkill();
    }

    public final int getPlayedTime() {
        var curtime = gameTime.GetGameTime();
        var elapsed = (int) (curtime - lastPlayedTimeUpdate);

        return getItemData().createPlayedTime + elapsed;
    }

    public final boolean isRefundExpired() {
        return (getPlayedTime() > 2 * time.Hour);
    }

    public final boolean isNotEmptyBag() {
        var bag = getAsBag();

        if (bag != null) {
            return !bag.isEmpty();
        }

        return false;
    }

    public final ObjectGuid getContainedIn() {
        return getItemData().containedIn;
    }

    public final void setContainedIn(ObjectGuid guid) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().containedIn), guid);
    }

    public final ObjectGuid getCreator() {
        return getItemData().creator;
    }

    public final void setCreator(ObjectGuid guid) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().creator), guid);
    }

    public final ObjectGuid getGiftCreator() {
        return getItemData().giftCreator;
    }

    public final void setGiftCreator(ObjectGuid guid) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().giftCreator), guid);
    }

    public final ItemBondingType getBonding() {
        return getBonusData().bonding;
    }

    public final boolean isSoulBound() {
        return hasItemFlag(ItemFieldFlags.Soulbound);
    }

    public final boolean isBoundAccountWide() {
        return getTemplate().hasFlag(ItemFlags.IsBoundToAccount);
    }

    public final boolean isBattlenetAccountBound() {
        return getTemplate().hasFlag(ItemFlags2.BnetAccountTradeOk);
    }

    public final Bag getAsBag() {
        return this instanceof Bag ? (bag) this : null;
    }

    public final AzeriteItem getAsAzeriteItem() {
        return this instanceof AzeriteItem ? (azeriteItem) this : null;
    }

    public final AzeriteEmpoweredItem getAsAzeriteEmpoweredItem() {
        return this instanceof AzeriteEmpoweredItem ? (azeriteEmpoweredItem) this : null;
    }

    public final boolean isRefundable() {
        return hasItemFlag(ItemFieldFlags.refundable);
    }

    public final boolean isBOPTradeable() {
        return hasItemFlag(ItemFieldFlags.BopTradeable);
    }

    public final boolean isWrapped() {
        return hasItemFlag(ItemFieldFlags.Wrapped);
    }

    public final boolean isLocked() {
        return !hasItemFlag(ItemFieldFlags.unlocked);
    }

    public final boolean isBag() {
        return getTemplate().getInventoryType() == inventoryType.bag;
    }

    public final boolean isAzeriteItem() {
        return getObjectTypeId() == TypeId.azeriteItem;
    }

    public final boolean isAzeriteEmpoweredItem() {
        return getObjectTypeId() == TypeId.azeriteEmpoweredItem;
    }

    public final boolean isCurrencyToken() {
        return getTemplate().isCurrencyToken();
    }

    public final boolean isBroken() {
        return getItemData().maxDurability > 0 && getItemData().durability == 0;
    }

    public final boolean isInTrade() {
        return mbInTrade;
    }

    public final void setInTrade(boolean b) {
        mbInTrade = b;
    }

    public final int getCount() {
        return getItemData().stackCount;
    }

    public final void setCount(int value) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().stackCount), value);

        var player = getOwnerUnit();

        if (player) {
            var tradeData = player.getTradeData();

            if (tradeData != null) {
                var slot = tradeData.getTradeSlotForItem(getGUID());

                if (slot != TradeSlots.Invalid) {
                    tradeData.setItem(slot, this, true);
                }
            }
        }
    }

    public final int getMaxStackCount() {
        return getTemplate().getMaxStackSize();
    }

    public final byte getSlot() {
        return slot;
    }

    public final void setSlot(byte slot) {
        slot = slot;
    }

    public final Bag getContainer() {
        return container;
    }

    public final void setContainer(Bag container) {
        container = container;
    }

    public final short getPos() {
        return (short) (getBagSlot() << 8 | getSlot());
    }

    public final int getItemRandomBonusListId() {
        return randomBonusListId;
    }

    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        text = text;
    }

    public final ItemUpdateState getState() {
        return updateState;
    }

    public final void setState(ItemUpdateState state) {
        setState(state, null);
    }

    public final boolean isInUpdateQueue() {
        return queuePos != -1;
    }

    public final int getQueuePos() {
        return queuePos;
    }

    public final boolean isPotion() {
        return getTemplate().isPotion();
    }

    public final boolean isVellum() {
        return getTemplate().isVellum();
    }

    public final boolean isConjuredConsumable() {
        return getTemplate().isConjuredConsumable();
    }

    public final boolean isRangedWeapon() {
        return getTemplate().isRangedWeapon();
    }

    public final ItemQuality getQuality() {
        return getBonusData().quality;
    }

    public final int getAppearanceModId() {
        return getItemData().itemAppearanceModID;
    }

    public final void setAppearanceModId(int appearanceModId) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemAppearanceModID), (byte) appearanceModId);
    }

    public final float getRepairCostMultiplier() {
        return getBonusData().repairCostMultiplier;
    }

    public final int getScalingContentTuningId() {
        return getBonusData().contentTuningId;
    }

    public final ObjectGuid getRefundRecipient() {
        return refundRecipient;
    }

    public final void setRefundRecipient(ObjectGuid guid) {
        refundRecipient = guid;
    }

    public final long getPaidMoney() {
        return paidMoney;
    }

    public final void setPaidMoney(long money) {
        paidMoney = money;
    }

    public final int getPaidExtendedCost() {
        return paidExtendedCost;
    }

    public final void setPaidExtendedCost(int iece) {
        paidExtendedCost = iece;
    }

    public final int getScriptId() {
        return getTemplate().getScriptId();
    }

    public final ObjectGuid getChildItem() {
        return childItem;
    }

    public final void setChildItem(ObjectGuid childItem) {
        childItem = childItem;
    }

    public final ItemEffectRecord[] getEffects() {
        return Arrays.copyOfRange(getBonusData().effects, 0, bonusData.effectCount);
    }

    private boolean isInBag() {
        return container != null;
    }

    public boolean create(long guidlow, int itemId, ItemContext context, Player owner) {
        create(ObjectGuid.create(HighGuid.Item, guidlow));

        setEntry(itemId);
        setObjectScale(1.0f);

        if (owner) {
            setOwnerGUID(owner.getGUID());
            setContainedIn(owner.getGUID());
        }

        var itemProto = global.getObjectMgr().getItemTemplate(itemId);

        if (itemProto == null) {
            return false;
        }

        setBonusData(new bonusData(itemProto));
        setCount(1);
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().maxDurability), itemProto.getMaxDurability());
        setDurability(itemProto.getMaxDurability());

        for (var i = 0; i < itemProto.getEffects().size(); ++i) {
            if (itemProto.getEffects().get(i).LegacySlotIndex < 5) {
                setSpellCharges(itemProto.getEffects().get(i).LegacySlotIndex, itemProto.getEffects().get(i).charges);
            }
        }

        setExpiration(itemProto.getDuration());
        setCreatePlayedTime(0);
        setContext(context);

        if (itemProto.getArtifactID() != 0) {
            initArtifactPowers(itemProto.getArtifactID(), (byte) 0);

            for (var artifactAppearance : CliDB.ArtifactAppearanceStorage.values()) {
                var artifactAppearanceSet = CliDB.ArtifactAppearanceSetStorage.get(artifactAppearance.ArtifactAppearanceSetID);

                if (artifactAppearanceSet != null) {
                    if (itemProto.getArtifactID() != artifactAppearanceSet.ArtifactID) {
                        continue;
                    }

                    var playerCondition = CliDB.PlayerConditionStorage.get(artifactAppearance.UnlockPlayerConditionID);

                    if (playerCondition != null) {
                        if (!owner || !ConditionManager.isPlayerMeetingCondition(owner, playerCondition)) {
                            continue;
                        }
                    }

                    setModifier(ItemModifier.artifactAppearanceId, artifactAppearance.id);
                    setAppearanceModId(artifactAppearance.ItemAppearanceModifierID);

                    break;
                }
            }

            checkArtifactRelicSlotUnlock(owner != null ? owner : getOwnerUnit());
        }

        return true;
    }

    @Override
    public String getName() {
        return getName(locale.enUS);
    }

    @Override
    public String getName(Locale locale) {
        var itemTemplate = getTemplate();
        var suffix = CliDB.ItemNameDescriptionStorage.get(getBonusData().suffix);

        if (suffix != null) {
            return String.format("%1$s %2$s", itemTemplate.getName(locale), suffix.Description[locale]);
        }

        return itemTemplate.getName(locale);
    }

    public final void updateDuration(Player owner, int diff) {
        int duration = getItemData().expiration;

        if (duration == 0) {
            return;
        }

        Log.outDebug(LogFilter.player, "Item.UpdateDuration item (Entry: {0} Duration {1} Diff {2})", getEntry(), duration, diff);

        if (duration <= diff) {
            var itemTemplate = getTemplate();
            global.getScriptMgr().<IItemOnExpire>RunScriptRet(p -> p.OnExpire(owner, itemTemplate), itemTemplate.getScriptId());

            owner.destroyItem(getBagSlot(), getSlot(), true);

            return;
        }

        setExpiration(duration - diff);
        setState(ItemUpdateState.changed, owner); // save new time in database
    }

    public void saveToDB(SQLTransaction trans) {
        PreparedStatement stmt;

        switch (updateState) {
            case New:
            case Changed: {
                byte index = 0;
                stmt = DB.characters.GetPreparedStatement(updateState == ItemUpdateState.New ? CharStatements.REP_ITEM_INSTANCE : CharStatements.UPD_ITEM_INSTANCE);
                stmt.AddValue(index, getEntry());
                stmt.AddValue(++index, getOwnerGUID().getCounter());
                stmt.AddValue(++index, getCreator().getCounter());
                stmt.AddValue(++index, getGiftCreator().getCounter());
                stmt.AddValue(++index, getCount());
                stmt.AddValue(++index, (int) getItemData().expiration);

                StringBuilder ss = new StringBuilder();

                for (byte i = 0; i < getItemData().spellCharges.getSize() && i < getBonusData().effectCount; ++i) {
                    ss.append(String.format("%1$s ", getSpellCharges(i)));
                }

                stmt.AddValue(++index, ss.toString());
                stmt.AddValue(++index, (int) getItemData().dynamicFlags);

                ss.setLength(0);

                for (EnchantmentSlot slot = 0; slot.getValue() < EnchantmentSlot.max.getValue(); ++slot) {
                    var enchantment = CliDB.SpellItemEnchantmentStorage.get(getEnchantmentId(slot));

                    if (enchantment != null && !enchantment.getFlags().hasFlag(SpellItemEnchantmentFlags.DoNotSaveToDB)) {
                        ss.append(String.format("%1$s %2$s %3$s ", getEnchantmentId(slot), getEnchantmentDuration(slot), getEnchantmentCharges(slot)));
                    } else {
                        ss.append("0 0 0 ");
                    }
                }

                stmt.AddValue(++index, ss.toString());
                stmt.AddValue(++index, randomBonusListId);
                stmt.AddValue(++index, (int) getItemData().durability);
                stmt.AddValue(++index, (int) getItemData().createPlayedTime);
                stmt.AddValue(++index, text);
                stmt.AddValue(++index, getModifier(ItemModifier.battlePetSpeciesId));
                stmt.AddValue(++index, getModifier(ItemModifier.BattlePetBreedData));
                stmt.AddValue(++index, getModifier(ItemModifier.battlePetLevel));
                stmt.AddValue(++index, getModifier(ItemModifier.BattlePetDisplayId));
                stmt.AddValue(++index, (byte) getItemData().context);

                ss.setLength(0);

                for (int bonusListID : getBonusListIDs()) {
                    ss.append(String.format("%1$s ", bonusListID));
                }

                stmt.AddValue(++index, ss.toString());
                stmt.AddValue(++index, getGUID().getCounter());

                DB.characters.execute(stmt);

                if ((updateState == ItemUpdateState.changed) && isWrapped()) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_GIFT_OWNER);
                    stmt.AddValue(0, getOwnerGUID().getCounter());
                    stmt.AddValue(1, getGUID().getCounter());
                    DB.characters.execute(stmt);
                }

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_GEMS);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                if (getItemData().gems.size() != 0) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEM_INSTANCE_GEMS);
                    stmt.AddValue(0, getGUID().getCounter());
                    var i = 0;
                    var gemFields = 4;

                    for (var gemData : getItemData().gems) {
                        if (gemData.itemId != 0) {
                            stmt.AddValue(1 + i * gemFields, (int) gemData.itemId);
                            StringBuilder gemBonusListIDs = new StringBuilder();

                            for (var bonusListID : gemData.bonusListIDs) {
                                if (bonusListID != 0) {
                                    gemBonusListIDs.append(String.format("%1$s ", bonusListID));
                                }
                            }

                            stmt.AddValue(2 + i * gemFields, gemBonusListIDs.toString());
                            stmt.AddValue(3 + i * gemFields, (byte) gemData.context);
                            stmt.AddValue(4 + i * gemFields, gemScalingLevels.get(i));
                        } else {
                            stmt.AddValue(1 + i * gemFields, 0);
                            stmt.AddValue(2 + i * gemFields, "");
                            stmt.AddValue(3 + i * gemFields, 0);
                            stmt.AddValue(4 + i * gemFields, 0);
                        }

                        ++i;
                    }

                    for (; i < ItemConst.MaxGemSockets; ++i) {
                        stmt.AddValue(1 + i * gemFields, 0);
                        stmt.AddValue(2 + i * gemFields, "");
                        stmt.AddValue(3 + i * gemFields, 0);
                        stmt.AddValue(4 + i * gemFields, 0);
                    }

                    trans.append(stmt);
                }

                ItemModifier[] transmogMods = {ItemModifier.TransmogAppearanceAllSpecs, ItemModifier.TransmogAppearanceSpec1, ItemModifier.TransmogAppearanceSpec2, ItemModifier.TransmogAppearanceSpec3, ItemModifier.TransmogAppearanceSpec4, ItemModifier.TransmogAppearanceSpec5, ItemModifier.EnchantIllusionAllSpecs, ItemModifier.EnchantIllusionSpec1, ItemModifier.EnchantIllusionSpec2, ItemModifier.EnchantIllusionSpec3, ItemModifier.EnchantIllusionSpec4, ItemModifier.EnchantIllusionSpec5, ItemModifier.TransmogSecondaryAppearanceAllSpecs, ItemModifier.TransmogSecondaryAppearanceSpec1, ItemModifier.TransmogSecondaryAppearanceSpec2, ItemModifier.TransmogSecondaryAppearanceSpec3, ItemModifier.TransmogSecondaryAppearanceSpec4, ItemModifier.TransmogSecondaryAppearanceSpec5};

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_TRANSMOG);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                if (transmogMods.Any(modifier -> getModifier(modifier) != 0)) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEM_INSTANCE_TRANSMOG);
                    stmt.AddValue(0, getGUID().getCounter());
                    stmt.AddValue(1, getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                    stmt.AddValue(2, getModifier(ItemModifier.TransmogAppearanceSpec1));
                    stmt.AddValue(3, getModifier(ItemModifier.TransmogAppearanceSpec2));
                    stmt.AddValue(4, getModifier(ItemModifier.TransmogAppearanceSpec3));
                    stmt.AddValue(5, getModifier(ItemModifier.TransmogAppearanceSpec4));
                    stmt.AddValue(6, getModifier(ItemModifier.TransmogAppearanceSpec5));
                    stmt.AddValue(7, getModifier(ItemModifier.EnchantIllusionAllSpecs));
                    stmt.AddValue(8, getModifier(ItemModifier.EnchantIllusionSpec1));
                    stmt.AddValue(9, getModifier(ItemModifier.EnchantIllusionSpec2));
                    stmt.AddValue(10, getModifier(ItemModifier.EnchantIllusionSpec3));
                    stmt.AddValue(11, getModifier(ItemModifier.EnchantIllusionSpec4));
                    stmt.AddValue(12, getModifier(ItemModifier.EnchantIllusionSpec5));
                    stmt.AddValue(13, getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                    stmt.AddValue(14, getModifier(ItemModifier.TransmogSecondaryAppearanceSpec1));
                    stmt.AddValue(15, getModifier(ItemModifier.TransmogSecondaryAppearanceSpec2));
                    stmt.AddValue(16, getModifier(ItemModifier.TransmogSecondaryAppearanceSpec3));
                    stmt.AddValue(17, getModifier(ItemModifier.TransmogSecondaryAppearanceSpec4));
                    stmt.AddValue(18, getModifier(ItemModifier.TransmogSecondaryAppearanceSpec5));
                    trans.append(stmt);
                }

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_ARTIFACT);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_ARTIFACT_POWERS);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                if (getTemplate().getArtifactID() != 0) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEM_INSTANCE_ARTIFACT);
                    stmt.AddValue(0, getGUID().getCounter());
                    stmt.AddValue(1, (long) getItemData().artifactXP);
                    stmt.AddValue(2, getModifier(ItemModifier.artifactAppearanceId));
                    stmt.AddValue(3, getModifier(ItemModifier.ArtifactTier));
                    trans.append(stmt);

                    for (var artifactPower : getItemData().artifactPowers) {
                        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEM_INSTANCE_ARTIFACT_POWERS);
                        stmt.AddValue(0, getGUID().getCounter());
                        stmt.AddValue(1, artifactPower.artifactPowerId);
                        stmt.AddValue(2, artifactPower.purchasedRank);
                        trans.append(stmt);
                    }
                }

                ItemModifier[] modifiersTable = {ItemModifier.TimewalkerLevel, ItemModifier.ArtifactKnowledgeLevel};

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_MODIFIERS);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                if (modifiersTable.Any(modifier -> getModifier(modifier) != 0)) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEM_INSTANCE_MODIFIERS);
                    stmt.AddValue(0, getGUID().getCounter());
                    stmt.AddValue(1, getModifier(ItemModifier.TimewalkerLevel));
                    stmt.AddValue(2, getModifier(ItemModifier.artifactKnowledgeLevel));
                    trans.append(stmt);
                }

                break;
            }
            case Removed: {
                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_GEMS);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_TRANSMOG);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_ARTIFACT);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_ARTIFACT_POWERS);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_INSTANCE_MODIFIERS);
                stmt.AddValue(0, getGUID().getCounter());
                trans.append(stmt);

                if (isWrapped()) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GIFT);
                    stmt.AddValue(0, getGUID().getCounter());
                    trans.append(stmt);
                }

                // Delete the items if this is a container
                if (getLoot() != null && !getLoot().isLooted()) {
                    global.getLootItemStorage().removeStoredLootForContainer(getGUID().getCounter());
                }

                close();

                return;
            }
            case Unchanged:
                break;
        }

        setState(ItemUpdateState.Unchanged);
    }

    public boolean loadFromDB(long guid, ObjectGuid ownerGuid, SQLFields fields, int entry) {
        // create item before any checks for store correct guid
        // and allow use "FSetState(ITEM_REMOVED); saveToDB();" for deleting item from DB
        create(ObjectGuid.create(HighGuid.Item, guid));

        setEntry(entry);
        setObjectScale(1.0f);

        var proto = getTemplate();

        if (proto == null) {
            return false;
        }

        setBonusData(new bonusData(proto));

        // set owner (not if item is only loaded for gbank/auction/mail
        if (!ownerGuid.isEmpty()) {
            setOwnerGUID(ownerGuid);
        }

        var itemFlags = fields.<Integer>Read(7);
        var need_save = false;
        var creator = fields.<Long>Read(2);

        if (creator != 0) {
            if (!(boolean) (itemFlags & ItemFieldFlags.Child.getValue())) {
                setCreator(ObjectGuid.create(HighGuid.Player, creator));
            } else {
                setCreator(ObjectGuid.create(HighGuid.Item, creator));
            }
        }

        var giftCreator = fields.<Long>Read(3);

        if (giftCreator != 0) {
            setGiftCreator(ObjectGuid.create(HighGuid.Player, giftCreator));
        }

        setCount(fields.<Integer>Read(4));

        var duration = fields.<Integer>Read(5);
        setExpiration(duration);

        // update duration if need, and remove if not need
        if (proto.getDuration() != duration) {
            setExpiration(proto.getDuration());
            need_save = true;
        }

        replaceAllItemFlags(ItemFieldFlags.forValue(itemFlags));

        var durability = fields.<Integer>Read(10);
        setDurability(durability);
        // update max durability (and durability) if need
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().maxDurability), proto.getMaxDurability());

        // do not overwrite durability for wrapped items
        if (durability > proto.getMaxDurability() && !isWrapped()) {
            setDurability(proto.getMaxDurability());
            need_save = true;
        }

        setCreatePlayedTime(fields.<Integer>Read(11));
        setText(fields.<String>Read(12));

        setModifier(ItemModifier.battlePetSpeciesId, fields.<Integer>Read(13));
        setModifier(ItemModifier.BattlePetBreedData, fields.<Integer>Read(14));
        setModifier(ItemModifier.battlePetLevel, fields.<SHORT>Read(14));
        setModifier(ItemModifier.BattlePetDisplayId, fields.<Integer>Read(16));

        setContext(itemContext.forValue(fields.<Byte>Read(17)));

        var bonusListString = new LocalizedString();
        ArrayList<Integer> bonusListIDs = new ArrayList<>();

        for (var i = 0; i < bonusListString.length; ++i) {
            int bonusListID;
            tangible.OutObject<Integer> tempOut_bonusListID = new tangible.OutObject<Integer>();
            if (tangible.TryParseHelper.tryParseInt(bonusListString.get(i), tempOut_bonusListID)) {
                bonusListID = tempOut_bonusListID.outArgValue;
                bonusListIDs.add(bonusListID);
            } else {
                bonusListID = tempOut_bonusListID.outArgValue;
            }
        }

        setBonuses(bonusListIDs);

        // load charges after bonuses, they can add more item effects
        var tokens = new LocalizedString();

        for (byte i = 0; i < getItemData().spellCharges.getSize() && i < getBonusData().effectCount && i < tokens.length; ++i) {
            int value;
            tangible.OutObject<Integer> tempOut_value = new tangible.OutObject<Integer>();
            if (tangible.TryParseHelper.tryParseInt(tokens.get(i), tempOut_value)) {
                value = tempOut_value.outArgValue;
                setSpellCharges(i, value);
            } else {
                value = tempOut_value.outArgValue;
            }
        }

        setModifier(ItemModifier.TransmogAppearanceAllSpecs, fields.<Integer>Read(19));
        setModifier(ItemModifier.TransmogAppearanceSpec1, fields.<Integer>Read(20));
        setModifier(ItemModifier.TransmogAppearanceSpec2, fields.<Integer>Read(21));
        setModifier(ItemModifier.TransmogAppearanceSpec3, fields.<Integer>Read(22));
        setModifier(ItemModifier.TransmogAppearanceSpec4, fields.<Integer>Read(23));
        setModifier(ItemModifier.TransmogAppearanceSpec5, fields.<Integer>Read(24));

        setModifier(ItemModifier.EnchantIllusionAllSpecs, fields.<Integer>Read(25));
        setModifier(ItemModifier.EnchantIllusionSpec1, fields.<Integer>Read(26));
        setModifier(ItemModifier.EnchantIllusionSpec2, fields.<Integer>Read(27));
        setModifier(ItemModifier.EnchantIllusionSpec3, fields.<Integer>Read(28));
        setModifier(ItemModifier.EnchantIllusionSpec4, fields.<Integer>Read(29));
        setModifier(ItemModifier.EnchantIllusionSpec4, fields.<Integer>Read(30));

        setModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs, fields.<Integer>Read(31));
        setModifier(ItemModifier.TransmogSecondaryAppearanceSpec1, fields.<Integer>Read(32));
        setModifier(ItemModifier.TransmogSecondaryAppearanceSpec2, fields.<Integer>Read(33));
        setModifier(ItemModifier.TransmogSecondaryAppearanceSpec3, fields.<Integer>Read(34));
        setModifier(ItemModifier.TransmogSecondaryAppearanceSpec4, fields.<Integer>Read(35));
        setModifier(ItemModifier.TransmogSecondaryAppearanceSpec5, fields.<Integer>Read(36));

        var gemFields = 4;
        var gemData = new ItemDynamicFieldGems[ItemConst.MaxGemSockets];

        for (var i = 0; i < ItemConst.MaxGemSockets; ++i) {
            gemData[i] = new ItemDynamicFieldGems();
            gemData[i].itemId = fields.<Integer>Read(37 + i * gemFields);
            var gemBonusListIDs = new LocalizedString();

            if (!gemBonusListIDs.isEmpty()) {
                int b = 0;

                for (String token : gemBonusListIDs) {
                    int bonusListID;
                    tangible.OutObject<Integer> tempOut_bonusListID2 = new tangible.OutObject<Integer>();
                    if (tangible.TryParseHelper.tryParseInt(token, tempOut_bonusListID2) && bonusListID != 0) {
                        bonusListID = tempOut_bonusListID2.outArgValue;
                        gemData[i].BonusListIDs[b++] = (short) bonusListID;
                    } else {
                        bonusListID = tempOut_bonusListID2.outArgValue;
                    }
                }
            }

            gemData[i].context = fields.<Byte>Read(39 + i * gemFields);

            if (gemData[i].itemId != 0) {
                setGem((short) i, gemData[i], fields.<Integer>Read(40 + i * gemFields));
            }
        }

        setModifier(ItemModifier.TimewalkerLevel, fields.<Integer>Read(49));
        setModifier(ItemModifier.artifactKnowledgeLevel, fields.<Integer>Read(50));

        // Enchants must be loaded after all other bonus/scaling data
        var enchantmentTokens = new LocalizedString();

        if (enchantmentTokens.length == EnchantmentSlot.max.getValue() * 3) {
            for (var i = 0; i < EnchantmentSlot.max.getValue(); ++i) {
                var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, i);
                setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.ID), Integer.parseInt(enchantmentTokens.get(i * 3 + 0)));
                setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.duration), Integer.parseInt(enchantmentTokens.get(i * 3 + 1)));
                setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.charges), SHORT.parseShort(enchantmentTokens.get(i * 3 + 2)));
            }
        }

        randomBonusListId = fields.<Integer>Read(9);

        // Remove bind flag for items vs NO_BIND set
        if (isSoulBound() && getBonding() == ItemBondingType.NONE) {
            removeItemFlag(ItemFieldFlags.Soulbound);
            need_save = true;
        }

        if (need_save) // normal item changed state set not work at loading
        {
            byte index = 0;
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ITEM_INSTANCE_ON_LOAD);
            stmt.AddValue(index++, (int) getItemData().expiration);
            stmt.AddValue(index++, (int) getItemData().dynamicFlags);
            stmt.AddValue(index++, (int) getItemData().durability);
            stmt.AddValue(index++, guid);
            DB.characters.execute(stmt);
        }

        return true;
    }

    public final void loadArtifactData(Player owner, long xp, int artifactAppearanceId, int artifactTier, ArrayList<ArtifactPowerData> powers) {
        for (byte i = 0; i <= artifactTier; ++i) {
            initArtifactPowers(getTemplate().getArtifactID(), i);
        }

        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactXP), xp);
        setModifier(ItemModifier.artifactAppearanceId, artifactAppearanceId);
        setModifier(ItemModifier.ArtifactTier, artifactTier);

        var artifactAppearance = CliDB.ArtifactAppearanceStorage.get(artifactAppearanceId);

        if (artifactAppearance != null) {
            setAppearanceModId(artifactAppearance.ItemAppearanceModifierID);
        }

        byte totalPurchasedRanks = 0;

        for (var power : powers) {
            power.currentRankWithBonus += power.purchasedRank;
            totalPurchasedRanks += power.purchasedRank;

            var artifactPower = CliDB.ArtifactPowerStorage.get(power.artifactPowerId);

            for (var e = EnchantmentSlot.Sock1; e.getValue() <= EnchantmentSlot.Sock3.getValue(); ++e) {
                var enchant = CliDB.SpellItemEnchantmentStorage.get(getEnchantmentId(e));

                if (enchant != null) {
                    for (int i = 0; i < ItemConst.MaxItemEnchantmentEffects; ++i) {
                        switch (enchant.Effect[i]) {
                            case ItemEnchantmentType.ArtifactPowerBonusRankByType:
                                if (artifactPower.Label == enchant.EffectArg[i]) {
                                    power.currentRankWithBonus += (byte) enchant.EffectPointsMin[i];
                                }

                                break;
                            case ItemEnchantmentType.ArtifactPowerBonusRankByID:
                                if (artifactPower.id == enchant.EffectArg[i]) {
                                    power.currentRankWithBonus += (byte) enchant.EffectPointsMin[i];
                                }

                                break;
                            case ItemEnchantmentType.ArtifactPowerBonusRankPicker:
                                if (getBonusData().GemRelicType[e - EnchantmentSlot.Sock1] != -1) {
                                    var artifactPowerPicker = CliDB.ArtifactPowerPickerStorage.get(enchant.EffectArg[i]);

                                    if (artifactPowerPicker != null) {
                                        var playerCondition = CliDB.PlayerConditionStorage.get(artifactPowerPicker.playerConditionID);

                                        if (playerCondition == null || (owner != null && ConditionManager.isPlayerMeetingCondition(owner, playerCondition))) {
                                            if (artifactPower.Label == getBonusData().GemRelicType[e - EnchantmentSlot.Sock1]) {
                                                power.currentRankWithBonus += (byte) enchant.EffectPointsMin[i];
                                            }
                                        }
                                    }
                                }

                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            setArtifactPower((short) power.artifactPowerId, power.purchasedRank, power.currentRankWithBonus);
        }

        for (var power : powers) {
            var scaledArtifactPowerEntry = CliDB.ArtifactPowerStorage.get(power.artifactPowerId);

            if (!scaledArtifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.ScalesWithNumPowers)) {
                continue;
            }

            setArtifactPower((short) power.artifactPowerId, power.purchasedRank, (byte) (totalPurchasedRanks + 1));
        }

        checkArtifactRelicSlotUnlock(owner);
    }

    public final void checkArtifactRelicSlotUnlock(Player owner) {
        if (!owner) {
            return;
        }

        var artifactId = getTemplate().getArtifactID();

        if (artifactId == 0) {
            return;
        }

        for (var artifactUnlock : CliDB.ArtifactUnlockStorage.values()) {
            if (artifactUnlock.ArtifactID == artifactId) {
                if (owner.meetPlayerCondition(artifactUnlock.playerConditionID)) {
                    addBonuses(artifactUnlock.ItemBonusListID);
                }
            }
        }
    }

    public void deleteFromDB(SQLTransaction trans) {
        deleteFromDB(trans, getGUID().getCounter());

        // Delete the items if this is a container
        if (getLoot() != null && !getLoot().isLooted()) {
            global.getLootItemStorage().removeStoredLootForContainer(getGUID().getCounter());
        }
    }

    public final void deleteFromInventoryDB(SQLTransaction trans) {
        deleteFromInventoryDB(trans, getGUID().getCounter());
    }

    public final void setItemRandomBonusList(int bonusListId) {
        if (bonusListId == 0) {
            return;
        }

        addBonuses(bonusListId);
    }

    public final void setState(ItemUpdateState state, Player forplayer) {
        if (updateState == ItemUpdateState.New && state == ItemUpdateState.removed) {
            // pretend the item never existed
            if (forplayer) {
                removeItemFromUpdateQueueOf(this, forplayer);
                forplayer.deleteRefundReference(getGUID());
            }

            return;
        }

        if (state != ItemUpdateState.Unchanged) {
            // new items must stay in new state until saved
            if (updateState != ItemUpdateState.New) {
                updateState = state;
            }

            if (forplayer) {
                addItemToUpdateQueueOf(this, forplayer);
            }
        } else {
            // unset in queue
            // the item must be removed from the queue manually
            queuePos = -1;
            updateState = ItemUpdateState.Unchanged;
        }
    }

    public final boolean canBeTraded(boolean mail) {
        return canBeTraded(mail, false);
    }

    public final boolean canBeTraded() {
        return canBeTraded(false, false);
    }

    public final boolean canBeTraded(boolean mail, boolean trade) {
        if (getLootGenerated()) {
            return false;
        }

        if ((!mail || !isBoundAccountWide()) && (isSoulBound() && (!isBOPTradeable() || !trade))) {
            return false;
        }

        if (isBag() && (player.isBagPos(getPos()) || !getAsBag().isEmpty())) {
            return false;
        }

        var owner = getOwnerUnit();

        if (owner != null) {
            if (owner.canUnequipItem(getPos(), false) != InventoryResult.Ok) {
                return false;
            }

            if (Objects.equals(owner.getLootGUID(), getGUID())) {
                return false;
            }
        }

        if (isBoundByEnchant()) {
            return false;
        }

        return true;
    }

    public final long calculateDurabilityRepairCost(float discount) {
        int maxDurability = getItemData().maxDurability;

        if (maxDurability == 0) {
            return 0;
        }

        int curDurability = getItemData().durability;

        var lostDurability = maxDurability - curDurability;

        if (lostDurability == 0) {
            return 0;
        }

        var itemTemplate = getTemplate();

        var durabilityCost = CliDB.DurabilityCostsStorage.get(getItemLevel(getOwnerUnit()));

        if (durabilityCost == null) {
            return 0;
        }

        var durabilityQualityEntryId = ((int) getQuality().getValue() + 1) * 2;
        var durabilityQualityEntry = CliDB.DurabilityQualityStorage.get(durabilityQualityEntryId);

        if (durabilityQualityEntry == null) {
            return 0;
        }

        int dmultiplier = 0;

        if (itemTemplate.getClass() == itemClass.Weapon) {
            dmultiplier = durabilityCost.WeaponSubClassCost[itemTemplate.getSubClass()];
        } else if (itemTemplate.getClass() == itemClass.armor) {
            dmultiplier = durabilityCost.ArmorSubClassCost[itemTemplate.getSubClass()];
        }

        var cost = (long) Math.rint(lostDurability * dmultiplier * durabilityQualityEntry.Data * getRepairCostMultiplier());
        cost = (long) (cost * discount * WorldConfig.getFloatValue(WorldCfg.RateRepaircost));

        if (cost == 0) // Fix for ITEM_QUALITY_ARTIFACT
        {
            cost = 1;
        }

        return cost;
    }

    public final InventoryResult canBeMergedPartlyWith(ItemTemplate proto) {
        // not allow merge looting currently items
        if (getLootGenerated()) {
            return InventoryResult.LootGone;
        }

        // check item type
        if (getEntry() != proto.getId()) {
            return InventoryResult.CantStack;
        }

        // check free space (full stacks can't be target of merge
        if (getCount() >= proto.getMaxStackSize()) {
            return InventoryResult.CantStack;
        }

        return InventoryResult.Ok;
    }

    public final boolean isFitToSpellRequirements(SpellInfo spellInfo) {
        var proto = getTemplate();

        var isEnchantSpell = spellInfo.hasEffect(SpellEffectName.EnchantItem) || spellInfo.hasEffect(SpellEffectName.EnchantItemTemporary) || spellInfo.hasEffect(SpellEffectName.EnchantItemPrismatic);

        if (spellInfo.getEquippedItemClass().getValue() != -1) // -1 == any item class
        {
            if (isEnchantSpell && proto.hasFlag(ItemFlags3.CanStoreEnchants)) {
                return true;
            }

            if (spellInfo.getEquippedItemClass() != proto.getClass()) {
                return false; //  wrong item class
            }

            if (spellInfo.getEquippedItemSubClassMask() != 0) // 0 == any subclass
            {
                if ((spellInfo.getEquippedItemSubClassMask() & (1 << (int) proto.getSubClass())) == 0) {
                    return false; // subclass not present in mask
                }
            }
        }

        if (isEnchantSpell && spellInfo.getEquippedItemInventoryTypeMask() != 0) // 0 == any inventory type
        {
            // Special case - accept weapon type for main and offhand requirements
            if (proto.getInventoryType() == inventoryType.Weapon && (boolean) (spellInfo.getEquippedItemInventoryTypeMask() & (1 << inventoryType.WeaponMainhand.getValue())) || (boolean) (spellInfo.getEquippedItemInventoryTypeMask() & (1 << inventoryType.WeaponOffhand.getValue()))) {
                return true;
            } else if ((spellInfo.getEquippedItemInventoryTypeMask() & (1 << proto.getInventoryType().getValue())) == 0) {
                return false; // inventory type not present in mask
            }
        }

        return true;
    }

    public final void setEnchantment(EnchantmentSlot slot, int id, int duration, int charges) {
        setEnchantment(slot, id, duration, charges, null);
    }

    public final void setEnchantment(EnchantmentSlot slot, int id, int duration, int charges, ObjectGuid caster) {
        // Better lost small time at check in comparison lost time at item save to DB.
        if ((getEnchantmentId(slot) == id) && (getEnchantmentDuration(slot) == duration) && (getEnchantmentCharges(slot) == charges)) {
            return;
        }

        var owner = getOwnerUnit();

        if (slot.getValue() < EnchantmentSlot.MaxInspected.getValue()) {
            var oldEnchant = CliDB.SpellItemEnchantmentStorage.get(getEnchantmentId(slot));

            if (oldEnchant != null && !oldEnchant.getFlags().hasFlag(SpellItemEnchantmentFlags.DoNotLog)) {
                owner.getSession().sendEnchantmentLog(getOwnerGUID(), ObjectGuid.Empty, getGUID(), getEntry(), oldEnchant.id, (int) slot.getValue());
            }

            var newEnchant = CliDB.SpellItemEnchantmentStorage.get(id);

            if (newEnchant != null && !newEnchant.getFlags().hasFlag(SpellItemEnchantmentFlags.DoNotLog)) {
                owner.getSession().sendEnchantmentLog(getOwnerGUID(), caster, getGUID(), getEntry(), id, (int) slot.getValue());
            }
        }

        applyArtifactPowerEnchantmentBonuses(slot, getEnchantmentId(slot), false, owner);
        applyArtifactPowerEnchantmentBonuses(slot, id, true, owner);

        var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, slot.getValue());
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.ID), id);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.duration), duration);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.charges), (short) charges);
        setState(ItemUpdateState.changed, owner);
    }

    public final void setEnchantmentDuration(EnchantmentSlot slot, int duration, Player owner) {
        if (getEnchantmentDuration(slot) == duration) {
            return;
        }

        var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, slot.getValue());
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.duration), duration);
        setState(ItemUpdateState.changed, owner);
        // Cannot use GetOwner() here, has to be passed as an argument to avoid freeze due to hashtable locking
    }

    public final void setEnchantmentCharges(EnchantmentSlot slot, int charges) {
        if (getEnchantmentCharges(slot) == charges) {
            return;
        }

        var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, slot.getValue());
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.charges), (short) charges);
        setState(ItemUpdateState.changed, getOwnerUnit());
    }

    public final void clearEnchantment(EnchantmentSlot slot) {
        if (getEnchantmentId(slot) == 0) {
            return;
        }

        var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, slot.getValue());
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.ID), 0);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.duration), 0);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.charges), (short) 0);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.inactive), (short) 0);
        setState(ItemUpdateState.changed, getOwnerUnit());
    }

    public final SocketedGem getGem(short slot) {
        //ASSERT(slot < MAX_GEM_SOCKETS);
        return slot < getItemData().gems.size() ? getItemData().gems.get(slot) : null;
    }

    public final void setGem(short slot, ItemDynamicFieldGems gem, int gemScalingLevel) {
        //ASSERT(slot < MAX_GEM_SOCKETS);
        gemScalingLevels.set(slot, gemScalingLevel);
        getBonusData().GemItemLevelBonus[slot] = 0;
        var gemTemplate = global.getObjectMgr().getItemTemplate(gem.itemId);

        if (gemTemplate != null) {
            var gemProperties = CliDB.GemPropertiesStorage.get(gemTemplate.getGemProperties());

            if (gemProperties != null) {
                var gemEnchant = CliDB.SpellItemEnchantmentStorage.get(gemProperties.EnchantId);

                if (gemEnchant != null) {
                    BonusData gemBonus = new bonusData(gemTemplate);

                    for (var bonusListId : gem.bonusListIDs) {
                        gemBonus.addBonusList(bonusListId);
                    }

                    var gemBaseItemLevel = gemTemplate.getBaseItemLevel();

                    if (gemBonus.playerLevelToItemLevelCurveId != 0) {
                        var scaledIlvl = (int) global.getDB2Mgr().GetCurveValueAt(gemBonus.playerLevelToItemLevelCurveId, gemScalingLevel);

                        if (scaledIlvl != 0) {
                            gemBaseItemLevel = scaledIlvl;
                        }
                    }

                    getBonusData().GemRelicType[slot] = gemBonus.relicType;

                    for (int i = 0; i < ItemConst.MaxItemEnchantmentEffects; ++i) {
                        switch (gemEnchant.Effect[i]) {
                            case ItemEnchantmentType.BonusListID: {
                                var bonusesEffect = global.getDB2Mgr().GetItemBonusList(gemEnchant.EffectArg[i]);

                                if (bonusesEffect != null) {
                                    for (var itemBonus : bonusesEffect) {
                                        if (itemBonus.BonusType == ItemBonusType.itemLevel) {

                                            getBonusData().GemItemLevelBonus[slot] += (int) itemBonus.Value[0];
                                        }
                                    }
                                }

                                break;
                            }
                            case ItemEnchantmentType.BonusListCurve: {
                                var artifactrBonusListId = global.getDB2Mgr().GetItemBonusListForItemLevelDelta((short) global.getDB2Mgr().GetCurveValueAt((int) Curves.ArtifactRelicItemLevelBonus.getValue(), gemBaseItemLevel + gemBonus.itemLevelBonus));

                                if (artifactrBonusListId != 0) {
                                    var bonusesEffect = global.getDB2Mgr().GetItemBonusList(artifactrBonusListId);

                                    if (bonusesEffect != null) {
                                        for (var itemBonus : bonusesEffect) {
                                            if (itemBonus.BonusType == ItemBonusType.itemLevel) {
                                                getBonusData().GemItemLevelBonus[slot] += (int) itemBonus.Value[0];
                                            }
                                        }
                                    }
                                }

                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            }
        }

        SocketedGem gemField = getValues().modifyValue(getItemData()).modifyValue(getItemData().gems, slot);
        setUpdateFieldValue(gemField.modifyValue(gemField.itemId), gem.itemId);
        setUpdateFieldValue(gemField.modifyValue(gemField.context), gem.context);

        for (var i = 0; i < 16; ++i) {

            setUpdateFieldValue(ref gemField.modifyValue(gemField.bonusListIDs, i), gem.BonusListIDs[i]);
        }
    }

    public final boolean gemsFitSockets() {
        int gemSlot = 0;

        for (var gemData : getItemData().gems) {
            var SocketColor = getTemplate().getSocketColor(gemSlot);

            if (SocketColor == 0) // no socket slot
            {
                continue;
            }

            SocketColor GemColor = SocketColor.forValue(0);

            var gemProto = global.getObjectMgr().getItemTemplate(gemData.itemId);

            if (gemProto != null) {
                var gemProperty = CliDB.GemPropertiesStorage.get(gemProto.getGemProperties());

                if (gemProperty != null) {
                    GemColor = gemProperty.type;
                }
            }

            if (!GemColor.hasFlag(ItemConst.SocketColorToGemTypeMask[SocketColor.getValue()])) // bad gem color on this socket
            {
                return false;
            }
        }

        return true;
    }

    public final byte getGemCountWithID(int GemID) {
        var list = (ArrayList<SocketedGem>) getItemData().gems;

        return (byte) list.size() (gemData -> gemData.itemId == GemID);
    }

    public final byte getGemCountWithLimitCategory(int limitCategory) {
        var list = (ArrayList<SocketedGem>) getItemData().gems;

        return (byte) list.size() (gemData ->
        {
            var gemProto = global.getObjectMgr().getItemTemplate(gemData.itemId);

            if (gemProto == null) {
                return false;
            }

            return gemProto.getItemLimitCategory() == limitCategory;
        });
    }

    public final boolean isLimitedToAnotherMapOrZone(int cur_mapId, int cur_zoneId) {
        var proto = getTemplate();

        return proto != null && ((proto.getMap() != 0 && proto.getMap() != cur_mapId) || ((proto.getArea(0) != 0 && proto.getArea(0) != cur_zoneId) && (proto.getArea(1) != 0 && proto.getArea(1) != cur_zoneId)));
    }

    public final void sendUpdateSockets() {
        SocketGemsSuccess socketGems = new SocketGemsSuccess();
        socketGems.item = getGUID();

        getOwnerUnit().sendPacket(socketGems);
    }

    public final void sendTimeUpdate(Player owner) {
        int duration = getItemData().expiration;

        if (duration == 0) {
            return;
        }

        ItemTimeUpdate itemTimeUpdate = new ItemTimeUpdate();
        itemTimeUpdate.itemGuid = getGUID();
        itemTimeUpdate.durationLeft = duration;
        owner.sendPacket(itemTimeUpdate);
    }

    public final Item cloneItem(int count) {
        return cloneItem(count, null);
    }

    public final Item cloneItem(int count, Player player) {
        var newItem = createItem(getEntry(), count, getContext(), player);

        if (newItem == null) {
            return null;
        }

        newItem.setCreator(getCreator());
        newItem.setGiftCreator(getGiftCreator());
        newItem.replaceAllItemFlags(ItemFieldFlags.forValue(getItemData().dynamicFlags & ~(int) (ItemFieldFlags.refundable.getValue() | ItemFieldFlags.BopTradeable.getValue())));
        newItem.setExpiration(getItemData().expiration);

        // player CAN be NULL in which case we must not update random properties because that accesses player's item update queue
        if (player != null) {
            newItem.setItemRandomBonusList(randomBonusListId);
        }

        return newItem;
    }

    public final boolean isBindedNotWith(Player player) {
        // not binded item
        if (!isSoulBound()) {
            return false;
        }

        // own item
        if (Objects.equals(getOwnerGUID(), player.getGUID())) {
            return false;
        }

        if (isBOPTradeable()) {
            if (allowedGuiDs.contains(player.getGUID())) {
                return false;
            }
        }

        // BOA item case
        if (isBoundAccountWide()) {
            return false;
        }

        return true;
    }

    @Override
    public void buildUpdate(HashMap<Player, UpdateData> data) {
        var owner = getOwnerUnit();

        if (owner != null) {
            buildFieldsUpdate(owner, data);
        }

        clearUpdateMask(false);
    }

    @Override
    public UpdateFieldFlag getUpdateFieldFlagsFor(Player target) {
        if (Objects.equals(target.getGUID(), getOwnerGUID())) {
            return UpdateFieldFlag.owner;
        }

        return UpdateFieldFlag.NONE;
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        getObjectData().writeCreate(buffer, flags, this, target);
        getItemData().writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize() + 1);
        data.writeInt8((byte) flags.getValue());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        if (getValues().hasChanged(TypeId.object)) {
            getObjectData().writeUpdate(buffer, flags, this, target);
        }

        if (getValues().hasChanged(TypeId.item)) {
            getItemData().writeUpdate(buffer, flags, this, target);
        }


        data.writeInt32(buffer.getSize());
        data.writeInt32(getValues().getChangedObjectTypeMask());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdateWithFlag(WorldPacket data, UpdateFieldFlag flags, Player target) {
        UpdateMask valuesMask = new UpdateMask(14);
        valuesMask.set(getObjectTypeId().item.getValue());

        WorldPacket buffer = new WorldPacket();
        UpdateMask mask = new UpdateMask(40);

        buffer.writeInt32(valuesMask.getBlock(0));
        getItemData().appendAllowedFieldsMaskForFlag(mask, flags);
        getItemData().writeUpdate(buffer, mask, true, this, target);

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(getItemData());
        super.clearUpdateMask(remove);
    }

    @Override
    public boolean addToObjectUpdate() {
        var owner = getOwnerUnit();

        if (owner) {
            owner.getMap().addUpdateObject(this);

            return true;
        }

        return false;
    }

    @Override
    public void removeFromObjectUpdate() {
        var owner = getOwnerUnit();

        if (owner) {
            owner.getMap().removeUpdateObject(this);
        }
    }

    public final void saveRefundDataToDB() {
        deleteRefundDataFromDB();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEM_REFUND_INSTANCE);
        stmt.AddValue(0, getGUID().getCounter());
        stmt.AddValue(1, getRefundRecipient().getCounter());
        stmt.AddValue(2, getPaidMoney());
        stmt.AddValue(3, (short) getPaidExtendedCost());
        DB.characters.execute(stmt);
    }

    public final void deleteRefundDataFromDB() {
        deleteRefundDataFromDB(null);
    }

    public final void deleteRefundDataFromDB(SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_REFUND_INSTANCE);
        stmt.AddValue(0, getGUID().getCounter());

        if (trans != null) {
            trans.append(stmt);
        } else {
            DB.characters.execute(stmt);
        }
    }

    public final void setNotRefundable(Player owner, boolean changestate, SQLTransaction trans) {
        setNotRefundable(owner, changestate, trans, true);
    }

    public final void setNotRefundable(Player owner, boolean changestate) {
        setNotRefundable(owner, changestate, null, true);
    }

    public final void setNotRefundable(Player owner) {
        setNotRefundable(owner, true, null, true);
    }

    public final void setNotRefundable(Player owner, boolean changestate, SQLTransaction trans, boolean addToCollection) {
        if (!isRefundable()) {
            return;
        }

        ItemExpirePurchaseRefund itemExpirePurchaseRefund = new ItemExpirePurchaseRefund();
        itemExpirePurchaseRefund.itemGUID = getGUID();
        owner.sendPacket(itemExpirePurchaseRefund);

        removeItemFlag(ItemFieldFlags.refundable);

        // Following is not applicable in the trading procedure
        if (changestate) {
            setState(ItemUpdateState.changed, owner);
        }

        setRefundRecipient(ObjectGuid.Empty);
        setPaidMoney(0);
        setPaidExtendedCost(0);
        deleteRefundDataFromDB(trans);

        owner.deleteRefundReference(getGUID());

        if (addToCollection) {
            owner.getSession().getCollectionMgr().addItemAppearance(this);
        }
    }

    public final void updatePlayedTime(Player owner) {
        // Get current played time
        int current_playtime = getItemData().createPlayedTime;
        // Calculate time elapsed since last played time update
        var curtime = gameTime.GetGameTime();
        var elapsed = (int) (curtime - lastPlayedTimeUpdate);
        var new_playtime = current_playtime + elapsed;

        // Check if the refund timer has expired yet
        if (new_playtime <= 2 * time.Hour) {
            // No? Proceed.
            // Update the data field
            setCreatePlayedTime(new_playtime);
            // Flag as changed to get saved to DB
            setState(ItemUpdateState.changed, owner);
            // Speaks for itself
            lastPlayedTimeUpdate = curtime;

            return;
        }

        // Yes
        setNotRefundable(owner);
    }

    public final void setSoulboundTradeable(ArrayList<ObjectGuid> allowedLooters) {
        setItemFlag(ItemFieldFlags.BopTradeable);
        allowedGuiDs = allowedLooters;
    }

    public final void clearSoulboundTradeable(Player currentOwner) {
        removeItemFlag(ItemFieldFlags.BopTradeable);

        if (allowedGuiDs.isEmpty()) {
            return;
        }

        currentOwner.getSession().getCollectionMgr().addItemAppearance(this);
        allowedGuiDs.clear();
        setState(ItemUpdateState.changed, currentOwner);
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEM_BOP_TRADE);
        stmt.AddValue(0, getGUID().getCounter());
        DB.characters.execute(stmt);
    }

    public final boolean checkSoulboundTradeExpire() {
        // called from owner's update - GetOwner() MUST be valid
        if (getItemData().createPlayedTime + 2 * time.Hour < getOwnerUnit().getTotalPlayedTime()) {
            clearSoulboundTradeable(getOwnerUnit());

            return true; // remove from tradeable list
        }

        return false;
    }

    public final int getSellPrice(Player owner) {
        return getSellPrice(getTemplate(), (int) getQuality().getValue(), getItemLevel(owner));
    }

    public final int getItemLevel(Player owner) {
        var itemTemplate = getTemplate();
        int minItemLevel = owner.getUnitData().minItemLevel;
        int minItemLevelCutoff = owner.getUnitData().minItemLevelCutoff;
        var maxItemLevel = itemTemplate.hasFlag(ItemFlags3.IgnoreItemLevelCapInPvp) ? 0 : owner.getUnitData().maxItemLevel;
        var pvpBonus = owner.isUsingPvpItemLevels();

        int azeriteLevel = 0;
        var azeriteItem = getAsAzeriteItem();

        if (azeriteItem != null) {
            azeriteLevel = azeriteItem.GetEffectiveLevel();
        }

        return getItemLevel(itemTemplate, getBonusData(), owner.getLevel(), getModifier(ItemModifier.TimewalkerLevel), minItemLevel, minItemLevelCutoff, maxItemLevel, pvpBonus, azeriteLevel);
    }

    public final float getItemStatValue(int index, Player owner) {
        switch (ItemModType.forValue(getItemStatType(index))) {
            case Corruption:
            case CorruptionResistance:
                return getBonusData().StatPercentEditor[index];
            default:
                break;
        }

        var itemLevel = getItemLevel(owner);
        var randomPropPoints = ItemEnchantmentManager.getRandomPropertyPoints(itemLevel, getQuality(), getTemplate().getInventoryType(), getTemplate().getSubClass());

        if (randomPropPoints != 0) {
            var statValue = getBonusData().StatPercentEditor[index] * randomPropPoints * 0.0001f;
            var gtCost = CliDB.ItemSocketCostPerLevelGameTable.GetRow(itemLevel);

            if (gtCost != null) {
                statValue -= getBonusData().ItemStatSocketCostMultiplier[index] * gtCost.SocketCost;
            }

            return statValue;
        }

        return 0f;
    }

    public final ItemDisenchantLootRecord getDisenchantLoot(Player owner) {
        if (!getBonusData().canDisenchant) {
            return null;
        }

        return getDisenchantLoot(getTemplate(), (int) getQuality().getValue(), getItemLevel(owner));
    }

    public final int getDisplayId(Player owner) {
        var itemModifiedAppearanceId = getModifier(ItemConst.AppearanceModifierSlotBySpec[owner.getActiveTalentGroup()]);

        if (itemModifiedAppearanceId == 0) {
            itemModifiedAppearanceId = getModifier(ItemModifier.TransmogAppearanceAllSpecs);
        }

        var transmog = CliDB.ItemModifiedAppearanceStorage.get(itemModifiedAppearanceId);

        if (transmog != null) {
            var itemAppearance = CliDB.ItemAppearanceStorage.get(transmog.ItemAppearanceID);

            if (itemAppearance != null) {
                return itemAppearance.ItemDisplayInfoID;
            }
        }

        return global.getDB2Mgr().GetItemDisplayId(getEntry(), getAppearanceModId());
    }

    public final ItemModifiedAppearanceRecord getItemModifiedAppearance() {
        return global.getDB2Mgr().getItemModifiedAppearance(getEntry(), getBonusData().appearanceModID);
    }

    public final int getModifier(ItemModifier modifier) {
        var modifierIndex = getItemData().modifiers.getValue().VALUES.FindIndexIf(mod ->
        {
            return mod.type == (byte) modifier.getValue();
        });

        if (modifierIndex != -1) {
            return getItemData().modifiers.getValue().VALUES.get(modifierIndex).value;
        }

        return 0;
    }

    public final void setModifier(ItemModifier modifier, int value) {
        var modifierIndex = getItemData().modifiers.getValue().VALUES.FindIndexIf(mod ->
        {
            return mod.type == (byte) modifier.getValue();
        });

        if (value != 0) {
            if (modifierIndex == -1) {
                ItemMod mod = new ItemMod();
                mod.value = value;
                mod.type = (byte) modifier.getValue();

                addDynamicUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().modifiers).getValue().modifyValue(getItemData().modifiers.getValue().VALUES), mod);
            } else {
                ItemModList itemModList = getValues().modifyValue(getItemData()).modifyValue(getItemData().modifiers);
                itemModList.modifyValue(itemModList.VALUES, modifierIndex);

                setUpdateFieldValue(ref itemModList.modifyValue(itemModList.VALUES, modifierIndex).getValue().value, value);
            }
        } else {
            if (modifierIndex == -1) {
                return;
            }

            removeDynamicUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().modifiers).getValue().modifyValue(getItemData().modifiers.getValue().VALUES), modifierIndex);
        }
    }

    public final int getVisibleEntry(Player owner) {
        var itemModifiedAppearanceId = getModifier(ItemConst.AppearanceModifierSlotBySpec[owner.getActiveTalentGroup()]);

        if (itemModifiedAppearanceId == 0) {
            itemModifiedAppearanceId = getModifier(ItemModifier.TransmogAppearanceAllSpecs);
        }

        var transmog = CliDB.ItemModifiedAppearanceStorage.get(itemModifiedAppearanceId);

        if (transmog != null) {
            return transmog.itemID;
        }

        return getEntry();
    }

    public final short getVisibleAppearanceModId(Player owner) {
        var itemModifiedAppearanceId = getModifier(ItemConst.AppearanceModifierSlotBySpec[owner.getActiveTalentGroup()]);

        if (itemModifiedAppearanceId == 0) {
            itemModifiedAppearanceId = getModifier(ItemModifier.TransmogAppearanceAllSpecs);
        }

        var transmog = CliDB.ItemModifiedAppearanceStorage.get(itemModifiedAppearanceId);

        if (transmog != null) {
            return (short) transmog.ItemAppearanceModifierID;
        }

        return (short) getAppearanceModId();
    }

    public final int getVisibleSecondaryModifiedAppearanceId(Player owner) {
        var itemModifiedAppearanceId = getModifier(ItemConst.SecondaryAppearanceModifierSlotBySpec[owner.getActiveTalentGroup()]);

        if (itemModifiedAppearanceId == 0) {
            itemModifiedAppearanceId = getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs);
        }

        return itemModifiedAppearanceId;
    }

    public final int getVisibleEnchantmentId(Player owner) {
        var enchantmentId = getModifier(ItemConst.IllusionModifierSlotBySpec[owner.getActiveTalentGroup()]);

        if (enchantmentId == 0) {
            enchantmentId = getModifier(ItemModifier.EnchantIllusionAllSpecs);
        }

        if (enchantmentId == 0) {
            enchantmentId = getEnchantmentId(EnchantmentSlot.Perm);
        }

        return enchantmentId;
    }

    public final short getVisibleItemVisual(Player owner) {
        var enchant = CliDB.SpellItemEnchantmentStorage.get(getVisibleEnchantmentId(owner));

        if (enchant != null) {
            return enchant.itemVisual;
        }

        return 0;
    }

    public final ArrayList<Integer> getBonusListIDs() {
        return getItemData().itemBonusKey.getValue().bonusListIDs;
    }

    public final void addBonuses(int bonusListID) {
        var bonusListIDs = getBonusListIDs();

        if (bonusListIDs.contains(bonusListID)) {
            return;
        }

        var bonuses = global.getDB2Mgr().GetItemBonusList(bonusListID);

        if (bonuses != null) {
            ItemBonusKey itemBonusKey = new itemBonusKey();
            itemBonusKey.itemID = getEntry();
            itemBonusKey.bonusListIDs = getBonusListIDs();
            itemBonusKey.bonusListIDs.add(bonusListID);
            setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemBonusKey), itemBonusKey);

            for (var bonus : bonuses) {
                getBonusData().addBonus(bonus.BonusType, bonus.value);
            }

            setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemAppearanceModID), (byte) getBonusData().appearanceModID);
        }
    }

    public final void setBonuses(ArrayList<Integer> bonusListIDs) {
        if (bonusListIDs == null) {
            bonusListIDs = new ArrayList<>();
        }

        ItemBonusKey itemBonusKey = new itemBonusKey();
        itemBonusKey.itemID = getEntry();
        itemBonusKey.bonusListIDs = bonusListIDs;
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemBonusKey), itemBonusKey);

        for (var bonusListID : getBonusListIDs()) {
            getBonusData().addBonusList(bonusListID);
        }

        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemAppearanceModID), (byte) getBonusData().appearanceModID);
    }

    public final void clearBonuses() {
        ItemBonusKey itemBonusKey = new itemBonusKey();
        itemBonusKey.itemID = getEntry();
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemBonusKey), itemBonusKey);
        setBonusData(new bonusData(getTemplate()));
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().itemAppearanceModID), (byte) getBonusData().appearanceModID);
    }

    public final boolean isArtifactDisabled() {
        var artifact = CliDB.ArtifactStorage.get(getTemplate().getArtifactID());

        if (artifact != null) {
            return artifact.artifactCategoryID != 2; // fishing artifact
        }

        return true;
    }

    public final ArtifactPower getArtifactPower(int artifactPowerId) {
        var index = artifactPowerIdToIndex.get(artifactPowerId);

        if (index != 0) {
            return getItemData().artifactPowers.get(index);
        }

        return null;
    }

    public final void setArtifactPower(short artifactPowerId, byte purchasedRank, byte currentRankWithBonus) {
        var foundIndex = artifactPowerIdToIndex.get(artifactPowerId);

        if (foundIndex != 0) {
            ArtifactPower artifactPower = getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactPowers, foundIndex);
            tangible.RefObject<Byte> tempRef_PurchasedRank = new tangible.RefObject<Byte>(artifactPower.purchasedRank);
            setUpdateFieldValue(tempRef_PurchasedRank, purchasedRank);
            artifactPower.purchasedRank = tempRef_PurchasedRank.refArgValue;
            tangible.RefObject<Byte> tempRef_CurrentRankWithBonus = new tangible.RefObject<Byte>(artifactPower.currentRankWithBonus);
            setUpdateFieldValue(tempRef_CurrentRankWithBonus, currentRankWithBonus);
            artifactPower.currentRankWithBonus = tempRef_CurrentRankWithBonus.refArgValue;
        }
    }

    public final void initArtifactPowers(byte artifactId, byte artifactTier) {
        for (var artifactPower : global.getDB2Mgr().GetArtifactPowers(artifactId)) {
            if (artifactPower.tier != artifactTier) {
                continue;
            }

            if (artifactPowerIdToIndex.containsKey(artifactPower.id)) {
                continue;
            }

            ArtifactPowerData powerData = new ArtifactPowerData();
            powerData.artifactPowerId = artifactPower.id;
            powerData.purchasedRank = 0;
            powerData.currentRankWithBonus = (byte) ((artifactPower.flags.getValue() & ArtifactPowerFlag.first.getValue().getValue()) == ArtifactPowerFlag.first.getValue() ? 1 : 0);
            addArtifactPower(powerData);
        }
    }

    public final int getTotalUnlockedArtifactPowers() {
        var purchased = getTotalPurchasedArtifactPowers();
        long artifactXp = getItemData().artifactXP;
        var currentArtifactTier = getModifier(ItemModifier.ArtifactTier);
        int extraUnlocked = 0;

        do {
            long xpCost = 0;
            var cost = CliDB.ArtifactLevelXPGameTable.GetRow(purchased + extraUnlocked + 1);

            if (cost != null) {
                xpCost = (long) (currentArtifactTier == PlayerConst.MaxArtifactTier ? cost.XP2 : cost.XP);
            }

            if (artifactXp < xpCost) {
                break;
            }

            artifactXp -= xpCost;
            ++extraUnlocked;
        } while (true);

        return purchased + extraUnlocked;
    }

    public final int getTotalPurchasedArtifactPowers() {
        int purchasedRanks = 0;

        for (var power : getItemData().artifactPowers) {
            purchasedRanks += power.purchasedRank;
        }

        return purchasedRanks;
    }

    public final void copyArtifactDataFromParent(Item parent) {
        system.arraycopy(parent.getBonusData().gemItemLevelBonus, 0, getBonusData().gemItemLevelBonus, 0, getBonusData().gemItemLevelBonus.length);
        setModifier(ItemModifier.artifactAppearanceId, parent.getModifier(ItemModifier.artifactAppearanceId));
        setAppearanceModId(parent.getAppearanceModId());
    }

    public final void setArtifactXP(long xp) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactXP), xp);
    }

    public final void giveArtifactXp(long amount, Item sourceItem, ArtifactCategory artifactCategoryId) {
        var owner = getOwnerUnit();

        if (!owner) {
            return;
        }

        if (artifactCategoryId != 0) {
            int artifactKnowledgeLevel = 1;

            if (sourceItem != null && sourceItem.getModifier(ItemModifier.artifactKnowledgeLevel) != 0) {
                artifactKnowledgeLevel = sourceItem.getModifier(ItemModifier.artifactKnowledgeLevel);
            }

            var artifactKnowledge = CliDB.ArtifactKnowledgeMultiplierGameTable.GetRow(artifactKnowledgeLevel);

            if (artifactKnowledge != null) {
                amount = (long) (amount * artifactKnowledge.multiplier);
            }

            if (amount >= 5000) {
                amount = 50 * (amount / 50);
            } else if (amount >= 1000) {
                amount = 25 * (amount / 25);
            } else if (amount >= 50) {
                amount = 5 * (amount / 5);
            }
        }

        setArtifactXP(getItemData().artifactXP + amount);

        ArtifactXpGain artifactXpGain = new ArtifactXpGain();
        artifactXpGain.artifactGUID = getGUID();
        artifactXpGain.amount = amount;
        owner.sendPacket(artifactXpGain);

        setState(ItemUpdateState.changed, owner);
    }

    public final ItemContext getContext() {
        return itemContext.forValue((int) getItemData().context);
    }

    public final void setContext(ItemContext context) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().context), context.getValue());
    }

    public final void setPetitionId(int petitionId) {
        var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, 0);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.ID), petitionId);
    }

    public final void setPetitionNumSignatures(int signatures) {
        var enchantmentField = getValues().modifyValue(getItemData()).modifyValue(getItemData().enchantment, 0);
        setUpdateFieldValue(enchantmentField.modifyValue(enchantmentField.duration), signatures);
    }

    public final void setFixedLevel(int level) {
        if (!getBonusData().hasFixedLevel || getModifier(ItemModifier.TimewalkerLevel) != 0) {
            return;
        }

        if (getBonusData().playerLevelToItemLevelCurveId != 0) {
            var levels = global.getDB2Mgr().GetContentTuningData(getBonusData().contentTuningId, 0, true);

            if (levels != null) {
                level = (int) Math.min(Math.max((short) level, levels.getValue().minLevel), levels.getValue().maxLevel);
            }

            setModifier(ItemModifier.TimewalkerLevel, level);
        }
    }

    public final int getRequiredLevel() {
        var fixedLevel = (int) getModifier(ItemModifier.TimewalkerLevel);

        if (getBonusData().requiredLevelCurve != 0) {
            return (int) global.getDB2Mgr().GetCurveValueAt(getBonusData().requiredLevelCurve, fixedLevel);
        }

        if (getBonusData().requiredLevelOverride != 0) {
            return getBonusData().requiredLevelOverride;
        }

        if (getBonusData().hasFixedLevel && getBonusData().playerLevelToItemLevelCurveId != 0) {
            return fixedLevel;
        }

        return getBonusData().requiredLevel;
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nOwner: %2$s Count: %3$s BagSlot: %4$s Slot: %5$s Equipped: %6$s", super.getDebugInfo(), getOwnerGUID(), getCount(), getBagSlot(), getSlot(), isEquipped());
    }

    public final void setBinding(boolean val) {
        if (val) {
            setItemFlag(ItemFieldFlags.Soulbound);
        } else {
            removeItemFlag(ItemFieldFlags.Soulbound);
        }
    }

    public final boolean hasItemFlag(ItemFieldFlags flag) {
        return (getItemData().dynamicFlags & (int) flag.getValue()) != 0;
    }

    public final void setItemFlag(ItemFieldFlags flags) {
        setUpdateFieldFlagValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().dynamicFlags), (int) flags.getValue());
    }

    public final void removeItemFlag(ItemFieldFlags flags) {
        removeUpdateFieldFlagValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().dynamicFlags), (int) flags.getValue());
    }

    public final void replaceAllItemFlags(ItemFieldFlags flags) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().dynamicFlags), (int) flags.getValue());
    }

    public final boolean hasItemFlag2(ItemFieldFlags2 flag) {
        return (getItemData().dynamicFlags2 & (int) flag.getValue()) != 0;
    }

    public final void setItemFlag2(ItemFieldFlags2 flags) {
        setUpdateFieldFlagValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().dynamicFlags2), (int) flags.getValue());
    }

    public final void removeItemFlag2(ItemFieldFlags2 flags) {
        removeUpdateFieldFlagValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().dynamicFlags2), (int) flags.getValue());
    }

    public final void replaceAllItemFlags2(ItemFieldFlags2 flags) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().dynamicFlags2), (int) flags.getValue());
    }

    public final void setDurability(int durability) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().durability), durability);
    }

    public final void setMaxDurability(int maxDurability) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().maxDurability), maxDurability);
    }

    public final void setInTrade() {
        setInTrade(true);
    }

    public final int getEnchantmentId(EnchantmentSlot slot) {
        return getItemData().enchantment.get(slot.getValue()).ID;
    }

    public final int getEnchantmentDuration(EnchantmentSlot slot) {
        return getItemData().enchantment.get(slot.getValue()).duration;
    }

    public final int getEnchantmentCharges(EnchantmentSlot slot) {
        return getItemData().enchantment.get(slot.getValue()).charges;
    }

    public final void setCreatePlayedTime(int createPlayedTime) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().createPlayedTime), createPlayedTime);
    }

    public final int getSpellCharges() {
        return getSpellCharges(0);
    }

    public final int getSpellCharges(int index) {
        return getItemData().spellCharges.get(index);
    }

    public final void setSpellCharges(int index, int value) {

        setUpdateFieldValue(ref getValues().modifyValue(getItemData()).modifyValue(getItemData().spellCharges, index), value);
    }

    public final void FSetState(ItemUpdateState state) // forced
    {
        updateState = state;
    }

    @Override
    public boolean hasQuest(int quest_id) {
        return getTemplate().getStartQuest() == quest_id;
    }

    @Override
    public boolean hasInvolvedQuest(int quest_id) {
        return false;
    }

    public final int getItemStatType(int index) {
        return getBonusData().ItemStatType[index];
    }

    public final SocketColor getSocketColor(int index) {
        return getBonusData().socketColor[index];
    }

    @Override
    public Loot getLootForPlayer(Player player) {
        return getLoot();
    }

    private boolean hasEnchantRequiredSkill(Player player) {
        // Check all enchants for required skill
        for (var enchant_slot = EnchantmentSlot.Perm; enchant_slot.getValue() < EnchantmentSlot.max.getValue(); ++enchant_slot) {
            var enchant_id = getEnchantmentId(enchant_slot);

            if (enchant_id != 0) {
                var enchantEntry = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

                if (enchantEntry != null) {
                    if (enchantEntry.RequiredSkillID != 0 && player.getSkillValue(SkillType.forValue(enchantEntry.RequiredSkillID)).getValue() < enchantEntry.RequiredSkillRank) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int getEnchantRequiredLevel() {
        int level = 0;

        // Check all enchants for required level
        for (var enchant_slot = EnchantmentSlot.Perm; enchant_slot.getValue() < EnchantmentSlot.max.getValue(); ++enchant_slot) {
            var enchant_id = getEnchantmentId(enchant_slot);

            if (enchant_id != 0) {
                var enchantEntry = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

                if (enchantEntry != null) {
                    if (enchantEntry.minLevel > level) {
                        level = enchantEntry.minLevel;
                    }
                }
            }
        }

        return level;
    }

    private boolean isBoundByEnchant() {
        // Check all enchants for soulbound
        for (var enchant_slot = EnchantmentSlot.Perm; enchant_slot.getValue() < EnchantmentSlot.max.getValue(); ++enchant_slot) {
            var enchant_id = getEnchantmentId(enchant_slot);

            if (enchant_id != 0) {
                var enchantEntry = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

                if (enchantEntry != null) {
                    if (enchantEntry.getFlags().hasFlag(SpellItemEnchantmentFlags.Soulbound)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedItemMask, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        getItemData().filterDisallowedFieldsMaskForFlag(requestedItemMask, flags);

        if (requestedItemMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().item.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().item.getValue())) {
            getItemData().writeUpdate(buffer, requestedItemMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    private boolean isValidTransmogrificationTarget() {
        var proto = getTemplate();

        if (proto == null) {
            return false;
        }

        if (proto.getClass() != itemClass.armor && proto.getClass() != itemClass.Weapon) {
            return false;
        }

        if (proto.getClass() == itemClass.Weapon && proto.getSubClass() == (int) ItemSubClassWeapon.FishingPole.getValue()) {
            return false;
        }

        if (proto.hasFlag(ItemFlags2.NoAlterItemVisual)) {
            return false;
        }

        if (!hasStats()) {
            return false;
        }

        return true;
    }

    private boolean hasStats() {
        var proto = getTemplate();
        var owner = getOwnerUnit();

        for (byte i = 0; i < ItemConst.MaxStats; ++i) {
            if ((owner ? getItemStatValue(i, owner) : proto.getStatPercentEditor(i)) != 0) {
                return true;
            }
        }

        return false;
    }

    private int getBuyPrice(Player owner, tangible.OutObject<Boolean> standardPrice) {
        return getBuyPrice(getTemplate(), (int) getQuality().getValue(), getItemLevel(owner), standardPrice);
    }

    private void addArtifactPower(ArtifactPowerData artifactPower) {
        var index = artifactPowerIdToIndex.size();
        artifactPowerIdToIndex.put(artifactPower.artifactPowerId, (short) index);

        ArtifactPower powerField = new ArtifactPower();
        powerField.artifactPowerId = (short) artifactPower.artifactPowerId;
        powerField.purchasedRank = artifactPower.purchasedRank;
        powerField.currentRankWithBonus = artifactPower.currentRankWithBonus;

        addDynamicUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactPowers), powerField);
    }

    private void applyArtifactPowerEnchantmentBonuses(EnchantmentSlot slot, int enchantId, boolean apply, Player owner) {
        var enchant = CliDB.SpellItemEnchantmentStorage.get(enchantId);

        if (enchant != null) {
            for (int i = 0; i < ItemConst.MaxItemEnchantmentEffects; ++i) {
                switch (enchant.Effect[i]) {
                    case ItemEnchantmentType.ArtifactPowerBonusRankByType: {
                        for (var artifactPowerIndex = 0; artifactPowerIndex < getItemData().artifactPowers.size(); ++artifactPowerIndex) {
                            var artifactPower = getItemData().artifactPowers.get(artifactPowerIndex);

                            if (CliDB.ArtifactPowerStorage.get(artifactPower.artifactPowerId).Label == enchant.EffectArg[i]) {
                                var newRank = artifactPower.currentRankWithBonus;

                                if (apply) {
                                    newRank += (byte) enchant.EffectPointsMin[i];
                                } else {
                                    newRank -= (byte) enchant.EffectPointsMin[i];
                                }

                                artifactPower = getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactPowers, artifactPowerIndex);
                                tangible.RefObject<Byte> tempRef_CurrentRankWithBonus = new tangible.RefObject<Byte>(artifactPower.currentRankWithBonus);
                                setUpdateFieldValue(tempRef_CurrentRankWithBonus, newRank);
                                artifactPower.currentRankWithBonus = tempRef_CurrentRankWithBonus.refArgValue;

                                if (isEquipped()) {
                                    var artifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(artifactPower.artifactPowerId, (byte) (newRank != 0 ? newRank - 1 : 0));

                                    if (artifactPowerRank != null) {
                                        owner.applyArtifactPowerRank(this, artifactPowerRank, newRank != 0);
                                    }
                                }
                            }
                        }
                    }

                    break;
                    case ItemEnchantmentType.ArtifactPowerBonusRankByID: {
                        var artifactPowerIndex = artifactPowerIdToIndex.get(enchant.EffectArg[i]);

                        if (artifactPowerIndex != 0) {
                            var newRank = getItemData().artifactPowers.get(artifactPowerIndex).currentRankWithBonus;

                            if (apply) {
                                newRank += (byte) enchant.EffectPointsMin[i];
                            } else {
                                newRank -= (byte) enchant.EffectPointsMin[i];
                            }

                            ArtifactPower artifactPower = getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactPowers, artifactPowerIndex);
                            tangible.RefObject<Byte> tempRef_CurrentRankWithBonus2 = new tangible.RefObject<Byte>(artifactPower.currentRankWithBonus);
                            setUpdateFieldValue(tempRef_CurrentRankWithBonus2, newRank);
                            artifactPower.currentRankWithBonus = tempRef_CurrentRankWithBonus2.refArgValue;

                            if (isEquipped()) {
                                var artifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(getItemData().artifactPowers.get(artifactPowerIndex).artifactPowerId, (byte) (newRank != 0 ? newRank - 1 : 0));

                                if (artifactPowerRank != null) {
                                    owner.applyArtifactPowerRank(this, artifactPowerRank, newRank != 0);
                                }
                            }
                        }
                    }

                    break;
                    case ItemEnchantmentType.ArtifactPowerBonusRankPicker:
                        if (slot.getValue() >= EnchantmentSlot.Sock1.getValue() && slot.getValue() <= EnchantmentSlot.Sock3.getValue() && getBonusData().GemRelicType[slot - EnchantmentSlot.Sock1] != -1) {
                            var artifactPowerPicker = CliDB.ArtifactPowerPickerStorage.get(enchant.EffectArg[i]);

                            if (artifactPowerPicker != null) {
                                var playerCondition = CliDB.PlayerConditionStorage.get(artifactPowerPicker.playerConditionID);

                                if (playerCondition == null || ConditionManager.isPlayerMeetingCondition(owner, playerCondition)) {
                                    for (var artifactPowerIndex = 0; artifactPowerIndex < getItemData().artifactPowers.size(); ++artifactPowerIndex) {
                                        var artifactPower = getItemData().artifactPowers.get(artifactPowerIndex);

                                        if (CliDB.ArtifactPowerStorage.get(artifactPower.artifactPowerId).Label == getBonusData().GemRelicType[slot - EnchantmentSlot.Sock1]) {
                                            var newRank = artifactPower.currentRankWithBonus;

                                            if (apply) {
                                                newRank += (byte) enchant.EffectPointsMin[i];
                                            } else {
                                                newRank -= (byte) enchant.EffectPointsMin[i];
                                            }

                                            artifactPower = getValues().modifyValue(getItemData()).modifyValue(getItemData().artifactPowers, artifactPowerIndex);
                                            tangible.RefObject<Byte> tempRef_CurrentRankWithBonus3 = new tangible.RefObject<Byte>(artifactPower.currentRankWithBonus);
                                            setUpdateFieldValue(tempRef_CurrentRankWithBonus3, newRank);
                                            artifactPower.currentRankWithBonus = tempRef_CurrentRankWithBonus3.refArgValue;

                                            if (isEquipped()) {
                                                var artifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(artifactPower.artifactPowerId, (byte) (newRank != 0 ? newRank - 1 : 0));

                                                if (artifactPowerRank != null) {
                                                    owner.applyArtifactPowerRank(this, artifactPowerRank, newRank != 0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void setExpiration(int expiration) {
        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().expiration), expiration);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final Item owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final ItemData itemMask = new itemData();

        public ValuesUpdateForPlayerWithMaskSender(Item owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), itemMask.getUpdateMask(), player);

            UpdateObject packet;
            tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }
}
