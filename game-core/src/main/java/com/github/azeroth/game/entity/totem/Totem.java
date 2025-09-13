package com.github.azeroth.game.entity.totem;

import Framework.Constants.*;
import com.github.azeroth.dbc.domain.SummonProperty;
import com.github.azeroth.game.entity.creature.Minion;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.packets.*;
import game.datastorage.*;
import game.spells.*;


public class Totem extends Minion {
    private TotemType totemType = TotemType.values()[0];
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _duration;
    private int duration;

    public Totem(SummonProperty propertiesRecord, Unit owner) {
        super(propertiesRecord, owner, false);
        unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.Totem.getValue());
        totemType = TotemType.Passive;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void Update(uint diff)
    @Override
    public void update(int diff) {
        if (!getOwnerUnit().isAlive() || !isAlive()) {
            unSummon(); // remove self

            return;
        }

        if (duration <= diff) {
            unSummon(); // remove self

            return;
        } else {
            duration -= diff;
        }

        super.update(diff);
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void InitStats(uint duration)
    @Override
    public void initStats(int duration) {
        // client requires SMSG_TOTEM_CREATED to be sent before adding to world and before removing old totem
        var owner = getOwnerUnit().getAsPlayer();

        if (owner) {
            if (summonProperty.slot >= SummonSlot.Totem.getValue() && summonProperty.slot < SharedConst.MaxTotemSlot) {
                TotemCreated packet = new TotemCreated();
                packet.totem = getGUID().clone();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: packet.Slot = (byte)(SummonPropertiesRecord.Slot - (int)Framework.Constants.SummonSlot.Totem);
                packet.slot = (byte) (summonProperty.slot - SummonSlot.Totem.getValue());
                packet.duration = duration;
                packet.spellID = unitData.createdBySpell;
                owner.getAsPlayer().sendPacket(packet);
            }

            // set display id depending on caster's race
            var totemDisplayId = Global.getSpellMgr().getModelForTotem(unitData.createdBySpell, owner.getRace());

            if (totemDisplayId != 0) {
                setDisplayId(totemDisplayId);
            } else {
                Log.outDebug(LogFilter.Misc, String.format("Totem with entry %1$s, does not have a specialized model for spell %2$s and race %3$s. Set to default.", getEntry(), unitData.createdBySpell, owner.getRace()));
            }
        }

        super.initStats(duration);

        // Get spell cast by totem
        var totemSpell = Global.getSpellMgr().getSpellInfo(getSpell(), getMap().getDifficultyID());

        if (totemSpell != null) {
            if (totemSpell.calcCastTime() != 0) { // If spell has cast time -> its an active totem
                totemType = TotemType.Active;
            }
        }

        this.duration = duration;
    }

    @Override
    public void initSummon() {
        if (totemType == TotemType.Passive && getSpell() != 0) {
            CastSpell(this, getSpell(), true);
        }

        // Some totems can have both instant effect and passive spell
        if (getSpell((byte) 1) != 0) {
            CastSpell(this, getSpell((byte) 1), true);
        }
    }

    @Override
    public void unSummon() {
        unSummon(TimeSpan.Zero);
    }

    @Override
    public void unSummon(TimeSpan msTime) {
        if (system.TimeSpan.opNotEquals(msTime, TimeSpan.Zero)) {
            events.AddEvent(new ForcedUnsummonDelayEvent(this), events.CalculateTime(msTime));

            return;
        }

        combatStop();
        removeAurasDueToSpell(getSpell(), getGUID().clone());

        // clear owner's totem slot
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (byte i = (int)Framework.Constants.SummonSlot.Totem; i < SharedConst.MaxTotemSlot; ++i)
        for (byte i = SummonSlot.Totem.getValue(); i < SharedConst.MaxTotemSlot; ++i) {
            if (game.entities.ObjectGuid.opEquals(getOwnerUnit().summonSlot[i].clone(), getGUID().clone())) {
                getOwnerUnit().summonSlot[i].clear();

                break;
            }
        }

        getOwnerUnit().removeAurasDueToSpell(getSpell(), getGUID().clone());

        // remove aura all party members too
        var owner = getOwnerUnit().getAsPlayer();

        if (owner != null) {
            owner.sendAutoRepeatCancel(this);

            var spell = Global.getSpellMgr().GetSpellInfo(unitData.createdBySpell, getMap().getDifficultyID());

            if (spell != null) {
                getSpellHistory().sendCooldownEvent(spell, 0, null, false);
            }

            var group = owner.getGroup();

            if (group) {
                for (var refe = group.getFirstMember(); refe != null; refe = refe.Next()) {
                    var target = refe.getSource();

                    if (target && target.isInMap(owner) && group.sameSubGroup(owner, target)) {
                        target.removeAurasDueToSpell(getSpell(), getGUID().clone());
                    }
                }
            }
        }

        addObjectToRemoveList();
    }


    @Override
    public boolean isImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster) {
        return isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster, false);
    }

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override bool IsImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster, bool requireImmunityPurgesEffectAttribute = false)
    @Override
    public boolean isImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster, boolean requireImmunityPurgesEffectAttribute) {
        // immune to all positive spells, except of stoneclaw totem absorb and sentry totem bind sight
        // totems positive spells have unit_caster target
        if (spellEffectInfo.effect != SpellEffectName.Dummy && spellEffectInfo.effect != SpellEffectName.ScriptEffect && spellInfo.isPositive() && spellEffectInfo.targetA.getTarget() != Targets.UnitCaster && spellEffectInfo.targetA.getCheckType() != SpellTargetCheckTypes.Entry) {
            return true;
        }

        switch (spellEffectInfo.applyAuraName) {
            case PeriodicDamage:
            case PeriodicLeech:
            case ModFear:
            case Transform:
                return true;
            default:
                break;
        }

        return super.isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster, requireImmunityPurgesEffectAttribute);
    }


    public final int getSpell() {
        return getSpell(0);
    }

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public uint GetSpell(byte slot = 0)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final int getSpell(byte slot) {
        return spells[slot];
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetTotemDuration()
    public final int getTotemDuration() {
        return duration;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void SetTotemDuration(uint duration)
    public final void setTotemDuration(int duration) {
        this.duration = duration;
    }

    public final TotemType getTotemType() {
        return totemType;
    }

    @Override
    public boolean updateStats(Stats stat) {
        return true;
    }

    @Override
    public boolean updateAllStats() {
        return true;
    }

    @Override
    public void updateResistances(SpellSchool school) {
    }

    @Override
    public void updateArmor() {
    }

    @Override
    public void updateMaxHealth() {
    }

    @Override
    public void updateMaxPower(PowerType power) {
    }


    @Override
    public void updateAttackPowerAndDamage() {
        updateAttackPowerAndDamage(false);
    }

    //C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override void UpdateAttackPowerAndDamage(bool ranged = false)
    @Override
    public void updateAttackPowerAndDamage(boolean ranged) {
    }

    @Override
    public void updateDamagePhysical(WeaponAttackType attType) {
    }
}