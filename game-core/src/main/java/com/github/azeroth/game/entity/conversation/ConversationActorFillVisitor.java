package com.github.azeroth.game.entity.conversation;

import Framework.Constants.*;
import game.datastorage.*;
import game.maps.*;


public class ConversationActorFillVisitor {
    private final Conversation conversation;
    private final Unit creator;
    private final Map map;
    private final ConversationActorTemplate actor = new ConversationActorTemplate();

    public ConversationActorFillVisitor(Conversation conversation, Unit creator, Map map, ConversationActorTemplate actor) {
        this.conversation = conversation;
        this.creator = creator;
        this.map = map;
        this.actor = actor.clone();
    }

    public final void invoke(ConversationActorTemplate template) {
        if (template.worldObjectTemplate == null) {
            invoke(template.worldObjectTemplate);
        }

        if (template.noObjectTemplate == null) {
            invoke(template.noObjectTemplate);
        }

        if (template.activePlayerTemplate == null) {
            invoke(template.activePlayerTemplate);
        }

        if (template.talkingHeadTemplate == null) {
            invoke(template.talkingHeadTemplate);
        }
    }

    public final void invoke(ConversationActorWorldObjectTemplate worldObject) {
        Creature bestFit = null;

        for (var creature : map.getCreatureBySpawnIdStore().get(worldObject.spawnId)) {
            bestFit = creature;

            // If creature is in a personal phase then we pick that one specifically
            if (game.entities.Objects.equals(creature.PhaseShift.PersonalGuid, creator.getGUID().clone())) {
                break;
            }
        }

        if (bestFit) {
            conversation.addActor(actor.id, actor.index, bestFit.getGUID().clone());
        }
    }

    public final void invoke(ConversationActorNoObjectTemplate noObject) {
        conversation.addActor(actor.id, actor.index, ConversationActorType.WorldObject, noObject.creatureId, noObject.creatureDisplayInfoId);
    }

    public final void invoke(ConversationActorActivePlayerTemplate activePlayer) {
        conversation.addActor(actor.id, actor.index, ObjectGuid.create(HighGuid.Player, (long) 0xFFFFFFFFFFFFFFFF));
    }

    public final void invoke(ConversationActorTalkingHeadTemplate talkingHead) {
        conversation.addActor(actor.id, actor.index, ConversationActorType.TalkingHead, talkingHead.creatureId, talkingHead.creatureDisplayInfoId);
    }
}