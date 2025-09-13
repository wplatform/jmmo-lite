package com.github.azeroth.game.server;


class AccountInfoQueryHolderPerRealm extends SQLQueryHolder<AccountInfoQueryLoad> {
    public final void initialize(int accountId, int battlenetAccountId) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ACCOUNT_DATA);
        stmt.AddValue(0, accountId);
        SetQuery(AccountInfoQueryLoad.GlobalAccountDataIndexPerRealm, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_TUTORIALS);
        stmt.AddValue(0, accountId);
        SetQuery(AccountInfoQueryLoad.TutorialsIndexPerRealm, stmt);
    }
}
