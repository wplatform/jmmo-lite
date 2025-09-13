package com.github.azeroth.game.map.model;


import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.map.Distance;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.model.ModelFlags;
import com.github.azeroth.game.map.collision.BIH;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.AreaInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;


@Getter
@EqualsAndHashCode
public class WorldModel {
    private final ArrayList<GroupModel> groupModels = new ArrayList<>();
    private final BIH groupTree = new BIH();
    @Setter
    public int flags;
    private int rootWmoid;
    @Setter
    private String name;
    private int refCount;


    public final void incRefCount() {
        ++refCount;
    }

    public final int decRefCount() {
        return --refCount;
    }


    public boolean intersectRay(Ray ray, Distance distance, boolean stopAtFirstHit, ModelIgnoreFlags ignoreFlags) {

        if ((ignoreFlags.ordinal() & ModelIgnoreFlags.M2.ordinal()) != ModelIgnoreFlags.Nothing.ordinal()) {
            if ((flags & ModelFlags.M2.ordinal()) != 0) {
                return false;
            }
        }

        // small M2 workaround, maybe better make separate class with virtual intersection funcs
        // in any case, there's no need to use a bound tree if we only have one submodel
        if (groupModels.size() == 1) {
            return groupModels.getFirst().intersectRay(ray, distance, stopAtFirstHit);
        }

        groupTree.intersectRay(ray, distance, stopAtFirstHit, (r, entry, dist, pStopAtFirstHit) -> {
            distance.hit = groupModels.get(entry).intersectRay(r, dist, pStopAtFirstHit);
            return distance.hit;
        });

        return distance.hit;

    }

    public final boolean intersectPoint(Vector3 p, Vector3 down, Distance distance, AreaInfo info) {
        distance.distance = 0f;

        if (groupModels.isEmpty()) {
            return false;
        }

        groupTree.intersectPoint(p, (point, entry) -> {
            Distance z = new Distance();
            GroupModel model = groupModels.get(entry);
            if (model.isInsideObject(point, down, z)) {
                if (z.distance < Float.POSITIVE_INFINITY) {
                    distance.distance = z.distance;
                    distance.hit = true;
                    info.rootId = rootWmoid;
                    info.groupId = model.getIGroupWmoid();
                    info.flags = model.getIMogpFlags();
                    info.result = true;
                }
            }
        });
        return distance.hit;
    }

    public final boolean getLocationInfo(Vector3 p, Vector3 down, Distance dist, GroupLocationInfo info) {
        dist.distance = 0f;

        if (groupModels.isEmpty()) {
            return false;
        }

        groupTree.intersectPoint(p, ((point, entry) -> {
            Distance z = new Distance();
            GroupModel model = groupModels.get(entry);
            if (model.isInsideObject(point, down, z)) {
                if (z.distance < Float.POSITIVE_INFINITY) {
                    info.rootId = rootWmoid;
                    info.hitModel = model;
                    dist.distance = z.distance;
                    dist.hit = true;
                }
            }
        }));


        return dist.hit;
    }


    public final boolean readFile(Path filePath) {
        if (!Files.exists(filePath)) {
            filePath = filePath.getParent().resolve(filePath.getFileName() + ".vmo");

            if (!Files.exists(filePath)) {
                return false;
            }
        }

        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip();
            byte[] stringBytes = new byte[8];
            buffer.get(stringBytes);

            if (!Objects.equals(new String(stringBytes, StandardCharsets.UTF_8), MapDefine.VMAP_MAGIC)) {
                return false;
            }

            stringBytes = new byte[4];
            buffer.get(stringBytes);
            if (!"WMOD".equals(new String(stringBytes, StandardCharsets.UTF_8))) {
                return false;
            }

            buffer.getInt(); //chunkSize notused
            rootWmoid = buffer.getInt();

            // read group models
            stringBytes = new byte[4];
            buffer.get(stringBytes);
            if (!"GMOD".equals(new String(stringBytes, StandardCharsets.UTF_8))) {
                return false;
            }

            var count = buffer.getInt();

            for (var i = 0; i < count; ++i) {
                GroupModel group = new GroupModel();
                group.readFromFile(buffer);
                groupModels.add(group);
            }

            // read group BIH
            stringBytes = new byte[4];
            buffer.get(stringBytes);
            if (!"GBIH".equals(new String(stringBytes, StandardCharsets.UTF_8))) {
                return false;
            }

            return groupTree.readFromFile(buffer);
        } catch (IOException e) {
            Logs.MISC.error("Load '{}' failed.", filePath, e);
            return false;
        }
    }
}
