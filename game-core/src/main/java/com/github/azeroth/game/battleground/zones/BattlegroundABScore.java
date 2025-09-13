package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.networking.packet.PVPMatchStatistics;

class BattlegroundABScore extends BattlegroundScore {
    private int basesAssaulted;
    private int basesDefended;

    public BattlegroundABScore(ObjectGuid playerGuid, Team team) {
        super(playerGuid, team);
        basesAssaulted = 0;
        basesDefended = 0;
    }

    @Override
    public void updateScore(ScoreType type, int value) {
        switch (type) {
            case BasesAssaulted:
                basesAssaulted += value;

                break;
            case BasesDefended:
                basesDefended += value;

                break;
            default:
                super.updateScore(type, value);

                break;
        }
    }

    @Override
    public void buildPvPLogPlayerDataPacket(tangible.OutObject<PVPMatchStatistics.PVPMatchPlayerStatistics> playerData) {
        super.buildPvPLogPlayerDataPacket(playerData);

        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat(ABObjectives.AssaultBase.getValue(), basesAssaulted));
        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat(ABObjectives.DefendBase.getValue(), basesDefended));
    }

    @Override
    public int getAttr1() {
        return basesAssaulted;
    }

    @Override
    public int getAttr2() {
        return basesDefended;
    }
}
