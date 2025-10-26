package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupUpdateFlag  implements EnumFlag.FlagValue {
    NONE              (0x00000000),       // nothing
    UNK704            (0x00000001),       // uint8[2] (unk)
    STATUS            (0x00000002),       // uint16 (GroupMemberStatusFlag)
    POWER_TYPE        (0x00000004),       // uint8 (PowerType)
    UNK322            (0x00000008),       // uint16 (unk)
    CUR_HP            (0x00000010),       // uint32 (HP)
    MAX_HP            (0x00000020),       // uint32 (max HP)
    CUR_POWER         (0x00000040),       // int16 (power value)
    MAX_POWER         (0x00000080),       // int16 (max power value)
    LEVEL             (0x00000100),       // uint16 (level value)
    UNK200000         (0x00000200),       // int16 (unk)
    ZONE              (0x00000400),       // uint16 (zone id)
    UNK2000000        (0x00000800),       // int16 (unk)
    UNK4000000        (0x00001000),       // int32 (unk)
    POSITION          (0x00002000),       // uint16 (x), uint16 (y), uint16 (z)
    VEHICLE_SEAT      (0x00104000),       // int32 (vehicle seat id)
    AURAS             (0x00008000),       // uint8 (unk), uint64 (mask), uint32 (count), for each bit set: uint32 (spell id) + uint16 (AuraFlags)  (if has flags Scalable -> 3x int32 (bps))
    PET               (0x00010000),       // complex (pet)
    PHASE             (0x00020000),       // int32 (unk), uint32 (phase count), for (count) uint16(phaseId)

    GROUP_UPDATE_FULL (UNK704.value | STATUS.value | POWER_TYPE.value |
    UNK322.value | CUR_HP.value | MAX_HP.value |
    CUR_POWER.value | MAX_POWER.value | LEVEL.value |
    UNK200000.value | ZONE.value | UNK2000000.value |
    UNK4000000.value | POSITION.value | VEHICLE_SEAT.value |
    AURAS.value | PET.value | PHASE.value); // all known flags;
    
    public final int value;
}
