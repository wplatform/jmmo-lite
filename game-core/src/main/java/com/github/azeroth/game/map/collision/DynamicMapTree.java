package com.github.azeroth.game.map.collision;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.map.model.GameObjectModel;
import com.github.azeroth.game.map.model.LocationInfo;
import com.github.azeroth.game.domain.map.AreaInfo;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.utils.MathUtil;

public class DynamicMapTree {

    private final RegularGrid2D grid = new RegularGrid2D();

    private DbcObjectManager dbcObjectManager;

    public final void insert(GameObjectModel mdl) {
        grid.insert(mdl);
    }

    public final void remove(GameObjectModel mdl) {
        grid.remove(mdl);
    }

    public final boolean contains(GameObjectModel mdl) {
        return grid.contains(mdl);
    }

    public final void balance() {
        grid.balance();
    }


    public final boolean getObjectHitPos(Vector3 startPos, Vector3 endPos, Vector3 resultHitPos, float modifyDist, PhaseShift phaseShift) {

        var maxDist = (endPos.sub(startPos)).len();
        // valid map coords should *never ever* produce float overflow, but this would produce NaNs too

        // prevent NaN values which can cause BIH intersection to enter infinite loop
        if (maxDist < 1e-10f) {
            resultHitPos.set(endPos);

            return false;
        }

        var dir = endPos.sub(startPos).scl(1.0f / maxDist); // direction with length of 1
        Ray ray = new Ray(startPos, dir);


        Distance distance = new Distance(maxDist);
        grid.intersectRay(ray, endPos, phaseShift, distance);
        if (distance.hit) {

            resultHitPos.set(startPos.add(dir.scl(distance.distance)));

            if (modifyDist < 0) {
                if (resultHitPos.sub(startPos).len() > -modifyDist) {

                    resultHitPos.set(resultHitPos.add(dir.scl(modifyDist)));
                } else {
                    resultHitPos.set(startPos);
                }

            } else {
                resultHitPos.set(resultHitPos.add(dir.scl(modifyDist)));
            }

            return true;
        }
        return false;
    }

    public final boolean isInLineOfSight(Vector3 startPos, Vector3 endPos, PhaseShift phaseShift) {
        var maxDist = (endPos.sub(startPos)).len();

        if (!MathUtil.fuzzyGt(maxDist, 0)) {
            return true;
        }

        Ray ray = new Ray(startPos, (endPos.sub(startPos).scl(1f / maxDist)));

        Distance distance = new Distance(maxDist);

        grid.intersectRay(ray, endPos, phaseShift, distance);

        return !distance.hit;
    }

    public final float getHeight(Vector3 v, float maxSearchDist, PhaseShift phaseShift) {

        Ray r = new Ray(v, new Vector3(0, 0, -1));
        Distance distance = new Distance(maxSearchDist);
        grid.intersectZAllignedRay(r, phaseShift, distance);


        if (distance.hit) {
            return v.z - maxSearchDist;
        } else {
            return Float.NEGATIVE_INFINITY;
        }
    }

    public final boolean getAreaInfo(Vector3 outPosZ, PhaseShift phaseShift, AreaInfo outAreaInfo) {
        Vector3 v = new Vector3(outPosZ.x, outPosZ.y, outPosZ.z + 0.5f);
        grid.intersectPoint(v, phaseShift, outAreaInfo);

        if (outAreaInfo.result) {
            outPosZ.z = outAreaInfo.floorZ;
        }
        return outAreaInfo.result;
    }

    public final AreaInfo getAreaAndLiquidData(Position pos, PhaseShift phaseShift, Byte reqLiquidType) {

        Vector3 v = new Vector3(pos.getX(), pos.getY(), pos.getZ() + 0.5f);
        LocationInfo locationInfo = new LocationInfo();
        grid.intersectPoint(v, phaseShift, locationInfo);

        if (locationInfo.hitModel != null) {
            AreaInfo data = new AreaInfo();
            data.floorZ = locationInfo.groundZ;
            var liquidType = locationInfo.hitModel.getLiquidType();


            if (reqLiquidType == null || (dbcObjectManager.getLiquidFlags(liquidType) & reqLiquidType) != 0) {
                Distance liquidLevelRef = new Distance();
                if (locationInfo.hitModel.getLiquidLevel(v, liquidLevelRef)) {
                    data.liquidType = liquidType;
                    data.liquidLevel = liquidLevelRef.distance;
                }

                data.adtId = locationInfo.dynHitModel.getNameSetId();
                data.rootId = locationInfo.rootId;
                data.groupId = locationInfo.hitModel.getIGroupWmoid();
                data.flags = locationInfo.hitModel.getIMogpFlags();

            }
            return data;
        }
        return null;
    }

    public void update(float t_diff) {
    }
}
