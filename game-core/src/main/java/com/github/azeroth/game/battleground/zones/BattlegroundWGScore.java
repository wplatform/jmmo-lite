package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.networking.packet.PVPMatchStatistics;

class BattlegroundWGScore extends BattlegroundScore {
    private int flagCaptures;
    private int flagReturns;

    public BattlegroundWGScore(ObjectGuid playerGuid, Team team) {
        super(playerGuid, team);
    }

    @Override
    public void updateScore(ScoreType type, int value) {
        switch (type) {
            case FlagCaptures: // Flags captured
                flagCaptures += value;

                break;
            case FlagReturns: // Flags returned
                flagReturns += value;

                break;
            default:
                super.updateScore(type, value);

                break;
        }
    }

    @Override
    public void buildPvPLogPlayerDataPacket(tangible.OutObject<PVPMatchStatistics.PVPMatchPlayerStatistics> playerData) {
        super.buildPvPLogPlayerDataPacket(playerData);

        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat(WSObjectives.CAPTUREFLAG, flagCaptures));
        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat(WSObjectives.RETURNFLAG, flagReturns));
    }

    @Override
    public int getAttr1() {
        return flagCaptures;
    }

    @Override
    public int getAttr2() {
        return flagReturns;
    }
}
