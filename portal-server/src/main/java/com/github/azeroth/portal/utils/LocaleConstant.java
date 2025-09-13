package com.github.azeroth.portal.utils;

import java.util.Arrays;

public enum LocaleConstant {
    LOCALE_enUS,
    LOCALE_koKR,
    LOCALE_frFR,
    LOCALE_deDE,
    LOCALE_zhCN,
    LOCALE_zhTW,
    LOCALE_esES,
    LOCALE_esMX,
    LOCALE_ruRU,
    LOCALE_none,
    LOCALE_ptBR,
    LOCALE_itIT,

    TOTAL_LOCALES;


    public static LocaleConstant fromName(String name) {
        return Arrays.stream(LocaleConstant.values())
                .filter(e -> e.name().endsWith(name))
                .findFirst().orElse(TOTAL_LOCALES);
    }

    public static boolean isValidLocale(String name) {
        LocaleConstant locale = LocaleConstant.fromName(name);
        return locale != TOTAL_LOCALES && locale != LOCALE_none;
    }
}
