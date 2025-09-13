package com.github.azeroth.game.misc;


import com.github.azeroth.game.entity.object.WorldObject;
import game.ObjectManager;
import game.Quest;
import game.WorldSession;

public class PlayerMenu {
    private final gossipMenu gossipMenu = new gossipMenu();
    private final questMenu questMenu = new questMenu();
    private final WorldSession session;
    private final interactionData interactionData = new interactionData();

    public PlayerMenu(WorldSession session) {
        session = session;

        if (session != null) {
            gossipMenu.setLocale(session.getSessionDbLocaleIndex());
        }
    }

    public final void clearMenus() {
        gossipMenu.clearMenu();
        questMenu.clearMenu();
    }

    public final void sendGossipMenu(int titleTextId, ObjectGuid objectGUID) {
        interactionData.reset();
        interactionData.setSourceGuid(objectGUID);

        GossipMessagePkt packet = new GossipMessagePkt();
        packet.gossipGUID = objectGUID;
        packet.gossipID = gossipMenu.getMenuId();

        var addon = global.getObjectMgr().getGossipMenuAddon(packet.gossipID);

        if (addon != null) {
            packet.friendshipFactionID = addon.friendshipFactionId;
        }

        var text = global.getObjectMgr().getNpcText(titleTextId);

        if (text != null) {
            packet.textID = (int) text.getData().SelectRandomElementByWeight(data -> data.probability).broadcastTextID;
        }


        for (var(index, item) : gossipMenu.getMenuItems()) {
            ClientGossipOptions opt = new ClientGossipOptions();
            opt.gossipOptionID = item.gossipOptionId;
            opt.optionNPC = item.optionNpc;
            opt.optionFlags = (byte) (item.BoxCoded ? 1 : 0); // makes pop up box password
            opt.optionCost = (int) item.boxMoney; // money required to open menu, 2.0.3
            opt.optionLanguage = item.language;
            opt.flags = item.flags;
            opt.orderIndex = (int) item.orderIndex;
            opt.text = item.optionText; // text for gossip item
            opt.confirm = item.boxText; // accept text (related to money) pop up box, 2.0.3
            opt.status = GossipOptionStatus.Available;
            opt.spellID = item.spellId;
            opt.overrideIconID = item.overrideIconId;
            packet.gossipOptions.add(opt);
        }

        for (byte i = 0; i < questMenu.getMenuItemCount(); ++i) {
            var item = questMenu.getItem(i);
            var questID = item.questId;
            var quest = global.getObjectMgr().getQuestTemplate(questID);

            if (quest != null) {
                ClientGossipText gossipText = new ClientGossipText();
                gossipText.questID = questID;
                gossipText.contentTuningID = quest.contentTuningId;
                gossipText.questType = item.questIcon;
                gossipText.questFlags = (int) quest.getFlags().getValue();
                gossipText.questFlagsEx = (int) quest.flagsEx.getValue();
                gossipText.repeatable = quest.isAutoComplete() && quest.isRepeatable() && !quest.isDailyOrWeekly() && !quest.isMonthly();

                gossipText.questTitle = quest.logTitle;
                var locale = session.getSessionDbLocaleIndex();

                if (locale != locale.enUS) {
                    var localeData = global.getObjectMgr().getQuestLocale(quest.id);

                    if (localeData != null) {
                        tangible.RefObject<String> tempRef_QuestTitle = new tangible.RefObject<String>(gossipText.questTitle);
                        ObjectManager.getLocaleString(localeData.logTitle, locale, tempRef_QuestTitle);
                        gossipText.questTitle = tempRef_QuestTitle.refArgValue;
                    }
                }

                packet.gossipText.add(gossipText);
            }
        }

        session.sendPacket(packet);
    }

    public final void sendCloseGossip() {
        interactionData.reset();

        session.sendPacket(new GossipComplete());
    }

    public final void sendPointOfInterest(int id) {
        var pointOfInterest = global.getObjectMgr().getPointOfInterest(id);

        if (pointOfInterest == null) {
            Logs.SQL.error("Request to send non-existing PointOfInterest (Id: {0}), ignored.", id);

            return;
        }

        GossipPOI packet = new GossipPOI();
        packet.id = pointOfInterest.getId();
        packet.name = pointOfInterest.getName();

        var locale = session.getSessionDbLocaleIndex();

        if (locale != locale.enUS) {
            var localeData = global.getObjectMgr().getPointOfInterestLocale(id);

            if (localeData != null) {
                tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(packet.name);
                ObjectManager.getLocaleString(localeData.getName(), locale, tempRef_Name);
                packet.name = tempRef_Name.refArgValue;
            }
        }

        packet.flags = pointOfInterest.getFlags();
        packet.pos = pointOfInterest.pos;
        packet.icon = pointOfInterest.getIcon();
        packet.importance = pointOfInterest.getImportance();
        packet.WMOGroupID = pointOfInterest.getWmoGroupId();

        session.sendPacket(packet);
    }

    public final void sendQuestGiverQuestListMessage(WorldObject questgiver) {
        var guid = questgiver.getGUID();
        var localeConstant = session.getSessionDbLocaleIndex();

        QuestGiverQuestListMessage questList = new QuestGiverQuestListMessage();
        questList.questGiverGUID = guid;

        var questGreeting = global.getObjectMgr().getQuestGreeting(questgiver.getObjectTypeId(), questgiver.getEntry());

        if (questGreeting != null) {
            questList.greetEmoteDelay = questGreeting.emoteDelay;
            questList.greetEmoteType = questGreeting.emoteType;
            questList.greeting = questGreeting.text;

            if (localeConstant != locale.enUS) {
                var questGreetingLocale = global.getObjectMgr().getQuestGreetingLocale(questgiver.getObjectTypeId(), questgiver.getEntry());

                if (questGreetingLocale != null) {
                    tangible.RefObject<String> tempRef_Greeting = new tangible.RefObject<String>(questList.greeting);
                    ObjectManager.getLocaleString(questGreetingLocale.greeting, localeConstant, tempRef_Greeting);
                    questList.greeting = tempRef_Greeting.refArgValue;
                }
            }
        }

        for (var i = 0; i < questMenu.getMenuItemCount(); ++i) {
            var questMenuItem = questMenu.getItem(i);

            var questID = questMenuItem.questId;
            var quest = global.getObjectMgr().getQuestTemplate(questID);

            if (quest != null) {
                ClientGossipText text = new ClientGossipText();
                text.questID = questID;
                text.contentTuningID = quest.contentTuningId;
                text.questType = questMenuItem.questIcon;
                text.questFlags = (int) quest.getFlags().getValue();
                text.questFlagsEx = (int) quest.flagsEx.getValue();
                text.repeatable = quest.isAutoComplete() && quest.isRepeatable() && !quest.isDailyOrWeekly() && !quest.isMonthly();
                text.questTitle = quest.logTitle;

                if (localeConstant != locale.enUS) {
                    var localeData = global.getObjectMgr().getQuestLocale(quest.id);

                    if (localeData != null) {
                        tangible.RefObject<String> tempRef_QuestTitle = new tangible.RefObject<String>(text.questTitle);
                        ObjectManager.getLocaleString(localeData.logTitle, localeConstant, tempRef_QuestTitle);
                        text.questTitle = tempRef_QuestTitle.refArgValue;
                    }
                }

                questList.questDataText.add(text);
            }
        }

        session.sendPacket(questList);
    }

    public final void sendQuestGiverStatus(QuestGiverStatus questStatus, ObjectGuid npcGUID) {
        var packet = new QuestGiverStatusPkt();
        packet.questGiver.guid = npcGUID;
        packet.questGiver.status = questStatus;

        session.sendPacket(packet);
    }

    public final void sendQuestGiverQuestDetails(Quest quest, ObjectGuid npcGUID, boolean autoLaunched, boolean displayPopup) {
        QuestGiverQuestDetails packet = new QuestGiverQuestDetails();

        packet.questTitle = quest.logTitle;
        packet.logDescription = quest.logDescription;
        packet.descriptionText = quest.questDescription;
        packet.portraitGiverText = quest.portraitGiverText;
        packet.portraitGiverName = quest.portraitGiverName;
        packet.portraitTurnInText = quest.portraitTurnInText;
        packet.portraitTurnInName = quest.portraitTurnInName;

        var locale = session.getSessionDbLocaleIndex();

        packet.conditionalDescriptionText = quest.conditionalQuestDescription.Select(text ->
        {
            var content = text.text.charAt(locale.enUS.getValue());
            tangible.RefObject<String> tempRef_content = new tangible.RefObject<String>(content);
            ObjectManager.getLocaleString(text.text, locale, tempRef_content);
            content = tempRef_content.refArgValue;

            return new ConditionalQuestText(text.playerConditionId, text.questgiverCreatureId, content);
        }).ToList();

        if (locale != locale.enUS) {
            var localeData = global.getObjectMgr().getQuestLocale(quest.id);

            if (localeData != null) {
                tangible.RefObject<String> tempRef_QuestTitle = new tangible.RefObject<String>(packet.questTitle);
                ObjectManager.getLocaleString(localeData.logTitle, locale, tempRef_QuestTitle);
                packet.questTitle = tempRef_QuestTitle.refArgValue;
                tangible.RefObject<String> tempRef_LogDescription = new tangible.RefObject<String>(packet.logDescription);
                ObjectManager.getLocaleString(localeData.logDescription, locale, tempRef_LogDescription);
                packet.logDescription = tempRef_LogDescription.refArgValue;
                tangible.RefObject<String> tempRef_DescriptionText = new tangible.RefObject<String>(packet.descriptionText);
                ObjectManager.getLocaleString(localeData.questDescription, locale, tempRef_DescriptionText);
                packet.descriptionText = tempRef_DescriptionText.refArgValue;
                tangible.RefObject<String> tempRef_PortraitGiverText = new tangible.RefObject<String>(packet.portraitGiverText);
                ObjectManager.getLocaleString(localeData.portraitGiverText, locale, tempRef_PortraitGiverText);
                packet.portraitGiverText = tempRef_PortraitGiverText.refArgValue;
                tangible.RefObject<String> tempRef_PortraitGiverName = new tangible.RefObject<String>(packet.portraitGiverName);
                ObjectManager.getLocaleString(localeData.portraitGiverName, locale, tempRef_PortraitGiverName);
                packet.portraitGiverName = tempRef_PortraitGiverName.refArgValue;
                tangible.RefObject<String> tempRef_PortraitTurnInText = new tangible.RefObject<String>(packet.portraitTurnInText);
                ObjectManager.getLocaleString(localeData.portraitTurnInText, locale, tempRef_PortraitTurnInText);
                packet.portraitTurnInText = tempRef_PortraitTurnInText.refArgValue;
                tangible.RefObject<String> tempRef_PortraitTurnInName = new tangible.RefObject<String>(packet.portraitTurnInName);
                ObjectManager.getLocaleString(localeData.portraitTurnInName, locale, tempRef_PortraitTurnInName);
                packet.portraitTurnInName = tempRef_PortraitTurnInName.refArgValue;
            }
        }

        packet.questGiverGUID = npcGUID;
        packet.informUnit = session.getPlayer().getPlayerSharingQuest();
        packet.questID = quest.id;
        packet.questPackageID = (int) quest.packageID;
        packet.portraitGiver = quest.questGiverPortrait;
        packet.portraitGiverMount = quest.questGiverPortraitMount;
        packet.portraitGiverModelSceneID = quest.questGiverPortraitModelSceneId;
        packet.portraitTurnIn = quest.questTurnInPortrait;
        packet.questSessionBonus = 0; //quest.GetQuestSessionBonus(); // this is only sent while quest session is active
        packet.autoLaunched = autoLaunched;
        packet.displayPopup = displayPopup;
        packet.QuestFlags[0] = (int) (quest.getFlags().getValue() & (WorldConfig.getBoolValue(WorldCfg.QuestIgnoreAutoAccept) ? ~QuestFlag.AutoAccept : ~QuestFlag.NONE).getValue().getValue());
        packet.QuestFlags[1] = (int) quest.flagsEx.getValue();
        packet.QuestFlags[2] = (int) quest.flagsEx2.getValue();
        packet.suggestedPartyMembers = quest.suggestedPlayers;

        // Is there a better way? what about game objects?
        var creature = ObjectAccessor.getCreature(session.getPlayer(), npcGUID);

        if (creature != null) {
            packet.questGiverCreatureID = (int) creature.getTemplate().entry;
        }

        // RewardSpell can teach multiple spells in trigger spell effects. But not all effects must be SPELL_EFFECT_LEARN_SPELL. See example spell 33950
        var spellInfo = global.getSpellMgr().getSpellInfo(quest.getRewardSpell(), Difficulty.NONE);

        if (spellInfo != null) {
            for (var spellEffectInfo : spellInfo.getEffects()) {
                if (spellEffectInfo.isEffect(SpellEffectName.LearnSpell)) {
                    packet.learnSpells.add(spellEffectInfo.triggerSpell);
                }
            }
        }

        quest.buildQuestRewards(packet.rewards, session.getPlayer());

        for (var i = 0; i < SharedConst.QuestEmoteCount; ++i) {
            var emote = new QuestDescEmote((int) quest.DetailsEmote[i], quest.DetailsEmoteDelay[i]);
            packet.descEmotes.add(emote);
        }

        var objs = quest.objectives;

        for (var i = 0; i < objs.size(); ++i) {
            var obj = new QuestObjectiveSimple();
            obj.id = objs.get(i).id;
            obj.objectID = objs.get(i).objectID;
            obj.amount = objs.get(i).amount;
            obj.type = (byte) objs.get(i).type.getValue();
            packet.objectives.add(obj);
        }

        session.sendPacket(packet);
    }

    public final void sendQuestQueryResponse(Quest quest) {
        if (WorldConfig.getBoolValue(WorldCfg.CacheDataQueries)) {
            session.sendPacket(quest.response[_session.getSessionDbLocaleIndex().getValue()]);
        } else {
            var queryPacket = quest.buildQueryData(session.getSessionDbLocaleIndex(), session.getPlayer());
            session.sendPacket(queryPacket);
        }
    }

    public final void sendQuestGiverOfferReward(Quest quest, ObjectGuid npcGUID, boolean autoLaunched) {
        QuestGiverOfferRewardMessage packet = new QuestGiverOfferRewardMessage();

        packet.questTitle = quest.logTitle;
        packet.rewardText = quest.offerRewardText;
        packet.portraitGiverText = quest.portraitGiverText;
        packet.portraitGiverName = quest.portraitGiverName;
        packet.portraitTurnInText = quest.portraitTurnInText;
        packet.portraitTurnInName = quest.portraitTurnInName;

        var locale = session.getSessionDbLocaleIndex();

        packet.conditionalRewardText = quest.conditionalOfferRewardText.Select(text ->
        {
            var content = text.text.charAt(locale.enUS.getValue());
            tangible.RefObject<String> tempRef_content = new tangible.RefObject<String>(content);
            ObjectManager.getLocaleString(text.text, locale, tempRef_content);
            content = tempRef_content.refArgValue;

            return new ConditionalQuestText(text.playerConditionId, text.questgiverCreatureId, content);
        }).ToList();

        if (locale != locale.enUS) {
            var localeData = global.getObjectMgr().getQuestLocale(quest.id);

            if (localeData != null) {
                tangible.RefObject<String> tempRef_QuestTitle = new tangible.RefObject<String>(packet.questTitle);
                ObjectManager.getLocaleString(localeData.logTitle, locale, tempRef_QuestTitle);
                packet.questTitle = tempRef_QuestTitle.refArgValue;
                tangible.RefObject<String> tempRef_PortraitGiverText = new tangible.RefObject<String>(packet.portraitGiverText);
                ObjectManager.getLocaleString(localeData.portraitGiverText, locale, tempRef_PortraitGiverText);
                packet.portraitGiverText = tempRef_PortraitGiverText.refArgValue;
                tangible.RefObject<String> tempRef_PortraitGiverName = new tangible.RefObject<String>(packet.portraitGiverName);
                ObjectManager.getLocaleString(localeData.portraitGiverName, locale, tempRef_PortraitGiverName);
                packet.portraitGiverName = tempRef_PortraitGiverName.refArgValue;
                tangible.RefObject<String> tempRef_PortraitTurnInText = new tangible.RefObject<String>(packet.portraitTurnInText);
                ObjectManager.getLocaleString(localeData.portraitTurnInText, locale, tempRef_PortraitTurnInText);
                packet.portraitTurnInText = tempRef_PortraitTurnInText.refArgValue;
                tangible.RefObject<String> tempRef_PortraitTurnInName = new tangible.RefObject<String>(packet.portraitTurnInName);
                ObjectManager.getLocaleString(localeData.portraitTurnInName, locale, tempRef_PortraitTurnInName);
                packet.portraitTurnInName = tempRef_PortraitTurnInName.refArgValue;
            }

            var questOfferRewardLocale = global.getObjectMgr().getQuestOfferRewardLocale(quest.id);

            if (questOfferRewardLocale != null) {
                tangible.RefObject<String> tempRef_RewardText = new tangible.RefObject<String>(packet.rewardText);
                ObjectManager.getLocaleString(questOfferRewardLocale.rewardText, locale, tempRef_RewardText);
                packet.rewardText = tempRef_RewardText.refArgValue;
            }
        }

        QuestGiverOfferReward offer = new QuestGiverOfferReward();

        quest.buildQuestRewards(offer.rewards, session.getPlayer());
        offer.questGiverGUID = npcGUID;

        // Is there a better way? what about game objects?
        var creature = ObjectAccessor.getCreature(session.getPlayer(), npcGUID);

        if (creature) {
            packet.questGiverCreatureID = creature.getEntry();
            offer.questGiverCreatureID = creature.getTemplate().entry;
        }

        offer.questID = quest.id;
        offer.autoLaunched = autoLaunched;
        offer.suggestedPartyMembers = quest.suggestedPlayers;

        for (int i = 0; i < SharedConst.QuestEmoteCount && quest.OfferRewardEmote[i] != 0; ++i) {
            offer.emotes.add(new QuestDescEmote(quest.OfferRewardEmote[i], quest.OfferRewardEmoteDelay[i]));
        }

        offer.QuestFlags[0] = (int) quest.getFlags().getValue();
        offer.QuestFlags[1] = (int) quest.flagsEx.getValue();
        offer.QuestFlags[2] = (int) quest.flagsEx2.getValue();

        packet.portraitTurnIn = quest.questTurnInPortrait;
        packet.portraitGiver = quest.questGiverPortrait;
        packet.portraitGiverMount = quest.questGiverPortraitMount;
        packet.portraitGiverModelSceneID = quest.questGiverPortraitModelSceneId;
        packet.questPackageID = quest.packageID;

        packet.questData = offer;

        session.sendPacket(packet);
    }

    public final void sendQuestGiverRequestItems(Quest quest, ObjectGuid npcGUID, boolean canComplete, boolean autoLaunched) {
        // We can always call to RequestItems, but this packet only goes out if there are actually
        // items.  Otherwise, we'll skip straight to the OfferReward

        if (!quest.hasQuestObjectiveType(QuestObjectiveType.item) && canComplete) {
            sendQuestGiverOfferReward(quest, npcGUID, true);

            return;
        }

        QuestGiverRequestItems packet = new QuestGiverRequestItems();

        packet.questTitle = quest.logTitle;
        packet.completionText = quest.requestItemsText;

        var locale = session.getSessionDbLocaleIndex();

        packet.conditionalCompletionText = quest.conditionalRequestItemsText.Select(text ->
        {
            var content = text.text.charAt(locale.enUS.getValue());
            tangible.RefObject<String> tempRef_content = new tangible.RefObject<String>(content);
            ObjectManager.getLocaleString(text.text, locale, tempRef_content);
            content = tempRef_content.refArgValue;

            return new ConditionalQuestText(text.playerConditionId, text.questgiverCreatureId, content);
        }).ToList();

        if (locale != locale.enUS) {
            var localeData = global.getObjectMgr().getQuestLocale(quest.id);

            if (localeData != null) {
                tangible.RefObject<String> tempRef_QuestTitle = new tangible.RefObject<String>(packet.questTitle);
                ObjectManager.getLocaleString(localeData.logTitle, locale, tempRef_QuestTitle);
                packet.questTitle = tempRef_QuestTitle.refArgValue;
            }

            var questRequestItemsLocale = global.getObjectMgr().getQuestRequestItemsLocale(quest.id);

            if (questRequestItemsLocale != null) {
                tangible.RefObject<String> tempRef_CompletionText = new tangible.RefObject<String>(packet.completionText);
                ObjectManager.getLocaleString(questRequestItemsLocale.completionText, locale, tempRef_CompletionText);
                packet.completionText = tempRef_CompletionText.refArgValue;
            }
        }

        packet.questGiverGUID = npcGUID;

        // Is there a better way? what about game objects?
        var creature = ObjectAccessor.getCreature(session.getPlayer(), npcGUID);

        if (creature) {
            packet.questGiverCreatureID = creature.getTemplate().entry;
        }

        packet.questID = quest.id;

        if (canComplete) {
            packet.compEmoteDelay = quest.emoteOnCompleteDelay;
            packet.compEmoteType = quest.emoteOnComplete;
        } else {
            packet.compEmoteDelay = quest.emoteOnIncompleteDelay;
            packet.compEmoteType = quest.emoteOnIncomplete;
        }

        packet.QuestFlags[0] = (int) quest.getFlags().getValue();
        packet.QuestFlags[1] = (int) quest.flagsEx.getValue();
        packet.QuestFlags[2] = (int) quest.flagsEx2.getValue();
        packet.suggestPartyMembers = quest.suggestedPlayers;

        // incomplete: FD
        // incomplete quest with item objective but item objective is complete DD
        packet.statusFlags = canComplete ? 0xFF : 0xFD;

        packet.moneyToGet = 0;

        for (var obj : quest.objectives) {
            switch (obj.type) {
                case Item:
                    packet.collect.add(new QuestObjectiveCollect((int) obj.objectID, obj.amount, (int) obj.flags.getValue()));

                    break;
                case Currency:
                    packet.currency.add(new QuestCurrency((int) obj.objectID, obj.amount));

                    break;
                case Money:
                    packet.moneyToGet += obj.amount;

                    break;
                default:
                    break;
            }
        }

        packet.autoLaunched = autoLaunched;

        session.sendPacket(packet);
    }

    public final GossipMenu getGossipMenu() {
        return gossipMenu;
    }

    public final QuestMenu getQuestMenu() {
        return questMenu;
    }

    public final InteractionData getInteractionData() {
        return interactionData;
    }

    public final int getGossipOptionSender(int selection) {
        return gossipMenu.getMenuItemSender(selection);
    }

    public final int getGossipOptionAction(int selection) {
        return gossipMenu.getMenuItemAction(selection);
    }

    public final boolean isGossipOptionCoded(int selection) {
        return gossipMenu.isMenuItemCoded(selection);
    }

    private boolean isEmpty() {
        return gossipMenu.isEmpty() && questMenu.isEmpty();
    }
}
