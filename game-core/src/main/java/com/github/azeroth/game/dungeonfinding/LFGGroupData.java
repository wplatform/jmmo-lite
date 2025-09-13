package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;


public class LFGGroupData {
    private final ArrayList<ObjectGuid> m_Players = new ArrayList<>();

    // General
    private LfgState m_State = LfgState.values()[0];
    private LfgState m_OldState = LfgState.values()[0];

    private ObjectGuid m_Leader = ObjectGuid.EMPTY;

    // Dungeon
    private int m_Dungeon;

    // Vote Kick
    private byte m_KicksLeft;
    private boolean m_VoteKickActive;

    public LFGGroupData() {
        m_State = LfgState.NONE;
        m_OldState = LfgState.NONE;
        m_KicksLeft = SharedConst.LFGMaxKicks;
    }

    public final boolean isLfgGroup() {
        return m_OldState != LfgState.NONE;
    }

    public final void restoreState() {
        m_State = m_OldState;
    }

    public final void addPlayer(ObjectGuid guid) {
        m_Players.add(guid);
    }

    public final byte removePlayer(ObjectGuid guid) {
        m_Players.remove(guid);

        return (byte) m_Players.size();
    }

    public final void removeAllPlayers() {
        m_Players.clear();
    }

    public final void decreaseKicksLeft() {
        if (m_KicksLeft != 0) {
            --m_KicksLeft;
        }
    }

    public final LfgState getState() {
        return m_State;
    }

    public final void setState(LfgState state) {
        switch (state) {
            case None:
                m_Dungeon = 0;
                m_KicksLeft = SharedConst.LFGMaxKicks;
                m_OldState = state;

                break;
            case FinishedDungeon:
            case Dungeon:
                m_OldState = state;

                break;
        }

        m_State = state;
    }

    public final LfgState getOldState() {
        return m_OldState;
    }

    public final ArrayList<ObjectGuid> getPlayers() {
        return m_Players;
    }

    public final byte getPlayerCount() {
        return (byte) m_Players.size();
    }

    public final ObjectGuid getLeader() {
        return m_Leader;
    }

    public final void setLeader(ObjectGuid guid) {
        m_Leader = guid;
    }

    public final int getDungeon() {
        return getDungeon(true);
    }

    public final void setDungeon(int dungeon) {
        m_Dungeon = dungeon;
    }

    public final int getDungeon(boolean asId) {
        if (asId) {
            return (m_Dungeon & 0x00FFFFFF);
        } else {
            return m_Dungeon;
        }
    }

    public final byte getKicksLeft() {
        return m_KicksLeft;
    }

    public final void setVoteKick(boolean active) {
        m_VoteKickActive = active;
    }

    public final boolean isVoteKickActive() {
        return m_VoteKickActive;
    }
}
