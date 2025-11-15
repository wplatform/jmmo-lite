package com.github.azeroth.auth.service;

import com.github.azeroth.auth.dto.AccountInfo;
import com.github.azeroth.auth.dto.AccountOpResult;

import java.util.Map;
import java.util.Optional;

public interface AccountService {

    default AccountOpResult createAccount(String username, String password, String email, int bnetAccountId) {
        return createAccount(username, password, email, bnetAccountId, (byte) 0);
    }

    default AccountOpResult createAccount(String username, String password, String email) {
        return createAccount(username, password, email, 0, (byte) 0);
    }

    default AccountOpResult createAccount(String username, String password) {
        return createAccount(username, password, "", 0, (byte) 0);
    }

    AccountOpResult createAccount(String username, String password, String email, int bNetAccountId, byte bnetIndex);


    Optional<AccountInfo> selectAccountInfoByUserName(String userName, int realmId);

    void updateAccountLastAttemptIp(String lastAttemptIp, String username);

    void updateAccountContinuedSession(byte[] sessionKey, int accountId);

    void updateAccountMuteTimeLogin(long mutetime, int id);

    void updateAccountLastIp(String last_ip, String username);

    Optional<Map<String, Object>> selectAccountContinuedSession(int accountId);

}
