package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.networking.packet.PVPMatchStatistics;

class BgEyeOfStormScore extends BattlegroundScore {
    private int flagCaptures;

    public BgEyeOfStormScore(ObjectGuid playerGuid, Team team) {
        super(playerGuid, team);
    }

    @Override
    public void updateScore(ScoreType type, int value) {
        switch (type) {
            case FlagCaptures: // Flags captured
                flagCaptures += value;

                break;
            default:
                super.updateScore(type, value);

                break;
        }
    }

    @Override
    public void buildPvPLogPlayerDataPacket(tangible.OutObject<PVPMatchStatistics.PVPMatchPlayerStatistics> playerData) {
        super.buildPvPLogPlayerDataPacket(playerData);

        playerData.outArgValue.stats.add(new PVPMatchStatistics.PVPMatchPlayerPVPStat((int) EotSMisc.objectiveCaptureFlag, flagCaptures));
    }

    @Override
    public int getAttr1() {
        return flagCaptures;
    }
}
