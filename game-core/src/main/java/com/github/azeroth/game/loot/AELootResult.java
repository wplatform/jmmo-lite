package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.item.Item;

import java.util.ArrayList;
import java.util.HashMap;

public class AELootResult {
    private final ArrayList<ResultValue> byOrder = new ArrayList<>();
    private final HashMap<item, Integer> byItem = new HashMap<item, Integer>();

    public final void add(Item item, byte count, LootType lootType, int dungeonEncounterId) {
        var id = byItem.get(item);

        if (id != 0) {
            var resultValue = byOrder.get(id);
            resultValue.count += count;
        } else {
            byItem.put(item, byOrder.size());
            ResultValue value = new ResultValue();
            value.item = item;
            value.count = count;
            value.lootType = lootType;
            value.dungeonEncounterId = dungeonEncounterId;
            byOrder.add(value);
        }
    }

    public final ArrayList<ResultValue> getByOrder() {
        return byOrder;
    }

    public final static class ResultValue {
        public Item item;
        public byte count;
        public LootType lootType = LootType.values()[0];
        public int dungeonEncounterId;

        public ResultValue clone() {
            ResultValue varCopy = new ResultValue();

            varCopy.item = this.item;
            varCopy.count = this.count;
            varCopy.lootType = this.lootType;
            varCopy.dungeonEncounterId = this.dungeonEncounterId;

            return varCopy;
        }
    }
}
