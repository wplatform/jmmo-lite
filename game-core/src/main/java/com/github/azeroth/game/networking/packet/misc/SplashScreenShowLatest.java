package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class SplashScreenShowLatest extends ServerPacket {
    public int UISplashScreenID;

    public SplashScreenShowLatest() {
        super(ServerOpcode.SplashScreenShowLatest);
    }

    @Override
    public void write() {
        this.writeInt32(UISplashScreenID);
    }
}
