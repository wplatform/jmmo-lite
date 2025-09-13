package com.github.azeroth.game.entity.player.model;


import com.github.azeroth.game.entity.player.PlayerDefine;
import com.github.azeroth.game.entity.player.enums.RuneType;
import lombok.Data;

@Data
public class Runes {
    public final RuneInfo[] runes = new RuneInfo[PlayerDefine.MAX_RUNES];
    private byte runeState;                                        // mask of available runes
    private RuneType lastUsedRune;
    private byte lastUsedRuneMask;

    public void setRuneState(int index) {
        setRuneState(index, true);
    };

    public void setRuneState(int index, boolean set) {
        if (set) {
            runeState |= (byte) (1 << index);
        } else {
            runeState &= (byte) ~(1 << index);
        }
    }
}
