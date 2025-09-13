package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.ServerPacket;

public class AuctionCommandResult extends ServerPacket {
    public int auctionID;

    /**
     * < the id of the auction that triggered this notification
     */
    public int command;

    /**
     * < the type of action that triggered this notification. Possible values are @ ref AuctionAction
     */
    public int errorCode;

    /**
     * < the error code that was generated when trying to perform the action. Possible values are @ ref AuctionError
     */
    public int bagResult;

    /**
     * < the bid error. Possible values are @ ref AuctionError
     */
    public ObjectGuid guid = ObjectGuid.EMPTY;

    /**
     * < the GUID of the bidder for this auction.
     */
    public long minIncrement;

    /**
     * < the sum of outbid is (1% of current bid) * 5, if the bid is too small, then this value is 1 copper.
     */
    public long money;

    /**
     * < the amount of money that the player bid in copper
     */
    public int desiredDelay;

    public AuctionCommandResult() {
        super(ServerOpcode.AuctionCommandResult);
    }

    @Override
    public void write() {
        this.writeInt32(auctionID);
        this.writeInt32(command);
        this.writeInt32(errorCode);
        this.writeInt32(bagResult);
        this.writeGuid(guid);
        this.writeInt64(minIncrement);
        this.writeInt64(money);
        this.writeInt32(desiredDelay);
    }
}
