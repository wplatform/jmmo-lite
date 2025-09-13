package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;
import com.github.azeroth.game.chat.PlayerIdentifier;
import com.github.azeroth.game.chat.Tail;
import com.github.azeroth.game.entity.player.Player;


class LearnCommands {

    private static boolean handleLearnCommand(CommandHandler handler, int spellId, String allRanksStr) {
        var targetPlayer = handler.getSelectedPlayerOrSelf();

        if (!targetPlayer) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        if (!global.getSpellMgr().isSpellValid(spellId, handler.getSession().getPlayer())) {
            handler.sendSysMessage(SysMessage.CommandSpellBroken, spellId);

            return false;
        }

        var allRanks = !allRanksStr.isEmpty() && allRanksStr.equalsIgnoreCase("all");

        if (!allRanks && targetPlayer.hasSpell(spellId)) {
            if (targetPlayer == handler.getPlayer()) {
                handler.sendSysMessage(SysMessage.YouKnownSpell);
            } else {
                handler.sendSysMessage(SysMessage.TargetKnownSpell, handler.getNameLink(targetPlayer));
            }

            return false;
        }

        targetPlayer.learnSpell(spellId, false);

        if (allRanks) {
            while ((spellId = global.getSpellMgr().getNextSpellInChain(spellId)) != 0) {
                targetPlayer.learnSpell(spellId, false);
            }
        }

        return true;
    }


    private static boolean handleUnLearnCommand(CommandHandler handler, int spellId, String allRanksStr) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var allRanks = !allRanksStr.isEmpty() && allRanksStr.equalsIgnoreCase("all");

        if (allRanks) {
            spellId = global.getSpellMgr().getFirstSpellInChain(spellId);
        }

        if (target.hasSpell(spellId)) {
            target.removeSpell(spellId, false, !allRanks);
        } else {
            handler.sendSysMessage(SysMessage.ForgetSpell);
        }

        return true;
    }


    private static class LearnAllCommands {

        private static boolean handleLearnAllGMCommand(CommandHandler handler) {
            for (var skillSpell : global.getSpellMgr().getSkillLineAbilityMapBounds((int) SkillType.internal.getValue())) {
                var spellInfo = global.getSpellMgr().getSpellInfo(skillSpell.spell, Difficulty.NONE);

                if (spellInfo == null || !global.getSpellMgr().isSpellValid(spellInfo, handler.getSession().getPlayer(), false)) {
                    continue;
                }

                handler.getSession().getPlayer().learnSpell(skillSpell.spell, false);
            }

            handler.sendSysMessage(SysMessage.LearningGmSkills);

            return true;
        }


        private static boolean handleLearnDebugSpellsCommand(CommandHandler handler) {
            var player = handler.getPlayer();
            player.learnSpell(63364, false); // 63364 - Saronite Barrier (reduces damage taken by 99%)
            player.learnSpell(1908, false); //  1908 - Uber Heal Over time (heals target to full constantly)
            player.learnSpell(27680, false); // 27680 - berserk (+500% damage, +150% speed, 10m duration)
            player.learnSpell(62555, false); // 62555 - berserk (+500% damage, +150% melee haste, 10m duration)
            player.learnSpell(64238, false); // 64238 - berserk (+900% damage, +150% melee haste, 30m duration)
            player.learnSpell(72525, false); // 72525 - berserk (+240% damage, +160% haste, infinite duration)
            player.learnSpell(66776, false); // 66776 - Rage (+300% damage, -95% damage taken, +100% speed, infinite duration)

            return true;
        }


        private static boolean handleLearnAllCraftsCommand(CommandHandler handler, PlayerIdentifier player) {
            if (player == null) {
                player = PlayerIdentifier.fromTargetOrSelf(handler);
            }

            if (player == null || !player.isConnected()) {
                return false;
            }

            var target = player.getConnectedPlayer();


            for (var(_, skillInfo) : CliDB.SkillLineStorage) {
                if ((skillInfo.categoryID == SkillCategory.profession || skillInfo.categoryID == SkillCategory.Secondary) && skillInfo.CanLink != 0) // only prof. with recipes have
                {
                    handleLearnSkillRecipesHelper(target, skillInfo.id);
                }
            }

            handler.sendSysMessage(SysMessage.CommandLearnAllCraft);

            return true;
        }


        private static boolean handleLearnAllDefaultCommand(CommandHandler handler, PlayerIdentifier player) {
            if (player == null) {
                player = PlayerIdentifier.fromTargetOrSelf(handler);
            }

            if (player == null || !player.isConnected()) {
                return false;
            }

            var target = player.getConnectedPlayer();
            target.learnDefaultSkills();
            target.learnCustomSpells();
            target.learnQuestRewardedSpells();

            handler.sendSysMessage(SysMessage.CommandLearnAllDefaultAndQuest, handler.getNameLink(target));

            return true;
        }


        private static boolean handleLearnAllLangCommand(CommandHandler handler) {
            global.getLanguageMgr().forEachLanguage((_, languageDesc) ->
            {
                if (languageDesc.spellId != 0) {
                    handler.getSession().getPlayer().learnSpell(languageDesc.spellId, false);
                }

                return true;
            });

            handler.sendSysMessage(SysMessage.CommandLearnAllLang);

            return true;
        }


        private static boolean handleLearnAllRecipesCommand(CommandHandler handler, Tail namePart) {
            //  Learns all recipes of specified profession and sets skill to max
            //  Example: .learn all_recipes enchanting

            var target = handler.getSelectedPlayer();

            if (!target) {
                handler.sendSysMessage(SysMessage.PlayerNotFound);

                return false;
            }

            if (namePart.isEmpty()) {
                return false;
            }

            var name = "";
            int skillId = 0;


            for (var(_, skillInfo) : CliDB.SkillLineStorage) {
                if ((skillInfo.categoryID != SkillCategory.profession && skillInfo.categoryID != SkillCategory.Secondary) || skillInfo.CanLink == 0) // only prof with recipes have set
                {
                    continue;
                }

                var locale = handler.getSessionDbcLocale();
                name = skillInfo.DisplayName[locale];

                if (StringUtil.isEmpty(name)) {
                    continue;
                }

                if (!name.Like(namePart)) {
                    locale = locale.forValue(0);

                    for (; locale.getValue() < locale.Total.getValue(); ++locale) {
                        name = skillInfo.DisplayName[locale];

                        if (name.isEmpty()) {
                            continue;
                        }

                        if (name.Like(namePart)) {
                            break;
                        }
                    }
                }

                if (locale.getValue() < locale.Total.getValue()) {
                    skillId = skillInfo.id;

                    break;
                }
            }

            if (!(name.isEmpty() && skillId != 0)) {
                return false;
            }

            handleLearnSkillRecipesHelper(target, skillId);

            var maxLevel = target.getPureMaxSkillValue(SkillType.forValue(skillId));
            target.setSkill(skillId, target.getSkillStep(SkillType.forValue(skillId)), maxLevel, maxLevel);
            handler.sendSysMessage(SysMessage.CommandLearnAllRecipes, name);

            return true;
        }


        private static boolean handleLearnAllTalentsCommand(CommandHandler handler) {
            var player = handler.getSession().getPlayer();
            var playerClass = (int) player.getClass().getValue();


            for (var(_, talentInfo) : CliDB.TalentStorage) {
                if (playerClass != talentInfo.classID) {
                    continue;
                }

                if (talentInfo.specID != 0 && player.getPrimarySpecialization() != talentInfo.specID) {
                    continue;
                }

                var spellInfo = global.getSpellMgr().getSpellInfo(talentInfo.spellID, Difficulty.NONE);

                if (spellInfo == null || !global.getSpellMgr().isSpellValid(spellInfo, handler.getSession().getPlayer(), false)) {
                    continue;
                }

                // learn highest rank of talent and learn all non-talent spell ranks (recursive by tree)
                player.addTalent(talentInfo, player.getActiveTalentGroup(), true);
                player.learnSpell(talentInfo.spellID, false);
            }

            player.sendTalentsInfoData();

            handler.sendSysMessage(SysMessage.CommandLearnClassTalents);

            return true;
        }


        private static boolean handleLearnAllPetTalentsCommand(CommandHandler handler) {
            return true;
        }

        private static void handleLearnSkillRecipesHelper(Player player, int skillId) {
            var classmask = player.getClassMask();

            var skillLineAbilities = global.getDB2Mgr().GetSkillLineAbilitiesBySkill(skillId);

            if (skillLineAbilities == null) {
                return;
            }

            for (var skillLine : skillLineAbilities) {
                // not high rank
                if (skillLine.SupercedesSpell != 0) {
                    continue;
                }

                // skip racial skills
                if (skillLine.raceMask != 0) {
                    continue;
                }

                // skip wrong class skills
                if (skillLine.ClassMask != 0 && (skillLine.ClassMask & classmask) == 0) {
                    continue;
                }

                var spellInfo = global.getSpellMgr().getSpellInfo(skillLine.spell, Difficulty.NONE);

                if (spellInfo == null || !global.getSpellMgr().isSpellValid(spellInfo, player, false)) {
                    continue;
                }

                player.learnSpell(skillLine.spell, false);
            }
        }
    }


    private static class LearnAllMyCommands {

        private static boolean handleLearnMyQuestsCommand(CommandHandler handler) {
            var player = handler.getPlayer();


            for (var(_, quest) : global.getObjectMgr().getQuestTemplates()) {
                if (quest.allowableClasses != 0 && player.satisfyQuestClass(quest, false)) {
                    player.learnQuestRewardedSpells(quest);
                }
            }

            return true;
        }


        private static boolean handleLearnMySpellsCommand(CommandHandler handler) {
            var classEntry = CliDB.ChrClassesStorage.get(handler.getPlayer().getClass());

            if (classEntry == null) {
                return true;
            }

            int family = classEntry.SpellClassSet;


            for (var(_, entry) : CliDB.SkillLineAbilityStorage) {
                var spellInfo = global.getSpellMgr().getSpellInfo(entry.spell, Difficulty.NONE);

                if (spellInfo == null) {
                    continue;
                }

                // skip server-side/triggered spells
                if (spellInfo.spellLevel == 0) {
                    continue;
                }

                // skip wrong class/race skills
                if (!handler.getSession().getPlayer().isSpellFitByClassAndRace(spellInfo.id)) {
                    continue;
                }

                // skip other spell families
                if ((int) spellInfo.spellFamilyName != family) {
                    continue;
                }

                // skip broken spells
                if (!global.getSpellMgr().isSpellValid(spellInfo, handler.getSession().getPlayer(), false)) {
                    continue;
                }

                handler.getSession().getPlayer().learnSpell(spellInfo.id, false);
            }

            handler.sendSysMessage(SysMessage.CommandLearnClassSpells);

            return true;
        }
    }
}
