package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.unit.Unit;

class ProcReflectDelayed extends BasicEvent {
    private final Unit victim;
    private final ObjectGuid casterGuid;

    public ProcReflectDelayed(Unit owner, ObjectGuid casterGuid) {
        victim = owner;
        casterGuid = casterGuid;
    }


    @Override
    public boolean execute(long etime, int pTime) {
        var caster = global.getObjAccessor().GetUnit(victim, casterGuid);

        if (!caster) {
            return true;
        }

        var typeMaskActor = procFlags.NONE;
        var typeMaskActionTarget = procFlags.forValue(procFlags.TakeHarmfulSpell.getValue() | procFlags.TakeHarmfulAbility.getValue());
        var spellTypeMask = ProcFlagsSpellType.forValue(ProcFlagsSpellType.damage.getValue() | ProcFlagsSpellType.NoDmgHeal.getValue());
        var spellPhaseMask = ProcFlagsSpellPhase.NONE;
        var hitMask = ProcFlagsHit.Reflect;

        unit.procSkillsAndAuras(caster, victim, new ProcFlagsInit(typeMaskActor), new ProcFlagsInit(typeMaskActionTarget), spellTypeMask, spellPhaseMask, hitMask, null, null, null);

        return true;
    }
}
