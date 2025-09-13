package com.github.azeroth.game.networking.packet.chat;


public class STextEmote extends ServerPacket {
    public ObjectGuid sourceGUID = ObjectGuid.EMPTY;
    public ObjectGuid sourceAccountGUID = ObjectGuid.EMPTY;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public int soundIndex = -1;
    public int emoteID;

    public STextEmote() {
        super(ServerOpcode.TextEmote);
    }

    @Override
    public void write() {
        this.writeGuid(sourceGUID);
        this.writeGuid(sourceAccountGUID);
        this.writeInt32(emoteID);
        this.writeInt32(soundIndex);
        this.writeGuid(targetGUID);
    }
}
