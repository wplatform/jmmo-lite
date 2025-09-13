package com.github.azeroth.game.battlepet;


import com.github.azeroth.dbc.defines.BattlePetSpeciesFlags;
import com.github.azeroth.dbc.domain.BattlePetBreedQuality;
import com.github.azeroth.dbc.domain.BattlePetSpecie;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.HighGuid;
import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.networking.packet.battlepet.PetBattleSlotUpdates;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import game.WorldSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class BattlePetMgr {
    private static final HashMap<Integer, BattlePetSpecie> BATTLEPETSPECIESBYCREATURE = new HashMap<>();
    private static final HashMap<Integer, BattlePetSpecie> BATTLEPETSPECIESBYSPELL = new HashMap<>();
    private static final MultiMap<Integer, Byte> AVAILABLEBREEDSPERSPECIES = new MultiMap<Integer, Byte>();
    private static final HashMap<Integer, BattlePetBreedQuality> DEFAULTQUALITYPERSPECIES = new HashMap<Integer, battlePetBreedQuality>();
    public static HashMap<Integer, HashMap<BattlePetState, Integer>> BATTLEPETBREEDSTATES = new HashMap<Integer, HashMap<BattlePetState, Integer>>();
    public static HashMap<Integer, HashMap<BattlePetState, Integer>> BATTLEPETSPECIESSTATES = new HashMap<Integer, HashMap<BattlePetState, Integer>>();
    private final WorldSession owner;
    private final short trapLevel;
    private final HashMap<Long, BattlePet> pets = new HashMap<Long, BattlePet>();
    private final ArrayList<BattlePetSlot> slots = new ArrayList<>();
    private boolean hasJournalLock;

    public BattlePetMgr(WorldSession owner) {
        owner = owner;

        for (byte i = 0; i < BattlePetSlot.count.getValue(); ++i) {
            BattlePetSlot slot = new BattlePetSlot();
            slot.index = i;
            slots.add(slot);
        }
    }

    public static void initialize() {
        var result = DB.Login.query("SELECT MAX(guid) FROM battle_pets");

        if (!result.isEmpty()) {
            global.getObjectMgr().getGenerator(HighGuid.BattlePet).set(result.<Long>Read(0) + 1);
        }

        for (var battlePetSpecies : CliDB.BattlePetSpeciesStorage.values()) {
            var creatureId = battlePetSpecies.creatureID;

            if (creatureId != 0) {
                BATTLEPETSPECIESBYCREATURE.put(creatureId, battlePetSpecies);
            }
        }

        for (var battlePetBreedState : CliDB.BattlePetBreedStateStorage.values()) {
            if (!BATTLEPETBREEDSTATES.containsKey(battlePetBreedState.battlePetBreedID)) {
                BATTLEPETBREEDSTATES.put(battlePetBreedState.battlePetBreedID, new HashMap<BattlePetState, Integer>());
            }

            BATTLEPETBREEDSTATES.get(battlePetBreedState.battlePetBreedID).put(BattlePetState.forValue(battlePetBreedState.BattlePetStateID), battlePetBreedState.value);
        }

        for (var battlePetSpeciesState : CliDB.BattlePetSpeciesStateStorage.values()) {
            if (!BATTLEPETSPECIESSTATES.containsKey(battlePetSpeciesState.battlePetSpeciesID)) {
                BATTLEPETSPECIESSTATES.put(battlePetSpeciesState.battlePetSpeciesID, new HashMap<BattlePetState, Integer>());
            }

            BATTLEPETSPECIESSTATES.get(battlePetSpeciesState.battlePetSpeciesID).put(BattlePetState.forValue(battlePetSpeciesState.BattlePetStateID), battlePetSpeciesState.value);
        }

        loadAvailablePetBreeds();
        loadDefaultPetQualities();
    }

    public static void addBattlePetSpeciesBySpell(int spellId, BattlePetSpecie speciesEntry) {
        BATTLEPETSPECIESBYSPELL.put(spellId, speciesEntry);
    }

    public static BattlePetSpecie getBattlePetSpeciesByCreature(int creatureId) {
        return BATTLEPETSPECIESBYCREATURE.get(creatureId);
    }

    public static BattlePetSpecie getBattlePetSpeciesBySpell(int spellId) {
        return BATTLEPETSPECIESBYSPELL.get(spellId);
    }

    public static short rollPetBreed(int species) {
        var list = AVAILABLEBREEDSPERSPECIES.get(species);

        if (list.isEmpty()) {
            return 3; // default B/B
        }

        return list.SelectRandom();
    }

    public static BattlePetBreedQuality getDefaultPetQuality(int species) {
        if (!DEFAULTQUALITYPERSPECIES.containsKey(species)) {
            return battlePetBreedQuality.Poor; // Default
        }

        return DEFAULTQUALITYPERSPECIES.get(species);
    }

    public static int selectPetDisplay(BattlePetSpecie speciesEntry) {
        var creatureTemplate = global.getObjectMgr().getCreatureTemplate(speciesEntry.creatureID);

        if (creatureTemplate != null) {
            if (!speciesEntry.getFlags().hasFlag(BattlePetSpeciesFlags.RandomDisplay)) {
                var creatureModel = creatureTemplate.getRandomValidModel();

                if (creatureModel != null) {
                    return creatureModel.creatureDisplayId;
                }
            }
        }

        return 0;
    }

    private static void loadAvailablePetBreeds() {
        var result = DB.World.query("SELECT speciesId, breedId FROM battle_pet_breeds");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 battle pet breeds. DB table `battle_pet_breeds` is empty.");

            return;
        }

        int count = 0;

        do {
            var speciesId = result.<Integer>Read(0);
            var breedId = result.<SHORT>Read(1);

            if (!CliDB.BattlePetSpeciesStorage.containsKey(speciesId)) {
                Logs.SQL.error("Non-existing BattlePetSpecies.db2 entry {0} was referenced in `battle_pet_breeds` by row ({1}, {2}).", speciesId, speciesId, breedId);

                continue;
            }

            // TODO: verify breed id (3 - 12 (male) or 3 - 22 (male and female)) if needed

            AVAILABLEBREEDSPERSPECIES.add(speciesId, (byte) breedId);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} battle pet breeds.", count);
    }

    private static void loadDefaultPetQualities() {
        var result = DB.World.query("SELECT speciesId, quality FROM battle_pet_quality");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 battle pet qualities. DB table `battle_pet_quality` is empty.");

            return;
        }

        do {
            var speciesId = result.<Integer>Read(0);
            var quality = battlePetBreedQuality.forValue(result.<Byte>Read(1));

            var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(speciesId);

            if (battlePetSpecies == null) {
                Logs.SQL.error(String.format("Non-existing BattlePetSpecies.db2 entry %1$s was referenced in `battle_pet_quality` by row (%2$s, %3$s).", speciesId, speciesId, quality));

                continue;
            }

            if (quality.getValue() >= battlePetBreedQuality.max.getValue()) {
                Logs.SQL.error(String.format("BattlePetSpecies.db2 entry %1$s was referenced in `battle_pet_quality` with non-existing quality %2$s).", speciesId, quality));

                continue;
            }

            if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.WellKnown) && quality.getValue() > battlePetBreedQuality.Rare.getValue()) {
                Logs.SQL.error(String.format("Learnable BattlePetSpecies.db2 entry %1$s was referenced in `battle_pet_quality` with invalid quality %2$s. Maximum allowed quality is BattlePetBreedQuality::Rare.", speciesId, quality));

                continue;
            }

            DEFAULTQUALITYPERSPECIES.put(speciesId, quality);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} battle pet qualities.", DEFAULTQUALITYPERSPECIES.size());
    }

    public final boolean isJournalLockAcquired() {
        return global.getWorldMgr().isBattlePetJournalLockAcquired(owner.getBattlenetAccountGUID());
    }

    public final WorldSession getOwner() {
        return owner;
    }

    public final short getTrapLevel() {
        return trapLevel;
    }

    public final ArrayList<BattlePetSlot> getSlots() {
        return slots;
    }

    public final boolean getHasJournalLock() {
        return hasJournalLock;
    }

    public final boolean isBattlePetSystemEnabled() {
        return getSlot(BattlePetSlot.Slot0).locked != true;
    }

    public final void loadFromDB(SQLResult petsResult, SQLResult slotsResult) {
        if (!petsResult.isEmpty()) {
            do {
                var species = petsResult.<Integer>Read(1);
                var ownerGuid = !petsResult.IsNull(11) ? ObjectGuid.create(HighGuid.Player, petsResult.<Long>Read(11)) : ObjectGuid.Empty;

                var speciesEntry = CliDB.BattlePetSpeciesStorage.get(species);

                if (speciesEntry != null) {
                    if (speciesEntry.getFlags().hasFlag(BattlePetSpeciesFlags.NotAccountWide)) {
                        if (ownerGuid.IsEmpty) {
                            Log.outError(LogFilter.misc, String.format("Battlenet account with id %1$s has battle pet of species %2$s with BattlePetSpeciesFlags::NotAccountWide but no owner", owner.getBattlenetAccountId(), species));

                            continue;
                        }
                    } else {
                        if (!ownerGuid.IsEmpty) {
                            Log.outError(LogFilter.misc, String.format("Battlenet account with id %1$s has battle pet of species %2$s without BattlePetSpeciesFlags::NotAccountWide but with owner", owner.getBattlenetAccountId(), species));

                            continue;
                        }
                    }

                    if (hasMaxPetCount(speciesEntry, ownerGuid)) {
                        if (ownerGuid.IsEmpty) {
                            Log.outError(LogFilter.misc, String.format("Battlenet account with id %1$s has more than maximum battle pets of species %2$s", owner.getBattlenetAccountId(), species));
                        } else {
                            Log.outError(LogFilter.misc, String.format("Battlenet account with id %1$s has more than maximum battle pets of species %2$s for player %3$s", owner.getBattlenetAccountId(), species, ownerGuid));
                        }

                        continue;
                    }

                    BattlePet pet = new BattlePet();
                    pet.packetInfo.guid = ObjectGuid.create(HighGuid.BattlePet, petsResult.<Long>Read(0));
                    pet.packetInfo.species = species;
                    pet.packetInfo.breed = petsResult.<SHORT>Read(2);
                    pet.packetInfo.displayID = petsResult.<Integer>Read(3);
                    pet.packetInfo.level = petsResult.<SHORT>Read(4);
                    pet.packetInfo.exp = petsResult.<SHORT>Read(5);
                    pet.packetInfo.health = petsResult.<Integer>Read(6);
                    pet.packetInfo.quality = petsResult.<Byte>Read(7);
                    pet.packetInfo.flags = petsResult.<SHORT>Read(8);
                    pet.packetInfo.name = petsResult.<String>Read(9);
                    pet.nameTimestamp = petsResult.<Long>Read(10);
                    pet.packetInfo.creatureID = speciesEntry.creatureID;

                    if (!petsResult.IsNull(12)) {
                        pet.declinedName = new declinedName();

                        for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
                            pet.declinedName.name.charAt(i) = petsResult.<String>Read(12 + i);
                        }
                    }

                    if (!ownerGuid.IsEmpty) {
                        BattlePetStruct.BattlePetOwnerInfo battlePetOwnerInfo = new BattlePetStruct.BattlePetOwnerInfo();
                        battlePetOwnerInfo.guid = ownerGuid;
                        battlePetOwnerInfo.playerVirtualRealm = global.getWorldMgr().getVirtualRealmAddress();
                        battlePetOwnerInfo.playerNativeRealm = global.getWorldMgr().getVirtualRealmAddress();
                        pet.packetInfo.ownerInfo = battlePetOwnerInfo;
                    }

                    pet.saveInfo = BattlePetSaveInfo.Unchanged;
                    pet.calculateStats();
                    pets.put(pet.packetInfo.guid.getCounter(), pet);
                }
            } while (petsResult.NextRow());
        }

        if (!slotsResult.isEmpty()) {
            byte i = 0; // slots.GetRowCount() should equal MAX_BATTLE_PET_SLOTS

            do {
                slots.get(i).index = slotsResult.<Byte>Read(0);
                var battlePet = pets.get(slotsResult.<Long>Read(1));

                if (battlePet != null) {
                    slots.get(i).pet = battlePet.packetInfo;
                }

                slots.get(i).locked = slotsResult.<Boolean>Read(2);
                i++;
            } while (slotsResult.NextRow());
        }
    }

    public final void saveToDB(SQLTransaction trans) {
        PreparedStatement stmt;

        for (var pair : pets.entrySet()) {
            if (pair.getValue() != null) {
                switch (pair.getValue().saveInfo) {
                    case BattlePetSaveInfo.New:
                        stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BATTLE_PETS);
                        stmt.AddValue(0, pair.getKey());
                        stmt.AddValue(1, owner.getBattlenetAccountId());
                        stmt.AddValue(2, pair.getValue().packetInfo.species);
                        stmt.AddValue(3, pair.getValue().packetInfo.breed);
                        stmt.AddValue(4, pair.getValue().packetInfo.displayID);
                        stmt.AddValue(5, pair.getValue().packetInfo.level);
                        stmt.AddValue(6, pair.getValue().packetInfo.exp);
                        stmt.AddValue(7, pair.getValue().packetInfo.health);
                        stmt.AddValue(8, pair.getValue().packetInfo.quality);
                        stmt.AddValue(9, pair.getValue().packetInfo.flags);
                        stmt.AddValue(10, pair.getValue().packetInfo.name);
                        stmt.AddValue(11, pair.getValue().nameTimestamp);

                        if (pair.getValue().packetInfo.ownerInfo.HasValue) {
                            stmt.AddValue(12, pair.getValue().packetInfo.ownerInfo.value.guid.counter);
                            stmt.AddValue(13, global.getWorldMgr().getRealmId().index);
                        } else {
                            stmt.AddNull(12);
                            stmt.AddNull(13);
                        }

                        trans.append(stmt);

                        if (pair.getValue().declinedName != null) {
                            stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BATTLE_PET_DECLINED_NAME);
                            stmt.AddValue(0, pair.getKey());

                            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                                stmt.AddValue(i + 1, pair.getValue().declinedName.name.charAt(i));
                            }

                            trans.append(stmt);
                        }


                        pair.getValue().saveInfo = BattlePetSaveInfo.Unchanged;

                        break;
                    case BattlePetSaveInfo.Changed:
                        stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_BATTLE_PETS);
                        stmt.AddValue(0, pair.getValue().packetInfo.level);
                        stmt.AddValue(1, pair.getValue().packetInfo.exp);
                        stmt.AddValue(2, pair.getValue().packetInfo.health);
                        stmt.AddValue(3, pair.getValue().packetInfo.quality);
                        stmt.AddValue(4, pair.getValue().packetInfo.flags);
                        stmt.AddValue(5, pair.getValue().packetInfo.name);
                        stmt.AddValue(6, pair.getValue().nameTimestamp);
                        stmt.AddValue(7, owner.getBattlenetAccountId());
                        stmt.AddValue(8, pair.getKey());
                        trans.append(stmt);

                        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_BATTLE_PET_DECLINED_NAME);
                        stmt.AddValue(0, pair.getKey());
                        trans.append(stmt);

                        if (pair.getValue().declinedName != null) {
                            stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BATTLE_PET_DECLINED_NAME);
                            stmt.AddValue(0, pair.getKey());

                            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                                stmt.AddValue(i + 1, pair.getValue().declinedName.name.charAt(i));
                            }

                            trans.append(stmt);
                        }

                        pair.getValue().saveInfo = BattlePetSaveInfo.Unchanged;

                        break;
                    case BattlePetSaveInfo.Removed:
                        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_BATTLE_PET_DECLINED_NAME);
                        stmt.AddValue(0, pair.getKey());
                        trans.append(stmt);

                        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_BATTLE_PETS);
                        stmt.AddValue(0, owner.getBattlenetAccountId());
                        stmt.AddValue(1, pair.getKey());
                        trans.append(stmt);
                        pets.remove(pair.getKey());

                        break;
                }
            }
        }

        stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_BATTLE_PET_SLOTS);
        stmt.AddValue(0, owner.getBattlenetAccountId());
        trans.append(stmt);

        for (var slot : slots) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_BATTLE_PET_SLOTS);
            stmt.AddValue(0, slot.index);
            stmt.AddValue(1, owner.getBattlenetAccountId());
            stmt.AddValue(2, slot.pet.guid.getCounter());
            stmt.AddValue(3, slot.locked);
            trans.append(stmt);
        }
    }

    public final BattlePet getPet(ObjectGuid guid) {
        return pets.get(guid.getCounter());
    }

    public final void addPet(int species, int display, short breed, BattlePetBreedQuality quality) {
        addPet(species, display, breed, quality, 1);
    }

    public final void addPet(int species, int display, short breed, BattlePetBreedQuality quality, short level) {
        var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(species);

        if (battlePetSpecies == null) // should never happen
        {
            return;
        }

        if (!battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.WellKnown)) // Not learnable
        {
            return;
        }

        BattlePet pet = new BattlePet();
        pet.packetInfo.guid = ObjectGuid.create(HighGuid.BattlePet, global.getObjectMgr().getGenerator(HighGuid.BattlePet).generate());
        pet.packetInfo.species = species;
        pet.packetInfo.creatureID = battlePetSpecies.creatureID;
        pet.packetInfo.displayID = display;
        pet.packetInfo.level = level;
        pet.packetInfo.exp = 0;
        pet.packetInfo.flags = 0;
        pet.packetInfo.breed = breed;
        pet.packetInfo.quality = (byte) quality.getValue();
        pet.packetInfo.name = "";
        pet.calculateStats();
        pet.packetInfo.health = pet.packetInfo.maxHealth;

        var player = owner.getPlayer();

        if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.NotAccountWide)) {
            BattlePetStruct.BattlePetOwnerInfo battlePetOwnerInfo = new BattlePetStruct.BattlePetOwnerInfo();
            battlePetOwnerInfo.guid = player.getGUID();
            battlePetOwnerInfo.playerVirtualRealm = global.getWorldMgr().getVirtualRealmAddress();
            battlePetOwnerInfo.playerNativeRealm = global.getWorldMgr().getVirtualRealmAddress();
            pet.packetInfo.ownerInfo = battlePetOwnerInfo;
        }

        pet.saveInfo = BattlePetSaveInfo.New;

        pets.put(pet.packetInfo.guid.getCounter(), pet);

        ArrayList<BattlePet> updates = new ArrayList<>();
        updates.add(pet);
        sendUpdates(updates, true);

        player.updateCriteria(CriteriaType.UniquePetsOwned);
        player.updateCriteria(CriteriaType.LearnedNewPet, species);
    }

    public final void removePet(ObjectGuid guid) {
        if (!getHasJournalLock()) {
            return;
        }

        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        pet.saveInfo = BattlePetSaveInfo.removed;
    }

    public final void clearFanfare(ObjectGuid guid) {
        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        pet.packetInfo.flags &= (short) ~BattlePetDbFlags.FanfareNeeded;

        if (pet.saveInfo != BattlePetSaveInfo.New) {
            pet.saveInfo = BattlePetSaveInfo.changed;
        }
    }

    public final void modifyName(ObjectGuid guid, String name, DeclinedName declinedName) {
        if (!getHasJournalLock()) {
            return;
        }

        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        pet.packetInfo.name = name;
        pet.nameTimestamp = gameTime.GetGameTime();

        pet.declinedName = new declinedName();

        if (declinedName != null) {
            pet.declinedName = declinedName;
        }

        if (pet.saveInfo != BattlePetSaveInfo.New) {
            pet.saveInfo = BattlePetSaveInfo.changed;
        }

        // Update the timestamp if the battle pet is summoned
        var summonedBattlePet = owner.getPlayer().getSummonedBattlePet();

        if (summonedBattlePet != null) {
            if (Objects.equals(summonedBattlePet.getBattlePetCompanionGUID(), guid)) {
                summonedBattlePet.setBattlePetCompanionNameTimestamp((int) pet.nameTimestamp);
            }
        }
    }

    public final byte getPetCount(BattlePetSpecie battlePetSpecies, ObjectGuid ownerGuid) {
        return (byte) pets.values().size() (battlePet ->
        {
            if (battlePet == null || battlePet.packetInfo.species != battlePetSpecies.id) {
                return false;
            }

            if (battlePet.saveInfo == BattlePetSaveInfo.removed) {
                return false;
            }

            if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.NotAccountWide)) {
                if (!ownerGuid.isEmpty() && battlePet.packetInfo.ownerInfo.HasValue) {
                    if (ObjectGuid.opNotEquals(battlePet.packetInfo.ownerInfo.value.guid, ownerGuid)) {
                        return false;
                    }
                }
            }

            return true;
        });
    }

    public final boolean hasMaxPetCount(BattlePetSpecie battlePetSpecies, ObjectGuid ownerGuid) {
        var maxPetsPerSpecies = battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.LegacyAccountUnique) ? 1 : SharedConst.DefaultMaxBattlePetsPerSpecies;

        return getPetCount(battlePetSpecies, ownerGuid) >= maxPetsPerSpecies;
    }

    public final int getPetUniqueSpeciesCount() {
        HashSet<Integer> speciesIds = new HashSet<Integer>();

        for (var pair : pets.entrySet()) {
            if (pair.getValue() != null) {
                speciesIds.add(pair.getValue().packetInfo.species);
            }
        }

        return (int) speciesIds.size();
    }

    public final void unlockSlot(BattlePetSlot slot) {

        var slotIndex = (byte) slot.ordinal();

        if (!slots.get(slotIndex).locked) {
            return;
        }

        slots.get(slotIndex).locked = false;

        PetBattleSlotUpdates updates = new PetBattleSlotUpdates();
        updates.slots.add(slots.get(slotIndex));
        updates.autoSlotted = false; // what's this?
        updates.newSlot = true; // causes the "new slot unlocked" bubble to appear
        owner.sendPacket(updates);
    }

    public final short getMaxPetLevel() {
        short level = 0;

        for (var pet : pets.entrySet()) {
            if (pet.getValue() != null && pet.getValue().saveInfo != BattlePetSaveInfo.removed) {
                level = Math.max(level, pet.getValue().packetInfo.level);
            }
        }

        return level;
    }

    public final void cageBattlePet(ObjectGuid guid) {
        if (!getHasJournalLock()) {
            return;
        }

        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(pet.packetInfo.species);

        if (battlePetSpecies != null) {
            if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.NotTradable)) {
                return;
            }
        }

        if (isPetInSlot(guid)) {
            return;
        }

        if (pet.packetInfo.health < pet.packetInfo.maxHealth) {
            return;
        }

        ArrayList<ItemPosCount> dest = new ArrayList<>();

        if (owner.getPlayer().canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, SharedConst.BattlePetCageItemId, 1) != InventoryResult.Ok) {
            return;
        }

        var item = owner.getPlayer().storeNewItem(dest, SharedConst.BattlePetCageItemId, true);

        if (!item) {
            return;
        }

        item.setModifier(ItemModifier.battlePetSpeciesId, pet.packetInfo.species);
        item.setModifier(ItemModifier.BattlePetBreedData, (int) (pet.packetInfo.breed | (pet.packetInfo.quality << 24)));
        item.setModifier(ItemModifier.battlePetLevel, pet.packetInfo.level);
        item.setModifier(ItemModifier.BattlePetDisplayId, pet.packetInfo.displayID);

        owner.getPlayer().sendNewItem(item, 1, true, false);

        removePet(guid);

        BattlePetDeleted deletePet = new BattlePetDeleted();
        deletePet.petGuid = guid;
        owner.sendPacket(deletePet);

        // Battle pet despawns if it's summoned
        var player = owner.getPlayer();
        var summonedBattlePet = player.getSummonedBattlePet();

        if (summonedBattlePet != null) {
            if (Objects.equals(summonedBattlePet.getBattlePetCompanionGUID(), guid)) {
                summonedBattlePet.despawnOrUnsummon();
                player.setBattlePetData(null);
            }
        }
    }

    public final void changeBattlePetQuality(ObjectGuid guid, BattlePetBreedQuality quality) {
        if (!getHasJournalLock()) {
            return;
        }

        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        if (quality.getValue() > battlePetBreedQuality.Rare.getValue()) {
            return;
        }

        var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(pet.packetInfo.species);

        if (battlePetSpecies != null) {
            if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.CantBattle)) {
                return;
            }
        }

        var qualityValue = (byte) quality.getValue();

        if (pet.packetInfo.quality >= qualityValue) {
            return;
        }

        pet.packetInfo.quality = qualityValue;
        pet.calculateStats();
        pet.packetInfo.health = pet.packetInfo.maxHealth;

        if (pet.saveInfo != BattlePetSaveInfo.New) {
            pet.saveInfo = BattlePetSaveInfo.changed;
        }

        ArrayList<BattlePet> updates = new ArrayList<>();
        updates.add(pet);
        sendUpdates(updates, false);

        // UF::PlayerData::CurrentBattlePetBreedQuality isn't updated (Intended)
        // owner.getPlayer().setCurrentBattlePetBreedQuality(qualityValue);
    }

    public final void grantBattlePetExperience(ObjectGuid guid, short xp, BattlePetXpSource xpSource) {
        if (!getHasJournalLock()) {
            return;
        }

        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        if (xp <= 0 || xpSource.getValue() >= BattlePetXpSource.count.getValue()) {
            return;
        }

        var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(pet.packetInfo.species);

        if (battlePetSpecies != null) {
            if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.CantBattle)) {
                return;
            }
        }

        var level = pet.packetInfo.level;

        if (level >= SharedConst.maxBattlePetLevel) {
            return;
        }

        var xpEntry = CliDB.BattlePetXPGameTable.GetRow(level);

        if (xpEntry == null) {
            return;
        }

        var player = owner.getPlayer();
        var nextLevelXp = (short) CliDB.GetBattlePetXPPerLevel(xpEntry);

        if (xpSource == BattlePetXpSource.PetBattle) {
            xp = (short) (xp * player.getTotalAuraMultiplier(AuraType.ModBattlePetXpPct));
        }

        xp += pet.packetInfo.exp;

        while (xp >= nextLevelXp && level < SharedConst.maxBattlePetLevel) {
            xp -= nextLevelXp;

            xpEntry = CliDB.BattlePetXPGameTable.GetRow(++level);

            if (xpEntry == null) {
                return;
            }

            nextLevelXp = (short) CliDB.GetBattlePetXPPerLevel(xpEntry);

            player.updateCriteria(CriteriaType.BattlePetReachLevel, pet.packetInfo.species, level);

            if (xpSource == BattlePetXpSource.PetBattle) {
                player.updateCriteria(CriteriaType.ActivelyEarnPetLevel, pet.packetInfo.species, level);
            }
        }

        pet.packetInfo.level = level;
        pet.packetInfo.exp = (short) (level < SharedConst.MaxBattlePetLevel ? xp : 0);
        pet.calculateStats();
        pet.packetInfo.health = pet.packetInfo.maxHealth;

        if (pet.saveInfo != BattlePetSaveInfo.New) {
            pet.saveInfo = BattlePetSaveInfo.changed;
        }

        ArrayList<BattlePet> updates = new ArrayList<>();
        updates.add(pet);
        sendUpdates(updates, false);
    }

    public final void grantBattlePetLevel(ObjectGuid guid, short grantedLevels) {
        if (!getHasJournalLock()) {
            return;
        }

        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(pet.packetInfo.species);

        if (battlePetSpecies != null) {
            if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.CantBattle)) {
                return;
            }
        }

        var level = pet.packetInfo.level;

        if (level >= SharedConst.maxBattlePetLevel) {
            return;
        }

        while (grantedLevels > 0 && level < SharedConst.maxBattlePetLevel) {
            ++level;
            --grantedLevels;

            owner.getPlayer().updateCriteria(CriteriaType.BattlePetReachLevel, pet.packetInfo.species, level);
        }

        pet.packetInfo.level = level;

        if (level >= SharedConst.maxBattlePetLevel) {
            pet.packetInfo.exp = 0;
        }

        pet.calculateStats();
        pet.packetInfo.health = pet.packetInfo.maxHealth;

        if (pet.saveInfo != BattlePetSaveInfo.New) {
            pet.saveInfo = BattlePetSaveInfo.changed;
        }

        var updates = new ArrayList<>();
        updates.add(pet);
        sendUpdates(updates, false);
    }

    public final void healBattlePetsPct(byte pct) {
        // TODO: After each Pet Battle, any injured companion will automatically
        // regain 50 % of the damage that was taken during combat
        ArrayList<BattlePet> updates = new ArrayList<>();

        for (var pet : pets.values()) {
            if (pet != null && pet.packetInfo.health != pet.packetInfo.maxHealth) {
                pet.packetInfo.health += MathUtil.CalculatePct(pet.packetInfo.maxHealth, pct);
                // don't allow Health to be greater than MaxHealth
                pet.packetInfo.health = Math.min(pet.packetInfo.health, pet.packetInfo.maxHealth);

                if (pet.saveInfo != BattlePetSaveInfo.New) {
                    pet.saveInfo = BattlePetSaveInfo.changed;
                }

                updates.add(pet);
            }
        }

        sendUpdates(updates, false);
    }

    public final void updateBattlePetData(ObjectGuid guid) {
        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        var player = owner.getPlayer();

        // Update battle pet related update fields
        var summonedBattlePet = player.getSummonedBattlePet();

        if (summonedBattlePet != null) {
            if (Objects.equals(summonedBattlePet.getBattlePetCompanionGUID(), guid)) {
                summonedBattlePet.setWildBattlePetLevel(pet.packetInfo.level);
                player.setBattlePetData(pet);
            }
        }
    }

    public final void summonPet(ObjectGuid guid) {
        var pet = getPet(guid);

        if (pet == null) {
            return;
        }

        var speciesEntry = CliDB.BattlePetSpeciesStorage.get(pet.packetInfo.species);

        if (speciesEntry == null) {
            return;
        }

        var player = owner.getPlayer();
        player.setBattlePetData(pet);

        CastSpellExtraArgs args = new CastSpellExtraArgs();
        var summonSpellId = speciesEntry.SummonSpellID;

        if (summonSpellId == 0) {
            summonSpellId = SharedConst.SpellSummonBattlePet;
            args.addSpellMod(SpellValueMod.BasePoint0, (int) speciesEntry.creatureID);
        }

        player.castSpell(owner.getPlayer(), summonSpellId, args);
    }

    public final void dismissPet() {
        var player = owner.getPlayer();
        var summonedBattlePet = player.getSummonedBattlePet();

        if (summonedBattlePet) {
            summonedBattlePet.despawnOrUnsummon();
            player.setBattlePetData(null);
        }
    }

    public final void sendJournal() {
        if (!getHasJournalLock()) {
            sendJournalLockStatus();
        }

        BattlePetJournal battlePetJournal = new BattlePetJournal();
        battlePetJournal.trap = trapLevel;
        battlePetJournal.hasJournalLock = hasJournalLock;

        for (var pet : pets.entrySet()) {
            if (pet.getValue() != null && pet.getValue().saveInfo != BattlePetSaveInfo.removed) {
                if (!pet.getValue().packetInfo.ownerInfo.HasValue || Objects.equals(pet.getValue().packetInfo.ownerInfo.value.guid, owner.getPlayer().getGUID())) {
                    battlePetJournal.pets.add(pet.getValue().packetInfo);
                }
            }
        }

        battlePetJournal.slots = slots;
        owner.sendPacket(battlePetJournal);
    }

    public final void sendError(BattlePetError error, int creatureId) {
        BattlePetErrorPacket battlePetError = new BattlePetErrorPacket();
        battlePetError.result = error;
        battlePetError.creatureID = creatureId;
        owner.sendPacket(battlePetError);
    }

    public final void sendJournalLockStatus() {
        if (!isJournalLockAcquired()) {
            toggleJournalLock(true);
        }

        if (getHasJournalLock()) {
            owner.sendPacket(new BattlePetJournalLockAcquired());
        } else {
            owner.sendPacket(new BattlePetJournalLockDenied());
        }
    }

    public final BattlePetSlot getSlot(BattlePetSlot slot) {
        return slot.getValue() < BattlePetSlot.count.getValue() ? slots.get((byte) slot.getValue()) : null;
    }

    public final void toggleJournalLock(boolean on) {
        hasJournalLock = on;
    }

    private boolean isPetInSlot(ObjectGuid guid) {
        for (var slot : slots) {
            if (Objects.equals(slot.pet.guid, guid)) {
                return true;
            }
        }

        return false;
    }

    private void sendUpdates(ArrayList<BattlePet> pets, boolean petAdded) {
        BattlePetUpdates updates = new BattlePetUpdates();

        for (var pet : pets) {
            updates.pets.add(pet.packetInfo);
        }

        updates.petAdded = petAdded;
        owner.sendPacket(updates);
    }
}
