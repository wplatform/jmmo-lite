package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.entity.item.enums.BuyResult;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class BuyFailed extends ServerPacket {
    public ObjectGuid vendorGUID = ObjectGuid.EMPTY;
    public int muId;
    public BuyResult reason = BuyResult.CANT_FIND_ITEM;

    public BuyFailed() {
        super(ServerOpCode.SMSG_BUY_FAILED);
    }

    @Override
    public void write() {
        this.writeGuid(vendorGUID);
        this.writeInt32(muId);
        this.writeInt8(reason.ordinal());
    }
}
