package com.github.azeroth.game.networking.packet.guild;

public class GuildEventEntry {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public ObjectGuid otherGUID = ObjectGuid.EMPTY;
    public byte transactionType;
    public byte rankID;
    public int transactionDate;
}
