package com.github.azeroth.game.entity.transport;


import com.badlogic.gdx.utils.IntArray;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.vehicle.ITransport;
import com.github.azeroth.game.domain.transport.TransportAnimation;

import java.time.Duration;
import java.util.ArrayList;

public class Transport extends WorldObject implements ITransport {
    private static final Duration POSITIONUPDATEINTERVAL = duration.ofSeconds(50);
    private final TransportAnimation animationInfo;

    private final IntArray stopFrames = new IntArray(9);
    private final TimeTracker positionUpdateTimer = new TimeTracker();
    private final ArrayList<WorldObject> passengers = new ArrayList<>();

    private int pathProgress;

    private int stateChangeTime;

    private int stateChangeProgress;
    private boolean autoCycleBetweenStopFrames;

    public Transport(GameObject owner) {
        super(owner);
        animationInfo = global.getTransportMgr().getTransportAnimInfo(owner.getTemplate().entry);
        pathProgress = gameTime.GetGameTimeMS() % getTransportPeriod();
        stateChangeTime = gameTime.GetGameTimeMS();
        stateChangeProgress = pathProgress;

        var goInfo = owner.getTemplate();

        if (goInfo.transport.timeto2ndfloor > 0) {
            stopFrames.add(goInfo.transport.timeto2ndfloor);

            if (goInfo.transport.timeto3rdfloor > 0) {
                stopFrames.add(goInfo.transport.timeto3rdfloor);

                if (goInfo.transport.timeto4thfloor > 0) {
                    stopFrames.add(goInfo.transport.timeto4thfloor);

                    if (goInfo.transport.timeto5thfloor > 0) {
                        stopFrames.add(goInfo.transport.timeto5thfloor);

                        if (goInfo.transport.timeto6thfloor > 0) {
                            stopFrames.add(goInfo.transport.timeto6thfloor);

                            if (goInfo.transport.timeto7thfloor > 0) {
                                stopFrames.add(goInfo.transport.timeto7thfloor);

                                if (goInfo.transport.timeto8thfloor > 0) {
                                    stopFrames.add(goInfo.transport.timeto8thfloor);

                                    if (goInfo.transport.timeto9thfloor > 0) {
                                        stopFrames.add(goInfo.transport.timeto9thfloor);

                                        if (goInfo.transport.timeto10thfloor > 0) {
                                            stopFrames.add(goInfo.transport.timeto10thfloor);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!stopFrames.isEmpty()) {
            pathProgress = 0;
            stateChangeProgress = 0;
        }

        positionUpdateTimer.reset(POSITIONUPDATEINTERVAL);
    }

    public final ObjectGuid getTransportGUID() {
        return owner.getGUID();
    }

    public final float getTransportOrientation() {
        return owner.getLocation().getO();
    }

    public final void addPassenger(WorldObject passenger) {
        if (!owner.isInWorld()) {
            return;
        }

        if (!passengers.contains(passenger)) {
            passengers.add(passenger);
            passenger.setTransport(this);
            passenger.getMovementInfo().transport.guid = getTransportGUID();
            Log.outDebug(LogFilter.transport, String.format("Object %1$s boarded transport %2$s.", passenger.getName(), owner.getName()));
        }
    }

    public final ITransport removePassenger(WorldObject passenger) {
        if (passengers.remove(passenger)) {
            passenger.setTransport(null);
            passenger.getMovementInfo().transport.reset();
            Log.outDebug(LogFilter.transport, String.format("Object %1$s removed from transport %2$s.", passenger.getName(), owner.getName()));

            var plr = passenger.toPlayer();

            if (plr != null) {
                plr.setFallInformation(0, plr.getLocation().getZ());
            }
        }

        return this;
    }

    public final void calculatePassengerPosition(Position pos) {
        itransport.calculatePassengerPosition(pos, owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(), owner.getLocation().getO());
    }

    public final void calculatePassengerOffset(Position pos) {
        itransport.calculatePassengerOffset(pos, owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(), owner.getLocation().getO());
    }

    public final int getMapIdForSpawning() {
        return owner.getTemplate().transport.spawnMap;
    }


    @Override
    public void update(int diff) {
        if (animationInfo == null) {
            return;
        }

        positionUpdateTimer.update(diff);

        if (!positionUpdateTimer.Passed) {
            return;
        }

        positionUpdateTimer.reset(POSITIONUPDATEINTERVAL);

        var now = gameTime.GetGameTimeMS();
        var period = getTransportPeriod();
        int newProgress = 0;

        if (stopFrames.isEmpty()) {
            newProgress = now % period;
        } else {
            var stopTargetTime = 0;

            if (owner.getGoState() == GOState.TransportActive) {
                stopTargetTime = 0;
            } else {
                stopTargetTime = stopFrames.get(owner.getGoState() - GOState.TransportStopped);
            }

            if (now < owner.getGameObjectFieldData().level) {
                var timeToStop = owner.getGameObjectFieldData().Level - stateChangeTime;
                var stopSourcePathPct = (float) _stateChangeProgress / (float) period;
                var stopTargetPathPct = (float) stopTargetTime / (float) period;
                var timeSinceStopProgressPct = (float) (now - stateChangeTime) / (float) timeToStop;

                float progressPct;

                if (!owner.hasDynamicFlag(GameObjectDynamicLowFlags.InvertedMovement)) {
                    if (owner.getGoState() == GOState.TransportActive) {
                        stopTargetPathPct = 1.0f;
                    }

                    var pathPctBetweenStops = stopTargetPathPct - stopSourcePathPct;

                    if (pathPctBetweenStops < 0.0f) {
                        pathPctBetweenStops += 1.0f;
                    }

                    progressPct = pathPctBetweenStops * timeSinceStopProgressPct + stopSourcePathPct;

                    if (progressPct > 1.0f) {
                        progressPct = progressPct - 1.0f;
                    }
                } else {
                    var pathPctBetweenStops = stopSourcePathPct - stopTargetPathPct;

                    if (pathPctBetweenStops < 0.0f) {
                        pathPctBetweenStops += 1.0f;
                    }

                    progressPct = stopSourcePathPct - pathPctBetweenStops * timeSinceStopProgressPct;

                    if (progressPct < 0.0f) {
                        progressPct += 1.0f;
                    }
                }

                newProgress = (int) ((float) period * progressPct) % period;
            } else {
                newProgress = stopTargetTime;
            }

            if (newProgress == stopTargetTime && newProgress != pathProgress) {
                int eventId;

                switch (owner.getGoState() - GOState.TransportActive) {
                    case 0:
                        eventId = owner.getTemplate().transport.reached1stfloor;

                        break;
                    case 1:
                        eventId = owner.getTemplate().transport.reached2ndfloor;

                        break;
                    case 2:
                        eventId = owner.getTemplate().transport.reached3rdfloor;

                        break;
                    case 3:
                        eventId = owner.getTemplate().transport.reached4thfloor;

                        break;
                    case 4:
                        eventId = owner.getTemplate().transport.reached5thfloor;

                        break;
                    case 5:
                        eventId = owner.getTemplate().transport.reached6thfloor;

                        break;
                    case 6:
                        eventId = owner.getTemplate().transport.reached7thfloor;

                        break;
                    case 7:
                        eventId = owner.getTemplate().transport.reached8thfloor;

                        break;
                    case 8:
                        eventId = owner.getTemplate().transport.reached9thfloor;

                        break;
                    case 9:
                        eventId = owner.getTemplate().transport.reached10thfloor;

                        break;
                    default:
                        eventId = 0;

                        break;
                }

                if (eventId != 0) {
                    GameEvents.trigger(eventId, owner, null);
                }

                if (autoCycleBetweenStopFrames) {
                    var currentState = owner.getGoState();
                    GOState newState;

                    if (currentState == GOState.TransportActive) {
                        newState = GOState.TransportStopped;
                    } else if (currentState - GOState.TransportActive == stopFrames.size()) {
                        newState = currentState - 1;
                    } else if (owner.hasDynamicFlag(GameObjectDynamicLowFlags.InvertedMovement)) {
                        newState = currentState - 1;
                    } else {
                        newState = currentState + 1;
                    }

                    owner.setGoState(newState);
                }
            }
        }

        if (pathProgress == newProgress) {
            return;
        }

        pathProgress = newProgress;

        var oldAnimation = animationInfo.getPrevAnimNode(newProgress);
        var newAnimation = animationInfo.getNextAnimNode(newProgress);

        if (oldAnimation != null && newAnimation != null) {
            var pathRotation = (new Quaternion(owner.getGameObjectFieldData().parentRotation.getValue().X, owner.getGameObjectFieldData().parentRotation.getValue().Y, owner.getGameObjectFieldData().parentRotation.getValue().Z, owner.getGameObjectFieldData().parentRotation.getValue().W)).ToMatrix();

            Vector3 prev = new Vector3(oldAnimation.pos.X, oldAnimation.pos.Y, oldAnimation.pos.Z);
            Vector3 next = new Vector3(newAnimation.pos.X, newAnimation.pos.Y, newAnimation.pos.Z);

            var dst = next;

            if (prev != next) {
                var animProgress = (float) (newProgress - oldAnimation.TimeIndex) / (float) (newAnimation.TimeIndex - oldAnimation.TimeIndex);

                dst = pathRotation.Multiply(Vector3.Lerp(prev, next, animProgress));
            }

            dst = pathRotation.Multiply(dst);
            dst += owner.getStationaryPosition1();

            owner.getMap().gameObjectRelocation(owner, dst.X, dst.Y, dst.Z, owner.getLocation().getO());
        }

        var oldRotation = animationInfo.getPrevAnimRotation(newProgress);
        var newRotation = animationInfo.getNextAnimRotation(newProgress);

        if (oldRotation != null && newRotation != null) {
            Quaternion prev = new Quaternion(oldRotation.Rot[0], oldRotation.Rot[1], oldRotation.Rot[2], oldRotation.Rot[3]);
            Quaternion next = new Quaternion(newRotation.Rot[0], newRotation.Rot[1], newRotation.Rot[2], newRotation.Rot[3]);

            var rotation = next;

            if (prev != next) {
                var animProgress = (float) (newProgress - oldRotation.TimeIndex) / (float) (newRotation.TimeIndex - oldRotation.TimeIndex);

                rotation = Quaternion.Lerp(prev, next, animProgress);
            }

            owner.setLocalRotation(rotation.X, rotation.Y, rotation.Z, rotation.W);
            owner.updateModelPosition();
        }

        // update progress marker for client
        owner.setPathProgressForClient((float) _pathProgress / (float) period);
    }

    @Override
    public void onStateChanged(GOState oldState, GOState newState) {
        if (stopFrames.isEmpty()) {
            if (newState != GOState.TransportActive) {
                owner.setGoState(GOState.TransportActive);
            }

            return;
        }

        int stopPathProgress = 0;

        if (newState != GOState.TransportActive) {
            var stopFrame = (int) (newState - GOState.TransportStopped);
            stopPathProgress = stopFrames.get(stopFrame);
        }

        stateChangeTime = gameTime.GetGameTimeMS();
        stateChangeProgress = pathProgress;
        var timeToStop = Math.abs(_pathProgress - stopPathProgress);
        owner.setLevel(gameTime.GetGameTimeMS() + timeToStop);
        owner.setPathProgressForClient((float) _pathProgress / (float) getTransportPeriod());

        if (oldState == GOState.active || oldState == newState) {
            // initialization
            if (pathProgress > stopPathProgress) {
                owner.setDynamicFlag(GameObjectDynamicLowFlags.InvertedMovement);
            } else {
                owner.removeDynamicFlag(GameObjectDynamicLowFlags.InvertedMovement);
            }

            return;
        }

        var pauseTimesCount = stopFrames.size();
        var newToOldStateDelta = newState - oldState;

        if (newToOldStateDelta.getValue() < 0) {
            newToOldStateDelta += pauseTimesCount + 1;
        }

        var oldToNewStateDelta = oldState - newState;

        if (oldToNewStateDelta.getValue() < 0) {
            oldToNewStateDelta += pauseTimesCount + 1;
        }

        // this additional check is neccessary because client doesn't check dynamic flags on progress update
        // instead it multiplies progress from dynamicflags field by -1 and then compares that against 0
        // when calculating path progress while we simply check the flag if (!owner.hasDynamicFlag(GO_DYNFLAG_LO_INVERTED_MOVEMENT))
        var isAtStartOfPath = stateChangeProgress == 0;

        if (oldToNewStateDelta.getValue() < newToOldStateDelta.getValue() && !isAtStartOfPath) {
            owner.setDynamicFlag(GameObjectDynamicLowFlags.InvertedMovement);
        } else {
            owner.removeDynamicFlag(GameObjectDynamicLowFlags.InvertedMovement);
        }
    }

    @Override
    public void onRelocated() {
        updatePassengerPositions();
    }

    public final void updatePassengerPositions() {
        for (var passenger : passengers) {
            var pos = passenger.getMovementInfo().transport.pos.Copy();
            calculatePassengerPosition(pos);
            itransport.UpdatePassengerPosition(this, owner.getMap(), passenger, pos, true);
        }
    }


    public final int getTransportPeriod() {
        if (animationInfo != null) {
            return animationInfo.getTotalTime();
        }

        return 1;
    }


    public final IntArray getPauseTimes() {
        return stopFrames;
    }

    public final void setAutoCycleBetweenStopFrames(boolean on) {
        autoCycleBetweenStopFrames = on;
    }
}
