package com.github.azeroth.game.cache;


import com.github.azeroth.game.arena.ArenaTeam;
import com.github.azeroth.game.networking.packet.InvalidatePlayer;

import java.util.HashMap;


public class CharacterCache {
    private final HashMap<ObjectGuid, CharacterCacheEntry> characterCacheStore = new HashMap<ObjectGuid, CharacterCacheEntry>();
    private final HashMap<String, CharacterCacheEntry> characterCacheByNameStore = new HashMap<String, CharacterCacheEntry>();

    private CharacterCache() {
    }

    public final void loadCharacterCacheStorage() {
        characterCacheStore.clear();
        var oldMSTime = System.currentTimeMillis();

        var result = DB.characters.query("SELECT guid, name, account, race, gender, class, level, deleteDate FROM character");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "No character name data loaded, empty query");

            return;
        }

        do {
            addCharacterCacheEntry(ObjectGuid.create(HighGuid.Player, result.<Long>Read(0)), result.<Integer>Read(2), result.<String>Read(1), result.<Byte>Read(4), result.<Byte>Read(3), result.<Byte>Read(5), result.<Byte>Read(6), result.<Integer>Read(7) != 0);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded character infos for %1$s character in %2$s ms", characterCacheStore.size(), time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void addCharacterCacheEntry(ObjectGuid guid, int accountId, String name, byte gender, byte race, byte playerClass, byte level, boolean isDeleted) {
        var data = new CharacterCacheEntry();
        data.guid = guid;
        data.name = name;
        data.accountId = accountId;
        data.raceId = race.forValue(race);
        data.sex = gender.forValue((byte) gender);
        data.classId = playerClass.forValue(playerClass);
        data.level = level;
        data.guildId = 0; // Will be set in guild loading or guild setting

        for (byte i = 0; i < SharedConst.MaxArenaSlot; ++i) {
            data.ArenaTeamId[i] = 0; // Will be set in arena teams loading
        }

        data.isDeleted = isDeleted;

        // Fill Name to Guid Store
        characterCacheByNameStore.put(name, data);
        characterCacheStore.put(guid, data);
    }

    public final void deleteCharacterCacheEntry(ObjectGuid guid, String name) {
        characterCacheStore.remove(guid);
        characterCacheByNameStore.remove(name);
    }


    public final void updateCharacterData(ObjectGuid guid, String name, Byte gender) {
        updateCharacterData(guid, name, gender, null);
    }

    public final void updateCharacterData(ObjectGuid guid, String name) {
        updateCharacterData(guid, name, null, null);
    }

    public final void updateCharacterData(ObjectGuid guid, String name, Byte gender, Byte race) {
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return;
        }

        var oldName = characterCacheEntry.name;
        characterCacheEntry.name = name;

        if (gender != null) {
            characterCacheEntry.sex = gender.forValue((byte) gender.byteValue());
        }

        if (race != null) {
            characterCacheEntry.raceId = race.forValue(race.byteValue());
        }

        InvalidatePlayer invalidatePlayer = new InvalidatePlayer();
        invalidatePlayer.guid = guid;
        global.getWorldMgr().sendGlobalMessage(invalidatePlayer);

        // Correct name -> pointer storage
        characterCacheByNameStore.remove(oldName);
        characterCacheByNameStore.put(name, characterCacheEntry);
    }

    public final void updateCharacterGender(ObjectGuid guid, byte gender) {
        TValue p;
        if (!(characterCacheStore.containsKey(guid) && (p = characterCacheStore.get(guid)) == p)) {
            return;
        }

        p.sex = gender.forValue((byte) gender);
    }

    public final void updateCharacterLevel(ObjectGuid guid, byte level) {
        TValue p;
        if (!(characterCacheStore.containsKey(guid) && (p = characterCacheStore.get(guid)) == p)) {
            return;
        }

        p.level = level;
    }

    public final void updateCharacterAccountId(ObjectGuid guid, int accountId) {
        TValue p;
        if (!(characterCacheStore.containsKey(guid) && (p = characterCacheStore.get(guid)) == p)) {
            return;
        }

        p.accountId = accountId;
    }

    public final void updateCharacterGuildId(ObjectGuid guid, long guildId) {
        TValue p;
        if (!(characterCacheStore.containsKey(guid) && (p = characterCacheStore.get(guid)) == p)) {
            return;
        }

        p.guildId = guildId;
    }

    public final void updateCharacterArenaTeamId(ObjectGuid guid, byte slot, int arenaTeamId) {
        TValue p;
        if (!(characterCacheStore.containsKey(guid) && (p = characterCacheStore.get(guid)) == p)) {
            return;
        }

        p.ArenaTeamId[slot] = arenaTeamId;
    }


    public final void updateCharacterInfoDeleted(ObjectGuid guid, boolean deleted) {
        updateCharacterInfoDeleted(guid, deleted, null);
    }

    public final void updateCharacterInfoDeleted(ObjectGuid guid, boolean deleted, String name) {
        TValue p;
        if (!(characterCacheStore.containsKey(guid) && (p = characterCacheStore.get(guid)) == p)) {
            return;
        }

        p.isDeleted = deleted;

        if (!name.isEmpty()) {
            p.name = name;
        }
    }

    public final boolean hasCharacterCacheEntry(ObjectGuid guid) {
        return characterCacheStore.containsKey(guid);
    }

    public final CharacterCacheEntry getCharacterCacheByGuid(ObjectGuid guid) {
        return characterCacheStore.get(guid);
    }

    public final CharacterCacheEntry getCharacterCacheByName(String name) {
        return characterCacheByNameStore.get(name);
    }

    public final ObjectGuid getCharacterGuidByName(String name) {
        var characterCacheEntry = characterCacheByNameStore.get(name);

        if (characterCacheEntry != null) {
            return characterCacheEntry.guid;
        }

        return ObjectGuid.Empty;
    }

    public final boolean getCharacterNameByGuid(ObjectGuid guid, tangible.OutObject<String> name) {
        name.outArgValue = "Unknown";
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return false;
        }

        name.outArgValue = characterCacheEntry.name;

        return true;
    }

    public final Team getCharacterTeamByGuid(ObjectGuid guid) {
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return 0;
        }

        return player.teamForRace(characterCacheEntry.raceId);
    }

    public final int getCharacterAccountIdByGuid(ObjectGuid guid) {
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return 0;
        }

        return characterCacheEntry.accountId;
    }

    public final int getCharacterAccountIdByName(String name) {
        var characterCacheEntry = characterCacheByNameStore.get(name);

        if (characterCacheEntry != null) {
            return characterCacheEntry.accountId;
        }

        return 0;
    }

    public final byte getCharacterLevelByGuid(ObjectGuid guid) {
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return 0;
        }

        return characterCacheEntry.level;
    }

    public final long getCharacterGuildIdByGuid(ObjectGuid guid) {
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return 0;
        }

        return characterCacheEntry.guildId;
    }

    public final int getCharacterArenaTeamIdByGuid(ObjectGuid guid, byte type) {
        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return 0;
        }

        return characterCacheEntry.ArenaTeamId[ArenaTeam.getSlotByType(type)];
    }

    public final boolean getCharacterNameAndClassByGUID(ObjectGuid guid, tangible.OutObject<String> name, tangible.OutObject<Byte> _class) {
        name.outArgValue = "Unknown";
        _class.outArgValue = 0;

        var characterCacheEntry = characterCacheStore.get(guid);

        if (characterCacheEntry == null) {
            return false;
        }

        name.outArgValue = characterCacheEntry.name;
        _class.outArgValue = (byte) characterCacheEntry.classId;

        return true;
    }
}
