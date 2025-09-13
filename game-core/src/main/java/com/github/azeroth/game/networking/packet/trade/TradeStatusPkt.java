package com.github.azeroth.game.networking.packet.trade;


import com.github.azeroth.defines.TradeStatus;
import com.github.azeroth.game.entity.item.enums.InventoryResult;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class TradeStatusPkt extends ServerPacket {
    public TradeStatus status = TradeStatus.INITIATED;
    public byte tradeSlot;
    public ObjectGuid partnerAccount = ObjectGuid.EMPTY;
    public ObjectGuid partner = ObjectGuid.EMPTY;
    public int currencyType;
    public int currencyQuantity;
    public boolean failureForYou;
    public InventoryResult bagResult = InventoryResult.OK;
    public int itemID;
    public int id;
    public boolean partnerIsSameBnetAccount;

    public TradeStatusPkt() {
        super(ServerOpCode.SMSG_TRADE_STATUS);
    }

    @Override
    public void write() {
        this.writeBit(partnerIsSameBnetAccount);
        this.writeBits(status.ordinal(), 5);

        switch (status) {
            case FAILED:
                this.writeBit(failureForYou);
                this.writeInt32(bagResult.ordinal());
                this.writeInt32(itemID);

                break;
            case INITIATED:
                this.writeInt32(id);

                break;
            case PROPOSED:
                this.writeGuid(partner);
                this.writeGuid(partnerAccount);

                break;
            case WRONG_REALM:
            case NOT_ON_TAPLIST:
                this.writeInt8(tradeSlot);

                break;
            case NOT_ENOUGH_CURRENCY:
            case CURRENCY_NOT_TRADABLE:
                this.writeInt32(currencyType);
                this.writeInt32(currencyQuantity);

                break;
            default:
                this.flushBits();

                break;
        }
    }
}
