package com.github.azeroth.game.misc;


import com.github.azeroth.game.domain.gossip.GossipMenuOption;
import game.*;

import java.util.Locale;
import java.util.TreeMap;


public class GossipMenu {
    private final TreeMap<Integer, GossipMenuItem> menuItems = new TreeMap<Integer, GossipMenuItem>();
    private int menuId;

    public final int addMenuItem(int gossipOptionId, int orderIndex, GossipOptionNpc optionNpc, String optionText, int language, GossipOptionFlags flags, Integer gossipNpcOptionId, int actionMenuId, int actionPoiId, boolean boxCoded, int boxMoney, String boxText, Integer spellId, Integer overrideIconId, int sender, int action) {
        // Find a free new id - script case
        if (orderIndex == -1) {
            orderIndex = 0;

            if (menuId != 0) {
                // set baseline orderIndex as higher than whatever exists in db
                var bounds = global.getObjectMgr().getGossipMenuItemsMapBounds(menuId);
                var itr = bounds.MaxBy(a -> a.orderIndex);

                if (itr != null) {
                    orderIndex = (int) (itr.orderIndex + 1);
                }
            }

            if (!menuItems.isEmpty()) {
                for (var pair : menuItems.entrySet()) {
                    if (pair.getValue().orderIndex > orderIndex) {
                        break;
                    }

                    orderIndex = (int) pair.getValue().orderIndex + 1;
                }
            }
        }

        if (gossipOptionId == 0) {
            gossipOptionId = -((int) _menuId * 100 + orderIndex);
        }

        GossipMenuItem menuItem = new GossipMenuItem();
        menuItem.setGossipOptionId(gossipOptionId);
        menuItem.setOrderIndex((int) orderIndex);
        menuItem.setOptionNpc(optionNpc);
        menuItem.setOptionText(optionText);
        menuItem.setLanguage(language);
        menuItem.setFlags(flags);
        menuItem.setGossipNpcOptionId(gossipNpcOptionId);
        menuItem.setBoxCoded(boxCoded);
        menuItem.setBoxMoney(boxMoney);
        menuItem.setBoxText(boxText);
        menuItem.setSpellId(spellId);
        menuItem.setOverrideIconId(overrideIconId);
        menuItem.setActionMenuId(actionMenuId);
        menuItem.setActionPoiId(actionPoiId);
        menuItem.setSender(sender);
        menuItem.setAction(action);

        menuItems.put((int) orderIndex, menuItem);

        return (int) orderIndex;
    }    private Locale locale = locale.values()[0];

    /**
     * Adds a localized gossip menu item from db by menu id and menu item id.
     *
     * @param menuId     menuId Gossip menu id.
     * @param menuItemId menuItemId Gossip menu item id.
     * @param sender     sender Identifier of the current menu.
     * @param action     action Custom action given to OnGossipHello.
     */
    public final void addMenuItem(int menuId, int menuItemId, int sender, int action) {
        // Find items for given menu id.
        var bounds = global.getObjectMgr().getGossipMenuItemsMapBounds(menuId);

        // Return if there are none.
        if (bounds.isEmpty()) {
            return;
        }

        /** Find the one with the given menu item id.
         */
        var gossipMenuItems = tangible.ListHelper.find(bounds, menuItem -> menuItem.orderIndex == menuItemId);

        if (gossipMenuItems == null) {
            return;
        }

        addMenuItem(gossipMenuItems, sender, action);
    }

    public final void addMenuItem(GossipMenuOption menuItem, int sender, int action) {
        // Store texts for localization.
        String strOptionText, strBoxText;
        var optionBroadcastText = CliDB.BroadcastTextStorage.get(menuItem.getOptionBroadcastTextId());
        var boxBroadcastText = CliDB.BroadcastTextStorage.get(menuItem.getBoxBroadcastTextId());

        // OptionText
        if (optionBroadcastText != null) {
            strOptionText = global.getDB2Mgr().GetBroadcastTextValue(optionBroadcastText, getLocale());
        } else {
            strOptionText = menuItem.getOptionText();

            /** Find localizations from database.
             */
            if (getLocale() != locale.enUS) {
                var gossipMenuLocale = global.getObjectMgr().getGossipMenuItemsLocale(menuItem.getMenuId(), menuItem.getOrderIndex());

                if (gossipMenuLocale != null) {
                    tangible.RefObject<String> tempRef_strOptionText = new tangible.RefObject<String>(strOptionText);
                    ObjectManager.getLocaleString(gossipMenuLocale.optionText, getLocale(), tempRef_strOptionText);
                    strOptionText = tempRef_strOptionText.refArgValue;
                }
            }
        }

        // BoxText
        if (boxBroadcastText != null) {
            strBoxText = global.getDB2Mgr().GetBroadcastTextValue(boxBroadcastText, getLocale());
        } else {
            strBoxText = menuItem.getBoxText();

            // Find localizations from database.
            if (getLocale() != locale.enUS) {
                var gossipMenuLocale = global.getObjectMgr().getGossipMenuItemsLocale(menuItem.getMenuId(), menuItem.getOrderIndex());

                if (gossipMenuLocale != null) {
                    tangible.RefObject<String> tempRef_strBoxText = new tangible.RefObject<String>(strBoxText);
                    ObjectManager.getLocaleString(gossipMenuLocale.boxText, getLocale(), tempRef_strBoxText);
                    strBoxText = tempRef_strBoxText.refArgValue;
                }
            }
        }

        addMenuItem(menuItem.getGossipOptionId(), (int) menuItem.getOrderIndex(), menuItem.getOptionNpc(), strOptionText, menuItem.getLanguage(), menuItem.getFlags(), menuItem.getGossipNpcOptionId(), menuItem.getActionMenuId(), menuItem.getActionPoiId(), menuItem.getBoxCoded(), menuItem.getBoxMoney(), strBoxText, menuItem.getSpellId(), menuItem.getOverrideIconId(), sender, action);
    }

    public final GossipMenuItem getItem(int gossipOptionId) {
        return menuItems.values().FirstOrDefault(item -> item.gossipOptionId == gossipOptionId);
    }

    public final int getMenuItemSender(int orderIndex) {
        var item = getItemByIndex(orderIndex);

        if (item != null) {
            return item.getSender();
        }

        return 0;
    }

    public final int getMenuItemAction(int orderIndex) {
        var item = getItemByIndex(orderIndex);

        if (item != null) {
            return item.getAction();
        }

        return 0;
    }

    public final boolean isMenuItemCoded(int orderIndex) {
        var item = getItemByIndex(orderIndex);

        if (item != null) {
            return item.getBoxCoded();
        }

        return false;
    }

    public final void clearMenu() {
        menuItems.clear();
    }

    public final int getMenuId() {
        return menuId;
    }

    public final void setMenuId(int menu_id) {
        menuId = menu_id;
    }

    public final int getMenuItemCount() {
        return menuItems.size();
    }

    public final boolean isEmpty() {
        return menuItems.isEmpty();
    }

    public final TreeMap<Integer, GossipMenuItem> getMenuItems() {
        return menuItems;
    }

    private GossipMenuItem getItemByIndex(int orderIndex) {
        return menuItems.get(orderIndex);
    }

    private Locale getLocale() {
        return locale;
    }

    public final void setLocale(Locale locale) {
        locale = locale;
    }


}
