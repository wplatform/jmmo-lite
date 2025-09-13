package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.BattlegroundScore;
import com.github.azeroth.game.networking.packet.PVPMatchStatistics;

class ArenaScore extends BattlegroundScore {

    private final int preMatchRating;

    private final int preMatchMMR;

    private final int postMatchRating;

    private final int postMatchMMR;

    public ArenaScore(ObjectGuid playerGuid, Team team) {
        super(playerGuid, team);
        teamId = (int) (team == Team.ALLIANCE ? PvPTeamId.Alliance : PvPTeamId.Horde);
    }

    @Override
    public void buildPvPLogPlayerDataPacket(tangible.OutObject<PVPMatchStatistics.PVPMatchPlayerStatistics> playerData) {
        super.buildPvPLogPlayerDataPacket(playerData);

        if (preMatchRating != 0) {
            playerData.outArgValue.preMatchRating = preMatchRating;
        }

        if (postMatchRating != preMatchRating) {
            playerData.outArgValue.ratingChange = (int) (PostMatchRating - preMatchRating);
        }

        if (preMatchMMR != 0) {
            playerData.outArgValue.preMatchMMR = preMatchMMR;
        }

        if (postMatchMMR != preMatchMMR) {
            playerData.outArgValue.mmrChange = (int) (PostMatchMMR - preMatchMMR);
        }
    }

    // For Logging purpose
    @Override
    public String toString() {
        return String.format("Damage done: %1$s Healing done: %2$s Killing blows: %3$s PreMatchRating: %4$s ", damageDone, healingDone, killingBlows, preMatchRating) + String.format("PreMatchMMR: %1$s PostMatchRating: %2$s PostMatchMMR: %3$s", preMatchMMR, postMatchRating, postMatchMMR);
    }
}
