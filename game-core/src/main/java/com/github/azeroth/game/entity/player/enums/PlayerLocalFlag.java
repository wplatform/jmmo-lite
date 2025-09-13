package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum PlayerLocalFlag {
    CONTROLLING_PET(0x00000001),   // Displays "You have an active summon already" when trying to tame new pet
    TRACK_STEALTH(0x00000002),
    RELEASE_TIMER(0x00000008),   // Display time till auto release spirit
    NO_RELEASE_WINDOW(0x00000010),   // Display no "release spirit" window at all
    NO_PET_BAR(0x00000020),   // CGPetInfo::IsPetBarUsed
    OVERRIDE_CAMERA_MIN_HEIGHT(0x00000040),
    NEWLY_BOOSTED_CHARACTER(0x00000080),
    USING_PARTY_GARRISON(0x00000100),
    CAN_USE_OBJECTS_MOUNTED(0x00000200),
    CAN_VISIT_PARTY_GARRISON(0x00000400),
    WAR_MODE(0x00000800),
    ACCOUNT_SECURED(0x00001000);   // Script_IsAccountSecured

    public final int value;
}
