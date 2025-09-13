package game;


import com.github.azeroth.game.battlepet.BattlePetMgr;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.mail.MailDraft;
import com.github.azeroth.game.mail.MailReceiver;
import com.github.azeroth.game.mail.MailSender;
import com.github.azeroth.game.listener.interfaces.iauctionhouse.IAuctionHouseOnAcutionRemove;
import com.github.azeroth.game.listener.interfaces.iauctionhouse.IAuctionHouseOnAuctionAdd;
import com.github.azeroth.game.listener.interfaces.iauctionhouse.IAuctionHouseOnAuctionExpire;
import com.github.azeroth.game.listener.interfaces.iauctionhouse.IAuctionHouseOnAuctionSuccessful;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class AuctionHouseObject {
    private final AuctionHouseRecord auctionHouse;
    private final SortedList<Integer, AuctionPosting> itemsByAuctionId = new SortedList<Integer, AuctionPosting>(); // ordered for replicate
    private final TreeMap<AuctionsBucketKey, AuctionsBucketData> buckets = new TreeMap<AuctionsBucketKey, AuctionsBucketData>(); // ordered for search by itemid only
    private final HashMap<ObjectGuid, CommodityQuote> commodityQuotes = new HashMap<ObjectGuid, CommodityQuote>();
    private final MultiMap<ObjectGuid, Integer> playerOwnedAuctions = new MultiMap<ObjectGuid, Integer>();
    private final MultiMap<ObjectGuid, Integer> playerBidderAuctions = new MultiMap<ObjectGuid, Integer>();

    // Map of throttled players for GetAll, and throttle expiry time
    // Stored here, rather than player object to maintain persistence after logout
    private final HashMap<ObjectGuid, PlayerReplicateThrottleData> replicateThrottleMap = new HashMap<ObjectGuid, PlayerReplicateThrottleData>();

    public AuctionHouseObject(int auctionHouseId) {
        auctionHouse = CliDB.AuctionHouseStorage.get(auctionHouseId);
    }

    public final int getAuctionHouseId() {
        return auctionHouse.id;
    }

    public final AuctionPosting getAuction(int auctionId) {
        return itemsByAuctionId.get(auctionId);
    }

    public final void addAuction(SQLTransaction trans, AuctionPosting auction) {
        var key = AuctionsBucketKey.forItem(auction.items.get(0));

        var bucket = buckets.get(key);

        if (bucket == null) {
            // we don't have any item for this key yet, create new bucket
            bucket = new AuctionsBucketData();
            bucket.key = key;

            var itemTemplate = auction.items.get(0).getTemplate();
            bucket.itemClass = (byte) itemTemplate.getClass().getValue();
            bucket.itemSubClass = (byte) itemTemplate.getSubClass();
            bucket.inventoryType = (byte) itemTemplate.getInventoryType().getValue();
            bucket.requiredLevel = (byte) auction.items.get(0).getRequiredLevel();

            switch (itemTemplate.getClass()) {
                case Weapon:
                case Armor:
                    bucket.sortLevel = (byte) key.getItemLevel();

                    break;
                case Container:
                    bucket.sortLevel = (byte) itemTemplate.getContainerSlots();

                    break;
                case Gem:
                case ItemEnhancement:
                    bucket.sortLevel = (byte) itemTemplate.getBaseItemLevel();

                    break;
                case Consumable:
                    bucket.sortLevel = Math.max((byte) 1, bucket.requiredLevel);

                    break;
                case Miscellaneous:
                case BattlePets:
                    bucket.sortLevel = 1;

                    break;
                case Recipe:
                    bucket.sortLevel = (byte) ((ItemSubClassRecipe.forValue(itemTemplate.getSubClass())).getValue() != ItemSubClassRecipe.Book ? itemTemplate.getRequiredSkillRank() : (int) itemTemplate.getBaseRequiredLevel());

                    break;
                default:
                    break;
            }

            for (var locale = locale.enUS; locale.getValue() < locale.Total.getValue(); ++locale) {
                if (locale == locale.NONE) {
                    continue;
                }

                bucket.FullName[locale.getValue()] = auction.items.get(0).getName(locale);
            }

            buckets.put(key, bucket);
        }

        // update cache fields
        var priceToDisplay = auction.buyoutOrUnitPrice != 0 ? auction.BuyoutOrUnitPrice : auction.bidAmount;

        if (bucket.minPrice == 0 || priceToDisplay < bucket.minPrice) {
            bucket.minPrice = priceToDisplay;
        }

        var itemModifiedAppearance = auction.items.get(0).getItemModifiedAppearance();

        if (itemModifiedAppearance != null) {
            var index = 0;

            for (var i = 0; i < bucket.itemModifiedAppearanceId.length; ++i) {
                if (bucket.ItemModifiedAppearanceId[i].id == itemModifiedAppearance.id) {
                    index = i;

                    break;
                }
            }

            bucket.ItemModifiedAppearanceId[index] = (itemModifiedAppearance.id, bucket.ItemModifiedAppearanceId[index].item2 + 1)
            ;
        }

        int quality;

        if (auction.items.get(0).getModifier(ItemModifier.battlePetSpeciesId) == 0) {
            quality = (byte) auction.items.get(0).getQuality().getValue();
        } else {
            quality = (auction.items.get(0).getModifier(ItemModifier.BattlePetBreedData) >>> 24) & 0xFF;

            for (var item : auction.items) {
                var battlePetLevel = (byte) item.getModifier(ItemModifier.battlePetLevel);

                if (bucket.minBattlePetLevel == 0) {
                    bucket.minBattlePetLevel = battlePetLevel;
                } else if (bucket.minBattlePetLevel > battlePetLevel) {
                    bucket.minBattlePetLevel = battlePetLevel;
                }

                bucket.maxBattlePetLevel = Math.max(bucket.maxBattlePetLevel, battlePetLevel);
                bucket.sortLevel = bucket.maxBattlePetLevel;
            }
        }

        bucket.qualityMask |= AuctionHouseFilterMask.forValue(1 << ((int) quality + 4));
        ++bucket.QualityCounts[quality];

        if (trans != null) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_AUCTION);
            stmt.AddValue(0, auction.id);
            stmt.AddValue(1, auctionHouse.id);
            stmt.AddValue(2, auction.owner.getCounter());
            stmt.AddValue(3, ObjectGuid.Empty.getCounter());
            stmt.AddValue(4, auction.minBid);
            stmt.AddValue(5, auction.buyoutOrUnitPrice);
            stmt.AddValue(6, auction.deposit);
            stmt.AddValue(7, auction.bidAmount);
            stmt.AddValue(8, time.DateTimeToUnixTime(auction.startTime));
            stmt.AddValue(9, time.DateTimeToUnixTime(auction.endTime));
            stmt.AddValue(10, (byte) auction.serverFlags.getValue());
            trans.append(stmt);

            for (var item : auction.items) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_AUCTION_ITEMS);
                stmt.AddValue(0, auction.id);
                stmt.AddValue(1, item.getGUID().getCounter());
                trans.append(stmt);
            }
        }

        for (var item : auction.items) {
            global.getAuctionHouseMgr().addAItem(item);
        }

        auction.bucket = bucket;
        playerOwnedAuctions.add(auction.owner, auction.id);

        for (var bidder : auction.bidderHistory) {
            playerBidderAuctions.add(bidder, auction.id);
        }

        itemsByAuctionId.set(auction.id, auction);

        AuctionPosting.Sorter insertSorter = new AuctionPosting.Sorter(locale.enUS, new AuctionSortDef[]{new(AuctionHouseSortOrder.price, false)},
        1);

        var auctionIndex = bucket.auctions.BinarySearch(auction, insertSorter);

        if (auctionIndex < 0) {
            auctionIndex = ~auctionIndex;
        }

        bucket.auctions.insert(auctionIndex, auction);

        global.getScriptMgr().<IAuctionHouseOnAuctionAdd>ForEach(p -> p.OnAuctionAdd(this, auction));
    }


    public final void removeAuction(SQLTransaction trans, AuctionPosting auction) {
        removeAuction(trans, auction, null);
    }

    public final void removeAuction(SQLTransaction trans, AuctionPosting auction, AuctionPosting auctionPosting) {
        var bucket = auction.bucket;

        tangible.ListHelper.removeAll(bucket.auctions, auct -> auct.id == auction.id);

        if (!bucket.auctions.isEmpty()) {
            // update cache fields
            var priceToDisplay = auction.buyoutOrUnitPrice != 0 ? auction.BuyoutOrUnitPrice : auction.bidAmount;

            if (bucket.minPrice == priceToDisplay) {
                bucket.minPrice = Long.MAX_VALUE;

                for (var remainingAuction : bucket.auctions) {
                    bucket.minPrice = Math.min(bucket.minPrice, remainingAuction.buyoutOrUnitPrice != 0 ? remainingAuction.BuyoutOrUnitPrice : remainingAuction.bidAmount);
                }
            }

            var itemModifiedAppearance = auction.items.get(0).getItemModifiedAppearance();

            if (itemModifiedAppearance != null) {
                var index = -1;

                for (var i = 0; i < bucket.itemModifiedAppearanceId.length; ++i) {
                    if (bucket.ItemModifiedAppearanceId[i].Item1 == itemModifiedAppearance.id) {
                        index = i;

                        break;
                    }
                }

                if (index != -1) {
                    if (--bucket.ItemModifiedAppearanceId[index].count == 0) {
                        bucket.ItemModifiedAppearanceId[index].id = 0;
                    }
                }
            }

            int quality;

            if (auction.items.get(0).getModifier(ItemModifier.battlePetSpeciesId) == 0) {
                quality = (int) auction.items.get(0).getQuality().getValue();
            } else {
                quality = (auction.items.get(0).getModifier(ItemModifier.BattlePetBreedData) >>> 24) & 0xFF;
                bucket.minBattlePetLevel = 0;
                bucket.maxBattlePetLevel = 0;

                for (var remainingAuction : bucket.auctions) {
                    for (var item : remainingAuction.items) {
                        var battlePetLevel = (byte) item.getModifier(ItemModifier.battlePetLevel);

                        if (bucket.minBattlePetLevel == 0) {
                            bucket.minBattlePetLevel = battlePetLevel;
                        } else if (bucket.minBattlePetLevel > battlePetLevel) {
                            bucket.minBattlePetLevel = battlePetLevel;
                        }

                        bucket.maxBattlePetLevel = Math.max(bucket.maxBattlePetLevel, battlePetLevel);
                    }
                }
            }

            if (--bucket.QualityCounts[quality] == 0) {
                bucket.qualityMask = AuctionHouseFilterMask.forValue(bucket.qualityMask.getValue() & AuctionHouseFilterMask.forValue(~(1 << ((int) quality + 4))).getValue());
            }
        } else {
            buckets.remove(bucket.key);
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_AUCTION);
        stmt.AddValue(0, auction.id);
        trans.append(stmt);

        for (var item : auction.items) {
            global.getAuctionHouseMgr().removeAItem(item.getGUID());
        }

        global.getScriptMgr().<IAuctionHouseOnAcutionRemove>ForEach(p -> p.OnAuctionRemove(this, auction));

        playerOwnedAuctions.remove(auction.owner, auction.id);

        for (var bidder : auction.bidderHistory) {
            playerBidderAuctions.remove(bidder, auction.id);
        }

        itemsByAuctionId.remove(auction.id);
    }

    public final void update() {
        var curTime = gameTime.GetSystemTime();
        var curTimeSteady = gameTime.Now();
        /**- Handle expired auctions
         */

        // Clear expired throttled players
        for (var key : replicateThrottleMap.keySet().ToList()) {
            if (replicateThrottleMap.get(key).nextAllowedReplication.compareTo(curTimeSteady) <= 0) {
                replicateThrottleMap.remove(key);
            }
        }

        for (var key : commodityQuotes.keySet().ToList()) {
            if (commodityQuotes.get(key).validTo.compareTo(curTimeSteady) < 0) {
                commodityQuotes.remove(key);
            }
        }

        if (itemsByAuctionId.isEmpty()) {
            return;
        }

        SQLTransaction trans = new SQLTransaction();

        for (var auction : itemsByAuctionId.VALUES.ToList()) {
            /**- filter auctions expired on next update
             */
            if (auction.endTime > curTime.plusMinutes(1)) {
                continue;
            }

            /**- Either cancel the auction if there was no bidder
             */
            if (auction.bidder.IsEmpty) {
                sendAuctionExpired(auction, trans);
                global.getScriptMgr().<IAuctionHouseOnAuctionExpire>ForEach(p -> p.OnAuctionExpire(this, auction));
            }
            /**- Or perform the transaction
             */
            else {
                //we should send an "item sold" message if the seller is online
                //we send the item to the winner
                //we send the money to the seller
                sendAuctionWon(auction, null, trans);
                sendAuctionSold(auction, null, trans);
                global.getScriptMgr().<IAuctionHouseOnAuctionSuccessful>ForEach(p -> p.OnAuctionSuccessful(this, auction));
            }

            /**- In any case clear the auction
             */
            removeAuction(trans, auction);
        }

        // Run DB changes
        DB.characters.CommitTransaction(trans);
    }

    public final void buildListBuckets(AuctionListBucketsResult listBucketsResult, Player player, String name, byte minLevel, byte maxLevel, AuctionHouseFilterMask filters, AuctionSearchClassFilters classFilters, byte[] knownPetBits, int knownPetBitsCount, byte maxKnownPetLevel, int offset, AuctionSortDef[] sorts, int sortCount) {
        ArrayList<Integer> knownAppearanceIds = new ArrayList<>();
        BitSet knownPetSpecies = new bitSet(knownPetBits);

        // prepare uncollected filter for more efficient searches
        if (filters.hasFlag(AuctionHouseFilterMask.UncollectedOnly)) {
            knownAppearanceIds = player.getSession().getCollectionMgr().getAppearanceIds();
        }

        //todo fix me
        //if (knownPetSpecies.length < CliDB.BattlePetSpeciesStorage.GetNumRows())
        //knownPetSpecies.resize(CliDB.BattlePetSpeciesStorage.GetNumRows());
        var sorter = new AuctionsBucketData.Sorter(player.getSession().getSessionDbcLocale(), sorts, sortCount);
        var builder = new AuctionsResultBuilder<AuctionsBucketData>(offset, sorter, AuctionHouseResultLimits.Browse);

        for (var bucket : buckets.entrySet()) {
            var bucketData = bucket.getValue();

            if (!name.isEmpty()) {
                if (filters.hasFlag(AuctionHouseFilterMask.ExactMatch)) {
                    if (!Objects.equals(bucketData.FullName[player.getSession().getSessionDbcLocale().getValue()], name)) {
                        continue;
                    }
                } else {
                    if (!bucketData.FullName[player.getSession().getSessionDbcLocale().getValue()].contains(name)) {
                        continue;
                    }
                }
            }

            if (minLevel != 0 && bucketData.requiredLevel < minLevel) {
                continue;
            }

            if (maxLevel != 0 && bucketData.requiredLevel > maxLevel) {
                continue;
            }

            if (!filters.hasFlag(bucketData.qualityMask)) {
                continue;
            }

            if (classFilters != null) {
                // if we dont want any class filters, Optional is not initialized
                // if we dont want this class included, SubclassMask is set to FILTER_SKIP_CLASS
                // if we want this class and did not specify and subclasses, its set to FILTER_SKIP_SUBCLASS
                // otherwise full restrictions apply
                if (classFilters.Classes[bucketData.ItemClass].subclassMask == AuctionSearchClassFilters.FilterType.SkipClass) {
                    continue;
                }

                if (classFilters.Classes[bucketData.ItemClass].subclassMask != AuctionSearchClassFilters.FilterType.SkipSubclass) {
                    if (!classFilters.Classes[bucketData.ItemClass].subclassMask.hasFlag(AuctionSearchClassFilters.FilterType.forValue(1 << bucketData.itemSubClass))) {
                        continue;
                    }

                    if (!classFilters.Classes[bucketData.ItemClass].InvTypes[bucketData.ItemSubClass].hasFlag(1 << bucketData.inventoryType)) {
                        continue;
                    }
                }
            }

            if (filters.hasFlag(AuctionHouseFilterMask.UncollectedOnly)) {
                // appearances - by ItemAppearanceId, not ItemModifiedAppearanceId
                if (bucketData.inventoryType != (byte) inventoryType.NonEquip.getValue() && bucketData.inventoryType != (byte) inventoryType.bag.getValue()) {
                    var hasAll = true;

                    for (var bucketAppearance : bucketData.itemModifiedAppearanceId) {
                        var itemModifiedAppearance = CliDB.ItemModifiedAppearanceStorage.get(bucketAppearance.Item1);

                        if (itemModifiedAppearance != null) {
                            if (!knownAppearanceIds.contains((int) itemModifiedAppearance.ItemAppearanceID)) {
                                hasAll = false;

                                break;
                            }
                        }
                    }

                    if (hasAll) {
                        continue;
                    }
                }
                // caged pets
                else if (bucket.getKey().battlePetSpeciesId != 0) {
                    if (knownPetSpecies.get(bucket.getKey().battlePetSpeciesId)) {
                        continue;
                    }
                }
                // toys
                else if (global.getDB2Mgr().IsToyItem(bucket.getKey().itemId)) {
                    if (player.getSession().getCollectionMgr().hasToy(bucket.getKey().itemId)) {
                        continue;
                    }
                }
                // mounts
                // recipes
                // pet items
                else if (bucketData.itemClass == itemClass.Consumable.getValue() || bucketData.itemClass == itemClass.Recipe.getValue() || bucketData.itemClass == itemClass.Miscellaneous.getValue()) {
                    var itemTemplate = global.getObjectMgr().getItemTemplate(bucket.getKey().itemId);

                    if (itemTemplate.getEffects().size() >= 2 && (itemTemplate.getEffects().get(0).spellID == 483 || itemTemplate.getEffects().get(0).spellID == 55884)) {
                        if (player.hasSpell((int) itemTemplate.getEffects().get(1).spellID)) {
                            continue;
                        }

                        var battlePetSpecies = BattlePetMgr.getBattlePetSpeciesBySpell((int) itemTemplate.getEffects().get(1).spellID);

                        if (battlePetSpecies != null) {
                            if (knownPetSpecies.get((int) battlePetSpecies.id)) {
                                continue;
                            }
                        }
                    }
                }
            }

            if (filters.hasFlag(AuctionHouseFilterMask.UsableOnly)) {
                if (bucketData.requiredLevel != 0 && player.getLevel() < bucketData.requiredLevel) {
                    continue;
                }

                if (player.canUseItem(global.getObjectMgr().getItemTemplate(bucket.getKey().itemId), true) != InventoryResult.Ok) {
                    continue;
                }

                // cannot learn caged pets whose level exceeds highest level of currently owned pet
                if (bucketData.minBattlePetLevel != 0 && bucketData.minBattlePetLevel > maxKnownPetLevel) {
                    continue;
                }
            }

            // TODO: this one needs to access loot history to know highest item level for every inventory type
            //if (filters.hasFlag(AuctionHouseFilterMask.UpgradesOnly))
            //{
            //}

            builder.addItem(bucketData);
        }

        for (var resultBucket : builder.getResultRange()) {
            BucketInfo bucketInfo = new BucketInfo();
            resultBucket.buildBucketInfo(bucketInfo, player);
            listBucketsResult.buckets.add(bucketInfo);
        }

        listBucketsResult.hasMoreResults = builder.hasMoreResults();
    }

    public final void buildListBuckets(AuctionListBucketsResult listBucketsResult, Player player, AuctionBucketKey[] keys, int keysCount, AuctionSortDef[] sorts, int sortCount) {
        ArrayList<AuctionsBucketData> buckets = new ArrayList<>();

        for (var i = 0; i < keysCount; ++i) {
            var bucketData = buckets.get(new AuctionsBucketKey(keys[i]));

            if (bucketData != null) {
                buckets.add(bucketData);
            }
        }

        AuctionsBucketData.Sorter sorter = new AuctionsBucketData.Sorter(player.getSession().getSessionDbcLocale(), sorts, sortCount);
        collections.sort(buckets, sorter);

        for (var resultBucket : buckets) {
            BucketInfo bucketInfo = new BucketInfo();
            resultBucket.buildBucketInfo(bucketInfo, player);
            listBucketsResult.buckets.add(bucketInfo);
        }

        listBucketsResult.hasMoreResults = false;
    }

    public final void buildListBiddedItems(AuctionListBiddedItemsResult listBidderItemsResult, Player player, int offset, AuctionSortDef[] sorts, int sortCount) {
        // always full list
        ArrayList<AuctionPosting> auctions = new ArrayList<>();

        for (var auctionId : playerBidderAuctions.get(player.getGUID())) {
            var auction = itemsByAuctionId.get(auctionId);

            if (auction != null) {
                auctions.add(auction);
            }
        }

        AuctionPosting.Sorter sorter = new AuctionPosting.Sorter(player.getSession().getSessionDbcLocale(), sorts, sortCount);
        collections.sort(auctions, sorter);

        for (var resultAuction : auctions) {
            AuctionItem auctionItem = new AuctionItem();
            resultAuction.buildAuctionItem(auctionItem, true, true, true, false);
            listBidderItemsResult.items.add(auctionItem);
        }

        listBidderItemsResult.hasMoreResults = false;
    }

    public final void buildListAuctionItems(AuctionListItemsResult listItemsResult, Player player, AuctionsBucketKey bucketKey, int offset, AuctionSortDef[] sorts, int sortCount) {
        listItemsResult.totalCount = 0;
        var bucket = buckets.get(bucketKey);

        if (bucket != null) {
            var sorter = new AuctionPosting.Sorter(player.getSession().getSessionDbcLocale(), sorts, sortCount);
            var builder = new AuctionsResultBuilder<AuctionPosting>(offset, sorter, AuctionHouseResultLimits.items);

            for (var auction : bucket.auctions) {
                builder.addItem(auction);

                for (var item : auction.items) {
                    listItemsResult.totalCount += item.count;
                }
            }

            for (var resultAuction : builder.getResultRange()) {
                AuctionItem auctionItem = new AuctionItem();
                resultAuction.buildAuctionItem(auctionItem, false, false, ObjectGuid.opNotEquals(resultAuction.ownerAccount, player.getSession().getAccountGUID()), resultAuction.bidder.IsEmpty);
                listItemsResult.items.add(auctionItem);
            }

            listItemsResult.hasMoreResults = builder.hasMoreResults();
        }
    }

    public final void buildListAuctionItems(AuctionListItemsResult listItemsResult, Player player, int itemId, int offset, AuctionSortDef[] sorts, int sortCount) {
        var sorter = new AuctionPosting.Sorter(player.getSession().getSessionDbcLocale(), sorts, sortCount);
        var builder = new AuctionsResultBuilder<AuctionPosting>(offset, sorter, AuctionHouseResultLimits.items);

        listItemsResult.totalCount = 0;
        var bucketData = buckets.get(new AuctionsBucketKey(itemId, (short) 0, (short) 0, (short) 0));

        if (bucketData != null) {
            for (var auction : bucketData.auctions) {
                builder.addItem(auction);

                for (var item : auction.items) {
                    listItemsResult.totalCount += item.count;
                }
            }
        }

        for (var resultAuction : builder.getResultRange()) {
            AuctionItem auctionItem = new AuctionItem();

            resultAuction.buildAuctionItem(auctionItem, false, true, ObjectGuid.opNotEquals(resultAuction.ownerAccount, player.getSession().getAccountGUID()), resultAuction.bidder.IsEmpty);

            listItemsResult.items.add(auctionItem);
        }

        listItemsResult.hasMoreResults = builder.hasMoreResults();
    }

    public final void buildListOwnedItems(AuctionListOwnedItemsResult listOwnerItemsResult, Player player, int offset, AuctionSortDef[] sorts, int sortCount) {
        // always full list
        ArrayList<AuctionPosting> auctions = new ArrayList<>();

        for (var auctionId : playerOwnedAuctions.get(player.getGUID())) {
            var auction = itemsByAuctionId.get(auctionId);

            if (auction != null) {
                auctions.add(auction);
            }
        }

        AuctionPosting.Sorter sorter = new AuctionPosting.Sorter(player.getSession().getSessionDbcLocale(), sorts, sortCount);
        collections.sort(auctions, sorter);

        for (var resultAuction : auctions) {
            AuctionItem auctionItem = new AuctionItem();
            resultAuction.buildAuctionItem(auctionItem, true, true, false, false);
            listOwnerItemsResult.items.add(auctionItem);
        }

        listOwnerItemsResult.hasMoreResults = false;
    }

    public final void buildReplicate(AuctionReplicateResponse replicateResponse, Player player, int global, int cursor, int tombstone, int count) {
        var curTime = gameTime.Now();

        var throttleData = replicateThrottleMap.get(player.getGUID());

        if (throttleData == null) {
            throttleData = new PlayerReplicateThrottleData();
            throttleData.nextAllowedReplication = curTime + duration.FromSeconds(WorldConfig.getIntValue(WorldCfg.AuctionReplicateDelay));
            throttleData.global = global.getAuctionHouseMgr().getGenerateReplicationId();
        } else {
            if (throttleData.global != global || throttleData.cursor != cursor || throttleData.tombstone != tombstone) {
                return;
            }

            if (!throttleData.isReplicationInProgress() && throttleData.nextAllowedReplication > curTime) {
                return;
            }
        }

        if (itemsByAuctionId.isEmpty() || count == 0) {
            return;
        }

        var keyIndex = itemsByAuctionId.IndexOfKey(cursor);

        for (var pair : itemsByAuctionId.Skip(keyIndex)) {
            AuctionItem auctionItem = new AuctionItem();
            pair.value.buildAuctionItem(auctionItem, false, true, true, pair.value.bidder.IsEmpty);
            replicateResponse.items.add(auctionItem);

            if (--count == 0) {
                break;
            }
        }

        replicateResponse.changeNumberGlobal = throttleData.global;
        replicateResponse.changeNumberCursor = throttleData.cursor = !replicateResponse.items.isEmpty() ? replicateResponse.items.get(replicateResponse.items.size() - 1).AuctionID : 0;
        replicateResponse.changeNumberTombstone = throttleData.tombstone = count == 0 ? itemsByAuctionId.first().Key : 0;
        replicateThrottleMap.put(player.getGUID(), throttleData);
    }

    public final long calculateAuctionHouseCut(long bidAmount) {
        return (long) Math.max((long) (MathUtil.CalculatePct(bidAmount, auctionHouse.ConsignmentRate) * WorldConfig.getFloatValue(WorldCfg.RateAuctionCut)), 0);
    }

    public final CommodityQuote createCommodityQuote(Player player, int itemId, int quantity) {
        var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

        if (itemTemplate == null) {
            return null;
        }

        var bucketData = buckets.get(AuctionsBucketKey.forCommodity(itemTemplate));

        if (bucketData == null) {
            return null;
        }

        long totalPrice = 0;
        var remainingQuantity = quantity;

        for (var auction : bucketData.auctions) {
            for (var auctionItem : auction.items) {
                if (auctionItem.count >= remainingQuantity) {
                    totalPrice += auction.BuyoutOrUnitPrice * remainingQuantity;
                    remainingQuantity = 0;

                    break;
                }

                totalPrice += auction.BuyoutOrUnitPrice * auctionItem.count;
                remainingQuantity -= auctionItem.count;
            }
        }

        // not enough items on auction house
        if (remainingQuantity != 0) {
            return null;
        }

        if (!player.hasEnoughMoney(totalPrice)) {
            return null;
        }

        var quote = commodityQuotes.get(player.getGUID());
        quote.totalPrice = totalPrice;
        quote.quantity = quantity;
        quote.validTo = gameTime.Now() + duration.FromSeconds(30);

        return quote;
    }

    public final void cancelCommodityQuote(ObjectGuid guid) {
        commodityQuotes.remove(guid);
    }

    public final boolean buyCommodity(SQLTransaction trans, Player player, int itemId, int quantity, Duration delayForNextAction) {
        var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

        if (itemTemplate == null) {
            return false;
        }

        var bucketItr = buckets.get(AuctionsBucketKey.forCommodity(itemTemplate));

        if (bucketItr == null) {
            player.getSession().sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, delayForNextAction);

            return false;
        }

        var quote = commodityQuotes.get(player.getGUID());

        if (quote == null) {
            player.getSession().sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, delayForNextAction);

            return false;
        }

        long totalPrice = 0;
        var remainingQuantity = quantity;
        ArrayList<AuctionPosting> auctions = new ArrayList<>();

        for (var i = 0; i < bucketItr.auctions.count; ) {
            var auction = bucketItr.Auctions[i++];
            auctions.add(auction);

            for (var auctionItem : auction.items) {
                if (auctionItem.count >= remainingQuantity) {
                    totalPrice += auction.BuyoutOrUnitPrice * remainingQuantity;
                    remainingQuantity = 0;
                    i = bucketItr.auctions.count;

                    break;
                }

                totalPrice += auction.BuyoutOrUnitPrice * auctionItem.count;
                remainingQuantity -= auctionItem.count;
            }
        }

        // not enough items on auction house
        if (remainingQuantity != 0) {
            player.getSession().sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, delayForNextAction);

            return false;
        }

        // something was bought between creating quote and finalizing transaction
        // but we allow lower price if new items were posted at lower price
        if (totalPrice > quote.totalPrice) {
            player.getSession().sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, delayForNextAction);

            return false;
        }

        if (!player.hasEnoughMoney(totalPrice)) {
            player.getSession().sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, delayForNextAction);

            return false;
        }

        ObjectGuid uniqueSeller = ObjectGuid.EMPTY;

        // prepare items
        ArrayList<MailedItemsBatch> items = new ArrayList<>();
        items.add(new MailedItemsBatch());

        remainingQuantity = quantity;
        ArrayList<Integer> removedItemsFromAuction = new ArrayList<>();

        for (var i = 0; i < bucketItr.auctions.count; ) {
            var auction = bucketItr.Auctions[i++];

            if (!uniqueSeller != null) {
                uniqueSeller = auction.owner;
            } else if (!uniqueSeller.equals(auction.owner)) {
                uniqueSeller = ObjectGuid.Empty;
            }

            int boughtFromAuction = 0;
            var removedItems = 0;

            for (var auctionItem : auction.items) {
                var itemsBatch = items.get(items.size() - 1);

                if (itemsBatch.isFull()) {
                    items.add(new MailedItemsBatch());
                    itemsBatch = items.get(items.size() - 1);
                }

                if (auctionItem.count >= remainingQuantity) {
                    var clonedItem = auctionItem.cloneItem(remainingQuantity, player);

                    if (!clonedItem) {
                        player.getSession().sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, delayForNextAction);

                        return false;
                    }

                    auctionItem.setCount(auctionItem.Count - remainingQuantity);
                    auctionItem.FSetState(ItemUpdateState.changed);
                    auctionItem.saveToDB(trans);
                    itemsBatch.addItem(clonedItem, auction.buyoutOrUnitPrice);
                    boughtFromAuction += remainingQuantity;
                    remainingQuantity = 0;
                    i = bucketItr.auctions.count;

                    break;
                }

                itemsBatch.addItem(auctionItem, auction.buyoutOrUnitPrice);
                boughtFromAuction += auctionItem.count;
                remainingQuantity -= auctionItem.count;
                ++removedItems;
            }

            removedItemsFromAuction.add(removedItems);

            if (player.getSession().hasPermission(RBACPermissions.LogGmTrade)) {
                var bidderAccId = player.getSession().getAccountId();

                String ownerName;
                tangible.OutObject<String> tempOut_ownerName = new tangible.OutObject<String>();
                if (!global.getCharacterCacheStorage().getCharacterNameByGuid(auction.owner, tempOut_ownerName)) {
                    ownerName = tempOut_ownerName.outArgValue;
                    ownerName = global.getObjectMgr().getSysMessage(SysMessage.unknown);
                } else {
                    ownerName = tempOut_ownerName.outArgValue;
                }

                Log.outCommand(bidderAccId, String.format("GM %1$s (Account: %2$s) bought commodity in auction: %3$s (Entry: %4$s ", player.getName(), bidderAccId, items.get(0).Items[0].getName(global.getWorldMgr().getDefaultDbcLocale()), items.get(0).Items[0].getEntry()) + String.format("Count: %1$s) and pay money: %2$s. Original owner %3$s (Account: %4$s)", boughtFromAuction, auction.BuyoutOrUnitPrice * boughtFromAuction, ownerName, global.getCharacterCacheStorage().getCharacterAccountIdByGuid(auction.owner)));
            }

            var auctionHouseCut = calculateAuctionHouseCut(auction.BuyoutOrUnitPrice * boughtFromAuction);
            var depositPart = global.getAuctionHouseMgr().getCommodityAuctionDeposit(items.get(0).Items[0].getTemplate(), (auction.EndTime - auction.startTime), boughtFromAuction);
            var profit = auction.BuyoutOrUnitPrice * boughtFromAuction + depositPart - auctionHouseCut;

            var owner = global.getObjAccessor().findConnectedPlayer(auction.owner);

            if (owner != null) {
                owner.updateCriteria(CriteriaType.MoneyEarnedFromAuctions, profit);
                owner.updateCriteria(CriteriaType.HighestAuctionSale, profit);
                owner.getSession().sendAuctionClosedNotification(auction, (float) WorldConfig.getIntValue(WorldCfg.MailDeliveryDelay), true);
            }

            (new MailDraft(global.getAuctionHouseMgr().buildCommodityAuctionMailSubject(AuctionMailType.sold, itemId, boughtFromAuction), global.getAuctionHouseMgr().buildAuctionSoldMailBody(player.getGUID(), auction.BuyoutOrUnitPrice * boughtFromAuction, boughtFromAuction, (int) depositPart, auctionHouseCut))).addMoney(profit).sendMailTo(trans, new MailReceiver(global.getObjAccessor().findConnectedPlayer(auction.owner), auction.owner), new MailSender(this), MailCheckMask.Copied, WorldConfig.getUIntValue(WorldCfg.MailDeliveryDelay));
        }

        player.modifyMoney(-(long) totalPrice);
        player.saveGoldToDB(trans);

        for (var batch : items) {
            MailDraft mail = new MailDraft(global.getAuctionHouseMgr().buildCommodityAuctionMailSubject(AuctionMailType.Won, itemId, batch.quantity), global.getAuctionHouseMgr().buildAuctionWonMailBody(uniqueSeller.getValue(), batch.totalPrice, batch.quantity));

            for (var i = 0; i < batch.itemsCount; ++i) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_AUCTION_ITEMS_BY_ITEM);
                stmt.AddValue(0, batch.Items[i].getGUID().getCounter());
                trans.append(stmt);

                batch.Items[i].setOwnerGUID(player.getGUID());
                batch.Items[i].saveToDB(trans);
                mail.addItem(batch.Items[i]);
            }

            mail.sendMailTo(trans, player, new MailSender(this), MailCheckMask.Copied);
        }

        AuctionWonNotification packet = new AuctionWonNotification();
        packet.info.initialize(auctions.get(0), items.get(0).Items[0]);
        player.sendPacket(packet);

        for (var i = 0; i < auctions.size(); ++i) {
            if (removedItemsFromAuction.get(i).equals(auctions.get(i).items.size())) {
                removeAuction(trans, auctions.get(i)); // bought all items
            } else if (!removedItemsFromAuction.get(i).equals(0)) {
                var lastRemovedItemIndex = removedItemsFromAuction.get(i);

                for (var c = 0; !removedItemsFromAuction.get(i).equals(c); ++c) {
                    global.getAuctionHouseMgr().removeAItem(auctions.get(i).items.get(c).getGUID());
                }

                auctions.get(i).items.subList(0, lastRemovedItemIndex).clear();
            }
        }

        return true;
    }

    // this function notified old bidder that his bid is no longer highest
    public final void sendAuctionOutbid(AuctionPosting auction, ObjectGuid newBidder, long newBidAmount, SQLTransaction trans) {
        var oldBidder = global.getObjAccessor().findConnectedPlayer(auction.bidder);

        // old bidder exist
        if ((oldBidder || global.getCharacterCacheStorage().hasCharacterCacheEntry(auction.bidder))) // && !sAuctionBotConfig.IsBotChar(auction.bidder))
        {
            if (oldBidder) {
                AuctionOutbidNotification packet = new AuctionOutbidNotification();
                packet.bidAmount = newBidAmount;
                packet.minIncrement = AuctionPosting.calculateMinIncrement(newBidAmount);
                packet.info.auctionID = auction.id;
                packet.info.bidder = newBidder;
                packet.info.item = new itemInstance(auction.items.get(0));
                oldBidder.sendPacket(packet);
            }

            (new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.Outbid, auction), "")).addMoney(auction.bidAmount).sendMailTo(trans, new MailReceiver(oldBidder, auction.bidder), new MailSender(this), MailCheckMask.Copied);
        }
    }

    public final void sendAuctionWon(AuctionPosting auction, Player bidder, SQLTransaction trans) {
        int bidderAccId;

        if (!bidder) {
            bidder = global.getObjAccessor().findConnectedPlayer(auction.bidder); // try lookup bidder when called from .Update
        }

        // data for gm.log
        var bidderName = "";
        var logGmTrade = auction.serverFlags.hasFlag(AuctionPostingServerFlag.GmLogBuyer);

        if (bidder) {
            bidderAccId = bidder.getSession().getAccountId();
            bidderName = bidder.getName();
        } else {
            bidderAccId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(auction.bidder);

            tangible.OutObject<String> tempOut_bidderName = new tangible.OutObject<String>();
            if (logGmTrade && !global.getCharacterCacheStorage().getCharacterNameByGuid(auction.bidder, tempOut_bidderName)) {
                bidderName = tempOut_bidderName.outArgValue;
                bidderName = global.getObjectMgr().getSysMessage(SysMessage.unknown);
            } else {
                bidderName = tempOut_bidderName.outArgValue;
            }
        }

        if (logGmTrade) {
            String ownerName;
            tangible.OutObject<String> tempOut_ownerName = new tangible.OutObject<String>();
            if (!global.getCharacterCacheStorage().getCharacterNameByGuid(auction.owner, tempOut_ownerName)) {
                ownerName = tempOut_ownerName.outArgValue;
                ownerName = global.getObjectMgr().getSysMessage(SysMessage.unknown);
            } else {
                ownerName = tempOut_ownerName.outArgValue;
            }

            var ownerAccId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(auction.owner);

            Log.outCommand(bidderAccId, String.format("GM %1$s (Account: %2$s) won item in auction: %3$s (Entry: %4$s", bidderName, bidderAccId, auction.items.get(0).getName(global.getWorldMgr().getDefaultDbcLocale()), auction.items.get(0).getEntry()) + String.format(" Count: %1$s) and pay money: %2$s. Original owner %3$s (Account: %4$s)", auction.getTotalItemCount(), auction.bidAmount, ownerName, ownerAccId));
        }

        // receiver exist
        if ((bidder != null || bidderAccId != 0)) // && !sAuctionBotConfig.IsBotChar(auction.bidder))
        {
            MailDraft mail = new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.Won, auction), global.getAuctionHouseMgr().buildAuctionWonMailBody(auction.owner, auction.bidAmount, auction.buyoutOrUnitPrice));

            // set owner to bidder (to prevent delete item with sender char deleting)
            // owner in `data` will set at mail receive and item extracting
            for (var item : auction.items) {
                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ITEM_OWNER);
                stmt.AddValue(0, auction.bidder.getCounter());
                stmt.AddValue(1, item.getGUID().getCounter());
                trans.append(stmt);

                mail.addItem(item);
            }

            if (bidder) {
                AuctionWonNotification packet = new AuctionWonNotification();
                packet.info.initialize(auction, auction.items.get(0));
                bidder.sendPacket(packet);

                // FIXME: for offline player need also
                bidder.updateCriteria(CriteriaType.AuctionsWon, 1);
            }

            mail.sendMailTo(trans, new MailReceiver(bidder, auction.bidder), new MailSender(this), MailCheckMask.Copied);
        } else {
            // bidder doesn't exist, delete the item
            for (var item : auction.items) {
                global.getAuctionHouseMgr().removeAItem(item.getGUID(), true, trans);
            }
        }
    }

    //call this method to send mail to auction owner, when auction is successful, it does not clear ram
    public final void sendAuctionSold(AuctionPosting auction, Player owner, SQLTransaction trans) {
        if (!owner) {
            owner = global.getObjAccessor().findConnectedPlayer(auction.owner);
        }

        // owner exist
        if ((owner || global.getCharacterCacheStorage().hasCharacterCacheEntry(auction.owner))) // && !sAuctionBotConfig.IsBotChar(auction.owner))
        {
            var auctionHouseCut = calculateAuctionHouseCut(auction.bidAmount);
            var profit = auction.bidAmount + auction.Deposit - auctionHouseCut;

            //FIXME: what do if owner offline
            if (owner) {
                owner.updateCriteria(CriteriaType.MoneyEarnedFromAuctions, profit);
                owner.updateCriteria(CriteriaType.HighestAuctionSale, auction.bidAmount);

                //send auction owner notification, bidder must be current!
                owner.getSession().sendAuctionClosedNotification(auction, (float) WorldConfig.getIntValue(WorldCfg.MailDeliveryDelay), true);
            }

            (new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.sold, auction), global.getAuctionHouseMgr().buildAuctionSoldMailBody(auction.bidder, auction.bidAmount, auction.buyoutOrUnitPrice, (int) auction.deposit, auctionHouseCut))).addMoney(profit).sendMailTo(trans, new MailReceiver(owner, auction.owner), new MailSender(this), MailCheckMask.Copied, WorldConfig.getUIntValue(WorldCfg.MailDeliveryDelay));
        }
    }

    public final void sendAuctionExpired(AuctionPosting auction, SQLTransaction trans) {
        var owner = global.getObjAccessor().findConnectedPlayer(auction.owner);

        // owner exist
        if ((owner || global.getCharacterCacheStorage().hasCharacterCacheEntry(auction.owner))) // && !sAuctionBotConfig.IsBotChar(auction.owner))
        {
            if (owner) {
                owner.getSession().sendAuctionClosedNotification(auction, 0.0f, false);
            }

            var itemIndex = 0;

            while (itemIndex < auction.items.size()) {
                MailDraft mail = new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.Expired, auction), "");

                for (var i = 0; i < SharedConst.MaxMailItems && itemIndex < auction.items.size(); ++i, ++itemIndex) {
                    mail.addItem(auction.items.get(itemIndex));
                }

                mail.sendMailTo(trans, new MailReceiver(owner, auction.owner), new MailSender(this), MailCheckMask.Copied, 0);
            }
        } else {
            // owner doesn't exist, delete the item
            for (var item : auction.items) {
                global.getAuctionHouseMgr().removeAItem(item.getGUID(), true, trans);
            }
        }
    }

    public final void sendAuctionRemoved(AuctionPosting auction, Player owner, SQLTransaction trans) {
        var itemIndex = 0;

        while (itemIndex < auction.items.size()) {
            MailDraft draft = new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.Cancelled, auction), "");

            for (var i = 0; i < SharedConst.MaxMailItems && itemIndex < auction.items.size(); ++i, ++itemIndex) {
                draft.addItem(auction.items.get(itemIndex));
            }

            draft.sendMailTo(trans, owner, new MailSender(this), MailCheckMask.Copied);
        }
    }

    //this function sends mail, when auction is cancelled to old bidder
    public final void sendAuctionCancelledToBidder(AuctionPosting auction, SQLTransaction trans) {
        var bidder = global.getObjAccessor().findConnectedPlayer(auction.bidder);

        // bidder exist
        if ((bidder || global.getCharacterCacheStorage().hasCharacterCacheEntry(auction.bidder))) // && !sAuctionBotConfig.IsBotChar(auction.bidder))
        {
            (new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.removed, auction), "")).addMoney(auction.bidAmount).sendMailTo(trans, new MailReceiver(bidder, auction.bidder), new MailSender(this), MailCheckMask.Copied);
        }
    }

    public final void sendAuctionInvoice(AuctionPosting auction, Player owner, SQLTransaction trans) {
        if (!owner) {
            owner = global.getObjAccessor().findConnectedPlayer(auction.owner);
        }

        // owner exist (online or offline)
        if ((owner || global.getCharacterCacheStorage().hasCharacterCacheEntry(auction.owner))) // && !sAuctionBotConfig.IsBotChar(auction.owner))
        {
            ByteBuffer tempBuffer = new byteBuffer();
            tempBuffer.writePackedTime(gameTime.GetGameTime() + WorldConfig.getIntValue(WorldCfg.MailDeliveryDelay));
            var eta = tempBuffer.readUInt();

            (new MailDraft(global.getAuctionHouseMgr().buildItemAuctionMailSubject(AuctionMailType.Invoice, auction), global.getAuctionHouseMgr().buildAuctionInvoiceMailBody(auction.bidder, auction.bidAmount, auction.buyoutOrUnitPrice, (int) auction.deposit, calculateAuctionHouseCut(auction.bidAmount), WorldConfig.getUIntValue(WorldCfg.MailDeliveryDelay), eta))).sendMailTo(trans, new MailReceiver(owner, auction.owner), new MailSender(this), MailCheckMask.Copied);
        }
    }

    private static class PlayerReplicateThrottleData {
        public int global;
        public int cursor;
        public int tombstone;
        public LocalDateTime nextAllowedReplication = LocalDateTime.MIN;

        public final boolean isReplicationInProgress() {
            return cursor != tombstone && global != 0;
        }
    }

    private static class MailedItemsBatch {
        public final Item[] items = new Item[SharedConst.MaxMailItems];
        public long totalPrice;
        public int quantity;

        public int itemsCount;

        public final boolean isFull() {
            return itemsCount >= items.length;
        }

        public final void addItem(Item item, long unitPrice) {
            Items[ItemsCount++] = item;
            quantity += item.getCount();
            totalPrice += unitPrice * item.getCount();
        }
    }
}
