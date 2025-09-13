package com.github.azeroth.game.spell;

public class AuraKey implements IEquatable<AuraKey> {
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public ObjectGuid item = ObjectGuid.EMPTY;
    public int spellId;
    public int effectMask;

    public AuraKey(ObjectGuid caster, ObjectGuid item, int spellId, int effectMask) {
        caster = caster;
        item = item;
        spellId = spellId;
        effectMask = effectMask;
    }

    public static boolean opEquals(AuraKey first, AuraKey other) {
        if (first == other) {
            return true;
        }

        if (first == null || other == null) {
            return false;
        }

        return first.equals(other);
    }

    public static boolean opNotEquals(AuraKey first, AuraKey other) {
        return !(AuraKey.opEquals(first, other));
    }

    public final boolean equals(AuraKey other) {
        return Objects.equals(other.caster, caster) && Objects.equals(other.item, item) && other.spellId == spellId && other.effectMask == effectMask;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof AuraKey && Equals((AuraKey) obj);
    }

    @Override
    public int hashCode() {
        class AnonymousType {
            public ObjectGuid caster;
            public ObjectGuid item;
            public int spellId;
            public int effectMask;

            public AnonymousType(ObjectGuid _Caster, ObjectGuid _Item, int _SpellId, int _EffectMask) {
                caster = _Caster;
                item = _Item;
                spellId = _SpellId;
                effectMask = _EffectMask;
            }
        }
        return (new AnonymousType(caster, item, spellId, effectMask)).hashCode();
    }
}
