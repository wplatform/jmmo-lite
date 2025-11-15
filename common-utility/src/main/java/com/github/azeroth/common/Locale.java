package com.github.azeroth.common;

public enum Locale {
    enUS,
    koKR,
    frFR,
    deDE,
    zhCN,
    zhTW,
    esES,
    esMX,
    ruRU,
    none,
    ptBR,
    itIT;

    public static Locale indexOf(int index) {
        return Locale.values()[index];
    }
}
