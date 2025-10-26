package com.github.azeroth.game.entity.creature;

import Framework.Constants.*;
import com.github.azeroth.dbc.domain.SummonProperty;
import com.github.azeroth.defines.WeaponAttackType;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.domain.unit.WeaponDamageRange;
import game.datastorage.*;


public class Guardian extends Minion {
    private static final int ENTRY_IMP = 416;
    private static final int ENTRY_VOIDWALKER = 1860;
    private static final int ENTRY_SUCCUBUS = 1863;
    private static final int ENTRY_FELHUNTER = 417;
    private static final int ENTRY_FELGUARD = 17252;
    private static final int ENTRY_WATER_ELEMENTAL = 510;
    private static final int ENTRY_TREANT = 1964;
    private static final int ENTRY_FIRE_ELEMENTAL = 15438;
    private static final int ENTRY_GHOUL = 26125;
    private static final int ENTRY_BLOODWORM = 28017;
    private final float[] statFromOwner = new float[Stats.Max.getValue()];

    private float bonusSpellDamage;

    public Guardian(SummonProperty propertiesRecord, Unit owner, boolean isWorldObject) {
        super(propertiesRecord, owner, isWorldObject);
        bonusSpellDamage = 0;

        unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.Guardian.getValue());

        if (propertiesRecord != null && (propertiesRecord.title == SummonTitle.Pet || propertiesRecord.control == SummonCategory.Pet)) {
            unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.ControlableGuardian.getValue());
            initCharmInfo();
        }
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void InitStats(uint duration)
    @Override
    public void initStats(int duration) {
        super.initStats(duration);

        initStatsForLevel(getOwnerUnit().getLevel());

        if (getOwnerUnit().isTypeId(TypeId.Player) && hasUnitTypeMask(unitTypeMask.ControlableGuardian)) {
            getCharmInfo().initCharmCreateSpells();
        }

        reactState = ReactStates.Aggressive;
    }

    @Override
    public void initSummon() {
        super.initSummon();

        if (getOwnerUnit().isTypeId(TypeId.Player) && game.entities.ObjectGuid.opEquals(getOwnerUnit().getMinionGUID().clone(), getGUID().clone()) && getOwnerUnit().getCharmedGUID().isEmpty()) {
            getOwnerUnit().getAsPlayer().charmSpellInitialize();
        }
    }

    // @todo Move stat mods code to pet passive auras
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public bool InitStatsForLevel(uint petlevel)
    public final boolean initStatsForLevel(int petlevel) {
        var cinfo = getCreatureTemplate();

        setLevel(petlevel);

        //Determine pet type
        var petType = PetType.Max;

        if (isPet() && getOwnerUnit().isTypeId(TypeId.Player)) {
            if (getOwnerUnit().getUnitClass() == PlayerClass.Warlock || getOwnerUnit().getUnitClass() == PlayerClass.Shaman || getOwnerUnit().getUnitClass() == PlayerClass.Deathknight) { // Risen Ghoul
                petType = PetType.Summon;
            } else if (getOwnerUnit().getUnitClass() == PlayerClass.Hunter) {
                petType = PetType.Hunter;
                unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.HunterPet.getValue());
            } else {
                Log.outError(LogFilter.Unit, "Unknown type pet {0} is summoned by player class {1}", getEntry(), getOwnerUnit().getUnitClass());
            }
        }
        var creatureID = (petType == PetType.Hunter) ? 1 : cinfo.entry;

        setMeleeDamageSchool(SpellSchool.forValue(cinfo.dmgSchool));

        setStatFlatModifier(UnitMods.Armor, UnitModifierFlatType.Base, (float) petlevel * 50);

        setBaseAttackTime(WeaponAttackType.BaseAttack, SharedConst.BaseAttackTime);
        setBaseAttackTime(WeaponAttackType.OffAttack, SharedConst.BaseAttackTime);
        setBaseAttackTime(WeaponAttackType.RangedAttack, SharedConst.BaseAttackTime);

        //scale
        setObjectScale(getNativeObjectScale());

        // Resistance
        // Hunters pet should not inherit resistances from creature_template, they have separate auras for that
        if (!isHunterPet()) {
            for (var i = SpellSchool.Holy.getValue(); i < SpellSchool.Max.getValue(); ++i) {
                setStatFlatModifier(UnitMods.ResistanceStart + i, UnitModifierFlatType.Base, cinfo.resistance[i]);
            }
        }

        // Health, Mana or Power, Armor
        var pInfo = Global.getObjectMgr().getPetLevelInfo(creatureID, petlevel);

        if (pInfo != null) { // exist in DB
            setCreateHealth(pInfo.health);
            setCreateMana(pInfo.mana);

            if (pInfo.armor > 0) {
                setStatFlatModifier(UnitMods.Armor, UnitModifierFlatType.Base, pInfo.armor);
            }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (byte stat = 0; stat < (int)Stats.Max; ++stat)
            for (byte stat = 0; stat < Stats.Max.getValue(); ++stat) {
                setCreateStat(Stats.forValue(stat), pInfo.stats[stat]);
            }
        } else { // not exist in DB, use some default fake data
            // remove elite bonuses included in DB values
            var stats = Global.getObjectMgr().getCreatureBaseStats(petlevel, cinfo.unitClass);
            applyLevelScaling();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: SetCreateHealth((uint)(Global.DB2Mgr.EvaluateExpectedStat(ExpectedStatType.CreatureHealth, petlevel, cinfo.GetHealthScalingExpansion(), UnitData.ContentTuningID, (PlayerClass)cinfo.UnitClass) * cinfo.ModHealth * cinfo.ModHealthExtra * GetHealthMod(cinfo.Rank)));
            setCreateHealth((int) (Global.getDB2Mgr().evaluateExpectedStat(ExpectedStatType.CreatureHealth, petlevel, cinfo.healthScalingExpansion, unitData.contentTuningID, PlayerClass.forValue(cinfo.unitClass)) * cinfo.modHealth * cinfo.modHealthExtra * getHealthMod(cinfo.rank)));
            setCreateMana(stats.generateMana(cinfo));

            setCreateStat(Stats.Strength, 22);
            setCreateStat(Stats.Agility, 22);
            setCreateStat(Stats.Stamina, 25);
            setCreateStat(Stats.Intellect, 28);
        }

        // Power
        if (petType == PetType.Hunter) { // Hunter pets have focus
            setPowerType(PowerType.Focus);
        } else if (isPetGhoul() || isPetAbomination()) { // DK pets have energy
            setPowerType(PowerType.Energy);
            setFullPower(PowerType.Energy);
        } else if (isPetImp() || isPetFelhunter() || isPetVoidwalker() || isPetSuccubus() || isPetDoomguard() || isPetFelguard()) { // Warlock pets have energy (since 5.x)
            setPowerType(PowerType.Energy);
        } else {
            setPowerType(PowerType.Mana);
        }

        // Damage
        setBonusDamage(0);

        switch (petType) {
            case Summon: {
                // the damage bonus used for pets is either fire or shadow damage, whatever is higher
                var fire = getOwnerUnit().getAsPlayer().activePlayerData.modDamageDonePos.get(SpellSchool.Fire.getValue());
                var shadow = getOwnerUnit().getAsPlayer().activePlayerData.modDamageDonePos.get(SpellSchool.Shadow.getValue());
                var val = (fire > shadow) ? fire : shadow;

                if (val < 0) {
                    val = 0;
                }

                setBonusDamage((int) (val * 0.15f));

                setBaseWeaponDamage(WeaponAttackType.BASE_ATTACK, WeaponDamageRange.MIN_DAMAGE, petlevel - (petlevel / 4));
                setBaseWeaponDamage(WeaponAttackType.BASE_ATTACK, WeaponDamageRange.MAX_DAMAGE, petlevel + (petlevel / 4));

                break;
            }
            case Hunter: {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: AsPet.SetPetNextLevelExperience((uint)(Global.ObjectMgr.GetXPForLevel(petlevel) * 0.05f));
                getAsPet().setPetNextLevelExperience((int) (Global.getObjectMgr().getXPForLevel(petlevel) * 0.05f));
                //these formula may not be correct; however, it is designed to be close to what it should be
                //this makes dps 0.5 of pets level
                setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel - (petlevel / 4));
                //damage range is then petlevel / 2
                setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel + (petlevel / 4));

                //damage is increased afterwards as strength and pet scaling modify attack power
                break;
            }
            default: {
                switch (getEntry()) {
                    case 510: { // mage Water Elemental
                        setBonusDamage((int) (getOwnerUnit().spellBaseDamageBonusDone(SpellSchoolMask.Frost) * 0.33f));

                        break;
                    }
                    case 1964: { //force of nature
                        if (pInfo == null) {
                            setCreateHealth(30 + 30 * petlevel);
                        }

                        var bonusDmg = getOwnerUnit().spellBaseDamageBonusDone(SpellSchoolMask.Nature) * 0.15f;
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel * 2.5f - ((float) petlevel / 2) + bonusDmg);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel * 2.5f + ((float) petlevel / 2) + bonusDmg);

                        break;
                    }
                    case 15352: { //earth elemental 36213
                        if (pInfo == null) {
                            setCreateHealth(100 + 120 * petlevel);
                        }

                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel - (petlevel / 4));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel + (petlevel / 4));

                        break;
                    }
                    case 15438: { //fire elemental
                        if (pInfo == null) {
                            setCreateHealth(40 * petlevel);
                            setCreateMana(28 + 10 * petlevel);
                        }

                        setBonusDamage((int) (getOwnerUnit().spellBaseDamageBonusDone(SpellSchoolMask.Fire) * 0.5f));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel * 4 - petlevel);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel * 4 + petlevel);

                        break;
                    }
                    case 19668: { // Shadowfiend
                        if (pInfo == null) {
                            setCreateMana(28 + 10 * petlevel);
                            setCreateHealth(28 + 30 * petlevel);
                        }

                        var bonusDmg = (int) (getOwnerUnit().spellBaseDamageBonusDone(SpellSchoolMask.Shadow) * 0.3f);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, (petlevel * 4 - petlevel) + bonusDmg);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, (petlevel * 4 + petlevel) + bonusDmg);

                        break;
                    }
                    case 19833: { //Snake Trap - Venomous Snake
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, (petlevel / 2) - 25);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, (petlevel / 2) - 18);

                        break;
                    }
                    case 19921: { //Snake Trap - Viper
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel / 2 - 10);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel / 2);

                        break;
                    }
                    case 29264: { // Feral Spirit
                        if (pInfo == null) {
                            setCreateHealth(30 * petlevel);
                        }

                        // wolf attack speed is 1.5s
                        setBaseAttackTime(WeaponAttackType.BaseAttack, cinfo.baseAttackTime);

                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, (petlevel * 4 - petlevel));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, (petlevel * 4 + petlevel));

                        setStatFlatModifier(UnitMods.Armor, UnitModifierFlatType.Base, getOwnerUnit().getArmor() * 0.35f); // Bonus Armor (35% of player armor)
                        setStatFlatModifier(UnitMods.StatStamina, UnitModifierFlatType.Base, getOwnerUnit().getStat(Stats.Stamina) * 0.3f); // Bonus Stamina (30% of player stamina)

                        if (!HasAura(58877)) { //prevent apply twice for the 2 wolves
                            addAura(58877, this); //Spirit Hunt, passive, Spirit Wolves' attacks heal them and their master for 150% of damage done.
                        }

                        break;
                    }
                    case 31216: { // Mirror Image
                        setBonusDamage((int) (getOwnerUnit().spellBaseDamageBonusDone(SpellSchoolMask.Frost) * 0.33f));
                        setDisplayId(getOwnerUnit().getDisplayId());

                        if (pInfo == null) {
                            setCreateMana(28 + 30 * petlevel);
                            setCreateHealth(28 + 10 * petlevel);
                        }

                        break;
                    }
                    case 27829: { // Ebon Gargoyle
                        if (pInfo == null) {
                            setCreateMana(28 + 10 * petlevel);
                            setCreateHealth(28 + 30 * petlevel);
                        }

                        setBonusDamage((int) (getOwnerUnit().getTotalAttackPowerValue(WeaponAttackType.BaseAttack) * 0.5f));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel - (petlevel / 4));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel + (petlevel / 4));

                        break;
                    }
                    case 28017: { // Bloodworms
                        setCreateHealth(4 * petlevel);
                        setBonusDamage((int) (getOwnerUnit().getTotalAttackPowerValue(WeaponAttackType.BaseAttack) * 0.006f));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, petlevel - 30 - (petlevel / 4));
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, petlevel - 30 + (petlevel / 4));

                        break;
                    }
                    default: {
                        /* ToDo: Check what 5f5d2028 broke/fixed and how much of Creature::UpdateLevelDependantStats()
                         * should be copied here (or moved to another method or if that function should be called here
                         * or not just for this default case)
                         */
                        var basedamage = getBaseDamageForLevel(petlevel);

                        var weaponBaseMinDamage = basedamage;
                        var weaponBaseMaxDamage = basedamage * 1.5f;

                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage, weaponBaseMinDamage);
                        setBaseWeaponDamage(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage, weaponBaseMaxDamage);

                        break;
                    }
                }

                break;
            }
        }

        updateAllStats();

        setFullHealth();
        setFullPower(PowerType.Mana);

        return true;
    }

    @Override
    public boolean updateStats(Stats stat) {
        var value = getTotalStatValue(stat);
        updateStatBuffMod(stat);
        var ownersBonus = 0.0f;

        var owner = getOwnerUnit();
        // Handle Death Knight Glyphs and Talents
        var mod = 0.75f;

        if (isPetGhoul() && (stat == Stats.Stamina || stat == Stats.Strength)) {
            switch (stat) {
                case Stamina:
                    mod = 0.3f;

                    break; // Default Owner's Stamina scale
                case Strength:
                    mod = 0.7f;

                    break; // Default Owner's Strength scale
                default:
                    break;
            }

            ownersBonus = owner.getStat(stat) * mod;
            value += ownersBonus;
        } else if (stat == Stats.Stamina) {
            ownersBonus = MathUtil.CalculatePct(owner.getStat(Stats.Stamina), 30);
            value += ownersBonus;
        }
        //warlock's and mage's pets gain 30% of owner's intellect
        else if (stat == Stats.Intellect) {
            if (owner.getUnitClass() == PlayerClass.Warlock || owner.getUnitClass() == PlayerClass.Mage) {
                ownersBonus = MathUtil.CalculatePct(owner.getStat(stat), 30);
                value += ownersBonus;
            }
        }

        setStat(stat, (int) value);
        statFromOwner[stat.getValue()] = ownersBonus;
        updateStatBuffMod(stat);

        switch (stat) {
            case Strength:
                updateAttackPowerAndDamage();

                break;
            case Agility:
                updateArmor();

                break;
            case Stamina:
                updateMaxHealth();

                break;
            case Intellect:
                updateMaxPower(PowerType.Mana);

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean updateAllStats() {
        updateMaxHealth();

        for (var i = Stats.Strength; i.getValue() < Stats.Max.getValue(); ++i) {
            updateStats(i);
        }

        for (var i = PowerType.Mana; i.getValue() < PowerType.Max.getValue(); ++i) {
            updateMaxPower(i);
        }

        updateAllResistances();

        return true;
    }

    @Override
    public void updateResistances(SpellSchool school) {
        if (school.getValue() > SpellSchool.Normal.getValue()) {
            var baseValue = getFlatModifierValue(UnitMods.ResistanceStart + school.getValue(), UnitModifierFlatType.Base);
            var bonusValue = getTotalAuraModValue(UnitMods.ResistanceStart + school.getValue()) - baseValue;

            // hunter and warlock pets gain 40% of owner's resistance
            if (isPet()) {
                baseValue += MathUtil.CalculatePct(owner.getResistance(school), 40);
                bonusValue += MathUtil.CalculatePct(owner.getBonusResistanceMod(school), 40);
            }

            setResistance(school, (int) baseValue);
            setBonusResistanceMod(school, (int) bonusValue);
        } else {
            updateArmor();
        }
    }

    @Override
    public void updateArmor() {
        var bonusArmor = 0.0f;
        var unitMod = UnitMods.Armor;

        // hunter pets gain 35% of owner's armor value, warlock pets gain 100% of owner's armor
        if (isHunterPet()) {
            bonusArmor = MathUtil.CalculatePct(getOwnerUnit().getArmor(), 70);
        } else if (isPet()) {
            bonusArmor = getOwnerUnit().getArmor();
        }

        var value = getFlatModifierValue(unitMod, UnitModifierFlatType.Base);
        var baseValue = value;
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Base);
        value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total) + bonusArmor;
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

        setArmor((int) baseValue, (int) (value - baseValue));
    }

    @Override
    public void updateMaxHealth() {
        var unitMod = UnitMods.Health;
        var stamina = getStat(Stats.Stamina) - getCreateStat(Stats.Stamina);

        float multiplicator;

        switch (getEntry()) {
            case ENTRY_IMP:
                multiplicator = 8.4f;

                break;
            case ENTRY_VOIDWALKER:
                multiplicator = 11.0f;

                break;
            case ENTRY_SUCCUBUS:
                multiplicator = 9.1f;

                break;
            case ENTRY_FELHUNTER:
                multiplicator = 9.5f;

                break;
            case ENTRY_FELGUARD:
                multiplicator = 11.0f;

                break;
            case ENTRY_BLOODWORM:
                multiplicator = 1.0f;

                break;
            default:
                multiplicator = 10.0f;

                break;
        }

        var value = getFlatModifierValue(unitMod, UnitModifierFlatType.Base) + getCreateHealth();
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Base);
        value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total) + stamina * multiplicator;
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: SetMaxHealth((uint)value);
        setMaxHealth((int) value);
    }

    @Override
    public void updateMaxPower(PowerType power) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: if (GetPowerIndex(power) == (uint)PowerType.Max)
        if (getPowerIndex(power) == (int) PowerType.Max.getValue()) {
            return;
        }

        var unitMod = UnitMods.PowerStart + power.getValue();

        var value = getFlatModifierValue(unitMod, UnitModifierFlatType.Base) + getCreatePowerValue(power);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Base);
        value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

        setMaxPower(power, (int) value);
    }


    @Override
    public void updateAttackPowerAndDamage() {
        updateAttackPowerAndDamage(false);
    }

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override void UpdateAttackPowerAndDamage(bool ranged = false)
    @Override
    public void updateAttackPowerAndDamage(boolean ranged) {
        if (ranged) {
            return;
        }

        float val;
        double bonusAP = 0.0f;
        var unitMod = UnitMods.AttackPower;

        if (getEntry() == ENTRY_IMP) { // imp's attack power
            val = getStat(Stats.Strength) - 10.0f;
        } else {
            val = 2 * getStat(Stats.Strength) - 20.0f;
        }

        var owner = getOwnerUnit() ? getOwnerUnit().getAsPlayer() : null;

        if (owner != null) {
            if (isHunterPet()) { //hunter pets benefit from owner's attack power
                var mod = 1.0f; //Hunter contribution modifier
                bonusAP = owner.getTotalAttackPowerValue(WeaponAttackType.RangedAttack) * 0.22f * mod;
                setBonusDamage((int) (owner.getTotalAttackPowerValue(WeaponAttackType.RangedAttack) * 0.1287f * mod));
            } else if (isPetGhoul()) { //ghouls benefit from deathknight's attack power (may be summon pet or not)
                bonusAP = owner.getTotalAttackPowerValue(WeaponAttackType.BaseAttack) * 0.22f;
                setBonusDamage((int) (owner.getTotalAttackPowerValue(WeaponAttackType.BaseAttack) * 0.1287f));
            } else if (isSpiritWolf()) { //wolf benefit from shaman's attack power
                var dmgMultiplier = 0.31f;
                bonusAP = owner.getTotalAttackPowerValue(WeaponAttackType.BaseAttack) * dmgMultiplier;
                setBonusDamage((int) (owner.getTotalAttackPowerValue(WeaponAttackType.BaseAttack) * dmgMultiplier));
            }
            //demons benefit from warlocks shadow or fire damage
            else if (isPet()) {
                var fire = owner.activePlayerData.modDamageDonePos.get(SpellSchool.Fire.getValue()) - owner.activePlayerData.modDamageDoneNeg.get(SpellSchool.Fire.getValue());
                var shadow = owner.activePlayerData.modDamageDonePos.get(SpellSchool.Shadow.getValue()) - owner.activePlayerData.modDamageDoneNeg.get(SpellSchool.Shadow.getValue());
                var maximum = (fire > shadow) ? fire : shadow;

                if (maximum < 0) {
                    maximum = 0;
                }

                setBonusDamage((int) (maximum * 0.15f));
                bonusAP = maximum * 0.57f;
            }
            //water elementals benefit from mage's frost damage
            else if (getEntry() == ENTRY_WATER_ELEMENTAL) {
                var frost = owner.activePlayerData.modDamageDonePos.get(SpellSchool.Frost.getValue()) - owner.activePlayerData.modDamageDoneNeg.get(SpellSchool.Frost.getValue());

                if (frost < 0) {
                    frost = 0;
                }

                setBonusDamage((int) (frost * 0.4f));
            }
        }

        setStatFlatModifier(UnitMods.AttackPower, UnitModifierFlatType.Base, val + bonusAP);

        //in BASE_VALUE of UNIT_MOD_ATTACK_POWER for creatures we store data of meleeattackpower field in DB
        var baseAttPower = getFlatModifierValue(unitMod, UnitModifierFlatType.Base) * getPctModifierValue(unitMod, UnitModifierPctType.Base);
        var attPowerMultiplier = getPctModifierValue(unitMod, UnitModifierPctType.Total) - 1.0f;

        setAttackPower((int) baseAttPower);
        setAttackPowerMultiplier((float) attPowerMultiplier);

        //automatically update weapon damage after attack power modification
        updateDamagePhysical(WeaponAttackType.BaseAttack);
    }

    @Override
    public void updateDamagePhysical(WeaponAttackType attType) {
        if (attType.getValue() > WeaponAttackType.BaseAttack.getValue()) {
            return;
        }

        var bonusDamage = 0.0f;
        var playerOwner = owner.getAsPlayer();

        if (playerOwner != null) {
            //force of nature
            if (getEntry() == ENTRY_TREANT) {
                var spellDmg = playerOwner.activePlayerData.modDamageDonePos.get(SpellSchool.Nature.getValue()) - playerOwner.activePlayerData.modDamageDoneNeg.get(SpellSchool.Nature.getValue());

                if (spellDmg > 0) {
                    bonusDamage = spellDmg * 0.09f;
                }
            }
            //greater fire elemental
            else if (getEntry() == ENTRY_FIRE_ELEMENTAL) {
                var spellDmg = playerOwner.activePlayerData.modDamageDonePos.get(SpellSchool.Fire.getValue()) - playerOwner.activePlayerData.modDamageDoneNeg.get(SpellSchool.Fire.getValue());

                if (spellDmg > 0) {
                    bonusDamage = spellDmg * 0.4f;
                }
            }
        }

        var unitMod = UnitMods.DamageMainHand;

        double attSpeed = getBaseAttackTime(WeaponAttackType.BaseAttack) / 1000.0f;

        var baseValue = getFlatModifierValue(unitMod, UnitModifierFlatType.Base) + getTotalAttackPowerValue(attType, false) / 3.5f * attSpeed + bonusDamage;
        var basePct = getPctModifierValue(unitMod, UnitModifierPctType.Base);
        var totalValue = getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
        var totalPct = getPctModifierValue(unitMod, UnitModifierPctType.Total);

        var weaponMindamage = getWeaponDamageRange(WeaponAttackType.BaseAttack, WeaponDamageRange.MinDamage);
        var weaponMaxdamage = getWeaponDamageRange(WeaponAttackType.BaseAttack, WeaponDamageRange.MaxDamage);

        var mindamage = ((baseValue + weaponMindamage) * basePct + totalValue) * totalPct;
        var maxdamage = ((baseValue + weaponMaxdamage) * basePct + totalValue) * totalPct;

        SetUpdateFieldStatValue(values.modifyValue(unitData).modifyValue(unitData.minDamage), (float) mindamage);
        SetUpdateFieldStatValue(values.modifyValue(unitData).modifyValue(unitData.maxDamage), (float) maxdamage);
    }

    public final float getBonusDamage() {
        return bonusSpellDamage;
    }

    private void setBonusDamage(float damage) {
        bonusSpellDamage = damage;
        var playerOwner = getOwnerUnit().getAsPlayer();

        if (playerOwner != null) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: playerOwner.SetPetSpellPower((uint)damage);
            playerOwner.setPetSpellPower((int) damage);
        }
    }

    public final float getBonusStatFromOwner(Stats stat) {
        return statFromOwner[stat.getValue()];
    }
}