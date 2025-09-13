package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

//SpellFamilyFlags
@Getter
@RequiredArgsConstructor
public enum SpellFamilyFlag implements EnumFlag.FlagValue {
    // SPELLFAMILYFLAG  = SpellFamilyFlags[0]
    // SPELLFAMILYFLAG1 = SpellFamilyFlags[1]
    // SPELLFAMILYFLAG2 = SpellFamilyFlags[2]

    // Rogue
    SPELLFAMILYFLAG0_ROGUE_VANISH(0x00000800),
    SPELLFAMILYFLAG0_ROGUE_VAN_SPRINT(0x00000840), // vanish, Sprint
    SPELLFAMILYFLAG1_ROGUE_SHADOWSTEP(0x00000200), // Shadowstep
    SPELLFAMILYFLAG0_ROGUE_KICK(0x00000010), // Kick
    SPELLFAMILYFLAG1_ROGUE_DISMANTLE_SMOKE_BOMB(0x80100000), // dismantle, Smoke Bomb

    // Warrior
    SPELLFAMILYFLAG_WARRIOR_CHARGE(0x00000001),
    SPELLFAMILYFLAG_WARRIOR_SLAM(0x00200000),
    SPELLFAMILYFLAG_WARRIOR_EXECUTE(0x20000000),
    SPELLFAMILYFLAG_WARRIOR_CONCUSSION_BLOW(0x04000000),

    // Warlock
    SPELLFAMILYFLAG_WARLOCK_LIFETAP(0x00040000),

    // Druid
    SPELLFAMILYFLAG2_DRUID_STARFALL(0x00000100),

    // Paladin
    SPELLFAMILYFLAG1_PALADIN_DIVINESTORM(0x00020000),

    // Shaman
    SPELLFAMILYFLAG_SHAMAN_FROST_SHOCK(0x80000000),
    SPELLFAMILYFLAG_SHAMAN_HEALING_STREAM(0x00002000),
    SPELLFAMILYFLAG_SHAMAN_MANA_SPRING(0x00004000),
    SPELLFAMILYFLAG2_SHAMAN_LAVA_LASH(0x00000004),
    SPELLFAMILYFLAG_SHAMAN_FIRE_NOVA(0x28000000),

    // Deathknight
    SPELLFAMILYFLAG_DK_DEATH_STRIKE(0x00000010),
    SPELLFAMILYFLAG_DK_DEATH_COIL(0x00002000),

    /// @todo Figure out a more accurate name for the following familyflag(s)
    SPELLFAMILYFLAG_SHAMAN_TOTEM_EFFECTS(0x04000000)  // Seems to be linked to most totems and some totem effects

    public final int value;
    }
