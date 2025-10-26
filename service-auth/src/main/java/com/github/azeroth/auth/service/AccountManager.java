package com.github.azeroth.auth.service;


import com.github.azeroth.auth.domain.Account;
import com.github.azeroth.auth.domain.RbacDefaultPermissions;
import com.github.azeroth.auth.domain.RbacLinkedPermissions;
import com.github.azeroth.auth.dto.AccountOpResult;
import com.github.azeroth.auth.dto.AccountType;
import com.github.azeroth.auth.dto.RBACPermission;
import com.github.azeroth.auth.dto.RBACPermissions;
import com.github.azeroth.auth.repository.AccountRepository;
import com.github.azeroth.common.Functions;
import com.github.azeroth.common.Logs;
import com.github.azeroth.crypto.GruntSRP6;
import com.github.azeroth.utils.StringUtil;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;

public final class AccountManager implements AccountService {
    private static final int MAX_ACCOUNT_LENGTH = 16;
    private static final int MAX_EMAIL_LENGTH = 64;

    private final HashMap<Integer, RBACPermission> permissions = new HashMap<Integer, RBACPermission>();
    private final HashMap<Byte, Map<Integer, RBACPermission>> defaultPermissions = new HashMap<>();

    private AccountRepository accountRepo;

    private AccountManager() {
    }

    public HashMap<Integer, RBACPermission> getRBACPermissionList() {
        return permissions;
    }

    public AccountOpResult createAccount(String username, String password, String email, int bnetAccountId) {
        return createAccount(username, password, email, bnetAccountId, (byte) 0);
    }

    public AccountOpResult createAccount(String username, String password, String email) {
        return createAccount(username, password, email, 0, (byte) 0);
    }

    public AccountOpResult createAccount(String username, String password) {
        return createAccount(username, password, "", 0, (byte) 0);
    }

    public AccountOpResult createAccount(String username, String password, String email, int bNetAccountId, byte bnetIndex) {
        if (username.length() > MAX_ACCOUNT_LENGTH) {
            return AccountOpResult.NameTooLong;
        }

        if (password.length() > MAX_ACCOUNT_LENGTH) {
            return AccountOpResult.PassTooLong;
        }

        if (getId(username) != 0) {
            return AccountOpResult.NameAlreadyExist;
        }

        var regData = GruntSRP6.makeRegistrationData(username, password);
        Account account = new Account();
        account.setUserName(username);
        account.setSalt(regData.salt());
        account.setVerifier(regData.verifier());
        account.setEmail(email);
        account.setRegMail(email);

        if (bNetAccountId != 0 && bnetIndex != 0) {
            account.setBattleNetAccountId(bNetAccountId);
            account.setBattleNetIndex(bnetIndex);
        }
        accountRepo.save(account);
        return AccountOpResult.Ok;
    }

    @Transactional
    public AccountOpResult deleteAccount(int accountId) {

        if (!accountRepo.existsById(accountId)) {
            return AccountOpResult.NameNotExist;
        }

        accountRepo.deleteById(accountId);
        accountRepo.deleteAccountAccess(accountId);
        accountRepo.deleteRealmCharacters(accountId);
        accountRepo.deleteAccountBanned(accountId);
        accountRepo.deleteAccountBanned(accountId);
        return AccountOpResult.Ok;
    }

    public AccountOpResult changeUsername(int accountId, String newUsername, String newPassword) {
        // Check if accounts exists
        if (!accountRepo.existsById(accountId)) {
            return AccountOpResult.NameNotExist;
        }

        if (newUsername.length() > MAX_ACCOUNT_LENGTH) {
            return AccountOpResult.NameTooLong;
        }

        if (newPassword.length() > MAX_ACCOUNT_LENGTH) {
            return AccountOpResult.PassTooLong;
        }

        accountRepo.updateAccountUsername(newUsername, accountId);

        var regData = GruntSRP6.makeRegistrationData(newUsername, newPassword);

        accountRepo.updateAccountLogonInfo(regData.salt(), regData.verifier(), accountId);

        return AccountOpResult.Ok;
    }

    public AccountOpResult changePassword(int accountId, String newPassword) {
        if (newPassword.length() > MAX_ACCOUNT_LENGTH) {
            return AccountOpResult.PassTooLong;
        }
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            return AccountOpResult.NameNotExist; // account doesn't exist
        }
        var account = accountOpt.get();
        var regData = GruntSRP6.makeRegistrationData(account.getUserName(), newPassword);
        accountRepo.updateAccountLogonInfo(regData.salt(), regData.verifier(), accountId);
        return AccountOpResult.Ok;
    }

    public AccountOpResult changeEmail(int accountId, String newEmail) {

        if (newEmail.length() > MAX_EMAIL_LENGTH) {
            return AccountOpResult.EmailTooLong;
        }
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            return AccountOpResult.NameNotExist; // account doesn't exist
        }
        accountRepo.updateAccountEmail(newEmail, accountId);

        return AccountOpResult.Ok;
    }

    public AccountOpResult changeRegEmail(int accountId, String newEmail) {

        if (newEmail.length() > MAX_EMAIL_LENGTH) {
            return AccountOpResult.EmailTooLong;
        }
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            return AccountOpResult.NameNotExist; // account doesn't exist
        }
        accountRepo.updateAccountRegEmail(newEmail, accountId);

        return AccountOpResult.Ok;
    }

    public int getId(String username) {
        return accountRepo.getAccountIdByUsername(username);
    }

    public AccountType getSecurity(int accountId, int realmId) {
        var gmLevel = accountRepo.getGmLevelByRealmId(accountId, realmId);
        return AccountType.values()[gmLevel];
    }


    public boolean checkPassword(int accountId, String password) {

        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            return false;
        }
        var account = accountOpt.get();
        var salt = account.getSalt();
        var verifier = account.getVerifier();
        var username = account.getUserName();

        return new GruntSRP6(username, salt, new BigInteger(verifier)).checkCredentials(username, password);
    }

    public boolean checkEmail(int accountId, String newEmail) {
        // We simply return false for a non-existing email
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            return false;
        }
        var account = accountOpt.get();
        var oldEmail = account.getEmail();

        return StringUtil.equalsIgnoreCase(oldEmail, newEmail);
    }

    public int getCharactersCount(int accountId) {
        // check character count
        return accountRepo.selectSumRealmCharacters(accountId);
    }

    public boolean isBannedAccount(String name) {
        var result = accountRepo.selectBannedAccountByUsername(name);
        return !result.isEmpty();
    }

    public boolean isPlayerAccount(AccountType gmlevel) {
        return gmlevel == AccountType.SEC_PLAYER;
    }

    public boolean isAdminAccount(AccountType gmlevel) {
        return gmlevel.compareTo(AccountType.SEC_ADMINISTRATOR) >= 0 && gmlevel.compareTo(AccountType.SEC_CONSOLE) <= 0;
    }

    public boolean isConsoleAccount(AccountType gmlevel) {
        return gmlevel == AccountType.SEC_CONSOLE;
    }

    public void loadRBAC(int currentRealmId) {
        permissions.clear();
        defaultPermissions.clear();

        Logs.RBAC.debug("AccountMgr:LoadRBAC");

        var oldMSTime = System.currentTimeMillis();
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;

        Logs.RBAC.debug("AccountMgr:LoadRBAC: Loading permissions");

        var result = accountRepo.findAllRbacPermissions();

        if (result.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 account permission definitions. DB table `rbac_permissions` is empty.");

            return;
        }

        for (var permission : result) {
            permissions.put(permission.getId(), new RBACPermission(permission.getId(), permission.getName()));
            ++count1;
        }

        Logs.RBAC.debug("AccountMgr:LoadRBAC: Loading linked permissions");

        var linkedPermissions = accountRepo.findAllRbacLinkedPermissions();

        if (linkedPermissions.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 linked permissions. DB table `rbac_linked_permissions` is empty.");

            return;
        }

        int permissionId = 0;
        RBACPermission permission = null;
        for (RbacLinkedPermissions linkedPermission : linkedPermissions) {

            var newId = linkedPermission.getId();
            if (permissionId != newId) {
                permissionId = newId;
                permission = permissions.get(newId);
            }

            var linkedPermissionId = linkedPermission.getLinkedId();
            if (linkedPermissionId == permissionId) {
                Logs.SQL.error("RBAC Permission {} has itself as linked permission. Ignored", permissionId);
                continue;
            }

            Objects.requireNonNull(permission).addLinkedPermission(linkedPermissionId);
            ++count2;

        }

        Logs.RBAC.debug("AccountMgr::LoadRBAC: Loading default permissions");

        var defaultPermissionsList = accountRepo.queryDefaultPermissionsByRealmId(currentRealmId);

        if(defaultPermissionsList.isEmpty()) {
            Logs.SERVER_LOADING.info(">> Loaded 0 default permission definitions. DB table `rbac_default_permissions` is empty.");
            return;
        }
        int secId = 255;
        Map<Integer, RBACPermission> permissions = null;
        for (RbacDefaultPermissions rbacDefaultPermissions : defaultPermissionsList) {
            int newId = rbacDefaultPermissions.getSecId();
            if (secId != newId || permissions == null)
            {
                secId = newId;
                permissions.put(secId, Functions.addToMap(new RBACPermission(rbacDefaultPermissions.getPermissionId())));
                ++count3;
            }
        }


        Logs.SERVER_LOADING.info(">> Loaded {} permission definitions, {} linked permissions and {} default permissions in {} ms", count1, count2, count3, System.currentTimeMillis() - oldMSTime);



    }

    public void updateAccountAccess(int accountId, byte securityLevel, int realmId) {
        // Delete old security level from DB
        if (realmId == -1) {
            accountRepo.deleteAccountAccess(accountId);
        } else {
            accountRepo.deleteAccountAccessByRealm(accountId, realmId);
        }

        // Add new security level
        if (securityLevel != 0) {
            accountRepo.insertAccountAccess(accountId, securityLevel, realmId);
        }
    }

    public RBACPermission getRBACPermission(int permissionId) {
        Logs.RBAC.trace("AccountMgr::GetRBACPermission: {}", permissionId);
        return permissions.get(permissionId);
    }

    public boolean hasPermission(int accountId, RBACPermissions permissionId, int realmId) {
        if (accountId == 0) {
            Logs.RBAC.error("AccountMgr::HasPermission: Wrong accountId 0");

            return false;
        }

        return true;
    }

    public ArrayList<Integer> getRBACDefaultPermissions(byte secLevel) {
        return defaultPermissions.get(secLevel);
    }
}
