package com.github.azeroth.game.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Logs;
import com.github.azeroth.common.YieldResult;
import com.github.azeroth.defines.LineOfSightChecks;
import com.github.azeroth.game.domain.map.LiquidData;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.enums.LiquidHeaderTypeFlag;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.enums.ZLiquidStatus;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.movement.enums.NavTerrainFlag;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.phasing.PhasingHandler;
import lombok.Getter;
import lombok.Setter;
import org.recast4j.detour.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.recast4j.detour.NavMeshQuery.DT_STRAIGHTPATH_END;
import static org.recast4j.detour.NavMeshQuery.DT_STRAIGHTPATH_OFFMESH_CONNECTION;

@Getter
@Setter
public class PathGenerator {

    // 74*4.0f=296y number_of_points*interval = max_path_len
    // this is way more than actual evade range
    // I think we can safely cut those down even more
    private static final byte MAX_PATH_LENGTH         = 74;
    private static final byte MAX_POINT_PATH_LENGTH   = 74;
    private static final float SMOOTH_PATH_STEP_SIZE   = 4.0f;
    private static final float SMOOTH_PATH_SLOP        = 0.3f;

    private static final byte VERTEX_SIZE       = 3;


    private final long[] pathPolyRefs = new long[74];
    private final WorldObject source;
    private final DefaultQueryFilter filter = new DefaultQueryFilter();
    private NavMeshQuery navMeshQuery;
    private NavMesh navMesh;

    private int polyLength;
    private int pointPathLimit;
    private boolean useRaycast; // use raycast if true for a straight line path
    private boolean forceDestination;
    private boolean useStraightPath;
    private Vector3[] pathPoints;

    private Vector3 actualEndPosition;
    private Vector3 startPosition;
    private Vector3 endPosition;
    private final EnumFlag<PathType> pathType = EnumFlag.of(PathType.BLANK);

    public PathGenerator(WorldObject owner) {
        this.pointPathLimit = 74;
        this.source = owner;
        Logs.MAPS.debug("++ PathGenerator:PathGenerator for {}", source.getGUID());

        var mapId = PhasingHandler.getTerrainMapId(source.getPhaseShift(), source.getLocation().getMapId(), source.getMap().getTerrain(), source.getLocation().getX(), source.getLocation().getY());

        var disableManager = owner.getWorldContext().getDisableManager();
        var mMapManager = owner.getWorldContext().getMMapManager();
        if (disableManager.isPathfindingEnabled(source.getMapId())) {
            navMeshQuery = mMapManager.getNavMeshQuery(mapId, source.getMapId(), source.getInstanceId());
            navMesh = mMapManager.getNavMesh(mapId, source.getInstanceId());
        }
        createFilter();
    }


    public final boolean calculatePath(Position destPos) {
        return calculatePath(destPos, false);
    }

    public final boolean calculatePath(Position destPos, boolean forceDest) {
        if (!MapDefine.isValidMapCoordinate(destPos) || !MapDefine.isValidMapCoordinate(source.getLocation())) {
            return false;
        }

        var dest = destPos.toVector3();
        setEndPosition(dest);

        var start = source.getLocation().toVector3();
        setStartPosition(start);

        forceDestination = forceDest;

        Logs.MAPS.debug("++ PathGenerator.calculatePath() for {}", source.getGUID().toString());

        // make sure navMesh works - we can run on map w/o mmap
        // check if the start and end point have a .mmtile loaded (can we pass via not loaded tile on the way?)
        var _sourceUnit = source.toUnit();

        if (navMesh == null || navMeshQuery == null
                || (_sourceUnit != null && _sourceUnit.hasUnitState(UnitState.IGNORE_PATHFINDING))
                || !haveTile(start) || !haveTile(dest)) {
            buildShortcut();
            pathType.set(PathType.NORMAL, PathType.NOT_USING_PATH);

            return true;
        }

        updateFilter();
        buildPolyPath(start, dest);

        return true;
    }

    public final void shortenPathUntilDist(Position pos, float dist) {
        shortenPathUntilDist(new Vector3(pos.getX(), pos.getY(), pos.getZ()), dist);
    }

    public final void shortenPathUntilDist(Vector3 target, float dist) {
        if (getPathType().equals(PathType.BLANK) || pathPoints.length < 2) {
            Logs.MAPS.error("PathGenerator.ReducePathLengthByDist called before path was successfully built");

            return;
        }

        var distSq = dist * dist;
        // the first point of the path must be outside the specified range
        // (this should have really been checked by the caller...)
        if (target.dst2(pathPoints[0]) < distSq) {
            return;
        }

        // check if we even need to do anything
        if (target.dst2(pathPoints[pathPoints.length - 1])  >= distSq) {
            return;
        }

        var i = pathPoints.length - 1;
        var hitPos = new Position();
        var collisionHeight = source.getCollisionHeight();

        // find the first i s.t.:
        //  - _pathPoints[i] is still too close
        //  - _pathPoints[i-1] is too far away
        // => the end point is somewhere on the line between the two
        while (!(target.dst2(pathPoints[i - 1]) >= distSq)) {
            // we know that pathPoints[i] is too close already (from the previous iteration)

            // check if the shortened path is still in LoS with the target
            source.getHitSpherePointFor(new Position(pathPoints[i - 1].x, pathPoints[i - 1].y, pathPoints[i - 1].z + collisionHeight), hitPos);


            if (!source.getMap().isInLineOfSight(source.getPhaseShift(), hitPos, new Position(target.x, target.y, target.z + collisionHeight), LineOfSightChecks.ALL, ModelIgnoreFlags.Nothing)) {
                // whenver we find a point that is not in LoS anymore, simply use last valid path
                pathPoints = Arrays.copyOf(pathPoints, i + 1);
                return;
            }

            if (--i == 0) {
                // no point found that fulfills the condition
                pathPoints[0] = pathPoints[1];
                pathPoints = Arrays.copyOf(pathPoints, 2);


                return;
            }
        }

        // ok, _pathPoints[i] is too close, _pathPoints[i-1] is not, so our target point is somewhere between the two...
        //   ... settle for a guesstimate since i'm not confident in doing trig on every chase motion tick...
        // (@todo review this)

        pathPoints[i].add(pathPoints[i - 1].sub(pathPoints[i]).nor().scl(dist - (pathPoints[i].sub(target)).len()));

        pathPoints[i].add(pathPoints[i - 1].dot(pathPoints[i]));
        pathPoints = Arrays.copyOf(pathPoints, i + 1);
    }

    public final boolean isInvalidDestinationZ(WorldObject target) {
        return (target.getLocation().getZ() - getActualEndPosition().z) > 5.0f;
    }

    public final Vector3 getStartPosition() {
        return startPosition;
    }

    private void setStartPosition(Vector3 point) {
        startPosition = point;
    }

    public final Vector3 getEndPosition() {
        return endPosition;
    }

    private void setEndPosition(Vector3 point) {
        actualEndPosition = point;
        endPosition = point;
    }



    public final Vector3[] getPath() {
        return pathPoints;
    }



    public final void setPathLengthLimit(float distance) {
        pointPathLimit = Math.min((int) (distance / 4.0f), 74);
    }


    private long getPathPolyByPosition(long[] polyPath, int polyPathSize, float[] point, YieldResult<Float> distance) {
        if (polyPath == null || polyPathSize == 0) {
            return 0;
        }

        long nearestPoly = 0;
        var minDist = Float.MAX_VALUE;

        for (int i = 0; i < polyPathSize; ++i) {

            var result = navMeshQuery.closestPointOnPoly(polyPath[i], point);
            if (result.status.isFailed()) {
                continue;
            }
            float d = DetourCommon.vDist2DSqr(point, result.result.getClosest());
            if (d < minDist) {
                minDist = d;
                nearestPoly = polyPath[i];
            }

            if (minDist < 1.0f) // shortcut out - close enough for us
            {
                break;
            }
        }
        distance.set((float) Math.sqrt(minDist));
        return (minDist < 3.0f) ? nearestPoly : 0;
    }

    private long getPolyByLocation(float[] point, YieldResult<Float> distance) {
        // first we check the current path
        // if the current path doesn't contain the current poly,
        // we need to use the expensive navMesh.findNearestPoly
        var polyRef = getPathPolyByPosition(pathPolyRefs, polyLength, point, distance);

        if (polyRef != 0) {
            return polyRef;
        }

        // we don't have it in our old path
        // try to get it by findNearestPoly()
        // first try with low search box
        float[] extents = {3.0f, 5.0f, 3.0f}; // bounds of poly search area

        var nearestPoly = navMeshQuery.findNearestPoly(point, extents, filter);
        if (!nearestPoly.status.isFailed() && nearestPoly.result.getNearestRef() != 0) {
            distance.set(DetourCommon.vDist(nearestPoly.result.getNearestPos(), point));
            return nearestPoly.result.getNearestRef();
        }


        // still nothing ..
        // try with bigger search box
        // Note that the extent should not overlap more than 128 polygons in the navmesh (see dtNavMeshQuery.findNearestPoly)
        extents[1] = 50.0f;
        nearestPoly = navMeshQuery.findNearestPoly(point, extents, filter);
        if (!nearestPoly.status.isFailed() && nearestPoly.result.getNearestRef() != 0) {
            distance.set(DetourCommon.vDist(nearestPoly.result.getNearestPos(), point));
            return nearestPoly.result.getNearestRef();
        }

        distance.set(Float.MAX_VALUE);
        return 0;
    }

    private void buildPolyPath(Vector3 startPos, Vector3 endPos) {
        // *** getting start/end poly logic ***

        float distToStartPoly = 0, distToEndPoly = 0;
        float[] startPoint = {startPos.y, startPos.z, startPos.x};
        float[] endPoint = {endPos.y, endPos.z, endPos.x};


        YieldResult<Float> distanceStart = YieldResult.ofNull();
        YieldResult<Float> distanceEnd = YieldResult.ofNull();
        var startPoly = getPolyByLocation(startPoint, distanceStart);
        var endPoly = getPolyByLocation(endPoint, distanceEnd);
        distToStartPoly = distanceStart.get();
        distToEndPoly = distanceEnd.get();

        pathType.set(PathType.NORMAL);

        // we have a hole in our mesh
        // make shortcut path and mark it as NOPATH ( with flying and swimming exception )
        // its up to caller how he will use this info
        if (startPoly == 0 || endPoly == 0) {
            Logs.MAPS.debug("++ BuildPolyPath . (startPoly == 0 || endPoly == 0)\n");
            buildShortcut();
            var path = source.isTypeId(TypeId.UNIT) && source.toCreature().canFly();
            var waterPath = source.isTypeId(TypeId.UNIT) && source.toCreature().canSwim();

            if (waterPath) {
                // Check both start and end points, if they're both in water, then we can *safely* let the creature move
                for (Vector3 pathPoint : pathPoints) {
                    var status = source.getMap().getLiquidStatus(source.getPhaseShift(), pathPoint.x, pathPoint.y,
                            pathPoint.z, null, null, source.getCollisionHeight());
                    // One of the points is not in the water, cancel movement.
                    if (status.equals(ZLiquidStatus.NO_WATER)) {
                        waterPath = false;
                        break;
                    }
                }
            }

            if (path || waterPath) {
                pathType.addFlag(PathType.NORMAL, PathType.NOT_USING_PATH);
                return;
            }

            // raycast doesn't need endPoly to be valid
            if (!useRaycast) {
                pathType.set(PathType.NOPATH);
                return;
            }
        }

        // we may need a better number here
        var startFarFromPoly = distToStartPoly > 7.0f;
        var endFarFromPoly = distToEndPoly > 7.0f;

        if (startFarFromPoly || endFarFromPoly) {
            Logs.MAPS.debug("++ BuildPolyPath . farFromPoly distToStartPoly={0:F3} distToEndPoly={1:F3}", distToStartPoly, distToEndPoly);

            var buildShotrcut = false;
            var p = (distToStartPoly > 7.0f) ? startPos : endPos;

            if (source.getMap().isUnderWater(source.getPhaseShift(), p.x, p.y, p.z)) {
                Logs.MAPS.debug("++ BuildPolyPath :: underWater case");
                var _sourceUnit = source.toUnit();

                if (_sourceUnit != null) {
                    if (_sourceUnit.canSwim()) {
                        buildShotrcut = true;
                    }
                }
            } else {
                Logs.MAPS.debug("++ BuildPolyPath :: flying case");
                var _sourceUnit = source.toUnit();

                if (_sourceUnit != null) {
                    if (_sourceUnit.canFly()) {
                        buildShotrcut = true;
                    }
                    // Allow to build a shortcut if the unit is falling and it's trying to move downwards towards a target (i.e. charging)
                    else if (_sourceUnit.isFalling() && endPos.z < startPos.z) {
                        buildShotrcut = true;
                    }
                }
            }

            if (buildShotrcut) {
                buildShortcut();
                pathType.addFlag(PathType.NORMAL, PathType.NOT_USING_PATH);
                addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
                return;
            } else {
                // we may want to use closestPointOnPolyBoundary instead
                var result = navMeshQuery.closestPointOnPoly(endPoly, endPoint);
                if(result.status.isSuccess()) {
                    DetourCommon.vCopy(endPoint, result.result.getClosest());
                    setActualEndPosition(new Vector3(endPoint[2], endPoint[0], endPoint[1]));
                    return;
                }
                pathType.addFlag(PathType.INCOMPLETE);
                addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
            }
        }

        // *** poly path generating logic ***

        // start and end are on same polygon
        // handle this case as if they were 2 different polygons, building a line path split in some few points
        if (startPoly == endPoly && !useRaycast) {
            Logs.MAPS.debug("++ BuildPolyPath . (startPoly == endPoly)\n");

            pathPolyRefs[0] = startPoly;
            polyLength = 1;

            if (startFarFromPoly || endFarFromPoly) {
                pathType.addFlag(PathType.INCOMPLETE);
                addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
            } else {
                pathType.addFlag(PathType.NORMAL);
            }

            buildPointPath(startPoint, endPoint);
            return;
        }

        // look for startPoly/endPoly in current path
        // @todo we can merge it with getPathPolyByPosition() loop
        var startPolyFound = false;
        var endPolyFound = false;
        int pathStartIndex = 0;
        int pathEndIndex = 0;

        if (polyLength != 0) {
            for (; pathStartIndex < polyLength; ++pathStartIndex) {
                // here to carch few bugs
                if (pathPolyRefs[pathStartIndex] == 0) {
                    Logs.MAPS.error("Invalid poly ref in BuildPolyPath. _polyLength: {}, pathStartIndex: {}, startPos: {}, endPos: {}, mapid: {}",
                             polyLength, pathStartIndex, startPos, endPos, source.getLocation().getMapId());
                    break;
                }
                if (pathPolyRefs[pathStartIndex] == startPoly) {
                    startPolyFound = true;

                    break;
                }
            }

            for (pathEndIndex = polyLength - 1; pathEndIndex > pathStartIndex; --pathEndIndex) {
                if (pathPolyRefs[pathEndIndex] == endPoly) {
                    endPolyFound = true;

                    break;
                }
            }
        }

        if (startPolyFound && endPolyFound) {
            Logs.MAPS.debug("BuildPolyPath : (startPolyFound && endPolyFound)\n");

            // we moved along the path and the target did not move out of our old poly-path
            // our path is a simple subpath case, we have all the data we need
            // just "cut" it out

            polyLength = pathEndIndex - pathStartIndex + 1;
            System.arraycopy(pathPolyRefs, pathStartIndex, pathPolyRefs, 0, polyLength);
        } else if (startPolyFound && !endPolyFound) {
            Logs.MAPS.debug("BuildPolyPath : (startPolyFound && !endPolyFound)\n");

            // we are moving on the old path but target moved out
            // so we have atleast part of poly-path ready

            polyLength -= pathStartIndex;

            // try to adjust the suffix of the path instead of recalculating entire length
            // at given interval the target cannot get too far from its last location
            // thus we have less poly to cover
            // sub-path of optimal path is optimal

            // take ~80% of the original length
            // @todo play with the values here
            var prefixPolyLength = (int) (polyLength * 0.8f + 0.5f);
            System.arraycopy(pathPolyRefs, pathStartIndex, pathPolyRefs, 0, prefixPolyLength);

            var suffixStartPoly = pathPolyRefs[prefixPolyLength - 1];

            // we need any point on our suffix start poly to generate poly-path, so we need last poly in prefix data


            var result = navMeshQuery.closestPointOnPoly(suffixStartPoly, endPoint);
            if(!result.status.isFailed()) {

                // we can hit offmesh connection as last poly - closestPointOnPoly() don't like that
                // try to recover by using prev polyref
                --prefixPolyLength;
                suffixStartPoly = pathPolyRefs[prefixPolyLength-1];
                result = navMeshQuery.closestPointOnPoly(suffixStartPoly, endPoint);

                if(!result.status.isFailed()) {
                    buildShortcut();
                    pathType.addFlag(PathType.NOPATH);
                    return;
                }
            }


            // generate suffix
            int suffixPolyLength = 0;

            int dtResult;

            if (useRaycast) {
                Logs.MAPS.error("PathGenerator::BuildPolyPath() called with _useRaycast with a previous path for unit {}", source.getGUID());
                buildShortcut();
                pathType.addFlag(PathType.NOPATH);
                return;
            } else {
                var navMeshQueryPath = navMeshQuery.findPath(suffixStartPoly, endPoly, result.result.getClosest(), endPoint, filter);
                if(navMeshQueryPath.status.isFailed()) {
                    // this is probably an error state, but we'll leave it
                    // and hopefully recover on the next Update
                    // we still need to copy our preffix
                    Logs.MAPS.error("Path Build failed {}", source);
                } else {
                    var pathPolyRefsResult = navMeshQueryPath.result;
                    for (var i = 0; i < pathPolyRefs.length - (prefixPolyLength - 1); ++i) {
                        pathPolyRefs[(prefixPolyLength - 1) + i] = pathPolyRefsResult.get(i);
                    }
                    suffixPolyLength = pathPolyRefsResult.size();
                }
            }
            Logs.MAPS.debug("++  m_polyLength={} prefixPolyLength={} suffixPolyLength={}", polyLength, prefixPolyLength, suffixPolyLength);
            // new path = prefix + suffix - overlap
            polyLength = prefixPolyLength + suffixPolyLength - 1;
        } else {
            Logs.MAPS.debug("++ BuildPolyPath :: (!startPolyFound && !endPolyFound)");

            // either we have no path at all . first run
            // or something went really wrong . we aren't moving along the path to the target
            // just generate new path

            // free and invalidate old path data
            clear();

            if (useRaycast) {
                float hit = 0;

                var dtResult = navMeshQuery.raycast(startPoly, startPoint, endPoint, filter, 0, 0L);

                if(dtResult.status.isFailed() || dtResult.result.path.isEmpty()) {
                    buildShortcut();
                    pathType.addFlag(PathType.NOPATH);
                    addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
                    return;
                }

                // raycast() sets hit to FLT_MAX if there is a ray between start and end
                if (dtResult.result.t != Float.MAX_VALUE) {
                    // Walk back a bit from the hit point to make sure it's in the mesh (sometimes the point is actually outside of the polygons due to float precision issues)
                    hit *= 0.99f;
                    var hitPos = DetourCommon.vLerp( startPoint, endPoint, hit);

                    var polyHeight = navMeshQuery.getPolyHeight(pathPolyRefs[polyLength - 1], hitPos);

                    // if it fails again, clamp to poly boundary
                    if(polyHeight.status.isFailed()) {
                        var result = navMeshQuery.closestPointOnPolyBoundary(pathPolyRefs[polyLength - 1], hitPos);
                        if(result.status.isSuccess()) {
                            hitPos = result.result;
                        }
                    } else {
                        hitPos[1] = polyHeight.result;
                    }


                    pathPoints = new Vector3[2];
                    pathPoints[0] = getStartPosition();
                    pathPoints[1] = new Vector3(hitPos[2], hitPos[0], hitPos[1]);

                    normalizePath();
                    pathType.set(PathType.INCOMPLETE);
                    addFarFromPolyFlags(startFarFromPoly, false);
                    return;
                } else {
                    // clamp to poly boundary if we fail to get the height
                    var polyHeight = navMeshQuery.getPolyHeight(pathPolyRefs[polyLength - 1], endPoint);
                    if(polyHeight.status.isFailed()) {
                        var result = navMeshQuery.closestPointOnPolyBoundary(pathPolyRefs[polyLength - 1], endPoint);
                        if(result.status.isSuccess()) {
                            endPoint = result.result;
                        }
                    } else {
                        endPoint[1] = polyHeight.result;
                    }
                    pathPoints = new Vector3[2];
                    pathPoints[0] = getStartPosition();
                    pathPoints[1] = new Vector3(endPoint[2], endPoint[0], endPoint[1]);

                    normalizePath();

                    if (startFarFromPoly || endFarFromPoly) {
                        pathType.set(PathType.INCOMPLETE);

                        addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
                    } else {
                        pathType.set(PathType.NORMAL);
                    }

                    return;
                }
            } else {
                // max number of polygons in output path
                var dtResult = navMeshQuery.findPath(startPoly, endPoly, startPoint, endPoint, filter);
                if(dtResult.status.isFailed() || dtResult.result.isEmpty()) {
                    // only happens if we passed bad data to findPath(), or navmesh is messed up
                    Logs.MAPS.error("{} Path Build failed: 0 length path", source.getGUID());
                    buildShortcut();
                    pathType.addFlag(PathType.NOPATH);
                    return;
                }
                polyLength = dtResult.result.size();
                for (int i = 0; i < dtResult.result.size(); i++) {
                    pathPolyRefs[i] = dtResult.result.get(i);
                }
            }
        }

        // by now we know what type of path we can get
        if (pathPolyRefs[polyLength - 1] == endPoly && !pathType.hasFlag(PathType.INCOMPLETE)) {
            pathType.set(PathType.NORMAL);
        } else {
            pathType.set(PathType.INCOMPLETE);
        }

        addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);

        // generate the point-path out of our up-to-date poly-path
        buildPointPath(startPoint, endPoint);
    }

    private void buildPointPath(float[] startPoint, float[] endPoint) {
        var pathPoints = new float[74 * 3];
        var pointCount = 0;


        List<Long> pathPolyRefs = Arrays.stream(this.pathPolyRefs).boxed().limit(this.polyLength).toList();
        if (useRaycast) {
            // _straightLine uses raycast and it currently doesn't support building a point path, only a 2-point path with start and hitpoint/end is returned
            Logs.MAPS.error("PathGenerator::BuildPointPath() called with _useRaycast for unit {}", source.getGUID());
            buildShortcut();
            pathType.addFlag(PathType.NOPATH);
            return;
        }

        Result<List<StraightPathItem>> dtResult = null;
        if (useStraightPath) {
            dtResult = navMeshQuery.findStraightPath(startPoint, endPoint, pathPolyRefs, pointPathLimit, 0); // maximum number of points/polygons to use
        } else {
            dtResult = findSmoothPath(startPoint, endPoint, pathPolyRefs, pointPathLimit);  // maximum number of points
        }

        pointCount = dtResult.status.isSuccess() ? dtResult.result.size() : 0;

        // Special case with start and end positions very close to each other
        if (polyLength == 1 && pointCount == 1) {
            // First point is start position, append end position
            DetourCommon.vCopy(pathPoints, endPoint, 3);
            pointCount++;
        } if(dtResult.status.isFailed() || dtResult.result.size() < 2) {
            // only happens if pass bad data to findStraightPath or navmesh is broken
            // single point paths can be generated here
            /// @todo check the exact cases
            Logs.MAPS.debug("++ PathGenerator::BuildPointPath FAILED! path sized {} returned",dtResult.status.isFailed() ? 0 : dtResult.result.size());
            buildShortcut();
            pathType.addFlag(PathType.NOPATH);
            return;
        } else if (pointCount >= pointPathLimit) {
            Logs.MAPS.debug("++ PathGenerator::BuildPointPath FAILED! path sized {} returned, lower than limit set to {}", pointCount, pointPathLimit);
            buildShortcut();
            pathType.addFlag(PathType.SHORT);
            return;
        }

        this.pathPoints = new Vector3[pointCount];

        for (int i = 0; i < pointCount; ++i) {
            this.pathPoints[i] = new Vector3(pathPoints[i * 3 + 2], pathPoints[i * 3], pathPoints[i * 3 + 1]);
        }

        normalizePath();

        // first point is always our current location - we need the next one
        setActualEndPosition(this.pathPoints[pointCount - 1]);

        // force the given destination, if needed
        if (forceDestination && (!pathType.hasFlag(PathType.NORMAL) || !inRange(getEndPosition(), getActualEndPosition(), 1.0f, 1.0f))) {
            // we may want to keep partial subpath
            if (dist3DSqr(getActualEndPosition(), getEndPosition()) < 0.3f * dist3DSqr(getStartPosition(), getEndPosition())) {
                setActualEndPosition(getEndPosition());

                this.pathPoints[this.pathPoints.length - 1] = getEndPosition();

            } else {
                setActualEndPosition(getEndPosition());
                buildShortcut();
            }
            pathType.set(PathType.NORMAL, PathType.NOT_USING_PATH );
        }

        Logs.MAPS.debug("++ PathGenerator::BuildPointPath path type {} size {} poly-size {}", pathType, pointCount, polyLength);
    }

    private int fixupCorridor(List<Long> path, int maxPath, List<Long> visited, int nvisited) {
        var furthestPath = -1;
        var furthestVisited = -1;

        // Find furthest common polygon.
        for (var i = path.size() - 1; i >= 0; --i) {
            var found = false;

            for (var j = nvisited - 1; j >= 0; --j) {
                if (Objects.equals(path.get(i), visited.get(j))) {
                    furthestPath = i;
                    furthestVisited = j;
                    found = true;
                }
            }

            if (found) {
                break;
            }
        }

        // If no intersection found just return current path.
        if (furthestPath == -1 || furthestVisited == -1) {
            return path.size();
        }

        // Concatenate paths.

        // Adjust beginning of the buffer to include the visited.
        var req = nvisited - furthestVisited;
        var orig = Math.min((furthestPath + 1), path.size());
        var size = path.size() > orig ? path.size() - orig : 0;

        if (req + size > maxPath) {
            size = maxPath - req;
        }

        if (size != 0) {
            for (int i = 0; i < size; ++i) {
                path.set(req + i, path.get(req + i));
            }
        }

        // Store visited
        for (int i = 0; i < req; ++i) {
            path.set(i, visited.get((nvisited - 1) - i));
        }

        return req + size;
    }

    private Result<StraightPathItem> getSteerTarget(float[] startPos, float[] endPos, float minTargetDist, List<Long> path) {

        // Find steer target.
        var dtResult = navMeshQuery.findStraightPath(startPos, endPos, path, 3,0);

        if (dtResult.status.isFailed() || dtResult.result.isEmpty()) {
            return Result.failure();
        }

        // Find vertex far enough to steer to.
        int ns = 0;

        while (ns < dtResult.result.size()) {
            StraightPathItem straightPathItem = dtResult.result.get(ns);
            // Stop at Off-Mesh link or when point is further than slop away.
            if ((straightPathItem.getFlags() & DT_STRAIGHTPATH_OFFMESH_CONNECTION) != 0 ||
                    !inRangeYZX(straightPathItem.getPos(), startPos, minTargetDist, 1000.0f))
                break;
            ns++;
        }

        // Failed to find good point to steer to.
        if (ns >= dtResult.result.size()) {
            return Result.failure();
        }

        StraightPathItem result = dtResult.result.get(ns);
        result.getPos()[1] = startPos[1]; // keep Z second
        return Result.success(result);
    }

    private Result<List<StraightPathItem>> findSmoothPath(float[] startPos, float[] endPos, List<Long> polyPath, int maxSmoothPathSize) {
        var nsmoothPath = 0;

        var iterPos = new float[3];
        var targetPos = new float[3];

        List<Long> polys = new ArrayList<>(polyPath);

        if (polys.size() > 1) {
            // Pick the closest points on poly border
            var dtStatus = navMeshQuery.closestPointOnPolyBoundary(polys.getFirst(), startPos);
            if (dtStatus.status.isFailed()) {
                return Result.failure(dtStatus.message);
            }
            iterPos = dtStatus.result;
            dtStatus = navMeshQuery.closestPointOnPolyBoundary(polys.getLast(), endPos);
            if (dtStatus.status.isFailed()) {
                return Result.failure(dtStatus.message);
            }
            targetPos = dtStatus.result;
        } else {
            // Case where the path is on the same poly
            DetourCommon.vCopy(iterPos, startPos);
            DetourCommon.vCopy(targetPos, endPos);
        }

        List<StraightPathItem> smoothPathItems = new ArrayList<>();
        smoothPathItems.add(new StraightPathItem(iterPos, 0, polys.getFirst()));
        nsmoothPath++;

        // Move towards target a small advancement at a time until target reached or
        // when ran out of memory to store the path.
        while (!polys.isEmpty() && nsmoothPath < maxSmoothPathSize) {
            // Find location to steer towards.
            var steerTarget = getSteerTarget(iterPos, targetPos, 0.3f, polys);
            if (steerTarget.status.isFailed()) {
                break;
            }
            long steerPosRef = steerTarget.result.getRef();
            float[] steerPos = steerTarget.result.getPos();
            int steerPosFlag = steerTarget.result.getFlags();

            boolean endOfPath = (steerPosFlag & DT_STRAIGHTPATH_END) != 0;
            boolean offMeshConnection = (steerPosFlag & DT_STRAIGHTPATH_OFFMESH_CONNECTION) != 0;

            // Find movement delta.
            var delta = DetourCommon.vSub(steerPos, iterPos);
            var len = DetourCommon.vLen(delta);

            // If the steer target is end of path or off-mesh link, do not move past the location.
            if ((endOfPath || offMeshConnection) && len < 4.0f) {
                len = 1.0f;
            } else {
                len = SMOOTH_PATH_STEP_SIZE / len;
            }

            var moveTgt = DetourCommon.vMad(iterPos, delta, len);

            var moveAlongSurface = navMeshQuery.moveAlongSurface(polys.getFirst(), iterPos, moveTgt, filter);
            if (moveAlongSurface.status.isFailed()) {
                return Result.failure(moveAlongSurface.message);
            }

            var visited = moveAlongSurface.result.getVisited();
            var result = moveAlongSurface.result.getResultPos();
            var nvisited = moveAlongSurface.result.getVisited().size();

            var npolys = fixupCorridor(polys, 74, visited, nvisited);

            var polyHeight = navMeshQuery.getPolyHeight(polys.getFirst(), result);



            if (polyHeight.status.isFailed()) {
                Logs.MAPS.debug("Cannot find height at position X: {} Y: {} Z: {} for {}", result[2], result[0], result[1], source);
            } else {
                result[1] = polyHeight.result;
            }

            result[1] += 0.5f;
            DetourCommon.vCopy(iterPos, result);

            // Handle end of path and off-mesh links when close enough.
            if (endOfPath && inRangeYZX(iterPos, steerPos, SMOOTH_PATH_SLOP, 1.0f)) {
                // Reached end of path.
                DetourCommon.vCopy(iterPos, targetPos);

                if (nsmoothPath < maxSmoothPathSize) {
                    DetourCommon.vCopy(smoothPathItems.get(nsmoothPath).getPos(), iterPos);
                    nsmoothPath++;
                }

                break;
            } else if (offMeshConnection && inRangeYZX(iterPos, steerPos, SMOOTH_PATH_SLOP, 1.0f)) {
                // Advance the path up to and over the off-mesh connection.
                long prevRef = 0;
                var polyRef = polys.getFirst();
                int npos = 0;

                while (npos < npolys && polyRef != steerPosRef) {
                    prevRef = polyRef;
                    polyRef = polys.get(npos);
                    npos++;
                }

                for (var i = npos; i < npolys; ++i) {
                    polys.set(i - npos, polys.get(i));
                }

                npolys -= npos;

                // Handle the connection.

                var offMeshConnectionPolyEndPoints = navMesh.getOffMeshConnectionPolyEndPoints(prevRef, polyRef);

                if (offMeshConnectionPolyEndPoints.status.isSuccess()) {
                    if (nsmoothPath < maxSmoothPathSize) {

                        smoothPathItems.add(new StraightPathItem(offMeshConnectionPolyEndPoints.result.first, 0, 0L));

                        nsmoothPath++;
                    }
                    DetourCommon.vCopy(iterPos, offMeshConnectionPolyEndPoints.result.second);
                    var endPolyHeight = navMeshQuery.getPolyHeight(polys.getFirst(), iterPos);
                    if(endPolyHeight.status.isFailed()) {
                        return Result.failure(endPolyHeight.message);
                    }
                    iterPos[1] = endPolyHeight.result;
                    iterPos[1] += 0.5f;
                }
            }

            // Store results.
            if (nsmoothPath < maxSmoothPathSize) {
                smoothPathItems.add(new StraightPathItem(iterPos, 0, 0L));
                nsmoothPath++;
            }
        }

        // this is most likely a loop
        return Result.success(smoothPathItems);
    }

    private void normalizePath() {
        for (Vector3 point : pathPoints) {
            point.z = source.updateAllowedPositionZ(point.x, point.y, point.z);
        }
    }

    private void buildShortcut() {
        Logs.MAPS.debug("BuildShortcut : making shortcut");

        clear();

        // make two point path, our curr pos is the start, and dest is the end
        pathPoints = new Vector3[2];

        // set start and a default next position
        pathPoints[0] = getStartPosition();
        pathPoints[1] = getActualEndPosition();

        normalizePath();

        pathType.set(PathType.SHORTCUT);
    }

    private void createFilter() {
        int includeFlags = 0;
        int excludeFlags = 0;

        if (source.isTypeId(TypeId.UNIT)) {
            var creature = source.toCreature();

            if (!creature.isAquatic()) {
                includeFlags |= NavTerrainFlag.GROUND;          // walk
            }

            // creatures don't take environmental damage
            if (creature.canEnterWater()) {
                includeFlags |= (NavTerrainFlag.WATER | NavTerrainFlag.MAGMA_SLIME);                 // swim
            }
        } else {
            // assume Player
            // perfect support not possible, just stay 'safe'
            includeFlags |= (NavTerrainFlag.GROUND | NavTerrainFlag.WATER | NavTerrainFlag.MAGMA_SLIME);
        }
        this.filter.setIncludeFlags(includeFlags);
        this.filter.setExcludeFlags(excludeFlags);
        updateFilter();
    }

    private void updateFilter() {

        filter.setIncludeFlags(filter.getIncludeFlags() | source.getMap().getForceEnabledNavMeshFilterFlags());
        filter.setExcludeFlags(filter.getExcludeFlags() | source.getMap().getForceDisabledNavMeshFilterFlags());

        var _sourceUnit = source.toUnit();
        // allow creatures to cheat and use different movement types if they are moved
        // forcefully into terrain they can't normally move in
        if (_sourceUnit != null) {
            if (_sourceUnit.isInWater() || _sourceUnit.isUnderWater()) {

                int includedFlags = filter.getIncludeFlags();
                includedFlags |= getNavTerrain(startPosition.x,
                        startPosition.y,
                        startPosition.z);

                filter.setIncludeFlags((short) includedFlags);
            }

            var _sourceCreature = source.toCreature();

            if (_sourceCreature != null) {
                if (_sourceCreature.isInCombat() || _sourceCreature.isInEvadeMode()) {
                    filter.setIncludeFlags(filter.getIncludeFlags() | NavTerrainFlag.GROUND_STEEP);
                }
            }
        }
    }

    private int getNavTerrain(float x, float y, float z) {
        LiquidData data = new LiquidData();
        EnumFlag<ZLiquidStatus> liquidStatus = source.getMap().getLiquidStatus(source.getPhaseShift(), x, y, z, null, data, source.getCollisionHeight());

        if (liquidStatus.equals(ZLiquidStatus.NO_WATER))
            return NavTerrainFlag.GROUND;

        if (data.getTypeFlags().hasAnyFlag(LiquidHeaderTypeFlag.Water, LiquidHeaderTypeFlag.Ocean))
                return NavTerrainFlag.WATER;

        if (data.getTypeFlags().hasAnyFlag(LiquidHeaderTypeFlag.Magma, LiquidHeaderTypeFlag.Slime))
            return NavTerrainFlag.MAGMA_SLIME;

        return NavTerrainFlag.GROUND;
    }

    private boolean inRange(Vector3 p1, Vector3 p2, float r, float h) {
        var d = p1.sub(p2);
        return (d.x * d.x + d.y * d.y) < r * r && Math.abs(d.z) < h;
    }

    private float dist3DSqr(Vector3 p1, Vector3 p2) {
        return p1.dst2(p2);
    }

    private void addFarFromPolyFlags(boolean startFarFromPoly, boolean endFarFromPoly) {
        if (startFarFromPoly) {
            pathType.addFlag(PathType.FARFROMPOLY_START);
        }

        if (endFarFromPoly) {
            pathType.addFlag(PathType.FARFROMPOLY_END);
        }
    }

    private void clear() {
        polyLength = 0;
        pathPoints = null;
    }

    private boolean haveTile(Vector3 p) {

        float[] point = {p.y, p.z, p.x};
        int[] ints = navMesh.calcTileLoc(point);
        int tx = ints[0];
        int ty = ints[1];
        // Workaround
        // For some reason, often the tx and ty variables wont get a valid second
        // Use this check to prevent getting negative tile coords and crashing on getTileAt
        if (tx < 0 || ty < 0) {
            return false;
        }
        return (navMesh.getTileRefAt(tx, ty, 0) != 0);
    }

    private boolean inRangeYZX(float[] v1, float[] v2, float r, float h) {
        var dx = v2[0] - v1[0];
        var dy = v2[1] - v1[1]; // elevation
        var dz = v2[2] - v1[2];

        return (dx * dx + dz * dz) < r * r && Math.abs(dy) < h;
    }
}
