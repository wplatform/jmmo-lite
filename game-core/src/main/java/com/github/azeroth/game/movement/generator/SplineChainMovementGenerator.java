package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.SplineChainLink;
import com.github.azeroth.game.movement.SplineChainResumeInfo;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

import java.util.ArrayList;


public class SplineChainMovementGenerator extends MovementGenerator {
    private final int id;
    private final ArrayList<SplineChainLink> chain = new ArrayList<>();
    private final byte chainSize;
    private final boolean walk;
    private byte nextIndex;
    private byte nextFirstWP; // only used for resuming
    private int msToNext;


    public SplineChainMovementGenerator(int id, ArrayList<SplineChainLink> chain) {
        this(id, chain, false);
    }

    public SplineChainMovementGenerator(int id, ArrayList<SplineChainLink> chain, boolean walk) {
        id = id;
        chain = chain;
        chainSize = (byte) chain.size();
        walk = walk;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Roaming;
    }

    public SplineChainMovementGenerator(SplineChainResumeInfo info) {
        id = info.pointID;
        chain = info.chain;
        chainSize = (byte) info.chain.size();
        walk = info.isWalkMode;
        nextIndex = info.splineIndex;
        nextFirstWP = info.pointIndex;
        msToNext = info.timeToNext;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;

        if (info.splineIndex >= info.chain.size()) {
            addFlag(MovementGeneratorFlags.Finalized);
        }

        baseUnitState = UnitState.Roaming;
    }

    @Override
    public void initialize(Unit owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized);

        if (chainSize == 0) {
            Log.outError(LogFilter.movement, String.format("SplineChainMovementGenerator::Initialize: couldn't initialize generator, referenced spline is empty! (%1$s)", owner.getGUID()));

            return;
        }

        if (nextIndex >= chainSize) {
            Log.outWarn(LogFilter.movement, String.format("SplineChainMovementGenerator::Initialize: couldn't initialize generator, _nextIndex is >= chainSize (%1$s)", owner.getGUID()));
            msToNext = 0;

            return;
        }

        if (nextFirstWP != 0) // this is a resumed movegen that has to start with a partial spline
        {
            if (hasFlag(MovementGeneratorFlags.Finalized)) {
                return;
            }

            var thisLink = chain.get(nextIndex);

            if (nextFirstWP >= thisLink.points.size()) {
                Log.outError(LogFilter.movement, String.format("SplineChainMovementGenerator::Initialize: attempted to resume spline chain from invalid resume state, nextFirstWP >= path size (_nextIndex: %1$s, _nextFirstWP: %2$s). (%3$s)", nextIndex, nextFirstWP, owner.getGUID()));
                nextFirstWP = (byte) (thisLink.points.size() - 1);
            }

            owner.addUnitState(UnitState.RoamingMove);
            Span<Vector3> partial = thisLink.points.toArray(new Vector3[0]);

            sendPathSpline(owner, thisLink.velocity, partial[(_nextFirstWP - 1)..]);

            Log.outDebug(LogFilter.movement, String.format("SplineChainMovementGenerator::Initialize: resumed spline chain generator from resume state. (%1$s)", owner.getGUID()));

            ++nextIndex;

            if (nextIndex >= chainSize) {
                msToNext = 0;
            } else if (msToNext == 0) {
                msToNext = 1;
            }

            nextFirstWP = 0;
        } else {
            msToNext = Math.max(chain.get(nextIndex).timeToNext, 1);
            tangible.RefObject<Integer> tempRef__msToNext = new tangible.RefObject<Integer>(msToNext);
            sendSplineFor(owner, nextIndex, tempRef__msToNext);
            msToNext = tempRef__msToNext.refArgValue;

            ++nextIndex;

            if (nextIndex >= chainSize) {
                msToNext = 0;
            }
        }
    }

    @Override
    public void reset(Unit owner) {
        removeFlag(MovementGeneratorFlags.Deactivated);

        owner.stopMoving();
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || hasFlag(MovementGeneratorFlags.Finalized)) {
            return false;
        }

        // _msToNext being zero here means we're on the final spline
        if (msToNext == 0) {
            if (owner.getMoveSpline().finalized()) {
                addFlag(MovementGeneratorFlags.InformEnabled);

                return false;
            }

            return true;
        }

        if (msToNext <= diff) {
            // Send next spline
            Log.outDebug(LogFilter.movement, String.format("SplineChainMovementGenerator::Update: sending spline on index %1$s (%2$s ms late). (%3$s)", nextIndex, diff - msToNext, owner.getGUID()));
            msToNext = Math.max(chain.get(nextIndex).timeToNext, 1);
            tangible.RefObject<Integer> tempRef__msToNext = new tangible.RefObject<Integer>(msToNext);
            sendSplineFor(owner, nextIndex, tempRef__msToNext);
            msToNext = tempRef__msToNext.refArgValue;
            ++nextIndex;

            if (nextIndex >= chainSize) {
                // We have reached the final spline, once it finalizes we should also finalize the movegen (start checking on next update)
                msToNext = 0;

                return true;
            }
        } else {
            _msToNext -= diff;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        owner.clearUnitState(UnitState.RoamingMove);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.RoamingMove);
        }

        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled)) {
            var ai = owner.toCreature().getAI();

            if (ai != null) {
                ai.movementInform(MovementGeneratorType.SplineChain, id);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.SplineChain;
    }

    public final int getId() {
        return id;
    }

    private int sendPathSpline(Unit owner, float velocity, Span<Vector3> path) {
        var nodeCount = path.length;

        MoveSplineInit init = new MoveSplineInit(owner);

        if (nodeCount > 2) {
            init.movebyPath(path.ToArray());
        } else {
            init.moveTo(path[1], false, true);
        }

        if (velocity > 0.0f) {
            init.setVelocity(velocity);
        }

        init.setWalk(walk);

        return (int) init.launch();
    }

    private void sendSplineFor(Unit owner, int index, tangible.RefObject<Integer> duration) {
        Log.outDebug(LogFilter.movement, String.format("SplineChainMovementGenerator::SendSplineFor: sending spline on index: %1$s. (%2$s)", index, owner.getGUID()));

        var thisLink = chain.get(index);
        var actualDuration = sendPathSpline(owner, thisLink.velocity, new Span<Vector3>(thisLink.points.toArray(new Vector3[0])));

        if (actualDuration != thisLink.expectedDuration) {
            Log.outDebug(LogFilter.movement, String.format("SplineChainMovementGenerator::SendSplineFor: sent spline on index: %1$s, duration: %2$s ms. Expected duration: %3$s ms (delta %4$s ms). Adjusting. (%5$s)", index, actualDuration, thisLink.expectedDuration, actualDuration - thisLink.expectedDuration, owner.getGUID()));
            duration.refArgValue = (int) ((double) actualDuration / (double) thisLink.ExpectedDuration * duration.refArgValue);
        } else {
            Log.outDebug(LogFilter.movement, String.format("SplineChainMovementGenerator::SendSplineFor: sent spline on index %1$s, duration: %2$s ms. (%3$s)", index, actualDuration, owner.getGUID()));
        }
    }

    private SplineChainResumeInfo getResumeInfo(Unit owner) {
        if (nextIndex == 0) {
            return new SplineChainResumeInfo(id, chain, walk, (byte) 0, (byte) 0, msToNext);
        }

        if (owner.getMoveSpline().finalized()) {
            if (nextIndex < chainSize) {
                return new SplineChainResumeInfo(id, chain, walk, nextIndex, (byte) 0, 1);
            } else {
                return new SplineChainResumeInfo();
            }
        }

        return new SplineChainResumeInfo(id, chain, walk, (byte) (_nextIndex - 1), (byte) owner.getMoveSpline().currentSplineIdx(), msToNext);
    }
}
