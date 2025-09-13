package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.networking.ServerPacket;

public class BlackMarketWon extends ServerPacket {
    public int marketID;
    public itemInstance item;
    public int randomPropertiesID;

    public BlackMarketWon() {
        super(ServerOpcode.BlackMarketWon);
    }

    @Override
    public void write() {
        this.writeInt32(marketID);
        this.writeInt32(randomPropertiesID);
        item.write(this);
    }
}
