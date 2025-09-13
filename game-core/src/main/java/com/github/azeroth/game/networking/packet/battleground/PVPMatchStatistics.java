package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class PVPMatchStatistics {
    public ArrayList<PVPMatchPlayerstatistics> statistics = new ArrayList<>();
    public RatingData ratings;
    public byte[] playerCount = new byte[2];

    public final void write(WorldPacket data) {
        data.writeBit(ratings != null);
        data.writeInt32(statistics.size());

        for (var count : playerCount) {
            data.writeInt8(count);
        }

        if (ratings != null) {
            ratings.write(data);
        }

        for (var player : statistics) {
            player.write(data);
        }
    }

    public static class RatingData {
        public int[] prematch = new int[2];
        public int[] postmatch = new int[2];
        public int[] prematchMMR = new int[2];

        public final void write(WorldPacket data) {
            for (var id : prematch) {
                data.writeInt32(id);
            }

            for (var id : postmatch) {
                data.writeInt32(id);
            }

            for (var id : prematchMMR) {
                data.writeInt32(id);
            }
        }
    }

    public final static class HonorData {
        public int honorKills;
        public int deaths;
        public int contributionPoints;

        public void write(WorldPacket data) {
            data.writeInt32(honorKills);
            data.writeInt32(deaths);
            data.writeInt32(contributionPoints);
        }

        public HonorData clone() {
            HonorData varCopy = new HonorData();

            varCopy.honorKills = this.honorKills;
            varCopy.deaths = this.deaths;
            varCopy.contributionPoints = this.contributionPoints;

            return varCopy;
        }
    }

    public final static class PVPMatchPlayerPVPStat {
        public int pvpStatID;
        public int pvpStatValue;

        public PVPMatchPlayerPVPStat() {
        }

        public PVPMatchPlayerPVPStat(int pvpStatID, int pvpStatValue) {
            pvpStatID = pvpStatID;
            pvpStatValue = pvpStatValue;
        }

        public void write(WorldPacket data) {
            data.writeInt32(pvpStatID);
            data.writeInt32(pvpStatValue);
        }

        public PVPMatchPlayerPVPStat clone() {
            PVPMatchPlayerPVPStat varCopy = new PVPMatchPlayerPVPStat();

            varCopy.pvpStatID = this.pvpStatID;
            varCopy.pvpStatValue = this.pvpStatValue;

            return varCopy;
        }
    }

    public static class PVPMatchPlayerStatistics {
        public ObjectGuid playerGUID = ObjectGuid.EMPTY;
        public int kills;
        public byte faction;
        public boolean isInWorld;
        public honorData honor = null;
        public int damageDone;
        public int healingDone;
        public Integer preMatchRating = null;
        public Integer ratingChange = null;
        public Integer preMatchMMR = null;
        public Integer mmrChange = null;
        public ArrayList<PVPMatchPlayerPVPStat> stats = new ArrayList<>();
        public int primaryTalentTree;
        public int sex;
        public Race playerRace = race.values()[0];
        public int playerClass;
        public int creatureID;
        public int honorLevel;
        public int role;

        public final void write(WorldPacket data) {
            data.writeGuid(playerGUID);
            data.writeInt32(kills);
            data.writeInt32(damageDone);
            data.writeInt32(healingDone);
            data.writeInt32(stats.size());
            data.writeInt32(primaryTalentTree);
            data.writeInt32(sex);
            data.writeInt32((int) playerRace.getValue());
            data.writeInt32(playerClass);
            data.writeInt32(creatureID);
            data.writeInt32(honorLevel);
            data.writeInt32(role);

            for (var pvpStat : stats) {
                pvpStat.write(data);
            }

            data.writeBit(faction != 0);
            data.writeBit(isInWorld);
            data.writeBit(honor != null);
            data.writeBit(preMatchRating != null);
            data.writeBit(ratingChange != null);
            data.writeBit(preMatchMMR != null);
            data.writeBit(mmrChange != null);
            data.flushBits();

            if (honor != null) {
                honor.getValue().write(data);
            }

            if (preMatchRating != null) {
                data.writeInt32(preMatchRating.intValue());
            }

            if (ratingChange != null) {
                data.writeInt32(ratingChange.intValue());
            }

            if (preMatchMMR != null) {
                data.writeInt32(preMatchMMR.intValue());
            }

            if (mmrChange != null) {
                data.writeInt32(mmrChange.intValue());
            }
        }
    }
}
