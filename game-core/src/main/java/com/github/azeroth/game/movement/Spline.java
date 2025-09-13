package com.github.azeroth.game.movement;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.azeroth.game.map.SplineRawInitializer;
import com.github.azeroth.game.movement.model.EvaluationMode;

import java.util.Arrays;

public class Spline {
    private static final matrix4x4 s_catmullRomCoeffs = new matrix4x4(-0.5f, 1.5f, -1.5f, 0.5f, 1.0f, -2.5f, 2.0f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
    private static final Matrix4x4 s_Bezier3Coeffs = new matrix4x4(-1.0f, 3.0f, -3.0f, 1.0f, 3.0f, -6.0f, 3.0f, 0.0f, -3.0f, 3.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
    public EvaluationMode m_mode = EvaluationMode.values()[0];
    private int length;
    private Array<Vector3> points = new Array<>();
    private boolean cyclic;
    private float initialOrientation;

    // could be modified, affects segment length evaluation precision
    // lesser value saves more performance in cost of lover precision
    // minimal value is 1
    // client's value is 20, blizzs use 2-3 steps to compute length
    private int stepsPerSegment = 3;

    private int index_lo;
    private int index_hi;

    public final int getPointCount() {
        return points.length;
    }

    public final Vector3 getPoint(int i) {
        return points[i];
    }

    public final Vector3[] getPoints() {
        return points;
    }

    public final void clear() {
        Arrays.fill(points, 0);
    }

    public final int first() {
        return index_lo;
    }

    public final int last() {
        return index_hi;
    }

    public final boolean isCyclic() {
        return cyclic;
    }

    public final void set_steps_per_segment(int newStepsPerSegment) {
        stepsPerSegment = newStepsPerSegment;
    }

    public final void computeIndex(float t, tangible.RefObject<Integer> index, tangible.RefObject<Float> u) {
        //ASSERT(t >= 0.f && t <= 1.f);
        T length_ = (T) (t * length());
        index.refArgValue = computeIndexInBounds(length_);
        //ASSERT(index < index_hi);
        u.refArgValue = (float) (length_ - length(index.refArgValue)) / (float) length(index.refArgValue, index.refArgValue + 1);
    }

    public final dynamic length() {
        if (lengths.length == 0) {
            return null;
        }

        return lengths[index_hi];
    }

    public final dynamic length(int first, int last) {
        return lengths[last] - (dynamic) lengths[first];
    }

    public final dynamic length(int idx) {
        return lengths[Idx];
    }

    public final void set_length(int i, T length) {
        lengths[i] = length;
    }

    public final void initLengths(IInitializer<T> cacher) {
        var i = index_lo;
        tangible.RefObject<T[]> tempRef_lengths = new tangible.RefObject<T[]>(lengths);
        Array.Resize(tempRef_lengths, index_hi + 1);
        lengths = tempRef_lengths.refArgValue;
        T prev_length;
        T new_length;

        while (i < index_hi) {
            new_length = (dynamic) cacher.invoke(this, i);

            if ((dynamic) new_length < 0) // todo fix me this is a ulgy hack.
            {
                new_length = (dynamic) (type.GetTypeCode(T.class) == TypeCode.Int32 ? Integer.MAX_VALUE : Double.MAX_VALUE);
            }

            lengths[++i] = new_length;

            prev_length = new_length;
        }
    }

    public final void initLengths() {
        var i = index_lo;

        dynamic length = null;
        tangible.RefObject<T[]> tempRef_lengths = new tangible.RefObject<T[]>(lengths);
        Array.Resize(tempRef_lengths, index_hi + 1);
        lengths = tempRef_lengths.refArgValue;

        while (i < index_hi) {
            length += (int) segLength(i);
            lengths[++i] = length;
        }
    }

    public final boolean empty() {
        return index_lo == index_hi;
    }

    private int computeIndexInBounds(T length_) {
        // Temporary disabled: causes infinite loop with t = 1.f
		/*
			index_type hi = index_hi;
			index_type lo = index_lo;

			index_type i = lo + (float)(hi - lo) * t;

			while ((lengths[i] > length) || (lengths[i + 1] <= length))
			{
				if (lengths[i] > length)
					hi = i - 1; // too big
				else if (lengths[i + 1] <= length)
					lo = i + 1; // too small

				i = (hi + lo) / 2;
			}*/

        var i = index_lo;
        var N = index_hi;

        while (i + 1 < N && (dynamic) lengths[i + 1] < length_) {
            ++i;
        }

        return i;
    }

    private void C_Evaluate(Span<Vector3> vertice, float t, Matrix4x4 matr, tangible.OutObject<Vector3> result) {
        Vector4 tvec = new Vector4(t * t * t, t * t, t, 1.0f);
        var weights = Vector4.Transform(tvec, matr);

        result.outArgValue = vertice[0] * weights.X + vertice[1] * weights.Y + vertice[2] * weights.Z + vertice[3] * weights.W;
    }

    private void C_Evaluate_Derivative(Span<Vector3> vertice, float t, Matrix4x4 matr, tangible.OutObject<Vector3> result) {
        Vector4 tvec = new Vector4(3.0f * t * t, 2.0f * t, 1.0f, 0.0f);
        var weights = Vector4.Transform(tvec, matr);

        result.outArgValue = vertice[0] * weights.X + vertice[1] * weights.Y + vertice[2] * weights.Z + vertice[3] * weights.W;
    }


    ///#region Evaluate

    public final void evaluate_Percent(int idx, float u, tangible.OutObject<Vector3> c) {
        switch (m_mode) {
            case Linear:
                evaluateLinear(idx, u, c);

                break;
            case Catmullrom:
                evaluateCatmullRom(idx, u, c);

                break;
            case Bezier3_Unused:
                evaluateBezier3(idx, u, c);

                break;
            default:
                c.outArgValue = new Vector3();

                break;
        }
    }

    private void evaluateLinear(int index, float u, tangible.OutObject<Vector3> result) {
        result.outArgValue = points[index] + (points[index + 1] - points[index]) * u;
    }

    private void evaluateCatmullRom(int index, float t, tangible.OutObject<Vector3> result) {
        Span<Vector3> span = points;

        C_Evaluate(span[(index - 1)..],t, s_catmullRomCoeffs, result);
    }

    private void evaluateBezier3(int index, float t, tangible.OutObject<Vector3> result) {
        index *= (int) 3;
        Span<Vector3> span = points;

        C_Evaluate(span[index..],t, s_Bezier3Coeffs, result);
    }


    ///#endregion


    ///#region Init

    public final void initSplineCustom(SplineRawInitializer initializer) {
        tangible.RefObject<EvaluationMode> tempRef_m_mode = new tangible.RefObject<EvaluationMode>(m_mode);
        tangible.RefObject<Boolean> tempRef__cyclic = new tangible.RefObject<Boolean>(cyclic);
        tangible.RefObject<Vector3[]> tempRef_points = new tangible.RefObject<Vector3[]>(points);
        tangible.RefObject<Integer> tempRef_index_lo = new tangible.RefObject<Integer>(index_lo);
        tangible.RefObject<Integer> tempRef_index_hi = new tangible.RefObject<Integer>(index_hi);
        initializer.initialize(tempRef_m_mode, tempRef__cyclic, tempRef_points, tempRef_index_lo, tempRef_index_hi);
        index_hi = tempRef_index_hi.refArgValue;
        index_lo = tempRef_index_lo.refArgValue;
        points = tempRef_points.refArgValue;
        cyclic = tempRef__cyclic.refArgValue;
        m_mode = tempRef_m_mode.refArgValue;
    }


    public final void initCyclicSpline(Vector3[] controls, int count, EvaluationMode m, int cyclic_point) {
        initCyclicSpline(controls, count, m, cyclic_point, 0f);
    }

    public final void initCyclicSpline(Vector3[] controls, int count, EvaluationMode m, int cyclic_point, float orientation) {
        m_mode = m;
        cyclic = true;

        initSpline(controls, count, m, orientation);
    }


    public final void initSpline(Span<Vector3> controls, int count, EvaluationMode m) {
        initSpline(controls, count, m, 0f);
    }

    public final void initSpline(Span<Vector3> controls, int count, EvaluationMode m, float orientation) {
        m_mode = m;
        cyclic = false;
        initialOrientation = orientation;

        switch (m_mode) {
            case Linear:
            case Catmullrom:
                initCatmullRom(controls, count, cyclic, 0);

                break;
            case Bezier3_Unused:
                initBezier3(controls, count, cyclic, 0);

                break;
            default:
                break;
        }
    }

    private void initLinear(Vector3[] controls, int count, boolean cyclic, int cyclic_point) {
        var real_size = count + 1;

        tangible.RefObject<T[]> tempRef_points = new tangible.RefObject<T[]>(points);
        Array.Resize(tempRef_points, real_size);
        points = tempRef_points.refArgValue;
        system.arraycopy(controls, 0, points, 0, count);

        // first and last two indexes are space for special 'virtual points'
        // these points are required for proper C_Evaluate and C_Evaluate_Derivative methtod work
        if (cyclic) {
            points[count] = controls[cyclic_point];
        } else {
            points[count] = controls[count - 1];
        }

        index_lo = 0;
        index_hi = cyclic ? count : (count - 1);
    }

    private void initCatmullRom(Span<Vector3> controls, int count, boolean cyclic, int cyclic_point) {
        var real_size = count + (cyclic ? (1 + 2) : (1 + 1));

        points = new Vector3[real_size];

        var lo_index = 1;
        var high_index = lo_index + count - 1;

        system.arraycopy(controls.ToArray(), 0, points, lo_index, count);

        // first and last two indexes are space for special 'virtual points'
        // these points are required for proper C_Evaluate and C_Evaluate_Derivative methtod work
        if (cyclic) {
            if (cyclic_point == 0) {
                points[0] = controls[count - 1];
            } else {
                points[0] = controls[0] - new Vector3((float) Math.cos(initialOrientation), (float) Math.sin(initialOrientation), 0.0f);
            }

            points[high_index + 1] = controls[cyclic_point];
            points[high_index + 2] = controls[cyclic_point + 1];
        } else {
            points[0] = controls[0] - new Vector3((float) Math.cos(initialOrientation), (float) Math.sin(initialOrientation), 0.0f);
            points[high_index + 1] = controls[count - 1];
        }

        index_lo = lo_index;
        index_hi = high_index + (cyclic ? 1 : 0);
    }

    private void initBezier3(Span<Vector3> controls, int count, boolean cyclic, int cyclic_point) {
        var c = (int) (count / 3 * 3);
        var t = (int) (c / 3);

        tangible.RefObject<T[]> tempRef_points = new tangible.RefObject<T[]>(points);
        Array.Resize(tempRef_points, c);
        points = tempRef_points.refArgValue;
        system.arraycopy(controls.ToArray(), 0, points, 0, c);

        index_lo = 0;
        index_hi = t - 1;
    }


    ///#endregion


    ///#region EvaluateDerivative

    public final void evaluate_Derivative(int idx, float u, tangible.OutObject<Vector3> hermite) {
        switch (m_mode) {
            case Linear:
                evaluateDerivativeLinear(idx, u, hermite);

                break;
            case Catmullrom:
                evaluateDerivativeCatmullRom(idx, u, hermite);

                break;
            case Bezier3_Unused:
                evaluateDerivativeBezier3(idx, u, hermite);

                break;
            default:
                hermite.outArgValue = new Vector3();

                break;
        }
    }

    private void evaluateDerivativeLinear(int index, float t, tangible.OutObject<Vector3> result) {
        result.outArgValue = points[index + 1] - points[index];
    }

    private void evaluateDerivativeCatmullRom(int index, float t, tangible.OutObject<Vector3> result) {
        Span<Vector3> span = points;

        C_Evaluate_Derivative(span[(index - 1)..],t, s_catmullRomCoeffs, result);
    }

    private void evaluateDerivativeBezier3(int index, float t, tangible.OutObject<Vector3> result) {
        index *= (int) 3;
        Span<Vector3> span = points;

        C_Evaluate_Derivative(span[index..],t, s_Bezier3Coeffs, result);
    }


    ///#endregion


    ///#region SegLength

    public final float segLength(int i) {
        switch (m_mode) {
            case Linear:
                return segLengthLinear(i);
            case Catmullrom:
                return segLengthCatmullRom(i);
            case Bezier3_Unused:
                return segLengthBezier3(i);
            default:
                return 0;
        }
    }

    private float segLengthLinear(int index) {
        return (points[index] - points[index + 1]).length();
    }

    private float segLengthCatmullRom(int index) {
        var p = points.AsSpan(index - 1);
        var curPos = p[1];

        var i = 1;
        double length = 0;

        while (i <= stepsPerSegment) {
            Vector3 nextPos;
            tangible.OutObject<Vector3> tempOut_nextPos = new tangible.OutObject<Vector3>();
            C_Evaluate(p, i / (float) stepsPerSegment, s_catmullRomCoeffs, tempOut_nextPos);
            nextPos = tempOut_nextPos.outArgValue;
            length += (nextPos - curPos).length();
            curPos = nextPos;
            ++i;
        }

        return (float) length;
    }

    private float segLengthBezier3(int index) {
        index *= (int) 3;

        var p = points.AsSpan(index);

        Vector3 nextPos;
        tangible.OutObject<Vector3> tempOut_nextPos = new tangible.OutObject<Vector3>();
        C_Evaluate(p, 0.0f, s_Bezier3Coeffs, tempOut_nextPos);
        nextPos = tempOut_nextPos.outArgValue;
        var curPos = nextPos;

        var i = 1;
        double length = 0;

        while (i <= stepsPerSegment) {
            tangible.OutObject<Vector3> tempOut_nextPos2 = new tangible.OutObject<Vector3>();
            C_Evaluate(p, i / (float) stepsPerSegment, s_Bezier3Coeffs, tempOut_nextPos2);
            nextPos = tempOut_nextPos2.outArgValue;
            length += (nextPos - curPos).length();
            curPos = nextPos;
            ++i;
        }

        return (float) length;
    }


    ///#endregion
}
