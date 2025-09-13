package com.github.azeroth.game.domain.condition;

import java.util.ArrayList;
import java.util.List;

public class ConditionsReference {

    public List<Condition> conditions;

    public boolean isEmpty() {
        return conditions == null || conditions.isEmpty();
    }

    public void add(Condition condition) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        conditions.add(condition);
    }
}
