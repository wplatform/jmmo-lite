package com.github.azeroth.game.domain.condition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class ConditionId {
    public int sourceGroup;
    public int sourceEntry;
    public int sourceId;
}
