package com.github.azeroth.game.entity.item;

import com.github.azeroth.common.Functions;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.defines.ItemContext;
import com.github.azeroth.dbc.domain.ItemBonus;
import com.github.azeroth.dbc.domain.ItemBonusTreeNode;
import com.github.azeroth.dbc.domain.ItemLevelSelectorQuality;
import com.github.azeroth.dbc.domain.MapDifficulty;
import com.github.azeroth.game.condition.Conditions;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.world.WorldContext;

import java.util.*;
import java.util.function.BinaryOperator;

public class ItemBonusManager {

    private final Map<Short /*itemBonusListId*/, List<ItemBonus>> itemBonusLists = new HashMap<>();
    private final Map<Short /*itemLevelDelta*/, Integer /*itemBonusListId*/> itemLevelDeltaToBonusListContainer = new HashMap<>();
    private final Map<Short /*itemLevelSelectorQualitySetId*/, TreeSet<ItemLevelSelectorQuality>> itemLevelQualitySelectorQualities = new HashMap<>();
    private final Map<Short /*itemBonusTreeId*/, Set<ItemBonusTreeNode>> itemBonusTrees = new HashMap<>();
    private final Map<Integer /*itemId*/, Short /*itemBonusTreeId*/> itemToBonusTree = new HashMap<>();

    private WorldContext worldContext;
    private DbcObjectManager dbcObjectManager;

    void load() {
        for (var bonus : dbcObjectManager.itemBonus())
            itemBonusLists.compute(bonus.getParentItemBonusListID(), Functions.addToList(bonus));

        for (var itemBonusListLevelDelta : dbcObjectManager.itemBonusListLevelDelta())
            itemLevelDeltaToBonusListContainer.put(itemBonusListLevelDelta.getItemLevelDelta(), itemBonusListLevelDelta.getId());

        for (var itemLevelSelectorQuality : dbcObjectManager.itemLevelSelectorQuality())
            itemLevelQualitySelectorQualities.compute(itemLevelSelectorQuality.getParentILSQualitySetID(), Functions.addToTreeSet(itemLevelSelectorQuality));

        for (var bonusTreeNode : dbcObjectManager.itemBonusTreeNode())
            itemBonusTrees.compute(bonusTreeNode.getParentItemBonusTreeID(), Functions.addToSet(bonusTreeNode));

        for (var itemBonusTreeAssignment : dbcObjectManager.itemXBonusTree())
            itemToBonusTree.put(itemBonusTreeAssignment.getItemID(), itemBonusTreeAssignment.getItemBonusTreeID());
    }



    ItemContext getContextForPlayer(MapDifficulty mapDifficulty, Player player) {
        if (mapDifficulty == null)
            return ItemContext.NONE;

        BinaryOperator<ItemContext> evalContext = (currentContext, newContext) -> {
            if (newContext == ItemContext.NONE)
                newContext = currentContext;
            else if (newContext == ItemContext.FORCE_TO_NONE)
                newContext = ItemContext.NONE;
            return newContext;
        };

        ItemContext context = ItemContext.NONE;

        var difficulty = dbcObjectManager.difficulty(mapDifficulty.getDifficultyID());

        if (difficulty != null)
            context = evalContext.apply(context, ItemContext.values()[difficulty.getItemContext()]);

        context = evalContext.apply(context, ItemContext.values()[mapDifficulty.getItemContext()]);

        if (mapDifficulty.getItemContextPickerID() != 0)
        {
            var selectedPickerEntry = null;
            for (var itemContextPickerEntry : dbcObjectManager.itemContextPickerEntry())
            {
                if (itemContextPickerEntry.getItemContextPickerID() != mapDifficulty.getItemContextPickerID())
                    continue;

                if (itemContextPickerEntry.getPVal() <= 0)
                    continue;

                boolean meetsPlayerCondition = false;
                if (player != null)
                    meetsPlayerCondition = Conditions.isPlayerMeetingCondition(player, itemContextPickerEntry.getPlayerConditionID());

                if (itemContextPickerEntry->Flags & 0x1)
                    meetsPlayerCondition = !meetsPlayerCondition;

                if (!meetsPlayerCondition)
                    continue;

                if (!selectedPickerEntry || selectedPickerEntry->OrderIndex < itemContextPickerEntry->OrderIndex)
                    selectedPickerEntry = itemContextPickerEntry;
            }

            if (selectedPickerEntry)
                context = evalContext(context, ItemContext(selectedPickerEntry->ItemCreationContext));
        }

        return context;
    }
}
