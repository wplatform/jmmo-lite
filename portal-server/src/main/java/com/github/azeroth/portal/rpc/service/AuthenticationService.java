package com.github.azeroth.portal.rpc.service;

import bgs.protocol.authentication.v1.AuthenticationServiceProto;
import bgs.protocol.challenge.v1.ChallengeServiceProto;
import bnet.protocol.EntityProto;
import bnet.protocol.RpcProto;
import com.github.azeroth.service.auth.domain.AccountBanned;
import com.github.azeroth.service.auth.domain.BattlenetAccount;
import com.github.azeroth.service.auth.domain.BattlenetAccountBan;
import com.github.azeroth.utils.RandomUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.github.azeroth.service.auth.dto.AccountInfo;
import com.github.azeroth.service.auth.dto.GameAccount;
import com.github.azeroth.service.auth.repository.AccountBannedRepository;
import com.github.azeroth.service.auth.repository.BattlenetAccountBanRepository;
import com.github.azeroth.service.auth.repository.BattlenetAccountRepository;
import com.github.azeroth.common.RpcErrorCode;
import com.github.azeroth.portal.boot.LoginRestProperties;
import com.github.azeroth.portal.realm.ClientBuild;
import com.github.azeroth.portal.rpc.DefaultRpcChannel;
import com.github.azeroth.portal.rpc.DefaultRpcController;
import com.github.azeroth.portal.rpc.RpcSession;
import com.github.azeroth.portal.utils.LocaleConstant;
import com.github.azeroth.utils.SysProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AuthenticationService implements AuthenticationServiceProto.AuthenticationService.Interface {


    private final AuthenticationServiceProto.AuthenticationListener.Interface authenticationListener = AuthenticationServiceProto.AuthenticationListener.newStub(new DefaultRpcChannel());
    private final ChallengeServiceProto.ChallengeListener.Interface challengeListener = ChallengeServiceProto.ChallengeListener.newStub(new DefaultRpcChannel());

    private final BattlenetAccountRepository battlenetAccountRepo;
    private final BattlenetAccountBanRepository battlenetAccountBanRepo;
    private final AccountBannedRepository accountBannedRepo;
    private final LoginRestProperties loginRestProperties;


    @Override
    public void logon(RpcController controller, AuthenticationServiceProto.LogonRequest request, RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController statusController = (DefaultRpcController) controller;
        if (!SysProperties.PORTAL_WOW_PROGRAM_NAME.equals(request.getProgram())) {
            log.info("Attempted to log in with game other than WoW (using {})!", request.getProgram());
            statusController.setFailed(RpcErrorCode.ERROR_BAD_PROGRAM);
            done.run(RpcProto.NoData.getDefaultInstance());
            return;
        }


        if (!ClientBuild.PLATFORM.contains(request.getPlatform()))
        {
            log.info("Attempted to log in from an unsupported platform (using {})!", request.getPlatform());
            statusController.setFailed(RpcErrorCode.ERROR_BAD_PROGRAM);
            done.run(RpcProto.NoData.getDefaultInstance());
            return;
        }

        if (!LocaleConstant.isValidLocale(request.getLocale())) {
            log.info("Attempted to log in with unsupported locale (using {})!", request.getLocale());
            statusController.setFailed(RpcErrorCode.ERROR_BAD_LOCALE);
            done.run(RpcProto.NoData.getDefaultInstance());
            return;
        }

        if (!SysProperties.PORTAL_SUPPORTED_PLATFORM_LIST.contains(request.getPlatform())) {
            log.info("attempted to log in from an unsupported platform (using {})!", request.getPlatform());
            statusController.setFailed(RpcErrorCode.ERROR_BAD_PLATFORM);
            done.run(RpcProto.NoData.getDefaultInstance());
            return;
        }

        RpcSession rpcSession = statusController.getSession();
        rpcSession.setLocale(request.getLocale());
        rpcSession.setPlatform(request.getPlatform());
        rpcSession.setBuild(request.getApplicationVersion());

        if (request.hasCachedWebCredentials()) {
            AuthenticationServiceProto.VerifyWebCredentialsRequest parameter = AuthenticationServiceProto.VerifyWebCredentialsRequest.newBuilder().setWebCredentials(request.getCachedWebCredentials()).build();
            this.verifyWebCredentials(controller, parameter, done);
        } else {
            String webAuthUrl = "https://%s:%d/bnetserver/login/".formatted(loginRestProperties.getExternalAddress(), loginRestProperties.getLoginResTPort());
            ChallengeServiceProto.ChallengeExternalRequest challengeExternalRequest = ChallengeServiceProto.ChallengeExternalRequest.newBuilder().setPayloadType("web_auth_url").setPayload(ByteString.copyFromUtf8(webAuthUrl)).build();
            challengeListener.onExternalChallenge(statusController, challengeExternalRequest, null);
            done.run(RpcProto.NoData.getDefaultInstance());
        }
    }


    @Override
    public void moduleNotify(RpcController controller, AuthenticationServiceProto.ModuleNotification request, RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NoData.getDefaultInstance());
    }

    @Override
    public void moduleMessage(RpcController controller, AuthenticationServiceProto.ModuleMessageRequest request, RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NoData.getDefaultInstance());
    }

    @Override
    public void selectGameAccountDEPRECATED(RpcController controller, EntityProto.EntityId request, RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NoData.getDefaultInstance());
    }

    @Override
    public void generateSSOToken(RpcController controller, AuthenticationServiceProto.GenerateSSOTokenRequest request, RpcCallback<AuthenticationServiceProto.GenerateSSOTokenResponse> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(AuthenticationServiceProto.GenerateSSOTokenResponse.getDefaultInstance());
    }

    @Override
    public void selectGameAccount(RpcController controller, AuthenticationServiceProto.SelectGameAccountRequest request, RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(RpcProto.NoData.getDefaultInstance());
    }

    @Override
    public void verifyWebCredentials(RpcController controller, AuthenticationServiceProto.VerifyWebCredentialsRequest request, RpcCallback<RpcProto.NoData> done) {
        DefaultRpcController statusController = (DefaultRpcController) controller;
        if (request.getWebCredentials().isEmpty()) {
            statusController.setFailed(RpcErrorCode.ERROR_DENIED);
            done.run(RpcProto.NoData.newBuilder().build());
            return;
        }
        String loginTicket = request.getWebCredentials().toStringUtf8();

        Optional<BattlenetAccount> accountByTicket = battlenetAccountRepo.queryByLoginTicket(loginTicket);

        if (accountByTicket.isEmpty()) {
            statusController.setFailed(RpcErrorCode.ERROR_DENIED);
            done.run(RpcProto.NoData.newBuilder().build());
            return;
        }
        BattlenetAccount account = accountByTicket.get();
        AccountInfo accountInfo = new AccountInfo(account);

        if (System.currentTimeMillis() - account.getLoginTicketExpiry() < 0) {
            statusController.setFailed(RpcErrorCode.ERROR_TIMED_OUT);
            done.run(RpcProto.NoData.newBuilder().build());
            return;
        }

        // If the IP is 'locked', check that the player comes indeed from the correct IP address
        if (!Objects.equals(account.getLocked(), 0)) {
            log.debug("[Session::HandleVerifyWebCredentials] Account '{}' is locked to IP - '{}' is logging in from '{}'", account.getEmail(), account.getLastIp(), statusController.remoteAddress().getHostName());
            statusController.setFailed(RpcErrorCode.ERROR_RISK_ACCOUNT_LOCKED);
            done.run(RpcProto.NoData.newBuilder().build());
            return;
        }

        List<BattlenetAccountBan> accountBans = battlenetAccountBanRepo.findByAccount(account.getId());
        for (BattlenetAccountBan accountBan : accountBans) {
            boolean banded = accountBan.getId().getBandate() > System.currentTimeMillis();
            boolean permanentlyBanned =  Objects.equals(accountBan.getUnbandate(), accountBan.getId().getBandate());
            accountInfo.setBanded(banded);
            accountInfo.setPermanentlyBanned(permanentlyBanned);
            if (permanentlyBanned) {
                log.info("[Session::HandleVerifyWebCredentials] Banned account {} tried to login!", account.getEmail());
                statusController.setFailed(RpcErrorCode.ERROR_GAME_ACCOUNT_BANNED);
                done.run(RpcProto.NoData.newBuilder().build());
                return;
            } else if (banded) {
                log.info("[Session::HandleVerifyWebCredentials] Temporarily banned account {} tried to login!", account.getEmail());
                statusController.setFailed(RpcErrorCode.ERROR_GAME_ACCOUNT_BANNED);
                done.run(RpcProto.NoData.newBuilder().build());
                return;
            }
        }

        Map<Long, GameAccount> accounts = accountInfo.getAccounts();
        accounts.forEach((key, value)-> {
            List<AccountBanned> accountBanneds = accountBannedRepo.findByAccountId(key);
            for (AccountBanned accountBan : accountBanneds) {
                boolean banded = accountBan.getId().getBandate() > System.currentTimeMillis();
                boolean permanentlyBanned =  Objects.equals(accountBan.getUnbandate(), accountBan.getId().getBandate());
                value.setBanded(banded);
                value.setPermanentlyBanned(permanentlyBanned);
                value.setUnbandate(accountBan.getUnbandate());
            }
        });

        done.run(RpcProto.NoData.getDefaultInstance());
        AuthenticationServiceProto.LogonResult.Builder resultBuilder = AuthenticationServiceProto.LogonResult.newBuilder()
                .setErrorCode(RpcErrorCode.STATUS_OK)
                .setAccountId(EntityProto.EntityId.newBuilder().setLow(account.getId()).setHigh(0x100000000000000L).build())
                .setSessionKey(ByteString.copyFrom(RandomUtil.randomBytes((SysProperties.PORTAL_SESSION_KEY_LENGTH))));

        account.getAccounts().forEach(gameAccount -> resultBuilder.addGameAccountId(EntityProto.EntityId.newBuilder()
                .setLow(gameAccount.getId()).setHigh(0x200000200576F57L).build()));

        if (StringUtils.hasText(account.getLockCountry())) {
            resultBuilder.setGeoipCountry(account.getLockCountry());
        }

        RpcSession rpcSession = statusController.getSession();
        rpcSession.setAuthorized(true);
        rpcSession.setAccountInfo(accountInfo);


        authenticationListener.onLogonComplete(statusController, resultBuilder.build(), response -> {
        });
    }


    @Override
    public void generateWebCredentials(RpcController controller, AuthenticationServiceProto.GenerateWebCredentialsRequest request, RpcCallback<AuthenticationServiceProto.GenerateWebCredentialsResponse> done) {
        DefaultRpcController nettyRpcController = (DefaultRpcController) controller;
        nettyRpcController.setFailed(RpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED);
        done.run(AuthenticationServiceProto.GenerateWebCredentialsResponse.getDefaultInstance());
    }



}


