package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.spell.Spell;

import java.util.ArrayList;


public class SpellCastLogData {
    private final ArrayList<SpellLogpowerData> powerData = new ArrayList<>();

    private long health;
    private double attackPower;
    private double spellPower;
    private int armor;

    public final void initialize(Unit unit) {
        health = unit.getHealth();
        attackPower = unit.getTotalAttackPowerValue(unit.getClass() == playerClass.Hunter ? WeaponAttackType.RangedAttack : WeaponAttackType.BaseAttack);
        spellPower = unit.spellBaseDamageBonusDone(spellSchoolMask.spell);
        armor = unit.getArmor();
        powerData.add(new SpellLogPowerData(unit.getDisplayPowerType().getValue(), unit.getPower(unit.getDisplayPowerType()), 0));
    }

    public final void initialize(Spell spell) {
        var unitCaster = spell.getCaster().toUnit();

        if (unitCaster != null) {
            health = unitCaster.getHealth();
            attackPower = unitCaster.getTotalAttackPowerValue(unitCaster.getClass() == playerClass.Hunter ? WeaponAttackType.RangedAttack : WeaponAttackType.BaseAttack);
            spellPower = unitCaster.spellBaseDamageBonusDone(spellSchoolMask.spell);
            armor = unitCaster.getArmor();
            var primaryPower = unitCaster.getDisplayPowerType();
            var primaryPowerAdded = false;

            for (var cost : spell.getPowerCost()) {
                powerData.add(new SpellLogPowerData(cost.power.getValue(), unitCaster.getPower(cost.power), (int) cost.amount));

                if (cost.power == primaryPowerType) {
                    primaryPowerAdded = true;
                }
            }

            if (!primaryPowerAdded) {
                powerData.add(0, new SpellLogPowerData(primaryPowerType.getValue(), unitCaster.getPower(primaryPowerType), 0));
            }
        }
    }

    public final void write(WorldPacket data) {
        data.writeInt64(health);
        data.writeInt32((int) attackPower);
        data.writeInt32((int) spellPower);
        data.writeInt32(armor);
        data.writeBits(powerData.size(), 9);
        data.flushBits();

        for (var powerData : powerData) {
            data.writeInt32(powerData.powerType);
            data.writeInt32(powerData.amount);
            data.writeInt32(powerData.cost);
        }
    }
}
