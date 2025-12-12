package com.github.azeroth.game.miscellaneous;


import com.github.azeroth.defines.Expansion;
import com.github.azeroth.defines.XPColorChar;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.listener.interfaces.iformula.*;


public class Formulas {

    public static float HKHonorAtLevelF(int level) {
        return HKHonorAtLevelF(level, 1.0f);
    }

    public static float HKHonorAtLevelF(int level, float multiplier) {
        var honor = multiplier * level * 1.55f;
        global.getScriptMgr().<IFormulaOnHonorCalculation>ForEach(p -> p.OnHonorCalculation(honor, level, multiplier));

        return honor;
    }


    public static int HKHonorAtLevel(int level) {
        return HKHonorAtLevel(level, 1.0f);
    }

    public static int HKHonorAtLevel(int level, float multiplier) {
        return (int) Math.ceil(HKHonorAtLevelF(level, multiplier));
    }

    public static int getGrayLevel(int pl_level) {
        int level;

        if (pl_level < 7) {
            level = 0;
        } else if (pl_level < 35) {
            byte count = 0;

            for (var i = 15; i <= pl_level; ++i) {
                if (i % 5 == 0) {
                    ++count;
                }
            }

            level = (pl_level - 7) - (count - 1);
        } else {
            level = pl_level - 10;
        }

        global.getScriptMgr().<IFormulaOnGrayLevelCalculation>ForEach(p -> p.OnGrayLevelCalculation(level, pl_level));

        return level;
    }

    public static XPColorChar getColorCode(int pl_level, int mob_level) {
        XPColorChar color;

        if (mob_level >= pl_level + 5) {
            color = XPColorChar.XP_RED;
        } else if (mob_level >= pl_level + 3) {
            color = XPColorChar.XP_ORANGE;
        } else if (mob_level >= pl_level - 2) {
            color = XPColorChar.XP_YELLOW;
        } else if (mob_level > getGrayLevel(pl_level)) {
            color = XPColorChar.XP_GREEN;
        } else {
            color = XPColorChar.XP_GRAY;
        }

        global.getScriptMgr().<IFormulaOnColorCodeCaclculation>ForEach(p -> p.OnColorCodeCalculation(color, pl_level, mob_level));

        return color;
    }

    public static int getZeroDifference(int pl_level) {
        int diff;

        if (pl_level < 4) {
            diff = 5;
        } else if (pl_level < 10) {
            diff = 6;
        } else if (pl_level < 12) {
            diff = 7;
        } else if (pl_level < 16) {
            diff = 8;
        } else if (pl_level < 20) {
            diff = 9;
        } else if (pl_level < 30) {
            diff = 11;
        } else if (pl_level < 40) {
            diff = 12;
        } else if (pl_level < 45) {
            diff = 13;
        } else if (pl_level < 50) {
            diff = 14;
        } else if (pl_level < 55) {
            diff = 15;
        } else if (pl_level < 60) {
            diff = 16;
        } else {
            diff = 17;
        }

        global.getScriptMgr().<IFormulaOnZeroDifference>ForEach(p -> p.OnZeroDifferenceCalculation(diff, pl_level));

        return diff;
    }

    public static int baseGain(int pl_level, int mob_level) {
        int baseGain;

        var xpPlayer = CliDB.XpGameTable.GetRow(pl_level);
        var xpMob = CliDB.XpGameTable.GetRow(mob_level);

        if (mob_level >= pl_level) {
            var nLevelDiff = mob_level - pl_level;

            if (nLevelDiff > 4) {
                nLevelDiff = 4;
            }

            baseGain = (int) Math.rint(xpPlayer.PerKill * (1 + 0.05f * nLevelDiff));
        } else {
            var gray_level = getGrayLevel(pl_level);

            if (mob_level > gray_level) {
                var ZD = getZeroDifference(pl_level);
                baseGain = (int) Math.rint(xpMob.PerKill * ((1 - ((pl_level - mob_level) / ZD)) * (xpMob.Divisor / xpPlayer.Divisor)));
            } else {
                baseGain = 0;
            }
        }

        if (WorldConfig.getIntValue(WorldCfg.MinCreatureScaledXpRatio) != 0 && pl_level != mob_level) {
            // Use mob level instead of player level to avoid overscaling on gain in a min is enforced
            var baseGainMin = baseGain(pl_level, pl_level) * WorldConfig.getUIntValue(WorldCfg.MinCreatureScaledXpRatio) / 100;
            baseGain = Math.max(baseGainMin, baseGain);
        }

        global.getScriptMgr().<IFormulaOnBaseGainCalculation>ForEach(p -> p.OnBaseGainCalculation(baseGain, pl_level, mob_level));

        return baseGain;
    }


    public static int XPGain(Player player, Unit u) {
        return XPGain(player, u, false);
    }

    public static int XPGain(Player player, Unit u, boolean isBattleGround) {
        var creature = u.toCreature();
        int gain = 0;

        if (!creature || creature.getCanGiveExperience()) {
            var xpMod = 1.0f;

            gain = baseGain(player.getLevel(), u.getLevelForTarget(player));

            if (gain != 0 && creature) {
                // Players get only 10% xp for killing creatures of lower expansion levels than himself
                if (ConfigMgr.GetDefaultValue("player.lowerExpInLowerExpansions", true) && (creature.getCreatureTemplate().getHealthScalingExpansion() < getExpansionForLevel(player.getLevel()).getValue())) {
                    gain = (int) Math.rint(gain / 10.0f);
                }

                if (creature.isElite()) {
                    // Elites in instances have a 2.75x XP bonus instead of the regular 2x world bonus.
                    if (u.getMap().isDungeon()) {
                        xpMod *= 2.75f;
                    } else {
                        xpMod *= 2.0f;
                    }
                }

                xpMod *= creature.getCreatureTemplate().modExperience;
            }

            xpMod *= isBattleGround ? WorldConfig.getFloatValue(WorldCfg.RateXpBgKill) : WorldConfig.getFloatValue(WorldCfg.RateXpKill);

            if (creature && creature.getPlayerDamageReq() != 0) // if players dealt less than 50% of the damage and were credited anyway (due to CREATURE_FLAG_EXTRA_NO_PLAYER_DAMAGE_REQ), scale XP gained appropriately (linear scaling)
            {
                xpMod *= 1.0f - 2.0f * creature.getPlayerDamageReq() / creature.getMaxHealth();
            }

            gain = (int) (gain * xpMod);
        }

        global.getScriptMgr().<IFormulaOnGainCalculation>ForEach(p -> p.OnGainCalculation(gain, player, u));

        return gain;
    }

    public static float XPInGroupRate(int count, boolean isRaid) {
        float rate;

        if (isRaid) {
            // FIXME: Must apply decrease modifiers depending on raid size.
            // set to < 1 to, so client will display raid related strings
            rate = 0.99f;
        } else {
            switch (count) {
                case 0:
                case 1:
                case 2:
                    rate = 1.0f;

                    break;
                case 3:
                    rate = 1.166f;

                    break;
                case 4:
                    rate = 1.3f;

                    break;
                case 5:
                default:
                    rate = 1.4f;

                    break;
            }
        }

        global.getScriptMgr().<IFormulaOnGroupRateCaclulation>ForEach(p -> p.OnGroupRateCalculation(rate, count, isRaid));

        return rate;
    }

    public static int conquestRatingCalculator(int rate) {
        if (rate <= 1500) {
            return 1350; // Default conquest points
        } else if (rate > 3000) {
            rate = 3000;
        }

        // http://www.arenajunkies.com/topic/179536-conquest-point-cap-vs-personal-rating-chart/page__st__60#entry3085246
        return (int) (1.4326 * ((1511.26 / (1 + 1639.28 / Math.exp(0.00412 * rate))) + 850.15));
    }

    public static int bgConquestRatingCalculator(int rate) {
        // WowWiki: Battlegroundratings receive a bonus of 22.2% to the cap they generate
        return (int) ((conquestRatingCalculator(rate) * 1.222f) + 0.5f);
    }

    private static Expansion getExpansionForLevel(int level) {
        if (level < 60) {
            return Expansion.CLASSIC;
        } else if (level < 70) {
            return Expansion.THE_BURNING_CRUSADE;
        } else if (level < 80) {
            return Expansion.WRATH_OF_THE_LICH_KING;
        } else if (level < 85) {
            return Expansion.CATACLYSM;
        } else if (level < 90) {
            return Expansion.MISTS_OF_PANDARIA;
        } else if (level < 100) {
            return Expansion.WARLORDS_OF_DRAENOR;
        } else {
            return Expansion.LEGION;
        }
    }
}
