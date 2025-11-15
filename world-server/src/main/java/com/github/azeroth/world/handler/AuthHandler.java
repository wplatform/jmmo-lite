package com.github.azeroth.world.handler;

import com.github.azeroth.auth.dto.AccountType;
import com.github.azeroth.auth.dto.RBACPermissions;
import com.github.azeroth.auth.dto.RealmJoinTicket;
import com.github.azeroth.auth.realm.*;
import com.github.azeroth.auth.service.AccountService;
import com.github.azeroth.common.Logs;
import com.github.azeroth.crypto.SessionKeyGenerator;
import com.github.azeroth.defines.BattleNetRpcErrorCode;
import com.github.azeroth.defines.Expansion;
import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.event.AccountEvent;
import com.github.azeroth.game.event.WorldEventPublisher;
import com.github.azeroth.game.networking.packet.authentication.*;
import com.github.azeroth.game.networking.packet.character.CharacterLoginFailed;
import com.github.azeroth.game.networking.packet.character.LoginFailureReason;
import com.github.azeroth.game.networking.packet.clientconfig.ClientCacheVersion;
import com.github.azeroth.game.networking.packet.misc.TimeSyncResponse;
import com.github.azeroth.game.networking.packet.system.EuropaTicketConfig;
import com.github.azeroth.game.networking.packet.system.FeatureSystemStatusGlueScreen;
import com.github.azeroth.game.networking.packet.system.SetTimeZoneInformation;
import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.game.world.setting.WorldSetting;
import com.github.azeroth.net.Connection;
import com.github.azeroth.net.server.NettyPipeline;
import com.github.azeroth.time.GameTime;
import com.github.azeroth.utils.JsonUtil;
import com.github.azeroth.utils.RandomUtil;
import com.github.azeroth.utils.SecureUtils;
import com.github.azeroth.world.World;
import com.github.azeroth.world.network.*;
import com.github.azeroth.world.session.WorldServerSession;
import com.github.azeroth.world.traffic.WorldProtocolCodec;
import lombok.RequiredArgsConstructor;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;


@RequiredArgsConstructor
public class AuthHandler {


    private static final byte[] AUTH_CHECK_SEED = {(byte) 0xDE, 0x3A, 0x2A, (byte) 0x8E, 0x6B, (byte) 0x89, 0x52, 0x66, (byte) 0x88, (byte) 0x9D, 0x7E, 0x7A, 0x77, 0x1D, 0x5D, 0x1F,
            0x4E, (byte) 0xD9, 0x0C, 0x23, (byte) 0x9B, (byte) 0xCD, 0x0E, (byte) 0xDC, (byte) 0xD2, (byte) 0xE8, 0x04, 0x3A, 0x68, 0x64, (byte) 0xC7, (byte) 0xB0};
    private static final byte[] SESSION_KEY_SEED = {(byte) 0xE8, 0x1E, (byte) 0x8B, 0x59, 0x27, 0x62, 0x1E, (byte) 0xAA, (byte) 0x86, 0x15, 0x18, (byte) 0xEA, (byte) 0xC0, (byte) 0xBF, 0x66, (byte) 0x8C,
            0x6D, (byte) 0xBF, (byte) 0x83, (byte) 0x93, (byte) 0xBC, (byte) 0xAA, (byte) 0x80, 0x52, 0x5B, 0x1E, (byte) 0xDC, 0x23, (byte) 0xA0, 0x12, (byte) 0xB7, 0x50};
    private static final byte[] CONTINUED_SESSION_SEED = {0x56, 0x5C, 0x61, (byte) 0x9C, 0x48, 0x3A, 0x52, 0x1F, 0x61, 0x5D, 0x05, 0x49, (byte) 0xB2, (byte) 0x9A, 0x39, (byte) 0xBF,
            0x4B, (byte) 0x97, (byte) 0xB0, 0x1B, (byte) 0xF9, 0x6C, (byte) 0xDE, (byte) 0xD6, (byte) 0x80, 0x1D, (byte) 0xAB, 0x26, 0x02, (byte) 0xA9, (byte) 0x9B, (byte) 0x9D};
    private static final byte[] ENCRYPTION_KEY_SEED = {0x71, (byte) 0xC9, (byte) 0xED, 0x5A, (byte) 0xA7, 0x0E, 0x4D, (byte) 0xFF, 0x4C, 0x36, (byte) 0xA6, 0x5A, 0x3E, 0x46, (byte) 0x8A, 0x4A,
            0x5D, (byte) 0xA1, 0x48, (byte) 0xC8, 0x30, 0x47, 0x4A, (byte) 0xDE, (byte) 0xF6, 0x0D, 0x6C, (byte) 0xBE, 0x6F, (byte) 0xE4, 0x55, 0x73};


    private final World world;
    private final WorldSetting worldSettings;
    private final AccountService accountService;
    private final WorldEventPublisher worldEventPublisher;
    private final RealmManager realmManager;


    public void handlePing(WorldRequest request, WorldResponse response) {
        WorldSession session = request.getSession();
        Ping ping = request.receiveObject(Ping.class);
        request.withConnection(connection -> {
            WorldConnection worldConnection = connection.as(WorldConnection.class);
            Instant lastPingTime = worldConnection.getLastPingTime();
            if (lastPingTime == null) {
                worldConnection.setLastPingTime(Instant.now());
            } else {
                Instant currentTime = Instant.now();
                Duration duration = Duration.between(lastPingTime, currentTime);
                if (duration.toSeconds() < 27) {
                    int overSpeedPings = worldConnection.incrementAndGetOverSpeedPings();
                    if (overSpeedPings > worldSettings.maxOverSpeedPings && !session.hasPermission(RBACPermissions.SKIP_CHECK_OVERSPEED_PING)) {
                        Logs.NETWORK.error("WorldSocket::HandlePing: {} kicked for over-speed pings (address: {})",
                                session.getPlayer(), request.getRemoteHost());
                        connection.close();
                    }
                } else {
                    worldConnection.setOverSpeedPings(0);
                }
            }
        });
        session.setLatency(ping.latency);
        response.setWorldPacket(new Pong(ping.serial));
    }


    public void handleAuthSession(WorldRequest request, WorldResponse response) {
        AuthSession authSession = request.receiveObject(AuthSession.class);
        try {
            var joinTicket = JsonUtil.fromJson(authSession.realmJoinTicket, RealmJoinTicket.class);
            // Get the account information from the auth database
            var accountInfo = accountService.selectAccountInfoByUserName(joinTicket.getGameAccount(), worldSettings.realmID);
            if (accountInfo.isEmpty()) {
                Logs.NETWORK.error("HandleAuthSession: Sent Auth Response (unknown account). {}", joinTicket.getGameAccount());
                request.withConnection(Connection::close);
                return;
            }
            var account = accountInfo.get();
            var clientBuild = realmManager.getBuildInfo(account.getClientBuild());
            if (clientBuild == null) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.BAD_VERSION);
                response.setWorldPacket(packet);
                Logs.NETWORK.error("HandleAuthSession: Missing client build info for build {} ({}).", account.getClientBuild(), account.getAccountId());
                request.withConnection(Connection::close);
                return;
            }
            var buildVariant = VariantId.of(joinTicket.getPlatform(), joinTicket.getClientArch(), joinTicket.getType());
            var authKey = clientBuild.getAuthKeys().stream()
                    .filter(key -> key.variant().equals(buildVariant))
                    .findFirst();
            if (authKey.isEmpty()) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.BAD_VERSION);
                response.setWorldPacket(packet);
                Logs.NETWORK.error("HandleAuthSession: Missing client build auth key for build {} variant {} ({}).", account.getClientBuild(),
                        buildVariant, request.getRemoteHost());
                request.withConnection(Connection::close);
                return;
            }

            request.withConnection(connection -> {
                WorldConnection worldConnection = (WorldConnection) connection;
                var digestKeyHash = SecureUtils.sha512(account.getSessionKey(), authKey.get().key());
                var sessionKeyHmac = SecureUtils.hmacSHA512(digestKeyHash,
                        worldConnection.getServerChallenge(), authSession.localChallenge, SESSION_KEY_SEED);
                var sessionKeyGenerator = new SessionKeyGenerator(sessionKeyHmac);
                sessionKeyGenerator.generate(worldConnection.getSessionKey(), worldConnection.getSessionKey().length);
                var encryptKeyGen = SecureUtils.hmacSHA512(worldConnection.getSessionKey(), authSession.localChallenge,
                        worldConnection.getServerChallenge(), ENCRYPTION_KEY_SEED);
                System.arraycopy(encryptKeyGen, 0, worldConnection.getEncryptKey(), 0, encryptKeyGen.length);
            });


            if (worldSettings.allowLoggingIPAddressesInDatabase) {
                accountService.updateAccountLastAttemptIp(request.getRemoteHost(), joinTicket.getGameAccount());
            }

            request.withConnection(connection -> {
                WorldConnection worldConnection = (WorldConnection) connection;
                accountService.updateAccountContinuedSession(worldConnection.getSessionKey(), account.getAccountId());
            });

            if (authSession.realmID != worldSettings.realmID) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.DENIED);
                response.setWorldPacket(packet);
                Logs.NETWORK.error("HandleAuthSession: Client {} requested connecting with realm id {} but this realm has id {} set in config.",
                        request.getRemoteHost(), authSession.realmID, worldSettings.realmID);
                request.withConnection(Connection::close);
                return;
            }

            if (worldSettings.wardenEnabled && !Platform.isValid(account.getOs())) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.DENIED);
                response.setWorldPacket(packet);
                Logs.NETWORK.error("HandleAuthSession: Client {} attempted to log in using invalid client OS ({}).",
                        request.getRemoteHost(), account.getOs());
                request.withConnection(Connection::close);
                return;
            }


            if (account.getIsBnetBanned()) {

                if (!Objects.equals(account.getLastIp(), request.getRemoteHost())) {
                    AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.RISK_ACCOUNT_LOCKED);
                    response.setWorldPacket(packet);
                    Logs.NETWORK.debug("HandleAuthSession: Sent Auth Response (Account IP differs. Original IP: {}, new IP: {}).", account.getLastIp(), request.getRemoteHost());
                    // We could log on hook only instead of an additional db log, however action logger is config based. Better keep DB logging as well
                    worldEventPublisher.publish(new AccountEvent(account.getAccountId(), AccountEvent.ON_FAILED_ACCOUNT_LOGIN));
                    request.withConnection(Connection::close);
                    return;
                }
            }

            long mutetime = account.getMuteTime();
            //! Negative mutetime indicates amount of seconds to be muted effective on next login - which is now.
            if (mutetime < 0) {
                mutetime = GameTime.getGameTime() - mutetime;
                accountService.updateAccountMuteTimeLogin(mutetime, account.getAccountId());
            }

            if (account.getIsBanned()) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.GAME_ACCOUNT_BANNED);
                response.setWorldPacket(packet);
                Logs.NETWORK.error("HandleAuthSession: Client {} Sent Auth Response (Account banned).", request.getRemoteHost());
                request.withConnection(Connection::close);
                return;
            }


            AccountType allowedAccountType = world.getPlayerSecurityLimit();
            Logs.NETWORK.debug("Allowed Level: {} Player Level {}", allowedAccountType, account.getSecurityLevel());
            if (allowedAccountType.ordinal() > AccountType.SEC_PLAYER.ordinal() && account.getSecurityLevel() < allowedAccountType.ordinal()) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.SERVER_IS_PRIVATE);
                response.setWorldPacket(packet);
                Logs.NETWORK.debug("HandleAuthSession: User tries to login but his security level is not enough");
                worldEventPublisher.publish(new AccountEvent(account.getAccountId(), AccountEvent.ON_FAILED_ACCOUNT_LOGIN));
                request.withConnection(Connection::close);
                return;
            }

            Logs.NETWORK.debug("WorldSocket::HandleAuthSession: Client '{}' authenticated successfully from {}.",
                    joinTicket.getGameAccount(), request.getRemoteHost());

            if (worldSettings.allowLoggingIPAddressesInDatabase) {
                // Update the last_ip in the database as it was successful for login
                accountService.updateAccountLastIp(request.getRemoteHost(), joinTicket.getGameAccount());
            }

            request.withConnection(connection -> {
                NetworkOperations networkOperations = connection.as(NetworkOperations.class);
                networkOperations.initSession(account, buildVariant);
                //todo load RBAC for account
                response.setWorldPacket(new EnterEncryptedMode(networkOperations.getEncryptKey(), true));

            });
            // At this point, we can safely hook a successful login
            worldEventPublisher.publish(new AccountEvent(account.getAccountId(), AccountEvent.ON_ACCOUNT_LOGIN));

        } catch (Exception e) {
            Logs.NETWORK.error("HandleAuthSession: Invalid join ticket (address: {})", request.getRemoteHost(), e);
            AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.WOW_SERVICES_INVALID_JOIN_TICKET);
            response.setWorldPacket(packet);
            request.withConnection(Connection::close);
        }
    }


    public void handleAuthContinuedSession(WorldRequest request, WorldResponse response) {
        AuthContinuedSession authSession = request.receiveObject(AuthContinuedSession.class);
        ConnectToKey key = new ConnectToKey(authSession.key);

        request.withConnection(connection -> {
            NetworkOperations networkOperations = connection.as(NetworkOperations.class);
            networkOperations.setType(key.connectionType);
            networkOperations.setId(key.key);
        });

        if (key.connectionType != ConnectionType.INSTANCE) {
            AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.DENIED);
            response.setWorldPacket(packet);
            request.withConnection(Connection::close);
            return;
        }

        var accountInfo = accountService.selectAccountContinuedSession(key.accountId);
        if (accountInfo.isEmpty()) {
            AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.DENIED);
            response.setWorldPacket(packet);
            request.withConnection(Connection::close);
            return;
        }

        String login = accountInfo.get().get("username").toString();
        byte[] sessionKey = (byte[]) accountInfo.get().get("session_key_bnet");

        request.withConnection(connection -> {
            NetworkOperations conn = connection.as(NetworkOperations.class);
            byte[] hmacSHA512 = SecureUtils.hmacSHA512(sessionKey, ByteBuffer.allocate(8).putLong(authSession.key).array(),
                    authSession.localChallenge, conn.getServerChallenge(), CONTINUED_SESSION_SEED);

            if (Arrays.compare(hmacSHA512, authSession.digest) != 0) {
                AuthResponse packet = new AuthResponse(BattleNetRpcErrorCode.DENIED);
                response.setWorldPacket(packet);
                connection.close();
                Logs.NETWORK.error("HandleAuthContinuedSession: Authentication failed for account: {} ('{}') address: {}", key.accountId, login, request.getRemoteHost());
                return;
            }

            byte[] encryptKeyGen = SecureUtils.hmacSHA512(sessionKey, authSession.localChallenge,
                    conn.getServerChallenge(), ENCRYPTION_KEY_SEED);

            // only first 32 bytes of the hmac are used
            byte[] encryptKey = conn.getEncryptKey();
            System.arraycopy(encryptKeyGen, 0, encryptKey, 0, encryptKey.length);
            var enterEncryptedMode = new EnterEncryptedMode(encryptKey, true);
            response.setWorldPacket(enterEncryptedMode);
        });
    }


    public void handleConnectToFailed(WorldRequest request, WorldResponse response) {
        var connectToFailed = request.receiveObject(ConnectToFailed.class);
        WorldSession session = request.getSession();
        if (session.getAttribute("loadingPlayer") != null) {
            switch (connectToFailed.serial) {
                case WorldAttempt1:
                    sendConnectToInstance(request, response, ConnectToSerial.WorldAttempt2);
                    break;
                case WorldAttempt2:
                    sendConnectToInstance(request, response, ConnectToSerial.WorldAttempt3);
                    break;
                case WorldAttempt3:
                    sendConnectToInstance(request, response, ConnectToSerial.WorldAttempt4);
                    break;
                case WorldAttempt4:
                    sendConnectToInstance(request, response, ConnectToSerial.WorldAttempt5);
                    break;
                case WorldAttempt5: {
                    Logs.NETWORK.error("{} failed to connect 5 times to world socket, aborting login", session);
                    abortLogin(request, response, LoginFailureReason.NO_WORLD);
                    break;
                }
                default:
            }
        }
    }

    public void handleEnterEncryptedModeAck(WorldRequest request, WorldResponse response) {
        request.withConnection(connection -> {
            var conn = connection.as(NetworkOperations.class);
            var codec = (WorldProtocolCodec) connection.channel().pipeline().get(NettyPipeline.WorldProtocolCodec);
            codec.enterEncryptedMode(conn.getEncryptKey());
            if (conn.getType() == ConnectionType.REALM) {
                world.addSession(conn.getSession());
            } else {
                world.addInstanceConnection(conn);
            }
        });
    }


    private void sendAuthResponse(WorldRequest request, WorldResponse response, BattleNetRpcErrorCode code, boolean queued, int queuePos) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.result = code;

        if (code == BattleNetRpcErrorCode.OK) {
            authResponse.successInfo = new AuthResponse.AuthSuccessInfo();

            authResponse.successInfo.activeExpansionLevel = request.getSession().getExpansion().getValue();
            authResponse.successInfo.accountExpansionLevel = 0; // GetAccountExpansion(); -- Classic Only: always send as 0
            authResponse.successInfo.time = (int) GameTime.getGameTime();

            // Send current home realm. Also there is no need to send it later in realm queries.
            var currentRealm = realmManager.getCurrentRealm();
            if (currentRealm != null) {
                authResponse.successInfo.virtualRealmAddress = currentRealm.getId().getAddress();
                authResponse.successInfo.virtualRealms.add(new VirtualRealmInfo(currentRealm.getId().getAddress(),
                        new VirtualRealmNameInfo(true, false, currentRealm.getName(), currentRealm.getNormalizedName())));
            }

            var objectManager = world.getObjectManager();
            if (request.getSession().hasPermission(RBACPermissions.USE_CHARACTER_TEMPLATES)) {
                authResponse.successInfo.templates.addAll(objectManager.getCharacterTemplateStorage().values());
            }

            authResponse.successInfo.availableClasses = objectManager.getClassExpansionRequirements();

            // TEMPORARY - prevent creating characters in uncompletable zone
            // This has the side effect of disabling Exile's Reach choice clientside without actually forcing character templates
            authResponse.successInfo.forceCharacterTemplate = world.getDisableManager().isDisabledFor(DisableType.MAP, 2175 /*Exile's Reach*/, null);
        }

        if (queued) {
            authResponse.waitInfo = new AuthWaitInfo();
            authResponse.waitInfo.waitCount = queuePos;
        }
        response.setWorldPacket(authResponse);
    }

    private void sendAuthWaitQueue(int position, WorldResponse response) {
        if (position != 0) {
            WaitQueueUpdate waitQueueUpdate = new WaitQueueUpdate();
            waitQueueUpdate.waitInfo.waitCount = position;
            waitQueueUpdate.waitInfo.waitTime = 0;
            waitQueueUpdate.waitInfo.hasFCM = false;
            response.setWorldPacket(waitQueueUpdate);
        } else
            response.setWorldPacket(new WaitQueueFinish());
    }

    private void sendClientCacheVersion(int version, WorldResponse response) {
        ClientCacheVersion cache = new ClientCacheVersion();
        cache.cacheVersion = version;
        response.setWorldPacket(cache);
    }

    private void sendSetTimeZoneInformation(WorldResponse response) {
        SetTimeZoneInformation packet = new SetTimeZoneInformation();
        String timeTZ = ZoneId.systemDefault().toString();
        packet.serverTimeTZ = timeTZ;
        packet.gameTimeTZ = timeTZ;
        packet.serverRegionalTZ = timeTZ;
        response.setWorldPacket(packet);
    }

    private void sendFeatureSystemStatusGlueScreen(WorldSetting worldSettings, WorldResponse response) {
        FeatureSystemStatusGlueScreen features = new FeatureSystemStatusGlueScreen();
        features.bpayStoreAvailable = false;
        features.bpayStoreDisabledByParentalControls = false;
        features.charUndeleteEnabled = worldSettings.featureSystemCharacterUndeleteEnabled;
        features.bpayStoreEnabled = worldSettings.featureSystemPayStoreEnabled;
        features.maxCharactersPerRealm = worldSettings.charactersPerRealm;
        features.minimumExpansionLevel = Expansion.CLASSIC.getValue();
        features.maximumExpansionLevel = worldSettings.expansion.getValue();

        features.europaTicketSystemStatus = new EuropaTicketConfig();
        features.europaTicketSystemStatus.throttleState.maxTries = 10;
        features.europaTicketSystemStatus.throttleState.perMilliseconds = 60000;
        features.europaTicketSystemStatus.throttleState.tryCount = 1;
        features.europaTicketSystemStatus.throttleState.lastResetTimeBeforeNow = 111111;
        features.europaTicketSystemStatus.ticketsEnabled = worldSettings.supportTicketsEnabled;
        features.europaTicketSystemStatus.bugsEnabled = worldSettings.supportBugsEnabled;
        features.europaTicketSystemStatus.complaintsEnabled = worldSettings.supportComplaintsEnabled;
        features.europaTicketSystemStatus.suggestionsEnabled = worldSettings.supportSuggestionsEnabled;
        response.setWorldPacket(features);
    }


    void sendConnectToInstance(WorldRequest request, WorldResponse response, ConnectToSerial serial) {
        var session = (WorldServerSession)request.getSession();
        request.withConnection(connection -> {
            InetAddress clientAddress = ((InetSocketAddress) connection.remoteAddress()).getAddress();
            Realm currentRealm = realmManager.getCurrentRealm();
            InetAddress instanceAddress = currentRealm.getAddressForClient(clientAddress);

            ConnectToKey instanceConnectKey = new ConnectToKey(session.getAccountId(), ConnectionType.INSTANCE, RandomUtil.randomInt(0, 0x7FFFFFFF));
            session.setInstanceConnectKey(instanceConnectKey);

            ConnectTo connectTo = new ConnectTo();
            connectTo.key = instanceConnectKey.getRaw();
            connectTo.serial = serial;
            connectTo.payload.port = (short) worldSettings.worldServerPort;
            connectTo.con = (byte) ConnectionType.INSTANCE.ordinal();

            switch (instanceAddress) {
                case Inet4Address inet4Address -> {
                    connectTo.payload.where.type = ConnectTo.AddressType.IPv4;
                    connectTo.payload.where.IPv4 = inet4Address.getAddress();
                }
                case Inet6Address inet6Address -> {
                    if(inet6Address.isLoopbackAddress()) {
                        connectTo.payload.where.IPv4 = inet6Address.getAddress();
                        connectTo.payload.where.type = ConnectTo.AddressType.IPv4;
                    } else if(inet6Address.isIPv4CompatibleAddress()) {
                        byte[] addressBytes = inet6Address.getAddress();
                        // Extract the last 4 bytes which represent the IPv4 address
                        byte[] ipv4Bytes = new byte[4];
                        System.arraycopy(addressBytes, addressBytes.length - 4, ipv4Bytes, 0, 4);
                        connectTo.payload.where.IPv4 = ipv4Bytes;
                        connectTo.payload.where.type = ConnectTo.AddressType.IPv4;
                    } else {
                        connectTo.payload.where.type = ConnectTo.AddressType.IPv6;
                        connectTo.payload.where.IPv6 = inet6Address.getAddress();
                    }
                }
                default -> throw new IllegalStateException("Unknow inet address type value: " + instanceAddress);
            }
            response.setWorldPacket(connectTo);
        });

    }


    private void abortLogin(WorldRequest request, WorldResponse response, LoginFailureReason reason) {

        var session = (WorldServerSession)request.getSession();
        var loadingPlayer = (ObjectGuid)session.getAttribute("loadingPlayer");
        if (loadingPlayer == null || session.getPlayer() != null) {
            session.kickout("AbortLogin incorrect player state when logging in");
            return;
        }
        response.setWorldPacket(new CharacterLoginFailed(reason));
    }


    void resetTimeSync(WorldSession session)
    {
        _timeSyncNextCounter = 0;
        _pendingTimeSyncRequests.clear();
    }

    void SendTimeSync()
    {
        WorldPackets::Misc::TimeSyncRequest timeSyncRequest;
        timeSyncRequest.SequenceIndex = _timeSyncNextCounter;
        SendPacket(timeSyncRequest.Write());

        RegisterTimeSync(_timeSyncNextCounter);

        // Schedule next sync in 10 sec (except for the 2 first packets, which are spaced by only 5s)
        _timeSyncTimer = _timeSyncNextCounter == 0 ? 5000 : 10000;
        _timeSyncNextCounter++;
    }

    void HandleTimeSyncResponse(TimeSyncResponse timeSyncResponse)
    {
        HandleTimeSync(timeSyncResponse.sequenceIndex, timeSyncResponse.clientTime, timeSyncResponse.GetReceivedTime());
    }

    void HandleQueuedMessagesEnd(QueuedMessagesEnd queuedMessagesEnd)
    {
        HandleTimeSync(SPECIAL_RESUME_COMMS_TIME_SYNC_COUNTER, queuedMessagesEnd.Timestamp, queuedMessagesEnd.GetRawPacket()->GetReceivedTime());
    }

    void HandleMoveInitActiveMoverComplete(MoveInitActiveMoverComplete moveInitActiveMoverComplete)
    {
        HandleTimeSync(SPECIAL_INIT_ACTIVE_MOVER_TIME_SYNC_COUNTER, moveInitActiveMoverComplete.Ticks, moveInitActiveMoverComplete.GetRawPacket()->GetReceivedTime());

        _player->UpdateObjectVisibility(false);
    }


    private void HandleTimeSync(WorldRequest request, int task, long clientTime)
    {
        Duration duration = request.getSession().getStopWatch().stop(task);
        // time it took for the request to travel to the client, for the client to process it and reply and for response to travel back to the server.
        // we are going to make 2 assumptions:
        // 1) we assume that the request processing time equals 0.
        // 2) we assume that the packet took as much time to travel from server to client than it took to travel from client to server.
        Duration lagDelayDuration = duration.dividedBy(2);

    /*
    clockDelta = serverTime - clientTime
    where
    serverTime: time that was displayed on the clock of the SERVER at the moment when the client processed the SMSG_TIME_SYNC_REQUEST packet.
    clientTime:  time that was displayed on the clock of the CLIENT at the moment when the client processed the SMSG_TIME_SYNC_REQUEST packet.

    Once clockDelta has been computed, we can compute the time of an event on server clock when we know the time of that same event on the client clock,
    using the following relation:
    serverTime = clockDelta + clientTime
    */
        int clockDelta = serverTimeAtSent.mapped() + lagDelay - clientTime;
        _timeSyncClockDeltaQueue->push_back(std::pair<int64, uint32>(clockDelta, duration));
        ComputeNewClockDelta();
    }

}
