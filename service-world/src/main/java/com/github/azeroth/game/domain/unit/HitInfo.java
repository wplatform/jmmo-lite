package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HitInfo implements EnumFlag.FlagValue {
    NORMALSWING(0x00000000),
    UNK1(0x00000001),               // req correct packet structure
    AFFECTS_VICTIM(0x00000002),
    OFFHAND(0x00000004),
    UNK2(0x00000008),
    MISS(0x00000010),
    FULL_ABSORB(0x00000020),
    PARTIAL_ABSORB(0x00000040),
    FULL_RESIST(0x00000080),
    PARTIAL_RESIST(0x00000100),
    CRITICALHIT(0x00000200),               // critical hit
    UNK10(0x00000400),
    UNK11(0x00000800),
    UNK12(0x00001000),
    BLOCK(0x00002000),               // blocked damage
    UNK14(0x00004000),               // set only if meleespellid is present//  no world text when victim is hit for 0 dmg(HideWorldTextForNoDamage?)
    UNK15(0x00008000),               // player victim?// something related to blod sprut visual (BloodSpurtInBack?)
    GLANCING(0x00010000),
    CRUSHING(0x00020000),
    NO_ANIMATION(0x00040000),
    UNK19(0x00080000),
    UNK20(0x00100000),
    SWINGNOHITSOUND(0x00200000),               // unused?
    UNK22(0x00400000),
    RAGE_GAIN(0x00800000),
    FAKE_DAMAGE(0x01000000);                // enables damage animation even if no damage done, set only if no damage

    public final int value;
}
