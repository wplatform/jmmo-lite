package com.github.azeroth.portal.rpc.service;

import bnet.protocol.AttributeProto;
import bnet.protocol.RpcProto;
import bnet.protocol.game_utilities.v1.GameUtilitiesServiceProto;
import com.github.azeroth.utils.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.github.azeroth.service.auth.domain.Account;
import com.github.azeroth.service.auth.domain.AccountLastPlayedCharacter;
import com.github.azeroth.service.auth.domain.Realmcharacter;
import com.github.azeroth.service.auth.domain.Realmlist;
import com.github.azeroth.service.auth.dto.AccountInfo;
import com.github.azeroth.service.auth.dto.GameAccount;
import com.github.azeroth.service.auth.repository.AccountLastPlayedCharacterRepository;
import com.github.azeroth.service.auth.repository.AccountRepository;
import com.github.azeroth.service.auth.repository.RealmcharacterRepository;
import com.github.azeroth.common.RpcErrorCode;
import com.github.azeroth.portal.model.RealmProto;
import com.github.azeroth.portal.realm.ClientBuild;
import com.github.azeroth.portal.realm.Realm;
import com.github.azeroth.portal.realm.RealmKey;
import com.github.azeroth.portal.realm.RealmManager;
import com.github.azeroth.portal.rpc.DefaultRpcController;
import com.github.azeroth.portal.rpc.RpcSession;
import com.github.azeroth.portal.utils.LocaleConstant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameUtilitiesService implements GameUtilitiesServiceProto.GameUtilitiesService.Interface {

    private final RealmManager realmManager;
    private final AccountRepository accountRepo;
    private final RealmcharacterRepository realmCharacterRepo;
    private final AccountLastPlayedCharacterRepository accountLastPlayedCharacterRepo;

    @Override
    @Transactional
    public void processClientRequest(RpcController controller, GameUtilitiesServiceProto.ClientRequest request,
                                     RpcCallback<GameUtilitiesServiceProto.ClientResponse> done) {
        DefaultRpcController rpcController = (DefaultRpcController) controller;
        if (!rpcController.getSession().isAuthorized()) {
            rpcController.setFailed(RpcErrorCode.ERROR_DENIED);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        AttributeProto.Attribute command = request.getAttributeList().stream()
                .filter(e -> e.getName().startsWith("Command_")).findFirst().orElse(null);

        if (command == null) {
            log.error(rpcController.format("ClientRequest with no command."));
            rpcController.setFailed(RpcErrorCode.ERROR_RPC_MALFORMED_REQUEST);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        switch (command.getName()) {
            case "Command_RealmListTicketRequest_v1_b9":
                processRealmListTicketRequest(rpcController, request, done);
                break;
            case "Command_LastCharPlayedRequest_v1_b9":
                processLastCharPlayedRequest(rpcController, request, done);
                break;
            case "Command_RealmListRequest_v1_b9":
                processRealmListRequest(rpcController, request, done);
                break;
            case "Command_RealmJoinRequest_v1_b9":
                processRealmJoinRequest(rpcController, request, done);
                break;
            default:
                log.error(rpcController.format("ClientRequest with unknown command {}."), command);
                rpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
                done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
                break;
        }

    }

    @Override
    public void presenceChannelCreated(RpcController controller,
                                       GameUtilitiesServiceProto.PresenceChannelCreatedRequest request,
                                       RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NoData.getDefaultInstance());
    }

    @Override
    public void processServerRequest(RpcController controller,
                                     GameUtilitiesServiceProto.ServerRequest request,
                                     RpcCallback<GameUtilitiesServiceProto.ServerResponse> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(GameUtilitiesServiceProto.ServerResponse.getDefaultInstance());
    }

    @Override
    public void onGameAccountOnline(RpcController controller,
                                    GameUtilitiesServiceProto.GameAccountOnlineNotification request,
                                    RpcCallback<RpcProto.NO_RESPONSE> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NO_RESPONSE.getDefaultInstance());
    }

    @Override
    public void onGameAccountOffline(RpcController controller,
                                     GameUtilitiesServiceProto.GameAccountOfflineNotification request,
                                     RpcCallback<RpcProto.NO_RESPONSE> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NO_RESPONSE.getDefaultInstance());
    }

    @Override
    public void getAllValuesForAttribute(RpcController controller,
                                         GameUtilitiesServiceProto.GetAllValuesForAttributeRequest request,
                                         RpcCallback<GameUtilitiesServiceProto.GetAllValuesForAttributeResponse> done) {
        DefaultRpcController rpcController = (DefaultRpcController) controller;
        if (!rpcController.getSession().isAuthorized()) {
            rpcController.setFailed(RpcErrorCode.ERROR_DENIED);
            done.run(GameUtilitiesServiceProto.GetAllValuesForAttributeResponse.getDefaultInstance());
            return;
        }

        if (!"Command_RealmListRequest_v1_b9".equals(request.getAttributeKey())) {
            rpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
            done.run(GameUtilitiesServiceProto.GetAllValuesForAttributeResponse.getDefaultInstance());
            return;
        }
        GameUtilitiesServiceProto.GetAllValuesForAttributeResponse.Builder builder = GameUtilitiesServiceProto.GetAllValuesForAttributeResponse.newBuilder();
        realmManager.realmKeys().forEach(
                realmKey -> builder.addAttributeValue(AttributeProto.Variant.newBuilder()
                        .setStringValue(realmKey.toSubRegionAddressString()).build())
        );
        done.run(builder.build());
    }

    @Override
    public void registerUtilities(RpcController controller,
                                  GameUtilitiesServiceProto.RegisterUtilitiesRequest request,
                                  RpcCallback<GameUtilitiesServiceProto.RegisterUtilitiesResponse> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(GameUtilitiesServiceProto.RegisterUtilitiesResponse.getDefaultInstance());
    }

    @Override
    public void unregisterUtilities(RpcController controller,
                                    GameUtilitiesServiceProto.UnregisterUtilitiesRequest request,
                                    RpcCallback<RpcProto.NO_RESPONSE> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NO_RESPONSE.getDefaultInstance());
    }


    private void processRealmJoinRequest(DefaultRpcController controller,
                                         GameUtilitiesServiceProto.ClientRequest request,
                                         RpcCallback<GameUtilitiesServiceProto.ClientResponse> done) {

        Map<String, AttributeProto.Attribute> attributeMap = request.getAttributeList().stream()
                .collect(Collectors.toMap(AttributeProto.Attribute::getName, v -> v, (v1, v2) -> v1));
        AttributeProto.Attribute attribute = attributeMap.get("Param_RealmAddress");
        RpcSession rpcSession = controller.getSession();
        AccountInfo selectAccountInfo = rpcSession.getAccountInfo();
        if (attribute == null || selectAccountInfo == null) {
            controller.setFailed(RpcErrorCode.ERROR_WOW_SERVICES_INVALID_JOIN_TICKET);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }
        int build = rpcSession.getBuild();
        long realmAddress = attribute.getValue().getUintValue();
        RealmKey realmKey = RealmKey.createRealmKey((int) realmAddress);
        Realm realm = realmManager.getRealmByKey(realmKey);
        if (realm == null || (realm.getFlags() & Realm.REALM_FLAG_OFFLINE) != 0 || realm.getBuild() != build) {
            controller.setFailed(RpcErrorCode.ERROR_USER_SERVER_NOT_PERMITTED_ON_REALM);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        RealmProto.RealmListServerIPAddresses serverAddresses = new RealmProto.RealmListServerIPAddresses();
        RealmProto.RealmIPAddressFamily addressFamily = new RealmProto.RealmIPAddressFamily();
        RealmProto.IPAddress address = new RealmProto.IPAddress();
        address.setIp(getAddressForClient(realm, controller.remoteAddress().getAddress()));
        address.setPort(realm.getPort());
        addressFamily.setFamily(1);
        addressFamily.setAddresses(List.of(address));
        serverAddresses.setFamilies(List.of(addressFamily));

        String json = "JSONRealmListServerIPAddresses:" + JsonUtil.toJson(serverAddresses);
        ByteString compressedValue = getCompressedValue(json);

        byte[] clientSecret = rpcSession.getClientSecret();
        byte[] serverSecret = RandomUtil.randomBytes(SysProperties.PORTAL_SERVER_SECRET_LENGTH);
        byte[] keyData = new byte[SysProperties.PORTAL_CLIENT_SECRET_LENGTH + SysProperties.PORTAL_SERVER_SECRET_LENGTH];
        System.arraycopy(clientSecret, 0, keyData, 0, SysProperties.PORTAL_CLIENT_SECRET_LENGTH);
        System.arraycopy(serverSecret, 0, keyData, SysProperties.PORTAL_CLIENT_SECRET_LENGTH, SysProperties.PORTAL_SERVER_SECRET_LENGTH);

        Optional<Account> accountRepoById = accountRepo.findById(selectAccountInfo.getId());
        if (accountRepoById.isEmpty()) {
            controller.setFailed(RpcErrorCode.ERROR_USER_SERVER_BAD_WOW_ACCOUNT);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        Account account = accountRepoById.get();
        account.setOs(rpcSession.getPlatform());
        account.setLastIp(controller.remoteAddress().getAddress().getHostName());
        account.setSessionKeyBnet(SecureUtils.bytesToHexString(keyData));
        account.setLocale((short) LocaleConstant.fromName(rpcSession.getLocale()).ordinal());
        account.setOs(rpcSession.getPlatform());
        accountRepo.save(account);


        GameUtilitiesServiceProto.ClientResponse.Builder builder = GameUtilitiesServiceProto.ClientResponse.newBuilder();

        builder.addAttribute(AttributeProto.Attribute.newBuilder()
                .setName("Param_RealmJoinTicket")
                .setValue(AttributeProto.Variant.newBuilder().setBlobValue(ByteString.copyFromUtf8(account.getUsername())).build()).build());

        builder.addAttribute(AttributeProto.Attribute.newBuilder()
                .setName("Param_ServerAddresses")
                .setValue(AttributeProto.Variant.newBuilder().setBlobValue(compressedValue).build()).build());

        builder.addAttribute(AttributeProto.Attribute.newBuilder()
                .setName("Param_JoinSecret")
                .setValue(AttributeProto.Variant.newBuilder().setBlobValue(ByteString.copyFrom(serverSecret)).build()).build());

        done.run(builder.build());
    }

    private void processRealmListRequest(DefaultRpcController controller,
                                         GameUtilitiesServiceProto.ClientRequest request,
                                         RpcCallback<GameUtilitiesServiceProto.ClientResponse> done) {

        RpcSession rpcSession = controller.getSession();
        AccountInfo accountInfo = rpcSession.getAccountInfo();
        if (accountInfo.getAccounts().isEmpty()) {
            controller.setFailed(RpcErrorCode.ERROR_USER_SERVER_BAD_WOW_ACCOUNT);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        Map<String, AttributeProto.Attribute> attributeMap = request.getAttributeList().stream()
                .collect(Collectors.toMap(AttributeProto.Attribute::getName, v -> v, (v1, v2) -> v1));
        AttributeProto.Attribute attribute = attributeMap.get("Command_RealmListRequest_v1_b9");
        if (attribute == null) {
            controller.setFailed(RpcErrorCode.ERROR_UTIL_SERVER_UNKNOWN_REALM);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }
        String subRegionId = attribute.getValue().getStringValue();
        int build = rpcSession.getBuild();
        List<RealmKey> realmKeys = realmManager.realmKeys().stream()
                .filter(v -> subRegionId.equals(v.toSubRegionAddressString()))
                .toList();
        RealmProto.RealmListUpdates realmList = new RealmProto.RealmListUpdates();

        List<RealmProto.RealmState> realmStates = realmKeys.stream().map(realmManager::getRealmByKey).map(v -> {
            RealmProto.RealmState state = new RealmProto.RealmState();
            RealmProto.RealmEntry realmEntry = getRealmEntry(build, v);
            realmEntry.setPopulationState((v.getFlags() & Realm.REALM_FLAG_OFFLINE) != 0 ? 0 : Math.max((int) v.getPopulationLevel(), 1));
            realmEntry.setFlags(v.getBuild() != build ? v.getFlags() | Realm.REALM_FLAG_VERSION_MISMATCH : v.getFlags());
            state.setUpdate(realmEntry);
            state.setDeleting(false);
            return state;
        }).collect(Collectors.toList());
        realmList.setUpdates(realmStates);
        String value = "JSONRealmListUpdates:" + JsonUtil.toJson(realmList);

        ByteString compressedValue = getCompressedValue(value);

        if (compressedValue.isEmpty()) {
            controller.setFailed(RpcErrorCode.ERROR_UTIL_SERVER_FAILED_TO_SERIALIZE_RESPONSE);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        GameUtilitiesServiceProto.ClientResponse.Builder builder = GameUtilitiesServiceProto.ClientResponse.newBuilder();
        builder.addAttribute(AttributeProto.Attribute.newBuilder()
                .setName("Param_RealmList")
                .setValue(AttributeProto.Variant.newBuilder().setBlobValue(compressedValue).build()).build());

        Map<Long, GameAccount> gameAccountMap = accountInfo.getAccounts();
        Object[][] byIdAccounts = realmCharacterRepo.findByIdAccounts(gameAccountMap.keySet());

        List<RealmProto.RealmCharacterCountEntry> countEntries = Arrays.stream(byIdAccounts).map(objects -> {
            Realmcharacter rc = (Realmcharacter) objects[0];
            Realmlist r = (Realmlist) objects[1];
            int address = RealmKey.createRealmKey(r.getRegion(), r.getBattlegroup(), r.getId().intValue()).getAddress();
            RealmProto.RealmCharacterCountEntry countEntry = new RealmProto.RealmCharacterCountEntry();
            countEntry.setWowRealmAddress(address);
            countEntry.setCount(rc.getNumchars() == null ? 0 : rc.getNumchars());
            return countEntry;
        }).toList();

        RealmProto.RealmCharacterCountList countList = new RealmProto.RealmCharacterCountList();
        countList.setCounts(countEntries);
        value = "JSONRealmCharacterCountList:" + JsonUtil.toJson(countList);
        compressedValue = getCompressedValue(value);
        builder.addAttribute(AttributeProto.Attribute.newBuilder()
                .setName("Param_CharacterCountList")
                .setValue(AttributeProto.Variant.newBuilder().setBlobValue(compressedValue).build()).build());

        done.run(builder.build());
    }

    private ByteString getCompressedValue(String value) {
        //for c++ std::string end with '\0';
        value += "\0";
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] compressed = Compress.compress(valueBytes);
        byte[] compressedWithLength = new byte[compressed.length + 4];

        //Data is always little endian in c
        ByteBuffer.wrap(compressedWithLength).order(ByteOrder.LITTLE_ENDIAN).putInt(valueBytes.length).put(compressed);
        return ByteString.copyFrom(compressedWithLength);
    }

    private void processLastCharPlayedRequest(DefaultRpcController controller,
                                              GameUtilitiesServiceProto.ClientRequest request,
                                              RpcCallback<GameUtilitiesServiceProto.ClientResponse> done) {
        Map<String, AttributeProto.Attribute> attributeMap = request.getAttributeList().stream()
                .collect(Collectors.toMap(AttributeProto.Attribute::getName, v -> v, (v1, v2) -> v1));
        AttributeProto.Attribute attribute = attributeMap.get("Command_LastCharPlayedRequest_v1_b9");
        if (attribute == null) {
            controller.setFailed(RpcErrorCode.ERROR_UTIL_SERVER_UNKNOWN_REALM);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }
        String stringValue = attribute.getValue().getStringValue();
        RpcSession rpcSession = controller.getSession();
        AccountInfo accountInfo = rpcSession.getAccountInfo();
        List<AccountLastPlayedCharacter> byIdAccountIds = accountLastPlayedCharacterRepo.findByIdAccountIds(accountInfo.getAccounts().keySet());
        AccountLastPlayedCharacter playedCharacter = byIdAccountIds.stream()
                .filter(v -> stringValue.equals(RealmKey.getSubRegionAddressString(v.getId().getRegion(), v.getId().getBattlegroup())))
                .findFirst().orElse(null);
        GameUtilitiesServiceProto.ClientResponse response = GameUtilitiesServiceProto.ClientResponse.getDefaultInstance();
        if (playedCharacter != null) {
            int build = rpcSession.getBuild();
            RealmKey realmKey = RealmKey.createRealmKey(playedCharacter.getId().getRegion(), playedCharacter.getId().getBattlegroup(), playedCharacter.getRealmId().intValue());
            Realm realm = realmManager.getRealmByKey(realmKey);
            if (realm != null && realm.getBuild() == build) {
                RealmProto.RealmEntry entry = getRealmEntry(build, realm);
                String value = "JamJSONRealmEntry:" + JsonUtil.toJson(entry);

                /*
                    uLong compressedLength = compressBound(uLong(json.length()));
                    compressed.resize(compressedLength + 4);
                    *reinterpret_cast<uint32*>(compressed.data()) = uint32(json.length() + 1);

                    if (compress(compressed.data() + 4, &compressedLength, reinterpret_cast<uint8 const*>(json.c_str()), uLong(json.length() + 1)) == Z_OK)
                        compressed.resize(compressedLength + 4);
                    else
                    compressed.clear();
                 */

                GameUtilitiesServiceProto.ClientResponse.Builder builder = GameUtilitiesServiceProto.ClientResponse.newBuilder();

                builder.addAttribute(AttributeProto.Attribute.newBuilder()
                        .setName("Param_RealmEntry")
                        .setValue(AttributeProto.Variant.newBuilder().setBlobValue(ByteString.copyFromUtf8(value)).build()).build());

                builder.addAttribute(AttributeProto.Attribute.newBuilder()
                        .setName("Param_CharacterName")
                        .setValue(AttributeProto.Variant.newBuilder().setStringValue(playedCharacter.getCharacterName()).build()).build());


                builder.addAttribute(AttributeProto.Attribute.newBuilder()
                        .setName("Param_CharacterGUID")
                        .setValue(AttributeProto.Variant.newBuilder().setBlobValue(
                                ByteString.copyFrom(ByteBuffer.wrap(new byte[8]).putLong(playedCharacter.getCharacterGUID()))
                        ).build()).build());

                builder.addAttribute(AttributeProto.Attribute.newBuilder()
                        .setName("Param_LastPlayedTime")
                        .setValue(AttributeProto.Variant.newBuilder().setIntValue(playedCharacter.getLastPlayedTime()).build()).build());

                //attribute = response->add_attribute();
                //attribute->set_name("Param_CharacterGUID");
                //attribute->mutable_value()->set_blob_value(&lastPlayerChar->second.CharacterGUID, sizeof(lastPlayerChar->second.CharacterGUID));
                response = builder.build();
            }


        }
        done.run(response);
    }

    private RealmProto.RealmEntry getRealmEntry(int build, Realm realm) {
        RealmProto.RealmEntry entry = new RealmProto.RealmEntry();
        entry.setWowRealmAddress(realm.getId().getAddress());
        entry.setCfgTimezonesID(1);
        entry.setPopulationState(Math.max((int) realm.getPopulationLevel(), 1));
        entry.setCfgCategoriesID(realm.getTimezone());

        RealmProto.ClientVersion version = new RealmProto.ClientVersion();
        ClientBuild buildInfo = realmManager.getBuildInfo(build);

        if (buildInfo != null) {
            version.setVersionBuild(buildInfo.getBuild());
            version.setVersionMajor(buildInfo.getMajorVersion());
            version.setVersionMinor(buildInfo.getMinorVersion());
            version.setVersionRevision(buildInfo.getBugfixVersion());
        } else {
            version.setVersionBuild(realm.getBuild());
            version.setVersionMajor(9);
            version.setVersionMinor(0);
            version.setVersionRevision(2);
        }
        entry.setVersion(version);
        entry.setCfgRealmsID(realm.getId().getRealm());
        entry.setFlags(realm.getFlags());
        entry.setName(realm.getName());
        entry.setCfgConfigsID(Realm.CONFIG_ID_BY_TYPE[realm.getType()]);
        entry.setCfgLanguagesID(1);
        return entry;
    }

    private void processRealmListTicketRequest(DefaultRpcController controller,
                                               GameUtilitiesServiceProto.ClientRequest request,
                                               RpcCallback<GameUtilitiesServiceProto.ClientResponse> done) {
        Map<String, AttributeProto.Attribute> attributeMap = request.getAttributeList().stream()
                .collect(Collectors.toMap(AttributeProto.Attribute::getName, v -> v, (v1, v2) -> v1));
        AttributeProto.Attribute param_identity = attributeMap.get("Param_Identity");

        RpcSession rpcSession = controller.getSession();
        AccountInfo accountInfo = rpcSession.getAccountInfo();


        GameAccount identityAccount = null;
        if (param_identity != null) {
            String blobValue = param_identity.getValue().getBlobValue().toStringUtf8();
            RealmProto.RealmListTicketIdentity ticketIdentity = JsonUtil.fromAttributeJsonValue(blobValue, RealmProto.RealmListTicketIdentity.class);
            identityAccount = accountInfo.getGameAccount((long) ticketIdentity.getGameAccountID());
        }

        if (identityAccount == null) {
            controller.setFailed(RpcErrorCode.ERROR_UTIL_SERVER_INVALID_IDENTITY_ARGS);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        if (identityAccount.isBanded()) {
            controller.setFailed(RpcErrorCode.ERROR_GAME_ACCOUNT_BANNED);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }
        if (identityAccount.isPermanentlyBanned()) {
            controller.setFailed(RpcErrorCode.ERROR_GAME_ACCOUNT_SUSPENDED);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        boolean clientInfoOk = false;
        AttributeProto.Attribute clientInfoParam = attributeMap.get("Param_ClientInfo");
        if (clientInfoParam != null) {
            String blobValue = clientInfoParam.getValue().getBlobValue().toStringUtf8();
            RealmProto.RealmListTicketClientInformation data = JsonUtil.fromAttributeJsonValue(blobValue, RealmProto.RealmListTicketClientInformation.class);
            int[] secret = data.getInfo().getSecret();
            if (secret.length == SysProperties.PORTAL_CLIENT_SECRET_LENGTH) {
                clientInfoOk = true;
                byte[] clientSecret = new byte[SysProperties.PORTAL_CLIENT_SECRET_LENGTH];
                for (int i = 0; i < secret.length; i++) {
                    clientSecret[i] = (byte) secret[i];
                }
                rpcSession.setClientSecret(clientSecret);
                rpcSession.setIdentityAccount(identityAccount);
            }
        }

        if (!clientInfoOk) {
            controller.setFailed(RpcErrorCode.ERROR_WOW_SERVICES_DENIED_REALM_LIST_TICKET);
            done.run(GameUtilitiesServiceProto.ClientResponse.getDefaultInstance());
            return;
        }

        Optional<Account> accountRepoById = accountRepo.findById(identityAccount.getId());
        accountRepoById.ifPresent(account -> {
            account.setLocale((short)LocaleConstant.fromName(rpcSession.getLocale()).ordinal());
            account.setLastLogin(Instant.now());
            account.setLastIp(controller.remoteAddress().getHostName());
            account.setOs(rpcSession.getPlatform());
            accountRepo.save(account);
        });

        done.run(GameUtilitiesServiceProto.ClientResponse.newBuilder()
                .addAttribute(AttributeProto.Attribute.newBuilder()
                        .setName("Param_RealmListTicket")
                        .setValue(AttributeProto.Variant.newBuilder()
                                .setBlobValue(ByteString.copyFromUtf8("AuthRealmListTicket")).build()).build()).build());


    }

    private String getAddressForClient(Realm realm, InetAddress clientAddr) {

        InetAddress externalAddress = realm.getExternalAddress();
        InetAddress localAddress = realm.getLocalAddress();
        InetAddress localSubnetMask = realm.getLocalSubnetMask();
        InetAddress realmIp;

        // Attempt to send best address for client
        if (clientAddr.isLoopbackAddress()) {
            // Try guessing if realm is also connected locally
            if (localAddress.isLoopbackAddress() || externalAddress.isLoopbackAddress())
                realmIp = clientAddr;
            else {
                // Assume that user connecting from the machine that bnetserver is located on
                // has all realms available in his local network
                realmIp = localAddress;
            }
        } else if (clientAddr instanceof Inet4Address) {
            int localValue = getIpV4Value(localAddress.getAddress());
            int clientValue = getIpV4Value(clientAddr.getAddress());
            int mask = getIpV4Value(localSubnetMask.getAddress());
            realmIp = (mask & localValue) == (mask & clientValue) ? localAddress : externalAddress;
        } else {
            realmIp = externalAddress;
        }
        // Return external IP
        return realmIp.getHostName();
    }

    public static int getIpV4Value(byte[] addr) {
        int address = addr[3] & 0xFF;
        address |= ((addr[2] << 8) & 0xFF00);
        address |= ((addr[1] << 16) & 0xFF0000);
        address |= ((addr[0] << 24) & 0xFF000000);
        return address;
    }


}
