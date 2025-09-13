package com.github.azeroth.game.server;

class AccountInfoQueryHolder extends SQLQueryHolder<AccountInfoQueryLoad> {

    public final void initialize(int accountId, int battlenetAccountId) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_TOYS);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.GlobalAccountToys, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BATTLE_PETS);
        stmt.AddValue(0, battlenetAccountId);
        stmt.AddValue(1, global.getWorldMgr().getRealmId().index);
        SetQuery(AccountInfoQueryLoad.BattlePets, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BATTLE_PET_SLOTS);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.BattlePetSlot, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_HEIRLOOMS);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.GlobalAccountHeirlooms, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_MOUNTS);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.mounts, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SelBnetCharacterCountsByAccountId);
        stmt.AddValue(0, accountId);
        SetQuery(AccountInfoQueryLoad.GlobalRealmCharacterCounts, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_ITEM_APPEARANCES);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.ItemAppearances, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_ITEM_FAVORITE_APPEARANCES);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.ItemFavoriteAppearances, stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_BNET_TRANSMOG_ILLUSIONS);
        stmt.AddValue(0, battlenetAccountId);
        SetQuery(AccountInfoQueryLoad.transmogIllusions, stmt);
    }
}
