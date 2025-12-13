package com.github.azeroth.game.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.Assert;
import com.github.azeroth.game.movement.model.EvaluationMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Getter
@Setter
@ToString
public class Spline {


    private List<Vector3> points = new ArrayList<>();
    private List<Float> lengths = new ArrayList<>();


    private int index_lo;
    private int index_hi;

    private EvaluationMode m_mode;
    private boolean cyclic;
    private float initialOrientation;

    // steps per segment (precision)
    private int stepsPerSegment = 3;

    // Coefficient matrices (rows as in C++ initializer)
    private static final float[][] CATMULL_ROM = new float[][]{
            { -0.5f, 1.5f, -1.5f, 0.5f },
            { 1.0f, -2.5f, 2.0f, -0.5f },
            { -0.5f, 0.0f, 0.5f, 0.0f },
            { 0.0f, 1.0f, 0.0f, 0.0f }
    };

    private static final float[][] BEZIER_3 = new float[][]{
            { -1.0f, 3.0f, -3.0f, 1.0f },
            { 3.0f, -6.0f, 3.0f, 0.0f },
            { -3.0f, 3.0f, 0.0f, 0.0f },
            { 1.0f, 0.0f, 0.0f, 0.0f }
    };

    public Spline() {
        index_lo = 0;
        index_hi = 0;
        m_mode = EvaluationMode.UninitializedMode;
        cyclic = false;
        initialOrientation = 0f;
    }

    public void evaluate_percent(int idx, float u, Vector3 out) {
        switch (m_mode) {
            case Linear: EvaluateLinear(idx, u, out); break;
            case Catmullrom: EvaluateCatmullRom(idx, u, out); break;
            case Bezier3_Unused: EvaluateBezier3(idx, u, out); break;
            default: throw new IllegalStateException("Uninitialized spline mode");
        }
    }

    public void evaluate_derivative(int idx, float u, Vector3 out) {
        switch (m_mode) {
            case Linear: EvaluateDerivativeLinear(idx, u, out); break;
            case Catmullrom: EvaluateDerivativeCatmullRom(idx, u, out); break;
            case Bezier3_Unused: EvaluateDerivativeBezier3(idx, u, out); break;
            default: throw new IllegalStateException("Uninitialized spline mode");
        }
    }

    public int first() { return index_lo; }
    public int last() { return index_hi; }
    public boolean empty() { return index_lo == index_hi; }
    public EvaluationMode mode() { return m_mode; }

    public void setLength(int i, float length) { lengths.set(i, length);}


    public int getPointCount() { return points.size(); }
    public Vector3 getPoint(int i) { return points.get(i); }

    public void init_spline(final Vector3[] controls, int count, EvaluationMode m, float orientation) {
        m_mode = m;
        cyclic = false;
        initialOrientation = orientation;
        initCatmullRomInternal(controls, count, 0);
        if (m_mode == EvaluationMode.Bezier3_Unused) {
            initBezier3Internal(controls, count, 0);
        } else {
            // As in C++ they use CatmullRom initializer even for linear mode
            // we should use catmullrom initializer even for linear mode! (client's internal structure limitation)
            initCatmullRomInternal(controls, count, 0);
        }
    }

    public void init_cyclic_spline(final Vector3[] controls, int count, EvaluationMode m, int cyclic_point, float orientation) {
        m_mode = m;
        cyclic = true;
        initialOrientation = orientation;
        if (m_mode == EvaluationMode.Bezier3_Unused) {
            initBezier3Internal(controls, count, cyclic_point);
        } else {
            initCatmullRomInternal(controls, count, cyclic_point);
        }
    }


    // Segment length dispatcher
    public float SegLength(int i) {
        return switch (m_mode) {
            case Linear -> SegLengthLinear(i);
            case Catmullrom -> SegLengthCatmullRom(i);
            case Bezier3_Unused -> SegLengthBezier3(i);
            default -> throw new IllegalStateException("Uninitialized spline mode");
        };
    }

    public void set_steps_per_segment(int s) { stepsPerSegment = s; }


    // --- Implementation methods ported from Spline.cpp ---
    protected void EvaluateLinear(int index, float u, Vector3 result) {
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3 p0 = points.get(index);
        Vector3 p1 = points.get(index+1);
        result.set(p1).sub(p0).scl(u).add(p0);
    }

    protected void EvaluateCatmullRom(int index, float t, Vector3 result) {
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        // vertices: points[index-1 .. index+2]
        Vector3[] v = new Vector3[4];
        for (int i = 0; i < 4; ++i) v[i] = points.get(index - 1 + i);
        cEvaluate(v, t, CATMULL_ROM, result);
    }

    protected void EvaluateBezier3(int index, float t, Vector3 result) {
        index *= 3;
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3[] v = new Vector3[4];
        for (int i = 0; i < 4; ++i) v[i] = points.get(index + i);
        cEvaluate(v, t, BEZIER_3, result);
    }

    protected void EvaluateDerivativeLinear(int index, float unused, Vector3 result) {
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3 p0 = points.get(index);
        Vector3 p1 = points.get(index+1);
        result.set(p1).sub(p0);
    }

    protected void EvaluateDerivativeCatmullRom(int index, float t, Vector3 result) {
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3[] v = new Vector3[4];
        for (int i = 0; i < 4; ++i) v[i] = points.get(index - 1 + i);
        cEvaluateDerivative(v, t, CATMULL_ROM, result);
    }

    protected void EvaluateDerivativeBezier3(int index, float t, Vector3 result) {
        index *= 3;
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3[] v = new Vector3[4];
        for (int i = 0; i < 4; ++i) v[i] = points.get(index + i);
        cEvaluateDerivative(v, t, BEZIER_3, result);
    }

    protected float SegLengthLinear(int index) {
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        return points.get(index).dst(points.get(index+1));
    }

    protected float SegLengthCatmullRom(int index) {
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3 curPos = new Vector3();
        Vector3 nextPos = new Vector3();
        Vector3[] p = new Vector3[4];
        for (int i = 0; i < 4; ++i) p[i] = points.get(index - 1 + i);
        curPos.set(p[1]);
        nextPos.set(curPos);
        int i = 1;
        float length = 0f;
        while (i <= stepsPerSegment) {
            cEvaluate(p, (float)i / (float)stepsPerSegment, CATMULL_ROM, nextPos);
            length += nextPos.dst(curPos);
            curPos.set(nextPos);
            ++i;
        }
        return length;
    }

    protected float SegLengthBezier3(int index) {
        index *= 3;
        if (!(index >= index_lo && index < index_hi)) throw new IllegalArgumentException("index out of range");
        Vector3 curPos = new Vector3();
        Vector3 nextPos = new Vector3();
        Vector3[] p = new Vector3[4];
        for (int i = 0; i < 4; ++i) p[i] = points.get(index + i);
        cEvaluate(p, 0f, BEZIER_3, nextPos);
        curPos.set(nextPos);
        int i = 1;
        float length = 0f;
        while (i <= stepsPerSegment) {
            cEvaluate(p, (float)i / (float)stepsPerSegment, BEZIER_3, nextPos);
            length += nextPos.dst(curPos);
            curPos.set(nextPos);
            ++i;
        }
        return length;
    }

    // Initializers
    protected void InitLinearInternal(final Vector3[] controls, int count, int cyclic_point) {
        if (count < 2) throw new IllegalArgumentException("count must be >=2");
        int real_size = count + 1;
        points = new ArrayList<>(real_size);
        for (int i = 0; i < count; ++i) points.add(new Vector3(controls[i]));
        if (cyclic) points.add(new Vector3(controls[cyclic_point])); else points.add(new Vector3(controls[count-1]));
        index_lo = 0; index_hi = cyclic ? count : (count - 1);
    }

    protected void initCatmullRomInternal(final Vector3[] controls, int count, int cyclic_point) {
        int real_size = count + (cyclic ? (1+2) : (1+1));
        points = new ArrayList<>(real_size);
        int lo_index = 1;
        int high_index = lo_index + count - 1;
        // fill with placeholders to ensure capacity
        for (int i = 0; i < real_size; ++i) points.add(new Vector3());
        for (int i = 0; i < count; ++i) points.set(lo_index + i, new Vector3(controls[i]));
        if (cyclic) {
            if (cyclic_point == 0) points.set(0, new Vector3(controls[count-1]));
            else points.set(0, new Vector3(controls[0]).sub((float)Math.cos(initialOrientation), (float)Math.sin(initialOrientation), 0f));
            points.set(high_index+1, new Vector3(controls[cyclic_point]));
            points.set(high_index+2, new Vector3(controls[cyclic_point+1]));
        } else {
            points.set(0, new Vector3(controls[0]).sub((float)Math.cos(initialOrientation), (float)Math.sin(initialOrientation), 0f));
            points.set(high_index+1, new Vector3(controls[count-1]));
        }
        index_lo = lo_index;
        index_hi = high_index + (cyclic ? 1 : 0);
    }

    protected void initBezier3Internal(final Vector3[] controls, int count, int ignored) {
        int c = (count / 3) * 3;
        int t = c / 3;
        points = new ArrayList<>(c);
        for (int i = 0; i < c; ++i) points.add(new Vector3(controls[i]));
        index_lo = 0; index_hi = t-1;
    }



    public void evaluate_percent(float t, Vector3 c) {
        if (t < 0f || t > 1f) throw new IllegalArgumentException("t must be in [0,1]");
        float length_ = t * length();
        int out_idx = computeIndexInBoundsAtLength(length_);
        if (out_idx >= index_hi) out_idx = index_hi - 1;
        float segLen = length(out_idx, out_idx+1);
        float out_u = segLen > 0f ? (length_ - length(out_idx)) / segLen : 0f;
        evaluate_percent(out_idx, out_u, c);
    }

    public void evaluate_derivative(float t, Vector3 hermite) {
        if (t < 0f || t > 1f) throw new IllegalArgumentException("t must be in [0,1]");
        float length_ = t * length();
        int out_idx = computeIndexInBoundsAtLength(length_);
        if (out_idx >= index_hi) out_idx = index_hi - 1;
        float segLen = length(out_idx, out_idx+1);
        float out_u = segLen > 0f ? (length_ - length(out_idx)) / segLen : 0f;
        evaluate_derivative(out_idx, out_u, hermite);
    }

    public int computeIndexInBoundsAtLength(float length_) {
        int i = index_lo;
        int N = index_hi;
        while (i + 1 < N && lengths.get(i + 1) < length_) ++i;
        return i;
    }

    public int computeIndexInBounds(float t) {
        if (t < 0f || t > 1f) throw new IllegalArgumentException("t must be in [0,1]");
        return computeIndexInBoundsAtLength(t * length());
    }

    public void initLengths() {
        int i = index_lo;
        float len = 0f;
        lengths = new ArrayList<>(index_hi + 1);
        for (int k = 0; k <= index_hi; ++k) lengths.add(0f);
        while (i < index_hi) {
            len += SegLength(i);
            lengths.set(++i, len);
        }
    }

    public void initLengths(BiFunction<Spline, Integer, Float> cacher) {
        int i = index_lo;
        lengths = new ArrayList<>(index_hi + 1);
        for (int k = 0; k <= index_hi; ++k) lengths.add(0f);
        float prev_length = 0, new_length = 0;
        while (i < index_hi)
        {
            new_length = cacher.apply(this, i);
            // length overflowed, assign to max positive value
            if (new_length < 0)
                new_length = Float.MAX_VALUE;
            lengths.set(++i, new_length);

            Assert.isTrue(prev_length <= new_length);
            prev_length = new_length;
        }
    }

    public void clear() {
        index_lo = 0;
        index_hi = 0;
        points.clear();
        lengths.clear();
    }

    public float length() {
        if (lengths.isEmpty()) return 0f;
        return lengths.getLast();
    }

    public float length(int idx) {
        if (idx < 0 || idx >= lengths.size()) return 0f;
        return lengths.get(idx);
    }

    public float length(int idxLo, int idxHi) {
        if (idxLo < 0 || idxHi >= lengths.size() || idxLo > idxHi) return 0f;
        return lengths.get(idxHi) - lengths.get(idxLo);
    }

    // -- helpers to evaluate using coefficient matrix (rows) like C++ code --
    private static void cEvaluate(Vector3[] vertice, float t, float[][] matr, Vector3 result) {
        float t2 = t * t;
        float t3 = t2 * t;
        float[] tvec = new float[] { t3, t2, t, 1f };
        float[] weights = new float[4];
        for (int k = 0; k < 4; ++k) {
            float wk = 0f;
            for (int i = 0; i < 4; ++i) wk += tvec[i] * matr[i][k];
            weights[k] = wk;
        }
        result.set(0f,0f,0f);
        result.add(vertice[0].x * weights[0], vertice[0].y * weights[0], vertice[0].z * weights[0]);
        result.add(vertice[1].x * weights[1], vertice[1].y * weights[1], vertice[1].z * weights[1]);
        result.add(vertice[2].x * weights[2], vertice[2].y * weights[2], vertice[2].z * weights[2]);
        result.add(vertice[3].x * weights[3], vertice[3].y * weights[3], vertice[3].z * weights[3]);
    }

    private static void cEvaluateDerivative(Vector3[] vertice, float t, float[][] matr, Vector3 result) {
        float t2 = t * t;
        float[] tvec = new float[] { 3f * t2, 2f * t, 1f, 0f };
        float[] weights = new float[4];
        for (int k = 0; k < 4; ++k) {
            float wk = 0f;
            for (int i = 0; i < 4; ++i) wk += tvec[i] * matr[i][k];
            weights[k] = wk;
        }
        result.set(0f,0f,0f);
        result.add(vertice[0].x * weights[0], vertice[0].y * weights[0], vertice[0].z * weights[0]);
        result.add(vertice[1].x * weights[1], vertice[1].y * weights[1], vertice[1].z * weights[1]);
        result.add(vertice[2].x * weights[2], vertice[2].y * weights[2], vertice[2].z * weights[2]);
        result.add(vertice[3].x * weights[3], vertice[3].y * weights[3], vertice[3].z * weights[3]);
    }
}
