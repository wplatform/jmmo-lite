package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;


public class LFGPlayerData {
    // General
    private RideTicket m_Ticket;
    private LfgState m_State = LfgState.values()[0];

    private LfgState m_OldState = LfgState.values()[0];

    // Player
    private Team m_Team = Team.values()[0];
    private ObjectGuid m_Group = ObjectGuid.EMPTY;

    // Queue
    private LfgRoles m_Roles = LfgRoles.values()[0];
    private ArrayList<Integer> m_SelectedDungeons = new ArrayList<>();

    // Achievement-related
    private byte m_NumberOfPartyMembersAtJoin;

    public LFGPlayerData() {
        m_State = LfgState.NONE;
        m_OldState = LfgState.NONE;
    }

    public final void restoreState() {
        if (m_OldState == LfgState.NONE) {
            m_SelectedDungeons.clear();
            m_Roles = LfgRoles.forValue(0);
        }

        m_State = m_OldState;
    }

    public final RideTicket getTicket() {
        return m_Ticket;
    }

    public final void setTicket(RideTicket ticket) {
        m_Ticket = ticket;
    }

    public final LfgState getState() {
        return m_State;
    }

    public final void setState(LfgState state) {
        switch (state) {
            case None:
            case FinishedDungeon:
                m_Roles = LfgRoles.forValue(0);
                m_SelectedDungeons.clear();
            case Dungeon:
                m_OldState = state;

                break;
        }

        m_State = state;
    }

    public final LfgState getOldState() {
        return m_OldState;
    }

    public final Team getTeam() {
        return m_Team;
    }

    public final void setTeam(Team team) {
        m_Team = team;
    }

    public final ObjectGuid getGroup() {
        return m_Group;
    }

    public final void setGroup(ObjectGuid group) {
        m_Group = group;
    }

    public final LfgRoles getRoles() {
        return m_Roles;
    }

    public final void setRoles(LfgRoles roles) {
        m_Roles = roles;
    }

    public final ArrayList<Integer> getSelectedDungeons() {
        return m_SelectedDungeons;
    }

    public final void setSelectedDungeons(ArrayList<Integer> dungeons) {
        m_SelectedDungeons = dungeons;
    }

    public final byte getNumberOfPartyMembersAtJoin() {
        return m_NumberOfPartyMembersAtJoin;
    }

    public final void setNumberOfPartyMembersAtJoin(byte count) {
        m_NumberOfPartyMembersAtJoin = count;
    }
}
