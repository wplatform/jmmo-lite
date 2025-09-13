package com.github.azeroth.game.spell;

class AbsorbAuraOrderPred extends Comparer<AuraEffect> {
    @Override
    public int compare(AuraEffect aurEffA, AuraEffect aurEffB) {
        var spellProtoA = aurEffA.getSpellInfo();
        var spellProtoB = aurEffB.getSpellInfo();

        // Fel Blossom
        if (spellProtoA.getId() == 28527) {
            return 1;
        }

        if (spellProtoB.getId() == 28527) {
            return 0;
        }

        // Ice Barrier
        if (spellProtoA.getCategory() == 471) {
            return 1;
        }

        if (spellProtoB.getCategory() == 471) {
            return 0;
        }

        // Sacrifice
        if (spellProtoA.getId() == 7812) {
            return 1;
        }

        if (spellProtoB.getId() == 7812) {
            return 0;
        }

        // Cauterize (must be last)
        if (spellProtoA.getId() == 86949) {
            return 0;
        }

        if (spellProtoB.getId() == 86949) {
            return 1;
        }

        // Spirit of Redemption (must be last)
        if (spellProtoA.getId() == 20711) {
            return 0;
        }

        if (spellProtoB.getId() == 20711) {
            return 1;
        }

        return 0;
    }
}
