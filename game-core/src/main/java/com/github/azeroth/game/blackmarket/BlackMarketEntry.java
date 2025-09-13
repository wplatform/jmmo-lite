package com.github.azeroth.game.blackmarket;


import com.github.azeroth.game.entity.player.Player;

public class BlackMarketEntry {
    private int marketId;
    private long currentBid;
    private int numBids;
    private long bidder;
    private int secondsRemaining;
    private boolean mailSent;


    public final int getMarketId() {
        return marketId;
    }

    public final long getCurrentBid() {
        return currentBid;
    }

    private void setCurrentBid(long value) {
        currentBid = value;
    }

    public final int getNumBids() {
        return numBids;
    }

    private void setNumBids(int value) {
        numBids = value;
    }

    public final long getBidder() {
        return bidder;
    }

    private void setBidder(long value) {
        bidder = value;
    }

    public final long getMinIncrement() {
        return (_currentBid / 20) - ((_currentBid / 20) % MoneyConstants.gold);
    }

    public final boolean getMailSent() {
        return mailSent;
    }

    public final void initialize(int marketId, int duration) {
        marketId = marketId;
        secondsRemaining = duration;
    }

    public final void update(long newTimeOfUpdate) {
        secondsRemaining = (int) (_secondsRemaining - (newTimeOfUpdate - global.getBlackMarketMgr().getLastUpdate()));
    }

    public final BlackMarketTemplate getTemplate() {
        return global.getBlackMarketMgr().getTemplateByID(marketId);
    }

    public final int getSecondsRemaining() {
        return (int) (_secondsRemaining - (gameTime.GetGameTime() - global.getBlackMarketMgr().getLastUpdate()));
    }

    public final boolean isCompleted() {
        return getSecondsRemaining() <= 0;
    }

    public final boolean loadFromDB(SQLFields fields) {
        marketId = fields.<Integer>Read(0);

        // Invalid MarketID
        var templ = global.getBlackMarketMgr().getTemplateByID(marketId);

        if (templ == null) {
            Log.outError(LogFilter.misc, "Black market auction {0} does not have a valid id.", marketId);

            return false;
        }

        currentBid = fields.<Long>Read(1);
        secondsRemaining = (int) (fields.<Long>Read(2) - global.getBlackMarketMgr().getLastUpdate());
        numBids = fields.<Integer>Read(3);
        bidder = fields.<Long>Read(4);

        // Either no bidder or existing player
        if (bidder != 0 && global.getCharacterCacheStorage().getCharacterAccountIdByGuid(ObjectGuid.create(HighGuid.Player, bidder)) == 0) // Probably a better way to check if player exists
        {
            Log.outError(LogFilter.misc, "Black market auction {0} does not have a valid bidder (GUID: {1}).", marketId, bidder);

            return false;
        }

        return true;
    }

    public final void saveToDB(SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_BLACKMARKET_AUCTIONS);

        stmt.AddValue(0, marketId);
        stmt.AddValue(1, currentBid);
        stmt.AddValue(2, getExpirationTime());
        stmt.AddValue(3, numBids);
        stmt.AddValue(4, bidder);

        trans.append(stmt);
    }

    public final void deleteFromDB(SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_BLACKMARKET_AUCTIONS);
        stmt.AddValue(0, marketId);
        trans.append(stmt);
    }

    public final boolean validateBid(long bid) {
        if (bid <= currentBid) {
            return false;
        }

        if (bid < currentBid + getMinIncrement()) {
            return false;
        }

        if (bid >= BlackMarketConst.MaxBid) {
            return false;
        }

        return true;
    }

    public final void placeBid(long bid, Player player, SQLTransaction trans) //Updated
    {
        if (bid < currentBid) {
            return;
        }

        currentBid = bid;
        ++numBids;

        if (getSecondsRemaining() < 30 * time.Minute) {
            secondsRemaining += 30 * time.Minute;
        }

        bidder = player.getGUID().getCounter();

        player.modifyMoney(-(long) bid);


        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_BLACKMARKET_AUCTIONS);

        stmt.AddValue(0, currentBid);
        stmt.AddValue(1, getExpirationTime());
        stmt.AddValue(2, numBids);
        stmt.AddValue(3, bidder);
        stmt.AddValue(4, marketId);

        trans.append(stmt);

        global.getBlackMarketMgr().update(true);
    }

    public final String buildAuctionMailSubject(BMAHMailAuctionAnswers response) {
        return getTemplate().item.itemID + ":0:" + response + ':' + getMarketId() + ':' + getTemplate().quantity;
    }

    public final String buildAuctionMailBody() {
        return getTemplate().sellerNPC + ":" + currentBid;
    }

    public final void setMailSent() {
        mailSent = true;
    } // Set when mail has been sent

    private long getExpirationTime() {
        return gameTime.GetGameTime() + getSecondsRemaining();
    }
}
