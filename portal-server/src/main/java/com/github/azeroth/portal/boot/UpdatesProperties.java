package com.github.azeroth.portal.boot;

import lombok.Data;

@Data
public class UpdatesProperties {
    private int enableDatabases;
    private int autoSetup;
    private int redundancy;
    private int archivedRedundancy;
    private int allowRehash;
    private int cleanDeadRefMaxCount;
}
