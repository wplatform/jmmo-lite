package com.github.azeroth.game.map.collision;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.IntArray;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.map.model.GameObjectModel;
import com.github.azeroth.utils.MathUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.function.BiConsumer;


public class BIH {

    public static final int MAX_STACK_SIZE = 64;
    private BoundingBox bounds;
    private int[] tree;
    private int[] objects;

    public BIH() {
        initEmpty();
    }

    public static float getAxisValue(Vector3 vector3, int axis) {
        return switch (axis) {
            case 0 -> vector3.x;
            case 1 -> vector3.y;
            case 2 -> vector3.z;
            default -> throw new IllegalStateException("Unexpected value: " + axis);
        };
    }

    public static void setAxisValue(Vector3 vector3, float value, int axis) {
        switch (axis) {
            case 0 -> vector3.x = value;
            case 1 -> vector3.y = value;
            case 2 -> vector3.z = value;
            default -> throw new IllegalStateException("Unexpected value: " + axis);
        }
    }

    public final boolean readFromFile(ByteBuffer buffer) {

        var lo = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        var hi = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        bounds = new BoundingBox(lo, hi);

        var treeSize = buffer.getInt();
        tree = new int[treeSize];

        IntBuffer intBuffer = buffer.asIntBuffer();
        intBuffer.get(tree);
        var count = intBuffer.get();
        objects = new int[count];
        intBuffer.get(objects);

        return true;
    }

    public final void build(ArrayList<GameObjectModel> primitives) {
        build(primitives, 3);
    }

    public final void build(ArrayList<GameObjectModel> primitives, int leafSize) {
        if (primitives.isEmpty()) {
            initEmpty();

            return;
        }

        BuildData dat = new BuildData();
        dat.maxPrims = leafSize;
        dat.numPrims = primitives.size();
        dat.indices = new int[dat.numPrims];
        dat.primBound = new BoundingBox[dat.numPrims];
        bounds = primitives.getFirst().getBoundingBox();

        for (var i = 0; i < dat.numPrims; ++i) {
            dat.indices[i] = i;
            dat.primBound[i] = primitives.get(i).getBoundingBox();

            bounds.min.x = Math.min(bounds.min.x, dat.primBound[i].min.x);
            bounds.min.y = Math.min(bounds.min.y, dat.primBound[i].min.y);
            bounds.min.z = Math.min(bounds.min.z, dat.primBound[i].min.z);

            bounds.max.x = Math.max(bounds.max.x, dat.primBound[i].max.x);
            bounds.max.y = Math.max(bounds.max.y, dat.primBound[i].max.y);
            bounds.max.z = Math.max(bounds.max.z, dat.primBound[i].max.z);
        }

        bounds.update();

        IntArray tempTree = new IntArray();
        BuildStats stats = new BuildStats();
        buildHierarchy(tempTree, dat, stats);

        objects = new int[dat.numPrims];

        System.arraycopy(dat.indices, 0, objects, 0, dat.numPrims);

        tree = tempTree.items;

        stats.printStats();
    }

    public final int primCount() {
        return objects.length;
    }

    public final void intersectRay(Ray r, Distance maxDist, RayIntersect rayIntersect) {
        intersectRay(r, maxDist, false, rayIntersect);
    }

    public final void intersectRay(Ray r, Distance maxDist, boolean stopAtFirst, RayIntersect rayIntersect) {


        float intervalMin = -1.f;
        float intervalMax = -1.f;
        Vector3 org = r.origin;
        Vector3 dir = r.direction;
        // inverse direction = 1 / direction;
        Vector3 invDir = new Vector3(1 / dir.x, 1 / dir.y, 1 / dir.z);
        for (int i = 0; i < 3; ++i) {
            if (MathUtil.fuzzyNe(getAxisValue(dir, i), 0.0f)) {
                float t1 = (getAxisValue(bounds.min, i) - getAxisValue(org, i)) * getAxisValue(invDir, i);
                float t2 = (getAxisValue(bounds.max, i) - getAxisValue(org, i)) * getAxisValue(invDir, i);
                if (t1 > t2) {
                    float tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }
                if (t1 > intervalMin)
                    intervalMin = t1;
                if (t2 < intervalMax || intervalMax < 0.f)
                    intervalMax = t2;
                // intervalMax can only become smaller for other axis,
                //  and intervalMin only larger respectively, so stop early
                if (intervalMax <= 0 || intervalMin >= maxDist.distance)
                    return;
            }
        }
        if (intervalMin > intervalMax)
            return;
        intervalMin = Math.max(intervalMin, 0.f);
        intervalMax = Math.min(intervalMax, maxDist.distance);

        int[] offsetFront = new int[3];
        int[] offsetBack = new int[3];
        int[] offsetFront3 = new int[3];
        int[] offsetBack3 = new int[3];

        // compute custom offsets from direction sign bit

        for (int i = 0; i < 3; ++i) {
            offsetFront[i] = Float.floatToRawIntBits(getAxisValue(dir, i)) >> 31;
            offsetBack[i] = offsetFront[i] ^ 1;
            offsetFront3[i] = offsetFront[i] * 3;
            offsetBack3[i] = offsetBack[i] * 3;

            // avoid always adding 1 during the inner loop
            ++offsetFront[i];
            ++offsetBack[i];
        }

        StackNode[] stack = new StackNode[MAX_STACK_SIZE];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new StackNode();
        }
        int stackPos = 0;
        int node = 0;

        while (true) {
            while (true) {
                int tn = tree[node];
                int axis = (tn & (3 << 30)) >>> 30;
                boolean isBvh2 = (tn & (1 << 29)) != 0;
                int offset = tn & ~(7 << 29);

                if (!isBvh2) {
                    if (axis < 3) {
                        // "normal" interior node
                        float tf = (Float.intBitsToFloat(tree[node + offsetFront[axis]]) - getAxisValue(org, axis) * getAxisValue(invDir, axis));
                        float tb = (Float.intBitsToFloat(tree[node + offsetBack[axis]]) - getAxisValue(org, axis) * getAxisValue(invDir, axis));
                        // ray passes between clip zones
                        if (tf < intervalMin && tb > intervalMax)
                            break;
                        int back = offset + offsetBack3[axis];
                        node = back;
                        // ray passes through far node only
                        if (tf < intervalMin) {
                            intervalMin = Math.max(tb, intervalMin);
                            continue;
                        }
                        node = offset + offsetFront3[axis]; // front
                        // ray passes through near node only
                        if (tb > intervalMax) {
                            intervalMax = Math.min(tf, intervalMax);
                            continue;
                        }
                        // ray passes through both nodes
                        // push back node
                        stack[stackPos].node = back;
                        stack[stackPos].near = Math.max(tb, intervalMin);
                        stack[stackPos].far = intervalMax;
                        stackPos++;
                        // update ray interval for front node
                        intervalMax = Math.min(tf, intervalMax);
                    } else {
                        // leaf - test some objects
                        int n = tree[node + 1];
                        while (n > 0) {
                            boolean hit = rayIntersect.apply(r, objects[offset], maxDist, stopAtFirst);
                            if (stopAtFirst && hit)
                                return;
                            --n;
                            ++offset;
                        }
                        break;
                    }
                }

            } // traversal loop
            do {
                // stack is empty?
                if (stackPos == 0)
                    return;
                // move back up the stack
                stackPos--;
                intervalMin = stack[stackPos].near;
                if (maxDist.distance < intervalMin)
                    continue;
                node = stack[stackPos].node;
                intervalMax = stack[stackPos].far;
                break;
            } while (true);
        }
    }

    public final void intersectPoint(Vector3 p, BiConsumer<Vector3, Integer> intersect) {
        if (!bounds.contains(p)) {
            return;
        }

        var stack = new StackNode[64];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new StackNode();
        }
        var stackPos = 0;
        var node = 0;

        while (true) {
            while (true) {
                var tn = tree[node];
                var axis = (tn & (3 << 30)) >>> 30;
                var isBvh2 = (tn & (1 << 29)) != 0;
                var offset = tn & ~(7 << 29);

                if (!isBvh2) {
                    if (axis < 3) {
                        // "normal" interior node
                        var tl = Float.intBitsToFloat(tree[node + 1]);
                        var tr = Float.intBitsToFloat(tree[node + 2]);

                        // point is between clip zones
                        if (tl < getAxisValue(p, axis) && tr > getAxisValue(p, axis)) {
                            break;
                        }

                        var right = offset + 3;
                        node = right;

                        // point is in right node only
                        if (tl < getAxisValue(p, axis)) {
                            continue;
                        }

                        node = offset; // left

                        // point is in left node only
                        if (tr > getAxisValue(p, axis)) {
                            continue;
                        }

                        // point is in both nodes
                        // push back right node
                        stack[stackPos].node = right;
                        stackPos++;

                    } else {
                        // leaf - test some objects
                        var n = tree[node + 1];

                        while (n > 0) {
                            intersect.accept(p, objects[offset]); // !!!
                            --n;
                            ++offset;
                        }

                        break;
                    }
                } else // isBvh2 node (empty space cut off left and right)
                {
                    if (axis > 2) {
                        return; // should not happen
                    }

                    var tl = Float.intBitsToFloat(tree[node + 1]);
                    var tr = Float.intBitsToFloat(tree[node + 2]);
                    node = offset;

                    if (tl > getAxisValue(p, axis) || tr < getAxisValue(p, axis)) {
                        break;
                    }

                }
            } // traversal loop

            // stack is empty?
            if (stackPos == 0) {
                return;
            }

            // move back up the stack
            stackPos--;
            node = stack[stackPos].node;
        }
    }

    private void initEmpty() {
        tree = new int[3];
        tree[0] = (3 << 30);
    }

    private void buildHierarchy(IntArray tempTree, BuildData dat, BuildStats stats) {
        // create space for the first node
        tempTree.add(3 << 30); // dummy leaf
        tempTree.add(0);
        tempTree.add(0);

        //seed bbox
        BoundingBox gridBox = new BoundingBox(bounds.min, bounds.max);

        BoundingBox nodeBox = gridBox;

        if (objects.length == 0)
            return;

        // seed subdivide function
        subdivide(0, dat.numPrims - 1, tempTree, dat, gridBox, nodeBox, 0, 1, stats);

    }

    private void subdivide(int left, int right, IntArray tempTree, BuildData dat, BoundingBox gridBox, BoundingBox nodeBox, int nodeIndex, int depth, BuildStats stats) {
        if ((right - left + 1) <= dat.maxPrims || depth >= MAX_STACK_SIZE) {
            // write leaf node
            stats.updateLeaf(depth, right - left + 1);
            createNode(tempTree, nodeIndex, left, right);
            return;
        }

        // calculate extents
        int axis = -1, prevAxis, rightOrig;
        float clipL, clipR, prevClip = Float.NaN;
        float split = Float.NaN, prevSplit;
        var wasLeft = true;

        while (true) {
            prevAxis = axis;
            prevSplit = split;
            // perform quick consistency checks
            Vector3 d = gridBox.max.sub(gridBox.min);
            if (d.x < 0 || d.y < 0 || d.z < 0)
                throw new IllegalStateException("Negative node extents");
            for (int i = 0; i < 3; i++) {
                if (getAxisValue(nodeBox.max, i) < getAxisValue(gridBox.min, i) || getAxisValue(nodeBox.min, i) > getAxisValue(gridBox.max, i)) {
                    //UI.printError(Module.ACCEL, "Reached tree area in error - discarding node with: %d objects", right - left + 1);
                    throw new IllegalStateException("Invalid node overlap");
                }
            }
            // find the longest axis
            if (d.x > d.y && d.x > d.z)
                axis = 0;
            else if (d.y > d.z)
                axis = 1;
            else
                axis = 2;
            split = 0.5f * (getAxisValue(gridBox.min, axis) + getAxisValue(gridBox.max, axis));

            // partition L/R subsets
            clipL = Float.NEGATIVE_INFINITY;
            clipR = Float.POSITIVE_INFINITY;
            rightOrig = right; // save this for later
            var nodeL = Float.POSITIVE_INFINITY;
            var nodeR = Float.NEGATIVE_INFINITY;

            for (var i = left; i <= right; ) {

                int obj = dat.indices[i];
                float minb = getAxisValue(dat.primBound[obj].min, axis);
                float maxb = getAxisValue(dat.primBound[obj].max, axis);
                float center = (minb + maxb) * 0.5f;
                if (center <= split) {
                    // stay left
                    i++;
                    if (clipL < maxb)
                        clipL = maxb;
                } else {
                    // move to the right most
                    int t = dat.indices[i];
                    dat.indices[i] = dat.indices[right];
                    dat.indices[right] = t;
                    right--;
                    if (clipR > minb)
                        clipR = minb;
                }
                nodeL = Math.min(nodeL, minb);
                nodeR = Math.max(nodeR, maxb);


            }

            // check for empty space
            if (nodeL > getAxisValue(nodeBox.min, axis) && nodeR < getAxisValue(nodeBox.max, axis)) {
                float nodeBoxW = getAxisValue(nodeBox.max, axis) - getAxisValue(nodeBox.min, axis);
                float nodeNewW = nodeR - nodeL;
                // node box is too big compare to space occupied by primitives?
                if (1.3f * nodeNewW < nodeBoxW) {
                    stats.updateBVH2();
                    int nextIndex = tempTree.size;
                    // allocate child
                    tempTree.add(0);
                    tempTree.add(0);
                    tempTree.add(0);
                    // write bvh2 clip node
                    stats.updateInner();
                    tempTree.set(nodeIndex, (axis << 30) | (1 << 29) | nextIndex);
                    tempTree.set(nodeIndex + 1, Float.floatToRawIntBits(nodeL));
                    tempTree.set(nodeIndex + 2, Float.floatToRawIntBits(nodeR));
                    // update nodebox and recurse
                    setAxisValue(nodeBox.min, nodeL, axis);
                    setAxisValue(nodeBox.max, nodeR, axis);
                    subdivide(left, rightOrig, tempTree, dat, gridBox, nodeBox, nextIndex, depth + 1, stats);
                    return;
                }
            }

            // ensure we are making progress in the subdivision
            if (right == rightOrig) {
                if (prevAxis == axis && MathUtil.fuzzyEq(prevSplit, split)) {
                    // we are stuck here - create a leaf
                    stats.updateLeaf(depth, right - left + 1);
                    createNode(tempTree, nodeIndex, left, right);
                    return;
                }
                // all left
                if (clipL <= split) {
                    // keep looping on left half
                    setAxisValue(gridBox.max, split, axis);
                    prevClip = clipL;
                    wasLeft = true;
                    continue;
                }
                setAxisValue(gridBox.max, split, axis);
                prevClip = Float.NaN;
            } else if (left > right) {
                // all right
                right = rightOrig;
                if (prevAxis == axis && MathUtil.fuzzyEq(prevSplit, split)) {
                    // we are stuck here - create a leaf
                    stats.updateLeaf(depth, right - left + 1);
                    createNode(tempTree, nodeIndex, left, right);
                    return;
                }
                if (clipR >= split) {
                    // keep looping on right half
                    setAxisValue(gridBox.min, split, axis);
                    prevClip = clipR;
                    wasLeft = false;
                    continue;
                }
                setAxisValue(gridBox.min, split, axis);
                prevClip = Float.NaN;
            } else {
                // we are actually splitting stuff
                if (prevAxis != -1 && !Float.isNaN(prevClip)) {
                    // second time through - lets create the previous split
                    // since it produced empty space
                    int nextIndex = tempTree.size;
                    // allocate child node
                    tempTree.add(0);
                    tempTree.add(0);
                    tempTree.add(0);
                    if (wasLeft) {
                        // create a node with a left child
                        // write leaf node
                        stats.updateInner();
                        tempTree.set(nodeIndex, (prevAxis << 30) | nextIndex);
                        tempTree.set(nodeIndex + 1, Float.floatToRawIntBits(prevClip));
                        tempTree.set(nodeIndex + 2, Float.floatToRawIntBits(Float.POSITIVE_INFINITY));
                    } else {
                        // create a node with a right child
                        // write leaf node
                        stats.updateInner();
                        tempTree.set(nodeIndex, (prevAxis << 30) | (nextIndex - 3));
                        tempTree.set(nodeIndex + 1, Float.floatToRawIntBits(Float.NEGATIVE_INFINITY));
                        tempTree.set(nodeIndex + 2, Float.floatToRawIntBits(prevClip));
                    }
                    // count stats for the unused leaf
                    depth++;
                    stats.updateLeaf(depth, 0);
                    // now we keep going as we are, with a new nodeIndex:
                    nodeIndex = nextIndex;
                }
                break;
            }
        }

        // compute index of child nodes
        int nextIndex = tempTree.size;
        // allocate left node
        int nl = right - left + 1;
        int nr = rightOrig - (right + 1) + 1;
        if (nl > 0) {
            tempTree.add(0);
            tempTree.add(0);
            tempTree.add(0);
        } else
            nextIndex -= 3;
        // allocate right node
        if (nr > 0) {
            tempTree.add(0);
            tempTree.add(0);
            tempTree.add(0);
        }
        // write leaf node
        stats.updateInner();
        tempTree.set(nodeIndex, (axis << 30) | nextIndex);
        tempTree.set(nodeIndex + 1, Float.floatToRawIntBits(clipL));
        tempTree.set(nodeIndex + 2, Float.floatToRawIntBits(clipR));
        // prepare L/R child boxes
        BoundingBox gridBoxL = new BoundingBox(gridBox);
        BoundingBox gridBoxR = new BoundingBox(gridBox);
        BoundingBox nodeBoxL = new BoundingBox(nodeBox);
        BoundingBox nodeBoxR = new BoundingBox(nodeBox);
        setAxisValue(gridBoxL.max, split, axis);
        setAxisValue(gridBoxR.min, split, axis);
        setAxisValue(nodeBoxL.max, clipL, axis);
        setAxisValue(nodeBoxL.min, clipR, axis);

        gridBoxL.update();
        gridBoxR.update();
        nodeBoxL.update();
        nodeBoxR.update();

        // recurse
        if (nl > 0)
            subdivide(left, right, tempTree, dat, gridBoxL, nodeBoxL, nextIndex, depth + 1, stats);
        else
            stats.updateLeaf(depth + 1, 0);
        if (nr > 0)
            subdivide(right + 1, rightOrig, tempTree, dat, gridBoxR, nodeBoxR, nextIndex + 3, depth + 1, stats);
        else
            stats.updateLeaf(depth + 1, 0);
    }

    private void createNode(IntArray tempTree, int nodeIndex, int left, int right) {
        // write leaf node
        tempTree.set(nodeIndex, (3 << 30) | left);
        tempTree.set(nodeIndex + 1, right - left + 1);
    }


    @FunctionalInterface
    public interface RayIntersect {
        boolean apply(Ray ray, int entry, Distance distance, boolean pStopAtFirstHit);
    }

    private final static class BuildData {
        public int[] indices;
        public BoundingBox[] primBound;

        public int numPrims;
        public int maxPrims;

    }


    private final static class StackNode {

        public int node;
        public float near;
        public float far;
    }

    public static class BuildStats {
        private final int[] numLeavesN = new int[6];
        public int numNodes;
        public int numLeaves;
        public int sumObjects;
        public int minObjects;
        public int maxObjects;
        public int sumDepth;
        public int minDepth;
        public int maxDepth;
        private int numBVH2;

        public BuildStats() {
            numNodes = 0;
            numLeaves = 0;
            sumObjects = 0;
            minObjects = 0x0FFFFFFF;
            maxObjects = -1;
            sumDepth = 0;
            minDepth = 0x0FFFFFFF;
            maxDepth = -1;
            numBVH2 = 0;

            for (var i = 0; i < 6; ++i) {
                numLeavesN[i] = 0;
            }
        }

        public final void updateInner() {
            numNodes++;
        }

        public final void updateBVH2() {
            numBVH2++;
        }

        public final void updateLeaf(int depth, int n) {
            numLeaves++;
            minDepth = Math.min(depth, minDepth);
            maxDepth = Math.max(depth, maxDepth);
            sumDepth += depth;
            minObjects = Math.min(n, minObjects);
            maxObjects = Math.max(n, maxObjects);
            sumObjects += n;
            var nl = Math.min(n, 5);
            ++numLeavesN[nl];
        }


        void printStats() {
            Logs.MAPS.debug("Tree stats:");
            Logs.MAPS.debug("  * Nodes:          %d".formatted(numNodes));
            Logs.MAPS.debug("  * Leaves:         %d".formatted(numLeaves));
            Logs.MAPS.debug("  * Objects: min    %d".formatted(minObjects));
            Logs.MAPS.debug("             avg    %.2f".formatted((float) sumObjects / numLeaves));
            Logs.MAPS.debug("           avg(n>0) %.2f".formatted((float) sumObjects / (numLeaves - numLeavesN[0])));
            Logs.MAPS.debug("             max    %d".formatted(maxObjects));
            Logs.MAPS.debug("  * Depth:   min    %d".formatted(minDepth));
            Logs.MAPS.debug("             avg    %.2f".formatted((float) sumDepth / numLeaves));
            Logs.MAPS.debug("             max    %d".formatted(maxDepth));
            Logs.MAPS.debug("  * Leaves w/: N=0  %3d%%".formatted(100 * numLeavesN[0] / numLeaves));
            Logs.MAPS.debug("               N=1  %3d%%".formatted(100 * numLeavesN[1] / numLeaves));
            Logs.MAPS.debug("               N=2  %3d%%".formatted(100 * numLeavesN[2] / numLeaves));
            Logs.MAPS.debug("               N=3  %3d%%".formatted(100 * numLeavesN[3] / numLeaves));
            Logs.MAPS.debug("               N=4  %3d%%".formatted(100 * numLeavesN[4] / numLeaves));
            Logs.MAPS.debug("               N>4  %3d%%".formatted(100 * numLeavesN[5] / numLeaves));
            Logs.MAPS.debug("  * BVH2 nodes:     %d (%3d%%)".formatted(numBVH2, 100 * numBVH2 / (numNodes + numLeaves - 2 * numBVH2)));
        }
    }

}
