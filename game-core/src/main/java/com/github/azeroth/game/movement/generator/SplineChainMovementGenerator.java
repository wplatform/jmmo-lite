package com.github.azeroth.game.movement.generator;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.Assert;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.model.SplineChainLink;
import com.github.azeroth.game.movement.model.SplineChainResumeInfo;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

import java.util.ArrayList;


public class SplineChainMovementGenerator extends MovementGenerator {
    private final int id;
    private final ArrayList<SplineChainLink> chain;
    private final byte chainSize;
    private final boolean walk;
    private byte nextIndex;
    private byte nextFirstWP; // only used for resuming
    private int msToNext;


    public SplineChainMovementGenerator(int id, ArrayList<SplineChainLink> chain) {
        this(id, chain, false);
    }

    public SplineChainMovementGenerator(int id, ArrayList<SplineChainLink> chain, boolean walk) {
        this.id = id;
        this.chain = chain;
        this.chainSize = (byte) chain.size();
        this.walk = walk;

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.NORMAL;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.ROAMING;
    }

    public SplineChainMovementGenerator(SplineChainResumeInfo info) {
        id = info.pointID;
        chain = info.chain;
        chainSize = (byte) info.chain.size();
        walk = info.walkMode;
        nextIndex = info.splineIndex;
        nextFirstWP = info.pointIndex;
        msToNext = info.timeToNext;

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.NORMAL;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);

        if (info.splineIndex >= info.chain.size()) {
            addFlag(MovementGeneratorFlag.FINALIZED);
        }

        baseUnitState = UnitState.ROAMING;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED);

        if (chainSize == 0) {
            Logs.MOVEMENT.error("SplineChainMovementGenerator::Initialize: couldn't initialize generator, referenced spline is empty! ({})", owner.getGUID());

            return;
        }

        if (nextIndex >= chainSize) {
            Logs.SPLINE_CHAIN.warn("SplineChainMovementGenerator::Initialize: couldn't initialize generator, _nextIndex is >= _chainSize ({})", owner.getGUID());
            msToNext = 0;

            return;
        }

        if (nextFirstWP != 0) // this is a resumed movegen that has to start with a partial spline
        {
            if (hasFlag(MovementGeneratorFlag.FINALIZED)) {
                return;
            }

            var thisLink = chain.get(nextIndex);

            if (nextFirstWP >= thisLink.points.size()) {
                Logs.SPLINE_CHAIN.error("SplineChainMovementGenerator::Initialize: attempted to resume spline chain from invalid resume state, _nextFirstWP >= path size (_nextIndex: {}, _nextFirstWP: {}). ({})", nextIndex, nextFirstWP, owner.getGUID());
                nextFirstWP = (byte) (thisLink.points.size() - 1);
            }

            owner.addUnitState(UnitState.ROAMING_MOVE);
            Vector3[] partial = thisLink.points.subList(nextFirstWP - 1, thisLink.points.size()).toArray(new Vector3[0]);

            sendPathSpline(owner, thisLink.velocity, partial);

            Logs.SPLINE_CHAIN.debug("SplineChainMovementGenerator::Initialize: resumed spline chain generator from resume state. ({})", owner.getGUID());

            ++nextIndex;

            if (nextIndex >= chainSize) {
                msToNext = 0;
            } else if (msToNext == 0) {
                msToNext = 1;
            }

            nextFirstWP = 0;
        } else {
            msToNext = Math.max(chain.get(nextIndex).timeToNext, 1);
            msToNext = sendSplineFor(owner, nextIndex, msToNext);

            ++nextIndex;

            if (nextIndex >= chainSize) {
                msToNext = 0;
            }
        }
    }

    @Override
    public void reset(Unit owner) {
        removeFlag(MovementGeneratorFlag.DEACTIVATED);

        owner.stopMoving();
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || hasFlag(MovementGeneratorFlag.FINALIZED)) {
            return false;
        }

        // _msToNext being zero here means we're on the final spline
        if (msToNext == 0) {
            if (owner.getMoveSpline().finalized()) {
                addFlag(MovementGeneratorFlag.INFORM_ENABLED);

                return false;
            }

            return true;
        }

        if (msToNext <= diff) {
            // Send next spline
            Logs.SPLINE_CHAIN.debug("SplineChainMovementGenerator::Update: sending spline on index {} ({} ms late). ({})", nextIndex, diff - msToNext, owner.getGUID());
            msToNext = Math.max(chain.get(nextIndex).timeToNext, 1);
            msToNext = sendSplineFor(owner, nextIndex, msToNext);
            ++nextIndex;

            if (nextIndex >= chainSize) {
                // We have reached the final spline, once it finalizes we should also finalize the movegen (start checking on next update)
                msToNext = 0;

                return true;
            }
        } else {
            msToNext -= diff;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlag.DEACTIVATED);
        owner.clearUnitState(UnitState.ROAMING_MOVE);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            owner.clearUnitState(UnitState.ROAMING_MOVE);
        }
        if (movementInform && hasFlag(MovementGeneratorFlag.INFORM_ENABLED)) {


            if (owner.getAi() instanceof CreatureAI ai) {
                ai.movementInform(MovementGeneratorType.SPLINE_CHAIN, id);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.SPLINE_CHAIN;
    }

    public final int getId() {
        return id;
    }

    private int sendPathSpline(Unit owner, float velocity, Vector3[] path) {
        var nodeCount = path.length;

        MoveSplineInit init = new MoveSplineInit(owner);

        if (nodeCount > 2) {
            init.movebyPath(path);
        } else {
            init.moveTo(path[1], false, true);
        }

        if (velocity > 0.0f) {
            init.setVelocity(velocity);
        }

        init.setWalk(walk);

        return init.launch();
    }

    private int sendSplineFor(Unit owner, int index, int duration) {

        Assert.isTrue(index < chainSize, "SplineChainMovementGenerator::SendSplineFor: referenced index ({}) higher than path size ({}})!", index, chainSize);
        Logs.SPLINE_CHAIN.debug("SplineChainMovementGenerator::SendSplineFor: sending spline on index: {}. ({})", index, owner.getGUID());

        var thisLink = chain.get(index);
        var actualDuration = sendPathSpline(owner, thisLink.velocity, thisLink.points.toArray(new Vector3[0]));

        if (actualDuration != thisLink.expectedDuration) {
            Logs.SPLINE_CHAIN.debug("SplineChainMovementGenerator::SendSplineFor: sent spline on index: {}, duration: {} ms. Expected duration: {} ms (delta {} ms). Adjusting. ({})", index, actualDuration, thisLink.expectedDuration, actualDuration - thisLink.expectedDuration, owner.getGUID());
            duration = (int) ((double) actualDuration / (double) thisLink.expectedDuration * duration);
        } else {
            Logs.SPLINE_CHAIN.debug("SplineChainMovementGenerator::SendSplineFor: sent spline on index {}, duration: {} ms. ({})", index, actualDuration, owner.getGUID());
        }
        return duration;
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

        return new SplineChainResumeInfo(id, chain, walk, (byte) (nextIndex - 1), (byte) owner.getMoveSpline().currentSplineIdx(), msToNext);
    }
}
