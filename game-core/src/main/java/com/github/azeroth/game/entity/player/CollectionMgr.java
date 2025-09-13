package com.github.azeroth.game.entity.player;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.networking.packet.AccountMountUpdate;
import com.github.azeroth.game.networking.packet.AccountTransmogUpdate;
import game.ConditionManager;
import game.WorldSession;
import game.datastorage.CliDB;
import game.datastorage.HeirloomRecord;
import game.datastorage.ItemModifiedAppearanceRecord;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class CollectionMgr {

    private static final HashMap<Integer, Integer> FACTIONSPECIFICMOUNTS = new HashMap<Integer, Integer>();
    private final WorldSession owner;

    private final HashMap<Integer, toyFlags> toys = new HashMap<Integer, toyFlags>();

    private final HashMap<Integer, HeirloomData> heirlooms = new HashMap<Integer, HeirloomData>();

    private final HashMap<Integer, MountStatusFlags> mounts = new HashMap<Integer, MountStatusFlags>();

    private final MultiMap<Integer, ObjectGuid> temporaryAppearances = new MultiMap<Integer, ObjectGuid>();

    private final HashMap<Integer, FavoriteAppearanceState> favoriteAppearances = new HashMap<Integer, FavoriteAppearanceState>();


    private final int[] playerClassByArmorSubclass = {PlayerClass.ClassMaskAllPlayable.getValue(), (1 << (playerClass.Priest.getValue() - 1)) | (1 << (playerClass.Mage.getValue() - 1)) | (1 << (playerClass.Warlock.getValue() - 1)), (1 << (playerClass.Rogue.getValue() - 1)) | (1 << (playerClass.Monk.getValue() - 1)) | (1 << (playerClass.Druid.getValue() - 1)) | (1 << (playerClass.DemonHunter.getValue() - 1)), (1 << (playerClass.Hunter.getValue() - 1)) | (1 << (playerClass.Shaman.getValue() - 1)), (1 << (playerClass.Warrior.getValue() - 1)) | (1 << (playerClass.Paladin.getValue() - 1)) | (1 << (playerClass.Deathknight.getValue() - 1)), playerClass.ClassMaskAllPlayable.getValue(), (1 << (playerClass.Warrior.getValue() - 1)) | (1 << (playerClass.Paladin.getValue() - 1)) | (1 << (playerClass.Shaman.getValue() - 1)), 1 << (playerClass.Paladin.getValue() - 1), 1 << (playerClass.Druid.getValue() - 1), 1 << (playerClass.Shaman.getValue() - 1), 1 << (playerClass.Deathknight.getValue() - 1), (1 << (playerClass.Paladin.getValue() - 1)) | (1 << (playerClass.Deathknight.getValue() - 1)) | (1 << (playerClass.Shaman.getValue() - 1)) | (1 << (playerClass.Druid.getValue() - 1))};

    private BitSet appearances;
    private BitSet transmogIllusions;

    public CollectionMgr(WorldSession owner) {
        owner = owner;
        appearances = new bitSet(0);
        transmogIllusions = new bitSet(0);
    }

    public static void loadMountDefinitions() {
        var oldMsTime = System.currentTimeMillis();

        var result = DB.World.query("SELECT spellId, otherFactionSpellId FROM mount_definitions");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 mount definitions. DB table `mount_definitions` is empty.");

            return;
        }

        do {
            var spellId = result.<Integer>Read(0);
            var otherFactionSpellId = result.<Integer>Read(1);

            if (global.getDB2Mgr().GetMount(spellId) == null) {
                Logs.SQL.error("Mount spell {0} defined in `mount_definitions` does not exist in mount.db2, skipped", spellId);

                continue;
            }

            if (otherFactionSpellId != 0 && global.getDB2Mgr().GetMount(otherFactionSpellId) == null) {
                Logs.SQL.error("otherFactionSpellId {0} defined in `mount_definitions` for spell {1} does not exist in mount.db2, skipped", otherFactionSpellId, spellId);

                continue;
            }

            FACTIONSPECIFICMOUNTS.put(spellId, otherFactionSpellId);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} mount definitions in {1} ms", FACTIONSPECIFICMOUNTS.size(), time.GetMSTimeDiffToNow(oldMsTime));
    }

    public final void loadToys() {
        for (var pair : toys.entrySet()) {
            owner.getPlayer().addToy(pair.getKey(), (int) pair.getValue());
        }
    }


    public final boolean addToy(int itemId, boolean isFavourite, boolean hasFanfare) {
        if (updateAccountToys(itemId, isFavourite, hasFanfare)) {
            if (owner.getPlayer() != null) {
                owner.getPlayer().addToy(itemId, (int) getToyFlags(isFavourite, hasFanfare).getValue());
            }

            return true;
        }

        return false;
    }

    public final void loadAccountToys(SQLResult result) {
        if (result.isEmpty()) {
            return;
        }

        do {
            var itemId = result.<Integer>Read(0);
            toys.put(itemId, getToyFlags(result.<Boolean>Read(1), result.<Boolean>Read(2)));
        } while (result.NextRow());
    }

    public final void saveAccountToys(SQLTransaction trans) {
        PreparedStatement stmt;

        for (var pair : toys.entrySet()) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.REP_ACCOUNT_TOYS);
            stmt.AddValue(0, owner.getBattlenetAccountId());
            stmt.AddValue(1, pair.getKey());
            stmt.AddValue(2, pair.getValue().hasFlag(toyFlags.favorite));
            stmt.AddValue(3, pair.getValue().hasFlag(toyFlags.HasFanfare));
            trans.append(stmt);
        }
    }


    public final void toySetFavorite(int itemId, boolean favorite) {
        if (!toys.containsKey(itemId)) {
            return;
        }

        if (favorite) {
            toys.put(itemId, toys.get(itemId).getValue() | toyFlags.favorite.getValue());
        } else {
            toys.put(itemId, toys.get(itemId).getValue() & ~toyFlags.favorite.getValue());
        }
    }


    public final void toyClearFanfare(int itemId) {
        if (!toys.containsKey(itemId)) {
            return;
        }

        toys.put(itemId, toys.get(itemId).getValue() & ~toyFlags.HasFanfare.getValue());
    }

    public final void onItemAdded(Item item) {
        if (global.getDB2Mgr().GetHeirloomByItemId(item.getEntry()) != null) {
            addHeirloom(item.getEntry(), 0);
        }

        addItemAppearance(item);
    }

    public final void loadAccountHeirlooms(SQLResult result) {
        if (result.isEmpty()) {
            return;
        }

        do {
            var itemId = result.<Integer>Read(0);
            var flags = HeirloomPlayerFlags.forValue(result.<Integer>Read(1));

            var heirloom = global.getDB2Mgr().GetHeirloomByItemId(itemId);

            if (heirloom == null) {
                continue;
            }

            int bonusId = 0;

            for (var upgradeLevel = heirloom.UpgradeItemID.length - 1; upgradeLevel >= 0; --upgradeLevel) {
                if ((flags.getValue() & (1 << upgradeLevel)) != 0) {
                    bonusId = heirloom.UpgradeItemBonusListID[upgradeLevel];

                    break;
                }
            }

            heirlooms.put(itemId, new HeirloomData(flags, bonusId));
        } while (result.NextRow());
    }

    public final void saveAccountHeirlooms(SQLTransaction trans) {
        PreparedStatement stmt;

        for (var heirloom : heirlooms.entrySet()) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.REP_ACCOUNT_HEIRLOOMS);
            stmt.AddValue(0, owner.getBattlenetAccountId());
            stmt.AddValue(1, heirloom.getKey());
            stmt.AddValue(2, (int) heirloom.getValue().flags);
            trans.append(stmt);
        }
    }


    public final int getHeirloomBonus(int itemId) {
        var data = heirlooms.get(itemId);

        if (data != null) {
            return data.bonusId;
        }

        return 0;
    }

    public final void loadHeirlooms() {
        for (var item : heirlooms.entrySet()) {
            owner.getPlayer().addHeirloom(item.getKey(), (int) item.getValue().flags);
        }
    }


    public final void addHeirloom(int itemId, HeirloomPlayerFlag flags) {
        if (updateAccountHeirlooms(itemId, flags)) {
            owner.getPlayer().addHeirloom(itemId, (int) flags.getValue());
        }
    }


    public final void upgradeHeirloom(int itemId, int castItem) {
        var player = owner.getPlayer();

        if (!player) {
            return;
        }

        var heirloom = global.getDB2Mgr().GetHeirloomByItemId(itemId);

        if (heirloom == null) {
            return;
        }

        var data = heirlooms.get(itemId);

        if (data == null) {
            return;
        }

        var flags = data.flags;
        int bonusId = 0;

        for (var upgradeLevel = 0; upgradeLevel < heirloom.UpgradeItemID.length; ++upgradeLevel) {
            if (heirloom.UpgradeItemID[upgradeLevel] == castItem) {
                flags |= HeirloomPlayerFlags.forValue(1 << upgradeLevel);
                bonusId = heirloom.UpgradeItemBonusListID[upgradeLevel];
            }
        }

        for (var item : player.getItemListByEntry(itemId, true)) {
            item.addBonuses(bonusId);
        }

        // Get heirloom offset to update only one part of dynamic field
        ArrayList<Integer> heirlooms = player.getActivePlayerData().heirlooms;
        var offset = heirlooms.indexOf(itemId);

        player.setHeirloomFlags(offset, (int) flags);
        data.flags = flags;
        data.bonusId = bonusId;
    }

    public final void checkHeirloomUpgrades(Item item) {
        var player = owner.getPlayer();

        if (!player) {
            return;
        }

        // Check already owned heirloom for upgrade kits
        var heirloom = global.getDB2Mgr().GetHeirloomByItemId(item.getEntry());

        if (heirloom != null) {
            var data = heirlooms.get(item.getEntry());

            if (data == null) {
                return;
            }

            // Check for heirloom pairs (normal - heroic, heroic - mythic)
            var heirloomItemId = heirloom.StaticUpgradedItemID;
            int newItemId = 0;
            HeirloomRecord heirloomDiff;

            while ((heirloomDiff = global.getDB2Mgr().GetHeirloomByItemId(heirloomItemId)) != null) {
                if (player.getItemByEntry(heirloomDiff.itemID)) {
                    newItemId = heirloomDiff.itemID;
                }

                var heirloomSub = global.getDB2Mgr().GetHeirloomByItemId(heirloomDiff.StaticUpgradedItemID);

                if (heirloomSub != null) {
                    heirloomItemId = heirloomSub.itemID;

                    continue;
                }

                break;
            }

            if (newItemId != 0) {
                ArrayList<Integer> heirlooms = player.getActivePlayerData().heirlooms;
                var offset = heirlooms.indexOf(item.getEntry());

                player.setHeirloom(offset, newItemId);
                player.setHeirloomFlags(offset, 0);

                heirlooms.remove(item.getEntry());
                heirlooms.put(newItemId, null);

                return;
            }

            var bonusListIDs = item.getBonusListIDs();

            for (var bonusId : bonusListIDs) {
                if (bonusId != data.bonusId) {
                    item.clearBonuses();

                    break;
                }
            }

            if (!bonusListIDs.contains(data.bonusId)) {
                item.addBonuses(data.bonusId);
            }
        }
    }

    public final void loadMounts() {
        for (var m : mounts.ToList()) {
            addMount(m.key, m.value);
        }
    }

    public final void loadAccountMounts(SQLResult result) {
        if (result.isEmpty()) {
            return;
        }

        do {
            var mountSpellId = result.<Integer>Read(0);
            var flags = MountStatusFlags.forValue(result.<Byte>Read(1));

            if (global.getDB2Mgr().GetMount(mountSpellId) == null) {
                continue;
            }

            mounts.put(mountSpellId, flags);
        } while (result.NextRow());
    }

    public final void saveAccountMounts(SQLTransaction trans) {
        for (var mount : mounts.entrySet()) {
            var stmt = DB.Login.GetPreparedStatement(LoginStatements.REP_ACCOUNT_MOUNTS);
            stmt.AddValue(0, owner.getBattlenetAccountId());
            stmt.AddValue(1, mount.getKey());
            stmt.AddValue(2, (byte) mount.getValue());
            trans.append(stmt);
        }
    }


    public final boolean addMount(int spellId, MountStatusFlags flags, boolean factionMount) {
        return addMount(spellId, flags, factionMount, false);
    }

    public final boolean addMount(int spellId, MountStatusFlags flags) {
        return addMount(spellId, flags, false, false);
    }

    public final boolean addMount(int spellId, MountStatusFlags flags, boolean factionMount, boolean learned) {
        var player = owner.getPlayer();

        if (!player) {
            return false;
        }

        var mount = global.getDB2Mgr().GetMount(spellId);

        if (mount == null) {
            return false;
        }

        var value = FACTIONSPECIFICMOUNTS.get(spellId);

        if (value != 0 && !factionMount) {
            addMount(value, flags, true, learned);
        }

        mounts.put(spellId, flags);

        // Mount condition only applies to using it, should still learn it.
        if (mount.playerConditionID != 0) {
            var playerCondition = CliDB.PlayerConditionStorage.get(mount.playerConditionID);

            if (playerCondition != null && !ConditionManager.isPlayerMeetingCondition(player, playerCondition)) {
                return false;
            }
        }

        if (!learned) {
            if (!factionMount) {
                sendSingleMountUpdate(spellId, flags);
            }

            if (!player.hasSpell(spellId)) {
                player.learnSpell(spellId, true);
            }
        }

        return true;
    }


    public final void mountSetFavorite(int spellId, boolean favorite) {
        if (!mounts.containsKey(spellId)) {
            return;
        }

        if (favorite) {
            mounts.put(spellId, mounts.get(spellId).getValue() | MountStatusFlags.isFavorite.getValue());
        } else {
            mounts.put(spellId, mounts.get(spellId).getValue() & ~MountStatusFlags.isFavorite.getValue());
        }

        sendSingleMountUpdate(spellId, mounts.get(spellId));
    }

    public final void loadItemAppearances() {
        var owner = owner.getPlayer();

        for (var blockValue : appearances.ToBlockRange()) {
            owner.addTransmogBlock(blockValue);
        }

        for (var value : temporaryAppearances.keySet()) {
            owner.addConditionalTransmog(value);
        }
    }

    public final void loadAccountItemAppearances(SQLResult knownAppearances, SQLResult favoriteAppearances) {
        if (!knownAppearances.isEmpty()) {
            var blocks = new int[1];

            do {
                var blobIndex = knownAppearances.<SHORT>Read(0);

                if (blobIndex >= blocks.length) {
                    tangible.RefObject<T[]> tempRef_blocks = new tangible.RefObject<T[]>(blocks);
                    Array.Resize(tempRef_blocks, blobIndex + 1);
                    blocks = tempRef_blocks.refArgValue;
                }

                blocks[blobIndex] = knownAppearances.<Integer>Read(1);
            } while (knownAppearances.NextRow());

            appearances = new bitSet(blocks);
        }

        if (!favoriteAppearances.isEmpty()) {
            do {
                favoriteAppearances.put(favoriteAppearances.<Integer>Read(0), FavoriteAppearanceState.Unchanged);
            } while (favoriteAppearances.NextRow());
        }

        // Static item appearances known by every player
        int[] hiddenAppearanceItems = {134110, 134111, 134112, 168659, 142503, 142504, 168665, 158329, 143539, 168664};

        for (var hiddenItem : hiddenAppearanceItems) {
            var hiddenAppearance = global.getDB2Mgr().getItemModifiedAppearance(hiddenItem, 0);

            //ASSERT(hiddenAppearance);
            if (appearances.length <= hiddenAppearance.id) {
                appearances.length = (int) hiddenAppearance.id + 1;
            }

            appearances.set((int) hiddenAppearance.id, true);
        }
    }

    public final void saveAccountItemAppearances(SQLTransaction trans) {
        PreparedStatement stmt;
        short blockIndex = 0;

        for (var blockValue : appearances.ToBlockRange()) {
            if (blockValue != 0) // this table is only appended/bits are set (never cleared) so don't save empty blocks
            {
                stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BNET_ITEM_APPEARANCES);
                stmt.AddValue(0, owner.getBattlenetAccountId());
                stmt.AddValue(1, blockIndex);
                stmt.AddValue(2, blockValue);
                trans.append(stmt);
            }

            ++blockIndex;
        }

        for (var key : favoriteAppearances.keySet()) {
            var appearanceState = favoriteAppearances.get(key);

            switch (appearanceState) {
                case New:
                    stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BNET_ITEM_FAVORITE_APPEARANCE);
                    stmt.AddValue(0, owner.getBattlenetAccountId());
                    stmt.AddValue(1, key);
                    trans.append(stmt);
                    favoriteAppearances.put(key, FavoriteAppearanceState.Unchanged);

                    break;
                case Removed:
                    stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_BNET_ITEM_FAVORITE_APPEARANCE);
                    stmt.AddValue(0, owner.getBattlenetAccountId());
                    stmt.AddValue(1, key);
                    trans.append(stmt);
                    favoriteAppearances.remove(key);

                    break;
                case Unchanged:
                    break;
            }
        }
    }

    public final void addItemAppearance(Item item) {
        if (!item.isSoulBound()) {
            return;
        }

        var itemModifiedAppearance = item.getItemModifiedAppearance();

        if (!canAddAppearance(itemModifiedAppearance)) {
            return;
        }

        if (item.isBOPTradeable() || item.isRefundable()) {
            addTemporaryAppearance(item.getGUID(), itemModifiedAppearance);

            return;
        }

        addItemAppearance(itemModifiedAppearance);
    }


    public final void addItemAppearance(int itemId) {
        addItemAppearance(itemId, 0);
    }

    public final void addItemAppearance(int itemId, int appearanceModId) {
        var itemModifiedAppearance = global.getDB2Mgr().getItemModifiedAppearance(itemId, appearanceModId);

        if (!canAddAppearance(itemModifiedAppearance)) {
            return;
        }

        addItemAppearance(itemModifiedAppearance);
    }

    public final void removeTemporaryAppearance(Item item) {
        var itemModifiedAppearance = item.getItemModifiedAppearance();

        if (itemModifiedAppearance == null) {
            return;
        }

        var guid = temporaryAppearances.get(itemModifiedAppearance.id);

        if (guid.isEmpty()) {
            return;
        }

        guid.remove(item.getGUID());

        if (guid.isEmpty()) {
            owner.getPlayer().removeConditionalTransmog(itemModifiedAppearance.id);
            temporaryAppearances.remove(itemModifiedAppearance.id);
        }
    }


//	public (bool PermAppearance, bool TempAppearance) hasItemAppearance(uint itemModifiedAppearanceId)
//		{
//			if (itemModifiedAppearanceId < appearances.count && appearances.Get((int)itemModifiedAppearanceId))
//				return (true, false);
//
//			if (temporaryAppearances.ContainsKey(itemModifiedAppearanceId))
//				return (true, true);
//
//			return (false, false);
//		}


    public final ArrayList<ObjectGuid> getItemsProvidingTemporaryAppearance(int itemModifiedAppearanceId) {
        return temporaryAppearances.get(itemModifiedAppearanceId);
    }


    public final ArrayList<Integer> getAppearanceIds() {
        ArrayList<Integer> appearances = new ArrayList<>();

        for (int id : appearances) {
            appearances.add((int) CliDB.ItemModifiedAppearanceStorage.get(id).ItemAppearanceID);
        }

        return appearances;
    }


    public final void setAppearanceIsFavorite(int itemModifiedAppearanceId, boolean apply) {
        var apperanceState = favoriteAppearances.get(itemModifiedAppearanceId);

        if (apply) {
            if (!favoriteAppearances.containsKey(itemModifiedAppearanceId)) {
                favoriteAppearances.put(itemModifiedAppearanceId, FavoriteAppearanceState.New);
            } else if (apperanceState == FavoriteAppearanceState.removed) {
                apperanceState = FavoriteAppearanceState.Unchanged;
            } else {
                return;
            }
        } else if (favoriteAppearances.containsKey(itemModifiedAppearanceId)) {
            if (apperanceState == FavoriteAppearanceState.New) {
                favoriteAppearances.remove(itemModifiedAppearanceId);
            } else {
                apperanceState = FavoriteAppearanceState.removed;
            }
        } else {
            return;
        }

        favoriteAppearances.put(itemModifiedAppearanceId, apperanceState);

        AccountTransmogUpdate accountTransmogUpdate = new AccountTransmogUpdate();
        accountTransmogUpdate.isFullUpdate = false;
        accountTransmogUpdate.isSetFavorite = apply;
        accountTransmogUpdate.favoriteAppearances.add(itemModifiedAppearanceId);

        owner.sendPacket(accountTransmogUpdate);
    }

    public final void sendFavoriteAppearances() {
        AccountTransmogUpdate accountTransmogUpdate = new AccountTransmogUpdate();
        accountTransmogUpdate.isFullUpdate = true;

        for (var pair : favoriteAppearances.entrySet()) {
            if (pair.getValue() != FavoriteAppearanceState.removed) {
                accountTransmogUpdate.favoriteAppearances.add(pair.getKey());
            }
        }

        owner.sendPacket(accountTransmogUpdate);
    }


    public final void addTransmogSet(int transmogSetId) {
        var items = global.getDB2Mgr().GetTransmogSetItems(transmogSetId);

        if (items.isEmpty()) {
            return;
        }

        for (var item : items) {
            var itemModifiedAppearance = CliDB.ItemModifiedAppearanceStorage.get(item.itemModifiedAppearanceID);

            if (itemModifiedAppearance == null) {
                continue;
            }

            addItemAppearance(itemModifiedAppearance);
        }
    }

    public final void loadTransmogIllusions() {
        var owner = owner.getPlayer();

        for (var blockValue : transmogIllusions.ToBlockRange()) {
            owner.addIllusionBlock(blockValue);
        }
    }

    public final void loadAccountTransmogIllusions(SQLResult knownTransmogIllusions) {
        var blocks = new int[7];

        if (!knownTransmogIllusions.isEmpty()) {
            do {
                var blobIndex = knownTransmogIllusions.<SHORT>Read(0);

                if (blobIndex >= blocks.length) {
                    tangible.RefObject<T[]> tempRef_blocks = new tangible.RefObject<T[]>(blocks);
                    Array.Resize(tempRef_blocks, blobIndex + 1);
                    blocks = tempRef_blocks.refArgValue;
                }

                blocks[blobIndex] = knownTransmogIllusions.<Integer>Read(1);
            } while (knownTransmogIllusions.NextRow());
        }

        transmogIllusions = new bitSet(blocks);

        // Static illusions known by every player
        short[] defaultIllusions = {3, 13, 22, 23, 34, 43, 44};

        for (var illusionId : defaultIllusions) {
            transmogIllusions.set(illusionId, true);
        }
    }

    public final void saveAccountTransmogIllusions(SQLTransaction trans) {
        short blockIndex = 0;

        for (var blockValue : transmogIllusions.ToBlockRange()) {
            if (blockValue != 0) // this table is only appended/bits are set (never cleared) so don't save empty blocks
            {
                var stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BNET_TRANSMOG_ILLUSIONS);
                stmt.AddValue(0, owner.getBattlenetAccountId());
                stmt.AddValue(1, blockIndex);
                stmt.AddValue(2, blockValue);
                trans.append(stmt);
            }

            ++blockIndex;
        }
    }


    public final void addTransmogIllusion(int transmogIllusionId) {
        var owner = owner.getPlayer();

        if (transmogIllusions.count <= transmogIllusionId) {
            var numBlocks = (int) (transmogIllusions.count << 2);
            transmogIllusions.length = transmogIllusionId + 1;
            numBlocks = (int) (transmogIllusions.count << 2) - numBlocks;

            while (numBlocks-- != 0) {
                owner.addIllusionBlock(0);
            }
        }

        transmogIllusions.set((int) transmogIllusionId, true);
        var blockIndex = transmogIllusionId / 32;
        var bitIndex = transmogIllusionId % 32;

        owner.addIllusionFlag(blockIndex, 1 << bitIndex);
    }


    public final boolean hasTransmogIllusion(int transmogIllusionId) {
        return transmogIllusionId < transmogIllusions.count && transmogIllusions.Get((int) transmogIllusionId);
    }


    public final boolean hasToy(int itemId) {
        return toys.containsKey(itemId);
    }


    public final HashMap<Integer, toyFlags> getAccountToys() {
        return toys;
    }


    public final HashMap<Integer, HeirloomData> getAccountHeirlooms() {
        return heirlooms;
    }


    public final HashMap<Integer, MountStatusFlags> getAccountMounts() {
        return mounts;
    }


    private boolean updateAccountToys(int itemId, boolean isFavourite, boolean hasFanfare) {
        if (toys.containsKey(itemId)) {
            return false;
        }

        toys.put(itemId, getToyFlags(isFavourite, hasFanfare));

        return true;
    }

    private ToyFlags getToyFlags(boolean isFavourite, boolean hasFanfare) {
        var flags = toyFlags.NONE;

        if (isFavourite) {
            flags = toyFlags.forValue(flags.getValue() | toyFlags.favorite.getValue());
        }

        if (hasFanfare) {
            flags = toyFlags.forValue(flags.getValue() | toyFlags.HasFanfare.getValue());
        }

        return flags;
    }


    private boolean updateAccountHeirlooms(int itemId, HeirloomPlayerFlag flags) {
        if (heirlooms.containsKey(itemId)) {
            return false;
        }

        heirlooms.put(itemId, new HeirloomData(flags));

        return true;
    }


    private void sendSingleMountUpdate(int spellId, MountStatusFlags mountStatusFlags) {
        var player = owner.getPlayer();

        if (!player) {
            return;
        }

        AccountMountUpdate mountUpdate = new AccountMountUpdate();
        mountUpdate.isFullUpdate = false;
        mountUpdate.mounts.put(spellId, mountStatusFlags);
        player.sendPacket(mountUpdate);
    }

    private boolean canAddAppearance(ItemModifiedAppearanceRecord itemModifiedAppearance) {
        if (itemModifiedAppearance == null) {
            return false;
        }

        if (itemModifiedAppearance.TransmogSourceTypeEnum == 6 || itemModifiedAppearance.TransmogSourceTypeEnum == 9) {
            return false;
        }

        if (!CliDB.ItemSearchNameStorage.containsKey(itemModifiedAppearance.itemID)) {
            return false;
        }

        var itemTemplate = global.getObjectMgr().getItemTemplate(itemModifiedAppearance.itemID);

        if (itemTemplate == null) {
            return false;
        }

        if (!owner.getPlayer()) {
            return false;
        }

        if (owner.getPlayer().canUseItem(itemTemplate) != InventoryResult.Ok) {
            return false;
        }

        if (itemTemplate.hasFlag(ItemFlags2.NoSourceForItemVisual) || itemTemplate.getQuality() == itemQuality.artifact) {
            return false;
        }

        switch (itemTemplate.getClass()) {
            case Weapon: {
                if (!(boolean) (owner.getPlayer().getWeaponProficiency() & (1 << (int) itemTemplate.getSubClass()))) {
                    return false;
                }

                if (itemTemplate.getSubClass() == ItemSubClassWeapon.Exotic.getValue() || itemTemplate.getSubClass() == ItemSubClassWeapon.Exotic2.getValue() || itemTemplate.getSubClass() == ItemSubClassWeapon.Miscellaneous.getValue() || itemTemplate.getSubClass() == ItemSubClassWeapon.Thrown.getValue() || itemTemplate.getSubClass() == ItemSubClassWeapon.Spear.getValue() || itemTemplate.getSubClass() == ItemSubClassWeapon.FishingPole.getValue()) {
                    return false;
                }

                break;
            }
            case Armor: {
                switch (itemTemplate.getInventoryType()) {
                    case Body:
                    case Shield:
                    case Cloak:
                    case Tabard:
                    case Holdable:
                        break;
                    case Head:
                    case Shoulders:
                    case Chest:
                    case Waist:
                    case Legs:
                    case Feet:
                    case Wrists:
                    case Hands:
                    case Robe:
                        if (itemTemplate.getSubClass() == ItemSubClassArmor.Miscellaneous.getValue()) {
                            return false;
                        }

                        break;
                    default:
                        return false;
                }

                if (itemTemplate.getInventoryType() != inventoryType.Cloak) {
                    if (!(boolean) (_playerClassByArmorSubclass[itemTemplate.getSubClass()] & owner.getPlayer().getClassMask())) {
                        return false;
                    }
                }

                break;
            }
            default:
                return false;
        }

        if (itemTemplate.getQuality().getValue() < itemQuality.Uncommon.getValue()) {
            if (!itemTemplate.hasFlag(ItemFlags2.IgnoreQualityForItemVisualSource) || !itemTemplate.hasFlag(ItemFlags3.ActsAsTransmogHiddenVisualOption)) {
                return false;
            }
        }

        return itemModifiedAppearance.id >= appearances.count || !appearances.Get((int) itemModifiedAppearance.id);
    }

    //todo  check this
    private void addItemAppearance(ItemModifiedAppearanceRecord itemModifiedAppearance) {
        var owner = owner.getPlayer();

        if (appearances.count <= itemModifiedAppearance.id) {
            var numBlocks = (int) (appearances.count << 2);
            appearances.length = itemModifiedAppearance.id + 1;
            numBlocks = (int) (appearances.count << 2) - numBlocks;

            while (numBlocks-- != 0) {
                owner.addTransmogBlock(0);
            }
        }

        appearances.set((int) itemModifiedAppearance.id, true);
        var blockIndex = itemModifiedAppearance.Id / 32;
        var bitIndex = itemModifiedAppearance.Id % 32;
        owner.addTransmogFlag(blockIndex, 1 << bitIndex);
        var temporaryAppearance = temporaryAppearances.get(itemModifiedAppearance.id); // make a copy

        if (!temporaryAppearance.isEmpty()) {
            owner.removeConditionalTransmog(itemModifiedAppearance.id);
            temporaryAppearances.remove(itemModifiedAppearance.id);
        }

        var item = CliDB.ItemStorage.get(itemModifiedAppearance.itemID);

        if (item != null) {
            var transmogSlot = item.ItemTransmogrificationSlots[(int) item.inventoryType];

            if (transmogSlot >= 0) {
                owner.getPlayer().updateCriteria(CriteriaType.LearnAnyTransmogInSlot, (long) transmogSlot, itemModifiedAppearance.id);
            }
        }

        var sets = global.getDB2Mgr().GetTransmogSetsForItemModifiedAppearance(itemModifiedAppearance.id);

        for (var set : sets) {
            if (isSetCompleted(set.id)) {
                owner.getPlayer().updateCriteria(CriteriaType.CollectTransmogSetFromGroup, set.TransmogSetGroupID);
            }
        }
    }

    private void addTemporaryAppearance(ObjectGuid itemGuid, ItemModifiedAppearanceRecord itemModifiedAppearance) {
        var itemsWithAppearance = temporaryAppearances.get(itemModifiedAppearance.id);

        if (itemsWithAppearance.isEmpty()) {
            owner.getPlayer().addConditionalTransmog(itemModifiedAppearance.id);
        }

        itemsWithAppearance.add(itemGuid);
    }


    private boolean isSetCompleted(int transmogSetId) {
        var transmogSetItems = global.getDB2Mgr().GetTransmogSetItems(transmogSetId);

        if (transmogSetItems.isEmpty()) {
            return false;
        }

        var knownPieces = new int[EquipmentSlot.End];

        for (var i = 0; i < EquipmentSlot.End; ++i) {
            knownPieces[i] = -1;
        }

        for (var transmogSetItem : transmogSetItems) {
            var itemModifiedAppearance = CliDB.ItemModifiedAppearanceStorage.get(transmogSetItem.itemModifiedAppearanceID);

            if (itemModifiedAppearance == null) {
                continue;
            }

            var item = CliDB.ItemStorage.get(itemModifiedAppearance.itemID);

            if (item == null) {
                continue;
            }

            var transmogSlot = item.ItemTransmogrificationSlots[(int) item.inventoryType];

            if (transmogSlot < 0 || knownPieces[transmogSlot] == 1) {
                continue;
            }


            var(hasAppearance, isTemporary) = hasItemAppearance(transmogSetItem.itemModifiedAppearanceID);

            knownPieces[transmogSlot] = (hasAppearance && !isTemporary) ? 1 : 0;
        }

        return !knownPieces.contains(0);
    }
}
