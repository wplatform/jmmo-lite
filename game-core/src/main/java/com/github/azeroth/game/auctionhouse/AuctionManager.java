package game;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemTemplate;
import com.github.azeroth.game.entity.player.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class AuctionManager {
    private static final int MIN_AUCTION_TIME = 12 * time.Hour;
    private final AuctionHouseObject hordeAuctions;
    private final AuctionHouseObject allianceAuctions;
    private final AuctionHouseObject neutralAuctions;
    private final AuctionHouseObject goblinAuctions;
    private final HashMap<ObjectGuid, PlayerPendingAuctions> pendingAuctionsByPlayer = new HashMap<ObjectGuid, PlayerPendingAuctions>();
    private final HashMap<ObjectGuid, item> itemsByGuid = new HashMap<ObjectGuid, item>();
    private final HashMap<ObjectGuid, PlayerThrottleObject> playerThrottleObjects = new HashMap<ObjectGuid, PlayerThrottleObject>();

    private int replicateIdGenerator;
    private LocalDateTime playerThrottleObjectsCleanupTime = LocalDateTime.MIN;

    private AuctionManager() {
        hordeAuctions = new AuctionHouseObject(6);
        allianceAuctions = new AuctionHouseObject(2);
        neutralAuctions = new AuctionHouseObject(1);
        goblinAuctions = new AuctionHouseObject(7);
        replicateIdGenerator = 0;
        playerThrottleObjectsCleanupTime = gameTime.Now() + duration.FromHours(1);
    }

    public final int getGenerateReplicationId() {
        return ++replicateIdGenerator;
    }

    public final AuctionHouseObject getAuctionsMap(int factionTemplateId) {
        if (WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionAuction)) {
            return neutralAuctions;
        }

        // teams have linked auction houses
        var uEntry = CliDB.FactionTemplateStorage.get(factionTemplateId);

        if (uEntry == null) {
            return neutralAuctions;
        } else if (uEntry.factionGroup.hasFlag((byte) FactionMasks.Alliance.getValue())) {
            return allianceAuctions;
        } else if (uEntry.factionGroup.hasFlag((byte) FactionMasks.Horde.getValue())) {
            return hordeAuctions;
        } else {
            return neutralAuctions;
        }
    }

    public final AuctionHouseObject getAuctionsById(int auctionHouseId) {
        switch (auctionHouseId) {
            case 1:
                return neutralAuctions;
            case 2:
                return allianceAuctions;
            case 6:
                return hordeAuctions;
            case 7:
                return goblinAuctions;
            default:
                break;
        }

        return neutralAuctions;
    }

    public final Item getAItem(ObjectGuid itemGuid) {
        return itemsByGuid.get(itemGuid);
    }

    public final long getCommodityAuctionDeposit(ItemTemplate item, Duration time, int quantity) {
        var sellPrice = item.getSellPrice();

        return (long) ((Math.ceil(Math.floor(Math.max(0.15 * quantity * sellPrice, 100.0)) / MoneyConstants.Silver) * MoneyConstants.Silver) * (time.Minutes / (MIN_AUCTION_TIME / time.Minute)));
    }

    public final long getItemAuctionDeposit(Player player, Item item, Duration time) {
        var sellPrice = item.getSellPrice(player);

        return (long) ((Math.ceil(Math.floor(Math.max(sellPrice * 0.15, 100.0)) / MoneyConstants.Silver) * MoneyConstants.Silver) * (time.Minutes / (MIN_AUCTION_TIME / time.Minute)));
    }

    public final String buildItemAuctionMailSubject(AuctionMailType type, AuctionPosting auction) {
        return buildAuctionMailSubject(auction.items.get(0).getEntry(), type, auction.id, auction.getTotalItemCount(), auction.items.get(0).getModifier(ItemModifier.battlePetSpeciesId), auction.items.get(0).getContext(), auction.items.get(0).getBonusListIDs());
    }

    public final String buildCommodityAuctionMailSubject(AuctionMailType type, int itemId, int itemCount) {
        return buildAuctionMailSubject(itemId, type, 0, itemCount, 0, itemContext.NONE, null);
    }

    public final String buildAuctionMailSubject(int itemId, AuctionMailType type, int auctionId, int itemCount, int battlePetSpeciesId, ItemContext context, ArrayList<Integer> bonusListIds) {
        var str = String.format("%1$s:0:%2$s:%3$s:%4$s:%5$s:0:0:0:0:%6$s:%7$s", itemId, (int) type.getValue(), auctionId, itemCount, battlePetSpeciesId, (int) context.getValue(), bonusListIds.size());

        for (var bonusListId : bonusListIds) {
            str += ':' + bonusListId;
        }

        return str;
    }

    public final String buildAuctionWonMailBody(ObjectGuid guid, long bid, long buyout) {
        return String.format("%1$s:%2$s:%3$s:0", guid, bid, buyout);
    }

    public final String buildAuctionSoldMailBody(ObjectGuid guid, long bid, long buyout, int deposit, long consignment) {
        return String.format("%1$s:%2$s:%3$s:%4$s:%5$s:0", guid, bid, buyout, deposit, consignment);
    }

    public final String buildAuctionInvoiceMailBody(ObjectGuid guid, long bid, long buyout, int deposit, long consignment, int moneyDelay, int eta) {
        return String.format("%1$s:%2$s:%3$s:%4$s:%5$s:%6$s:%7$s:0", guid, bid, buyout, deposit, consignment, moneyDelay, eta);
    }

    public final void loadAuctions() {
        var oldMSTime = System.currentTimeMillis();

        // need to clear in case we are reloading
        itemsByGuid.clear();

        var result = DB.characters.query(DB.characters.GetPreparedStatement(CharStatements.SEL_AUCTION_ITEMS));

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 auctions. DB table `auctionhouse` is empty.");

            return;
        }

        // data needs to be at first place for item.LoadFromDB
        int count = 0;
        MultiMap<Integer, item> itemsByAuction = new MultiMap<Integer, item>();
        MultiMap<Integer, ObjectGuid> biddersByAuction = new MultiMap<Integer, ObjectGuid>();

        do {
            var itemGuid = result.<Long>Read(0);
            var itemEntry = result.<Integer>Read(1);

            var proto = global.getObjectMgr().getItemTemplate(itemEntry);

            if (proto == null) {
                Log.outError(LogFilter.misc, String.format("AuctionHouseMgr.LoadAuctionItems: Unknown item (GUID: %1$s item entry: #%2$s) in auction, skipped.", itemGuid, itemEntry));

                continue;
            }

            var item = item.newItemOrBag(proto);

            if (!item.loadFromDB(itemGuid, ObjectGuid.create(HighGuid.Player, result.<Long>Read(51)), result.GetFields(), itemEntry)) {
                item.close();

                continue;
            }

            var auctionId = result.<Integer>Read(52);
            itemsByAuction.add(auctionId, item);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s auction items in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));

        oldMSTime = System.currentTimeMillis();
        count = 0;

        result = DB.characters.query(DB.characters.GetPreparedStatement(CharStatements.SEL_AUCTION_BIDDERS));

        if (!result.isEmpty()) {
            do {
                biddersByAuction.add(result.<Integer>Read(0), ObjectGuid.create(HighGuid.Player, result.<Long>Read(1)));
            } while (result.NextRow());
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s auction bidders in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));

        oldMSTime = System.currentTimeMillis();
        count = 0;

        result = DB.characters.query(DB.characters.GetPreparedStatement(CharStatements.SEL_AUCTIONS));

        if (!result.isEmpty()) {
            SQLTransaction trans = new SQLTransaction();

            do {
                AuctionPosting auction = new AuctionPosting();
                auction.id = result.<Integer>Read(0);
                var auctionHouseId = result.<Integer>Read(1);

                var auctionHouse = getAuctionsById(auctionHouseId);

                if (auctionHouse == null) {
                    Log.outError(LogFilter.misc, String.format("Auction %1$s has wrong auctionHouseId %2$s", auction.id, auctionHouseId));
                    var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_AUCTION);
                    stmt.AddValue(0, auction.id);
                    trans.append(stmt);

                    continue;
                }

                if (!itemsByAuction.ContainsKey(auction.id)) {
                    Log.outError(LogFilter.misc, String.format("Auction %1$s has no items", auction.id));
                    var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_AUCTION);
                    stmt.AddValue(0, auction.id);
                    trans.append(stmt);

                    continue;
                }

                auction.items = itemsByAuction.get(auction.id);
                auction.owner = ObjectGuid.create(HighGuid.Player, result.<Long>Read(2));
                auction.ownerAccount = ObjectGuid.create(HighGuid.wowAccount, global.getCharacterCacheStorage().getCharacterAccountIdByGuid(auction.owner));
                var bidder = result.<Long>Read(3);

                if (bidder != 0) {
                    auction.bidder = ObjectGuid.create(HighGuid.Player, bidder);
                }

                auction.minBid = result.<Long>Read(4);
                auction.buyoutOrUnitPrice = result.<Long>Read(5);
                auction.deposit = result.<Long>Read(6);
                auction.bidAmount = result.<Long>Read(7);
                auction.startTime = time.UnixTimeToDateTime(result.<Long>Read(8));
                auction.endTime = time.UnixTimeToDateTime(result.<Long>Read(9));
                auction.serverFlags = AuctionPostingServerFlag.forValue(result.<Byte>Read(10));

                if (biddersByAuction.ContainsKey(auction.id)) {
                    auction.bidderHistory = biddersByAuction.get(auction.id);
                }

                auctionHouse.addAuction(null, auction);

                ++count;
            } while (result.NextRow());

            DB.characters.CommitTransaction(trans);
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s auctions in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void addAItem(Item item) {
        if (item == null || itemsByGuid.containsKey(item.getGUID())) {
            return;
        }

        itemsByGuid.put(item.getGUID(), item);
    }


    public final boolean removeAItem(ObjectGuid guid, boolean deleteItem) {
        return removeAItem(guid, deleteItem, null);
    }

    public final boolean removeAItem(ObjectGuid guid) {
        return removeAItem(guid, false, null);
    }

    public final boolean removeAItem(ObjectGuid guid, boolean deleteItem, SQLTransaction trans) {
        var item = itemsByGuid.get(guid);

        if (item == null) {
            return false;
        }

        if (deleteItem) {
            item.FSetState(ItemUpdateState.removed);
            item.saveToDB(trans);
        }

        itemsByGuid.remove(guid);

        return true;
    }

    public final boolean pendingAuctionAdd(Player player, int auctionHouseId, int auctionId, long deposit) {
        var pendingAuction = pendingAuctionsByPlayer.GetOrAdd(player.getGUID(), () -> new PlayerPendingAuctions());
        // Get deposit so far
        long totalDeposit = 0;

        for (var thisAuction : pendingAuction.auctions) {
            totalDeposit += thisAuction.deposit;
        }

        // Add this deposit
        totalDeposit += deposit;

        if (!player.hasEnoughMoney(totalDeposit)) {
            return false;
        }

        pendingAuction.auctions.add(new PendingAuctionInfo(auctionId, auctionHouseId, deposit));

        return true;
    }

    public final int pendingAuctionCount(Player player) {
        var itr = pendingAuctionsByPlayer.get(player.getGUID());

        if (itr != null) {
            return itr.auctions.count;
        }

        return 0;
    }

    public final void pendingAuctionProcess(Player player) {
        var playerPendingAuctions = pendingAuctionsByPlayer.get(player.getGUID());

        if (playerPendingAuctions == null) {
            return;
        }

        long totaldeposit = 0;
        var auctionIndex = 0;

        for (; auctionIndex < playerPendingAuctions.auctions.count; ++auctionIndex) {
            var pendingAuction = playerPendingAuctions.Auctions[auctionIndex];

            if (!player.hasEnoughMoney(totaldeposit + pendingAuction.deposit)) {
                break;
            }

            totaldeposit += pendingAuction.deposit;
        }

        // expire auctions we cannot afford
        if (auctionIndex < playerPendingAuctions.auctions.count) {
            SQLTransaction trans = new SQLTransaction();

            do {
                var pendingAuction = playerPendingAuctions.Auctions[auctionIndex];
                var auction = getAuctionsById(pendingAuction.auctionHouseId).getAuction(pendingAuction.auctionId);

                if (auction != null) {
                    auction.endTime = gameTime.GetSystemTime();
                }

                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_AUCTION_EXPIRATION);
                stmt.AddValue(0, (int) gameTime.GetGameTime());
                stmt.AddValue(1, pendingAuction.auctionId);
                trans.append(stmt);
                ++auctionIndex;
            } while (auctionIndex < playerPendingAuctions.auctions.count);

            DB.characters.CommitTransaction(trans);
        }

        pendingAuctionsByPlayer.remove(player.getGUID());
        player.modifyMoney(-(long) totaldeposit);
    }

    public final void updatePendingAuctions() {
        for (var pair : pendingAuctionsByPlayer.entrySet()) {
            var playerGUID = pair.getKey();
            var player = global.getObjAccessor().findConnectedPlayer(playerGUID);

            if (player != null) {
                // Check if there were auctions since last update process if not
                if (pendingAuctionCount(player) == pair.getValue().lastAuctionsSize) {
                    pendingAuctionProcess(player);
                } else {
                    pendingAuctionsByPlayer.get(playerGUID).lastAuctionsSize = pendingAuctionCount(player);
                }
            } else {
                // Expire any auctions that we couldn't get a deposit for
                Log.outWarn(LogFilter.Auctionhouse, String.format("Player %1$s was offline, unable to retrieve deposit!", playerGUID));

                SQLTransaction trans = new SQLTransaction();

                for (var pendingAuction : pair.getValue().auctions) {
                    var auction = getAuctionsById(pendingAuction.auctionHouseId).getAuction(pendingAuction.auctionId);

                    if (auction != null) {
                        auction.endTime = gameTime.GetSystemTime();
                    }

                    var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_AUCTION_EXPIRATION);
                    stmt.AddValue(0, (int) gameTime.GetGameTime());
                    stmt.AddValue(1, pendingAuction.auctionId);
                    trans.append(stmt);
                }

                DB.characters.CommitTransaction(trans);
                pendingAuctionsByPlayer.remove(playerGUID);
            }
        }
    }

    public final void update() {
        hordeAuctions.update();
        allianceAuctions.update();
        neutralAuctions.update();
        goblinAuctions.update();

        var now = gameTime.Now();

        if (now.compareTo(playerThrottleObjectsCleanupTime) >= 0) {
            for (var pair : playerThrottleObjects.ToList()) {
                if (pair.value.periodEnd < now) {
                    playerThrottleObjects.remove(pair.key);
                }
            }

            playerThrottleObjectsCleanupTime = now + duration.FromHours(1);
        }
    }


    public final AuctionThrottleResult checkThrottle(Player player, boolean addonTainted) {
        return checkThrottle(player, addonTainted, AuctionCommand.SellItem);
    }

    public final AuctionThrottleResult checkThrottle(Player player, boolean addonTainted, AuctionCommand command) {
        var now = gameTime.Now();

        var throttleObject = playerThrottleObjects.GetOrAdd(player.getGUID(), () -> new PlayerThrottleObject());

        if (now.compareTo(throttleObject.periodEnd) > 0) {
            throttleObject.periodEnd = now + durationofMinutes(1);
            throttleObject.queriesRemaining = 100;
        }

        if (throttleObject.queriesRemaining == 0) {
            player.getSession().sendAuctionCommandResult(0, command, AuctionResult.AuctionHouseBusy, throttleObject.PeriodEnd - now);

            return new AuctionThrottleResult(duration.Zero, true);
        }

        if ((--throttleObject.queriesRemaining) == 0) {
            return new AuctionThrottleResult(throttleObject.PeriodEnd - now, false);
        } else {
            return new AuctionThrottleResult(duration.ofSeconds(WorldConfig.getIntValue(addonTainted ? WorldCfg.AuctionTaintedSearchDelay : WorldCfg.AuctionSearchDelay)), false);
        }
    }

    public final AuctionHouseRecord getAuctionHouseEntry(int factionTemplateId) {
        int houseId = 0;

        tangible.RefObject<Integer> tempRef_houseId = new tangible.RefObject<Integer>(houseId);
        var tempVar = getAuctionHouseEntry(factionTemplateId, tempRef_houseId);
        houseId = tempRef_houseId.refArgValue;
        return tempVar;
    }

    public final AuctionHouseRecord getAuctionHouseEntry(int factionTemplateId, tangible.RefObject<Integer> houseId) {
        int houseid = 1; // Auction House

        if (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionAuction)) {
            // FIXME: found way for proper auctionhouse selection by another way
            // AuctionHouse.dbc have faction field with _player_ factions associated with auction house races.
            // but no easy way convert creature faction to player race faction for specific city
            switch (factionTemplateId) {
                case 120:
                    houseid = 7;

                    break; // booty bay, Blackwater Auction House
                case 474:
                    houseid = 7;

                    break; // gadgetzan, Blackwater Auction House
                case 855:
                    houseid = 7;

                    break; // everlook, Blackwater Auction House
                default: // default
                {
                    var u_entry = CliDB.FactionTemplateStorage.get(factionTemplateId);

                    if (u_entry == null) {
                        houseid = 1; // Auction House
                    } else if ((u_entry.factionGroup & FactionMasks.Alliance.getValue()) != 0) {
                        houseid = 2; // Alliance Auction House
                    } else if ((u_entry.factionGroup & FactionMasks.Horde.getValue()) != 0) {
                        houseid = 6; // Horde Auction House
                    } else {
                        houseid = 1; // Auction House
                    }

                    break;
                }
            }
        }

        houseId.refArgValue = houseid;

        return CliDB.AuctionHouseStorage.get(houseid);
    }

    private static class PendingAuctionInfo {
        public final int auctionId;
        public final int auctionHouseId;
        public final long deposit;

        public PendingAuctionInfo(int auctionId, int auctionHouseId, long deposit) {
            auctionId = auctionId;
            auctionHouseId = auctionHouseId;
            deposit = deposit;
        }
    }

    private static class PlayerPendingAuctions {
        public final ArrayList<PendingAuctionInfo> auctions = new ArrayList<>();
        public int lastAuctionsSize;
    }

    private static class PlayerThrottleObject {
        public LocalDateTime periodEnd = LocalDateTime.MIN;
        public byte queriesRemaining = 100;
    }
}
