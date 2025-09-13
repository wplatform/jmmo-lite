package com.github.azeroth.game.entity.player;


import com.github.azeroth.game.entity.player.enums.PlayerSpellState;

public class PlayerSpell {
    public PlayerSpellState state;
    public boolean active;
    public boolean dependent;
    public boolean disabled;
    public boolean favorite;
    public Integer traitDefinitionId = null;
}
