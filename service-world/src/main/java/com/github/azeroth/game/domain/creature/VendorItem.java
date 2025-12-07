package com.github.azeroth.game.domain.creature;


import com.github.azeroth.utils.StringUtil;


public class VendorItem {
    public int entry;
    public int item;
    public int maxcount;
    public int incrtime;
    public int extendedCost;
    public ItemVendorType type;
    public int[] bonusListIDs;
    public int playerConditionId;
    public boolean ignoreFiltering;

    public VendorItem(int entry, int item, int maxcount, int incrtime, int extendedCost, ItemVendorType type, String bonusListIDs, int playerConditionId, boolean ignoreFiltering) {
        this.entry = entry;
        this.item = item;
        this.maxcount = maxcount;
        this.incrtime = incrtime;
        this.extendedCost = extendedCost;
        this.type = type;
        this.bonusListIDs = StringUtil.tokenizeInts(bonusListIDs, " ");
        this.playerConditionId = playerConditionId;
        this.ignoreFiltering = ignoreFiltering;
    }
}
