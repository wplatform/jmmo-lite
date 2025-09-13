package com.github.azeroth.game.misc;

import java.util.ArrayList;


public class QuestMenu {
    private final ArrayList<QuestMenuItem> questMenuItems = new ArrayList<>();

    public final void addMenuItem(int questId, byte icon) {
        if (global.getObjectMgr().getQuestTemplate(questId) == null) {
            return;
        }

        QuestMenuItem questMenuItem = new QuestMenuItem();

        questMenuItem.questId = questId;
        questMenuItem.questIcon = icon;

        questMenuItems.add(questMenuItem);
    }

    public final void clearMenu() {
        questMenuItems.clear();
    }

    public final int getMenuItemCount() {
        return questMenuItems.size();
    }

    public final boolean isEmpty() {
        return questMenuItems.isEmpty();
    }

    public final QuestMenuItem getItem(int index) {
        return questMenuItems.LookupByIndex(index);
    }

    private boolean hasItem(int questId) {
        for (var item : questMenuItems) {
            if (item.questId == questId) {
                return true;
            }
        }

        return false;
    }
}
