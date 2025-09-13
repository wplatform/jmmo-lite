package com.github.azeroth.game.blackmarket;


import com.github.azeroth.game.networking.packet.ItemBonuses;
import com.github.azeroth.game.networking.packet.itemInstance;

import java.util.ArrayList;

public class BlackMarketTemplate {

    public int marketID;

    public int sellerNPC;

    public int quantity;

    public long minBid;
    public long duration;
    public float chance;
    public itemInstance item;

    public final boolean loadFromDB(SQLFields fields) {
        marketID = fields.<Integer>Read(0);
        sellerNPC = fields.<Integer>Read(1);
        item = new itemInstance();
        item.itemID = fields.<Integer>Read(2);
        quantity = fields.<Integer>Read(3);
        minBid = fields.<Long>Read(4);
        duration = fields.<Integer>Read(5);
        chance = fields.<Float>Read(6);

        var bonusListIDsTok = new LocalizedString();
        ArrayList<Integer> bonusListIDs = new ArrayList<>();

        if (!bonusListIDsTok.isEmpty()) {
            for (String token : bonusListIDsTok) {
                int id;
                tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(token, tempOut_id)) {
                    id = tempOut_id.outArgValue;
                    bonusListIDs.add(id);
                } else {
                    id = tempOut_id.outArgValue;
                }
            }
        }

        if (!bonusListIDs.isEmpty()) {
            item.itemBonus = new ItemBonuses();
            item.itemBonus.bonusListIDs = bonusListIDs;
        }

        if (global.getObjectMgr().getCreatureTemplate(sellerNPC) == null) {
            Log.outError(LogFilter.misc, "Black market template {0} does not have a valid seller. (Entry: {1})", marketID, sellerNPC);

            return false;
        }

        if (global.getObjectMgr().getItemTemplate(item.itemID) == null) {
            Log.outError(LogFilter.misc, "Black market template {0} does not have a valid item. (Entry: {1})", marketID, item.itemID);

            return false;
        }

        return true;
    }
}
