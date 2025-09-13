package com.github.azeroth.game.spell.auras.enums;

import lombok.RequiredArgsConstructor;

// these are modes, in which aura effect handler may be called
@RequiredArgsConstructor
public enum AuraEffectHandleMode {
    AURA_EFFECT_HANDLE_DEFAULT(0x0),
    AURA_EFFECT_HANDLE_REAL(0x01), // handler applies/removes effect from unit
    AURA_EFFECT_HANDLE_SEND_FOR_CLIENT(0x02), // handler sends apply/remove packet to unit
    AURA_EFFECT_HANDLE_CHANGE_AMOUNT(0x04), // handler updates effect on target after effect amount change
    AURA_EFFECT_HANDLE_REAPPLY(0x08), // handler updates effect on target after aura is reapplied on target
    AURA_EFFECT_HANDLE_STAT(0x10), // handler updates effect on target when stat removal/apply is needed for calculations by core
    AURA_EFFECT_HANDLE_SKILL(0x20), // handler updates effect on target when skill removal/apply is needed for calculations by core
    AURA_EFFECT_HANDLE_SEND_FOR_CLIENT_MASK(AURA_EFFECT_HANDLE_SEND_FOR_CLIENT.value | AURA_EFFECT_HANDLE_REAL.value), // any case handler need to send packet
    AURA_EFFECT_HANDLE_CHANGE_AMOUNT_MASK(AURA_EFFECT_HANDLE_CHANGE_AMOUNT.value | AURA_EFFECT_HANDLE_REAL.value), // any case handler applies effect depending on amount
    AURA_EFFECT_HANDLE_CHANGE_AMOUNT_SEND_FOR_CLIENT_MASK(AURA_EFFECT_HANDLE_CHANGE_AMOUNT_MASK.value | AURA_EFFECT_HANDLE_SEND_FOR_CLIENT_MASK.value),
    AURA_EFFECT_HANDLE_REAL_OR_REAPPLY_MASK(AURA_EFFECT_HANDLE_REAPPLY.value | AURA_EFFECT_HANDLE_REAL.value);

    public final int value;
}
