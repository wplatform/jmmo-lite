package com.github.azeroth.game.arena;


import java.util.HashMap;
import java.util.Objects;


public class ArenaTeamManager {
    private final HashMap<Integer, ArenaTeam> arenaTeamStorage = new HashMap<Integer, ArenaTeam>();

    private int nextArenaTeamId;

    private ArenaTeamManager() {
        nextArenaTeamId = 1;
    }

    public final ArenaTeam getArenaTeamById(int arenaTeamId) {
        return arenaTeamStorage.get(arenaTeamId);
    }

    public final ArenaTeam getArenaTeamByName(String arenaTeamName) {
        var search = arenaTeamName.toLowerCase();


        for (var(_, team) : arenaTeamStorage) {
            if (Objects.equals(search, team.getName().toLowerCase())) {
                return team;
            }
        }

        return null;
    }

    public final ArenaTeam getArenaTeamByCaptain(ObjectGuid guid) {

        for (var(_, team) : arenaTeamStorage) {
            if (Objects.equals(team.getCaptain(), guid)) {
                return team;
            }
        }

        return null;
    }

    public final void addArenaTeam(ArenaTeam arenaTeam) {
        var added = arenaTeamStorage.TryAdd(arenaTeam.getId(), arenaTeam);
    }

    public final void removeArenaTeam(int arenaTeamId) {
        arenaTeamStorage.remove(arenaTeamId);
    }

    public final int generateArenaTeamId() {
        if (nextArenaTeamId >= 0xFFFFFFFE) {
            Log.outError(LogFilter.Battleground, "Arena team ids overflow!! Can't continue, shutting down server. ");
            global.getWorldMgr().stopNow();
        }

        return nextArenaTeamId++;
    }

    public final void loadArenaTeams() {
        var oldMSTime = System.currentTimeMillis();

        // Clean out the trash before loading anything
        DB.characters.DirectExecute("DELETE FROM arena_team_member WHERE arenaTeamId NOT IN (SELECT arenaTeamId FROM arena_team)"); // One-time query

        //                                                        0        1         2         3          4              5            6            7           8
        var result = DB.characters.query("SELECT arenaTeamId, name, captainGuid, type, backgroundColor, emblemStyle, emblemColor, borderStyle, borderColor, " + "rating, weekGames, weekWins, seasonGames, seasonWins, `rank` FROM arena_team ORDER BY arenaTeamId ASC");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 arena teams. DB table `arena_team` is empty!");

            return;
        }

        var result2 = DB.characters.query("SELECT arenaTeamId, atm.guid, atm.weekGames, atm.weekWins, atm.seasonGames, atm.seasonWins, c.name, class, personalRating, matchMakerRating FROM arena_team_member atm" + " INNER JOIN arena_team ate USING (arenaTeamId) LEFT JOIN character AS c ON atm.guid = c.guid" + " LEFT JOIN character_arena_stats AS cas ON c.guid = cas.guid AND (cas.slot = 0 AND ate.type = 2 OR cas.slot = 1 AND ate.type = 3 OR cas.slot = 2 AND ate.type = 5)" + " ORDER BY atm.arenateamid ASC");

        int count = 0;

        do {
            ArenaTeam newArenaTeam = new ArenaTeam();

            if (!newArenaTeam.loadArenaTeamFromDB(result) || !newArenaTeam.loadMembersFromDB(result2)) {
                newArenaTeam.disband(null);

                continue;
            }

            addArenaTeam(newArenaTeam);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} arena teams in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void setNextArenaTeamId(int id) {
        nextArenaTeamId = id;
    }

    public final HashMap<Integer, ArenaTeam> getArenaTeamMap() {
        return arenaTeamStorage;
    }
}
