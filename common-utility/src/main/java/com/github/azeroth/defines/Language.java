package com.github.azeroth.defines;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// Languages.db2 (9.2.0.42423)
public enum Language {
    LANG_UNIVERSAL(0),
    LANG_ORCISH(1),
    LANG_DARNASSIAN(2),
    LANG_TAURAHE(3),
    LANG_DWARVISH(6),
    LANG_COMMON(7),
    LANG_DEMONIC(8),
    LANG_TITAN(9),
    LANG_THALASSIAN(10),
    LANG_DRACONIC(11),
    LANG_KALIMAG(12),
    LANG_GNOMISH(13),
    LANG_TROLL(14),
    LANG_GUTTERSPEAK(33),
    LANG_DRAENEI(35),
    LANG_ZOMBIE(36),
    LANG_GNOMISH_BINARY(37),
    LANG_GOBLIN_BINARY(38),
    LANG_WORGEN(39),
    LANG_GOBLIN(40),
    LANG_PANDAREN_NEUTRAL(42),
    LANG_PANDAREN_ALLIANCE(43),
    LANG_PANDAREN_HORDE(44),
    LANG_SPRITE(168),
    LANG_SHATH_YAR(178),
    LANG_NERGLISH(179),
    LANG_MOONKIN(180),
    LANG_SHALASSIAN(181),
    LANG_THALASSIAN_2(182),
    LANG_ADDON(183),
    LANG_ADDON_LOGGED(184),
    LANG_VULPERA(285),
    LANG_COMPLEX_CIPHER(287),
    LANG_BASIC_CYPHER(288),
    LANG_METRIAL(290),
    LANG_ALTONIAN(291),
    LANG_SOPRANIAN(292),
    LANG_AEALIC(293),
    LANG_DEALIC(294),
    LANG_TREBELIM(295),
    LANG_BASSALIM(296),
    LANG_EMBEDDED_LANGUAGES(297),
    LANG_UNKNOWABLE(298);
    public final short value;

    Language(int value) {
        this.value = (short) value;
    }

    public static Language valueOf(short value) {
        return Arrays.stream(values()).filter(language -> language.value == value).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown language value: " + value));
    }
}
