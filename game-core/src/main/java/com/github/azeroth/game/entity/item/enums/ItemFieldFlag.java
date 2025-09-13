package com.github.azeroth.game.entity.item.enums;

import lombok.RequiredArgsConstructor;

// ITEM_FIELD_FLAGS
@RequiredArgsConstructor
public
enum ItemFieldFlag {
    SOULBOUND(0x00000001), // Item is soulbound and cannot be traded <<--
    TRANSLATED(0x00000002), // Item text will not read as garbage when player does not know the language
    UNLOCKED(0x00000004), // Item had lock but can be opened now
    WRAPPED(0x00000008), // Item is wrapped and contains another item
    UNK2(0x00000010),
    UNK3(0x00000020),
    UNK4(0x00000040),
    UNK5(0x00000080),
    BOP_TRADEABLE(0x00000100), // Allows trading soulbound items
    READABLE(0x00000200), // Opens text page when right clicked
    UNK6(0x00000400),
    UNK7(0x00000800),
    REFUNDABLE(0x00001000), // Item can be returned to vendor for its original cost (extended cost)
    UNK8(0x00002000),
    UNK9(0x00004000),
    UNK10(0x00008000),
    UNK11(0x00010000),
    UNK12(0x00020000),
    UNK13(0x00040000),
    CHILD(0x00080000),
    UNK15(0x00100000),
    NEW_ITEM(0x00200000), // Item glows in inventory
    AZERITE_EMPOWERED_ITEM_VIEWED(0x00400000), // Won't play azerite powers animation when viewing it
    UNK18(0x00800000),
    UNK19(0x01000000),
    UNK20(0x02000000),
    UNK21(0x04000000),
    UNK22(0x08000000),
    UNK23(0x10000000),
    UNK24(0x20000000),
    UNK25(0x40000000),
    UNK26(0x80000000);
    public final int value;
}
