package com.github.azeroth.game.map.collision;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.IntIntMap;
import com.github.azeroth.common.Logs;
import com.github.azeroth.common.Pair;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.model.*;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.enums.LoadResult;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.AreaInfo;
import com.github.azeroth.game.map.model.LocationInfo;
import com.github.azeroth.game.map.model.ModelInstance;
import com.github.azeroth.game.map.model.WorldModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;
import java.util.Objects;


public class StaticMapTree {

    private final int mapId;
    private final BIH tree = new BIH();
    private final IntIntMap spawnIndices = new IntIntMap();
    private final BitSet loadedTiles = new BitSet();
    private final IntIntMap loadedSpawns = new IntIntMap();
    private ModelInstance[] treeValues;
    private int nTreeValues;

    public StaticMapTree(int mapId) {
        this.mapId = mapId;
    }

    public static LoadResult canLoadMap(VMapManager vm, int mapID, int tileX, int tileY) {

        Path mapPath = vm.getVMapPath().resolve(vm.getMapFileName(mapID));


        if (!Files.exists(mapPath)) {
            return LoadResult.FileNotFound;
        }

        try (FileChannel fileChannel = FileChannel.open(mapPath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            fileChannel.read(buffer);
            if (!Objects.equals(new String(buffer.array(), StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {
                return LoadResult.VersionMismatch;
            }
        } catch (IOException e) {
            Logs.MISC.error("StaticMapTree::CanLoadMapTile() : cannot read map file {}", mapPath, e);
            return LoadResult.ReadFromFileFailed;
        }

        Path tileFile = openMapTileFile(vm, mapID, tileX, tileY).first();


        if (!Files.exists(tileFile)) {
            return LoadResult.FileNotFound;
        }

        try (FileChannel fileChannel = FileChannel.open(tileFile, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            fileChannel.read(buffer);
            if (!Objects.equals(new String(buffer.array(), StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {
                return LoadResult.VersionMismatch;
            }
        } catch (IOException e) {
            Logs.MISC.error("StaticMapTree::CanLoadMapTile() : cannot read title map file {}", tileFile, e);
            return LoadResult.ReadFromFileFailed;
        }

        return LoadResult.Success;
    }

    public static String getTileFileName(int mapID, int tileX, int tileY) {
        return String.format("%4d_%2d_%2d.vmtile", mapID, tileY, tileX);
    }

    private static int packTileID(int tileX, int tileY) {
        return tileX << 16 | tileY;
    }

    private static void unpackTileID(int id, Position xyPos) {
        xyPos.setX(id >>> 16);
        xyPos.setY(id & 0xFF);
    }

    private static Pair<Path, Integer> openMapTileFile(VMapManager vm, int mapID, int tileX, int tileY) {
        Path vmapPath = vm.getVMapPath();
        Path titleFilePath = vmapPath.resolve(getTileFileName(mapID, tileX, tileY));
        if (Files.exists(titleFilePath)) {

            return Pair.of(titleFilePath, mapID);
        }

        var parentMapId = vm.getParentMapId(mapID);

        while (parentMapId != -1) {

            titleFilePath = vmapPath.resolve(getTileFileName(parentMapId, tileX, tileY));
            if (Files.exists(titleFilePath)) {

                return Pair.of(titleFilePath, parentMapId);
            }

            parentMapId = vm.getParentMapId(parentMapId);
        }

        return Pair.of(titleFilePath, mapID);
    }

    public final LoadResult initMap(Path filePath) {


        Logs.MAPS.debug("StaticMapTree::InitMap() : initializing StaticMapTree '{}'", filePath);


        if (!Files.exists(filePath)) {
            return LoadResult.FileNotFound;
        }

        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip();
            byte[] stringBytes = new byte[8];
            buffer.get(stringBytes);

            if (!Objects.equals(new String(stringBytes, StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {
                return LoadResult.VersionMismatch;
            }
            stringBytes = new byte[4];
            buffer.get(stringBytes);
            if (!"NODE".equals(new String(stringBytes, StandardCharsets.UTF_8))) {
                return LoadResult.ReadFromFileFailed;
            }

            if (!tree.readFromFile(buffer)) {
                return LoadResult.ReadFromFileFailed;
            }

            nTreeValues = tree.primCount();
            treeValues = new ModelInstance[nTreeValues];

            stringBytes = new byte[4];
            buffer.get(stringBytes);
            if (!"SIDX".equals(new String(stringBytes, StandardCharsets.UTF_8))) {
                return LoadResult.ReadFromFileFailed;
            }

            var spawnIndicesSize = buffer.getInt();

            for (int i = 0; i < spawnIndicesSize; ++i) {
                var spawnId = buffer.getInt();
                spawnIndices.put(spawnId, i);
            }
        } catch (IOException e) {
            Logs.MAPS.error("StaticMapTree::InitMap() :  initializing StaticMapTree '{}' error.", filePath, e);
            return LoadResult.ReadFromFileFailed;
        }

        return LoadResult.Success;
    }

    public final void unloadMap(VMapManager vm) {

        for (var pair : loadedSpawns) {
            for (int refCount = 0; refCount < pair.key; ++refCount) {
                vm.releaseModelInstance(treeValues[pair.key].name);
            }

            treeValues[pair.key].setUnloaded();
        }

        loadedSpawns.clear();
        loadedTiles.clear();


    }

    public final LoadResult loadMapTile(int tileX, int tileY, VMapManager vm) {


        if (treeValues == null) {
            Logs.MISC.error("StaticMapTree::LoadMapTile() : tree has not been initialized [{}, {}]", tileX, tileY);

            return LoadResult.ReadFromFileFailed;
        }

        var result = LoadResult.FileNotFound;

        var pair = openMapTileFile(vm, mapId, tileX, tileY);
        if (Files.exists(pair.first())) {
            result = LoadResult.Success;
            try (FileChannel fileChannel = FileChannel.open(pair.first(), StandardOpenOption.READ)) {

                ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
                fileChannel.read(buffer);
                buffer.flip();
                byte[] stringBytes = new byte[8];
                buffer.get(stringBytes);

                if (!Objects.equals(new String(stringBytes, StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {
                    result = LoadResult.VersionMismatch;
                }

                if (result == LoadResult.Success) {
                    var numSpawns = buffer.getInt();

                    for (int i = 0; i < numSpawns && result == LoadResult.Success; ++i) {
                        // read model spawns
                        ModelSpawn spawn = new ModelSpawn();
                        int usedMapId = pair.second();
                        if (ModelSpawn.readFromFile(buffer, spawn)) {
                            // acquire model instance
                            WorldModel model = vm.acquireModelInstance(spawn.name, spawn.flags);

                            if (model == null) {
                                Logs.MISC.error("StaticMapTree::LoadMapTile() : could not acquire WorldModel pointer [{}, {}]", tileX, tileY);

                            }

                            // update tree
                            int spawnIndex = spawnIndices.get(spawn.id, -1);
                            if (spawnIndex != -1) {
                                int loadCount = loadedSpawns.get(spawnIndex, -1);
                                if (loadCount != -1) {
                                    if (spawnIndex >= nTreeValues) {

                                        Logs.MAPS.error("StaticMapTree::LoadMapTile() : invalid tree element ({}/{}) referenced in tile {}", spawnIndex, nTreeValues, pair.first());

                                        continue;
                                    }

                                    treeValues[spawnIndex] = new ModelInstance(spawn, model);

                                    loadedSpawns.put(spawnIndex, 1);
                                } else {
                                    loadedSpawns.put(spawnIndex, ++loadCount);
                                }
                            } else if (mapId == usedMapId) {
                                // unknown parent spawn might appear in because it overlaps multiple tiles
                                // in case the original tile is swapped but its neighbour is now (adding this spawn)
                                // we want to not mark it as loading error and just skip that model
                                Logs.MAPS.error("StaticMapTree::LoadMapTile() : invalid tree element (spawn {}) referenced in tile {} by map {}", spawn.id, pair.first(), usedMapId);
                                result = LoadResult.ReadFromFileFailed;
                            }

                        } else {
                            Logs.MAPS.error("StaticMapTree::LoadMapTile() : cannot read model from file (spawn index {}) referenced in tile {} by map {}", i, pair.first(), usedMapId);
                            result = LoadResult.ReadFromFileFailed;
                        }
                    }
                }

                loadedTiles.set(packTileID(tileX, tileY), true);
            } catch (IOException e) {
                Logs.MAPS.error("StaticMapTree::LoadMapTile() :  initializing StaticMapTree '{}' error.", pair.first(), e);
                result = LoadResult.ReadFromFileFailed;
            }
        } else {
            loadedTiles.set(packTileID(tileX, tileY), false);
        }

        return result;


    }

    public final void unloadMapTile(int tileX, int tileY, VMapManager vm) {

        var tileID = packTileID(tileX, tileY);

        if (!loadedTiles.get(tileID)) {
            Logs.MISC.error("StaticMapTree::UnloadMapTile() : trying to unload non-loaded tile - Map:{} X:{} Y:{}", mapId, tileX, tileY);
            return;

        } else {
            var fileResult = openMapTileFile(vm, mapId, tileX, tileY);
            Path titleFile = fileResult.first();
            if (Files.exists(titleFile)) {
                try (FileChannel fileChannel = FileChannel.open(titleFile, StandardOpenOption.READ)) {
                    var result = true;
                    ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
                    fileChannel.read(buffer);
                    buffer.flip();
                    byte[] stringBytes = new byte[8];
                    buffer.get(stringBytes);

                    if (!Objects.equals(new String(stringBytes, StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {
                        result = false;
                    }

                    var numSpawns = buffer.getInt();

                    for (int i = 0; i < numSpawns && result; ++i) {
                        // read model spawns
                        ModelSpawn spawn = new ModelSpawn();
                        result = ModelSpawn.readFromFile(buffer, spawn);

                        if (result) {
                            // release model instance
                            vm.releaseModelInstance(spawn.name);

                            // update tree

                            int referencedNode = spawnIndices.get(spawn.id, -1);

                            if (referencedNode != -1) {
                                int loaded = loadedSpawns.get(referencedNode, -1);


                                if (loaded == -1) {

                                    Logs.MISC.error("StaticMapTree::UnloadMapTile() : trying to unload non-referenced model '{}' (ID:{})", spawn.name, spawn.id);

                                } else if (--loaded == 0) {
                                    treeValues[referencedNode].setUnloaded();
                                    loadedSpawns.remove(referencedNode, -1);
                                }
                            } else if (mapId == fileResult.second()) {
                                // logic documented in StaticMapTree::LoadMapTile
                                result = false;
                            }
                        }
                    }
                } catch (IOException e) {
                    Logs.MAPS.error("StaticMapTree::UnloadMapTile() :  uninitializing StaticMapTree '{}' error.", fileResult.first(), e);
                }
            }
        }

        loadedTiles.set(tileID, false);


    }

    public final boolean getAreaInfo(Vector3 outPos, AreaInfo outAreaInfo) {
        tree.intersectPoint(outPos, (point, entry) -> {
            if (treeValues[entry] == null) {
                return;
            }
            treeValues[entry].intersectPoint(point, outAreaInfo);
            if (outAreaInfo.result) {
                outPos.z = outAreaInfo.floorZ;
            }
        });
        return outAreaInfo.result;
    }

    public final boolean getLocationInfo(Vector3 pos, LocationInfo info) {
        tree.intersectPoint(pos, (point, entry) -> {
            if (treeValues[entry] != null) {
                treeValues[entry].getLocationInfo(point, info);
            }
        });

        return info.result;
    }

    public float getHeight(Vector3 pPos, float maxSearchDist) {
        var height = Float.POSITIVE_INFINITY;
        Vector3 dir = new Vector3(0, 0, -1);
        // direction with length of 1
        Ray ray = new Ray(pPos, dir);

        Distance maxDist = new Distance(maxSearchDist);
        if (getIntersectionTime(ray, maxDist, false, ModelIgnoreFlags.Nothing)) {
            height = pPos.z - maxDist.distance;
        }
        return height;
    }

    public final boolean getObjectHitPos(Vector3 pPos1, Vector3 pPos2, Vector3 pResultHitPos, float pModifyDist) {
        boolean result;
        var maxDist = (pPos2.sub(pPos1)).len();

        // prevent NaN values which can cause BIH intersection to enter infinite loop
        if (maxDist < 1e-10f) {
            pResultHitPos.set(pPos2);

            return false;
        }

        var dir = pPos2.sub(pPos1).scl(1.0f / maxDist); // direction with length of 1
        Ray ray = new Ray(pPos1, dir);

        Distance distance = new Distance(maxDist);
        if (getIntersectionTime(ray, distance, false, ModelIgnoreFlags.Nothing)) {


            pResultHitPos.set(pPos1.add(dir.scl(distance.distance)));

            if (pModifyDist < 0) {
                if (pResultHitPos.sub(pPos1).len() > -pModifyDist) {
                    pResultHitPos.set(pResultHitPos.add(dir.scl(pModifyDist)));
                } else {
                    pResultHitPos.set(pPos1);
                }
            } else {
                pResultHitPos.set(pResultHitPos.add(dir.scl(pModifyDist)));
            }

            result = true;
        } else {
            pResultHitPos.set(pPos1);
            result = false;
        }

        return result;
    }

    public final boolean isInLineOfSight(Vector3 pos1, Vector3 pos2, ModelIgnoreFlags ignoreFlags) {
        var maxDist = pos2.sub(pos1).len();

        // return false if distance is over max float, in case of cheater teleporting to the end of the universe
        if (maxDist == Float.MAX_VALUE || maxDist == Float.POSITIVE_INFINITY) {
            return false;
        }

        // prevent NaN values which can cause BIH intersection to enter infinite loop
        if (maxDist < 1e-10f) {
            return true;
        }

        // direction with length of 1
        Ray ray = new Ray(pos1, (pos2.sub(pos1).scl(1f / maxDist)));

        Distance distance = new Distance(maxDist);

        return !getIntersectionTime(ray, distance, true, ignoreFlags);
    }

    public final int numLoadedTiles() {
        return loadedTiles.size();
    }

    private boolean getIntersectionTime(Ray pRay, Distance pMaxDist, boolean pStopAtFirstHit, ModelIgnoreFlags ignoreFlags) {

        tree.intersectRay(pRay, pMaxDist, pStopAtFirstHit, ((ray, entry, distance, pStopAtFirstHit1) -> treeValues[entry].intersectRay(ray, distance, pStopAtFirstHit, ignoreFlags)));

        return pMaxDist.hit;
    }
}
