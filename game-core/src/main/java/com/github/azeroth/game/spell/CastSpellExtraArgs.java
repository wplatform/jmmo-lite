package com.github.azeroth.game.spell;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.spell.enums.SpellValueMod;
import com.github.azeroth.game.spell.enums.TriggerCastFlag;

import java.util.EnumMap;


public class CastSpellExtraArgs {
    public TriggerCastFlag triggerFlags = TriggerCastFlag.NONE;
    public Item castItem;
    public Spell triggeringSpell;
    public AuraEffect triggeringAura;
    public ObjectGuid originalCaster = ObjectGuid.EMPTY;
    public Difficulty castDifficulty = Difficulty.DIFFICULTY_NONE;
    public ObjectGuid originalCastId = ObjectGuid.EMPTY;
    public Integer originalCastItemLevel = null;
    //public IntFloatMap spellValueOverrides = new IntFloatMap(SpellValueMod.BASE_POINT_END.ordinal());
    public EnumMap<SpellValueMod, Float> spellValueOverrides = new EnumMap<>(SpellValueMod.class);

    public Object customArg;
    public Byte empowerStage = null;

    public CastSpellExtraArgs() {
    }

    public CastSpellExtraArgs(boolean triggered) {
        triggerFlags = triggered ? TriggerCastFlag.FULL_MASK : TriggerCastFlag.NONE;
    }

    public CastSpellExtraArgs(TriggerCastFlag trigger) {
        triggerFlags = trigger;
    }

    public CastSpellExtraArgs(Item item) {
        triggerFlags = TriggerCastFlag.FULL_MASK;
        castItem = item;
    }

    public CastSpellExtraArgs(Spell triggeringSpell) {
        triggerFlags = TriggerCastFlag.FULL_MASK;
        setTriggeringSpell(triggeringSpell);
    }

    public CastSpellExtraArgs(AuraEffect eff) {
        triggerFlags = TriggerCastFlag.FULL_MASK;
        setTriggeringAura(eff);
    }

    public CastSpellExtraArgs(Difficulty castDifficulty) {
        this.castDifficulty = castDifficulty;
    }

    public CastSpellExtraArgs(SpellValueMod mod, float val) {
        spellValueOverrides.put(mod, val);
    }

    public final CastSpellExtraArgs setTriggerFlags(TriggerCastFlag flag) {
        triggerFlags = flag;

        return this;
    }

    public final CastSpellExtraArgs setCastItem(Item item) {
        castItem = item;

        return this;
    }

    public final CastSpellExtraArgs setIsTriggered(boolean triggered) {
        triggerFlags = triggered ? TriggerCastFlag.FULL_MASK : TriggerCastFlag.NONE;

        return this;
    }

    public final CastSpellExtraArgs setSpellValueMod(SpellValueMod mod, float val) {
        spellValueOverrides.put(mod, val);

        return this;
    }

    public final CastSpellExtraArgs setTriggeringSpell(Spell triggeringSpell) {
        this.triggeringSpell = triggeringSpell;

        if (triggeringSpell != null) {
            originalCastItemLevel = triggeringSpell.castItemLevel;
            originalCastId = triggeringSpell.castId;
        }

        return this;
    }

    public final CastSpellExtraArgs setTriggeringAura(AuraEffect triggeringAura) {
        this.triggeringAura = triggeringAura;

        if (triggeringAura != null) {
            originalCastId = triggeringAura.getBase().getCastId();
        }

        return this;
    }

    public final CastSpellExtraArgs setOriginalCaster(ObjectGuid guid) {
        originalCaster = guid;

        return this;
    }

    public final CastSpellExtraArgs setCastDifficulty(Difficulty castDifficulty) {
        this.castDifficulty = castDifficulty;

        return this;
    }

    public final CastSpellExtraArgs setOriginalCastId(ObjectGuid castId) {
        originalCastId = castId;

        return this;
    }

    public final CastSpellExtraArgs addSpellMod(SpellValueMod mod, float val) {
        spellValueOverrides.put(mod, val);

        return this;
    }

    public final CastSpellExtraArgs setCustomArg(Object customArg) {
        customArg = customArg;

        return this;
    }
}
