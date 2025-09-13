package com.github.azeroth.game.networking.packet.gameobject;


import com.github.azeroth.game.networking.ServerPacket;

class GameObjectCustomAnim extends ServerPacket {
    public ObjectGuid objectGUID = ObjectGuid.EMPTY;

    public int customAnim;
    public boolean playAsDespawn;

    public GameObjectCustomAnim() {

        super(ServerOpcode.GameObjectCustomAnim);
    }

    @Override
    public void write() {
        this.writeGuid(objectGUID);
        this.writeInt32(customAnim);
        this.writeBit(playAsDespawn);
        this.flushBits();
    }
}
