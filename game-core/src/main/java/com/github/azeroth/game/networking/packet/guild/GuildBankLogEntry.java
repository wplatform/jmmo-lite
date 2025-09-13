package com.github.azeroth.game.networking.packet.guild;

public class GuildBankLogEntry {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public int timeOffset;
    public byte entryType;
    public Long money = null;
    public Integer itemID = null;
    public Integer count = null;
    public Byte otherTab = null;
}
