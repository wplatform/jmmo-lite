package com.github.azeroth.game.networking.packet.gameobject;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

public class GameObjectActivateAnimKit extends ServerPacket {
    public ObjectGuid objectGUID = ObjectGuid.EMPTY;
    public int animKitID;
    public boolean maintain;

    public GameObjectActivateAnimKit() {
        super(ServerOpcode.GameObjectActivateAnimKit);
    }

    @Override
    public void write() {
        this.writeGuid(objectGUID);
        this.writeInt32(animKitID);
        this.writeBit(maintain);
        this.flushBits();
    }
}
