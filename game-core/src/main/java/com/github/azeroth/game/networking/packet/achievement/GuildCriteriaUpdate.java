package com.github.azeroth.game.networking.packet.achievement;


import java.util.ArrayList;


public class GuildCriteriaUpdate extends ServerPacket {
    public ArrayList<GuildCriteriaprogress> progress = new ArrayList<>();

    public GuildCriteriaUpdate() {
        super(ServerOpcode.GuildCriteriaUpdate);
    }

    @Override
    public void write() {
        this.writeInt32(progress.size());

        for (var progress : progress) {
            this.writeInt32(progress.criteriaID);
            this.writeInt64(progress.dateCreated);
            this.writeInt64(progress.dateStarted);
            this.writePackedTime(progress.dateUpdated);
            this.writeInt32(0); // this is a hack. this is a packed time written as int64 (progress.dateUpdated)
            this.writeInt64(progress.quantity);
            this.writeGuid(progress.playerGUID);
            this.writeInt32(progress.flags);
        }
    }
}
