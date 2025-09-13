package com.github.azeroth.game.battleground;


/**
 * stores information for players in queue
 */
public class PlayerQueueInfo {

    public int lastOnlineTime; // for tracking and removing offline players from queue after 5 minutes
    public GroupQueueInfo groupInfo; // pointer to the associated groupqueueinfo
}
