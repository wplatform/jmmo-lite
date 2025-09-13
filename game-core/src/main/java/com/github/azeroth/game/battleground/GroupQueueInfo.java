package com.github.azeroth.game.battleground;


import com.github.azeroth.game.entity.ObjectGuid;

import java.util.HashMap;

/**
 * stores information about the group in queue (also used when joined as solo!)
 */
public class GroupQueueInfo {
    public HashMap<ObjectGuid, PlayerQueueInfo> players = new HashMap<ObjectGuid, PlayerQueueInfo>(); // player queue info map
    public teamFaction team = Team.values()[0]; // Player team (ALLIANCE/HORDE)

    public int arenaTeamId; // team id if rated match

    public int joinTime; // time when group was added

    public int removeInviteTime; // time when we will remove invite for players in group

    public int isInvitedToBGInstanceGUID; // was invited to certain BG

    public int arenaTeamRating; // if rated match, inited to the rating of the team

    public int arenaMatchmakerRating; // if rated match, inited to the rating of the team

    public int opponentsTeamRating; // for rated arena matches

    public int opponentsMatchmakerRating; // for rated arena matches
}
