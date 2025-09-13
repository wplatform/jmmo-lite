package com.github.azeroth.game.networking.packet.achievement;

public final class GuildCriteriaProgress {

    public int criteriaID;
    public long dateCreated;
    public long dateStarted;
    public long dateUpdated;

    public long quantity;
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public int flags;

    public GuildCriteriaProgress clone() {
        GuildCriteriaProgress varCopy = new GuildCriteriaProgress();

        varCopy.criteriaID = this.criteriaID;
        varCopy.dateCreated = this.dateCreated;
        varCopy.dateStarted = this.dateStarted;
        varCopy.dateUpdated = this.dateUpdated;
        varCopy.quantity = this.quantity;
        varCopy.playerGUID = this.playerGUID;
        varCopy.flags = this.flags;

        return varCopy;
    }
}
