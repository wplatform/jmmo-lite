package com.github.azeroth.game.domain.condition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
public final class ConditionTypeInfo {
    public final String name;
    public final boolean hasConditionValue1;
    public final boolean hasConditionValue2;
    public final boolean hasConditionValue3;
    public final boolean hasConditionStringValue1;

}
