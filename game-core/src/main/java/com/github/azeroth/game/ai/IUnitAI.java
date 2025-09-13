package com.github.azeroth.game.ai;


import com.github.azeroth.defines.SpellCastResult;
import com.github.azeroth.game.domain.unit.DamageEffectType;
import com.github.azeroth.game.entity.unit.CalcDamageInfo;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import com.github.azeroth.game.spell.SpellInfo;

import java.util.ArrayList;


public interface IUnitAI {
    void attackStart(Unit victim);

    void attackStartCaster(Unit victim, float dist);

    boolean canAIAttack(Unit victim);

    void damageDealt(Unit victim, tangible.RefObject<Double> damage, DamageEffectType damageType);

    void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType);

    void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType, SpellInfo spellInfo);

    void doAction(int action);

    SpellCastResult doCast(int spellId);

    SpellCastResult doCast(Unit victim, int spellId);

    SpellCastResult doCast(Unit victim, int spellId, CastSpellExtraArgs args);

    SpellCastResult doCastAOE(int spellId);

    SpellCastResult doCastAOE(int spellId, CastSpellExtraArgs args);

    SpellCastResult doCastSelf(int spellId);

    SpellCastResult doCastSelf(int spellId, CastSpellExtraArgs args);

    SpellCastResult doCastVictim(int spellId);

    SpellCastResult doCastVictim(int spellId, CastSpellExtraArgs args);

    void doMeleeAttackIfReady();

    boolean doSpellAttackIfReady(int spellId);

    int getData();

    int getData(int id);

    String getDebugInfo();

    ObjectGuid getGUID();

    void setGUID(ObjectGuid guid);

    ObjectGuid getGUID(int id);

    void healDone(Unit to, double addhealth);

    void healReceived(Unit by, double addhealth);

    void initializeAI();

    void justEnteredCombat(Unit who);

    void justExitedCombat();

    void onCharmed(boolean isNew);

    void onDespawn();

    void onGameEvent(boolean start, short eventId);

    void onMeleeAttack(CalcDamageInfo damageInfo, WeaponAttackType attType, boolean extra);

    void reset();

    Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank);

    Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly);

    Unit selectTarget(SelectTargetMethod targetType, int offset, float dist);

    Unit selectTarget(SelectTargetMethod targetType, int offset);

    Unit selectTarget(SelectTargetMethod targetType);

    Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank, int aura);

    Unit selectTarget(SelectTargetMethod targetType, int offset, ICheck<unit> selector);

    Unit selectTarget(SelectTargetMethod targetType, int offset, tangible.Func1Param<unit, Boolean> selector);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank, int aura);

    ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, tangible.Func1Param<unit, Boolean> selector);

    void setData(int id, int value);

    void setGUID(ObjectGuid guid, int id);

    boolean shouldSparWith(Unit target);

    void spellInterrupted(int spellId, int unTimeMs);

    void updateAI(int diff);
}
