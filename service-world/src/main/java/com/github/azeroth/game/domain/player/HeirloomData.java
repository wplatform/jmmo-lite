package com.github.azeroth.game.domain.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeirloomData {
    private int heirloomPlayerFlags;
    private int bonusId;
}
