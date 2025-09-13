package com.github.azeroth.game.networking.packet.petition;


public class ServerPetitionShowList extends ServerPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;
    public int price = 0;

    public ServerPetitionShowList() {
        super(ServerOpcode.PetitionShowList);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeInt32(price);
    }
}
