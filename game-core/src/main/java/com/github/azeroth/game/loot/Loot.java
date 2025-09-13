package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Loot {
    private final ArrayList<ObjectGuid> playersLooting = new ArrayList<>();
    private final MultiMap<ObjectGuid, NotNormalLootItem> playerFFAItems = new MultiMap<ObjectGuid, NotNormalLootItem>();
    private final LootMethod lootMethod;
    private final HashMap<Integer, LootRoll> rolls = new HashMap<Integer, LootRoll>(); // used if an item is under rolling
    private final ArrayList<ObjectGuid> allowedLooters = new ArrayList<>();
    // Loot GUID
    private final ObjectGuid guid;
    private final ObjectGuid owner; // The WorldObject that holds this loot
    private final ObjectGuid lootMaster;
    public ArrayList<LootItem> items = new ArrayList<>();
    public int gold;
    public byte unlootedCount;
    public ObjectGuid roundRobinPlayer = ObjectGuid.EMPTY; // GUID of the player having the Round-Robin ownership for the loot. If 0, round robin owner has released.
    public LootType loot_type = LootType.values()[0]; // required for achievement system    private ItemContext itemContext = itemContext.values()[0];
    private boolean wasOpened; // true if at least one player received the loot content
    private int dungeonEncounterId;
    public loot(Map map, ObjectGuid owner, LootType type, PlayerGroup group) {
        loot_type = type;
        guid = map ? ObjectGuid.create(HighGuid.LootObject, map.getId(), 0, map.generateLowGuid(HighGuid.LootObject)) : ObjectGuid.Empty;
        owner = owner;
        itemContext = itemContext.NONE;
        lootMethod = group != null ? group.getLootMethod() : lootMethod.FreeForAll;
        lootMaster = group != null ? group.getMasterLooterGuid() : ObjectGuid.Empty;
    }

    // Inserts the item into the loot (called by LootTemplate processors)
    public final void addItem(LootStoreItem item) {
        var proto = global.getObjectMgr().getItemTemplate(item.itemid);

        if (proto == null) {
            return;
        }

        var count = RandomUtil.URand(item.mincount, item.maxcount);
        var stacks = (int) (count / proto.getMaxStackSize() + ((boolean) (count % proto.getMaxStackSize()) ? 1 : 0));

        for (int i = 0; i < stacks && items.size() < SharedConst.MaxNRLootItems; ++i) {
            LootItem generatedLoot = new LootItem(item);
            generatedLoot.context = itemContext;
            generatedLoot.count = (byte) Math.min(count, proto.getMaxStackSize());
            generatedLoot.lootListId = (int) items.size();

            if (itemContext != 0) {
                var bonusListIDs = global.getDB2Mgr().GetDefaultItemBonusTree(generatedLoot.itemid, itemContext);
                generatedLoot.bonusListIDs.addAll(bonusListIDs);
            }

            items.add(generatedLoot);
            count -= proto.getMaxStackSize();
        }
    }

    public final boolean autoStore(Player player, byte bag, byte slot, boolean broadcast) {
        return autoStore(player, bag, slot, broadcast, false);
    }

    public final boolean autoStore(Player player, byte bag, byte slot) {
        return autoStore(player, bag, slot, false, false);
    }

    public final boolean autoStore(Player player, byte bag, byte slot, boolean broadcast, boolean createdByPlayer) {
        var allLooted = true;

        for (int i = 0; i < items.size(); ++i) {
            NotNormalLootItem ffaitem;
            tangible.OutObject<NotNormalLootItem> tempOut_ffaitem = new tangible.OutObject<NotNormalLootItem>();
            var lootItem = lootItemInSlot(i, player, tempOut_ffaitem);
            ffaitem = tempOut_ffaitem.outArgValue;

            if (lootItem == null || lootItem.is_looted) {
                continue;
            }

            if (!lootItem.hasAllowedLooter(getGUID())) {
                continue;
            }

            if (lootItem.is_blocked) {
                continue;
            }

            // dont allow protected item to be looted by someone else
            if (!lootItem.rollWinnerGUID.isEmpty() && ObjectGuid.opNotEquals(lootItem.rollWinnerGUID, getGUID())) {
                continue;
            }

            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canStoreNewItem(bag, slot, dest, lootItem.itemid, lootItem.count);

            if (msg != InventoryResult.Ok && slot != ItemConst.NullSlot) {
                msg = player.canStoreNewItem(bag, ItemConst.NullSlot, dest, lootItem.itemid, lootItem.count);
            }

            if (msg != InventoryResult.Ok && bag != ItemConst.NullBag) {
                msg = player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, lootItem.itemid, lootItem.count);
            }

            if (msg != InventoryResult.Ok) {
                player.sendEquipError(msg, null, null, lootItem.itemid);
                allLooted = false;

                continue;
            }

            if (ffaitem != null) {
                ffaitem.is_looted = true;
            }

            if (!lootItem.freeforall) {
                lootItem.is_looted = true;
            }

            --unlootedCount;

            var pItem = player.storeNewItem(dest, lootItem.itemid, true, lootItem.randomBonusListId, null, lootItem.context, lootItem.bonusListIDs);
            player.sendNewItem(pItem, lootItem.count, false, createdByPlayer, broadcast);
            player.applyItemLootedSpell(pItem, true);
        }

        return allLooted;
    }

    public final LootItem getItemInSlot(int lootListId) {
        if (lootListId < items.size()) {
            return items.get((int) lootListId);
        }

        return null;
    }

    public final boolean fillLoot(int lootId, LootStore store, Player lootOwner, boolean personal, boolean noEmptyError, LootModes lootMode) {
        return fillLoot(lootId, store, lootOwner, personal, noEmptyError, lootMode, 0);
    }

    // Calls processor of corresponding LootTemplate (which handles everything including references)

    public final boolean fillLoot(int lootId, LootStore store, Player lootOwner, boolean personal, boolean noEmptyError) {
        return fillLoot(lootId, store, lootOwner, personal, noEmptyError, LootModes.Default, 0);
    }

    public final boolean fillLoot(int lootId, LootStore store, Player lootOwner, boolean personal) {
        return fillLoot(lootId, store, lootOwner, personal, false, LootModes.Default, 0);
    }

    public final boolean fillLoot(int lootId, LootStore store, Player lootOwner, boolean personal, boolean noEmptyError, LootModes lootMode, ItemContext context) {
        // Must be provided
        if (lootOwner == null) {
            return false;
        }

        var tab = store.getLootFor(lootId);

        if (tab == null) {
            if (!noEmptyError) {
                Logs.SQL.error("Table '{0}' loot id #{1} used but it doesn't have records.", store.getName(), lootId);
            }

            return false;
        }

        itemContext = context;

        tab.process(this, store.isRatesAllowed(), (byte) lootMode.getValue(), (byte) 0); // Processing is done there, callback via loot.addItem()

        // Setting access rights for group loot case
        var group = lootOwner.getGroup();

        if (!personal && group != null) {
            if (loot_type == LootType.Corpse) {
                roundRobinPlayer = lootOwner.getGUID();
            }

            for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                var player = refe.getSource();

                if (player) // should actually be looted object instead of lootOwner but looter has to be really close so doesnt really matter
                {
                    if (player.isAtGroupRewardDistance(lootOwner)) {
                        fillNotNormalLootFor(player);
                    }
                }
            }

            for (var item : items) {
                if (!item.follow_loot_rules || item.freeforall) {
                    continue;
                }

                var proto = global.getObjectMgr().getItemTemplate(item.itemid);

                if (proto != null) {
                    if (proto.getQuality().getValue() < group.getLootThreshold().getValue()) {
                        item.is_underthreshold = true;
                    } else {
                        switch (lootMethod) {
                            case MasterLoot:
                            case GroupLoot:
                            case NeedBeforeGreed: {
                                item.is_blocked = true;

                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            }
        }
        // ... for personal loot
        else {
            fillNotNormalLootFor(lootOwner);
        }

        return true;
    }

    public final void update() {
        for (var pair : rolls.ToList()) {
            if (pair.value.updateRoll()) {
                rolls.remove(pair.key);
            }
        }
    }

    public final void fillNotNormalLootFor(Player player) {
        var plguid = player.getGUID();
        allowedLooters.add(plguid);

        ArrayList<NotNormalLootItem> ffaItems = new ArrayList<>();

        for (var item : items) {
            if (!item.allowedForPlayer(player, this)) {
                continue;
            }

            item.addAllowedLooter(player);

            if (item.freeforall) {
                ffaItems.add(new NotNormalLootItem((byte) item.lootListId));
                ++unlootedCount;
            } else if (!item.is_counted) {
                item.is_counted = true;
                ++unlootedCount;
            }
        }

        if (!ffaItems.isEmpty()) {
            playerFFAItems.set(player.getGUID(), ffaItems);
        }
    }

    public final void notifyItemRemoved(byte lootListId, Map map) {
        // notify all players that are looting this that the item was removed
        // convert the index to the slot the player sees
        for (var i = 0; i < playersLooting.size(); ++i) {
            var item = items.get(lootListId);

            if (!item.getAllowedLooters().contains(playersLooting.get(i))) {
                continue;
            }

            var player = global.getObjAccessor().getPlayer(map, playersLooting.get(i));

            if (player) {
                player.sendNotifyLootItemRemoved(getGUID(), getOwnerGUID(), lootListId);
            } else {
                playersLooting.remove(i);
            }
        }
    }

    public final void notifyMoneyRemoved(Map map) {
        // notify all players that are looting this that the money was removed
        for (var i = 0; i < playersLooting.size(); ++i) {
            var player = global.getObjAccessor().getPlayer(map, playersLooting.get(i));

            if (player != null) {
                player.sendNotifyLootMoneyRemoved(getGUID());
            } else {
                playersLooting.remove(i);
            }
        }
    }

    public final void onLootOpened(Map map, ObjectGuid looter) {
        addLooter(looter);

        if (!wasOpened) {
            wasOpened = true;

            if (lootMethod == lootMethod.GroupLoot || lootMethod == lootMethod.NeedBeforeGreed) {
                short maxEnchantingSkill = 0;

                for (var allowedLooterGuid : allowedLooters) {
                    var allowedLooter = global.getObjAccessor().getPlayer(map, allowedLooterGuid);

                    if (allowedLooter != null) {
                        maxEnchantingSkill = Math.max(maxEnchantingSkill, allowedLooter.getSkillValue(SkillType.Enchanting));
                    }
                }

                for (int lootListId = 0; lootListId < items.size(); ++lootListId) {
                    var item = items.get((int) lootListId);

                    if (!item.is_blocked) {
                        continue;
                    }

                    LootRoll lootRoll = new LootRoll();
                    var inserted = rolls.TryAdd(lootListId, lootRoll);

                    if (!lootRoll.tryToStart(map, this, lootListId, maxEnchantingSkill)) {
                        rolls.remove(lootListId);
                    }
                }
            } else if (lootMethod == lootMethod.MasterLoot) {
                if (Objects.equals(looter, lootMaster)) {
                    var lootMaster = global.getObjAccessor().getPlayer(map, looter);

                    if (lootMaster != null) {
                        MasterLootCandidateList masterLootCandidateList = new MasterLootCandidateList();
                        masterLootCandidateList.lootObj = getGUID();
                        masterLootCandidateList.players = allowedLooters;
                        lootMaster.sendPacket(masterLootCandidateList);
                    }
                }
            }
        }
    }

    public final boolean hasAllowedLooter(ObjectGuid looter) {
        return allowedLooters.contains(looter);
    }

    public final void generateMoneyLoot(int minAmount, int maxAmount) {
        if (maxAmount > 0) {
            if (maxAmount <= minAmount) {
                gold = (int) (maxAmount * WorldConfig.getFloatValue(WorldCfg.RateDropMoney));
            } else if ((maxAmount - minAmount) < 32700) {
                gold = (int) (RandomUtil.URand(minAmount, maxAmount) * WorldConfig.getFloatValue(WorldCfg.RateDropMoney));
            } else {
                gold = (int) (RandomUtil.URand(minAmount >>> 8, maxAmount >>> 8) * WorldConfig.getFloatValue(WorldCfg.RateDropMoney)) << 8;
            }
        }
    }

    public final LootItem lootItemInSlot(int lootSlot, Player player) {
        tangible.OutObject<NotNormalLootItem> tempOut__ = new tangible.OutObject<NotNormalLootItem>();
        var tempVar = lootItemInSlot(lootSlot, player, tempOut__);
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    public final LootItem lootItemInSlot(int lootListId, Player player, tangible.OutObject<NotNormalLootItem> ffaItem) {
        ffaItem.outArgValue = null;

        if (lootListId >= items.size()) {
            return null;
        }

        var item = items.get((int) lootListId);
        var is_looted = item.is_looted;

        if (item.freeforall) {
            var itemList = playerFFAItems.get(player.getGUID());

            if (itemList != null) {
                for (var notNormalLootItem : itemList) {
                    if (notNormalLootItem.lootListId == lootListId) {
                        is_looted = notNormalLootItem.is_looted;
                        ffaItem.outArgValue = notNormalLootItem;

                        break;
                    }
                }
            }
        }

        if (is_looted) {
            return null;
        }

        return item;
    }

    // return true if there is any item that is lootable for any player (not quest item, FFA or conditional)
    public final boolean hasItemForAll() {
        // Gold is always lootable
        if (gold != 0) {
            return true;
        }

        for (var item : items) {
            if (!item.is_looted && item.follow_loot_rules && !item.freeforall && item.conditions.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    // return true if there is any FFA, quest or conditional item for the player.
    public final boolean hasItemFor(Player player) {
        // quest items
        for (var lootItem : items) {
            if (!lootItem.is_looted && !lootItem.follow_loot_rules && lootItem.getAllowedLooters().contains(player.getGUID())) {
                return true;
            }
        }

        var ffaItems = getPlayerFFAItems().get(player.getGUID());

        if (ffaItems != null) {
            var hasFfaItem = ffaItems.Any(ffaItem -> !ffaItem.is_looted);

            if (hasFfaItem) {
                return true;
            }
        }

        return false;
    }

    // return true if there is any item over the group threshold (i.e. not underthreshold).
    public final boolean hasOverThresholdItem() {
        for (byte i = 0; i < items.size(); ++i) {
            if (!items.get(i).is_looted && !items.get(i).is_underthreshold && !items.get(i).freeforall) {
                return true;
            }
        }

        return false;
    }

    public final void buildLootResponse(LootResponse packet, Player viewer) {
        packet.coins = gold;

        for (var item : items) {
            var uiType = item.getUiTypeForPlayer(viewer, this);

            if (!uiType != null) {
                continue;
            }

            LootItemData lootItem = new lootItemData();
            lootItem.lootListID = (byte) item.lootListId;
            lootItem.UIType = uiType;
            lootItem.quantity = item.count;
            lootItem.loot = new itemInstance(item);
            packet.items.add(lootItem);
        }
    }

    public final void notifyLootList(Map map) {
        LootList lootList = new LootList();

        lootList.owner = getOwnerGUID();
        lootList.lootObj = getGUID();

        if (getLootMethod() == lootMethod.MasterLoot && hasOverThresholdItem()) {
            lootList.master = getLootMasterGUID();
        }

        if (!roundRobinPlayer.isEmpty()) {
            lootList.roundRobinWinner = roundRobinPlayer;
        }

        lootList.write();

        for (var allowedLooterGuid : allowedLooters) {
            var allowedLooter = global.getObjAccessor().getPlayer(map, allowedLooterGuid);

            if (allowedLooter != null) {
                allowedLooter.sendPacket(lootList);
            }
        }
    }

    public final boolean isLooted() {
        return gold == 0 && unlootedCount == 0;
    }

    public final void addLooter(ObjectGuid guid) {
        playersLooting.add(guid);
    }

    public final void removeLooter(ObjectGuid guid) {
        playersLooting.remove(guid);
    }

    public final ObjectGuid getGUID() {
        return guid;
    }

    public final ObjectGuid getOwnerGUID() {
        return owner;
    }

    public final ItemContext getItemContext() {
        return itemContext;
    }

    public final void setItemContext(ItemContext context) {
        itemContext = context;
    }

    public final LootMethod getLootMethod() {
        return lootMethod;
    }

    public final ObjectGuid getLootMasterGUID() {
        return lootMaster;
    }

    public final int getDungeonEncounterId() {
        return dungeonEncounterId;
    }

    public final void setDungeonEncounterId(int dungeonEncounterId) {
        dungeonEncounterId = dungeonEncounterId;
    }

    public final MultiMap<ObjectGuid, NotNormalLootItem> getPlayerFFAItems() {
        return playerFFAItems;
    }


}
