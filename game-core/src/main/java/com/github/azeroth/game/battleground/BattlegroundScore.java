package com.github.azeroth.game.battleground;


import com.github.azeroth.game.networking.packet.PVPMatchStatistics;

public class BattlegroundScore {
    public ObjectGuid playerGuid = ObjectGuid.EMPTY;
    public int teamId;

    // Default score, present in every type
    public int killingBlows;

    public int deaths;

    public int honorableKills;

    public int bonusHonor;

    public int damageDone;

    public int healingDone;

    public battlegroundScore(ObjectGuid playerGuid, Team team) {
        playerGuid = playerGuid;
        teamId = (int) (team == Team.ALLIANCE ? PvPTeamId.Alliance : PvPTeamId.Horde);
    }


    public void updateScore(ScoreType type, int value) {
        switch (type) {
            case KillingBlows:
                killingBlows += value;

                break;
            case Deaths:
                deaths += value;

                break;
            case HonorableKills:
                honorableKills += value;

                break;
            case BonusHonor:
                bonusHonor += value;

                break;
            case DamageDone:
                damageDone += value;

                break;
            case HealingDone:
                healingDone += value;

                break;
            default:

                break;
        }
    }

    public void buildPvPLogPlayerDataPacket(tangible.OutObject<PVPMatchStatistics.PVPMatchPlayerStatistics> playerData) {
        playerData.outArgValue = new PVPMatchStatistics.PVPMatchPlayerStatistics();
        playerData.outArgValue.playerGUID = playerGuid;
        playerData.outArgValue.kills = killingBlows;
        playerData.outArgValue.faction = (byte) teamId;

        if (honorableKills != 0 || deaths != 0 || bonusHonor != 0) {
            PVPMatchStatistics.HonorData playerDataHonor = new PVPMatchStatistics.HonorData();
            playerDataHonor.honorKills = honorableKills;
            playerDataHonor.deaths = deaths;
            playerDataHonor.contributionPoints = bonusHonor;
            playerData.outArgValue.honor = playerDataHonor;
        }

        playerData.outArgValue.damageDone = damageDone;
        playerData.outArgValue.healingDone = healingDone;
    }


    public int getAttr1() {
        return 0;
    }


    public int getAttr2() {
        return 0;
    }


    public int getAttr3() {
        return 0;
    }


    public int getAttr4() {
        return 0;
    }


    public int getAttr5() {
        return 0;
    }
}
