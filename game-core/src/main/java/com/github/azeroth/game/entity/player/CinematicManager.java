package com.github.azeroth.game.entity.player;


import com.github.azeroth.game.entity.creature.TempSummon;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;


public class CinematicManager implements Closeable {
    // Remote location information
    private final Player player;
    private final Position remoteSightPosition;
    private TempSummon cinematicObject;
    private int cinematicDiff;
    private int lastCinematicCheck;
    private CinematicSequencesRecord activeCinematic;
    private int activeCinematicCameraIndex;
    private int cinematicLength;
    private ArrayList<FlyByCamera> cinematicCamera;

    public CinematicManager(Player playerref) {
        player = playerref;
        setActiveCinematicCameraIndex(-1);
        remoteSightPosition = new Position(0.0f, 0.0f, 0.0f);
    }

    public final int getCinematicDiff() {
        return cinematicDiff;
    }

    public final void setCinematicDiff(int value) {
        cinematicDiff = value;
    }

    public final int getLastCinematicCheck() {
        return lastCinematicCheck;
    }

    public final void setLastCinematicCheck(int value) {
        lastCinematicCheck = value;
    }

    public final CinematicSequencesRecord getActiveCinematic() {
        return activeCinematic;
    }

    public final void setActiveCinematic(CinematicSequencesRecord value) {
        activeCinematic = value;
    }

    public final int getActiveCinematicCameraIndex() {
        return activeCinematicCameraIndex;
    }

    public final void setActiveCinematicCameraIndex(int value) {
        activeCinematicCameraIndex = value;
    }

    public final int getCinematicLength() {
        return cinematicLength;
    }

    public final void setCinematicLength(int value) {
        cinematicLength = value;
    }

    public final ArrayList<FlyByCamera> getCinematicCamera() {
        return cinematicCamera;
    }

    public final void setCinematicCamera(ArrayList<FlyByCamera> value) {
        cinematicCamera = value;
    }

    public void close() throws IOException {
        if (getCinematicCamera() != null && getActiveCinematic() != null) {
            endCinematic();
        }
    }

    public final void beginCinematic(CinematicSequencesRecord cinematic) {
        setActiveCinematic(cinematic);
        setActiveCinematicCameraIndex(-1);
    }

    public final void nextCinematicCamera() {
        // Sanity check for active camera set
        if (getActiveCinematic() == null || getActiveCinematicCameraIndex() >= getActiveCinematic().camera.length) {
            return;
        }

        setActiveCinematicCameraIndex(getActiveCinematicCameraIndex() + 1);
        int cinematicCameraId = getActiveCinematic().Camera[getActiveCinematicCameraIndex()];

        if (cinematicCameraId == 0) {
            return;
        }

        var flyByCameras = M2Storage.GetFlyByCameras(cinematicCameraId);

        if (!flyByCameras.isEmpty()) {
            // Initialize diff, and set camera
            setCinematicDiff(0);
            setCinematicCamera(flyByCameras);

            if (!getCinematicCamera().isEmpty()) {
                var firstCamera = getCinematicCamera().FirstOrDefault();
                Position pos = new Position(firstCamera.locations.X, firstCamera.locations.Y, firstCamera.locations.Z, firstCamera.locations.W);

                if (!pos.isPositionValid()) {
                    return;
                }

                player.getMap().loadGridForActiveObject(pos.getX(), pos.getY(), player);
                cinematicObject = player.summonCreature(1, pos, TempSummonType.TimedDespawn, durationofMinutes(5));

                if (cinematicObject) {
                    cinematicObject.setActive(true);
                    player.setViewpoint(cinematicObject, true);
                }

                // Get cinematic length
                setCinematicLength(getCinematicCamera().LastOrDefault().timeStamp);
            }
        }
    }

    public final void endCinematic() {
        if (getActiveCinematic() == null) {
            return;
        }

        setCinematicDiff(0);
        setCinematicCamera(null);
        setActiveCinematic(null);
        setActiveCinematicCameraIndex(-1);

        if (cinematicObject) {
            var vpObject = player.getViewpoint();

            if (vpObject) {
                if (vpObject == cinematicObject) {
                    player.setViewpoint(cinematicObject, false);
                }
            }

            cinematicObject.addObjectToRemoveList();
        }
    }

    public final void updateCinematicLocation(int diff) {
        if (getActiveCinematic() == null || getActiveCinematicCameraIndex() == -1 || getCinematicCamera() == null || getCinematicCamera().isEmpty()) {
            return;
        }

        Position lastPosition = new Position();
        int lastTimestamp = 0;
        Position nextPosition = new Position();
        int nextTimestamp = 0;

        // Obtain direction of travel
        for (var cam : getCinematicCamera()) {
            if (cam.timeStamp > getCinematicDiff()) {
                nextPosition = new Position(cam.locations.X, cam.locations.Y, cam.locations.Z, cam.locations.W);
                nextTimestamp = cam.timeStamp;

                break;
            }

            lastPosition = new Position(cam.locations.X, cam.locations.Y, cam.locations.Z, cam.locations.W);
            lastTimestamp = cam.timeStamp;
        }

        var angle = lastPosition.getAbsoluteAngle(nextPosition);
        angle -= lastPosition.getO();

        if (angle < 0) {
            angle += 2 * MathUtil.PI;
        }

        // Look for position around 2 second ahead of us.
        var workDiff = (int) getCinematicDiff();

        // Modify result based on camera direction (Humans for example, have the camera point behind)
        workDiff += (int) ((2 * time.InMilliseconds) * Math.cos(angle));

        // Get an iterator to the last entry in the cameras, to make sure we don't go beyond the end
        var endItr = getCinematicCamera().LastOrDefault();

        if (endItr != null && workDiff > endItr.timeStamp) {
            workDiff = (int) endItr.timeStamp;
        }

        // Never try to go back in time before the start of cinematic!
        if (workDiff < 0) {
            workDiff = (int) getCinematicDiff();
        }

        // Obtain the previous and next waypoint based on timestamp
        for (var cam : getCinematicCamera()) {
            if (cam.timeStamp >= workDiff) {
                nextPosition = new Position(cam.locations.X, cam.locations.Y, cam.locations.Z, cam.locations.W);
                nextTimestamp = cam.timeStamp;

                break;
            }

            lastPosition = new Position(cam.locations.X, cam.locations.Y, cam.locations.Z, cam.locations.W);
            lastTimestamp = cam.timeStamp;
        }

        // Never try to go beyond the end of the cinematic
        if (workDiff > nextTimestamp) {
            workDiff = (int) nextTimestamp;
        }

        // Interpolate the position for this moment in time (or the adjusted moment in time)
        var timeDiff = nextTimestamp - lastTimestamp;
        var interDiff = (int) (workDiff - lastTimestamp);
        var xDiff = nextPosition.getX() - lastPosition.getX();
        var yDiff = nextPosition.getY() - lastPosition.getY();
        var zDiff = nextPosition.getZ() - lastPosition.getZ();

        Position interPosition = new Position(lastPosition.getX() + (xDiff * ((float) interDiff / timeDiff)), lastPosition.getY() + (yDiff * ((float) interDiff / timeDiff)), lastPosition.getZ() + (zDiff * ((float) interDiff / timeDiff)));

        // Advance (at speed) to this position. The remote sight object is used
        // to send update information to player in cinematic
        if (cinematicObject && interPosition.isPositionValid()) {
            cinematicObject.monsterMoveWithSpeed(interPosition.getX(), interPosition.getY(), interPosition.getZ(), 500.0f, false, true);
        }

        // If we never received an end packet 10 seconds after the final timestamp then force an end
        if (getCinematicDiff() > getCinematicLength() + 10 * time.InMilliseconds) {
            endCinematic();
        }
    }

    public final boolean isOnCinematic() {
        return getCinematicCamera() != null;
    }
}
