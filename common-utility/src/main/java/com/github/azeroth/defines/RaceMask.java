package com.github.azeroth.defines;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class RaceMask {


    public static final RaceMask ALL_PLAYABLE = RaceMask.of(
            RaceMask.getMaskForRace(Race.HUMAN) |
                    RaceMask.getMaskForRace(Race.ORC) |
                    RaceMask.getMaskForRace(Race.DWARF) |
                    RaceMask.getMaskForRace(Race.NIGHT_ELF) |
                    RaceMask.getMaskForRace(Race.UNDEAD) |
                    RaceMask.getMaskForRace(Race.TAUREN) |
                    RaceMask.getMaskForRace(Race.GNOME) |
                    RaceMask.getMaskForRace(Race.TROLL) |
                    RaceMask.getMaskForRace(Race.BLOOD_ELF) |
                    RaceMask.getMaskForRace(Race.DRAENEI) |
                    RaceMask.getMaskForRace(Race.GOBLIN) |
                    RaceMask.getMaskForRace(Race.WORGEN) |
                    RaceMask.getMaskForRace(Race.PANDAREN_NEUTRAL) |
                    RaceMask.getMaskForRace(Race.PANDAREN_ALLIANCE) |
                    RaceMask.getMaskForRace(Race.PANDAREN_HORDE) |
                    RaceMask.getMaskForRace(Race.NIGHTBORNE) |
                    RaceMask.getMaskForRace(Race.HIGHMOUNTAIN_TAUREN) |
                    RaceMask.getMaskForRace(Race.VOID_ELF) |
                    RaceMask.getMaskForRace(Race.LIGHTFORGED_DRAENEI) |
                    RaceMask.getMaskForRace(Race.ZANDALARI_TROLL) |
                    RaceMask.getMaskForRace(Race.KUL_TIRAN) |
                    RaceMask.getMaskForRace(Race.DARK_IRON_DWARF) |
                    RaceMask.getMaskForRace(Race.VULPERA) |
                    RaceMask.getMaskForRace(Race.MAGHAR_ORC) |
                    RaceMask.getMaskForRace(Race.MECHAGNOME) |
                    RaceMask.getMaskForRace(Race.DRACTHYR_ALLIANCE) |
                    RaceMask.getMaskForRace(Race.DRACTHYR_HORDE)
    );

    public static final RaceMask NEUTRAL = RaceMask.of(RaceMask.getMaskForRace(Race.PANDAREN_NEUTRAL));

    public static final RaceMask ALLIANCE = RaceMask.of(
            RaceMask.getMaskForRace(Race.HUMAN) |
                    RaceMask.getMaskForRace(Race.DWARF) |
                    RaceMask.getMaskForRace(Race.NIGHT_ELF) |
                    RaceMask.getMaskForRace(Race.GNOME) |
                    RaceMask.getMaskForRace(Race.DRAENEI) |
                    RaceMask.getMaskForRace(Race.WORGEN) |
                    RaceMask.getMaskForRace(Race.PANDAREN_ALLIANCE) |
                    RaceMask.getMaskForRace(Race.VOID_ELF) |
                    RaceMask.getMaskForRace(Race.LIGHTFORGED_DRAENEI) |
                    RaceMask.getMaskForRace(Race.KUL_TIRAN) |
                    RaceMask.getMaskForRace(Race.DARK_IRON_DWARF) |
                    RaceMask.getMaskForRace(Race.MECHAGNOME) |
                    RaceMask.getMaskForRace(Race.DRACTHYR_ALLIANCE)
    );

    public static final RaceMask HORDE = RaceMask.of(ALL_PLAYABLE.rawValue & ~(NEUTRAL.rawValue | ALLIANCE.rawValue));





    private long rawValue;

    public boolean hasRace(Race race) {
        return (rawValue & getMaskForRace(race)) != 0;
    }

    private static int getRaceBit(Race race) {
        return switch (race) {
            case HUMAN, ORC, DWARF, NIGHT_ELF, UNDEAD, TAUREN, GNOME,
                 TROLL, GOBLIN, BLOOD_ELF, DRAENEI, WORGEN, PANDAREN_NEUTRAL,
                 PANDAREN_ALLIANCE, PANDAREN_HORDE, NIGHTBORNE, HIGHMOUNTAIN_TAUREN,
                 VOID_ELF, LIGHTFORGED_DRAENEI -> race.ordinal() - 1;
            default -> -1;
        };
    }

    public static long getMaskForRace(Race race) {
        int raceBit = getRaceBit(race);
        return raceBit >= 0 && raceBit < 64 ? (1L << raceBit) : 0;
    }

    public boolean isEmpty() {
        return rawValue == 0;
    }

    public RaceMask and(RaceMask right) {
        rawValue &= right.rawValue;
        return this;
    }

    public RaceMask or(RaceMask right) {
        rawValue |= right.rawValue;
        return this;
    }

    public boolean hasRaceMask(long mask) {
        return (rawValue & mask) != 0;
    }

    public boolean hasRaceMask(RaceMask right) {
        return (rawValue & right.rawValue) != 0;
    }

    public RaceMask not() {
        rawValue = ~rawValue;
        return this;
    }

    public static RaceMask of(long rawValue) {
        return new RaceMask(rawValue);
    }

}
