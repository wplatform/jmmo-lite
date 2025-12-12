package com.github.azeroth.game.domain.map.enums;

public enum LoadResult {
    Success,
    AlreadyLoaded,
    FileNotFound,
    VersionMismatch,
    ReadFromFileFailed,
    DisabledInConfig,
    LibraryError
}
