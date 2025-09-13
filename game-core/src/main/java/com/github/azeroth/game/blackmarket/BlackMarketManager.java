package com.github.azeroth.game.blackmarket;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.mail.MailDraft;
import com.github.azeroth.game.mail.MailReceiver;
import com.github.azeroth.game.mail.MailSender;
import com.github.azeroth.game.networking.packet.BlackMarketItem;
import com.github.azeroth.game.networking.packet.BlackMarketRequestItemsResult;

import java.util.ArrayList;
import java.util.HashMap;


public class BlackMarketManager {
    private final HashMap<Integer, BlackMarketEntry> auctions = new HashMap<Integer, BlackMarketEntry>();
    private final HashMap<Integer, BlackMarketTemplate> templates = new HashMap<Integer, BlackMarketTemplate>();
    private long lastUpdate;


    private BlackMarketManager() {
    }

    public final boolean isEnabled() {
        return WorldConfig.getBoolValue(WorldCfg.BlackmarketEnabled);
    }

    public final long getLastUpdate() {
        return lastUpdate;
    }

    public final void loadTemplates() {
        var oldMSTime = System.currentTimeMillis();

        // Clear in case we are reloading
        templates.clear();

        var result = DB.World.query("SELECT marketId, sellerNpc, itemEntry, quantity, minBid, duration, chance, bonusListIDs FROM blackmarket_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 black market templates. DB table `blackmarket_template` is empty.");

            return;
        }

        do {
            BlackMarketTemplate templ = new BlackMarketTemplate();

            if (!templ.loadFromDB(result.GetFields())) // Add checks
            {
                continue;
            }

            addTemplate(templ);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} black market templates in {1} ms.", templates.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadAuctions() {
        var oldMSTime = System.currentTimeMillis();

        // Clear in case we are reloading
        auctions.clear();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_BLACKMARKET_AUCTIONS);
        var result = DB.characters.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 black market auctions. DB table `blackmarket_auctions` is empty.");

            return;
        }

        lastUpdate = gameTime.GetGameTime(); //Set update time before loading

        SQLTransaction trans = new SQLTransaction();

        do {
            BlackMarketEntry auction = new BlackMarketEntry();

            if (!auction.loadFromDB(result.GetFields())) {
                auction.deleteFromDB(trans);

                continue;
            }

            if (auction.isCompleted()) {
                auction.deleteFromDB(trans);

                continue;
            }

            addAuction(auction);
        } while (result.NextRow());

        DB.characters.CommitTransaction(trans);

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} black market auctions in {1} ms.", auctions.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }


    public final void update() {
        update(false);
    }

    public final void update(boolean updateTime) {
        SQLTransaction trans = new SQLTransaction();
        var now = gameTime.GetGameTime();

        for (var entry : auctions.values()) {
            if (entry.isCompleted() && entry.bidder != 0) {
                sendAuctionWonMail(entry, trans);
            }

            if (updateTime) {
                entry.update(now);
            }
        }

        if (updateTime) {
            lastUpdate = now;
        }

        DB.characters.CommitTransaction(trans);
    }

    public final void refreshAuctions() {
        SQLTransaction trans = new SQLTransaction();

        // Delete completed auctions
        for (var pair : auctions.entrySet()) {
            if (!pair.getValue().isCompleted()) {
                continue;
            }

            pair.getValue().deleteFromDB(trans);
            auctions.remove(pair.getKey());
        }

        DB.characters.CommitTransaction(trans);
        trans = new SQLTransaction();

        ArrayList<BlackMarketTemplate> templates = new ArrayList<>();

        for (var pair : templates.entrySet()) {
            if (getAuctionByID(pair.getValue().marketID) != null) {
                continue;
            }

            if (!RandomUtil.randChance(pair.getValue().chance)) {
                continue;
            }

            templates.add(pair.getValue());
        }

        templates.RandomResize(WorldConfig.getUIntValue(WorldCfg.BlackmarketMaxAuctions));

        for (var templat : templates) {
            BlackMarketEntry entry = new BlackMarketEntry();
            entry.initialize(templat.marketID, (int) templat.duration);
            entry.saveToDB(trans);
            addAuction(entry);
        }

        DB.characters.CommitTransaction(trans);

        update(true);
    }


    public final void buildItemsResponse(BlackMarketRequestItemsResult packet, Player player) {
        packet.lastUpdateID = (int) lastUpdate;

        for (var pair : auctions.entrySet()) {
            var templ = pair.getValue().getTemplate();

            BlackMarketItem item = new BlackMarketItem();
            item.marketID = pair.getValue().MarketId;
            item.sellerNPC = templ.sellerNPC;
            item.item = templ.item;
            item.quantity = templ.quantity;

            // No bids yet
            if (pair.getValue().numBids == 0) {
                item.minBid = templ.minBid;
                item.minIncrement = 1;
            } else {
                item.minIncrement = pair.getValue().minIncrement; // 5% increment minimum
                item.minBid = pair.getValue().currentBid + item.minIncrement;
            }

            item.currentBid = pair.getValue().currentBid;
            item.secondsRemaining = pair.getValue().getSecondsRemaining();
            item.highBid = (pair.getValue().bidder == player.getGUID().getCounter());
            item.numBids = pair.getValue().numBids;

            packet.items.add(item);
        }
    }

    public final void addAuction(BlackMarketEntry auction) {
        auctions.put(auction.getMarketId(), auction);
    }

    public final void addTemplate(BlackMarketTemplate templ) {
        templates.put(templ.marketID, templ);
    }

    public final void sendAuctionWonMail(BlackMarketEntry entry, SQLTransaction trans) {
        // Mail already sent
        if (entry.getMailSent()) {
            return;
        }

        int bidderAccId;
        var bidderGuid = ObjectGuid.create(HighGuid.Player, entry.getBidder());
        var bidder = global.getObjAccessor().findConnectedPlayer(bidderGuid);
        // data for gm.log
        var bidderName = "";
        boolean logGmTrade;

        if (bidder) {
            bidderAccId = bidder.getSession().getAccountId();
            bidderName = bidder.getName();
            logGmTrade = bidder.getSession().hasPermission(RBACPermissions.LogGmTrade);
        } else {
            bidderAccId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(bidderGuid);

            if (bidderAccId == 0) // Account exists
            {
                return;
            }

            logGmTrade = global.getAccountMgr().hasPermission(bidderAccId, RBACPermissions.LogGmTrade, global.getWorldMgr().getRealmId().index);

            tangible.OutObject<String> tempOut_bidderName = new tangible.OutObject<String>();
            if (logGmTrade && !global.getCharacterCacheStorage().getCharacterNameByGuid(bidderGuid, tempOut_bidderName)) {
                bidderName = tempOut_bidderName.outArgValue;
                bidderName = global.getObjectMgr().getSysMessage(SysMessage.unknown);
            } else {
                bidderName = tempOut_bidderName.outArgValue;
            }
        }

        // Create item
        var templ = entry.getTemplate();
        var item = item.createItem(templ.item.itemID, templ.quantity, itemContext.BlackMarket);

        if (!item) {
            return;
        }

        if (templ.item.itemBonus != null) {
            for (var bonusList : templ.item.itemBonus.bonusListIDs) {
                item.addBonuses(bonusList);
            }
        }

        item.setOwnerGUID(bidderGuid);

        item.saveToDB(trans);

        // Log trade
        if (logGmTrade) {
            Log.outCommand(bidderAccId, "GM {0} (Account: {1}) won item in blackmarket auction: {2} (Entry: {3} Count: {4}) and payed gold : {5}.", bidderName, bidderAccId, item.getTemplate().getName(), item.getEntry(), item.getCount(), entry.getCurrentBid() / MoneyConstants.gold);
        }

        if (bidder) {
            bidder.getSession().sendBlackMarketWonNotification(entry, item);
        }

        (new MailDraft(entry.buildAuctionMailSubject(BMAHMailAuctionAnswers.Won), entry.buildAuctionMailBody())).addItem(item).sendMailTo(trans, new MailReceiver(bidder, entry.getBidder()), new MailSender(entry), MailCheckMask.Copied);

        entry.setMailSent();
    }

    public final void sendAuctionOutbidMail(BlackMarketEntry entry, SQLTransaction trans) {
        var oldBidder_guid = ObjectGuid.create(HighGuid.Player, entry.getBidder());
        var oldBidder = global.getObjAccessor().findConnectedPlayer(oldBidder_guid);

        int oldBidder_accId = 0;

        if (!oldBidder) {
            oldBidder_accId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(oldBidder_guid);
        }

        // old bidder exist
        if (!oldBidder && oldBidder_accId == 0) {
            return;
        }

        if (oldBidder) {
            oldBidder.getSession().sendBlackMarketOutbidNotification(entry.getTemplate());
        }

        (new MailDraft(entry.buildAuctionMailSubject(BMAHMailAuctionAnswers.Outbid), entry.buildAuctionMailBody())).addMoney(entry.getCurrentBid()).sendMailTo(trans, new MailReceiver(oldBidder, entry.getBidder()), new MailSender(entry), MailCheckMask.Copied);
    }

    public final BlackMarketEntry getAuctionByID(int marketId) {
        return auctions.get(marketId);
    }

    public final BlackMarketTemplate getTemplateByID(int marketId) {
        return templates.get(marketId);
    }
}
