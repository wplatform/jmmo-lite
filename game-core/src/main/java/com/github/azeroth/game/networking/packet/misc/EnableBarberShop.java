package com.github.azeroth.game.networking.packet.misc;


public class EnableBarberShop extends ServerPacket {
    public EnableBarberShop() {
        super(ServerOpcode.EnableBarberShop);
    }

    @Override
    public void write() {
    }
}
