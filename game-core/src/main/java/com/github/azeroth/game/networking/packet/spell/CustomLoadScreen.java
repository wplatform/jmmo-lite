package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;

public class CustomLoadScreen extends ServerPacket {
    private final int teleportSpellID;
    private final int loadingScreenID;

    public CustomLoadScreen(int teleportSpellId, int loadingScreenId) {
        super(ServerOpcode.CustomLoadScreen);
        teleportSpellID = teleportSpellId;
        loadingScreenID = loadingScreenId;
    }

    @Override
    public void write() {
        this.writeInt32(teleportSpellID);
        this.writeInt32(loadingScreenID);
    }
}
