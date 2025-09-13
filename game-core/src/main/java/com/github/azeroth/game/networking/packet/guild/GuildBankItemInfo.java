package com.github.azeroth.game.networking.packet.guild;

import java.util.ArrayList;


public class GuildBankItemInfo {
    public itemInstance item = new itemInstance();
    public int slot;
    public int count;
    public int enchantmentID;
    public int charges;
    public int onUseEnchantmentID;
    public int flags;
    public boolean locked;
    public ArrayList<ItemGemData> socketEnchant = new ArrayList<>();
}
