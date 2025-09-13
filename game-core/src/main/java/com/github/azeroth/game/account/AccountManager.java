package com.github.azeroth.game.account;


import Framework.Cryptography.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class AccountManager {
    private static final int MAXACCOUNTLENGTH = 16;
    private static final int MAXEMAILLENGTH = 64;

    private final HashMap<Integer, RBACPermission> permissions = new HashMap<Integer, RBACPermission>();
    private final MultiMap<Byte, Integer> defaultPermissions = new MultiMap<Byte, Integer>();

    private AccountManager() {
    }

    public HashMap<Integer, RBACPermission> getRBACPermissionList() {
        return permissions;
    }

    public AccountOpResult createAccount(String username, String password, String email, int bnetAccountId) {
        return createAccount(username, password, email, bnetAccountId, 0);
    }

    public AccountOpResult createAccount(String username, String password, String email) {
        return createAccount(username, password, email, 0, 0);
    }

    public AccountOpResult createAccount(String username, String password) {
        return createAccount(username, password, "", 0, 0);
    }

    public AccountOpResult createAccount(String username, String password, String email, int bnetAccountId, byte bnetIndex) {
        if (username.length() > MAXACCOUNTLENGTH) {
            return AccountOpResult.NameTooLong;
        }

        if (password.length() > MAXACCOUNTLENGTH) {
            return AccountOpResult.PassTooLong;
        }

        if (getId(username) != 0) {
            return AccountOpResult.NameAlreadyExist;
        }


        var(salt, verifier) = SRP6.MakeRegistrationData(username, password);

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_ACCOUNT);
        stmt.AddValue(0, username);
        stmt.AddValue(1, salt);
        stmt.AddValue(2, verifier);
        stmt.AddValue(3, email);
        stmt.AddValue(4, email);

        if (bnetAccountId != 0 && bnetIndex != 0) {
            stmt.AddValue(5, bnetAccountId);
            stmt.AddValue(6, bnetIndex);
        } else {
            stmt.AddNull(5);
            stmt.AddNull(6);
        }

        DB.Login.DirectExecute(stmt); // Enforce saving, otherwise AddGroup can fail

        stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_REALM_CHARACTERS_INIT);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }

    public AccountOpResult deleteAccount(int accountId) {
        // Check if accounts exists
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_BY_ID);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (result.isEmpty()) {
            return AccountOpResult.NameNotExist;
        }

        // Obtain accounts character
        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARS_BY_ACCOUNT_ID);
        stmt.AddValue(0, accountId);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            do {
                var guid = ObjectGuid.create(HighGuid.Player, result.<Long>Read(0));

                // Kick if player is online
                var p = global.getObjAccessor().findPlayer(guid);

                if (p) {
                    var s = p.getSession();
                    s.kickPlayer("AccountMgr::DeleteAccount Deleting the account"); // mark session to remove at next session list update
                    s.logoutPlayer(false); // logout player without waiting next session list update
                }

                player.deleteFromDB(guid, accountId, false); // no need to update realm character
            } while (result.NextRow());
        }

        // table realm specific but common for all character of account for realm
        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_TUTORIALS);
        stmt.AddValue(0, accountId);
        DB.characters.execute(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ACCOUNT_DATA);
        stmt.AddValue(0, accountId);
        DB.characters.execute(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_BAN);
        stmt.AddValue(0, accountId);
        DB.characters.execute(stmt);

        SQLTransaction trans = new SQLTransaction();

        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_ACCOUNT);
        stmt.AddValue(0, accountId);
        trans.append(stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_ACCOUNT_ACCESS);
        stmt.AddValue(0, accountId);
        trans.append(stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_REALM_CHARACTERS);
        stmt.AddValue(0, accountId);
        trans.append(stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_ACCOUNT_BANNED);
        stmt.AddValue(0, accountId);
        trans.append(stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_ACCOUNT_MUTED);
        stmt.AddValue(0, accountId);
        trans.append(stmt);

        DB.Login.CommitTransaction(trans);

        return AccountOpResult.Ok;
    }

    public AccountOpResult changeUsername(int accountId, String newUsername, String newPassword) {
        // Check if accounts exists
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_BY_ID);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (result.isEmpty()) {
            return AccountOpResult.NameNotExist;
        }

        if (newUsername.length() > MAXACCOUNTLENGTH) {
            return AccountOpResult.NameTooLong;
        }

        if (newPassword.length() > MAXACCOUNTLENGTH) {
            return AccountOpResult.PassTooLong;
        }

        stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_USERNAME);
        stmt.AddValue(0, newUsername);
        stmt.AddValue(1, accountId);
        DB.Login.execute(stmt);


        var(salt, verifier) = SRP6.MakeRegistrationData(newUsername, newPassword);
        stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_LOGON);
        stmt.AddValue(0, salt);
        stmt.AddValue(1, verifier);
        stmt.AddValue(2, accountId);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }

    public AccountOpResult changePassword(int accountId, String newPassword) {
        String username;
        tangible.OutObject<String> tempOut_username = new tangible.OutObject<String>();
        if (!getName(accountId, tempOut_username)) {
            username = tempOut_username.outArgValue;
            return AccountOpResult.NameNotExist; // account doesn't exist
        } else {
            username = tempOut_username.outArgValue;
        }

        if (newPassword.length() > MAXACCOUNTLENGTH) {
            return AccountOpResult.PassTooLong;
        }


        var(salt, verifier) = SRP6.MakeRegistrationData(username, newPassword);

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_LOGON);
        stmt.AddValue(0, salt);
        stmt.AddValue(1, verifier);
        stmt.AddValue(2, accountId);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }

    public AccountOpResult changeEmail(int accountId, String newEmail) {
        tangible.OutObject<String> tempOut__ = new tangible.OutObject<String>();
        if (!getName(accountId, tempOut__)) {
            _ = tempOut__.outArgValue;
            return AccountOpResult.NameNotExist; // account doesn't exist
        } else {
            _ = tempOut__.outArgValue;
        }

        if (newEmail.length() > MAXEMAILLENGTH) {
            return AccountOpResult.EmailTooLong;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_EMAIL);
        stmt.AddValue(0, newEmail);
        stmt.AddValue(1, accountId);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }

    public AccountOpResult changeRegEmail(int accountId, String newEmail) {
        tangible.OutObject<String> tempOut__ = new tangible.OutObject<String>();
        if (!getName(accountId, tempOut__)) {
            _ = tempOut__.outArgValue;
            return AccountOpResult.NameNotExist; // account doesn't exist
        } else {
            _ = tempOut__.outArgValue;
        }

        if (newEmail.length() > MAXEMAILLENGTH) {
            return AccountOpResult.EmailTooLong;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_REG_EMAIL);
        stmt.AddValue(0, newEmail);
        stmt.AddValue(1, accountId);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }

    public int getId(String username) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.GET_ACCOUNT_ID_BY_USERNAME);
        stmt.AddValue(0, username);
        var result = DB.Login.query(stmt);

        return !result.isEmpty() ? result.<Integer>Read(0) : 0;
    }

    public AccountTypes getSecurity(int accountId, int realmId) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.GET_GMLEVEL_BY_REALMID);
        stmt.AddValue(0, accountId);
        stmt.AddValue(1, realmId);
        var result = DB.Login.query(stmt);

        return !result.isEmpty() ? AccountTypes.forValue(result.<Integer>Read(0)) : AccountTypes.player;
    }

    public QueryCallback getSecurityAsync(int accountId, int realmId, tangible.Action1Param<Integer> callback) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.GET_GMLEVEL_BY_REALMID);
        stmt.AddValue(0, accountId);
        stmt.AddValue(1, realmId);

        return DB.Login.AsyncQuery(stmt).WithCallback(result ->
        {
            callback.invoke(!result.isEmpty() ? result.<Byte>Read(0) : (int) AccountTypes.player.getValue());
        });
    }

    public boolean getName(int accountId, tangible.OutObject<String> name) {
        name.outArgValue = "";
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.GET_USERNAME_BY_ID);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            name.outArgValue = result.<String>Read(0);

            return true;
        }

        return false;
    }

    public boolean getEmail(int accountId, tangible.OutObject<String> email) {
        email.outArgValue = "";
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.GET_EMAIL_BY_ID);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            email.outArgValue = result.<String>Read(0);

            return true;
        }

        return false;
    }

    public boolean checkPassword(int accountId, String password) {
        String username;
        tangible.OutObject<String> tempOut_username = new tangible.OutObject<String>();
        if (!getName(accountId, tempOut_username)) {
            username = tempOut_username.outArgValue;
            return false;
        } else {
            username = tempOut_username.outArgValue;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_CHECK_PASSWORD);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            var salt = result.<byte[]>Read(0);
            var verifier = result.<byte[]>Read(1);

            if (SRP6.CheckLogin(username, password, salt, verifier)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkEmail(int accountId, String newEmail) {
        // We simply return false for a non-existing email
        String oldEmail;
        tangible.OutObject<String> tempOut_oldEmail = new tangible.OutObject<String>();
        if (!getEmail(accountId, tempOut_oldEmail)) {
            oldEmail = tempOut_oldEmail.outArgValue;
            return false;
        } else {
            oldEmail = tempOut_oldEmail.outArgValue;
        }

        if (Objects.equals(oldEmail, newEmail)) {
            return true;
        }

        return false;
    }

    public int getCharactersCount(int accountId) {
        // check character count
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_SUM_CHARS);
        stmt.AddValue(0, accountId);
        var result = DB.characters.query(stmt);

        return result.isEmpty() ? 0 : (int) result.<Long>Read(0);
    }

    public boolean isBannedAccount(String name) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_BANNED_BY_USERNAME);
        stmt.AddValue(0, name);
        var result = DB.Login.query(stmt);

        return !result.isEmpty();
    }

    public boolean isPlayerAccount(AccountTypes gmlevel) {
        return gmlevel == AccountTypes.player;
    }

    public boolean isAdminAccount(AccountTypes gmlevel) {
        return gmlevel.getValue() >= AccountTypes.Administrator.getValue() && gmlevel.getValue() <= AccountTypes.Console.getValue();
    }

    public boolean isConsoleAccount(AccountTypes gmlevel) {
        return gmlevel == AccountTypes.Console;
    }

    public void loadRBAC() {
        permissions.clear();
        defaultPermissions.clear();

        Log.outDebug(LogFilter.Rbac, "AccountMgr:LoadRBAC");
        var oldMSTime = System.currentTimeMillis();
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;

        Log.outDebug(LogFilter.Rbac, "AccountMgr:LoadRBAC: Loading permissions");
        var result = DB.Login.query("SELECT id, name FROM rbac_permissions");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 account permission definitions. DB table `rbac_permissions` is empty.");

            return;
        }

        do {
            var id = result.<Integer>Read(0);
            permissions.put(id, new RBACPermission(id, result.<String>Read(1)));
            ++count1;
        } while (result.NextRow());

        Log.outDebug(LogFilter.Rbac, "AccountMgr:LoadRBAC: Loading linked permissions");
        result = DB.Login.query("SELECT id, linkedId FROM rbac_linked_permissions ORDER BY id ASC");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 linked permissions. DB table `rbac_linked_permissions` is empty.");

            return;
        }

        int permissionId = 0;
        RBACPermission permission = null;

        do {
            var newId = result.<Integer>Read(0);

            if (permissionId != newId) {
                permissionId = newId;
                permission = permissions.get(newId);
            }

            var linkedPermissionId = result.<Integer>Read(1);

            if (linkedPermissionId == permissionId) {
                Logs.SQL.error("RBAC Permission {0} has itself as linked permission. Ignored", permissionId);

                continue;
            }

            permission.addLinkedPermission(linkedPermissionId);
            ++count2;
        } while (result.NextRow());

        Log.outDebug(LogFilter.Rbac, "AccountMgr:LoadRBAC: Loading default permissions");
        result = DB.Login.query("SELECT secId, permissionId FROM rbac_default_permissions ORDER BY secId ASC");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 default permission definitions. DB table `rbac_default_permissions` is empty.");

            return;
        }

        int secId = 255;

        do {
            var newId = result.<Integer>Read(0);

            if (secId != newId) {
                secId = newId;
            }

            defaultPermissions.add((byte) secId, result.<Integer>Read(1));
            ++count3;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} permission definitions, {1} linked permissions and {2} default permissions in {3} ms", count1, count2, count3, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void updateAccountAccess(RBACData rbac, int accountId, byte securityLevel, int realmId) {
        if (rbac != null && securityLevel != rbac.getSecurityLevel()) {
            rbac.setSecurityLevel(securityLevel);
        }

        PreparedStatement stmt;
        SQLTransaction trans = new SQLTransaction();

        // Delete old security level from DB
        if (realmId == -1) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_ACCOUNT_ACCESS);
            stmt.AddValue(0, accountId);
            trans.append(stmt);
        } else {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_ACCOUNT_ACCESS_BY_REALM);
            stmt.AddValue(0, accountId);
            stmt.AddValue(1, realmId);
            trans.append(stmt);
        }

        // Add new security level
        if (securityLevel != 0) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_ACCOUNT_ACCESS);
            stmt.AddValue(0, accountId);
            stmt.AddValue(1, securityLevel);
            stmt.AddValue(2, realmId);
            trans.append(stmt);
        }

        DB.Login.CommitTransaction(trans);
    }

    public RBACPermission getRBACPermission(int permissionId) {
        Log.outDebug(LogFilter.Rbac, "AccountMgr:GetRBACPermission: {0}", permissionId);

        return permissions.get(permissionId);
    }

    public boolean hasPermission(int accountId, RBACPermissions permissionId, int realmId) {
        if (accountId == 0) {
            Log.outError(LogFilter.Rbac, "AccountMgr:HasPermission: Wrong accountId 0");

            return false;
        }

        RBACData rbac = new RBACData(accountId, "", (int) realmId, (byte) getSecurity(accountId, (int) realmId).getValue());
        rbac.loadFromDB();
        var hasPermission = rbac.hasPermission(permissionId);

        Log.outDebug(LogFilter.Rbac, "AccountMgr:HasPermission [AccountId: {0}, PermissionId: {1}, realmId: {2}]: {3}", accountId, permissionId, realmId, hasPermission);

        return hasPermission;
    }

    public ArrayList<Integer> getRBACDefaultPermissions(byte secLevel) {
        return defaultPermissions.get(secLevel);
    }
}
