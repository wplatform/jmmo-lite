package com.github.azeroth.game.entity.player;


import game.WorldConfig;

public class RestMgr {
    private final Player player;
    private final double[] restBonus = new double[RestTypes.max.getValue()];
    private long restTime;
    private int innAreaTriggerId;
    private RestFlag restFlagMask = RestFlag.values()[0];

    public RestMgr(Player player) {
        player = player;
    }

    public final void setRestBonus(RestTypes restType, double restBonus) {
        int nextLevelXp;
        var affectedByRaF = false;

        switch (restType) {
            case XP:
                // Reset restBonus (XP only) for max level players
                if (player.getLevel() >= WorldConfig.getIntValue(WorldCfg.MaxPlayerLevel)) {
                    restBonus = 0;
                }

                nextLevelXp = player.getActivePlayerData().nextLevelXP;
                affectedByRaF = true;

                break;
            case Honor:
                // Reset restBonus (Honor only) for players with max honor level.
                if (player.isMaxHonorLevel()) {
                    restBonus = 0;
                }

                nextLevelXp = player.getActivePlayerData().honorNextLevel;

                break;
            default:
                return;
        }

        var rest_bonus_max = nextLevelXp * 1.5f / 2;

        if (restBonus < 0) {
            restBonus = 0;
        }

        if (restBonus > rest_bonus_max) {
            restBonus = rest_bonus_max;
        }

        var oldBonus = (int) (_restBonus[restType.getValue()]);
        _restBonus[restType.getValue()] = restBonus;

        var oldRestState = PlayerRestState.forValue((int) player.getActivePlayerData().restInfo.get(restType.getValue()).stateID);
        var newRestState = PlayerRestState.NORMAL;

        if (affectedByRaF && player.getsRecruitAFriendBonus(true) && (player.getSession().isARecruiter() || player.getSession().getRecruiterId() != 0)) {
            newRestState = PlayerRestState.RAFLinked;
        } else if (_restBonus[restType.getValue()] >= 1) {
            newRestState = PlayerRestState.Rested;
        }

        if (oldBonus == restBonus && oldRestState == newRestState) {
            return;
        }

        // update data for client
        player.setRestThreshold(restType, (int) _restBonus[restType.getValue()]);
        player.setRestState(restType, newRestState);
    }

    public final void addRestBonus(RestTypes restType, double restBonus) {
        // Don't add extra rest bonus to max level players. Note: Might need different condition in next expansion for honor XP (PLAYER_LEVEL_MIN_HONOR perhaps).
        if (player.getLevel() >= WorldConfig.getIntValue(WorldCfg.MaxPlayerLevel)) {
            restBonus = 0;
        }

        var totalRestBonus = getRestBonus(restType) + restBonus;
        setRestBonus(restType, totalRestBonus);
    }


    public final void setRestFlag(RestFlag restFlag) {
        setRestFlag(restFlag, 0);
    }

    public final void setRestFlag(RestFlag restFlag, int triggerId) {
        var oldRestMask = restFlagMask;
        restFlagMask = RestFlag.forValue(restFlagMask.getValue() | restFlag.getValue());

        if (oldRestMask == 0 && restFlagMask != 0) // only set flag/time on the first rest state
        {
            restTime = gameTime.GetGameTime();
            player.setPlayerFlag(playerFlags.Resting);
        }

        if (triggerId != 0) {
            innAreaTriggerId = triggerId;
        }
    }

    public final void removeRestFlag(RestFlag restFlag) {
        var oldRestMask = restFlagMask;
        restFlagMask = RestFlag.forValue(restFlagMask.getValue() & ~restFlag.getValue());

        if (oldRestMask != 0 && restFlagMask == 0) // only remove flag/time on the last rest state remove
        {
            restTime = 0;
            player.removePlayerFlag(playerFlags.Resting);
        }
    }

    public final double getRestBonusFor(RestTypes restType, int xp) {
        var rested_bonus = getRestBonus(restType); // xp for each rested bonus

        if (rested_bonus > xp) // max rested_bonus == xp or (r+x) = 200% xp
        {
            rested_bonus = xp;
        }

        var rested_loss = rested_bonus;

        if (restType == RestTypes.XP) {
            tangible.RefObject<Double> tempRef_rested_loss = new tangible.RefObject<Double>(rested_loss);
            MathUtil.AddPct(tempRef_rested_loss, player.getTotalAuraModifier(AuraType.ModRestedXpConsumption));
            rested_loss = tempRef_rested_loss.refArgValue;
        }

        setRestBonus(restType, getRestBonus(restType) - rested_loss);

        Log.outDebug(LogFilter.player, "RestMgr.GetRestBonus: Player '{0}' ({1}) gain {2} xp (+{3} Rested Bonus). Rested points={4}", player.getGUID().toString(), player.getName(), xp + rested_bonus, rested_bonus, getRestBonus(restType));

        return rested_bonus;
    }

    public final void update(int now) {
        if (RandomUtil.randChance(3) && restTime > 0) // freeze update
        {
            var timeDiff = now - restTime;

            if (timeDiff >= 10) {
                restTime = now;

                var bubble = 0.125f * WorldConfig.getFloatValue(WorldCfg.RateRestIngame);
                addRestBonus(RestTypes.XP, timeDiff * calcExtraPerSec(RestTypes.XP, bubble));
            }
        }
    }

    public final void loadRestBonus(RestTypes restType, PlayerRestState state, float restBonus) {
        _restBonus[restType.getValue()] = restBonus;
        player.setRestState(restType, state);
        player.setRestThreshold(restType, (int) restBonus);
    }

    public final float calcExtraPerSec(RestTypes restType, float bubble) {
        switch (restType) {
            case Honor:
                return player.getActivePlayerData().HonorNextLevel / 72000.0f * bubble;
            case XP:
                return player.getActivePlayerData().NextLevelXP / 72000.0f * bubble;
            default:
                return 0.0f;
        }
    }

    public final double getRestBonus(RestTypes restType) {
        return _restBonus[restType.getValue()];
    }

    public final boolean hasRestFlag(RestFlag restFlag) {
        return (restFlagMask.getValue() & restFlag.getValue()) != 0;
    }

    public final int getInnTriggerId() {
        return innAreaTriggerId;
    }
}
