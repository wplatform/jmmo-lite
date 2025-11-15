package com.github.azeroth.auth.dto;

import lombok.Data;
@Data
public class ClientVersion {
    private int versionMajor;
    private int versionMinor;
    private int versionRevision;
    private int versionBuild;
}
