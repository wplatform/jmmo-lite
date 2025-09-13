package com.github.azeroth.game.map.model;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.model.MeshTriangle;
import com.github.azeroth.game.domain.map.model.WmoLiquid;
import com.github.azeroth.game.map.collision.BIH;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Data
public class GroupModel {


    private final ArrayList<Vector3> vertices = new ArrayList<>();
    private final ArrayList<MeshTriangle> triangles = new ArrayList<>();
    private final BIH meshTree = new BIH();
    private BoundingBox boundingBox;
    private int iMogpFlags;
    private int iGroupWmoid;
    private WmoLiquid iLiquid;

    public GroupModel() {
        iLiquid = null;
    }


    public GroupModel(int mogpFlags, int groupWMOID, BoundingBox bound) {
        boundingBox = bound;
        iMogpFlags = mogpFlags;
        iGroupWmoid = groupWMOID;
        iLiquid = null;
    }

    public final boolean readFromFile(ByteBuffer buffer) {
        triangles.clear();
        vertices.clear();
        iLiquid = null;


        var lo = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        var hi = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
        boundingBox = new BoundingBox(lo, hi);
        iMogpFlags = buffer.getInt();
        iGroupWmoid = buffer.getInt();


        byte[] bytes = new byte[4];
        buffer.get(bytes);
        String chunkName = new String(bytes, StandardCharsets.UTF_8);

        // read vertices
        if (!"VERT".equals(chunkName)) {
            return false;
        }

        var chunkSize = buffer.getInt();
        var count = buffer.getInt();

        if (count == 0) {
            return false;
        }

        for (var i = 0; i < count; ++i) {
            vertices.add(new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));
        }

        // read triangle mesh
        bytes = new byte[4];
        buffer.get(bytes);
        chunkName = new String(bytes, StandardCharsets.UTF_8);
        if (!"TRIM".equals(chunkName)) {
            return false;
        }

        chunkSize = buffer.getInt();
        count = buffer.getInt();

        for (var i = 0; i < count; ++i) {
            triangles.add(new MeshTriangle(buffer.getInt(), buffer.getInt(), buffer.getInt()));
        }

        // read mesh BIH
        bytes = new byte[4];
        buffer.get(bytes);
        chunkName = new String(bytes, StandardCharsets.UTF_8);
        if (!"MBIH".equals(chunkName)) {
            return false;
        }

        meshTree.readFromFile(buffer);

        // write liquid data
        bytes = new byte[4];
        buffer.get(bytes);
        chunkName = new String(bytes, StandardCharsets.UTF_8);
        if (!"LIQU".equals(chunkName)) {
            return false;
        }

        chunkSize = buffer.getInt();

        if (chunkSize > 0) {
            iLiquid = WmoLiquid.readFromFile(buffer);
        }

        return true;
    }

    public boolean intersectRay(Ray ray, Distance distance, boolean stopAtFirstHit) {
        if (triangles.isEmpty()) {
            return false;
        }

        meshTree.intersectRay(ray, distance, stopAtFirstHit, (r, entry, maxDist, stopAtFirst) -> {
            distance.hit = intersectTriangle(triangles.get(entry), vertices, ray, distance) || distance.hit;
            return distance.hit;
        });

        return distance.hit;
    }

    public final boolean isInsideObject(Vector3 pos, Vector3 down, Distance z_dist) {


        if (triangles.isEmpty() || !boundingBox.contains(pos)) {
            return false;
        }

        var rPos = pos.sub(down.scl(0.1f));
        var dist = new Distance(Float.POSITIVE_INFINITY);
        Ray ray = new Ray(rPos, down);

        var hit = intersectRay(ray, dist, false);
        if (hit) {
            z_dist.distance = dist.distance - 0.1f;
            z_dist.hit = true;
        }

        return hit;
    }

    public final boolean getLiquidLevel(Vector3 pos, Distance liqHeight) {


        if (iLiquid != null) {
            return iLiquid.getLiquidHeight(pos, liqHeight);
        }

        return false;
    }

    public final int getLiquidType() {
        if (iLiquid != null) {
            return iLiquid.getLiquidType();
        }

        return 0;
    }


    private boolean intersectTriangle(MeshTriangle tri, ArrayList<Vector3> points, Ray ray, Distance distance) {
        final float eps = 1e-5f;

        // See RTR2 ch. 13.7 for the algorithm.

        var e1 = points.get(tri.idx1).sub(points.get(tri.idx0));
        var e2 = points.get(tri.idx2).sub(points.get(tri.idx0));


        var p = ray.direction.crs(e2);
        var a = e1.dot(p);

        if (Math.abs(a) < eps) {
            // Determinant is ill-conditioned; abort early
            return false;
        }

        var f = 1.0f / a;
        var s = ray.origin.sub(points.get(tri.idx0));
        var u = f * s.dot(p);

        if ((u < 0.0f) || (u > 1.0f)) {
            // We hit the plane of the m_geometry, but outside the m_geometry
            return false;
        }

        var q = s.crs(e1);
        var v = f * ray.direction.dot(q);

        if ((v < 0.0f) || ((u + v) > 1.0f)) {
            // We hit the plane of the triangle, but outside the triangle
            return false;
        }

        var t = f * e2.dot(q);

        if ((t > 0.0f) && (t < distance.distance)) {
            // This is a new hit, closer than the previous one
            distance.distance = t;

            return true;
        }

        // This hit is after the previous hit, so ignore it
        return false;
    }
}
