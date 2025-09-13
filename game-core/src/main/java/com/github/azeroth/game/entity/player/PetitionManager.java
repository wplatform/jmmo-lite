package com.github.azeroth.game.entity.player;


import java.util.HashMap;
import java.util.Objects;

public class PetitionManager {
    private final HashMap<ObjectGuid, Petition> petitionStorage = new HashMap<ObjectGuid, Petition>();

    private PetitionManager() {
    }

    public final void loadPetitions() {
        var oldMsTime = System.currentTimeMillis();
        petitionStorage.clear();

        var result = DB.characters.query("SELECT petitionguid, ownerguid, name FROM petition");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 petitions.");

            return;
        }

        int count = 0;

        do {
            addPetition(ObjectGuid.create(HighGuid.Item, result.<Long>Read(0)), ObjectGuid.create(HighGuid.Player, result.<Long>Read(1)), result.<String>Read(2), true);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s petitions in: %2$s ms.", count, time.GetMSTimeDiffToNow(oldMsTime)));
    }

    public final void loadSignatures() {
        var oldMSTime = System.currentTimeMillis();

        var result = DB.characters.query("SELECT petitionguid, player_account, playerguid FROM petition_sign");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 Petition signs!");

            return;
        }

        int count = 0;

        do {
            var petition = getPetition(ObjectGuid.create(HighGuid.Item, result.<Long>Read(0)));

            if (petition == null) {
                continue;
            }

            petition.addSignature(result.<Integer>Read(1), ObjectGuid.create(HighGuid.Player, result.<Long>Read(2)), true);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s Petition signs in %2$s ms.", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void addPetition(ObjectGuid petitionGuid, ObjectGuid ownerGuid, String name, boolean isLoading) {
        Petition p = new Petition();
        p.petitionGuid = petitionGuid;
        p.ownerGuid = ownerGuid;
        p.petitionName = name;
        p.signatures.clear();

        petitionStorage.put(petitionGuid, p);

        if (isLoading) {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PETITION);
        stmt.AddValue(0, ownerGuid.getCounter());
        stmt.AddValue(1, petitionGuid.getCounter());
        stmt.AddValue(2, name);
        DB.characters.execute(stmt);
    }

    public final void removePetition(ObjectGuid petitionGuid) {
        petitionStorage.remove(petitionGuid);

        // Delete From DB
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PETITION_BY_GUID);
        stmt.AddValue(0, petitionGuid.getCounter());
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PETITION_SIGNATURE_BY_GUID);
        stmt.AddValue(0, petitionGuid.getCounter());
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);
    }

    public final Petition getPetition(ObjectGuid petitionGuid) {
        return petitionStorage.get(petitionGuid);
    }

    public final Petition getPetitionByOwner(ObjectGuid ownerGuid) {
        return petitionStorage.FirstOrDefault(p -> Objects.equals(p.value.ownerGuid, ownerGuid)).value;
    }

    public final void removePetitionsByOwner(ObjectGuid ownerGuid) {
        for (var key : petitionStorage.keySet().ToList()) {
            if (Objects.equals(petitionStorage.get(key).ownerGuid, ownerGuid)) {
                petitionStorage.remove(key);

                break;
            }
        }

        SQLTransaction trans = new SQLTransaction();
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PETITION_BY_OWNER);
        stmt.AddValue(0, ownerGuid.getCounter());
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PETITION_SIGNATURE_BY_OWNER);
        stmt.AddValue(0, ownerGuid.getCounter());
        trans.append(stmt);
        DB.characters.CommitTransaction(trans);
    }

    public final void removeSignaturesBySigner(ObjectGuid signerGuid) {
        for (var petitionPair : petitionStorage.entrySet()) {
            petitionPair.getValue().removeSignatureBySigner(signerGuid);
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_PETITION_SIGNATURES);
        stmt.AddValue(0, signerGuid.getCounter());
        DB.characters.execute(stmt);
    }
}
