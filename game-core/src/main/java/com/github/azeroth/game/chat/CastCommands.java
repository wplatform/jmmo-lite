package com.github.azeroth.game.chat;


import com.github.azeroth.game.spell.CastSpellExtraArgs;

class CastCommands {


    private static boolean handleCastCommand(CommandHandler handler, int spellId, String triggeredStr) {
        var target = handler.getSelectedUnit();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (!checkSpellExistsAndIsValid(handler, spellId)) {
            return false;
        }

        var triggerFlags = getTriggerFlags(triggeredStr);

        if (!triggerFlags != null) {
            return false;
        }

        handler.getSession().getPlayer().castSpell(target, spellId, new CastSpellExtraArgs(triggerFlags));

        return true;
    }



    private static boolean handleCastBackCommand(CommandHandler handler, int spellId, String triggeredStr) {
        var caster = handler.getSelectedCreature();

        if (!caster) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (checkSpellExistsAndIsValid(handler, spellId)) {
            return false;
        }

        var triggerFlags = getTriggerFlags(triggeredStr);

        if (!triggerFlags != null) {
            return false;
        }

        caster.castSpell(handler.getSession().getPlayer(), spellId, new CastSpellExtraArgs(triggerFlags));

        return true;
    }



    private static boolean handleCastDistCommand(CommandHandler handler, int spellId, float dist, String triggeredStr) {
        if (checkSpellExistsAndIsValid(handler, spellId)) {
            return false;
        }

        var triggerFlags = getTriggerFlags(triggeredStr);

        if (!triggerFlags != null) {
            return false;
        }

        var closestPos = new Position();
        handler.getSession().getPlayer().getClosePoint(closestPos, dist);

        handler.getSession().getPlayer().castSpell(closestPos, spellId, new CastSpellExtraArgs(triggerFlags));

        return true;
    }



    private static boolean handleCastSelfCommand(CommandHandler handler, int spellId, String triggeredStr) {
        var target = handler.getSelectedUnit();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (!checkSpellExistsAndIsValid(handler, spellId)) {
            return false;
        }

        var triggerFlags = getTriggerFlags(triggeredStr);

        if (!triggerFlags != null) {
            return false;
        }

        target.castSpell(target, spellId, new CastSpellExtraArgs(triggerFlags));

        return true;
    }



    private static boolean handleCastTargetCommad(CommandHandler handler, int spellId, String triggeredStr) {
        var caster = handler.getSelectedCreature();

        if (!caster) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (!caster.getVictim()) {
            handler.sendSysMessage(SysMessage.SelectedTargetNotHaveVictim);

            return false;
        }

        if (checkSpellExistsAndIsValid(handler, spellId)) {
            return false;
        }

        var triggerFlags = getTriggerFlags(triggeredStr);

        if (!triggerFlags != null) {
            return false;
        }

        caster.castSpell(caster.getVictim(), spellId, new CastSpellExtraArgs(triggerFlags));

        return true;
    }



    private static boolean handleCastDestCommand(CommandHandler handler, int spellId, float x, float y, float z, String triggeredStr) {
        var caster = handler.getSelectedUnit();

        if (!caster) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (checkSpellExistsAndIsValid(handler, spellId)) {
            return false;
        }

        var triggerFlags = getTriggerFlags(triggeredStr);

        if (!triggerFlags != null) {
            return false;
        }

        caster.castSpell(new Position(x, y, z), spellId, new CastSpellExtraArgs(triggerFlags));

        return true;
    }

    private static TriggerCastFlags getTriggerFlags(String triggeredStr) {
        if (!triggeredStr.isEmpty()) {
            if (triggeredStr.startsWith("triggered")) // check if "triggered" starts with *triggeredStr (e.g. "trig", "trigger", etc.)
            {
                return TriggerCastFlags.FullDebugMask;
            } else {
                return null;
            }
        }

        return TriggerCastFlags.NONE;
    }


    private static boolean checkSpellExistsAndIsValid(CommandHandler handler, int spellId) {
        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, Difficulty.NONE);

        if (spellInfo == null) {
            handler.sendSysMessage(SysMessage.CommandNospellfound);

            return false;
        }

        if (!global.getSpellMgr().isSpellValid(spellInfo, handler.getPlayer())) {
            handler.sendSysMessage(SysMessage.CommandSpellBroken, spellInfo.getId());

            return false;
        }

        return true;
    }
}
