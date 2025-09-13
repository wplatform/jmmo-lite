package com.github.azeroth.game.entity.creature;

import Framework.Constants.*;
import com.github.azeroth.dbc.domain.SummonProperty;
import com.github.azeroth.game.entity.unit.Unit;
import game.datastorage.*;


public class Puppet extends Minion {
    public Puppet(SummonProperty propertiesRecord, Unit owner) {
        super(propertiesRecord, owner, false);
        unitTypeMask = unitTypeMask.forValue(unitTypeMask.getValue() | unitTypeMask.Puppet.getValue());
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void InitStats(uint duration)
    @Override
    public void initStats(int duration) {
        super.initStats(duration);

        setLevel(getOwnerUnit().getLevel());
        reactState = ReactStates.Passive;
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void Update(uint diff)
    @Override
    public void update(int diff) {
        super.update(diff);

        //check if caster is channelling?
        if (isInWorld) {
            if (!isAlive()) {
                unSummon();
            }
        }
        // @todo why long distance .die does not remove it
    }
}