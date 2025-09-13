package com.github.azeroth.game.chat;


import com.github.azeroth.game.spell.AuraCreateInfo;

class SpellCommands {

    private static boolean handleCooldownCommand(CommandHandler handler, Integer spellIdArg) {
        var target = handler.getSelectedUnit();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var owner = target.getCharmerOrOwnerPlayerOrPlayerItself();

        if (!owner) {
            owner = handler.getSession().getPlayer();
            target = owner;
        }

        var nameLink = handler.getNameLink(owner);

        if (spellIdArg == null) {
            target.getSpellHistory().resetAllCooldowns();
            target.getSpellHistory().resetAllCharges();
            handler.sendSysMessage(SysMessage.RemoveallCooldown, nameLink);
        } else {
            var spellInfo = global.getSpellMgr().getSpellInfo(spellIdArg.intValue(), target.getMap().getDifficultyID());

            if (spellInfo == null) {
                handler.sendSysMessage(SysMessage.UnknownSpell, owner == handler.getSession().getPlayer() ? handler.getSysMessage(SysMessage.You) : nameLink);

                return false;
            }

            target.getSpellHistory().resetCooldown(spellInfo.id, true);
            target.getSpellHistory().resetCharges(spellInfo.chargeCategoryId);
            handler.sendSysMessage(SysMessage.RemoveallCooldown, spellInfo.id, owner == handler.getSession().getPlayer() ? handler.getSysMessage(SysMessage.You) : nameLink);
        }

        return true;
    }


    private static boolean handleAuraCommand(CommandHandler handler, int spellId) {
        var target = handler.getSelectedUnit();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, target.getMap().getDifficultyID());

        if (spellInfo == null) {
            return false;
        }

        var castId = ObjectGuid.create(HighGuid.Cast, SpellCastSource.NORMAL, target.getLocation().getMapId(), spellId, target.getMap().generateLowGuid(HighGuid.Cast));
        AuraCreateInfo createInfo = new AuraCreateInfo(castId, spellInfo, target.getMap().getDifficultyID(), SpellConst.MaxEffects, target);
        createInfo.setCaster(target);

        aura.tryRefreshStackOrCreate(createInfo);

        return true;
    }



    private static boolean handleUnAuraCommand(CommandHandler handler) {
        return handleUnAuraCommand(handler, 0);
    }

    private static boolean handleUnAuraCommand(CommandHandler handler, int spellId) {
        var target = handler.getSelectedUnit();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (spellId == 0) {
            target.removeAllAuras();

            return true;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, Difficulty.NONE);

        if (spellInfo != null) {
            target.removeAura(spellInfo.getId());

            return true;
        }

        return true;
    }


    private static boolean handleSetSkillCommand(CommandHandler handler, int skillId, int level, Integer maxSkillArg) {
        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var skillLine = CliDB.SkillLineStorage.get(skillId);

        if (skillLine == null) {
            handler.sendSysMessage(SysMessage.InvalidSkillId, skillId);

            return false;
        }

        var targetHasSkill = target.getSkillValue(SkillType.forValue(skillId)) != 0;

        // If our target does not yet have the skill they are trying to add to them, the chosen level also becomes
        // the max level of the new profession.
        var max = (short) (maxSkillArg == null ? targetHasSkill ? target.getPureMaxSkillValue(SkillType.forValue(skillId)) : level : maxSkillArg.intValue());

        if (level == 0 || level > max) {
            return false;
        }

        // If the player has the skill, we get the current skill step. If they don't have the skill, we
        // add the skill to the player's book with step 1 (which is the first rank, in most cases something
        // like 'Apprentice <skill>'.
        target.setSkill(SkillType.forValue(skillId), (int) (targetHasSkill ? target.getSkillStep(SkillType.forValue(skillId)) : 1), level, max);
        handler.sendSysMessage(SysMessage.SetSkill, skillId, skillLine.DisplayName[handler.getSessionDbcLocale()], handler.getNameLink(target), level, max);

        return true;
    }
}
