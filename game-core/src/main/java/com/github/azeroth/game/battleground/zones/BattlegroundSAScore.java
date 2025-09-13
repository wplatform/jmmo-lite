package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.networking.packet.PVPMatchStatistics;

class BattlegroundSAScore extends BattlegroundScore {
    private int demolishersDestroyed;
    private int gatesDestroyed;

    public BattlegroundSAScore(ObjectGuid playerGuid, Team team) {
        super(playerGuid, team);
    }

    @Override
    public void updateScore(ScoreType type, int value) {
        switch (type) {
            case DestroyedDemolisher:
                demolishersDestroyed += value;

                break;
            case DestroyedWall:
                gatesDestroyed += value;

                break;
            default:
                super.updateScore(type, value);

                break;
        }
    }

    @Override
    public void buildPvPLogPlayerDataPacket(tangible.OutObject<PVPMatchStatistics.PVPMatchPlayerStatistics> playerData) {
        super.buildPvPLogPlayerDataPacket(playerData);

        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat(SAObjectives.demolishersDestroyed.getValue(), demolishersDestroyed));
        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat(SAObjectives.gatesDestroyed.getValue(), gatesDestroyed));
    }

    @Override
    public int getAttr1() {
        return demolishersDestroyed;
    }

    @Override
    public int getAttr2() {
        return gatesDestroyed;
    }
}
