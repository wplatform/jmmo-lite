package com.github.azeroth.game.entity.player;


import java.util.ArrayList;


public class VoidStorageItem {
    private long itemId;
    private int itemEntry;
    private ObjectGuid creatorGuid = ObjectGuid.EMPTY;
    private int randomBonusListId;
    private int fixedScalingLevel;
    private int artifactKnowledgeLevel;
    private Itemcontext context = itemContext.values()[0];
    private ArrayList<Integer> bonusListIDs = new ArrayList<>();

    public VoidStorageItem(long id, int entry, ObjectGuid creator, int randomBonusListId, int fixedScalingLevel, int artifactKnowledgeLevel, ItemContext context, ArrayList<Integer> bonuses) {
        setItemId(id);
        setItemEntry(entry);
        setCreatorGuid(creator);
        setRandomBonusListId(randomBonusListId);
        setFixedScalingLevel(fixedScalingLevel);
        setArtifactKnowledgeLevel(artifactKnowledgeLevel);
        setContext(context);

        for (var value : bonuses) {
            getBonusListIDs().add(value);
        }
    }

    public final long getItemId() {
        return itemId;
    }

    public final void setItemId(long value) {
        itemId = value;
    }

    public final int getItemEntry() {
        return itemEntry;
    }

    public final void setItemEntry(int value) {
        itemEntry = value;
    }

    public final ObjectGuid getCreatorGuid() {
        return creatorGuid;
    }

    public final void setCreatorGuid(ObjectGuid value) {
        creatorGuid = value;
    }

    public final int getRandomBonusListId() {
        return randomBonusListId;
    }

    public final void setRandomBonusListId(int value) {
        randomBonusListId = value;
    }

    public final int getFixedScalingLevel() {
        return fixedScalingLevel;
    }

    public final void setFixedScalingLevel(int value) {
        fixedScalingLevel = value;
    }

    public final int getArtifactKnowledgeLevel() {
        return artifactKnowledgeLevel;
    }

    public final void setArtifactKnowledgeLevel(int value) {
        artifactKnowledgeLevel = value;
    }

    public final ItemContext getContext() {
        return context;
    }

    public final void setContext(ItemContext value) {
        context = value;
    }

    public final ArrayList<Integer> getBonusListIDs() {
        return bonusListIDs;
    }

    public final void setBonusListIDs(ArrayList<Integer> value) {
        bonusListIDs = value;
    }
}
