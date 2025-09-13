package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum TeleportToOptions {
    TELE_TO_GM_MODE(0x01),
    TELE_TO_NOT_LEAVE_TRANSPORT(0x02),
    TELE_TO_NOT_LEAVE_COMBAT(0x04),
    TELE_TO_NOT_UNSUMMON_PET(0x08),
    TELE_TO_SPELL(0x10),
    TELE_TO_TRANSPORT_TELEPORT(0x20), // 3.3.5 only
    TELE_REVIVE_AT_TELEPORT(0x40),
    TELE_TO_SEAMLESS(0x80);

    public final int value;
}
