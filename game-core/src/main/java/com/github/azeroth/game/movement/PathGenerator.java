package com.github.azeroth.game.movement;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.movement.enums.NavTerrainFlag;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.phasing.PhasingHandler;

public class PathGenerator {
    private final long[] pathPolyRefs = new long[74];
    private final WorldObject source;
    private final Detour.dtQueryFilter filter = new Detour.dtQueryFilter();
    private final Detour.dtNavMeshQuery navMeshQuery;
    private final Detour.dtNavMesh navMesh;

    private int polyLength;
    private int pointPathLimit;
    private boolean useRaycast; // use raycast if true for a straight line path
    private boolean forceDestination;
    private boolean useStraightPath;
    private Vector3[] pathPoints;

    private Vector3 actualEndPosition;
    private Vector3 startPosition;
    private Vector3 endPosition;
    private EnumFlag<PathType> pathType;

    public PathGenerator(WorldObject owner) {
        polyLength = 0;
        pathType = EnumFlag.of(PathType.BLANK);
        useStraightPath = false;
        forceDestination = false;
        pointPathLimit = 74;
        endPosition = Vector3.Zero;
        source = owner;
        navMesh = null;
        navMeshQuery = null;
        Logs.MAPS.debug("PathGenerator:PathGenerator for {}", source.getGUID().toString());

        var mapId = PhasingHandler.getTerrainMapId(source.getPhaseShift(), source.getLocation().getMapId(), source.getMap().getTerrain(), source.getLocation().getX(), source.getLocation().getY());

        var disableManager = owner.getWorldContext().getDisableManager();
        var mMapManager = owner.getWorldContext().getMMapManager();
        if (disableManager.isPathfindingEnabled(source.getLocation().getMapId())) {
            navMesh = mMapManager.getNavMesh(mapId);
            navMeshQuery = mMapManager.getNavMeshQuery(mapId, source.getInstanceId());
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

        Logs.MAPS.debug("PathGenerator.calculatePath() for {0} \n", source.getGUID().toString());

        // make sure navMesh works - we can run on map w/o mmap
        // check if the start and end point have a .mmtile loaded (can we pass via not loaded tile on the way?)
        var _sourceUnit = source.toUnit();

        if (navMesh == null || navMeshQuery == null || (_sourceUnit != null && _sourceUnit.hasUnitState(UnitState.IgnorePathfinding)) || !haveTile(start) || !haveTile(dest)) {
            buildShortcut();
            pathType = PathType.forValue(PathType.NORMAL.getValue() | PathType.NOTUSINGPATH.getValue());

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
        if (getPathType() == PathType.BLANK || pathPoints.length < 2) {
            Logs.MAPS.error("PathGenerator.ReducePathLengthByDist called before path was successfully built");

            return;
        }

        var distSq = dist * dist;

        // the first point of the path must be outside the specified range
        // (this should have really been checked by the caller...)
        if ((_pathPoints[0] - target).LengthSquared() < distSq) {
            return;
        }

        // check if we even need to do anything
        if ((_pathPoints[_pathPoints.length - 1] - target).LengthSquared() >= distSq) {
            return;
        }

        var i = pathPoints.length - 1;
        var collisionHeight = source.getCollisionHeight();

        // find the first i s.t.:
        //  - _pathPoints[i] is still too close
        //  - _pathPoints[i-1] is too far away
        // => the end point is somewhere on the line between the two
        while (true) {
            // we know that pathPoints[i] is too close already (from the previous iteration)
            var point = _pathPoints[i - 1];

            if ((point - target).LengthSquared() >= distSq) {
                break; // bingo!
            }

            // check if the shortened path is still in LoS with the target
            var hitPos = new Position();
            source.getHitSpherePointFor(new Position(point.X, point.Y, point.Z + collisionHeight), hitPos);

            if (!source.getMap().isInLineOfSight(source.getPhaseShift(), hitPos, point.X, point.Y, point.Z + collisionHeight, LineOfSightChecks.All, ModelIgnoreFlags.Nothing)) {
                // whenver we find a point that is not in LoS anymore, simply use last valid path
                tangible.RefObject<T[]> tempRef__pathPoints = new tangible.RefObject<T[]>(pathPoints);
                Array.Resize(tempRef__pathPoints, i + 1);
                pathPoints = tempRef__pathPoints.refArgValue;

                return;
            }

            if (--i == 0) {
                // no point found that fulfills the condition
                _pathPoints[0] = _pathPoints[1];
                tangible.RefObject<T[]> tempRef__pathPoints2 = new tangible.RefObject<T[]>(pathPoints);
                Array.Resize(tempRef__pathPoints2, 2);
                pathPoints = tempRef__pathPoints2.refArgValue;

                return;
            }
        }

        // ok, _pathPoints[i] is too close, _pathPoints[i-1] is not, so our target point is somewhere between the two...
        //   ... settle for a guesstimate since i'm not confident in doing trig on every chase motion tick...
        // (@todo review this)
        _pathPoints[i] += (_pathPoints[i - 1] - _pathPoints[i]).direction() * (dist - (_pathPoints[i] - target).length());
        tangible.RefObject<T[]> tempRef__pathPoints3 = new tangible.RefObject<T[]>(pathPoints);
        Array.Resize(tempRef__pathPoints3, i + 1);
        pathPoints = tempRef__pathPoints3.refArgValue;
    }

    public final boolean isInvalidDestinationZ(WorldObject target) {
        return (target.getLocation().getZ() - getActualEndPosition().Z) > 5.0f;
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

    public final Vector3 getActualEndPosition() {
        return actualEndPosition;
    }

    private void setActualEndPosition(Vector3 point) {
        actualEndPosition = point;
    }

    public final Vector3[] getPath() {
        return pathPoints;
    }

    public final EnumFlag<PathType> getPathType() {
        return pathType;
    }

    public final void setUseStraightPath(boolean useStraightPath) {
        useStraightPath = useStraightPath;
    }

    public final void setPathLengthLimit(float distance) {
        pointPathLimit = Math.min((int) (distance / 4.0f), 74);
    }

    public final void setUseRaycast(boolean useRaycast) {
        useRaycast = useRaycast;
    }

    private long getPathPolyByPosition(long[] polyPath, int polyPathSize, float[] point, tangible.RefObject<Float> distance) {
        if (polyPath == null || polyPathSize == 0) {
            return 0;
        }

        long nearestPoly = 0;
        var minDist = Float.MAX_VALUE;

        for (int i = 0; i < polyPathSize; ++i) {
            var closestPoint = new float[3];
            var posOverPoly = false;

            tangible.RefObject<Boolean> tempRef_posOverPoly = new tangible.RefObject<Boolean>(posOverPoly);
            if (Detour.dtStatusFailed(navMeshQuery.closestPointOnPoly(polyPath[i], point, closestPoint, tempRef_posOverPoly))) {
                posOverPoly = tempRef_posOverPoly.refArgValue;
                continue;
            } else {
                posOverPoly = tempRef_posOverPoly.refArgValue;
            }

            var d = Detour.dtVdistSqr(point, closestPoint);

            if (d < minDist) {
                minDist = d;
                nearestPoly = polyPath[i];
            }

            if (minDist < 1.0f) // shortcut out - close enough for us
            {
                break;
            }
        }

        distance.refArgValue = (float) Math.sqrt(minDist);

        return (minDist < 3.0f) ? nearestPoly : 0;
    }

    private long getPolyByLocation(float[] point, tangible.RefObject<Float> distance) {
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

        float[] closestPoint = {0.0f, 0.0f, 0.0f};

        tangible.RefObject<Long> tempRef_polyRef = new tangible.RefObject<Long>(polyRef);
        tangible.RefObject<float[]> tempRef_closestPoint = new tangible.RefObject<float[]>(closestPoint);
        if (Detour.dtStatusSucceed(navMeshQuery.findNearestPoly(point, extents, filter, tempRef_polyRef, tempRef_closestPoint)) && polyRef != 0) {
            closestPoint = tempRef_closestPoint.refArgValue;
            polyRef = tempRef_polyRef.refArgValue;
            distance.refArgValue = Detour.dtVdist(closestPoint, point);

            return polyRef;
        } else {
            closestPoint = tempRef_closestPoint.refArgValue;
            polyRef = tempRef_polyRef.refArgValue;
        }

        // still nothing ..
        // try with bigger search box
        // Note that the extent should not overlap more than 128 polygons in the navmesh (see dtNavMeshQuery.findNearestPoly)
        extents[1] = 50.0f;

        tangible.RefObject<Long> tempRef_polyRef2 = new tangible.RefObject<Long>(polyRef);
        tangible.RefObject<float[]> tempRef_closestPoint2 = new tangible.RefObject<float[]>(closestPoint);
        if (Detour.dtStatusSucceed(navMeshQuery.findNearestPoly(point, extents, filter, tempRef_polyRef2, tempRef_closestPoint2)) && polyRef != 0) {
            closestPoint = tempRef_closestPoint2.refArgValue;
            polyRef = tempRef_polyRef2.refArgValue;
            distance.refArgValue = Detour.dtVdist(closestPoint, point);

            return polyRef;
        } else {
            closestPoint = tempRef_closestPoint2.refArgValue;
            polyRef = tempRef_polyRef2.refArgValue;
        }

        distance.refArgValue = Float.MAX_VALUE;

        return 0;
    }

    private void buildPolyPath(Vector3 startPos, Vector3 endPos) {
        // *** getting start/end poly logic ***

        float distToStartPoly = 0;
        float distToEndPoly = 0;

        float[] startPoint = {startPos.y, startPos.z, startPos.x};

        float[] endPoint = {endPos.y, endPos.z, endPos.x};

        tangible.RefObject<Float> tempRef_distToStartPoly = new tangible.RefObject<Float>(distToStartPoly);
        var startPoly = getPolyByLocation(startPoint, tempRef_distToStartPoly);
        distToStartPoly = tempRef_distToStartPoly.refArgValue;
        tangible.RefObject<Float> tempRef_distToEndPoly = new tangible.RefObject<Float>(distToEndPoly);
        var endPoly = getPolyByLocation(endPoint, tempRef_distToEndPoly);
        distToEndPoly = tempRef_distToEndPoly.refArgValue;

        pathType = PathType.NORMAL;

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
                for (int i = 0; i < pathPoints.length; ++i) {
                    var status = source.getMap().getLiquidStatus(source.getPhaseShift(), _pathPoints[i].X, _pathPoints[i].Y, _pathPoints[i].Z, LiquidHeaderTypeFlags.AllLiquids, source.getCollisionHeight());

                    // One of the points is not in the water, cancel movement.
                    if (status == ZLiquidStatus.NoWater) {
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
            Logs.MAPS.debug("++ BuildPolyPath . farFromPoly distToStartPoly={0:F3} distToEndPoly={1:F3}\n", distToStartPoly, distToEndPoly);

            var buildShotrcut = false;
            var p = (distToStartPoly > 7.0f) ? startPos : endPos;

            if (source.getMap().isUnderWater(source.getPhaseShift(), p.X, p.Y, p.Z)) {
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
                    else if (_sourceUnit.isFalling() && endPos.Z < startPos.Z) {
                        buildShotrcut = true;
                    }
                }
            }

            if (buildShotrcut) {
                buildShortcut();
                pathType = PathType.forValue(PathType.NORMAL.getValue() | PathType.NOTUSINGPATH.getValue());

                addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);

                return;
            } else {
                var closestPoint = new float[3];
                // we may want to use closestPointOnPolyBoundary instead
                var posOverPoly = false;

                tangible.RefObject<Boolean> tempRef_posOverPoly = new tangible.RefObject<Boolean>(posOverPoly);
                if (Detour.dtStatusSucceed(navMeshQuery.closestPointOnPoly(endPoly, endPoint, closestPoint, tempRef_posOverPoly))) {
                    posOverPoly = tempRef_posOverPoly.refArgValue;
                    Detour.dtVcopy(endPoint, closestPoint);
                    setActualEndPosition(new Vector3(endPoint[2], endPoint[0], endPoint[1]));
                } else {
                    posOverPoly = tempRef_posOverPoly.refArgValue;
                }

                pathType = PathType.INCOMPLETE;

                addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
            }
        }

        // *** poly path generating logic ***

        // start and end are on same polygon
        // handle this case as if they were 2 different polygons, building a line path split in some few points
        if (startPoly == endPoly && !useRaycast) {
            Logs.MAPS.debug("++ BuildPolyPath . (startPoly == endPoly)\n");

            _pathPolyRefs[0] = startPoly;
            polyLength = 1;

            if (startFarFromPoly || endFarFromPoly) {
                pathType = PathType.INCOMPLETE;

                addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
            } else {
                pathType = PathType.NORMAL;
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
                if (_pathPolyRefs[pathStartIndex] == 0) {
                    Logs.MAPS.error("Invalid poly ref in BuildPolyPath. _polyLength: {0}, pathStartIndex: {1}," + " startPos: {2}, endPos: {3}, mapid: {4}", polyLength, pathStartIndex, startPos, endPos, source.getLocation().getMapId());

                    break;
                }

                if (_pathPolyRefs[pathStartIndex] == startPoly) {
                    startPolyFound = true;

                    break;
                }
            }

            for (pathEndIndex = _polyLength - 1; pathEndIndex > pathStartIndex; --pathEndIndex) {
                if (_pathPolyRefs[pathEndIndex] == endPoly) {
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
            system.arraycopy(pathPolyRefs, pathStartIndex, pathPolyRefs, 0, polyLength);
        } else if (startPolyFound && !endPolyFound) {
            Logs.MAPS.debug("BuildPolyPath : (startPolyFound && !endPolyFound)\n");

            // we are moving on the old path but target moved out
            // so we have atleast part of poly-path ready

            _polyLength -= pathStartIndex;

            // try to adjust the suffix of the path instead of recalculating entire length
            // at given interval the target cannot get too far from its last location
            // thus we have less poly to cover
            // sub-path of optimal path is optimal

            // take ~80% of the original length
            // @todo play with the values here
            var prefixPolyLength = (int) (_polyLength * 0.8f + 0.5f);
            system.arraycopy(pathPolyRefs, pathStartIndex, pathPolyRefs, 0, prefixPolyLength);

            var suffixStartPoly = _pathPolyRefs[prefixPolyLength - 1];

            // we need any point on our suffix start poly to generate poly-path, so we need last poly in prefix data
            var suffixEndPoint = new float[3];
            var posOverPoly = false;

            tangible.RefObject<Boolean> tempRef_posOverPoly2 = new tangible.RefObject<Boolean>(posOverPoly);
            if (Detour.dtStatusFailed(navMeshQuery.closestPointOnPoly(suffixStartPoly, endPoint, suffixEndPoint, tempRef_posOverPoly2))) {
                posOverPoly = tempRef_posOverPoly2.refArgValue;
                // we can hit offmesh connection as last poly - closestPointOnPoly() don't like that
                // try to recover by using prev polyref
                --prefixPolyLength;
                suffixStartPoly = _pathPolyRefs[prefixPolyLength - 1];

                tangible.RefObject<Boolean> tempRef_posOverPoly3 = new tangible.RefObject<Boolean>(posOverPoly);
                if (Detour.dtStatusFailed(navMeshQuery.closestPointOnPoly(suffixStartPoly, endPoint, suffixEndPoint, tempRef_posOverPoly3))) {
                    posOverPoly = tempRef_posOverPoly3.refArgValue;
                    // suffixStartPoly is still invalid, error state
                    buildShortcut();
                    pathType = PathType.NOPATH;

                    return;
                } else {
                    posOverPoly = tempRef_posOverPoly3.refArgValue;
                }
            } else {
                posOverPoly = tempRef_posOverPoly2.refArgValue;
            }

            // generate suffix
            int suffixPolyLength = 0;
            var tempPolyRefs = new long[_pathPolyRefs.length];

            int dtResult;

            if (useRaycast) {
                Logs.MAPS.error(String.format("PathGenerator::BuildPolyPath() called with _useRaycast with a previous path for unit %1$s", source.getGUID()));
                buildShortcut();
                pathType = PathType.NOPATH;

                return;
            } else {
                tangible.RefObject<Integer> tempRef_suffixPolyLength = new tangible.RefObject<Integer>(suffixPolyLength);
                dtResult = navMeshQuery.findPath(suffixStartPoly, endPoly, suffixEndPoint, endPoint, filter, tempPolyRefs, tempRef_suffixPolyLength, 74 - (int) prefixPolyLength);
                suffixPolyLength = tempRef_suffixPolyLength.refArgValue;
            }

            if (suffixPolyLength == 0 || Detour.dtStatusFailed(dtResult)) {
                // this is probably an error state, but we'll leave it
                // and hopefully recover on the next Update
                // we still need to copy our preffix
                Logs.MAPS.error(String.format("Path Build failed\n%1$s", source.getDebugInfo()));
            }

            Logs.MAPS.debug("m_polyLength={0} prefixPolyLength={1} suffixPolyLength={2} \n", polyLength, prefixPolyLength, suffixPolyLength);

            for (var i = 0; i < pathPolyRefs.length - (prefixPolyLength - 1); ++i) {
                _pathPolyRefs[(prefixPolyLength - 1) + i] = tempPolyRefs[i];
            }

            // new path = prefix + suffix - overlap
            polyLength = prefixPolyLength + suffixPolyLength - 1;
        } else {
            Logs.MAPS.debug("++ BuildPolyPath . (!startPolyFound && !endPolyFound)\n");

            // either we have no path at all . first run
            // or something went really wrong . we aren't moving along the path to the target
            // just generate new path

            // free and invalidate old path data
            clear();

            int dtResult;

            if (useRaycast) {
                float hit = 0;
                var hitNormal = new float[3];

                tangible.RefObject<Float> tempRef_hit = new tangible.RefObject<Float>(hit);
                tangible.RefObject<Integer> tempRef__polyLength = new tangible.RefObject<Integer>(polyLength);
                dtResult = navMeshQuery.raycast(startPoly, startPoint, endPoint, filter, tempRef_hit, hitNormal, pathPolyRefs, tempRef__polyLength, 74);
                polyLength = tempRef__polyLength.refArgValue;
                hit = tempRef_hit.refArgValue;

                if (polyLength == 0 || Detour.dtStatusFailed(dtResult)) {
                    buildShortcut();
                    pathType = PathType.NOPATH;
                    addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);

                    return;
                }

                // raycast() sets hit to FLT_MAX if there is a ray between start and end
                if (hit != Float.MAX_VALUE) {
                    var hitPos = new float[3];

                    // Walk back a bit from the hit point to make sure it's in the mesh (sometimes the point is actually outside of the polygons due to float precision issues)
                    hit *= 0.99f;
                    Detour.dtVlerp(hitPos, startPoint, endPoint, hit);

                    // if it fails again, clamp to poly boundary
                    tangible.RefObject<Float> tempRef_Object = new tangible.RefObject<Float>(hitPos[1]);
                    if (Detour.dtStatusFailed(navMeshQuery.getPolyHeight(_pathPolyRefs[_polyLength - 1], hitPos, tempRef_Object))) {
                        hitPos[1] = tempRef_Object.refArgValue;
                        navMeshQuery.closestPointOnPolyBoundary(_pathPolyRefs[_polyLength - 1], hitPos, hitPos);
                    } else {
                        hitPos[1] = tempRef_Object.refArgValue;
                    }

                    pathPoints = new Vector3[2];
                    _pathPoints[0] = getStartPosition();
                    _pathPoints[1] = new Vector3(hitPos[2], hitPos[0], hitPos[1]);

                    normalizePath();
                    pathType = PathType.INCOMPLETE;
                    addFarFromPolyFlags(startFarFromPoly, false);

                    return;
                } else {
                    // clamp to poly boundary if we fail to get the height
                    tangible.RefObject<Float> tempRef_Object2 = new tangible.RefObject<Float>(endPoint[1]);
                    if (Detour.dtStatusFailed(navMeshQuery.getPolyHeight(_pathPolyRefs[_polyLength - 1], endPoint, tempRef_Object2))) {
                        endPoint[1] = tempRef_Object2.refArgValue;
                        navMeshQuery.closestPointOnPolyBoundary(_pathPolyRefs[_polyLength - 1], endPoint, endPoint);
                    } else {
                        endPoint[1] = tempRef_Object2.refArgValue;
                    }

                    pathPoints = new Vector3[2];
                    _pathPoints[0] = getStartPosition();
                    _pathPoints[1] = new Vector3(endPoint[2], endPoint[0], endPoint[1]);

                    normalizePath();

                    if (startFarFromPoly || endFarFromPoly) {
                        pathType = PathType.INCOMPLETE;

                        addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);
                    } else {
                        pathType = PathType.NORMAL;
                    }

                    return;
                }
            } else {
                tangible.RefObject<Integer> tempRef__polyLength2 = new tangible.RefObject<Integer>(polyLength);
                dtResult = navMeshQuery.findPath(startPoly, endPoly, startPoint, endPoint, filter, pathPolyRefs, tempRef__polyLength2, 74); // max number of polygons in output path
                polyLength = tempRef__polyLength2.refArgValue;
            }

            if (polyLength == 0 || Detour.dtStatusFailed(dtResult)) {
                // only happens if we passed bad data to findPath(), or navmesh is messed up
                Logs.MAPS.error("{0}'s Path Build failed: 0 length path", source.getGUID().toString());
                buildShortcut();
                pathType = PathType.NOPATH;

                return;
            }
        }

        // by now we know what type of path we can get
        if (_pathPolyRefs[_polyLength - 1] == endPoly && !pathType.hasFlag(PathType.INCOMPLETE)) {
            pathType = PathType.NORMAL;
        } else {
            pathType = PathType.INCOMPLETE;
        }

        addFarFromPolyFlags(startFarFromPoly, endFarFromPoly);

        // generate the point-path out of our up-to-date poly-path
        buildPointPath(startPoint, endPoint);
    }

    private void buildPointPath(float[] startPoint, float[] endPoint) {
        var pathPoints = new float[74 * 3];
        var pointCount = 0;
        int dtResult;

        if (useRaycast) {
            // _straightLine uses raycast and it currently doesn't support building a point path, only a 2-point path with start and hitpoint/end is returned
            Logs.MAPS.error(String.format("PathGenerator::BuildPointPath() called with _useRaycast for unit %1$s", source.getGUID()));
            buildShortcut();
            pathType = PathType.NOPATH;

            return;
        } else if (useStraightPath) {
            tangible.RefObject<Integer> tempRef_pointCount = new tangible.RefObject<Integer>(pointCount);
            dtResult = navMeshQuery.findStraightPath(startPoint, endPoint, pathPolyRefs, (int) polyLength, pathPoints, null, null, tempRef_pointCount, (int) pointPathLimit, 0); // maximum number of points/polygons to use
            pointCount = tempRef_pointCount.refArgValue;
        } else {
            tangible.OutObject<float[]> tempOut_pathPoints = new tangible.OutObject<float[]>();
            tangible.OutObject<Integer> tempOut_pointCount = new tangible.OutObject<Integer>();
            dtResult = findSmoothPath(startPoint, endPoint, pathPolyRefs, polyLength, tempOut_pathPoints, tempOut_pointCount, pointPathLimit); // maximum number of points
            pointCount = tempOut_pointCount.outArgValue;
            pathPoints = tempOut_pathPoints.outArgValue;
        }

        // Special case with start and end positions very close to each other
        if (polyLength == 1 && pointCount == 1) {
            // First point is start position, append end position
            Detour.dtVcopy(pathPoints, 1 * 3, endPoint, 0);
            pointCount++;
        } else if (pointCount < 2 || Detour.dtStatusFailed(dtResult)) {
            // only happens if pass bad data to findStraightPath or navmesh is broken
            // single point paths can be generated here
            // @todo check the exact cases
            Logs.MAPS.debug("++ PathGenerator.BuildPointPath FAILED! path sized {0} returned\n", pointCount);
            buildShortcut();
            pathType = PathType.forValue(pathType.getValue() | PathType.NOPATH.getValue());

            return;
        } else if (pointCount == pointPathLimit) {
            Logs.MAPS.debug("++ PathGenerator.BuildPointPath FAILED! path sized {0} returned, lower than limit set to {1}\n", pointCount, pointPathLimit);
            buildShortcut();
            pathType = PathType.forValue(pathType.getValue() | PathType.SHORT.getValue());

            return;
        }

        pathPoints = new Vector3[pointCount];

        for (int i = 0; i < pointCount; ++i) {
            _pathPoints[i] = new Vector3(pathPoints[i * 3 + 2], pathPoints[i * 3], pathPoints[i * 3 + 1]);
        }

        normalizePath();

        // first point is always our current location - we need the next one
        setActualEndPosition(_pathPoints[pointCount - 1]);

        // force the given destination, if needed
        if (forceDestination && (!pathType.hasFlag(PathType.NORMAL) || !inRange(getEndPosition(), getActualEndPosition(), 1.0f, 1.0f))) {
            // we may want to keep partial subpath
            if (dist3DSqr(getActualEndPosition(), getEndPosition()) < 0.3f * dist3DSqr(getStartPosition(), getEndPosition())) {
                setActualEndPosition(getEndPosition());

                _pathPoints[ ^ 1] =getEndPosition();
            } else {
                setActualEndPosition(getEndPosition());
                buildShortcut();
            }

            pathType = PathType.forValue(PathType.NORMAL.getValue() | PathType.NOTUSINGPATH.getValue());
        }

        Logs.MAPS.debug("PathGenerator.BuildPointPath path type {0} size {1} poly-size {2}\n", pathType, pointCount, polyLength);
    }

    private int fixupCorridor(long[] path, int npath, int maxPath, long[] visited, int nvisited) {
        var furthestPath = -1;
        var furthestVisited = -1;

        // Find furthest common polygon.
        for (var i = (int) npath - 1; i >= 0; --i) {
            var found = false;

            for (var j = (int) nvisited - 1; j >= 0; --j) {
                if (path[i] == visited[j]) {
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
            return npath;
        }

        // Concatenate paths.

        // Adjust beginning of the buffer to include the visited.
        var req = (int) (nvisited - furthestVisited);
        var orig = (int) ((furthestPath + 1) < npath ? furthestPath + 1 : (int) npath);
        var size = npath > orig ? npath - orig : 0;

        if (req + size > maxPath) {
            size = maxPath - req;
        }

        if (size != 0) {
            system.arraycopy(path, (int) orig, path, (int) req, (int) size);
        }

        // Store visited
        for (int i = 0; i < req; ++i) {
            path[i] = visited[(nvisited - 1) - i];
        }

        return req + size;
    }

    private boolean getSteerTarget(float[] startPos, float[] endPos, float minTargetDist, long[] path, int pathSize, tangible.OutObject<float[]> steerPos, tangible.OutObject<Detour.dtStraightPathFlags> steerPosFlag, tangible.OutObject<Long> steerPosRef) {
        steerPosRef.outArgValue = 0;
        steerPos.outArgValue = new float[3];
        steerPosFlag.outArgValue = Detour.dtStraightPathFlags.forValue(0);

        // Find steer target.
        var steerPath = new float[3 * 3];
        var steerPathFlags = new byte[3];
        var steerPathPolys = new long[3];
        var nsteerPath = 0;
        tangible.RefObject<Integer> tempRef_nsteerPath = new tangible.RefObject<Integer>(nsteerPath);
        var dtResult = navMeshQuery.findStraightPath(startPos, endPos, path, (int) pathSize, steerPath, steerPathFlags, steerPathPolys, tempRef_nsteerPath, 3, 0);
        nsteerPath = tempRef_nsteerPath.refArgValue;

        if (nsteerPath == 0 || Detour.dtStatusFailed(dtResult)) {
            return false;
        }

        // Find vertex far enough to steer to.
        int ns = 0;

        while (ns < nsteerPath) {
            Span<Float> span = steerPath;

            // Stop at Off-Mesh link or when point is further than slop away.

            if ((steerPathFlags[ns].hasFlag((byte) Detour.dtStraightPathFlags.DT_STRAIGHTPATH_OFFMESH_CONNECTION.getValue()) || !inRangeYZX(span[(new integer(ns * 3))..].
            ToArray(), startPos, minTargetDist, 1000.0f)))
            {
                break;
            }

            ns++;
        }

        // Failed to find good point to steer to.
        if (ns >= nsteerPath) {
            return false;
        }

        Detour.dtVcopy(steerPos.outArgValue, 0, steerPath, (int) ns * 3);
        steerPos.outArgValue[1] = startPos[1]; // keep Z second
        steerPosFlag.outArgValue = Detour.dtStraightPathFlags.forValue(steerPathFlags[ns]);
        steerPosRef.outArgValue = steerPathPolys[ns];

        return true;
    }

    private int findSmoothPath(float[] startPos, float[] endPos, long[] polyPath, int polyPathSize, tangible.OutObject<float[]> smoothPath, tangible.OutObject<Integer> smoothPathSize, int maxSmoothPathSize) {
        smoothPathSize.outArgValue = 0;
        var nsmoothPath = 0;
        smoothPath.outArgValue = new float[74 * 3];

        var polys = new long[74];
        system.arraycopy(polyPath, 0, polys, 0, polyPathSize);
        var npolys = polyPathSize;

        var iterPos = new float[3];
        var targetPos = new float[3];

        if (polyPathSize > 1) {
            // Pick the closest points on poly border
            if (Detour.dtStatusFailed(navMeshQuery.closestPointOnPolyBoundary(polys[0], startPos, iterPos))) {
                return Detour.DT_FAILURE;
            }

            if (Detour.dtStatusFailed(navMeshQuery.closestPointOnPolyBoundary(polys[npolys - 1], endPos, targetPos))) {
                return Detour.DT_FAILURE;
            }
        } else {
            // Case where the path is on the same poly
            Detour.dtVcopy(iterPos, startPos);
            Detour.dtVcopy(targetPos, endPos);
        }

        Detour.dtVcopy(smoothPath.outArgValue, nsmoothPath * 3, iterPos, 0);
        nsmoothPath++;

        // Move towards target a small advancement at a time until target reached or
        // when ran out of memory to store the path.
        while (npolys != 0 && nsmoothPath < maxSmoothPathSize) {
            // Find location to steer towards.
            float[] steerPos;
            tangible.OutObject<float[]> tempOut_steerPos = new tangible.OutObject<float[]>();
            Detour.dtStraightPathFlags steerPosFlag;
            tangible.OutObject<Detour.dtStraightPathFlags> tempOut_steerPosFlag = new tangible.OutObject<Detour.dtStraightPathFlags>();
            long steerPosRef;
            tangible.OutObject<Long> tempOut_steerPosRef = new tangible.OutObject<Long>();
            if (!getSteerTarget(iterPos, targetPos, 0.3f, polys, npolys, tempOut_steerPos, tempOut_steerPosFlag, tempOut_steerPosRef)) {
                steerPosRef = tempOut_steerPosRef.outArgValue;
                steerPosFlag = tempOut_steerPosFlag.outArgValue;
                steerPos = tempOut_steerPos.outArgValue;
                break;
            } else {
                steerPosRef = tempOut_steerPosRef.outArgValue;
                steerPosFlag = tempOut_steerPosFlag.outArgValue;
                steerPos = tempOut_steerPos.outArgValue;
            }

            var endOfPath = steerPosFlag.hasFlag(Detour.dtStraightPathFlags.DT_STRAIGHTPATH_END);
            var offMeshConnection = steerPosFlag.hasFlag(Detour.dtStraightPathFlags.DT_STRAIGHTPATH_OFFMESH_CONNECTION);

            // Find movement delta.
            var delta = new float[3];
            Detour.dtVsub(delta, steerPos, iterPos);
            var len = (float) Math.sqrt(Detour.dtVdot(delta, delta));

            // If the steer target is end of path or off-mesh link, do not move past the location.
            if ((endOfPath || offMeshConnection) && len < 4.0f) {
                len = 1.0f;
            } else {
                len = 4.0f / len;
            }

            var moveTgt = new float[3];
            Detour.dtVmad(moveTgt, iterPos, delta, len);

            // Move
            var result = new float[3];
            var MAX_VISIT_POLY = 16;
            var visited = new long[MAX_VISIT_POLY];

            var nvisited = 0;

            tangible.RefObject<Integer> tempRef_nvisited = new tangible.RefObject<Integer>(nvisited);
            if (Detour.dtStatusFailed(navMeshQuery.moveAlongSurface(polys[0], iterPos, moveTgt, filter, result, visited, tempRef_nvisited, MAX_VISIT_POLY))) {
                nvisited = tempRef_nvisited.refArgValue;
                return Detour.DT_FAILURE;
            } else {
                nvisited = tempRef_nvisited.refArgValue;
            }

            npolys = fixupCorridor(polys, npolys, 74, visited, nvisited);

            tangible.RefObject<Float> tempRef_Object = new tangible.RefObject<Float>(result[1]);
            if (Detour.dtStatusFailed(navMeshQuery.getPolyHeight(polys[0], result, tempRef_Object))) {
                result[1] = tempRef_Object.refArgValue;
                Logs.MAPS.debug(String.format("Cannot find height at position X: %1$s Y: %2$s Z: %3$s for %4$s", result[2], result[0], result[1], source.getDebugInfo()));
            } else {
                result[1] = tempRef_Object.refArgValue;
            }

            result[1] += 0.5f;
            Detour.dtVcopy(iterPos, result);

            // Handle end of path and off-mesh links when close enough.
            if (endOfPath && inRangeYZX(iterPos, steerPos, 0.3f, 1.0f)) {
                // Reached end of path.
                Detour.dtVcopy(iterPos, targetPos);

                if (nsmoothPath < maxSmoothPathSize) {
                    Detour.dtVcopy(smoothPath.outArgValue, nsmoothPath * 3, iterPos, 0);
                    nsmoothPath++;
                }

                break;
            } else if (offMeshConnection && inRangeYZX(iterPos, steerPos, 0.3f, 1.0f)) {
                // Advance the path up to and over the off-mesh connection.
                long prevRef = 0;
                var polyRef = polys[0];
                int npos = 0;

                while (npos < npolys && polyRef != steerPosRef) {
                    prevRef = polyRef;
                    polyRef = polys[npos];
                    npos++;
                }

                for (var i = npos; i < npolys; ++i) {
                    polys[i - npos] = polys[i];
                }

                npolys -= npos;

                // Handle the connection.
                var connectionStartPos = new float[3];
                var connectionEndPos = new float[3];

                if (Detour.dtStatusSucceed(navMesh.getOffMeshConnectionPolyEndPoints(prevRef, polyRef, connectionStartPos, connectionEndPos))) {
                    if (nsmoothPath < maxSmoothPathSize) {
                        Detour.dtVcopy(smoothPath.outArgValue, nsmoothPath * 3, connectionStartPos, 0);
                        nsmoothPath++;
                    }

                    // Move position at the other side of the off-mesh link.
                    Detour.dtVcopy(iterPos, connectionEndPos);

                    tangible.RefObject<Float> tempRef_Object2 = new tangible.RefObject<Float>(iterPos[1]);
                    if (Detour.dtStatusFailed(navMeshQuery.getPolyHeight(polys[0], iterPos, tempRef_Object2))) {
                        iterPos[1] = tempRef_Object2.refArgValue;
                        return Detour.DT_FAILURE;
                    } else {
                        iterPos[1] = tempRef_Object2.refArgValue;
                    }

                    iterPos[1] += 0.5f;
                }
            }

            // Store results.
            if (nsmoothPath < maxSmoothPathSize) {
                Detour.dtVcopy(smoothPath.outArgValue, nsmoothPath * 3, iterPos, 0);
                nsmoothPath++;
            }
        }

        smoothPathSize.outArgValue = nsmoothPath;

        // this is most likely a loop
        return nsmoothPath < 74 ? Detour.DT_SUCCESS : Detour.DT_FAILURE;
    }

    private void normalizePath() {
        for (int i = 0; i < pathPoints.length; ++i) {
            var point = _pathPoints[i];
            point.Z = source.updateAllowedPositionZ(point.X, point.Y, point.Z);
        }
    }

    private void buildShortcut() {
        Logs.MAPS.debug("BuildShortcut : making shortcut\n");

        clear();

        // make two point path, our curr pos is the start, and dest is the end
        pathPoints = new Vector3[2];

        // set start and a default next position
        _pathPoints[0] = getStartPosition();
        _pathPoints[1] = getActualEndPosition();

        normalizePath();

        pathType = PathType.SHORTCUT;
    }

    private void createFilter() {
        NavTerrainFlag includeFlags = NavTerrainFlag.forValue(0);
        NavTerrainFlag excludeFlags = NavTerrainFlag.forValue(0);

        if (source.isTypeId(TypeId.UNIT)) {
            var creature = source.toCreature();

            if (creature.canWalk()) {
                includeFlags = NavTerrainFlag.forValue(includeFlags.getValue() | NavTerrainFlag.ground.getValue());
            }

            // creatures don't take environmental damage
            if (creature.canEnterWater()) {
                includeFlags = NavTerrainFlag.forValue(includeFlags.getValue() | NavTerrainFlag.forValue(NavTerrainFlag.Water.getValue() | NavTerrainFlag.MagmaSlime.getValue()).getValue());
            }
        } else {
            includeFlags = NavTerrainFlag.forValue(NavTerrainFlag.ground.getValue() | NavTerrainFlag.Water.getValue() | NavTerrainFlag.MagmaSlime.getValue());
        }

        filter.setIncludeFlags((short) includeFlags.getValue());
        filter.setExcludeFlags((short) excludeFlags.getValue());

        updateFilter();
    }

    private void updateFilter() {
        // allow creatures to cheat and use different movement types if they are moved
        // forcefully into terrain they can't normally move in
        var _sourceUnit = source.toUnit();

        if (_sourceUnit != null) {
            if (_sourceUnit.isInWater() || _sourceUnit.isUnderWater()) {
                var includedFlags = NavTerrainFlag.forValue(filter.getIncludeFlags());
                includedFlags = NavTerrainFlag.forValue(includedFlags.getValue() | getNavTerrain(source.getLocation().getX(), source.getLocation().getY(), source.getLocation().getZ()).getValue());

                filter.setIncludeFlags((short) includedFlags.getValue());
            }

            var _sourceCreature = source.toCreature();

            if (_sourceCreature != null) {
                if (_sourceCreature.isInCombat() || _sourceCreature.isInEvadeMode()) {
                    filter.setIncludeFlags((short) (filter.getIncludeFlags() | (short) NavTerrainFlag.GroundSteep.getValue()));
                }
            }
        }
    }

    private NavTerrainFlag getNavTerrain(float x, float y, float z) {
        LiquidData data;
        tangible.OutObject<LiquidData> tempOut_data = new tangible.OutObject<LiquidData>();
        var liquidStatus = source.getMap().getLiquidStatus(source.getPhaseShift(), x, y, z, LiquidHeaderTypeFlags.AllLiquids, tempOut_data, source.getCollisionHeight());
        data = tempOut_data.outArgValue;

        if (liquidStatus == ZLiquidStatus.NoWater) {
            return NavTerrainFlag.ground;
        }

        data.type_flags = LiquidHeaderTypeFlags.forValue(data.type_flags.getValue() & ~LiquidHeaderTypeFlags.DarkWater.getValue());

        switch (data.type_flags) {
            case Water:
            case Ocean:
                return NavTerrainFlag.Water;
            case Magma:
            case Slime:
                return NavTerrainFlag.MagmaSlime;
            default:
                return NavTerrainFlag.ground;
        }
    }

    private boolean inRange(Vector3 p1, Vector3 p2, float r, float h) {
        var d = p1 - p2;

        return (d.X * d.X + d.Y * d.Y) < r * r && Math.abs(d.Z) < h;
    }

    private float dist3DSqr(Vector3 p1, Vector3 p2) {
        return (p1 - p2).LengthSquared();
    }

    private void addFarFromPolyFlags(boolean startFarFromPoly, boolean endFarFromPoly) {
        if (startFarFromPoly) {
            pathType = PathType.forValue(pathType.getValue() | PathType.FARFROMPOLYSTART.getValue());
        }

        if (endFarFromPoly) {
            pathType = PathType.forValue(pathType.getValue() | PathType.FARFROMPOLYEND.getValue());
        }
    }

    private void clear() {
        polyLength = 0;
        pathPoints = null;
    }

    private boolean haveTile(Vector3 p) {
        int tx = -1, ty = -1;

        float[] point = {p.Y, p.Z, p.X};

        tangible.RefObject<Integer> tempRef_tx = new tangible.RefObject<Integer>(tx);
        tangible.RefObject<Integer> tempRef_ty = new tangible.RefObject<Integer>(ty);
        navMesh.calcTileLoc(point, tempRef_tx, tempRef_ty);
        ty = tempRef_ty.refArgValue;
        tx = tempRef_tx.refArgValue;

        // Workaround
        // For some reason, often the tx and ty variables wont get a valid second
        // Use this check to prevent getting negative tile coords and crashing on getTileAt
        if (tx < 0 || ty < 0) {
            return false;
        }

        return (navMesh.getTileAt(tx, ty, 0) != null);
    }

    private boolean inRangeYZX(float[] v1, float[] v2, float r, float h) {
        var dx = v2[0] - v1[0];
        var dy = v2[1] - v1[1]; // elevation
        var dz = v2[2] - v1[2];

        return (dx * dx + dz * dz) < r * r && Math.abs(dy) < h;
    }
}
