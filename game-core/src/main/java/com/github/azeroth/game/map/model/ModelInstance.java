package com.github.azeroth.game.map.model;


import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.AreaInfo;
import com.github.azeroth.game.domain.map.model.*;
import com.github.azeroth.utils.MathUtil;

public class ModelInstance extends ModelMinimalData {
    private final Matrix3 iInvRot;
    private final float iInvScale;
    private WorldModel iModel;


    public ModelInstance(ModelSpawn spawn, WorldModel model) {
        flags = spawn.flags;
        adtId = spawn.adtId;
        id = spawn.id;
        iPos = spawn.iPos;
        iScale = spawn.iScale;
        iBound = spawn.iBound;
        name = spawn.name;

        iModel = model;


        iInvRot = MapDefine.fromEulerAnglesZYX(MathUtil.PI * spawn.iRot.y / 180.0f,
                MathUtil.PI * spawn.iRot.x / 180.0f,
                MathUtil.PI * spawn.iRot.z / 180.0f).inv();


        iInvScale = 1.0f / iScale;
    }

    public final boolean intersectRay(Ray pRay, Distance pMaxDist, boolean pStopAtFirstHit, ModelIgnoreFlags ignoreFlags) {
        if (iModel == null) {
            return false;
        }


        // child bounds are defined in object space:
        var p = pRay.origin.sub(iPos).mul(iInvRot).scl(iInvScale);
        var modRay = new Ray(p, pRay.direction.mul(iInvRot));
        var distance = new Distance(pMaxDist.distance * iInvScale);

        var hit = iModel.intersectRay(modRay, distance, pStopAtFirstHit, ignoreFlags);


        if (hit) {

            pMaxDist.distance = distance.distance * iScale;
        }

        return hit;
    }

    public final void intersectPoint(Vector3 p, AreaInfo info) {
        if (iModel == null) {
            return;
        }

        // M2 files don't contain area info, only WMO files
        if ((flags & ModelFlags.M2.value) != 0) {
            return;
        }

        if (!iBound.contains(p)) {
            return;
        }

        // child bounds are defined in object space:

        var pModel = p.sub(iPos).mul(iInvRot).scl(iInvScale);
        var zDirModel = new Vector3(0.0f, 0.0f, -1.0f).mul(iInvRot);

        Distance zDist = new Distance();

        if (iModel.intersectPoint(pModel, zDirModel, zDist, info)) {

            var modelGround = pModel.add(zDirModel.scl(zDist.distance));
            // Transform back to world space. Note that:
            // Mat * vec == vec * Mat.transpose()
            // and for rotation matrices: Mat.inverse() == Mat.transpose()
            var world_Z = modelGround.mul(iInvRot).scl(iScale).add(iPos).z;

            if (info.floorZ < world_Z) {
                info.floorZ = world_Z;

                info.adtId = adtId;
            }

        }
    }

    public final boolean getLiquidLevel(Vector3 p, LocationInfo info, Distance liqHeight) {
        // child bounds are defined in object space:
        var pModel = p.sub(iPos).mul(iInvRot).scl(iInvScale);

        //Vector3 zDirModel = iInvRot * Vector3(0.f, 0.f, -1.f);
        Distance zDist = new Distance();

        if (info.hitModel.getLiquidLevel(pModel, zDist)) {

            // calculate world height (zDist in model coords):
            // assume WMO not tilted (wouldn't make much sense anyway)
            liqHeight.distance = zDist.distance * iScale + iPos.z;

            return true;
        }
        return false;
    }

    public final boolean getLocationInfo(Vector3 p, LocationInfo info) {
        if (iModel == null) {
            return false;
        }

        // M2 files don't contain area info, only WMO files
        if ((flags & ModelFlags.M2.value) != 0) {
            return false;
        }

        if (!iBound.contains(p)) {
            return false;
        }

        // child bounds are defined in object space:
        var pModel = p.sub(iPos).mul(iInvRot).scl(iInvScale);
        var zDirModel = new Vector3(0.0f, 0.0f, -1.0f).mul(iInvRot);


        GroupLocationInfo groupInfo = new GroupLocationInfo();

        Distance zDist = new Distance();

        if (iModel.getLocationInfo(pModel, zDirModel, zDist, groupInfo)) {
            var modelGround = pModel.add(zDirModel.scl(zDist.distance));
            // Transform back to world space. Note that:
            // Mat * vec == vec * Mat.transpose()
            // and for rotation matrices: Mat.inverse() == Mat.transpose()
            var worldZ = modelGround.mul(iInvRot).scl(iScale).add(iPos).z;


            // hm...could it be handled automatically with zDist at intersection?
            if (info.groundZ < worldZ) {
                info.rootId = groupInfo.rootId;
                info.hitModel = groupInfo.hitModel;
                info.groundZ = worldZ;
                info.hitInstance = this;
                info.result = true;

                return true;
            }
        }

        return false;
    }

    public final void setUnloaded() {
        iModel = null;
    }
}
