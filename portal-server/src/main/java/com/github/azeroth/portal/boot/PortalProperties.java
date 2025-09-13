package com.github.azeroth.portal.boot;

import lombok.Data;

import java.io.File;

@Data
public class PortalProperties {

    private String bindIP;

    private String pidFile;

    private int battleNetPort;
    private int useProcessors;
    private int processPriority;
    private int realmsStateUpdateDelay;


    private File certificatesFile;
    private File privateKeyFile;

    private String banExpiryCheckInterval;
    private String sourceDirectory;
    private String mysqlExecutable;
    private String ipLocationFile;
    private boolean allowLoggingIPAddressesInDatabase;
    private boolean totPMasterSecret;


}
