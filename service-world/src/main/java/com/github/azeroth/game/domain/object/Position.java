package com.github.azeroth.game.domain.object;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.utils.MathUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Each map (instance, transport, continent) has its own cartesian coordinate system.
 * To place a new entity it is easiest to position yourself where it should be spawned and use the .gps command.
 * This position information is expressed through the following fields per database table. Some fields may be omitted.
 * (e.g. z and o are omitted for Quest PoI as they are shapes on a 2D map)
 * <p>
 * Field	Description            	Comment
 * x   	Vertical Axis         	S <= 0 <= N
 * y   	Horizontal Axis     	E <= 0 <= W
 * z   	Height	                below x/y plane <= 0 <= above x/y plane
 * o   	Orientation	            0 <= o <= 2π *
 * <p>
 * The positive X-axis points north, the positive Y-axis points west.
 * The Z-axis is vertical height, with 0 being sea level.
 * The origin of the coordinate system is in the center of the map.
 * The top-left corner of the map has X = 17066, Y = 17066
 * The bottom-right corner of the map has X = -17066, Y = -17066
 * The bottom-left corner of the map has X = -17006, Y = 17066
 * The top-right corner of the map has X = 17006, Y = -17066
 * Just to be absolutely clear, assuming you playing a character that is not flying or swimming and is facing north:
 * <p>
 * Forward = Vector3(1, 0, 0)
 * Right = Vector3(0, -1, 0)
 * Up = Vector3(0, 0, 1);
 * <p>
 * All maps are divided into 64x64 blocks for a total of 4096 (some of which may be unused).
 * Each block are divided into 16x16 chunks. each block will always use all of its 16x16 chunks.
 * <p>
 * Map size:
 * Each block is 533.33333 yards (1600 feet) in width and height. The map is divided into 64x64 blocks so the total
 * width and height of the map will be 34133.33312 yards, however the origin of the coordinate system is at
 * the center of the map so the minimum and maximum X and Y coordinates will be ±17066.66656).
 * Since each block has 16x16 chunks, the size of a chunk will be 33.3333 yards (100 feet).
 * <p>
 * Player's speed
 * Basic running speed of a player (without any speed modifying effects) is 7.1111 yards/s (21.3333 feet/s).
 * Player is able to reach one border of an ADT tile from another in 75 seconds. Thus,
 * the fastest mounts (310%) can get over ADT size in 24.2 seconds.
 */
@Getter
@NoArgsConstructor
public class Position {

    @Setter
    private float x;
    @Setter
    private float y;
    @Setter
    private float z;
    private float o;


    public Position(Position position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.o = position.o;
    }

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(float x, float y, float z, float o) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.o = normalizeOrientation(o);
    }

    public static float normalizeOrientation(float o) {
        // fmod only supports positive numbers. Thus we have
        // to emulate negative numbers
        if (o < 0) {
            float mod = o * -1;
            mod %= 2.0f * MathUtil.PI;
            mod = -mod + 2.0f * MathUtil.PI;
            return mod;
        }
        return o % 2.0f * MathUtil.PI;
    }

    public void relocate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void relocate(float x, float y, float z) {
        relocate(x, y);
        this.z = z;
    }

    public void relocate(float x, float y, float z, float o) {
        relocate(x, y, z);
        setO(o);
    }

    public void setO(float o) {
        this.o = normalizeOrientation(o);
    }

    public void relocate(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.o = pos.o;
    }

    public void relocateOffset(Position offset) {
        x = getX() + (float) (offset.getX() * Math.cos(getO()) + offset.getY() * Math.sin(getO() + Math.PI));
        y = getY() + (float) (offset.getY() * Math.cos(getO()) + offset.getX() * Math.sin(getO()));
        z = getZ() + offset.getZ();
        setO(getO() + offset.getO());
    }

    public boolean isPositionValid() {
        return MapDefine.isValidMapCoordinate(x, y, z, o);
    }

    public Position offsetPosition(Position endPos) {
        Position retOffset = new Position();
        float dx = endPos.getX() - getX();
        float dy = endPos.getY() - getY();
        retOffset.x = dx * (float) Math.cos(getO()) + dy * (float) Math.sin(getO());
        retOffset.y = dy * (float) Math.cos(getO()) - dx * (float) Math.sin(getO());
        retOffset.z = endPos.getZ() - getZ();
        retOffset.setO(endPos.getO() - getO());
        return retOffset;
    }

    public boolean isWithinBox(Position center, float xRadius, float yRadius, float zRadius) {
        // rotate the WorldObject position instead of rotating the whole cube, that way we can make a simplified
        // is-in-cube check and we have to calculate only one point instead of 4

        // 2PI = 360*, keep in mind that inGame orientation is counter-clockwise
        double rotation = 2 * Math.PI - center.getO();
        double sinVal = Math.sin(rotation);
        double cosVal = Math.cos(rotation);

        float BoxDistX = getX() - center.getX();
        float BoxDistY = getY() - center.getY();

        float rotX = (float) (center.getX() + BoxDistX * cosVal - BoxDistY * sinVal);
        float rotY = (float) (center.getY() + BoxDistY * cosVal + BoxDistX * sinVal);

        // box edges are parallel to coordiante axis, so we can treat every dimension independently :D
        float dz = getZ() - center.getZ();
        float dx = rotX - center.getX();
        float dy = rotY - center.getY();
        return (!(Math.abs(dx) > xRadius)) &&
                (!(Math.abs(dy) > yRadius)) &&
                (!(Math.abs(dz) > zRadius));
    }

    public boolean isWithinDoubleVerticalCylinder(Position center, float radius, float height) {
        float verticalDelta = getZ() - center.getZ();
        return isInDist2D(center, radius) && Math.abs(verticalDelta) <= height;
    }

    public boolean hasInArc(float arc, Position obj) {
        return hasInArc(arc, obj, 2.0f);
    }

    public boolean hasInArc(float arc, Position obj, float border) {
        // always have self in arc
        if (obj == this)
            return true;

        // move arc to range 0.. 2*pi
        arc = normalizeOrientation(arc);

        // move angle to range -pi ... +pi
        float angle = getRelativeAngle(obj);
        if (angle > MathUtil.PI)
            angle -= 2.0f * MathUtil.PI;

        float lborder = -1 * (arc / border);                        // in range -pi..0
        float rborder = (arc / border);                             // in range 0..pi
        return ((angle >= lborder) && (angle <= rborder));
    }

    public boolean hasInLine(Position pos, float objSize, float width) {
        if (!hasInArc(MathUtil.PI, pos, 2.0f))
            return false;

        width += objSize;
        float angle = getRelativeAngle(pos);
        return Math.abs(Math.sin(angle)) * getExactDist2D(pos.getX(), pos.getY()) < width;
    }

    public float getExactDist2D(float x, float y) {
        return (float) Math.sqrt(getExactDist2DSq(x, y));
    }

    public float getExactDist2D(Position pos) {
        return getExactDist2D(pos.x, pos.y);
    }

    public float getExactDist2DSq(float x, float y) {
        float dx = x - this.x;
        float dy = y - this.y;
        return dx * dx + dy * dy;
    }

    public float getExactDist2DSq(Position pos) {
        return getExactDist2DSq(pos.x, pos.y);
    }

    public float getExactDistSq(float x, float y, float z) {
        float dz = z - this.z;
        return getExactDist2DSq(x, y) + dz * dz;
    }

    public float getExactDistSq(Position pos) {
        return getExactDistSq(pos.x, pos.y, pos.z);
    }

    public float getExactDist(float x, float y, float z) {
        return (float) Math.sqrt(getExactDistSq(x, y, z));
    }

    public float getExactDist(Position pos) {
        return getExactDist(pos.x, pos.y, pos.z);
    }

    public float getAbsoluteAngle(float x, float y) {
        float dx = x - this.x;
        float dy = y - this.y;
        return normalizeOrientation((float) Math.atan2(dy, dx));
    }

    public float getAbsoluteAngle(Position pos) {
        return getAbsoluteAngle(pos.x, pos.y);
    }

    public float toAbsoluteAngle(float relAngle) {
        return normalizeOrientation(relAngle + o);
    }

    public float toRelativeAngle(float absAngle) {
        return normalizeOrientation(absAngle - o);
    }

    public float getRelativeAngle(float x, float y) {
        return toRelativeAngle(getAbsoluteAngle(x, y));
    }

    public float getRelativeAngle(Position pos) {
        return toRelativeAngle(getAbsoluteAngle(pos));
    }

    public boolean isInDist2D(float x, float y, float dist) {
        return getExactDist2DSq(x, y) < dist * dist;
    }

    public boolean isInDist2D(Position pos, float dist) {
        return getExactDist2DSq(pos) < dist * dist;
    }

    public boolean isInDist(float x, float y, float z, float dist) {
        return getExactDistSq(x, y, z) < dist * dist;
    }

    public boolean isInDist(Position pos, float dist) {
        return getExactDistSq(pos) < dist * dist;
    }

    public boolean isDefaultValue() {
        return x == 0.0f && y == 0.0f && z == 0.0f && o == 0.0f;
    }

    public final Vector3 toVector3() {
        return new Vector3(getX(), getY(), getZ());
    }

    public final Vector4 toVector4() {
        return new Vector4(getX(), getY(), getZ(), getO());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof Position p) {
            return (MathUtil.fuzzyEq(x, p.x) &&
                    MathUtil.fuzzyEq(y, p.y) &&
                    MathUtil.fuzzyEq(z, p.z) &&
                    MathUtil.fuzzyEq(o, p.o));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, o);
    }

    public String toString() {
        return "Position{X: %f Y: %f Z: %f O: %f}".formatted(x, y, z, o);
    }


    public static Position of(float x, float y, float z, float o) {
        return new Position(x, y, z, o);
    }

    public static Position of(float x, float y, float z) {
        return new Position(x, y, z);
    }

    public static Position of(Vector3 vec) {
        return new Position(vec.x, vec.y, vec.z);
    }

    public static Position of(Position pos) {
        return new Position(pos);
    }
}
