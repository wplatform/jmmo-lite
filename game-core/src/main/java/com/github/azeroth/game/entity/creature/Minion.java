package com.github.azeroth.game.entity.creature;


import com.github.azeroth.dbc.domain.SummonProperty;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.domain.unit.UnitTypeMask;

public class Minion extends TempSummon {
    protected Unit owner;
    private float followAngle;

    public Minion(SummonProperty propertiesRecord, Unit owner, boolean isWorldObject) {
        super(propertiesRecord, owner, isWorldObject);
        owner = owner;
        setUnitTypeMask(UnitTypeMask.forValue(getUnitTypeMask().getValue() | getUnitTypeMask().minion.getValue()));
        followAngle = SharedConst.PetFollowAngle;
        /** @todo: Find correct way
         */
        initCharmInfo();
    }

    public final boolean isGuardianPet() {
        return isPet() || (summonProperty != null && summonProperty.Control == SummonCategory.pet);
    }

    @Override
    public float getFollowAngle() {
        return followAngle;
    }

    public final void setFollowAngle(float angle) {
        followAngle = angle;
    }

    @Override
    public Unit getOwnerUnit() {
        return owner;
    }

    @Override
    public void initStats(int duration) {
        super.initStats(duration);

        setReactState(ReactStates.Passive);

        setCreatorGUID(getOwnerUnit().getGUID());
        setFaction(getOwnerUnit().getFaction()); // TODO: Is this correct? Overwrite the use of SummonPropertiesFlags::UseSummonerFaction

        getOwnerUnit().setMinion(this, true);
    }

    @Override
    public void removeFromWorld() {
        if (!isInWorld()) {
            return;
        }

        getOwnerUnit().setMinion(this, false);
        super.removeFromWorld();
    }

    @Override
    public void setDeathState(DeathState s) {
        super.setDeathState(s);

        if (s != deathState.JustDied || !isGuardianPet()) {
            return;
        }

        var owner = getOwnerUnit();

        if (owner == null || !owner.isPlayer() || ObjectGuid.opNotEquals(owner.getMinionGUID(), getGUID())) {
            return;
        }

        for (var controlled : owner.getControlled()) {
            if (controlled.getEntry() == getEntry() && controlled.isAlive()) {
                owner.setMinionGUID(controlled.getGUID());
                owner.setPetGUID(controlled.getGUID());
                owner.toPlayer().charmSpellInitialize();

                break;
            }
        }
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nOwner: %2$s", super.getDebugInfo(), (getOwnerUnit() ? getOwnerUnit().getGUID() : ""));
    }

    // Warlock pets
    public final boolean isPetImp() {
        return getEntry() == (int) PetEntry.Imp.getValue();
    }

    public final boolean isPetFelhunter() {
        return getEntry() == (int) PetEntry.FelHunter.getValue();
    }

    public final boolean isPetVoidwalker() {
        return getEntry() == (int) PetEntry.VoidWalker.getValue();
    }

    public final boolean isPetSuccubus() {
        return getEntry() == (int) PetEntry.Succubus.getValue();
    }

    public final boolean isPetDoomguard() {
        return getEntry() == (int) PetEntry.Doomguard.getValue();
    }

    public final boolean isPetFelguard() {
        return getEntry() == (int) PetEntry.Felguard.getValue();
    }

    // Death Knight pets
    public final boolean isPetGhoul() {
        return getEntry() == (int) PetEntry.Ghoul.getValue();
    } // Ghoul may be guardian or pet

    public final boolean isPetAbomination() {
        return getEntry() == (int) PetEntry.Abomination.getValue();
    } // Sludge Belcher dk talent

    // Shaman pet
    public final boolean isSpiritWolf() {
        return getEntry() == (int) PetEntry.SpiritWolf.getValue();
    } // Spirit wolf from feral spirits
}
