package com.github.azeroth.game.map.collision;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.game.domain.condition.DisableFlags;
import com.github.azeroth.game.condition.DisableManager;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.model.*;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.map.MapDefine;

import com.github.azeroth.game.domain.map.enums.LoadResult;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.AreaInfo;
import com.github.azeroth.game.map.model.GameObjectModel;
import com.github.azeroth.game.map.model.LocationInfo;
import com.github.azeroth.game.map.model.WorldModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


public class VMapManager {


    private final HashMap<String, WorldModel> loadedModelFiles = new HashMap<>();
    private final HashMap<Integer, StaticMapTree> instanceMapTrees = new HashMap<>();
    private final HashMap<Integer, Integer> parentMapData = new HashMap<>();
    private final ReentrantLock loadedModelFilesLock = new ReentrantLock();
    private boolean enableLineOfSightCalc;
    private boolean enableHeightCalc;
    private DbcObjectManager dbcObjectManager;
    private DisableManager disableManager;
    private Path dataPath;

    private VMapManager() {
    }

    public final boolean isLineOfSightCalcEnabled() {
        return enableLineOfSightCalc;
    }

    public final boolean isHeightCalcEnabled() {
        return enableHeightCalc;
    }

    public final boolean isMapLoadingEnabled() {
        return enableLineOfSightCalc || enableHeightCalc;
    }

    public final LoadResult loadMap(int mapId, int x, int y)  {
        if (!isMapLoadingEnabled()) {
            return LoadResult.DisabledInConfig;
        }

        var instanceTree = instanceMapTrees.get(mapId);

        if (instanceTree == null) {

            Path mapFilePath = getVMapPath().resolve(getMapFileName(mapId));

            StaticMapTree newTree = new StaticMapTree(mapId);
            var treeInitResult = newTree.initMap(mapFilePath);

            if (treeInitResult != LoadResult.Success) {
                return treeInitResult;
            }

            instanceMapTrees.put(mapId, newTree);

            instanceTree = newTree;
        }

        return instanceTree.loadMapTile(x, y, this);
    }

    public final void unloadMap(int mapId, int x, int y) {
        var instanceTree = instanceMapTrees.get(mapId);

        if (instanceTree != null) {
            instanceTree.unloadMapTile(x, y, this);

            if (instanceTree.numLoadedTiles() == 0) {
                instanceMapTrees.remove(mapId);
            }
        }
    }

    public final void unloadMap(int mapId) {
        var instanceTree = instanceMapTrees.get(mapId);

        if (instanceTree != null) {
            instanceTree.unloadMap(this);

            if (instanceTree.numLoadedTiles() == 0) {
                instanceMapTrees.remove(mapId);
            }
        }
    }

    public final boolean isInLineOfSight(int mapId, Position xyz1, Position xyz2, ModelIgnoreFlags ignoreFlags) {
        if (!isLineOfSightCalcEnabled() || disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPLOS.getValue())) {
            return true;
        }

        var instanceTree = instanceMapTrees.get(mapId);

        if (instanceTree != null) {
            var pos1 = convertPositionToInternalRep(xyz1.getX(), xyz1.getY(), xyz1.getZ());
            var pos2 = convertPositionToInternalRep(xyz2.getX(), xyz2.getY(), xyz2.getZ());

            if (pos1 != pos2) {
                return instanceTree.isInLineOfSight(pos1, pos2, ignoreFlags);
            }
        }

        return true;
    }

    public final boolean getObjectHitPos(int mapId, Position xyz1, Position xyz2, Position resultXYZ, float modifyDist) {
        if (isLineOfSightCalcEnabled() && !disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPLOS.getValue())) {
            var instanceTree = instanceMapTrees.get(mapId);

            if (instanceTree != null) {
                var pos1 = convertPositionToInternalRep(xyz1.getX(), xyz1.getY(), xyz1.getZ());
                var pos2 = convertPositionToInternalRep(xyz2.getX(), xyz2.getY(), xyz2.getZ());
                var outVector = new Vector3();
                var result = instanceTree.getObjectHitPos(pos1, pos2, outVector, modifyDist);
                outVector = convertPositionToInternalRep(outVector.x, outVector.y, outVector.z);

                resultXYZ.setX(outVector.x);
                resultXYZ.setY(outVector.y);
                resultXYZ.setZ(outVector.z);

                return result;
            }
        }

        resultXYZ.setX(xyz2.getX());
        resultXYZ.setY(xyz2.getY());
        resultXYZ.setZ(xyz2.getZ());

        return false;
    }

    public final float getHeight(int mapId, float x, float y, float z, float maxSearchDist) {
        if (isHeightCalcEnabled() && !disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPHEIGHT.getValue())) {
            var instanceTree = instanceMapTrees.get(mapId);

            if (instanceTree != null) {
                var pos = convertPositionToInternalRep(x, y, z);
                var height = instanceTree.getHeight(pos, maxSearchDist);

                if (Float.isInfinite(height)) {
                    height = MapDefine.VMAP_INVALID_HEIGHT_VALUE; // No height
                }

                return height;
            }
        }

        return MapDefine.VMAP_INVALID_HEIGHT_VALUE;
    }

    public final boolean getAreaInfo(int mapId, Position pos, AreaInfo areaInfo) {

        if (!disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPAREAFLAG.getValue())) {
            var instanceTree = instanceMapTrees.get(mapId);

            if (instanceTree != null) {
                var vector3 = convertPositionToInternalRep(pos.getX(), pos.getY(), pos.getZ());
                var result = instanceTree.getAreaInfo(vector3, areaInfo);

                // z is not touched by convertPositionToInternalRep(), so just copy
                pos.setZ(vector3.z);

                return result;
            }
        }

        return false;
    }

    public final boolean getLiquidLevel(int mapId, Vector3 v, int reqLiquidType, AreaInfo areaInfo) {
        if (!disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPLIQUIDSTATUS.getValue())) {
            var instanceTree = instanceMapTrees.get(mapId);

            if (instanceTree != null) {
                LocationInfo info = new LocationInfo();
                var pos = convertPositionToInternalRep(v.x, v.y, v.z);

                if (instanceTree.getLocationInfo(pos, info)) {
                    areaInfo.floorZ = info.groundZ;
                    areaInfo.liquidType = info.hitModel.getLiquidType(); // entry from liquidType.dbc
                    areaInfo.flags = info.hitModel.getIMogpFlags();

                    if (reqLiquidType != 0 && (dbcObjectManager.getLiquidFlags(areaInfo.liquidType) & reqLiquidType) == 0) {
                        return false;
                    }
                    Distance liquidLevelRef = new Distance(0);
                    if (info.hitInstance.getLiquidLevel(pos, info, liquidLevelRef)) {
                        areaInfo.liquidLevel = liquidLevelRef.distance;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public final AreaInfo getAreaAndLiquidData(int mapId, Position pos, Byte reqLiquidType) {
        var result = new AreaInfo();

        if (disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPLIQUIDSTATUS.getValue())) {
            result.floorZ = pos.getZ();
            AreaInfo areaInfo = new AreaInfo();
            getAreaInfo(mapId, pos, areaInfo);

            return result;
        }

        var instanceTree = instanceMapTrees.get(mapId);

        if (instanceTree != null) {
            LocationInfo info = new LocationInfo();
            var vector3 = convertPositionToInternalRep(pos.getX(), pos.getY(), pos.getZ());

            if (instanceTree.getLocationInfo(vector3, info)) {
                result.floorZ = info.groundZ;
                result.result = info.result;
                var liquidType = info.hitModel.getLiquidType();


                if (reqLiquidType == null || (dbcObjectManager.getLiquidFlags(liquidType) & reqLiquidType) != 0) {
                    Distance liquidLevelRef = new Distance();
                    if (info.hitInstance.getLiquidLevel(vector3, info, liquidLevelRef)) {
                        result.liquidType = liquidType;
                        result.liquidLevel = liquidLevelRef.distance;
                        result.liquidEnabled = true;
                    }
                }

                if (!disableManager.isVMAPDisabledFor(mapId, (byte) DisableFlags.VMAPLIQUIDSTATUS.getValue())) {
                    result.adtId = info.hitInstance.adtId;
                    result.rootId = info.rootId;
                    result.groupId = info.hitModel.getIGroupWmoid();
                    result.flags = info.hitModel.getIMogpFlags();
                }
            }
        }

        return result;
    }


    public boolean loadGameObjectModelList() {

        var oldMSTime = System.currentTimeMillis();
        var filename = getVMapPath().resolve(MapDefine.GAME_OBJECT_MODELS);

        if (!Files.exists(filename)) {
            Logs.MISC.error("Unable to open '{}' file.", filename);
            return false;
        }


        try (FileChannel fileChannel = FileChannel.open(filename)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip();
            byte[] stringBytes = new byte[8];
            buffer.get(stringBytes);

            if (!Objects.equals(new String(stringBytes, StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {

                Logs.MISC.error("File '{}' has wrong header, expected {}.", fileChannel, MapDefine.VMAP_MAGIC);

                return false;
            }


            while (buffer.remaining() != 0) {

                var displayId = buffer.getInt();
                var isWmo = buffer.get() == 1;
                var nameLength = buffer.getInt();
                byte[] strBytes = new byte[nameLength];
                buffer.get(strBytes);
                var name = new String(strBytes, StandardCharsets.UTF_8);

                var v1 = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
                var v2 = new Vector3(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());

                if (v1.isZero() || v2.isZero()) {
                    Logs.MISC.error("File '{}' Model '{}' has invalid v1{} v2{} values!", filename, name, v1, v2);
                    continue;
                }

                GameObjectModel.MODELS.put(displayId, new GameObjectModelData(name, v1, v2, isWmo));
            }

        } catch (IOException ex) {
            Logs.MISC.error("Loaded {} failed.", filename);
            return false;
        }
        Logs.SERVER_LOADING.info(">> Loaded {} GameObject models in {} ms", GameObjectModel.MODELS.size, System.currentTimeMillis() - oldMSTime);
        return true;

    }


    public final WorldModel acquireModelInstance(String filename) {
        return acquireModelInstance(filename, 0);
    }

    public final WorldModel acquireModelInstance(String fileName, int flags) {

        return loadedModelFiles.compute(fileName, (k, v) -> {
            if (v == null) {
                v = new WorldModel();


                Path filePath = getVMapPath().resolve(fileName);
                if (!v.readFile(filePath)) {
                    Logs.MISC.error("VMapManager: could not load '{}'", filePath);

                    return null;
                }

                Logs.MISC.debug("VMapManager: loading file '{}'", filePath);
                v.setFlags(flags);
                v.setName(fileName);

            }
            return v;
        });
    }

    public final void releaseModelInstance(String filename) {
        var model = loadedModelFiles.get(filename);

        if (model == null) {
            Logs.MISC.error("VMapManager: trying to unload non-loaded file '{}'", filename);

            return;
        }

        if (model.decRefCount() == 0) {
            Logs.MAPS.debug("VMapManager: unloading file '{}'", filename);
            loadedModelFiles.remove(filename);
        }
    }

    public final LoadResult existsMap(int mapId, int x, int y) {
        return StaticMapTree.canLoadMap(this, mapId, x, y);
    }

    public final int getParentMapId(int mapId) {
        if (parentMapData.containsKey(mapId)) {
            return (int) parentMapData.get(mapId);
        }

        return -1;
    }

    public String getMapFileName(int mapId) {
        return String.format("%4d.vmtree", mapId);
    }

    public final void setEnableLineOfSightCalc(boolean pVal) {
        enableLineOfSightCalc = pVal;
    }

    public final void setEnableHeightCalc(boolean pVal) {
        enableHeightCalc = pVal;
    }

    private Vector3 convertPositionToInternalRep(float x, float y, float z) {
        Vector3 pos = new Vector3();
        var mid = 0.5f * 64.0f * 533.33333333f;
        pos.x = mid - x;
        pos.y = mid - y;
        pos.z = z;

        return pos;
    }

    public Path getVMapPath() {
        return dataPath.resolve("/vmaps");
    }
}
