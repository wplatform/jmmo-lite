package com.github.azeroth.game.ai.enums;

import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.SpellInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpellTargetSelector {
    private Unit caster;
    private SpellInfo spellInfo;
}
