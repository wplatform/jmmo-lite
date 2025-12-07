package com.github.azeroth.game.ai.enums;

import com.github.azeroth.game.entity.unit.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NonTankTargetSelector {
    private Unit source;
    private boolean playerOnly;
}
