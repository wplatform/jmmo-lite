package com.github.azeroth.game.map.model;


import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.IntMap;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.model.*;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.game.map.collision.VMapManager;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.AreaInfo;
import lombok.Data;


@Data
public class GameObjectModel {

    public static final IntMap<GameObjectModelData> MODELS = new IntMap<>();

    private boolean collisionEnabled;
    private BoundingBox iBound;
    private Matrix3 iInvRot;
    private Vector3 iPos;
    private float iInvScale;
    private float iScale;
    private WorldModel iModel;
    private GameObjectModelOwnerBase modelOwner;
    private boolean isWmo;

    private VMapManager vMapManager;

    public static GameObjectModel create(GameObjectModelOwnerBase modelOwner) {
        GameObjectModel mdl = new GameObjectModel();

        if (!mdl.initialize(modelOwner)) {
            return null;
        }

        return mdl;
    }

    public boolean intersectRay(Ray ray, Distance distance, boolean stopAtFirstHit, PhaseShift phaseShift, ModelIgnoreFlags ignoreFlags) {
        if (!isCollisionEnabled() || !modelOwner.isSpawned()) {
            return false;
        }

        if (!modelOwner.isInPhase(phaseShift)) {
            return false;
        }

        // child bounds are defined in object space:
        //Vector3 p = iInvRot * (ray.origin() - iPos) * iInvScale;
        Vector3 p = ray.origin.sub(iPos).mul(iInvRot).scl(iInvScale).nor();
        //Ray modRay(p, iInvRot * ray.direction());
        var modRay = new Ray(p, ray.direction.mul(iInvRot));

        var hit = iModel.intersectRay(modRay, distance, stopAtFirstHit, ignoreFlags);

        if (hit) {
            distance.distance *= iScale;
            distance.hit = true;
        }

        return hit;
    }

    public void intersectPoint(Vector3 point, AreaInfo info, PhaseShift phaseShift) {
        if (!isCollisionEnabled() || !modelOwner.isSpawned() || !isMapObject()) {
            return;
        }

        if (!modelOwner.isInPhase(phaseShift)) {
            return;
        }

        if (!iBound.contains(point)) {
            return;
        }


        // child bounds are defined in object space:
        var pModel = point.sub(iPos).mul(iInvRot).scl(iInvScale);

        var zDirModel = new Vector3(0.0f, 0.0f, -1.0f).mul(iInvRot);

        Distance zDist = new Distance();

        if (iModel.intersectPoint(pModel, zDirModel, zDist, info)) {

            var modelGround = pModel.add(zDirModel.scl(zDist.distance));
            var world_Z = modelGround.mul(iInvRot).scl(iScale).add(iPos).z;

            if (info.floorZ < world_Z) {
                info.floorZ = world_Z;
                info.adtId = modelOwner.getNameSetId();
            }
        }
    }

    public final boolean getLocationInfo(Vector3 point, LocationInfo info, PhaseShift phaseShift) {
        if (!isCollisionEnabled() || !modelOwner.isSpawned() || !isMapObject()) {
            return false;
        }

        if (!modelOwner.isInPhase(phaseShift)) {
            return false;
        }

        if (!iBound.contains(point)) {
            return false;
        }

        // child bounds are defined in object space:
        var pModel = point.sub(iPos).scl(iInvScale).mul(iInvRot);
        var zDirModel = new Vector3(0.0f, 0.0f, -1.0f).mul(iInvRot);

        GroupLocationInfo groupInfo = new GroupLocationInfo();

        Distance zDist = new Distance();
        if (iModel.getLocationInfo(pModel, zDirModel, zDist, groupInfo)) {
            zDist.hit = true;
            var modelGround = pModel.add(zDirModel.scl(zDist.distance));
            var world_Z = modelGround.mul(iInvRot).scl(iScale).add(iPos).z;

            info.dynHitModel = this;
            if (info.groundZ < world_Z) {
                info.groundZ = world_Z;

                return true;
            }
        }
        return zDist.hit;
    }

    public final boolean getLiquidLevel(Vector3 point, LocationInfo info, Distance liqHeight) {
        // child bounds are defined in object space:
        var pModel = point.sub(iPos).mul(iInvRot).scl(iInvScale);

        //Vector3 zDirModel = iInvRot * Vector3(0.f, 0.f, -1.f);
        Distance zDist = new Distance();
        if (info.hitModel.getLiquidLevel(pModel, zDist)) {
            liqHeight.hit = true;
            // calculate world height (zDist in model coords):
            // assume WMO not tilted (wouldn't make much sense anyway)
            liqHeight.distance = zDist.distance * iScale + iPos.z;

            return true;
        }
        return liqHeight.hit;
    }

    public final boolean updatePosition() {
        if (iModel == null) {
            return false;
        }


        var it = MODELS.get(modelOwner.getDisplayId());

        if (it == null) {
            return false;
        }


        // ignore models with no bounds
        if (it.bound.min.isZero() && it.bound.max.isZero()) {
            Logs.MISC.error("GameObject model {} has zero bounds, loading skipped", it.name);

            return false;
        }

        iPos = modelOwner.getPosition();

        Matrix3 iRotation = new Matrix3();
        modelOwner.getRotation().toMatrix(iRotation.val);
        iInvRot = iRotation.inv();

        // transform bounding box:
        BoundingBox mdl_box = new BoundingBox(it.bound.min.scl(iScale), it.bound.max.scl(iScale));


        Vector3[] corners = {
                mdl_box.getCorner000(new Vector3()).mul(iRotation),
                mdl_box.getCorner010(new Vector3()).mul(iRotation),
                mdl_box.getCorner011(new Vector3()).mul(iRotation),
                mdl_box.getCorner100(new Vector3()).mul(iRotation),
                mdl_box.getCorner101(new Vector3()).mul(iRotation),
                mdl_box.getCorner110(new Vector3()).mul(iRotation),
                mdl_box.getCorner111(new Vector3()).mul(iRotation)
        };

        BoundingBox rotated_bounds = new BoundingBox(corners[0], corners[0]);

        for (var i = 0; i < 8; ++i) {

            rotated_bounds.min.x = Math.min(rotated_bounds.min.x, corners[i].x);
            rotated_bounds.min.y = Math.min(rotated_bounds.min.y, corners[i].y);
            rotated_bounds.min.z = Math.min(rotated_bounds.min.z, corners[i].z);

            rotated_bounds.max.x = Math.max(rotated_bounds.max.x, corners[i].x);
            rotated_bounds.max.y = Math.max(rotated_bounds.max.y, corners[i].y);
            rotated_bounds.max.z = Math.max(rotated_bounds.max.z, corners[i].z);

        }

        iBound = new BoundingBox(rotated_bounds.min.add(iPos), rotated_bounds.max.add(iPos));

        return true;
    }

    public Vector3 getPosition() {
        return iPos;
    }

    public BoundingBox getBoundingBox() {
        return iBound;
    }

    public final void enableCollision(boolean enable) {
        collisionEnabled = enable;
    }

    public final boolean isMapObject() {
        return isWmo;
    }

    public final byte getNameSetId() {
        return modelOwner.getNameSetId();
    }


    private boolean initialize(GameObjectModelOwnerBase modelOwner) {
        var modelData = MODELS.get(modelOwner.getDisplayId());

        if (modelData == null) {
            return false;
        }
        // ignore models with no bounds
        if (modelData.bound.min.isZero() && modelData.bound.max.isZero()) {
            Logs.MISC.error("GameObject model {} has zero bounds, loading skipped", modelData.name);

            return false;
        }

        if (iModel == null) {
            iModel = vMapManager.acquireModelInstance(modelData.name);
        }


        if (iModel == null) {
            return false;
        }

        iPos = this.modelOwner.getPosition();
        iScale = modelOwner.getScale();
        iInvScale = 1.f / iScale;


        Matrix3 iRotation = new Matrix3();
        this.modelOwner.getRotation().toMatrix(iRotation.val);
        iInvRot = iRotation.inv();

        // transform bounding box:
        BoundingBox mdl_box = new BoundingBox(modelData.bound.min.scl(iScale), modelData.bound.max.scl(iScale));


        Vector3[] corners = {
                mdl_box.getCorner000(new Vector3()).mul(iRotation),
                mdl_box.getCorner010(new Vector3()).mul(iRotation),
                mdl_box.getCorner011(new Vector3()).mul(iRotation),
                mdl_box.getCorner100(new Vector3()).mul(iRotation),
                mdl_box.getCorner101(new Vector3()).mul(iRotation),
                mdl_box.getCorner110(new Vector3()).mul(iRotation),
                mdl_box.getCorner111(new Vector3()).mul(iRotation)
        };

        BoundingBox rotated_bounds = new BoundingBox(corners[0], corners[0]);

        for (var i = 0; i < 8; ++i) {

            rotated_bounds.min.x = Math.min(rotated_bounds.min.x, corners[i].x);
            rotated_bounds.min.y = Math.min(rotated_bounds.min.y, corners[i].y);
            rotated_bounds.min.z = Math.min(rotated_bounds.min.z, corners[i].z);

            rotated_bounds.max.x = Math.max(rotated_bounds.max.x, corners[i].x);
            rotated_bounds.max.y = Math.max(rotated_bounds.max.y, corners[i].y);
            rotated_bounds.max.z = Math.max(rotated_bounds.max.z, corners[i].z);

        }

        iBound = new BoundingBox(rotated_bounds.min.add(iPos), rotated_bounds.max.add(iPos));

        return true;
    }

}
