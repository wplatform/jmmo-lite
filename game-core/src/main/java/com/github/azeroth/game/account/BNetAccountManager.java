package com.github.azeroth.game.account;

public final class BNetAccountManager {
    private BNetAccountManager() {
    }

    public AccountOpResult createBattlenetAccount(String email, String password, boolean withGameAccount, tangible.OutObject<String> gameAccountName) {
        gameAccountName.outArgValue = "";

        if (email.isEmpty() || email.length() > 320) {
            return AccountOpResult.NameTooLong;
        }

        if (password.isEmpty() || password.length() > 16) {
            return AccountOpResult.PassTooLong;
        }

        if (getId(email) != 0) {
            return AccountOpResult.NameAlreadyExist;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BNET_ACCOUNT);
        stmt.AddValue(0, email);
        stmt.AddValue(1, calculateShaPassHash(email.toUpperCase(), password.toUpperCase()));
        DB.Login.DirectExecute(stmt);

        var newAccountId = getId(email);

        if (withGameAccount) {
            gameAccountName.outArgValue = newAccountId + "#1";
            global.getAccountMgr().createAccount(gameAccountName.outArgValue, password, email, newAccountId, (byte) 1);
        }

        return AccountOpResult.Ok;
    }


    public AccountOpResult changePassword(int accountId, String newPassword) {
        String username;
        tangible.OutObject<String> tempOut_username = new tangible.OutObject<String>();
        if (!getName(accountId, tempOut_username)) {
            username = tempOut_username.outArgValue;
            return AccountOpResult.NameNotExist;
        } else {
            username = tempOut_username.outArgValue;
        }

        if (newPassword.length() > 16) {
            return AccountOpResult.PassTooLong;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_BNET_PASSWORD);
        stmt.AddValue(0, calculateShaPassHash(username.toUpperCase(), newPassword.toUpperCase()));
        stmt.AddValue(1, accountId);
        DB.Login.DirectExecute(stmt);

        return AccountOpResult.Ok;
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

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_CHECK_PASSWORD);
        stmt.AddValue(0, accountId);
        stmt.AddValue(1, calculateShaPassHash(username.toUpperCase(), password.toUpperCase()));

        return !DB.Login.query(stmt).isEmpty();
    }

    public AccountOpResult linkWithGameAccount(String email, String gameAccountName) {
        var bnetAccountId = getId(email);

        if (bnetAccountId == 0) {
            return AccountOpResult.NameNotExist;
        }

        var gameAccountId = global.getAccountMgr().getId(gameAccountName);

        if (gameAccountId == 0) {
            return AccountOpResult.NameNotExist;
        }

        if (getIdByGameAccount(gameAccountId) != 0) {
            return AccountOpResult.BadLink;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_BNET_GAME_ACCOUNT_LINK);
        stmt.AddValue(0, bnetAccountId);
        stmt.AddValue(1, getMaxIndex(bnetAccountId) + 1);
        stmt.AddValue(2, gameAccountId);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }

    public AccountOpResult unlinkGameAccount(String gameAccountName) {
        var gameAccountId = global.getAccountMgr().getId(gameAccountName);

        if (gameAccountId == 0) {
            return AccountOpResult.NameNotExist;
        }

        if (getIdByGameAccount(gameAccountId) == 0) {
            return AccountOpResult.BadLink;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_BNET_GAME_ACCOUNT_LINK);
        stmt.AddNull(0);
        stmt.AddNull(1);
        stmt.AddValue(2, gameAccountId);
        DB.Login.execute(stmt);

        return AccountOpResult.Ok;
    }


    public int getId(String username) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_ACCOUNT_ID_BY_EMAIL);
        stmt.AddValue(0, username);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            return result.<Integer>Read(0);
        }

        return 0;
    }


    public boolean getName(int accountId, tangible.OutObject<String> name) {
        name.outArgValue = "";
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_ACCOUNT_EMAIL_BY_ID);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            name.outArgValue = result.<String>Read(0);

            return true;
        }

        return false;
    }


    public int getIdByGameAccount(int gameAccountId) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_ACCOUNT_ID_BY_GAME_ACCOUNT);
        stmt.AddValue(0, gameAccountId);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            return result.<Integer>Read(0);
        }

        return 0;
    }


    public QueryCallback getIdByGameAccountAsync(int gameAccountId) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_ACCOUNT_ID_BY_GAME_ACCOUNT);
        stmt.AddValue(0, gameAccountId);

        return DB.Login.AsyncQuery(stmt);
    }


    public byte getMaxIndex(int accountId) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_MAX_ACCOUNT_INDEX);
        stmt.AddValue(0, accountId);
        var result = DB.Login.query(stmt);

        if (!result.isEmpty()) {
            return result.<Byte>Read(0);
        }

        return 0;
    }

    public String calculateShaPassHash(String name, String password) {
        var sha256 = SHA256.create();
        var i = sha256.ComputeHash(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return sha256.ComputeHash(Encoding.UTF8.GetBytes(i.ToHexString() + ":" + password)).ToHexString(true);
    }
}
