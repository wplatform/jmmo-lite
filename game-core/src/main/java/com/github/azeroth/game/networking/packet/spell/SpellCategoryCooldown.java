package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class SpellCategoryCooldown extends ServerPacket {
    public ArrayList<CategoryCooldownInfo> categoryCooldowns = new ArrayList<>();

    public SpellCategoryCooldown() {
        super(ServerOpcode.CategoryCooldown);
    }

    @Override
    public void write() {
        this.writeInt32(categoryCooldowns.size());

        for (var cooldown : categoryCooldowns) {
            this.writeInt32(cooldown.category);
            this.writeInt32(cooldown.modCooldown);
        }
    }

    public static class CategoryCooldownInfo {

        public int category; // SpellCategory Id
        public int modCooldown; // Reduced Cooldown in ms


        public CategoryCooldownInfo(int category, int cooldown) {
            category = category;
            modCooldown = cooldown;
        }
    }
}
