package com.github.azeroth.game.spell;


public class GOTargetInfo extends TargetInfoBase {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public long timeDelay;

    @Override
    public void doTargetSpellHit(Spell spell, SpellEffectInfo spellEffectInfo) {
        var go = Objects.equals(spell.getCaster().getGUID(), targetGUID) ? spell.getCaster().toGameObject() : ObjectAccessor.getGameObject(spell.getCaster(), targetGUID);

        if (go == null) {
            return;
        }

        spell.callScriptBeforeHitHandlers(SpellMissInfo.NONE);

        spell.handleEffects(null, null, go, null, spellEffectInfo, SpellEffectHandleMode.HitTarget);

        //AI functions
        if (go.getAI() != null) {
            go.getAI().spellHit(spell.getCaster(), spell.spellInfo);
        }

        if (spell.getCaster().isCreature() && spell.getCaster().toCreature().isAIEnabled()) {
            spell.getCaster().toCreature().getAI().spellHitTarget(go, spell.spellInfo);
        } else if (spell.getCaster().isGameObject() && spell.getCaster().toGameObject().getAI() != null) {
            spell.getCaster().toGameObject().getAI().spellHitTarget(go, spell.spellInfo);
        }

        spell.callScriptOnHitHandlers();
        spell.callScriptAfterHitHandlers();
    }
}
