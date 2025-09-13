package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(onMethod_ = {@Override})
@RequiredArgsConstructor
public
enum PlayerFlag implements EnumFlag.FlagValue {
    GROUP_LEADER(0x00000001),
    AFK(0x00000002),
    DND(0x00000004),
    GM(0x00000008),
    GHOST(0x00000010),
    RESTING(0x00000020),
    VOICE_CHAT(0x00000040),
    UNK7(0x00000080),       // pre-3.0.3 PLAYER_FLAGS_FFA_PVP flag for FFA PVP state
    CONTESTED_PVP(0x00000100),       // Player has been involved in a PvP combat and will be attacked by contested guards
    IN_PVP(0x00000200),
    WAR_MODE_ACTIVE(0x00000400),
    WAR_MODE_DESIRED(0x00000800),
    PLAYED_LONG_TIME(0x00001000),       // played long time
    PLAYED_TOO_LONG(0x00002000),       // played too long time
    IS_OUT_OF_BOUNDS(0x00004000),
    DEVELOPER(0x00008000),       // <Dev> prefix for something?
    LOW_LEVEL_RAID_ENABLED(0x00010000),       // pre-3.0.3 PLAYER_FLAGS_SANCTUARY flag for player entered sanctuary
    TAXI_BENCHMARK(0x00020000),       // taxi benchmark mode (on/off) (2.0.1)
    PVP_TIMER(0x00040000),       // 3.0.2, pvp timer active (after you disable pvp manually)
    UBER(0x00080000),
    UNK20(0x00100000),
    UNK21(0x00200000),
    COMMENTATOR2(0x00400000),
    HIDE_ACCOUNT_ACHIEVEMENTS(0x00800000),    // do not send account achievments in inspect packets
    PET_BATTLES_UNLOCKED(0x01000000),       // enables pet battles
    NO_XP_GAIN(0x02000000),
    UNK26(0x04000000),
    AUTO_DECLINE_GUILD(0x08000000),       // Automatically declines guild invites
    GUILD_LEVEL_ENABLED(0x10000000),       // Lua_GetGuildLevelEnabled() - enables guild leveling related UI
    VOID_UNLOCKED(0x20000000),       // void storage
    TIME_WALKING(0x40000000),
    COMMENTATOR_CAMERA(0x80000000);
    public final int value;
}
