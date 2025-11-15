package com.github.azeroth.utils;

import java.util.Arrays;
import java.util.List;

public class SysProperties {

    public static final long SSL_HANDSHAKE_TIMEOUT =
            Utils.getProperty("server.sslHandshakeTimeout", 10000);
    public static final String PORTAL_WOW_PROGRAM_NAME =
            Utils.getProperty("portal.wowProgramName", "WoW");

    public static final List<String> PORTAL_SUPPORTED_PLATFORM_LIST =
            listString(Utils.getProperty("portal.wowProgramName", "Win,Wn64,Mc64"));

    public static final int PORTAL_LOGIN_TICKET_LENGTH =
            Utils.getProperty("portal.loginTicketLength", 32);

    public static final int PORTAL_SESSION_KEY_LENGTH =
            Utils.getProperty("portal.sessionKeyLength", 64);

    public static final int PORTAL_CLIENT_SECRET_LENGTH =
            Utils.getProperty("portal.clientSecretLength", 32);

    public static final int PORTAL_ENCRYPT_KEY_LENGTH =
            Utils.getProperty("portal.encryptKeyLength", 16);

    public static final int PORTAL_SERVER_SECRET_LENGTH =
            Utils.getProperty("portal.serverSecretLength", 32);

    public static final int PORTAL_SERVER_IO_SELECT_COUNT =
            Utils.getProperty("portal.server.io.selectCount", 1);

    public static final int PORTAL_SERVER_IO_WORKER_COUNT =
            Utils.getProperty("portal.server.io.workerCount", 1);

    public static final int PORTAL_SERVER_IO_SO_RCVBUF =
            Utils.getProperty("portal.server.io.rcvbuf", 1024 * 16);

    public static final int PORTAL_SERVER_IO_SO_SNDBUF =
            Utils.getProperty("portal.server.io.sndbuf", 1024 * 16);

    public static final boolean PORTAL_SERVER_IO_PREFERNATIVE =
            Utils.getProperty("portal.server.io.preferNative", true);

    public static final String PORTAL_SERVER_CERTIFICATES_FILE =
            Utils.getProperty("portal.server.certificatesFile", "/portalserver.cert.pem");

    public static final String PORTAL_SERVER_PRIVATE_KEY_FILE =
            Utils.getProperty("portal.server.privateKeyFile", "/portalserver.first.pem");

    public static final String PORTAL_SERVER_RPC_TASK_HANDLER_THREAD_NAME =
            Utils.getProperty("portal.server.rpc.taskHandler.threadName", "portal-rpc-task-handler");

    public static final String PORTAL_SERVER_RPC_IO_WORKER_THREAD_NAME =
            Utils.getProperty("portal.server.rpc.ioWorker.threadName", "portal-rpc-io-worker");

    public static final String World_SERVER_IO_WORKER_THREAD_NAME =
            Utils.getProperty("world.server.ioWorker.threadName", "world-server-io-worker");

    public static final String WORLD_SERVER_TASK_HANDLER_THREAD_NAME =
            Utils.getProperty("world.server.protocol.taskHandler.threadName", "world-protocol-task-handler");

    public static final String CACHE_PERSISTENCE_FILE_NAME =
            Utils.getProperty("cache.persistenceFileName", "cache.db");

    public static final String METADATA_FOLDER =
            Utils.getProperty("cache.metadataFolder", "data");

    public static final String METADATA_DB2_FOLDER =
            Utils.getProperty("cache.metadataFolder", "db2");

    public static final String METADATA_MAPS_FOLDER =
            Utils.getProperty("cache.metadataFolder", "maps");

    private SysProperties() {
        // utility class
    }

    private static List<String> listString(String string) {
        List<String> stringList = Arrays.stream(string.split(","))
                .map(String::trim)
                .filter(e -> !e.isBlank())
                .toList();
        return List.copyOf(stringList);
    }


}
