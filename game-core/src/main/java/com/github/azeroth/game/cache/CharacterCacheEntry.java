package com.github.azeroth.game.cache;


public class CharacterCacheEntry {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public String name;
    public int accountId;
    public PlayerClass classId = playerClass.values()[0];
    public Race raceId = race.values()[0];
    public Gender sex = gender.values()[0];
    public byte level;
    public long guildId;
    public int[] arenaTeamId = new int[SharedConst.MaxArenaSlot];
    public boolean isDeleted;
}
