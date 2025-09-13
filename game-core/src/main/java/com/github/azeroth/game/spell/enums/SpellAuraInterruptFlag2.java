package com.github.azeroth.game.spell.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpellAuraInterruptFlag2 implements EnumFlag.FlagValue {
    NONE(0),
    Falling(0x00000001), // Implemented in Unit::UpdatePosition
    Swimming(0x00000002),
    NotMoving(0x00000004), // NYI
    ground(0x00000008),
    Transform(0x00000010), // NYI
    jump(0x00000020),
    ChangeSpec(0x00000040),
    AbandonVehicle(0x00000080), // Implemented in Unit::_ExitVehicle
    StartOfEncounter(0x00000100), // Implemented in Unit::AtStartOfEncounter
    EndOfEncounter(0x00000200), // Implemented in Unit::AtEndOfEncounter
    Disconnect(0x00000400), // NYI
    EnteringInstance(0x00000800), // Implemented in Map::AddPlayerToMap
    DuelEnd(0x00001000), // Implemented in Player::DuelComplete
    LeaveArenaOrBattleground(0x00002000), // Implemented in Battleground::RemovePlayerAtLeave
    ChangeTalent(0x00004000),
    ChangeGlyph(0x00008000),
    SeamlessTransfer(0x00010000), // NYI
    WarModeLeave(0x00020000), // Implemented in Player::UpdateWarModeAuras
    TouchingGround(0x00040000), // NYI
    chromieTime(0x00080000), // NYI
    SplineFlightOrFreeFlight(0x00100000), // NYI
    ProcOrPeriodicAttacking(0x00200000), // NYI
    StartOfMythicPlusRun(0x00400000), // Implemented in Unit::AtStartOfEncounter
    StartOfDungeonEncounter(0x00800000), // Implemented in Unit::AtStartOfEncounter - Similar to StartOfEncounter (but only with bosses, not m+ run or battleground)
    EndOfDungeonEncounter(0x01000000), // Implemented in Unit::AtEndOfEncounter - Similar to EndOfEncounter (but only with bosses, not m+ run or battleground)

    public final int value;
    }
