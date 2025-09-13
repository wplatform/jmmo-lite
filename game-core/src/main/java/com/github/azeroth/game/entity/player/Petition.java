package com.github.azeroth.game.entity.player;


import java.util.ArrayList;

public class Petition {
    public ObjectGuid petitionGuid = ObjectGuid.EMPTY;
    public ObjectGuid ownerGuid = ObjectGuid.EMPTY;
    public String petitionName;
	public ArrayList<(
    int accountId, ObjectGuid
    playerGuid)>signatures =new ArrayList<(
    int accountId, ObjectGuid
    playerGuid)>();


    public final boolean isPetitionSignedByAccount(int accountId) {
        for (var signature : signatures) {
            if (signature.accountId == accountId) {
                return true;
            }
        }

        return false;
    }


    public final void addSignature(int accountId, ObjectGuid playerGuid, boolean isLoading) {
        signatures.add((accountId, playerGuid));

        if (isLoading) {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PETITION_SIGNATURE);
        stmt.AddValue(0, ownerGuid.getCounter());
        stmt.AddValue(1, petitionGuid.getCounter());
        stmt.AddValue(2, playerGuid.getCounter());
        stmt.AddValue(3, accountId);

        DB.characters.execute(stmt);
    }

    public final void updateName(String newName) {
        petitionName = newName;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_PETITION_NAME);
        stmt.AddValue(0, newName);
        stmt.AddValue(1, petitionGuid.getCounter());
        DB.characters.execute(stmt);
    }

    public final void removeSignatureBySigner(ObjectGuid playerGuid) {
        for (var itr : signatures) {
            if (com.github.azeroth.game.entity.Objects.equals(itr.playerGuid, playerGuid)) {
                signatures.remove(itr);

                // notify owner
                var owner = global.getObjAccessor().findConnectedPlayer(ownerGuid);

                if (owner != null) {
                    owner.getSession().sendPetitionQuery(petitionGuid);
                }

                break;
            }
        }
    }
}
