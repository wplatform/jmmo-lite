package com.github.azeroth.game.battleground;


public final class BattlegroundQueueTypeId {
    public short battlemasterListId;
    public byte bgType;
    public boolean rated;
    public byte teamSize;

    public battlegroundQueueTypeId() {
    }

    public battlegroundQueueTypeId(short battlemasterListId, byte bgType, boolean rated, byte teamSize) {
        battlemasterListId = battlemasterListId;
        bgType = bgType;
        rated = rated;
        teamSize = teamSize;
    }

    public static BattlegroundQueueTypeId fromPacked(long packedQueueId) {
        return new battlegroundQueueTypeId((short) (packedQueueId & 0xFFFF), (byte) ((packedQueueId >>> 16) & 0xF), ((packedQueueId >>> 20) & 1) != 0, (byte) ((packedQueueId >>> 24) & 0x3F));
    }

    public static boolean opEquals(BattlegroundQueueTypeId left, BattlegroundQueueTypeId right) {
        return left.battlemasterListId == right.battlemasterListId && left.bgType == right.bgType && left.rated == right.rated && left.teamSize == right.teamSize;
    }

    public static boolean opNotEquals(BattlegroundQueueTypeId left, BattlegroundQueueTypeId right) {
        return !(BattlegroundQueueTypeId.opEquals(left, right));
    }

    public static boolean opLessThan(BattlegroundQueueTypeId left, BattlegroundQueueTypeId right) {
        if (left.battlemasterListId != right.battlemasterListId) {
            return left.battlemasterListId < right.battlemasterListId;
        }

        if (left.bgType != right.bgType) {
            return left.bgType < right.bgType;
        }

        if (left.rated != right.rated) {
            return (left.Rated ? 1 : 0) < (right.Rated ? 1 : 0);
        }

        return left.teamSize < right.teamSize;
    }

    public static boolean opGreaterThan(BattlegroundQueueTypeId left, BattlegroundQueueTypeId right) {
        if (left.battlemasterListId != right.battlemasterListId) {
            return left.battlemasterListId > right.battlemasterListId;
        }

        if (left.bgType != right.bgType) {
            return left.bgType > right.bgType;
        }

        if (left.rated != right.rated) {
            return (left.Rated ? 1 : 0) > (right.Rated ? 1 : 0);
        }

        return left.teamSize > right.teamSize;
    }

    public long getPacked() {
        return (long) battlemasterListId | ((long) (bgType & 0xF) << 16) | ((long) (Rated ? 1 : 0) << 20) | ((long) (teamSize & 0x3F) << 24) | 0x1F10000000000000;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (new SHORT(battlemasterListId)).hashCode() ^ (new Byte(bgType)).hashCode() ^ (new Boolean(rated)).hashCode() ^ (new Byte(teamSize)).hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ BattlemasterListId: %1$s, Type: %2$s, Rated: %3$s, TeamSize: %4$s }", battlemasterListId, bgType, rated, teamSize);
    }

    public BattlegroundQueueTypeId clone() {
        BattlegroundQueueTypeId varCopy = new battlegroundQueueTypeId();

        varCopy.battlemasterListId = this.battlemasterListId;
        varCopy.bgType = this.bgType;
        varCopy.rated = this.rated;
        varCopy.teamSize = this.teamSize;

        return varCopy;
    }
}
