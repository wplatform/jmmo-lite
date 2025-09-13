package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.unit.Unit;

class DelayedSpellTeleportEvent extends BasicEvent {
    private final Unit target;
    private final WorldLocation targetDest;
    private final TeleportToOptions options;
    private final int spellId;

    public DelayedSpellTeleportEvent(Unit target, WorldLocation targetDest, TeleportToOptions options, int spellId) {
        target = target;
        targetDest = targetDest;
        options = options;
        spellId = spellId;
    }

    @Override
    public boolean execute(long etime, int pTime) {
        if (targetDest.getMapId() == target.getLocation().getMapId()) {
            target.nearTeleportTo(targetDest, (options.getValue() & TeleportToOptions.spell.getValue()) != 0);
        } else {
            var player = target.toPlayer();

            if (player != null) {
                player.teleportTo(targetDest, options);
            } else {
                Log.outError(LogFilter.spells, String.format("Spell::EffectTeleportUnitsWithVisualLoadingScreen - spellId %1$s attempted to teleport creature to a different map.", spellId));
            }
        }

        return true;
    }
}
