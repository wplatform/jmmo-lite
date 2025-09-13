package com.github.azeroth.game.arena;


import com.github.azeroth.game.cache.CharacterCacheEntry;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;
import game.*;

import java.util.ArrayList;
import java.util.Objects;


public class ArenaTeam {
    private final ArrayList<ArenaTeamMember> members = new ArrayList<>();

    private int teamId;
    private byte type;
    private String teamName;
    private ObjectGuid captainGuid = ObjectGuid.EMPTY;

    private int backgroundColor; // ARGB format
    private byte emblemStyle; // icon id
    private int emblemColor; // ARGB format
    private byte borderStyle; // border image id
    private int borderColor; // ARGB format
    private arenaTeamStats stats = new arenaTeamStats();

    public ArenaTeam() {
        stats.rating = (short) WorldConfig.getIntValue(WorldCfg.ArenaStartRating);
    }

    public static byte getSlotByType(int type) {
        switch (ArenaTypes.forValue(type)) {
            case Team2v2:
                return 0;
            case Team3v3:
                return 1;
            case Team5v5:
                return 2;
            default:
                break;
        }

        Log.outError(LogFilter.Arena, "FATAL: Unknown arena team type {0} for some arena team", type);

        return (byte) 0xFF;
    }

    public static byte getTypeBySlot(byte slot) {
        switch (slot) {
            case 0:
                return (byte) ArenaTypes.Team2v2.getValue();
            case 1:
                return (byte) ArenaTypes.Team3v3.getValue();
            case 2:
                return (byte) ArenaTypes.Team5v5.getValue();
            default:
                break;
        }

        Log.outError(LogFilter.Arena, "FATAL: Unknown arena team slot {0} for some arena team", slot);

        return (byte) 0xFF;
    }

    public final boolean create(ObjectGuid captainGuid, byte type, String arenaTeamName, int backgroundColor, byte emblemStyle, int emblemColor, byte borderStyle, int borderColor) {
        // Check if captain exists
        if (global.getCharacterCacheStorage().getCharacterCacheByGuid(captainGuid) == null) {
            return false;
        }

        // Check if arena team name is already taken
        if (global.getArenaTeamMgr().getArenaTeamByName(arenaTeamName) != null) {
            return false;
        }

        // Generate new arena team id
        teamId = global.getArenaTeamMgr().generateArenaTeamId();

        // Assign member variables
        captainGuid = captainGuid;
        type = type;
        teamName = arenaTeamName;
        backgroundColor = backgroundColor;
        emblemStyle = emblemStyle;
        emblemColor = emblemColor;
        borderStyle = borderStyle;
        borderColor = borderColor;
        var captainLowGuid = captainGuid.getCounter();

        // Save arena team to db
        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ARENA_TEAM);
        stmt.AddValue(0, teamId);
        stmt.AddValue(1, teamName);
        stmt.AddValue(2, captainLowGuid);
        stmt.AddValue(3, type);
        stmt.AddValue(4, stats.rating);
        stmt.AddValue(5, backgroundColor);
        stmt.AddValue(6, emblemStyle);
        stmt.AddValue(7, emblemColor);
        stmt.AddValue(8, borderStyle);
        stmt.AddValue(9, borderColor);
        DB.characters.execute(stmt);

        // Add captain as member
        addMember(captainGuid);

        Log.outDebug(LogFilter.Arena, "New ArenaTeam created Id: {0}, Name: {1} Type: {2} Captain low GUID: {3}", getId(), getName(), getArenaType(), captainLowGuid);

        return true;
    }

    public final boolean addMember(ObjectGuid playerGuid) {
        String playerName;
        PlayerClass playerClass;

        // Check if arena team is full (Can't have more than type * 2 players)
        if (getMembersSize() >= getArenaType() * 2) {
            return false;
        }

        // Get player name and class either from db or character cache
        CharacterCacheEntry characterInfo;
        var player = global.getObjAccessor().findPlayer(playerGuid);

        if (player) {
            playerClass = player.getClass();
            playerName = player.getName();
        } else if ((characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(playerGuid)) != null) {
            playerName = characterInfo.name;
            playerClass = characterInfo.classId;
        } else {
            return false;
        }

        // Check if player is already in a similar arena team
        if ((player && player.getArenaTeamId(getSlot()) != 0) || global.getCharacterCacheStorage().getCharacterArenaTeamIdByGuid(playerGuid, getArenaType()) != 0) {
            Log.outDebug(LogFilter.Arena, "Arena: {0} {1} already has an arena team of type {2}", playerGuid.toString(), playerName, getArenaType());

            return false;
        }

        // Set player's personal rating
        int personalRating = 0;

        if (WorldConfig.getIntValue(WorldCfg.ArenaStartPersonalRating) > 0) {
            personalRating = WorldConfig.getUIntValue(WorldCfg.ArenaStartPersonalRating);
        } else if (getRating() >= 1000) {
            personalRating = 1000;
        }

        // Try to get player's match maker rating from db and fall back to config setting if not found
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MATCH_MAKER_RATING);
        stmt.AddValue(0, playerGuid.getCounter());
        stmt.AddValue(1, getSlot());
        var result = DB.characters.query(stmt);

        int matchMakerRating;

        if (!result.isEmpty()) {
            matchMakerRating = result.<SHORT>Read(0);
        } else {
            matchMakerRating = WorldConfig.getUIntValue(WorldCfg.ArenaStartMatchmakerRating);
        }

        // Remove all player signatures from other petitions
        // This will prevent player from joining too many arena teams and corrupt arena team data integrity
        //Player.removePetitionsAndSigns(playerGuid, getArenaType());

        // Feed data to the struct
        ArenaTeamMember newMember = new ArenaTeamMember();
        newMember.name = playerName;
        newMember.guid = playerGuid;
        newMember.class = (byte) playerClass.getValue();
        newMember.seasonGames = 0;
        newMember.weekGames = 0;
        newMember.seasonWins = 0;
        newMember.weekWins = 0;
        newMember.personalRating = (short) personalRating;
        newMember.matchMakerRating = (short) matchMakerRating;

        members.add(newMember);
        global.getCharacterCacheStorage().updateCharacterArenaTeamId(playerGuid, getSlot(), getId());

        // Save player's arena team membership to db
        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ARENA_TEAM_MEMBER);
        stmt.AddValue(0, teamId);
        stmt.AddValue(1, playerGuid.getCounter());
        stmt.AddValue(2, (short) personalRating);
        DB.characters.execute(stmt);

        // Inform player if online
        if (player) {
            player.setInArenaTeam(teamId, getSlot(), getArenaType());
            player.setArenaTeamIdInvited(0);

            // Hide promote/remove buttons
            if (ObjectGuid.opNotEquals(captainGuid, playerGuid)) {
                player.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.member, 1);
            }
        }

        Log.outDebug(LogFilter.Arena, "Player: {0} [{1}] joined arena team type: {2} [Id: {3}, Name: {4}].", playerName, playerGuid.toString(), getArenaType(), getId(), getName());

        return true;
    }

    public final boolean loadArenaTeamFromDB(SQLResult result) {
        if (result.isEmpty()) {
            return false;
        }

        teamId = result.<Integer>Read(0);
        teamName = result.<String>Read(1);
        captainGuid = ObjectGuid.create(HighGuid.Player, result.<Long>Read(2));
        type = result.<Byte>Read(3);
        backgroundColor = result.<Integer>Read(4);
        emblemStyle = result.<Byte>Read(5);
        emblemColor = result.<Integer>Read(6);
        borderStyle = result.<Byte>Read(7);
        borderColor = result.<Integer>Read(8);
        stats.rating = result.<SHORT>Read(9);
        stats.weekGames = result.<SHORT>Read(10);
        stats.weekWins = result.<SHORT>Read(11);
        stats.seasonGames = result.<SHORT>Read(12);
        stats.seasonWins = result.<SHORT>Read(13);
        stats.rank = result.<Integer>Read(14);

        return true;
    }

    public final boolean loadMembersFromDB(SQLResult result) {
        if (result.isEmpty()) {
            return false;
        }

        var captainPresentInTeam = false;

        do {
            var arenaTeamId = result.<Integer>Read(0);

            // We loaded all members for this arena_team already, break cycle
            if (arenaTeamId > teamId) {
                break;
            }

            ArenaTeamMember newMember = new ArenaTeamMember();
            newMember.guid = ObjectGuid.create(HighGuid.Player, result.<Long>Read(1));
            newMember.weekGames = result.<SHORT>Read(2);
            newMember.weekWins = result.<SHORT>Read(3);
            newMember.seasonGames = result.<SHORT>Read(4);
            newMember.seasonWins = result.<SHORT>Read(5);
            newMember.name = result.<String>Read(6);
            newMember.class = result.<Byte>Read(7);
            newMember.personalRating = result.<SHORT>Read(8);
            newMember.matchMakerRating = (short) (result.<SHORT>Read(9) > 0 ? result.<SHORT>Read(9) : 1500);

            // Delete member if character information is missing
            if (StringUtil.isEmpty(newMember.name)) {
                Logs.SQL.error("ArenaTeam {0} has member with empty name - probably {1} doesn't exist, deleting him from memberlist!", arenaTeamId, newMember.guid.toString());
                delMember(newMember.guid, true);

                continue;
            }

            // Check if team team has a valid captain
            if (Objects.equals(newMember.guid, getCaptain())) {
                captainPresentInTeam = true;
            }

            // Put the player in the team
            members.add(newMember);
            global.getCharacterCacheStorage().updateCharacterArenaTeamId(newMember.guid, getSlot(), getId());
        } while (result.NextRow());

        if (empty() || !captainPresentInTeam) {
            // Arena team is empty or captain is not in team, delete from db
            Log.outDebug(LogFilter.Arena, "ArenaTeam {0} does not have any members or its captain is not in team, disbanding it...", teamId);

            return false;
        }

        return true;
    }

    public final boolean setName(String name) {
        if (Objects.equals(teamName, name) || StringUtil.isEmpty(name) || name.length() > 24 || global.getObjectMgr().isReservedName(name) || !ObjectManager.isValidCharterName(name)) {
            return false;
        }

        teamName = name;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ARENA_TEAM_NAME);
        stmt.AddValue(0, teamName);
        stmt.AddValue(1, getId());
        DB.characters.execute(stmt);

        return true;
    }

    public final void delMember(ObjectGuid guid, boolean cleanDb) {
        // Remove member from team
        for (var member : members) {
            if (Objects.equals(member.guid, guid)) {
                members.remove(member);
                global.getCharacterCacheStorage().updateCharacterArenaTeamId(guid, getSlot(), 0);

                break;
            }
        }

        // Remove arena team info from player data
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            // delete all info regarding this team
            for (int i = 0; i < ArenaTeamInfoType.End.getValue(); ++i) {
                player.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.forValue(i), 0);
            }

            Log.outDebug(LogFilter.Arena, "Player: {0} [GUID: {1}] left arena team type: {2} [Id: {3}, Name: {4}].", player.getName(), player.getGUID().toString(), getArenaType(), getId(), getName());
        }

        // Only used for single member deletion, for arena team disband we use a single query for more efficiency
        if (cleanDb) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ARENA_TEAM_MEMBER);
            stmt.AddValue(0, getId());
            stmt.AddValue(1, guid.getCounter());
            DB.characters.execute(stmt);
        }
    }

    public final void disband(WorldSession session) {
        // Broadcast update
        if (session != null) {
            var player = session.getPlayer();

            if (player) {
                Log.outDebug(LogFilter.Arena, "Player: {0} [GUID: {1}] disbanded arena team type: {2} [Id: {3}, Name: {4}].", player.getName(), player.getGUID().toString(), getArenaType(), getId(), getName());
            }
        }

        // Remove all members from arena team
        while (!members.isEmpty()) {
            delMember(members.FirstOrDefault().guid, false);
        }

        // Update database
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ARENA_TEAM);
        stmt.AddValue(0, teamId);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ARENA_TEAM_MEMBERS);
        stmt.AddValue(0, teamId);
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        // Remove arena team from ArenaTeamMgr
        global.getArenaTeamMgr().removeArenaTeam(teamId);
    }

    public final void disband() {
        // Remove all members from arena team
        while (!members.isEmpty()) {
            delMember(members.get(0).guid, false);
        }

        // Update database
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ARENA_TEAM);
        stmt.AddValue(0, teamId);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ARENA_TEAM_MEMBERS);
        stmt.AddValue(0, teamId);
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        // Remove arena team from ArenaTeamMgr
        global.getArenaTeamMgr().removeArenaTeam(teamId);
    }

    public final void sendStats(WorldSession session) {
		/*WorldPacket data = new WorldPacket(ServerOpcode.ArenaTeamStats);
		data.writeInt32(getId());                                // team id
		data.writeInt32(stats.rating);                           // rating
		data.writeInt32(stats.weekGames);                        // games this week
		data.writeInt32(stats.weekWins);                         // wins this week
		data.writeInt32(stats.seasonGames);                      // played this season
		data.writeInt32(stats.seasonWins);                       // wins this season
		data.writeInt32(stats.rank);                             // rank
		session.sendPacket(data);*/
    }

    public final void notifyStatsChanged() {
        // This is called after a rated match ended
        // Updates arena team stats for every member of the team (not only the ones who participated!)
        for (var member : members) {
            var player = global.getObjAccessor().findPlayer(member.guid);

            if (player) {
                sendStats(player.getSession());
            }
        }
    }

    public final boolean isMember(ObjectGuid guid) {
        for (var member : members) {
            if (Objects.equals(member.guid, guid)) {
                return true;
            }
        }

        return false;
    }

    public final int getAverageMMR(PlayerGroup group) {
        if (!group) {
            return 0;
        }

        int matchMakerRating = 0;
        int playerDivider = 0;

        for (var member : members) {
            // Skip if player is not online
            if (!global.getObjAccessor().findPlayer(member.guid)) {
                continue;
            }

            // Skip if player is not a member of group
            if (!group.isMember(member.guid)) {
                continue;
            }

            matchMakerRating += member.matchMakerRating;
            ++playerDivider;
        }

        // x/0 = crash
        if (playerDivider == 0) {
            playerDivider = 1;
        }

        matchMakerRating /= playerDivider;

        return matchMakerRating;
    }

    public final void finishGame(int mod) {
        // Rating can only drop to 0
        if (stats.rating + mod < 0) {
            stats.rating = 0;
        } else {
            stats.rating += (short) mod;

            // Check if rating related achivements are met
            for (var member : members) {
                var player = global.getObjAccessor().findPlayer(member.guid);

                if (player) {
                    player.updateCriteria(CriteriaType.EarnTeamArenaRating, stats.rating, type);
                }
            }
        }

        // Update number of games played per season or week
        stats.weekGames += 1;
        stats.seasonGames += 1;

        // Update team's rank, start with rank 1 and increase until no team with more rating was found
        stats.rank = 1;


        for (var(_, team) : global.getArenaTeamMgr().getArenaTeamMap()) {
            if (team.getArenaType() == type && team.getStats().rating > stats.rating) {
                ++stats.rank;
            }
        }
    }

    public final int wonAgainst(int ownMMRating, int opponentMMRating, tangible.RefObject<Integer> ratingChange) {
        // Called when the team has won
        // Change in Matchmaker rating
        var mod = getMatchmakerRatingMod(ownMMRating, opponentMMRating, true);

        // Change in Team Rating
        ratingChange.refArgValue = getRatingMod(stats.rating, opponentMMRating, true);

        // Modify the team stats accordingly
        finishGame(ratingChange.refArgValue);

        // Update number of wins per season and week
        stats.weekWins += 1;
        stats.seasonWins += 1;

        // Return the rating change, used to display it on the results screen
        return mod;
    }

    public final int lostAgainst(int ownMMRating, int opponentMMRating, tangible.RefObject<Integer> ratingChange) {
        // Called when the team has lost
        // Change in Matchmaker Rating
        var mod = getMatchmakerRatingMod(ownMMRating, opponentMMRating, false);

        // Change in Team Rating
        ratingChange.refArgValue = getRatingMod(stats.rating, opponentMMRating, false);

        // Modify the team stats accordingly
        finishGame(ratingChange.refArgValue);

        // return the rating change, used to display it on the results screen
        return mod;
    }

    public final void memberLost(Player player, int againstMatchmakerRating) {
        memberLost(player, againstMatchmakerRating, -12);
    }

    public final void memberLost(Player player, int againstMatchmakerRating, int matchmakerRatingChange) {
        // Called for each participant of a match after losing
        for (var member : members) {
            if (Objects.equals(member.guid, player.getGUID())) {
                // Update personal rating
                var mod = getRatingMod(member.personalRating, againstMatchmakerRating, false);
                member.modifyPersonalRating(player, mod, getArenaType());

                // Update matchmaker rating
                member.modifyMatchmakerRating(matchmakerRatingChange, getSlot());

                // Update personal played stats
                member.weekGames += 1;
                member.seasonGames += 1;

                // update the unit fields
                player.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.GamesWeek, member.weekGames);
                player.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.GamesSeason, member.seasonGames);

                return;
            }
        }
    }

    public final void offlineMemberLost(ObjectGuid guid, int againstMatchmakerRating) {
        offlineMemberLost(guid, againstMatchmakerRating, -12);
    }

    public final void offlineMemberLost(ObjectGuid guid, int againstMatchmakerRating, int matchmakerRatingChange) {
        // Called for offline player after ending rated arena match!
        for (var member : members) {
            if (Objects.equals(member.guid, guid)) {
                // update personal rating
                var mod = getRatingMod(member.personalRating, againstMatchmakerRating, false);
                member.modifyPersonalRating(null, mod, getArenaType());

                // update matchmaker rating
                member.modifyMatchmakerRating(matchmakerRatingChange, getSlot());

                // update personal played stats
                member.weekGames += 1;
                member.seasonGames += 1;

                return;
            }
        }
    }

    public final void memberWon(Player player, int againstMatchmakerRating, int matchmakerRatingChange) {
        // called for each participant after winning a match
        for (var member : members) {
            if (Objects.equals(member.guid, player.getGUID())) {
                // update personal rating
                var mod = getRatingMod(member.personalRating, againstMatchmakerRating, true);
                member.modifyPersonalRating(player, mod, getArenaType());

                // update matchmaker rating
                member.modifyMatchmakerRating(matchmakerRatingChange, getSlot());

                // update personal stats
                member.weekGames += 1;
                member.seasonGames += 1;
                member.seasonWins += 1;
                member.weekWins += 1;
                // update unit fields
                player.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.GamesWeek, member.weekGames);
                player.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.GamesSeason, member.seasonGames);

                return;
            }
        }
    }

    public final void saveToDB() {
        // Save team and member stats to db
        // Called after a match has ended or when calculating arena_points

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ARENA_TEAM_STATS);
        stmt.AddValue(0, stats.rating);
        stmt.AddValue(1, stats.weekGames);
        stmt.AddValue(2, stats.weekWins);
        stmt.AddValue(3, stats.seasonGames);
        stmt.AddValue(4, stats.seasonWins);
        stmt.AddValue(5, stats.rank);
        stmt.AddValue(6, getId());
        trans.append(stmt);

        for (var member : members) {
            // Save the effort and go
            if (member.weekGames == 0) {
                continue;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ARENA_TEAM_MEMBER);
            stmt.AddValue(0, member.personalRating);
            stmt.AddValue(1, member.weekGames);
            stmt.AddValue(2, member.weekWins);
            stmt.AddValue(3, member.seasonGames);
            stmt.AddValue(4, member.seasonWins);
            stmt.AddValue(5, getId());
            stmt.AddValue(6, member.guid.getCounter());
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.REP_CHARACTER_ARENA_STATS);
            stmt.AddValue(0, member.guid.getCounter());
            stmt.AddValue(1, getSlot());
            stmt.AddValue(2, member.matchMakerRating);
            trans.append(stmt);
        }

        DB.characters.CommitTransaction(trans);
    }

    public final boolean finishWeek() {
        // No need to go further than this
        if (stats.weekGames == 0) {
            return false;
        }

        // Reset team stats
        stats.weekGames = 0;
        stats.weekWins = 0;

        // Reset member stats
        for (var member : members) {
            member.weekGames = 0;
            member.weekWins = 0;
        }

        return true;
    }

    public final boolean isFighting() {
        for (var member : members) {
            var player = global.getObjAccessor().findPlayer(member.guid);

            if (player) {
                if (player.getMap().isBattleArena()) {
                    return true;
                }
            }
        }

        return false;
    }

    public final ArenaTeamMember getMember(String name) {
        for (var member : members) {
            if (Objects.equals(member.name, name)) {
                return member;
            }
        }

        return null;
    }

    public final ArenaTeamMember getMember(ObjectGuid guid) {
        for (var member : members) {
            if (Objects.equals(member.guid, guid)) {
                return member;
            }
        }

        return null;
    }

    public final int getId() {
        return teamId;
    }

    public final byte getArenaType() {
        return type;
    }

    public final byte getSlot() {
        return getSlotByType(getArenaType());
    }

    public final ObjectGuid getCaptain() {
        return captainGuid;
    }

    public final void setCaptain(ObjectGuid guid) {
        // Disable remove/promote buttons
        var oldCaptain = global.getObjAccessor().findPlayer(getCaptain());

        if (oldCaptain) {
            oldCaptain.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.member, 1);
        }

        // Set new captain
        captainGuid = guid;

        // Update database
        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ARENA_TEAM_CAPTAIN);
        stmt.AddValue(0, guid.getCounter());
        stmt.AddValue(1, getId());
        DB.characters.execute(stmt);

        // Enable remove/promote buttons
        var newCaptain = global.getObjAccessor().findPlayer(guid);

        if (newCaptain) {
            newCaptain.setArenaTeamInfoField(getSlot(), ArenaTeamInfoType.member, 0);

            if (oldCaptain) {
                Log.outDebug(LogFilter.Arena, "Player: {0} [GUID: {1}] promoted player: {2} [GUID: {3}] to leader of arena team [Id: {4}, Name: {5}] [Type: {6}].", oldCaptain.getName(), oldCaptain.getGUID().toString(), newCaptain.getName(), newCaptain.getGUID().toString(), getId(), getName(), getArenaType());
            }
        }
    }

    public final String getName() {
        return teamName;
    }

    public final ArenaTeamStats getStats() {
        return stats;
    }

    public final int getRating() {
        return stats.rating;
    }

    public final int getMembersSize() {
        return members.size();
    }

    public final ArrayList<ArenaTeamMember> getMembers() {
        return members;
    }

    private void broadcastPacket(ServerPacket packet) {
        for (var member : members) {
            var player = global.getObjAccessor().findPlayer(member.guid);

            if (player) {
                player.sendPacket(packet);
            }
        }
    }

    private float getChanceAgainst(int ownRating, int opponentRating) {
        // Returns the chance to win against a team with the given rating, used in the rating adjustment calculation
        // ELO system
        return (float) (1.0f / (1.0f + Math.exp(Math.log(10.0f) * ((float) opponentRating - ownRating) / 650.0f)));
    }

    private int getMatchmakerRatingMod(int ownRating, int opponentRating, boolean won) {
        // 'Chance' calculation - to beat the opponent
        // This is a simulation. Not much info on how it really works
        var chance = getChanceAgainst(ownRating, opponentRating);
        var won_mod = (won) ? 1.0f : 0.0f;
        var mod = won_mod - chance;

        // Work in progress:
		/*
		// This is a simulation, as there is not much info on how it really works
		float confidence_mod = min(1.0f - fabs(mod), 0.5f);

		// Apply confidence factor to the mod:
		mod *= confidence_factor

		// And only after that update the new confidence factor
		confidence_factor -= ((confidence_factor - 1.0f) * confidence_mod) / confidence_factor;
		*/

        // Real rating modification
        mod *= WorldConfig.getFloatValue(WorldCfg.ArenaMatchmakerRatingModifier);

        return (int) Math.ceil(mod);
    }

    private int getRatingMod(int ownRating, int opponentRating, boolean won) {
        // 'Chance' calculation - to beat the opponent
        // This is a simulation. Not much info on how it really works
        var chance = getChanceAgainst(ownRating, opponentRating);

        // Calculate the rating modification
        float mod;

        // todo Replace this hack with using the confidence factor (limiting the factor to 2.0f)
        if (won) {
            if (ownRating < 1300) {
                var win_rating_modifier1 = WorldConfig.getFloatValue(WorldCfg.ArenaWinRatingModifier1);

                if (ownRating < 1000) {
                    mod = win_rating_modifier1 * (1.0f - chance);
                } else {
                    mod = ((win_rating_modifier1 / 2.0f) + ((win_rating_modifier1 / 2.0f) * (1300.0f - ownRating) / 300.0f)) * (1.0f - chance);
                }
            } else {
                mod = WorldConfig.getFloatValue(WorldCfg.ArenaWinRatingModifier2) * (1.0f - chance);
            }
        } else {
            mod = WorldConfig.getFloatValue(WorldCfg.ArenaLoseRatingModifier) * (-chance);
        }

        return (int) Math.ceil(mod);
    }

    private boolean empty() {
        return members.isEmpty();
    }
}
