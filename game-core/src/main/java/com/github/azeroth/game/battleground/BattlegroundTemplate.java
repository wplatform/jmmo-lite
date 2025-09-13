package com.github.azeroth.game.battleground;


import Framework.Threading.*;
import com.github.azeroth.game.WorldSafeLocsEntry;


public class BattlegroundTemplate {
    public BattlegroundTypeid id = BattlegroundTypeId.values()[0];
    public WorldSafeLocsEntry[] startLocation = new WorldSafeLocsEntry[SharedConst.PvpTeamsCount];
    public float maxStartDistSq;
    public byte weight;
    public int scriptId;
    public BattlemasterListRecord battlemasterEntry;

    public final boolean isArena() {
        return battlemasterEntry.instanceType == (int) MapTypes.Arena.getValue();
    }

    public final short getMinPlayersPerTeam() {
        return (short) battlemasterEntry.MinPlayers;
    }

    public final short getMaxPlayersPerTeam() {
        return (short) battlemasterEntry.MaxPlayers;
    }

    public final byte getMinLevel() {
        return battlemasterEntry.minLevel;
    }

    public final byte getMaxLevel() {
        return battlemasterEntry.maxLevel;
    }
}
