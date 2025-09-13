package com.github.azeroth.dbc.defines;

import com.github.azeroth.common.EnumFlag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(onMethod = @__({@Override}))
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AreaFlag implements EnumFlag.FlagValue {
    EmitBreathParticles(0x00000001),
    BreathParticlesOverrideParent(0x00000002),
    OnMapDungeon(0x00000004),
    AllowTradeChannel(0x00000008),
    EnemiesPvPFlagged(0x00000010),
    AllowResting(0x00000020),
    AllowDueling(0x00000040),
    FreeForAllPvP(0x00000080),
    LinkedChat(0x00000100), // Set in cities
    LinkedChatSpecialArea(0x00000200),
    ForceThisAreaWhenOnDynamicTransport(0x00000400),
    NoPvP(0x00000800),
    NoGhostOnRelease(0x00001000),
    SubZoneAmbientMultiplier(0x00002000),
    EnableFlightBoundsOnMap(0x00004000),
    PVPPOI(0x00008000),
    NoChatChannels(0x00010000),
    AreaNotInUse(0x00020000),
    Contested(0x00040000),
    NoPlayerSummoning(0x00080000),
    NoDuelingIfTournamentRealm(0x00100000),
    PlayersCallGuards(0x00200000),
    HordeResting(0x00400000),
    AllianceResting(0x00800000),
    CombatZone(0x01000000),
    ForceIndoors(0x02000000),
    ForceOutdoors(0x04000000),
    AllowHearthAndRessurectFromArea(0x08000000),
    NoLocalDefenseChannel(0x10000000),
    OnlyEvaluateGhostBindOnce(0x20000000),
    IsSubzone(0x40000000),
    DontEvaluateGraveyardFromClient(0x80000000);
    public final int value;
}
