package com.github.azeroth.game.movement;


import java.util.Comparator;


class MovementGeneratorComparator implements Comparator<MovementGenerator> {
    public final int compare(MovementGenerator a, MovementGenerator b) {
        if (a.equals(b)) {
            return 0;
        }

        if (a.mode.getValue() < b.mode.getValue()) {
            return 1;
        } else if (a.mode == b.mode) {
            if (a.priority.getValue() < b.priority.getValue()) {
                return 1;
            } else if (a.priority == b.priority) {
                return 0;
            }
        }

        return -1;
    }
}
