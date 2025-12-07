package com.github.azeroth.game.ai.enums;

import com.github.azeroth.defines.Power;
import com.github.azeroth.game.entity.unit.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PowerUsersSelector {
    private Unit me;
    private Power power;
    private float dist;
    private boolean playerOnly;
}
