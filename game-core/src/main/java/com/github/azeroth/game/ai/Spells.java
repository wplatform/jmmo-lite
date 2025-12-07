package game.ai;

import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.



//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Spells
public final class Spells {
    /* Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AutoShot = 75;
    public static final int AUTO_SHOT = 75;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Shoot = 3018;
    public static final int SHOOT = 3018;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Throw = 2764;
    public static final int THROW = 2764;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Wand = 5019;
    public static final int WAND = 5019;

    /* Warrior - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BattleStance = 2457;
    public static final int BATTLE_STANCE = 2457;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BerserkerStance = 2458;
    public static final int BERSERKER_STANCE = 2458;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DefensiveStance = 71;
    public static final int DEFENSIVE_STANCE = 71;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Charge = 11578;
    public static final int CHARGE = 11578;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Intercept = 20252;
    public static final int INTERCEPT = 20252;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint EnragedRegen = 55694;
    public static final int ENRAGED_REGEN = 55694;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IntimidatingShout = 5246;
    public static final int INTIMIDATING_SHOUT = 5246;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Pummel = 6552;
    public static final int PUMMEL = 6552;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShieldBash = 72;
    public static final int SHIELD_BASH = 72;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Bloodrage = 2687;
    public static final int BLOODRAGE = 2687;

    /* Warrior - Arms */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SweepingStrikes = 12328;
    public static final int SWEEPING_STRIKES = 12328;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MortalStrike = 12294;
    public static final int MORTAL_STRIKE = 12294;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Bladestorm = 46924;
    public static final int BLADESTORM = 46924;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Rend = 47465;
    public static final int REND = 47465;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Retaliation = 20230;
    public static final int RETALIATION = 20230;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShatteringThrow = 64382;
    public static final int SHATTERING_THROW = 64382;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ThunderClap = 47502;
    public static final int THUNDER_CLAP = 47502;

    /* Warrior - Fury */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeathWish = 12292;
    public static final int DEATH_WISH = 12292;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Bloodthirst = 23881;
    public static final int BLOODTHIRST = 23881;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveTitansGrip = 46917;
    public static final int PASSIVE_TITANS_GRIP = 46917;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DemoShout = 47437;
    public static final int DEMO_SHOUT = 47437;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Execute = 47471;
    public static final int EXECUTE = 47471;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HeroicFury = 60970;
    public static final int HEROIC_FURY = 60970;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Recklessness = 1719;
    public static final int RECKLESSNESS = 1719;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PiercingHowl = 12323;
    public static final int PIERCING_HOWL = 12323;

    /* Warrior - Protection */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Vigilance = 50720;
    public static final int VIGILANCE = 50720;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Devastate = 20243;
    public static final int DEVASTATE = 20243;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Shockwave = 46968;
    public static final int SHOCKWAVE = 46968;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ConcussionBlow = 12809;
    public static final int CONCUSSION_BLOW = 12809;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Disarm = 676;
    public static final int DISARM = 676;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LastStand = 12975;
    public static final int LAST_STAND = 12975;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShieldBlock = 2565;
    public static final int SHIELD_BLOCK = 2565;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShieldSlam = 47488;
    public static final int SHIELD_SLAM = 47488;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShieldWall = 871;
    public static final int SHIELD_WALL = 871;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Reflection = 23920;
    public static final int REFLECTION = 23920;

    /* Paladin - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PalAuraMastery = 31821;
    public static final int PAL_AURA_MASTERY = 31821;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LayOnHands = 48788;
    public static final int LAY_ON_HANDS = 48788;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BlessingOfMight = 48932;
    public static final int BLESSING_OF_MIGHT = 48932;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AvengingWrath = 31884;
    public static final int AVENGING_WRATH = 31884;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineProtection = 498;
    public static final int DIVINE_PROTECTION = 498;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineShield = 642;
    public static final int DIVINE_SHIELD = 642;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HammerOfJustice = 10308;
    public static final int HAMMER_OF_JUSTICE = 10308;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HandOfFreedom = 1044;
    public static final int HAND_OF_FREEDOM = 1044;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HandOfProtection = 10278;
    public static final int HAND_OF_PROTECTION = 10278;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HandOfSacrifice = 6940;
    public static final int HAND_OF_SACRIFICE = 6940;

    /* Paladin - Holy*/
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveIllumination = 20215;
    public static final int PASSIVE_ILLUMINATION = 20215;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HolyShock = 20473;
    public static final int HOLY_SHOCK = 20473;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BeaconOfLight = 53563;
    public static final int BEACON_OF_LIGHT = 53563;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Consecration = 48819;
    public static final int CONSECRATION = 48819;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FlashOfLight = 48785;
    public static final int FLASH_OF_LIGHT = 48785;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HolyLight = 48782;
    public static final int HOLY_LIGHT = 48782;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineFavor = 20216;
    public static final int DIVINE_FAVOR = 20216;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineIllumination = 31842;
    public static final int DIVINE_ILLUMINATION = 31842;

    /* Paladin - Protection */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BlessOfSanc = 20911;
    public static final int BLESS_OF_SANC = 20911;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HolyShield = 20925;
    public static final int HOLY_SHIELD = 20925;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AvengersShield = 48827;
    public static final int AVENGERS_SHIELD = 48827;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineSacrifice = 64205;
    public static final int DIVINE_SACRIFICE = 64205;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HammerOfRighteous = 53595;
    public static final int HAMMER_OF_RIGHTEOUS = 53595;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint RighteousFury = 25780;
    public static final int RIGHTEOUS_FURY = 25780;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShieldOfRighteous = 61411;
    public static final int SHIELD_OF_RIGHTEOUS = 61411;

    /* Paladin - Retribution */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SealOfCommand = 20375;
    public static final int SEAL_OF_COMMAND = 20375;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint CrusaderStrike = 35395;
    public static final int CRUSADER_STRIKE = 35395;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineStorm = 53385;
    public static final int DIVINE_STORM = 53385;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Judgement = 20271;
    public static final int JUDGEMENT = 20271;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HammerOfWrath = 48806;
    public static final int HAMMER_OF_WRATH = 48806;

    /* Hunter - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Deterrence = 19263;
    public static final int DETERRENCE = 19263;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ExplosiveTrap = 49067;
    public static final int EXPLOSIVE_TRAP = 49067;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FreezingArrow = 60192;
    public static final int FREEZING_ARROW = 60192;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint RapidFire = 3045;
    public static final int RAPID_FIRE = 3045;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint KillShot = 61006;
    public static final int KILL_SHOT = 61006;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MultiShot = 49048;
    public static final int MULTI_SHOT = 49048;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ViperSting = 3034;
    public static final int VIPER_STING = 3034;

    /* Hunter - Beast Mastery */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BestialWrath = 19574;
    public static final int BESTIAL_WRATH = 19574;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveBeastWithin = 34692;
    public static final int PASSIVE_BEAST_WITHIN = 34692;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveBeastMastery = 53270;
    public static final int PASSIVE_BEAST_MASTERY = 53270;

    /* Hunter - Marksmanship */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AimedShot = 19434;
    public static final int AIMED_SHOT = 19434;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveTrueshotAura = 19506;
    public static final int PASSIVE_TRUESHOT_AURA = 19506;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ChimeraShot = 53209;
    public static final int CHIMERA_SHOT = 53209;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ArcaneShot = 49045;
    public static final int ARCANE_SHOT = 49045;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SteadyShot = 49052;
    public static final int STEADY_SHOT = 49052;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Readiness = 23989;
    public static final int READINESS = 23989;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SilencingShot = 34490;
    public static final int SILENCING_SHOT = 34490;

    /* Hunter - Survival */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveLockAndLoad = 56344;
    public static final int PASSIVE_LOCK_AND_LOAD = 56344;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint WyvernSting = 19386;
    public static final int WYVERN_STING = 19386;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ExplosiveShot = 53301;
    public static final int EXPLOSIVE_SHOT = 53301;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BlackArrow = 3674;
    public static final int BLACK_ARROW = 3674;

    /* Rogue - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Dismantle = 51722;
    public static final int DISMANTLE = 51722;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Evasion = 26669;
    public static final int EVASION = 26669;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Kick = 1766;
    public static final int KICK = 1766;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Vanish = 26889;
    public static final int VANISH = 26889;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Blind = 2094;
    public static final int BLIND = 2094;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint CloakOfShadows = 31224;
    public static final int CLOAK_OF_SHADOWS = 31224;

    /* Rogue - Assassination */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ColdBlood = 14177;
    public static final int COLD_BLOOD = 14177;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Mutilate = 1329;
    public static final int MUTILATE = 1329;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HungerForBlood = 51662;
    public static final int HUNGER_FOR_BLOOD = 51662;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Envenom = 57993;
    public static final int ENVENOM = 57993;

    /* Rogue - Combat */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SinisterStrike = 48637;
    public static final int SINISTER_STRIKE = 48637;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BladeFlurry = 13877;
    public static final int BLADE_FLURRY = 13877;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AdrenalineRush = 13750;
    public static final int ADRENALINE_RUSH = 13750;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint KillingSpree = 51690;
    public static final int KILLING_SPREE = 51690;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Eviscerate = 48668;
    public static final int EVISCERATE = 48668;

    /* Rogue - Sublety */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Hemorrhage = 16511;
    public static final int HEMORRHAGE = 16511;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Premeditation = 14183;
    public static final int PREMEDITATION = 14183;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShadowDance = 51713;
    public static final int SHADOW_DANCE = 51713;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Preparation = 14185;
    public static final int PREPARATION = 14185;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Shadowstep = 36554;
    public static final int SHADOWSTEP = 36554;

    /* Priest - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FearWard = 6346;
    public static final int FEAR_WARD = 6346;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PowerWordFort = 48161;
    public static final int POWER_WORD_FORT = 48161;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineSpirit = 48073;
    public static final int DIVINE_SPIRIT = 48073;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShadowProtection = 48169;
    public static final int SHADOW_PROTECTION = 48169;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DivineHymn = 64843;
    public static final int DIVINE_HYMN = 64843;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HymnOfHope = 64901;
    public static final int HYMN_OF_HOPE = 64901;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShadowWordDeath = 48158;
    public static final int SHADOW_WORD_DEATH = 48158;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PsychicScream = 10890;
    public static final int PSYCHIC_SCREAM = 10890;

    /* Priest - Discipline */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveSoulWarding = 63574;
    public static final int PASSIVE_SOUL_WARDING = 63574;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PowerInfusion = 10060;
    public static final int POWER_INFUSION = 10060;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Penance = 47540;
    public static final int PENANCE = 47540;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PainSuppression = 33206;
    public static final int PAIN_SUPPRESSION = 33206;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint InnerFocus = 14751;
    public static final int INNER_FOCUS = 14751;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PowerWordShield = 48066;
    public static final int POWER_WORD_SHIELD = 48066;

    /* Priest - Holy */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveSpiritRedemption = 20711;
    public static final int PASSIVE_SPIRIT_REDEMPTION = 20711;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DesperatePrayer = 19236;
    public static final int DESPERATE_PRAYER = 19236;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint GuardianSpirit = 47788;
    public static final int GUARDIAN_SPIRIT = 47788;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FlashHeal = 48071;
    public static final int FLASH_HEAL = 48071;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Renew = 48068;
    public static final int RENEW = 48068;

    /* Priest - Shadow */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint VampiricEmbrace = 15286;
    public static final int VAMPIRIC_EMBRACE = 15286;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Shadowform = 15473;
    public static final int SHADOWFORM = 15473;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint VampiricTouch = 34914;
    public static final int VAMPIRIC_TOUCH = 34914;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MindFlay = 15407;
    public static final int MIND_FLAY = 15407;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MindBlast = 48127;
    public static final int MIND_BLAST = 48127;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShadowWordPain = 48125;
    public static final int SHADOW_WORD_PAIN = 48125;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DevouringPlague = 48300;
    public static final int DEVOURING_PLAGUE = 48300;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Dispersion = 47585;
    public static final int DISPERSION = 47585;

    /* Death Knight - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeathGrip = 49576;
    public static final int DEATH_GRIP = 49576;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Strangulate = 47476;
    public static final int STRANGULATE = 47476;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint EmpowerRuneWeap = 47568;
    public static final int EMPOWER_RUNE_WEAP = 47568;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IcebornFortitude = 48792;
    public static final int ICEBORN_FORTITUDE = 48792;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AntiMagicShell = 48707;
    public static final int ANTI_MAGIC_SHELL = 48707;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeathCoilDk = 49895;
    public static final int DEATH_COIL_DK = 49895;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MindFreeze = 47528;
    public static final int MIND_FREEZE = 47528;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IcyTouch = 49909;
    public static final int ICY_TOUCH = 49909;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraFrostFever = 55095;
    public static final int AURA_FROST_FEVER = 55095;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PlagueStrike = 49921;
    public static final int PLAGUE_STRIKE = 49921;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraBloodPlague = 55078;
    public static final int AURA_BLOOD_PLAGUE = 55078;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Pestilence = 50842;
    public static final int PESTILENCE = 50842;

    /* Death Knight - Blood */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint RuneTap = 48982;
    public static final int RUNE_TAP = 48982;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Hysteria = 49016;
    public static final int HYSTERIA = 49016;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HeartStrike = 55050;
    public static final int HEART_STRIKE = 55050;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeathStrike = 49924;
    public static final int DEATH_STRIKE = 49924;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BloodStrike = 49930;
    public static final int BLOOD_STRIKE = 49930;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MarkOfBlood = 49005;
    public static final int MARK_OF_BLOOD = 49005;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint VampiricBlood = 55233;
    public static final int VAMPIRIC_BLOOD = 55233;

    /* Death Knight - Frost */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveIcyTalons = 50887;
    public static final int PASSIVE_ICY_TALONS = 50887;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FrostStrike = 49143;
    public static final int FROST_STRIKE = 49143;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HowlingBlast = 49184;
    public static final int HOWLING_BLAST = 49184;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint UnbreakableArmor = 51271;
    public static final int UNBREAKABLE_ARMOR = 51271;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Obliterate = 51425;
    public static final int OBLITERATE = 51425;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Deathchill = 49796;
    public static final int DEATHCHILL = 49796;

    /* Death Knight - Unholy */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveUnholyBlight = 49194;
    public static final int PASSIVE_UNHOLY_BLIGHT = 49194;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveMasterOfGhoul = 52143;
    public static final int PASSIVE_MASTER_OF_GHOUL = 52143;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ScourgeStrike = 55090;
    public static final int SCOURGE_STRIKE = 55090;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeathAndDecay = 49938;
    public static final int DEATH_AND_DECAY = 49938;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AntiMagicZone = 51052;
    public static final int ANTI_MAGIC_ZONE = 51052;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SummonGargoyle = 49206;
    public static final int SUMMON_GARGOYLE = 49206;

    /* Shaman - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Heroism = 32182;
    public static final int HEROISM = 32182;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Bloodlust = 2825;
    public static final int BLOODLUST = 2825;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint GroundingTotem = 8177;
    public static final int GROUNDING_TOTEM = 8177;

    /* Shaman - Elemental*/
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveElementalFocus = 16164;
    public static final int PASSIVE_ELEMENTAL_FOCUS = 16164;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint TotemOfWrath = 30706;
    public static final int TOTEM_OF_WRATH = 30706;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Thunderstorm = 51490;
    public static final int THUNDERSTORM = 51490;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LightningBolt = 49238;
    public static final int LIGHTNING_BOLT = 49238;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint EarthShock = 49231;
    public static final int EARTH_SHOCK = 49231;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FlameShock = 49233;
    public static final int FLAME_SHOCK = 49233;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LavaBurst = 60043;
    public static final int LAVA_BURST = 60043;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ChainLightning = 49271;
    public static final int CHAIN_LIGHTNING = 49271;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ElementalMastery = 16166;
    public static final int ELEMENTAL_MASTERY = 16166;

    /* Shaman - Enhancement */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveSpiritWeapons = 16268;
    public static final int PASSIVE_SPIRIT_WEAPONS = 16268;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LavaLash = 60103;
    public static final int LAVA_LASH = 60103;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FeralSpirit = 51533;
    public static final int FERAL_SPIRIT = 51533;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraMaelstromWeapon = 53817;
    public static final int AURA_MAELSTROM_WEAPON = 53817;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Stormstrike = 17364;
    public static final int STORMSTRIKE = 17364;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShamanisticRage = 30823;
    public static final int SHAMANISTIC_RAGE = 30823;

    /* Shaman - Restoration*/
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShaNatureSwift = 591;
    public static final int SHA_NATURE_SWIFT = 591;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ManaTideTotem = 590;
    public static final int MANA_TIDE_TOTEM = 590;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint EarthShield = 49284;
    public static final int EARTH_SHIELD = 49284;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Riptide = 61295;
    public static final int RIPTIDE = 61295;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HealingWave = 49273;
    public static final int HEALING_WAVE = 49273;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LesserHealWave = 49276;
    public static final int LESSER_HEAL_WAVE = 49276;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint TidalForce = 55198;
    public static final int TIDAL_FORCE = 55198;

    /* Mage - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DampenMagic = 43015;
    public static final int DAMPEN_MAGIC = 43015;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Evocation = 12051;
    public static final int EVOCATION = 12051;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ManaShield = 43020;
    public static final int MANA_SHIELD = 43020;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MirrorImage = 55342;
    public static final int MIRROR_IMAGE = 55342;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Spellsteal = 30449;
    public static final int SPELLSTEAL = 30449;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Counterspell = 2139;
    public static final int COUNTERSPELL = 2139;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IceBlock = 45438;
    public static final int ICE_BLOCK = 45438;

    /* Mage - Arcane */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FocusMagic = 54646;
    public static final int FOCUS_MAGIC = 54646;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ArcanePower = 12042;
    public static final int ARCANE_POWER = 12042;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ArcaneBarrage = 44425;
    public static final int ARCANE_BARRAGE = 44425;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ArcaneBlast = 42897;
    public static final int ARCANE_BLAST = 42897;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraArcaneBlast = 36032;
    public static final int AURA_ARCANE_BLAST = 36032;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ArcaneMissiles = 42846;
    public static final int ARCANE_MISSILES = 42846;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PresenceOfMind = 12043;
    public static final int PRESENCE_OF_MIND = 12043;

    /* Mage - Fire */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Pyroblast = 11366;
    public static final int PYROBLAST = 11366;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Combustion = 11129;
    public static final int COMBUSTION = 11129;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint LivingBomb = 44457;
    public static final int LIVING_BOMB = 44457;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Fireball = 42833;
    public static final int FIREBALL = 42833;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FireBlast = 42873;
    public static final int FIRE_BLAST = 42873;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DragonsBreath = 31661;
    public static final int DRAGONS_BREATH = 31661;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint BlastWave = 11113;
    public static final int BLAST_WAVE = 11113;

    /* Mage - Frost */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IcyVeins = 12472;
    public static final int ICY_VEINS = 12472;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IceBarrier = 11426;
    public static final int ICE_BARRIER = 11426;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeepFreeze = 44572;
    public static final int DEEP_FREEZE = 44572;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FrostNova = 42917;
    public static final int FROST_NOVA = 42917;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Frostbolt = 42842;
    public static final int FROSTBOLT = 42842;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ColdSnap = 11958;
    public static final int COLD_SNAP = 11958;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint IceLance = 42914;
    public static final int ICE_LANCE = 42914;

    /* Warlock - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Fear = 6215;
    public static final int FEAR = 6215;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HowlOfTerror = 17928;
    public static final int HOWL_OF_TERROR = 17928;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Corruption = 47813;
    public static final int CORRUPTION = 47813;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DeathCoilW = 47860;
    public static final int DEATH_COIL_W = 47860;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ShadowBolt = 47809;
    public static final int SHADOW_BOLT = 47809;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Incinerate = 47838;
    public static final int INCINERATE = 47838;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Immolate = 47811;
    public static final int IMMOLATE = 47811;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SeedOfCorruption = 47836;
    public static final int SEED_OF_CORRUPTION = 47836;

    /* Warlock - Affliction */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint PassiveSiphonLife = 63108;
    public static final int PASSIVE_SIPHON_LIFE = 63108;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint UnstableAffliction = 30108;
    public static final int UNSTABLE_AFFLICTION = 30108;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Haunt = 48181;
    public static final int HAUNT = 48181;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint CurseOfAgony = 47864;
    public static final int CURSE_OF_AGONY = 47864;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DrainSoul = 47855;
    public static final int DRAIN_SOUL = 47855;

    /* Warlock - Demonology */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SoulLink = 19028;
    public static final int SOUL_LINK = 19028;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DemonicEmpowerment = 47193;
    public static final int DEMONIC_EMPOWERMENT = 47193;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Metamorphosis = 59672;
    public static final int METAMORPHOSIS = 59672;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ImmolationAura = 50589;
    public static final int IMMOLATION_AURA = 50589;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DemonCharge = 54785;
    public static final int DEMON_CHARGE = 54785;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraDecimation = 63167;
    public static final int AURA_DECIMATION = 63167;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraMoltenCore = 71165;
    public static final int AURA_MOLTEN_CORE = 71165;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SoulFire = 47825;
    public static final int SOUL_FIRE = 47825;

    /* Warlock - Destruction */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Shadowburn = 17877;
    public static final int SHADOWBURN = 17877;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Conflagrate = 17962;
    public static final int CONFLAGRATE = 17962;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint ChaosBolt = 50796;
    public static final int CHAOS_BOLT = 50796;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Shadowfury = 47847;
    public static final int SHADOWFURY = 47847;

    /* Druid - Generic */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Barkskin = 22812;
    public static final int BARKSKIN = 22812;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Innervate = 29166;
    public static final int INNERVATE = 29166;

    /* Druid - Balance */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint InsectSwarm = 5570;
    public static final int INSECT_SWARM = 5570;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MoonkinForm = 24858;
    public static final int MOONKIN_FORM = 24858;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Starfall = 48505;
    public static final int STARFALL = 48505;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Typhoon = 61384;
    public static final int TYPHOON = 61384;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint AuraEclipseLunar = 48518;
    public static final int AURA_ECLIPSE_LUNAR = 48518;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Moonfire = 48463;
    public static final int MOONFIRE = 48463;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Starfire = 48465;
    public static final int STARFIRE = 48465;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Wrath = 48461;
    public static final int WRATH = 48461;

    /* Druid - Feral */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint CatForm = 768;
    public static final int CAT_FORM = 768;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SurvivalInstincts = 61336;
    public static final int SURVIVAL_INSTINCTS = 61336;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Mangle = 33917;
    public static final int MANGLE = 33917;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Berserk = 50334;
    public static final int BERSERK = 50334;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint MangleCat = 48566;
    public static final int MANGLE_CAT = 48566;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint FeralChargeCat = 49376;
    public static final int FERAL_CHARGE_CAT = 49376;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Rake = 48574;
    public static final int RAKE = 48574;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Rip = 49800;
    public static final int RIP = 49800;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint SavageRoar = 52610;
    public static final int SAVAGE_ROAR = 52610;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint TigerFury = 50213;
    public static final int TIGER_FURY = 50213;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Claw = 48570;
    public static final int CLAW = 48570;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Dash = 33357;
    public static final int DASH = 33357;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Maim = 49802;
    public static final int MAIM = 49802;

    /* Druid - Restoration */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Swiftmend = 18562;
    public static final int SWIFTMEND = 18562;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint TreeOfLife = 33891;
    public static final int TREE_OF_LIFE = 33891;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint WildGrowth = 48438;
    public static final int WILD_GROWTH = 48438;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint NatureSwiftness = 17116;
    public static final int NATURE_SWIFTNESS = 17116;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Tranquility = 48447;
    public static final int TRANQUILITY = 48447;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Nourish = 50464;
    public static final int NOURISH = 50464;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint HealingTouch = 48378;
    public static final int HEALING_TOUCH = 48378;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Rejuvenation = 48441;
    public static final int REJUVENATION = 48441;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Regrowth = 48443;
    public static final int REGROWTH = 48443;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint Lifebloom = 48451;
    public static final int LIFEBLOOM = 48451;
}