package com.github.azeroth.game.networking.packet.bpay;


public class UpgradeStarted extends ServerPacket {
    private ObjectGuid characterGUID = ObjectGuid.EMPTY;

    public UpgradeStarted() {
        super(ServerOpcode.CharacterUpgradeStarted);
    }

    public final ObjectGuid getCharacterGUID() {
        return characterGUID;
    }

    public final void setCharacterGUID(ObjectGuid value) {
        characterGUID = value;
    }

    @Override
    public void write() {
        this.write(getCharacterGUID());
    }
}
