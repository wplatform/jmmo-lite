package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.networking.ServerPacket;

public class InitialSetup extends ServerPacket {

    public byte serverExpansionTier;

    public byte serverExpansionLevel;

    public InitialSetup() {
        super(ServerOpcode.InitialSetup);
    }

    @Override
    public void write() {
        this.writeInt8(serverExpansionLevel);
        this.writeInt8(serverExpansionTier);
    }
}
