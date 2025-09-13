package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class StartPurchase extends ClientPacket {
    private ObjectGuid targetCharacter = ObjectGuid.EMPTY;
    private int clientToken = 0;
    private int productID = 0;

    public StartPurchase(WorldPacket packet) {
        super(packet);
    }

    public final ObjectGuid getTargetCharacter() {
        return targetCharacter;
    }

    public final void setTargetCharacter(ObjectGuid value) {
        targetCharacter = value;
    }

    public final int getClientToken() {
        return clientToken;
    }

    public final void setClientToken(int value) {
        clientToken = value;
    }

    public final int getProductID() {
        return productID;
    }

    public final void setProductID(int value) {
        productID = value;
    }

    @Override
    public void read() {
        setClientToken(this.readUInt());
        setProductID(this.readUInt());
        setTargetCharacter(this.readPackedGuid());
    }
}
