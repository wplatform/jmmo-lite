package com.github.azeroth.game;


class EnumCharactersQueryHolder extends SQLQueryHolder<EnumCharacterQueryLoad> {
    private boolean isDeletedCharacters = false;

    public final boolean initialize(int accountId, boolean withDeclinedNames, boolean isDeletedCharacters) {
        isDeletedCharacters = isDeletedCharacters;

        CharStatements[][] statements =
                {
                        new CharStatements[]{CharStatements.SEL_ENUM, CharStatements.SEL_ENUM_DECLINED_NAME, CharStatements.SEL_ENUM_CUSTOMIZATIONS},
                        new CharStatements[]{CharStatements.SEL_UNDELETE_ENUM, CharStatements.SEL_UNDELETE_ENUM_DECLINED_NAME, CharStatements.SEL_UNDELETE_ENUM_CUSTOMIZATIONS}
                };

        var result = true;
        var stmt = DB.characters.GetPreparedStatement(statements[isDeletedCharacters ? 1 : 0][withDeclinedNames ? 1 : 0]);
        stmt.AddValue(0, accountId);
        SetQuery(EnumCharacterQueryLoad.characters, stmt);

        stmt = DB.characters.GetPreparedStatement(statements[isDeletedCharacters ? 1 : 0][2]);
        stmt.AddValue(0, accountId);
        SetQuery(EnumCharacterQueryLoad.customizations, stmt);

        return result;
    }

    public final boolean isDeletedCharacters() {
        return isDeletedCharacters;
    }
}
