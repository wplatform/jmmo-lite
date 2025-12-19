package com.github.azeroth.game.entity.pet;

import com.github.azeroth.game.domain.pet.PetType;
import com.github.azeroth.game.entity.creature.Guardian;


public class Pet extends Guardian {

    //C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new Dictionary<uint, PetSpell> Spells = new();

    public HashMap<Integer, PetSpell> spells = new HashMap<Integer, PetSpell>();
    public boolean removed;
    private static final int PET_FOCUS_REGEN_INTERVAL = 4 * Time.InMilliseconds;
    private static final int HAPPINESS_LEVEL_SIZE = 333000;
    private static final float PET_XPFACTOR = 0.05f;
    
//ORIGINAL LINE: readonly List<uint> _autospells = new();
    private final ArrayList<Integer> autospells = new ArrayList<Integer>();

    private PetType petType = getPetType().values()[0];
    private int duration; // time until unsummon (used mostly for summoned guardians and not used for controlled pets)
    private boolean loading;
    
//ORIGINAL LINE: uint _focusRegenTimer;
    private int focusRegenTimer;
    private GroupUpdatePetFlags mGroupUpdateMask = GroupUpdatePetFlags.values()[0];

    private DeclinedName declinedname;
    
//ORIGINAL LINE: ushort _petSpecialization;
    private short petSpecialization;

    @Override
    public float getNativeObjectScale() {
        var creatureFamily = CliDB.creatureFamilyStorage.LookupByKey(getTemplate().family);

        if (creatureFamily != null && creatureFamily.MinScale > 0.0f && getPetType() == PetType.Hunter) {
            float scale;

            if (getLevel() >= creatureFamily.MaxScaleLevel) {
                scale = creatureFamily.MaxScale;
            } else if (getLevel() <= creatureFamily.MinScaleLevel) {
                scale = creatureFamily.MinScale;
            } else {
                scale = creatureFamily.MinScale + (float)(getLevel() - creatureFamily.MinScaleLevel) / creatureFamily.MaxScaleLevel * (creatureFamily.MaxScale - creatureFamily.MinScale);
            }

            return scale;
        }
        return super.getNativeObjectScale();
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    
//ORIGINAL LINE: public override byte getPetAutoSpellSize()
    @Override
    public byte getPetAutoSpellSize() {

//ORIGINAL LINE: return (byte)_autospells.Count;
        return (byte)autospells.size();
    }

    public final Player getOwningPlayer() {
        return super.getOwnerUnit().getAsPlayer();
    }

    public final PetType getPetType() {
        return petType;
    }
    public final void setPetType(PetType value) {
        petType = value;
    }

    public final boolean isControlled() {
        return getPetType() == PetType.SUMMON_PET || getPetType() == PetType.HUNTER_PET;
    }

    public final boolean isTemporarySummoned() {
        return duration > 0;
    }

    public final int getDuration() {
        return duration;
    }

    
//ORIGINAL LINE: public ushort getSpecialization()
    public final short getSpecialization() {
        return petSpecialization;
    }

    public final GroupUpdatePetFlags getGroupUpdateFlag() {
        return mGroupUpdateMask;
    }
    public final void setGroupUpdateFlag(GroupUpdatePetFlags value) {
        if (getOwningPlayer().getGroup()) {
            mGroupUpdateMask = GroupUpdatePetFlags.forValue(mGroupUpdateMask.getValue() | value.getValue());
            getOwningPlayer().setGroupUpdateFlag(GroupUpdateFlags.Pet);
        }
    }


    public Pet(Player owner) {
        this(owner, PetType.Max);
    }

    
//ORIGINAL LINE: public Pet(Player owner, PetType type = PetType.Max)
    public Pet(Player owner, PetType type) {
        super(null, owner, true);
        petType = type;
        unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.Pet.getValue());

        if (type == PetType.Hunter) {
            unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.HunterPet.getValue());
        }

        if (!unitTypeMask.HasAnyFlag(unitTypeMask.ControlableGuardian)) {
            unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.ControlableGuardian.getValue());
            initCharmInfo();
        }

        setName("Pet");
        focusRegenTimer = PET_FOCUS_REGEN_INTERVAL;
    }

    @Override
    public void close() throws IOException {
        declinedname = null;
        super.close();
    }

    @Override
    public void addToWorld() {
        //- Register the pet for guid lookup
        if (!isInWorld) {
            // Register the pet for guid lookup
            super.addToWorld();
            initializeAI();
            var zoneScript = getZoneScript1() != null ? getZoneScript1() : getInstanceScript();

            if (zoneScript != null) {
                zoneScript.onCreatureCreate(this);
            }
        }

        // Prevent stuck pets when zoning. Pets default to "follow" when added to world
        // so we'll reset flags and let the AI handle things
        if (getCharmInfo() != null && getCharmInfo().hasCommandState(CommandStates.Follow)) {
            getCharmInfo().setIsCommandAttack(false);
            getCharmInfo().setIsCommandFollow(false);
            getCharmInfo().setIsAtStay(false);
            getCharmInfo().setIsFollowing(false);
            getCharmInfo().setIsReturning(false);
        }
    }

    @Override
    public void removeFromWorld() {
        // Remove the pet from the accessor
        if (isInWorld) {
            // Don't call the function for Creature, normal mobs + totems go in a different storage
            super.removeFromWorld();
            tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
            getMap().getObjectsStore().TryRemove(getGUID().clone(), tempOut__);
            _ = tempOut__.outArgValue;
        }
    }

    
//ORIGINAL LINE: public static Tuple<PetStable.PetInfo, PetSaveMode> GetLoadPetInfo(PetStable stable, uint petEntry, uint petnumber, System.Nullable<PetSaveMode> slot)
    public static Tuple<PetStable.PetInfo, PetSaveMode> getLoadPetInfo(PetStable stable, int petEntry, int petnumber, PetSaveMode slot) {
        if (petnumber != 0) {
            // Known petnumber entry
            for (var activeSlot = 0; activeSlot < stable.activePets.getLength(); ++activeSlot) {
                if (stable.activePets[activeSlot] != null && stable.activePets[activeSlot].petNumber == petnumber) {
                    return Tuple.Create(stable.activePets[activeSlot], PetSaveMode.FirstActiveSlot + activeSlot);
                }
            }

            for (var stableSlot = 0; stableSlot < stable.stabledPets.getLength(); ++stableSlot) {
                if (stable.stabledPets[stableSlot] != null && stable.stabledPets[stableSlot].petNumber == petnumber) {
                    return Tuple.Create(stable.stabledPets[stableSlot], PetSaveMode.FirstStableSlot + stableSlot);
                }
            }

            for (var pet : stable.unslottedPets) {
                if (pet.PetNumber == petnumber) {
                    return Tuple.Create(pet, PetSaveMode.NotInSlot);
                }
            }
        } else if (slot.getHasValue()) {
            // Current pet
            if (slot == PetSaveMode.AsCurrent) {
                if (stable.getCurrentActivePetIndex() != null && stable.activePets[stable.getCurrentActivePetIndex().intValue()] != null) {
                    return Tuple.Create(stable.activePets[stable.getCurrentActivePetIndex().intValue()], PetSaveMode.forValue(stable.getCurrentActivePetIndex()));
                }
            }

            if (slot >= PetSaveMode.FirstActiveSlot.getValue() && slot < PetSaveMode.LastActiveSlot.getValue()) {
                if (stable.activePets[(int)slot.getValue()] != null) {
                    return Tuple.Create(stable.activePets[(int)slot.getValue()], slot.getValue());
                }
            }

            if (slot >= PetSaveMode.FirstStableSlot.getValue() && slot < PetSaveMode.LastStableSlot.getValue()) {
                if (stable.stabledPets[(int)slot.getValue()] != null) {
                    return Tuple.Create(stable.stabledPets[(int)slot.getValue()], slot.getValue());
                }
            }
        } else if (petEntry != 0) {
            // known petEntry entry (unique for summoned pet, but non unique for hunter pet (only from current or not stabled pets)

            for (var pet : stable.unslottedPets) {
                if (pet.CreatureId == petEntry) {
                    return Tuple.Create(pet, PetSaveMode.NotInSlot);
                }
            }
        } else {
            // Any current or other non-stabled pet (for hunter "call pet")
            if (stable.activePets[0] != null) {
                return Tuple.Create(stable.activePets[0], PetSaveMode.FirstActiveSlot);
            }

            if (!stable.unslottedPets.Empty()) {
                return Tuple.Create(stable.unslottedPets.get(0), PetSaveMode.NotInSlot);
            }
        }

        return Tuple.<PetStable.PetInfo, PetSaveMode>Create(null, PetSaveMode.AsDeleted);
    }


    public final boolean loadPetFromDB(Player owner, int petEntry, int petnumber, boolean current) {
        return loadPetFromDB(owner, petEntry, petnumber, current, null);
    }

    public final boolean loadPetFromDB(Player owner, int petEntry, int petnumber) {
        return loadPetFromDB(owner, petEntry, petnumber, false, null);
    }

    public final boolean loadPetFromDB(Player owner, int petEntry) {
        return loadPetFromDB(owner, petEntry, 0, false, null);
    }

    public final boolean loadPetFromDB(Player owner) {
        return loadPetFromDB(owner, 0, 0, false, null);
    }

    
//ORIGINAL LINE: public bool LoadPetFromDB(Player owner, uint petEntry = 0, uint petnumber = 0, bool current = false, System.Nullable<PetSaveMode> forcedSlot = null)

    public final boolean loadPetFromDB(Player owner, int petEntry, int petnumber, boolean current, PetSaveMode forcedSlot) {
        loading = true;

        var petStable = owner.getPetStable1();

        var ownerid = owner.getGUID().getCounter();
//C# TO JAVA CONVERTER TODO TASK: Java has no equivalent to C# deconstruction declarations:
        var(petInfo, slot) = GetLoadPetInfo(petStable, petEntry, petnumber, forcedSlot);

        if (petInfo == null || (slot >= PetSaveMode.FirstStableSlot.getValue() && slot < PetSaveMode.LastStableSlot.getValue())) {
            loading = false;

            return false;
        }

        // Don't try to reload the current pet
        if (petStable.getCurrentPet() != null && owner.getCurrentPet() != null && petStable.getCurrentPet().petNumber == petInfo.PetNumber) {
            return false;
        }

        var spellInfo = Global.getSpellMgr().GetSpellInfo(petInfo.CreatedBySpellId, owner.getMap().getDifficultyID());

        var isTemporarySummon = spellInfo != null && spellInfo.Duration > 0;

        if (current && isTemporarySummon) {
            return false;
        }

        if (petInfo.Type == PetType.Hunter) {
            var creatureInfo = Global.getObjectMgr().getCreatureTemplate(petInfo.CreatureId);

            if (creatureInfo == null || !creatureInfo.isTameable(owner.getCanTameExoticPets())) {
                return false;
            }
        }

        if (current && owner.isPetNeedBeTemporaryUnsummoned()) {
            owner.setTemporaryUnsummonedPetNumber(petInfo.PetNumber);

            return false;
        }

        var map = owner.getMap();
        var guid = map.generateLowGuid(HighGuid.Pet);

        if (!create(guid, map, petInfo.CreatureId, petInfo.PetNumber)) {
            return false;
        }

        PhasingHandler.inheritPhaseShift(this, owner);

        setPetType(petInfo.Type);
        setFaction(owner.getFaction());
        setCreatedBySpell(petInfo.CreatedBySpellId);

        var pos = new Position();

        if (isCritter()) {
            owner.getClosePoint(pos, getCombatReach(), SharedConst.PetFollowDist, getFollowAngle());
            pos.setOrientation(owner.location.getOrientation());
            location.relocate(pos);

            if (!location.isPositionValid()) {
                Log.outError(LogFilter.Pet, "Pet (guidlow {0}, entry {1}) not loaded. Suggested coordinates isn't valid (X: {2} Y: {3})", getGUID().toString(), getEntry(), location.x, location.y);

                return false;
            }

            map.AddToMap(getAsCreature());

            return true;
        }

        getCharmInfo().setPetNumber(petInfo.PetNumber, isPermanentPetFor(owner));

        setDisplayId(petInfo.DisplayId);
        setNativeDisplayId(petInfo.DisplayId);

//ORIGINAL LINE: uint petlevel = petInfo.Level;
        int petlevel = petInfo.Level;
        replaceAllNpcFlags(NPCFlags.None);
        replaceAllNpcFlags2(NPCFlags2.None);
        setName(petInfo.Name);

        switch (getPetType()) {
            case Summon:
                petlevel = owner.getLevel();

                setClass(PlayerClass.Mage);
                replaceAllUnitFlags(UnitFlags.PlayerControlled); // this enables popup window (pet dismiss, cancel)

                break;
            case Hunter:
                setClass(PlayerClass.Warrior);
                setGender(Gender.None);
                setSheath(SheathState.Melee);
                replaceAllPetFlags(petInfo.WasRenamed ? UnitPetFlags.CanBeAbandoned : UnitPetFlags.CanBeRenamed.getValue() | UnitPetFlags.CanBeAbandoned.getValue());
                replaceAllUnitFlags(UnitFlags.PlayerControlled); // this enables popup window (pet abandon, cancel)

                break;
            default:
                if (!isPetGhoul()) {
                    Log.outError(LogFilter.Pet, "Pet have incorrect type ({0}) for pet loading.", getPetType());
                }

                break;
        }


//ORIGINAL LINE: SetPetNameTimestamp((uint)GameTime.GetGameTime());
        setPetNameTimestamp((int)GameTime.getGameTime()); // cast can't be helped here
        setCreatorGUID(owner.getGUID().clone());

        initStatsForLevel(petlevel);
        setPetExperience(petInfo.Experience);

        synchronizeLevelWithOwner();

        // Set pet's position after setting level, its size depends on it
        owner.getClosePoint(pos, getCombatReach(), SharedConst.PetFollowDist, getFollowAngle());
        location.relocate(pos);

        if (!location.isPositionValid()) {
            Log.outError(LogFilter.Pet, "Pet ({0}, entry {1}) not loaded. Suggested coordinates isn't valid (X: {2} Y: {3})", getGUID().toString(), getEntry(), location.x, location.y);

            return false;
        }

        reactState = petInfo.ReactState;
        setCanModifyStats(true);

        if (getPetType() == PetType.Summon && !current) { //all (?) summon pets come with full health when called, but not when they are current
            setFullPower(PowerType.Mana);
        } else {
            var savedhealth = petInfo.Health;
            var savedmana = petInfo.Mana;

            if (savedhealth == 0 && getPetType() == PetType.Hunter) {
                setDeathState(deathState.JustDied);
            } else {
                SetHealth(savedhealth);
                setPower(PowerType.Mana, (int)savedmana);
            }
        }

        // set current pet as current
        // 0-4=current
        // PET_SAVE_NOT_IN_SLOT(-1) = not stable slot (summoning))
        if (slot == PetSaveMode.NotInSlot) {
            var petInfoNumber = petInfo.PetNumber;

            if (!petStable.currentPetIndex.equals(0)) {
                owner.removePet(null, PetSaveMode.NotInSlot);
            }

            var unslottedPetIndex = tangible.ListHelper.findIndex(petStable.unslottedPets, unslottedPet -> unslottedPet.PetNumber == petInfoNumber);


//ORIGINAL LINE: petStable.SetCurrentUnslottedPetIndex((uint)unslottedPetIndex);
            petStable.setCurrentUnslottedPetIndex((int)unslottedPetIndex);
        } else if (PetSaveMode.FirstActiveSlot.getValue() <= slot && slot <= PetSaveMode.LastActiveSlot.getValue()) {
            var activePetIndex = Array.FindIndex(petStable.activePets, pet -> {
                if (pet != null) {
                    pet.PetNumber;
                }
            } == petnumber);

            if (activePetIndex == -1) {
                activePetIndex = (int)petnumber;
            }


//ORIGINAL LINE: petStable.SetCurrentActivePetIndex((uint)activePetIndex);
            petStable.setCurrentActivePetIndex((int)activePetIndex);
        }

        // Send fake summon spell cast - this is needed for correct cooldown application for spells
        // Example: 46584 - without this cooldown (which should be set always when pet is loaded) isn't set clientside
        // @todo pets should be summoned from real cast instead of just faking it?
        if (petInfo.CreatedBySpellId != 0) {
            SpellGo spellGo = new SpellGo();
            var castData = spellGo.cast;

            castData.casterGUID = owner.getGUID().clone();
            castData.casterUnit = owner.getGUID().clone();
            castData.castID = ObjectGuid.create(HighGuid.Cast, SpellCastSource.Normal, owner.location.mapId, petInfo.CreatedBySpellId, map.generateLowGuid(HighGuid.Cast));
            castData.spellID = (int)petInfo.CreatedBySpellId;
            castData.castFlags = SpellCastFlags.Unk9;
            castData.castTime = Time.getMSTime();
            owner.SendMessageToSet(spellGo, true);
        }

        owner.setMinion(this, true);

        if (!isTemporarySummon) {
            getCharmInfo().loadPetActionBar(petInfo.ActionBar);
        }

        map.AddToMap(getAsCreature());

        //set last used pet number (for use in BG's)
        if (owner.isPlayer() && isControlled() && !isTemporarySummoned() && (getPetType() == PetType.Summon || getPetType() == PetType.Hunter)) {
            owner.getAsPlayer().setLastPetNumber(petInfo.PetNumber);
        }

        var session = owner.getSession();
        var lastSaveTime = petInfo.LastSaveTime;
        var specializationId = petInfo.SpecializationId;

        owner.getSession().addQueryHolderCallback(DB.Characters.DelayQueryHolder(new PetLoadQueryHolder(ownerid, petInfo.PetNumber))).AfterComplete(holder -> {
            if (session.getPlayer() != owner || owner.getCurrentPet() != this) {
                return;
            }

            // passing previous checks ensure that 'this' is still valid
            if (removed) {
                return;
            }

            var timediff = (int)(GameTime.getGameTime() - lastSaveTime);
            loadAuras(holder.GetResult(PetLoginQueryLoad.Auras), holder.GetResult(PetLoginQueryLoad.AuraEffects), timediff);

            // load action bar, if data broken will fill later by default spells.
            if (!isTemporarySummon) {
                loadSpells(holder.GetResult(PetLoginQueryLoad.Spells));
                getSpellHistory().<Pet>loadFromDb(holder.GetResult(PetLoginQueryLoad.Cooldowns), holder.GetResult(PetLoginQueryLoad.Charges));
                learnPetPassives();
                initLevelupSpellsForLevel();

                if (getMap().isBattleArena()) {
                    removeArenaAuras();
                }

                castPetAuras(current);
            }

            Log.outDebug(LogFilter.Pet, String.format("New Pet has %1$s", getGUID().clone()));

            var specId = specializationId;
            var petSpec = CliDB.chrSpecializationStorage.LookupByKey(specId);

            if (petSpec != null) {
                specId = (short)Global.getDB2Mgr().getChrSpecializationByIndex(owner.hasAuraType(AuraType.OverridePetSpecs) ? PlayerClass.Max : 0, petSpec.OrderIndex).id;
            }

            setSpecialization(specId);

            // The SetSpecialization function will run these functions if the pet's spec is not 0
            if (getSpecialization() == 0) {
                cleanupActionBar(); // remove unknown spells from action bar after load

                owner.petSpellInitialize();
            }


            setGroupUpdateFlag(GroupUpdatePetFlags.Full);

            if (getPetType() == PetType.Hunter) {
                var result = holder.GetResult(PetLoginQueryLoad.DeclinedNames);

                if (!result.IsEmpty()) {
                    declinedname = new DeclinedName();

                    for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
                        declinedname.name.charAt(i) = result.<String>Read(i);
                    }
                }
            }

            // must be after SetMinion (owner guid check)
            loadTemplateImmunities();
            loading = false;
        });

        return true;
    }

    public final void savePetToDB(PetSaveMode mode) {
        if (getEntry() == 0) {
            return;
        }

        // save only fully controlled creature
        if (!isControlled()) {
            return;
        }

        // not save not player pets
        if (!getOwnerGUID().isPlayer()) {
            return;
        }

        var owner = getOwningPlayer();

        if (owner == null) {
            return;
        }

        // not save pet as current if another pet temporary unsummoned
        if (mode == PetSaveMode.AsCurrent && owner.getTemporaryUnsummonedPetNumber() != 0 && owner.getTemporaryUnsummonedPetNumber() != getCharmInfo().getPetNumber()) {
            // pet will lost anyway at restore temporary unsummoned
            if (getPetType() == PetType.Hunter) {
                return;
            }

            // for warlock case
            mode = PetSaveMode.NotInSlot;
        }


//ORIGINAL LINE: var curhealth = (uint)Health;
        var curhealth = (int)getHealth();
        var curmana = getPower(PowerType.Mana);

        SQLTransaction trans = new SQLTransaction();
        // save auras before possibly removing them
        saveAuras(trans);

        if (mode == PetSaveMode.AsCurrent) {
            var activeSlot = owner.getPetStable1().getCurrentActivePetIndex();

            if (activeSlot.getHasValue()) {
                mode = PetSaveMode.forValue(activeSlot);
            }
        }

        // stable and not in slot saves
        if (mode.getValue() < PetSaveMode.FirstActiveSlot.getValue() || mode.getValue() >= PetSaveMode.LastActiveSlot.getValue()) {
            removeAllAuras();
        }

        saveSpells(trans);
        getSpellHistory().<Pet>saveToDb(trans);
        DB.Characters.CommitTransaction(trans);

        // current/stable/not_in_slot
        if (mode != PetSaveMode.AsDeleted) {
            var ownerLowGUID = getOwnerGUID().getCounter();
            trans = new SQLTransaction();

            // remove current data
            var stmt = DB.Characters.GetPreparedStatement(CharStatements.DelCharPetById);
            stmt.AddValue(0, getCharmInfo().getPetNumber());
            trans.Append(stmt);

            // save pet
            var actionBar = generateActionBarData();

            fillPetInfo(owner.getPetStable1().getCurrentPet());

            stmt = DB.Characters.GetPreparedStatement(CharStatements.InsPet);
            stmt.AddValue(0, getCharmInfo().getPetNumber());
            stmt.AddValue(1, getEntry());
            stmt.AddValue(2, ownerLowGUID);
            stmt.AddValue(3, getNativeDisplayId());
            stmt.AddValue(4, getLevel());
            stmt.AddValue(5, unitData.petExperience);

//ORIGINAL LINE: stmt.AddValue(6, (byte)ReactState);
            stmt.AddValue(6, (byte)reactState.getValue());
            stmt.AddValue(7, (owner.getPetStable1().getCurrentActivePetIndex() != null ? (short)owner.getPetStable1().getCurrentActivePetIndex().intValue() : (short)PetSaveMode.NotInSlot.getValue()));
            stmt.AddValue(8, getName());
            stmt.AddValue(9, hasPetFlag(UnitPetFlags.CanBeRenamed) ? 0 : 1);
            stmt.AddValue(10, curhealth);
            stmt.AddValue(11, curmana);

            stmt.AddValue(12, actionBar);

            stmt.AddValue(13, GameTime.getGameTime());
            stmt.AddValue(14, unitData.createdBySpell);

//ORIGINAL LINE: stmt.AddValue(15, (byte)PetType);
            stmt.AddValue(15, (byte)getPetType().getValue());
            stmt.AddValue(16, getSpecialization());
            trans.Append(stmt);

            DB.Characters.CommitTransaction(trans);
        }
        // delete
        else {
            removeAllAuras();
            deleteFromDB(getCharmInfo().getPetNumber());
        }
    }

    public final void fillPetInfo(PetStable.PetInfo petInfo) {
        petInfo.petNumber = getCharmInfo().getPetNumber();
        petInfo.creatureId = getEntry();
        petInfo.displayId = getNativeDisplayId();

//ORIGINAL LINE: petInfo.Level = (byte)Level;
        petInfo.level = (byte)getLevel();
        petInfo.experience = unitData.petExperience;
        petInfo.reactState = reactState;
        petInfo.name = getName();
        petInfo.wasRenamed = !hasPetFlag(UnitPetFlags.CanBeRenamed);

//ORIGINAL LINE: petInfo.Health = (uint)Health;
        petInfo.health = (int)getHealth();

//ORIGINAL LINE: petInfo.Mana = (uint)GetPower(PowerType.Mana);
        petInfo.mana = (int)getPower(PowerType.Mana);
        petInfo.actionBar = generateActionBarData();

//ORIGINAL LINE: petInfo.LastSaveTime = (uint)GameTime.GetGameTime();
        petInfo.lastSaveTime = (int)GameTime.getGameTime();
        petInfo.createdBySpellId = unitData.createdBySpell;
        petInfo.type = getPetType();
        petInfo.specializationId = getSpecialization();
    }

    
//ORIGINAL LINE: public static void DeleteFromDB(uint petNumber)
    public static void deleteFromDB(int petNumber) {
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.Characters.GetPreparedStatement(CharStatements.DelCharPetById);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelCharPetDeclinedname);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetAuraEffects);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetAuras);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetSpells);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetSpellCooldowns);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetSpellCharges);
        stmt.AddValue(0, petNumber);
        trans.Append(stmt);

        DB.Characters.CommitTransaction(trans);
    }

    @Override
    public void setDeathState(DeathState s) {
        super.setDeathState(s);

        if (deathState == deathState.Corpse) {
            if (getPetType() == PetType.Hunter) {
                // pet corpse non lootable and non skinnable
                replaceAllDynamicFlags(UnitDynFlags.None);
                removeUnitFlag(UnitFlags.Skinnable);
            }
        } else if (deathState == deathState.Alive) {
            castPetAuras(true);
        }
    }

    
//ORIGINAL LINE: public override void Update(uint diff)
    @Override
    public void update(int diff) {
        if (removed) { // pet already removed, just wait in remove queue, no updates
            return;
        }

        if (loading) {
            return;
        }

        switch (deathState) {
            case Corpse: {
                if (getPetType() != PetType.Hunter || corpseRemoveTime <= GameTime.getGameTime()) {
                    remove(PetSaveMode.NotInSlot); //hunters' pets never get removed because of death, NEVER!

                    return;
                }

                break;
            }
            case Alive: {
                // unsummon pet that lost owner
                var owner = getOwningPlayer();

                if (owner == null || (!isWithinDistInMap(owner, getMap().getVisibilityRange()) && !isPossessed()) || (isControlled() && owner.getPetGUID().isEmpty())) {
                    remove(PetSaveMode.NotInSlot, true);

                    return;
                }

                if (isControlled()) {
                    if (game.entities.ObjectGuid.opNotEquals(owner.getPetGUID().clone(), getGUID().clone())) {
                        Log.outError(LogFilter.Pet, String.format("Pet %1$s is not pet of owner %2$s, removed", getEntry(), getOwningPlayer().getName()));
                        remove(PetSaveMode.NotInSlot);

                        return;
                    }
                }

                if (duration > 0) {
                    if (duration > diff) {
                        duration -= (int)diff;
                    } else {
                        remove(getPetType() != PetType.Summon ? PetSaveMode.AsDeleted : PetSaveMode.NotInSlot);

                        return;
                    }
                }

                //regenerate focus for hunter pets or energy for deathknight's ghoul
                if (focusRegenTimer != 0) {
                    if (focusRegenTimer > diff) {
                        focusRegenTimer -= diff;
                    } else {
                        switch (getDisplayPowerType()) {
                            case Focus:
                                regenerate(PowerType.Focus);
                                focusRegenTimer += PET_FOCUS_REGEN_INTERVAL - diff;

                                if (focusRegenTimer == 0) {
                                    ++focusRegenTimer;
                                }

                                // Reset if large diff (lag) causes focus to get 'stuck'
                                if (focusRegenTimer > PET_FOCUS_REGEN_INTERVAL) {
                                    focusRegenTimer = PET_FOCUS_REGEN_INTERVAL;
                                }

                                break;
                            default:
                                focusRegenTimer = 0;

                                break;
                        }
                    }
                }

                break;
            }
            default:
                break;
        }

        super.update(diff);
    }


    public final void remove(PetSaveMode mode) {
        remove(mode, false);
    }

    
//ORIGINAL LINE: public void Remove(PetSaveMode mode, bool returnreagent = false)
    public final void remove(PetSaveMode mode, boolean returnreagent) {
        getOwningPlayer().removePet(this, mode, returnreagent);
    }

    
//ORIGINAL LINE: public void GivePetXP(uint xp)
    public final void givePetXP(int xp) {
        if (getPetType() != PetType.Hunter) {
            return;
        }

        if (xp < 1) {
            return;
        }

        if (!isAlive()) {
            return;
        }

        var maxlevel = Math.min(WorldConfig.getUIntValue(WorldCfg.MaxPlayerLevel), getOwningPlayer().getLevel());
        var petlevel = getLevel();

        // If pet is detected to be at, or above(?) the players level, don't hand out XP
        if (petlevel >= maxlevel) {
            return;
        }


//ORIGINAL LINE: uint nextLvlXP = UnitData.PetNextLevelExperience;
        int nextLvlXP = unitData.petNextLevelExperience;

//ORIGINAL LINE: uint curXP = UnitData.PetExperience;
        int curXP = unitData.petExperience;
        var newXP = curXP + xp;

        // Check how much XP the pet should receive, and hand off have any left from previous levelups
        while (newXP >= nextLvlXP && petlevel < maxlevel) {
            // Subtract newXP from amount needed for nextlevel, and give pet the level
            newXP -= nextLvlXP;
            ++petlevel;

            givePetLevel((int)petlevel);

            nextLvlXP = unitData.petNextLevelExperience;
        }

        // Not affected by special conditions - give it new XP
        setPetExperience(petlevel < maxlevel ? newXP : 0);
    }

    public final void givePetLevel(int level) {
        if (level == 0 || level == getLevel()) {
            return;
        }

        if (getPetType() == PetType.Hunter) {
            setPetExperience(0);

//ORIGINAL LINE: SetPetNextLevelExperience((uint)(Global.ObjectMgr.GetXPForLevel((uint)level) * PetXPFactor));
            setPetNextLevelExperience((int)(Global.getObjectMgr().getXPForLevel((int)level) * PET_XPFACTOR));
        }


//ORIGINAL LINE: InitStatsForLevel((uint)level);
        initStatsForLevel((int)level);
        initLevelupSpellsForLevel();
    }

    public final boolean createBaseAtCreature(Creature creature) {
        if (!createBaseAtTamed(creature.getTemplate(), creature.getMap())) {
            return false;
        }

        location.relocate(creature.location);

        if (!location.isPositionValid()) {
            Log.outError(LogFilter.Pet, "Pet (guidlow {0}, entry {1}) not created base at creature. Suggested coordinates isn't valid (X: {2} Y: {3})", getGUID().toString(), getEntry(), location.x, location.y);

            return false;
        }

        var cinfo = getTemplate();

        if (cinfo == null) {
            Log.outError(LogFilter.Pet, "CreateBaseAtCreature() failed, creatureInfo is missing!");

            return false;
        }

        setDisplayId(creature.getDisplayId());
        var cFamily = CliDB.creatureFamilyStorage.LookupByKey(cinfo.family);

        if (cFamily != null) {
            setName(cFamily.Name.charAt(getOwningPlayer().getSession().getSessionDbcLocale()));
        } else {
            setName(creature.getName(Global.getWorldMgr().getDefaultDbcLocale()));
        }

        return true;
    }

    public final boolean createBaseAtCreatureInfo(CreatureTemplate cinfo, Unit owner) {
        if (!createBaseAtTamed(cinfo, owner.getMap())) {
            return false;
        }

        var cFamily = CliDB.creatureFamilyStorage.LookupByKey(cinfo.family);

        if (cFamily != null) {
            setName(cFamily.Name.charAt(getOwningPlayer().getSession().getSessionDbcLocale()));
        }

        location.relocate(owner.location);

        return true;
    }

    public final boolean haveInDiet(ItemTemplate item) {
        if (item.foodType == 0) {
            return false;
        }

        var cInfo = getTemplate();

        if (cInfo == null) {
            return false;
        }

        var cFamily = CliDB.creatureFamilyStorage.LookupByKey(cInfo.family);

        if (cFamily == null) {
            return false;
        }


//ORIGINAL LINE: uint diet = cFamily.PetFoodMask;
        int diet = cFamily.PetFoodMask;

//ORIGINAL LINE: var FoodMask = (uint)(1 << ((int)item.FoodType - 1));
        var foodMask = (int)(1 << ((int)item.foodType - 1));

        return diet.HasAnyFlag(foodMask);
    }

    
//ORIGINAL LINE: public bool LearnSpell(uint spellId)
    public final boolean learnSpell(int spellId) {
        // prevent duplicated entires in spell book
        if (!addSpell(spellId)) {
            return false;
        }

        if (!loading) {
            PetLearnedSpells packet = new PetLearnedSpells();
            packet.spells.add(spellId);
            getOwningPlayer().sendPacket(packet);
            getOwningPlayer().petSpellInitialize();
        }

        return true;
    }


    public final boolean removeSpell(int spellId, boolean learnPrev) {
        return removeSpell(spellId, learnPrev, true);
    }

    
//ORIGINAL LINE: public bool RemoveSpell(uint spellId, bool learnPrev, bool clearActionBar = true)

    public final boolean removeSpell(int spellId, boolean learnPrev, boolean clearActionBar) {
        var petSpell = spells.LookupByKey(spellId);

        if (petSpell == null) {
            return false;
        }

        if (petSpell.State == PetSpellState.Removed) {
            return false;
        }

        if (petSpell.State == PetSpellState.New) {
            spells.remove(spellId);
        } else {
            petSpell.State = PetSpellState.Removed;
        }

        removeAura(spellId);

        if (learnPrev) {
            var prevId = Global.getSpellMgr().getPrevSpellInChain(spellId);

            if (prevId != 0) {
                learnSpell(prevId);
            } else {
                learnPrev = false;
            }
        }

        // if remove last rank or non-ranked then update action bar at server and client if need
        if (clearActionBar && !learnPrev && getCharmInfo().removeSpellFromActionBar(spellId)) {
            if (!loading) {
                // need update action bar for last removed rank
                Unit owner = getOwningPlayer();

                if (owner) {
                    if (owner.isTypeId(TypeId.Player)) {
                        owner.getAsPlayer().petSpellInitialize();
                    }
                }
            }
        }

        return true;
    }

    public final void initPetCreateSpells() {
        getCharmInfo().initPetActionBar();
        spells.clear();

        learnPetPassives();
        initLevelupSpellsForLevel();

        castPetAuras(false);
    }

    public final void toggleAutocast(SpellInfo spellInfo, boolean apply) {
        if (!spellInfo.isAutocastable()) {
            return;
        }

        var petSpell = spells.LookupByKey(spellInfo.id);

        if (petSpell == null) {
            return;
        }

        var hasSpell = autospells.contains(spellInfo.id);

        if (apply) {
            if (!hasSpell) {
                autospells.add(spellInfo.id);

                if (petSpell.Active != ActiveStates.Enabled) {
                    petSpell.Active = ActiveStates.Enabled;

                    if (petSpell.State != PetSpellState.New) {
                        petSpell.State = PetSpellState.Changed;
                    }
                }
            }
        } else {
            if (hasSpell) {
                autospells.remove((Integer)spellInfo.id);

                if (petSpell.Active != ActiveStates.Disabled) {
                    petSpell.Active = ActiveStates.Disabled;

                    if (petSpell.State != PetSpellState.New) {
                        petSpell.State = PetSpellState.Changed;
                    }
                }
            }
        }
    }

    public final boolean isPermanentPetFor(Player owner) {
        switch (getPetType()) {
            case Summon:
                switch (owner.getClass()) {
                    case Warlock:
                        return getTemplate().creatureType == CreatureType.Demon;
                    case Deathknight:
                        return getTemplate().creatureType == CreatureType.Undead;
                    case Mage:
                        return getTemplate().creatureType == CreatureType.Elemental;
                    default:
                        return false;
                }
            case Hunter:
                return true;
            default:
                return false;
        }
    }

    
//ORIGINAL LINE: public bool Create(ulong guidlow, Map map, uint entry, uint petNumber)
    public final boolean create(long guidlow, Map map, int entry, int petNumber) {
        setMap(map);

        // TODO: counter should be constructed as (summon_count << 32) | petNumber
        create(ObjectGuid.create(HighGuid.Pet, map.getId(), entry, guidlow));

        spawnId = guidlow;
        originalEntry = entry;

        if (!initEntry(entry)) {
            return false;
        }

        // Force regen flag for player pets, just like we do for players themselves
        setUnitFlag2(UnitFlags2.RegeneratePower);
        setSheath(SheathState.Melee);

        getThreatManager().initialize();

        return true;
    }

    
//ORIGINAL LINE: public override bool HasSpell(uint spell)
    @Override
    public boolean hasSpell(int spell) {
        var petSpell = spells.LookupByKey(spell);

        return petSpell != null && petSpell.State != PetSpellState.Removed;
    }

    public final void castPetAura(PetAura aura) {
        var auraId = aura.getAura(getEntry());

        if (auraId == 0) {
            return;
        }

        CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlags.FullMask);

        if (auraId == 35696) { // Demonic Knowledge
            args.addSpellMod(SpellValueMod.BasePoint0, MathFunctions.CalculatePct(aura.getDamage(), getStat(Stats.Stamina) + getStat(Stats.Intellect)));
        }

        CastSpell(this, auraId, args);
    }

    public final void synchronizeLevelWithOwner() {
        Unit owner = getOwningPlayer();

        if (!owner || !owner.isTypeId(TypeId.Player)) {
            return;
        }

        switch (getPetType()) {
            // always same level
            case Summon:
            case Hunter:
                givePetLevel((int)owner.getLevel());

                break;
            default:
                break;
        }
    }


    @Override
    public void setDisplayId(int modelId) {
        setDisplayId(modelId, 1f);
    }

    
//ORIGINAL LINE: public override void SetDisplayId(uint modelId, float displayScale = 1f)

    @Override
    public void setDisplayId(int modelId, float displayScale) {
        super.setDisplayId(modelId, displayScale);

        if (!isControlled()) {
            return;
        }

        setGroupUpdateFlag(GroupUpdatePetFlags.ModelId);
    }

    
//ORIGINAL LINE: public override uint GetPetAutoSpellOnPos(byte pos)
    @Override
    public int getPetAutoSpellOnPos(byte pos) {
        if (pos >= autospells.size()) {
            return 0;
        } else {
            return autospells.get(pos);
        }
    }

    
//ORIGINAL LINE: public void SetDuration(uint dur)
    public final void setDuration(int dur) {
        duration = (int)dur;
    }

    
//ORIGINAL LINE: public void SetPetExperience(uint xp)
    public final void setPetExperience(int xp) {
        SetUpdateFieldValue(values.modifyValue(unitData).modifyValue(unitData.petExperience), xp);
    }

    
//ORIGINAL LINE: public void SetPetNextLevelExperience(uint xp)
    public final void setPetNextLevelExperience(int xp) {
        SetUpdateFieldValue(values.modifyValue(unitData).modifyValue(unitData.petNextLevelExperience), xp);
    }

    public final void resetGroupUpdateFlag() {
        mGroupUpdateMask = GroupUpdatePetFlags.None;

        if (getOwningPlayer().getGroup()) {
            getOwningPlayer().removeGroupUpdateFlag(GroupUpdateFlags.Pet);
        }
    }

    
//ORIGINAL LINE: public void SetSpecialization(uint spec)
    public final void setSpecialization(int spec) {
        if (petSpecialization == spec) {
            return;
        }

        // remove all the old spec's specalization spells, set the new spec, then add the new spec's spells
        // clearActionBars is false because we'll be updating the pet actionbar later so we don't have to do it now
        removeSpecializationSpells(false);

        if (!CliDB.chrSpecializationStorage.containsKey(spec)) {
            petSpecialization = 0;

            return;
        }


//ORIGINAL LINE: _petSpecialization = (ushort)spec;
        petSpecialization = (short)spec;
        learnSpecializationSpells();

        // resend SMSG_PET_SPELLS_MESSAGE to remove old specialization spells from the pet action bar
        cleanupActionBar();
        getOwningPlayer().petSpellInitialize();

        SetPetSpecialization setPetSpecialization = new SetPetSpecialization();
        setPetSpecialization.specID = petSpecialization;
        getOwningPlayer().sendPacket(setPetSpecialization);
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nPetType: %2$s PetNumber: %3$s", super.getDebugInfo(), getPetType(), getCharmInfo().getPetNumber());
    }

    public final DeclinedName getDeclinedNames() {
        return declinedname;
    }

    private boolean createBaseAtTamed(CreatureTemplate cinfo, Map map) {
        Log.outDebug(LogFilter.Pet, "CreateBaseForTamed");

        if (!create(map.generateLowGuid(HighGuid.Pet), map, cinfo.entry, Global.getObjectMgr().generatePetNumber())) {
            return false;
        }

        setPetNameTimestamp(0);
        setPetExperience(0);

//ORIGINAL LINE: SetPetNextLevelExperience((uint)(Global.ObjectMgr.GetXPForLevel(Level + 1) * PetXPFactor));
        setPetNextLevelExperience((int)(Global.getObjectMgr().getXPForLevel(getLevel() + 1) * PET_XPFACTOR));
        replaceAllNpcFlags(NPCFlags.None);
        replaceAllNpcFlags2(NPCFlags2.None);

        if (cinfo.creatureType == CreatureType.Beast) {
            setClass(PlayerClass.Warrior);
            setGender(Gender.None);
            setPowerType(PowerType.Focus);
            setSheath(SheathState.Melee);
            replaceAllPetFlags(UnitPetFlags.CanBeRenamed.getValue() | UnitPetFlags.CanBeAbandoned.getValue());
        }

        return true;
    }

    private void loadSpells(SQLResult result) {
        if (!result.IsEmpty()) {
            do {

//ORIGINAL LINE: AddSpell(result.Read<uint>(0), (ActiveStates)result.Read<byte>(1), PetSpellState.Unchanged);
                addSpell(result.<Integer>Read(0), ActiveStates.forValue(result.<Byte>Read(1)), PetSpellState.Unchanged);
            } while (result.NextRow());
        }
    }

    private void saveSpells(SQLTransaction trans) {
        for (var pair : spells.ToList()) {
            // prevent saving family passives to DB
            if (pair.Value.Type == PetSpellType.Family) {
                continue;
            }

            PreparedStatement stmt;

            switch (pair.Value.State) {
                case PetSpellState.Removed:
                    stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetSpellBySpell);
                    stmt.AddValue(0, getCharmInfo().getPetNumber());
                    stmt.AddValue(1, pair.Key);
                    trans.Append(stmt);

                    spells.remove(pair.Key);

                    continue;
                case PetSpellState.Changed:
                    stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetSpellBySpell);
                    stmt.AddValue(0, getCharmInfo().getPetNumber());
                    stmt.AddValue(1, pair.Key);
                    trans.Append(stmt);

                    stmt = DB.Characters.GetPreparedStatement(CharStatements.InsPetSpell);
                    stmt.AddValue(0, getCharmInfo().getPetNumber());
                    stmt.AddValue(1, pair.Key);

//ORIGINAL LINE: stmt.AddValue(2, (byte)pair.Value.Active);
                    stmt.AddValue(2, (byte)pair.Value.Active);
                    trans.Append(stmt);

                    break;
                case PetSpellState.New:
                    stmt = DB.Characters.GetPreparedStatement(CharStatements.InsPetSpell);
                    stmt.AddValue(0, getCharmInfo().getPetNumber());
                    stmt.AddValue(1, pair.Key);

//ORIGINAL LINE: stmt.AddValue(2, (byte)pair.Value.Active);
                    stmt.AddValue(2, (byte)pair.Value.Active);
                    trans.Append(stmt);

                    break;
                case PetSpellState.Unchanged:
                    continue;
            }

            pair.Value.State = PetSpellState.Unchanged;
        }
    }

    
//ORIGINAL LINE: void _LoadAuras(SQLResult auraResult, SQLResult effectResult, uint timediff)
    private void loadAuras(SQLResult auraResult, SQLResult effectResult, int timediff) {
        Log.outDebug(LogFilter.Pet, "Loading auras for {0}", getGUID().toString());

        ObjectGuid casterGuid = null;
        ObjectGuid itemGuid = null;
        HashMap<AuraKey, AuraLoadEffectInfo> effectInfo = new HashMap<AuraKey, AuraLoadEffectInfo>();

        if (!effectResult.IsEmpty()) {
            do {

//ORIGINAL LINE: int effectIndex = effectResult.Read<byte>(3);
                int effectIndex = effectResult.<Byte>Read(3);

//ORIGINAL LINE: casterGuid.SetRawValue(effectResult.Read<byte[]>(0));
                casterGuid.setRawValue(effectResult.<byte[]>Read(0));

                if (casterGuid.isEmpty()) {
                    casterGuid = getGUID().clone();
                }


//ORIGINAL LINE: AuraKey key = new(casterGuid, itemGuid, effectResult.Read<uint>(1), effectResult.Read<uint>(2));
                AuraKey key = new AuraKey(casterGuid.clone(), itemGuid.clone(), effectResult.<Integer>Read(1), effectResult.<Integer>Read(2));

                if (!effectInfo.containsKey(key)) {
                    effectInfo.put(key, new AuraLoadEffectInfo());
                }

                var info = effectInfo.get(key);
                info.amounts.put(effectIndex, effectResult.<Integer>Read(4));
                info.baseAmounts.put(effectIndex, effectResult.<Integer>Read(5));
            } while (effectResult.NextRow());
        }

        if (!auraResult.IsEmpty()) {
            do {
                // NULL guid stored - pet is the caster of the spell - see Pet._SaveAuras

//ORIGINAL LINE: casterGuid.SetRawValue(auraResult.Read<byte[]>(0));
                casterGuid.setRawValue(auraResult.<byte[]>Read(0));

                if (casterGuid.isEmpty()) {
                    casterGuid = getGUID().clone();
                }


//ORIGINAL LINE: AuraKey key = new(casterGuid, itemGuid, auraResult.Read<uint>(1), auraResult.Read<uint>(2));
                AuraKey key = new AuraKey(casterGuid.clone(), itemGuid.clone(), auraResult.<Integer>Read(1), auraResult.<Integer>Read(2));

//ORIGINAL LINE: var recalculateMask = auraResult.Read<uint>(3);
                var recalculateMask = auraResult.<Integer>Read(3);

//ORIGINAL LINE: var difficulty = (Difficulty)auraResult.Read<byte>(4);
                var difficulty = Difficulty.forValue(auraResult.<Byte>Read(4));

//ORIGINAL LINE: var stackCount = auraResult.Read<byte>(5);
                var stackCount = auraResult.<Byte>Read(5);
                var maxDuration = auraResult.<Integer>Read(6);
                var remainTime = auraResult.<Integer>Read(7);

//ORIGINAL LINE: var remainCharges = auraResult.Read<byte>(8);
                var remainCharges = auraResult.<Byte>Read(8);

                var spellInfo = Global.getSpellMgr().getSpellInfo(key.spellId, difficulty);

                if (spellInfo == null) {
                    Log.outError(LogFilter.Pet, "Pet._LoadAuras: Unknown aura (spellid {0}), ignore.", key.spellId);

                    continue;
                }

                if (difficulty != Difficulty.None && !CliDB.difficultyStorage.containsKey(difficulty)) {
                    Log.outError(LogFilter.Pet, String.format("Pet._LoadAuras: Unknown difficulty %1$s (spellid %2$s), ignore.", difficulty, key.spellId));

                    continue;
                }

                // negative effects should continue counting down after logout
                if (remainTime != -1 && (!spellInfo.isPositive() || spellInfo.hasAttribute(SpellAttr4.AuraExpiresOffline))) {
                    if (remainTime / Time.InMilliseconds <= timediff) {
                        continue;
                    }

                    remainTime -= (int)timediff * Time.InMilliseconds;
                }

                // prevent wrong values of remaincharges
                if (spellInfo.procCharges != 0) {
                    if (remainCharges <= 0) {

//ORIGINAL LINE: remainCharges = (byte)spellInfo.ProcCharges;
                        remainCharges = (byte)spellInfo.procCharges;
                    }
                } else {
                    remainCharges = 0;
                }

                var info = effectInfo.get(key);
                var castId = ObjectGuid.create(HighGuid.Cast, SpellCastSource.Normal, location.mapId, spellInfo.id, getMap().generateLowGuid(HighGuid.Cast));

                AuraCreateInfo createInfo = new AuraCreateInfo(castId.clone(), spellInfo, difficulty, system.Extentions.ExplodeMask(key.effectMask, SpellConst.getMaxEffects()), this);
                createInfo.setCasterGuid(casterGuid.clone());
                createInfo.setBaseAmount(info.baseAmounts);

                var aura = Aura.tryCreate(createInfo);

                if (aura != null) {
                    if (!aura.canBeSaved()) {
                        aura.remove();

                        continue;
                    }

                    aura.setLoadedState(maxDuration, remainTime, remainCharges, stackCount, recalculateMask, info.amounts);
                    aura.applyForTargets();
                    Log.outInfo(LogFilter.Pet, "Added aura spellid {0}, effectmask {1}", spellInfo.id, key.effectMask);
                }
            } while (auraResult.NextRow());
        }
    }

    private void saveAuras(SQLTransaction trans) {
        var stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetAuraEffects);
        stmt.AddValue(0, getCharmInfo().getPetNumber());
        trans.Append(stmt);

        stmt = DB.Characters.GetPreparedStatement(CharStatements.DelPetAuras);
        stmt.AddValue(0, getCharmInfo().getPetNumber());
        trans.Append(stmt);


//ORIGINAL LINE: byte index;
        byte index;

        for (var aura : getAuraQuery().canBeSaved().alsoMatches(a -> !isPetAura(a)).getResults()) {
            int recalculateMask;
            tangible.OutObject<Integer> tempOutRecalculateMask = new tangible.OutObject<Integer>();

//ORIGINAL LINE: var key = aura.GenerateKey(out var recalculateMask);
            var key = aura.generateKey(tempOutRecalculateMask);
            recalculateMask = tempOutRecalculateMask.outArgValue;

            // don't save guid of caster in case we are caster of the spell - guid for pet is generated every pet load, so it won't match saved guid anyways
            if (game.entities.ObjectGuid.opEquals(key.caster.clone(), getGUID().clone())) {
                key.caster.clear();
            }

            index = 0;
            stmt = DB.Characters.GetPreparedStatement(CharStatements.InsPetAura);
            stmt.AddValue(index++, getCharmInfo().getPetNumber());
            stmt.AddValue(index++, key.caster.getRawValue());
            stmt.AddValue(index++, key.spellId);
            stmt.AddValue(index++, key.effectMask);
            stmt.AddValue(index++, recalculateMask);

//ORIGINAL LINE: stmt.AddValue(index++, (byte)aura.CastDifficulty);
            stmt.AddValue(index++, (byte)aura.getCastDifficulty().getValue());
            stmt.AddValue(index++, aura.getStackAmount());
            stmt.AddValue(index++, aura.getMaxDuration());
            stmt.AddValue(index++, aura.getDuration());
            stmt.AddValue(index++, aura.getCharges());
            trans.Append(stmt);

            for (var effect : aura.getAuraEffects().entrySet()) {
                index = 0;
                stmt = DB.Characters.GetPreparedStatement(CharStatements.InsPetAuraEffect);
                stmt.AddValue(index++, getCharmInfo().getPetNumber());
                stmt.AddValue(index++, key.caster.getRawValue());
                stmt.AddValue(index++, key.spellId);
                stmt.AddValue(index++, key.effectMask);
                stmt.AddValue(index++, effect.getValue().EffIndex);
                stmt.AddValue(index++, effect.getValue().Amount);
                stmt.AddValue(index++, effect.getValue().BaseAmount);
                trans.Append(stmt);
            }
        }
    }


    private boolean addSpell(int spellId, ActiveStates active, PetSpellState state) {
        return addSpell(spellId, active, state, PetSpellType.Normal);
    }

    private boolean addSpell(int spellId, ActiveStates active) {
        return addSpell(spellId, active, PetSpellState.New, PetSpellType.Normal);
    }

    private boolean addSpell(int spellId) {
        return addSpell(spellId, ActiveStates.Decide, PetSpellState.New, PetSpellType.Normal);
    }

    
//ORIGINAL LINE: bool AddSpell(uint spellId, ActiveStates active = ActiveStates.Decide, PetSpellState state = PetSpellState.New, PetSpellType type = PetSpellType.Normal)

    private boolean addSpell(int spellId, ActiveStates active, PetSpellState state, PetSpellType type) {
        var spellInfo = Global.getSpellMgr().getSpellInfo(spellId, Difficulty.None);

        if (spellInfo == null) {
            // do pet spell book cleanup
            if (state == PetSpellState.Unchanged) { // spell load case
                Log.outError(LogFilter.Pet, "addSpell: Non-existed in SpellStore spell #{0} request, deleting for all pets in `pet_spell`.", spellId);

                var stmt = DB.Characters.GetPreparedStatement(CharStatements.DelInvalidPetSpell);

                stmt.AddValue(0, spellId);

                DB.Characters.Execute(stmt);
            } else {
                Log.outError(LogFilter.Pet, "addSpell: Non-existed in SpellStore spell #{0} request.", spellId);
            }

            return false;
        }

        var petSpell = spells.LookupByKey(spellId);

        if (petSpell != null) {
            if (petSpell.State == PetSpellState.Removed) {
                state = PetSpellState.Changed;
            } else {
                if (state == PetSpellState.Unchanged && petSpell.State != PetSpellState.Unchanged) {
                    // can be in case spell loading but learned at some previous spell loading
                    petSpell.State = PetSpellState.Unchanged;

                    if (active == ActiveStates.Enabled) {
                        toggleAutocast(spellInfo, true);
                    } else if (active == ActiveStates.Disabled) {
                        toggleAutocast(spellInfo, false);
                    }

                    return false;
                }
            }
        }

        PetSpell newspell = new PetSpell();
        newspell.state = state;
        newspell.type = type;

        if (active == ActiveStates.Decide) { // active was not used before, so we save it's autocast/passive state here
            if (spellInfo.isAutocastable()) {
                newspell.active = ActiveStates.Disabled;
            } else {
                newspell.active = ActiveStates.Passive;
            }
        } else {
            newspell.active = active;
        }

        // talent: unlearn all other talent ranks (high and low)
        if (spellInfo.isRanked()) {
            for (var pair : spells.entrySet()) {
                if (pair.getValue().State == PetSpellState.Removed) {
                    continue;
                }

                var oldRankSpellInfo = Global.getSpellMgr().GetSpellInfo(pair.getKey(), Difficulty.None);

                if (oldRankSpellInfo == null) {
                    continue;
                }

                if (spellInfo.isDifferentRankOf(oldRankSpellInfo)) {
                    // replace by new high rank
                    if (spellInfo.isHighRankOf(oldRankSpellInfo)) {
                        newspell.active = pair.getValue().Active;

                        if (newspell.active == ActiveStates.Enabled) {
                            toggleAutocast(oldRankSpellInfo, false);
                        }

                        unlearnSpell(pair.getKey(), false, false);

                        break;
                    }
                    // ignore new lesser rank
                    else {
                        return false;
                    }
                }
            }
        }

        spells.put(spellId, newspell);

        if (spellInfo.isPassive() && (spellInfo.casterAuraState == 0 || hasAuraState(spellInfo.casterAuraState))) {
            CastSpell(this, spellId, true);
        } else {
            getCharmInfo().addSpellToActionBar(spellInfo);
        }

        if (newspell.active == ActiveStates.Enabled) {
            toggleAutocast(spellInfo, true);
        }

        return true;
    }

    
//ORIGINAL LINE: void LearnSpells(List<uint> spellIds)
    private void learnSpells(ArrayList<Integer> spellIds) {
        PetLearnedSpells packet = new PetLearnedSpells();

        for (var spell : spellIds) {
            if (!addSpell(spell)) {
                continue;
            }

            packet.spells.add(spell);
        }

        if (!loading) {
            getOwningPlayer().sendPacket(packet);
        }
    }

    private void initLevelupSpellsForLevel() {
        var level = getLevel();
        var levelupSpells = getTemplate().family != 0 ? Global.getSpellMgr().getPetLevelupSpellList(getTemplate().family) : null;

        if (levelupSpells != null) {
            // PetLevelupSpellSet ordered by levels, process in reversed order
            for (var pair : levelupSpells.getKeyValueList()) {
                // will called first if level down
                if (pair.Key > level) {
                    unlearnSpell(pair.Value, true); // will learn prev rank if any
                }
                // will called if level up
                else {
                    learnSpell(pair.Value); // will unlearn prev rank if any
                }
            }
        }

        // default spells (can be not learned if pet level (as owner level decrease result for example) less first possible in normal game)
        var defSpells = Global.getSpellMgr().getPetDefaultSpellsEntry((int)getEntry());

        if (defSpells != null) {
            for (var spellId : defSpells.spellid) {
                var spellInfo = Global.getSpellMgr().getSpellInfo(spellId, Difficulty.None);

                if (spellInfo == null) {
                    continue;
                }

                // will called first if level down
                if (spellInfo.spellLevel > level) {
                    unlearnSpell(spellInfo.id, true);
                }
                // will called if level up
                else {
                    learnSpell(spellInfo.id);
                }
            }
        }
    }


    private boolean unlearnSpell(int spellId, boolean learnPrev) {
        return unlearnSpell(spellId, learnPrev, true);
    }

    
//ORIGINAL LINE: bool UnlearnSpell(uint spellId, bool learnPrev, bool clearActionBar = true)

    private boolean unlearnSpell(int spellId, boolean learnPrev, boolean clearActionBar) {
        if (removeSpell(spellId, learnPrev, clearActionBar)) {
            if (!loading) {
                PetUnlearnedSpells packet = new PetUnlearnedSpells();
                packet.spells.add(spellId);
                getOwningPlayer().sendPacket(packet);
            }

            return true;
        }

        return false;
    }

    
//ORIGINAL LINE: void UnlearnSpells(List<uint> spellIds, bool learnPrev, bool clearActionBar)
    private void unlearnSpells(ArrayList<Integer> spellIds, boolean learnPrev, boolean clearActionBar) {
        PetUnlearnedSpells packet = new PetUnlearnedSpells();

        for (var spell : spellIds) {
            if (!removeSpell(spell, learnPrev, clearActionBar)) {
                continue;
            }

            packet.spells.add(spell);
        }

        if (!loading) {
            getOwningPlayer().sendPacket(packet);
        }
    }

    private void cleanupActionBar() {

//ORIGINAL LINE: for (byte i = 0; i < SharedConst.ActionBarIndexMax; ++i)
        for (byte i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            var ab = getCharmInfo().getActionBarEntry(i);

            if (ab != null) {
                if (ab.getAction() != 0 && ab.isActionBarForSpell()) {
                    if (!hasSpell(ab.getAction())) {
                        getCharmInfo().setActionBar(i, 0, ActiveStates.Passive);
                    } else if (ab.getActiveState() == ActiveStates.Enabled) {
                        var spellInfo = Global.getSpellMgr().getSpellInfo(ab.getAction(), Difficulty.None);

                        if (spellInfo != null) {
                            toggleAutocast(spellInfo, true);
                        }
                    }
                }
            }
        }
    }

    // Get all passive spells in our skill line
    private void learnPetPassives() {
        var cInfo = getTemplate();

        if (cInfo == null) {
            return;
        }

        var cFamily = CliDB.creatureFamilyStorage.LookupByKey(cInfo.family);

        if (cFamily == null) {
            return;
        }

        var petStore = Global.getSpellMgr().petFamilySpellsStorage.LookupByKey(cInfo.family);

        if (petStore != null) {
            // For general hunter pets skill 270
            // Passive 01~10, Passive 00 (20782, not used), Ferocious Inspiration (34457)
            // Scale 01~03 (34902~34904, bonus from owner, not used)
            for (var spellId : petStore) {
                addSpell(spellId, ActiveStates.Decide, PetSpellState.New, PetSpellType.Family);
            }
        }
    }

    private void castPetAuras(boolean current) {
        var owner = getOwningPlayer();

        if (!isPermanentPetFor(owner)) {
            return;
        }

        for (var pa : owner.petAuras) {
            if (!current && pa.isRemovedOnChangePet()) {
                owner.removePetAura(pa);
            } else {
                castPetAura(pa);
            }
        }
    }

    private boolean isPetAura(Aura aura) {
        var owner = getOwningPlayer();

        // if the owner has that pet aura, return true
        for (var petAura : owner.petAuras) {
            if (petAura.getAura(getEntry()) == aura.getId()) {
                return true;
            }
        }

        return false;
    }

    
//ORIGINAL LINE: void LearnSpellHighRank(uint spellid)
    private void learnSpellHighRank(int spellid) {
        learnSpell(spellid);
        var next = Global.getSpellMgr().getNextSpellInChain(spellid);

        if (next != 0) {
            learnSpellHighRank(next);
        }
    }

    private void learnSpecializationSpells() {

//ORIGINAL LINE: List<uint> learnedSpells = new();
        ArrayList<Integer> learnedSpells = new ArrayList<Integer>();

        var specSpells = Global.getDB2Mgr().getSpecializationSpells(petSpecialization);

        if (specSpells != null) {
            for (var specSpell : specSpells) {
                var spellInfo = Global.getSpellMgr().getSpellInfo(specSpell.spellID, Difficulty.None);

                if (spellInfo == null || spellInfo.spellLevel > getLevel()) {
                    continue;
                }

                learnedSpells.add(specSpell.spellID);
            }
        }

        learnSpells(learnedSpells);
    }

    private void removeSpecializationSpells(boolean clearActionBar) {

//ORIGINAL LINE: List<uint> unlearnedSpells = new();
        ArrayList<Integer> unlearnedSpells = new ArrayList<Integer>();


//ORIGINAL LINE: for (uint i = 0; i < PlayerConst.MaxSpecializations; ++i)
        for (int i = 0; i < PlayerConst.MaxSpecializations; ++i) {
            var specialization = Global.getDB2Mgr().getChrSpecializationByIndex(0, i);

            if (specialization != null) {
                var specSpells = Global.getDB2Mgr().getSpecializationSpells(specialization.id);

                if (specSpells != null) {
                    for (var specSpell : specSpells) {
                        unlearnedSpells.add(specSpell.spellID);
                    }
                }
            }

            var specialization1 = Global.getDB2Mgr().getChrSpecializationByIndex(PlayerClass.Max, i);

            if (specialization1 != null) {
                var specSpells = Global.getDB2Mgr().getSpecializationSpells(specialization1.id);

                if (specSpells != null) {
                    for (var specSpell : specSpells) {
                        unlearnedSpells.add(specSpell.spellID);
                    }
                }
            }
        }

        unlearnSpells(unlearnedSpells, true, clearActionBar);
    }

    private String generateActionBarData() {
        StringBuilder ss = new StringBuilder();


//ORIGINAL LINE: for (byte i = SharedConst.ActionBarIndexStart; i < SharedConst.ActionBarIndexEnd; ++i)
        for (byte i = SharedConst.ActionBarIndexStart; i < SharedConst.ActionBarIndexEnd; ++i) {

//ORIGINAL LINE: ss.AppendFormat("{0} {1} ", (uint)GetCharmInfo().GetActionBarEntry(i).GetActiveState(), (uint)GetCharmInfo().GetActionBarEntry(i).GetAction());
            ss.append(String.format("%1$s %2$s ", (int)getCharmInfo().getActionBarEntry(i).getActiveState().getValue(), (int)getCharmInfo().getActionBarEntry(i).getAction()));
        }

        return ss.toString();
    }
}
