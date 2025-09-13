package com.github.azeroth.game.map;

import com.github.azeroth.game.movement.model.EvaluationMode;

import java.util.ArrayList;

public class SplineRawInitializer {
    private final ArrayList<Vector3> points;

    public SplineRawInitializer(ArrayList<Vector3> points) {
        points = points;
    }

    public final void initialize(tangible.RefObject<EvaluationMode> mode, tangible.RefObject<Boolean> cyclic, tangible.RefObject<Vector3[]> points, tangible.RefObject<Integer> lo, tangible.RefObject<Integer> hi) {
        mode.refArgValue = EvaluationMode.Catmullrom;
        cyclic.refArgValue = false;
        points.refArgValue = new Vector3[_points.size()];

        for (var i = 0; i < points.size(); ++i) {
            points.refArgValue[i] = points.get(i);
        }

        lo.refArgValue = 1;
        hi.refArgValue = points.refArgValue.length - 2;
    }
}
