package com.github.azeroth.game.scenario;


import com.github.azeroth.game.achievement.CriteriaHandler;
import com.github.azeroth.game.achievement.CriteriaTree;
import com.github.azeroth.game.achievement.criteria;
import com.github.azeroth.game.achievement.criteriaProgress;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;


public class Scenario extends CriteriaHandler {
    private final ArrayList<ObjectGuid> players = new ArrayList<>();
    private final HashMap<ScenarioStepRecord, ScenarioStepState> stepStates = new HashMap<ScenarioStepRecord, ScenarioStepState>();
    protected ScenarioData data;
    private ScenarioStepRecord currentstep;

    public Scenario(ScenarioData scenarioData) {
        data = scenarioData;
        currentstep = null;

        //ASSERT(data);

        for (var scenarioStep : data.steps.values()) {
            setStepState(scenarioStep, ScenarioStepState.NotStarted);
        }

        var firstStep = getFirstStep();

        if (firstStep != null) {
            setStep(firstStep);
        } else {
            Log.outError(LogFilter.Scenario, "Scenario.Scenario: Could not launch Scenario (id: {0}), found no valid scenario step", data.entry.id);
        }
    }

    @Override
    public void reset() {
        super.reset();
        setStep(getFirstStep());
    }

    public void completeStep(ScenarioStepRecord step) {
        var quest = global.getObjectMgr().getQuestTemplate(step.rewardQuestID);

        if (quest != null) {
            for (var guid : players) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.rewardQuest(quest, lootItemType.item, 0, null, false);
                }
            }
        }

        if (step.IsBonusObjective()) {
            return;
        }

        ScenarioStepRecord newStep = null;

        for (var scenarioStep : data.steps.values()) {
            if (scenarioStep.IsBonusObjective()) {
                continue;
            }

            if (getStepState(scenarioStep) == ScenarioStepState.Done) {
                continue;
            }

            if (newStep == null || scenarioStep.orderIndex < newStep.orderIndex) {
                newStep = scenarioStep;
            }
        }

        setStep(newStep);

        if (isComplete()) {
            completeScenario();
        } else {
            Log.outError(LogFilter.Scenario, "Scenario.CompleteStep: Scenario (id: {0}, step: {1}) was completed, but could not determine new step, or validate scenario completion.", step.scenarioID, step.id);
        }
    }

    public void completeScenario() {
        sendPacket(new ScenarioCompleted(data.entry.id));
    }

    public void onPlayerEnter(Player player) {
        players.add(player.getGUID());
        sendScenarioState(player);
    }

    public void onPlayerExit(Player player) {
        players.remove(player.getGUID());
        sendBootPlayer(player);
    }

    public final ScenarioRecord getEntry() {
        return data.entry;
    }

    @Override
    public void sendCriteriaUpdate(Criteria criteria, CriteriaProgress progress, Duration timeElapsed, boolean timedCompleted) {
        ScenarioProgressUpdate progressUpdate = new ScenarioProgressUpdate();
        progressUpdate.criteriaProgress.id = criteria.id;
        progressUpdate.criteriaProgress.quantity = progress.counter;
        progressUpdate.criteriaProgress.player = progress.playerGUID;
        progressUpdate.criteriaProgress.date = progress.date;

        if (criteria.entry.startTimer != 0) {
            progressUpdate.criteriaProgress.flags = timedCompleted ? 1 : 0;
        }

        progressUpdate.criteriaProgress.timeFromStart = (int) timeElapsed.TotalSeconds;
        progressUpdate.criteriaProgress.timeFromCreate = 0;

        sendPacket(progressUpdate);
    }

    @Override
    public boolean canUpdateCriteriaTree(Criteria criteria, CriteriaTree tree, Player referencePlayer) {
        var step = tree.scenarioStep;

        if (step == null) {
            return false;
        }

        if (step.scenarioID != data.entry.id) {
            return false;
        }

        var currentStep = getStep();

        if (currentStep == null) {
            return false;
        }

        if (step.IsBonusObjective()) {
            return true;
        }

        return currentStep == step;
    }

    @Override
    public boolean canCompleteCriteriaTree(CriteriaTree tree) {
        var step = tree.scenarioStep;

        if (step == null) {
            return false;
        }

        var state = getStepState(step);

        if (state == ScenarioStepState.Done) {
            return false;
        }

        var currentStep = getStep();

        if (currentStep == null) {
            return false;
        }

        if (step.IsBonusObjective()) {
            if (step != currentStep) {
                return false;
            }
        }

        return super.canCompleteCriteriaTree(tree);
    }

    @Override
    public void completedCriteriaTree(CriteriaTree tree, Player referencePlayer) {
        var step = tree.scenarioStep;

        if (!isCompletedStep(step)) {
            return;
        }

        setStepState(step, ScenarioStepState.Done);
        completeStep(step);
    }

    @Override
    public void sendPacket(ServerPacket data) {
        for (var guid : players) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                player.sendPacket(data);
            }
        }
    }

    public final ScenarioStepRecord getLastStep() {
        // Do it like this because we don't know what order they're in inside the container.
        ScenarioStepRecord lastStep = null;

        for (var scenarioStep : data.steps.values()) {
            if (scenarioStep.IsBonusObjective()) {
                continue;
            }

            if (lastStep == null || scenarioStep.orderIndex > lastStep.orderIndex) {
                lastStep = scenarioStep;
            }
        }

        return lastStep;
    }

    public final void sendScenarioState(Player player) {
        ScenarioState scenarioState = new ScenarioState();
        buildScenarioState(scenarioState);
        player.sendPacket(scenarioState);
    }

    @Override
    public ArrayList<criteria> getCriteriaByType(CriteriaType type, int asset) {
        return global.getCriteriaMgr().getScenarioCriteriaByTypeAndScenario(type, data.entry.id);
    }

    public void update(int diff) {
    }

    public final void setStepState(ScenarioStepRecord step, ScenarioStepState state) {
        stepStates.put(step, state);
    }

    public final ScenarioStepRecord getStep() {
        return currentstep;
    }

    private void setStep(ScenarioStepRecord step) {
        currentstep = step;

        if (step != null) {
            setStepState(step, ScenarioStepState.inProgress);
        }

        ScenarioState scenarioState = new ScenarioState();
        buildScenarioState(scenarioState);
        sendPacket(scenarioState);
    }

    @Override
    public void sendCriteriaProgressRemoved(int criteriaId) {
    }

    @Override
    public void afterCriteriaTreeUpdate(CriteriaTree tree, Player referencePlayer) {
    }

    @Override
    public void sendAllData(Player receiver) {
    }

    private boolean isComplete() {
        for (var scenarioStep : data.steps.values()) {
            if (scenarioStep.IsBonusObjective()) {
                continue;
            }

            if (getStepState(scenarioStep) != ScenarioStepState.Done) {
                return false;
            }
        }

        return true;
    }

    private ScenarioStepState getStepState(ScenarioStepRecord step) {
        if (!stepStates.containsKey(step)) {
            return ScenarioStepState.Invalid;
        }

        return stepStates.get(step);
    }

    private boolean isCompletedStep(ScenarioStepRecord step) {
        var tree = global.getCriteriaMgr().getCriteriaTree(step.CriteriaTreeId);

        if (tree == null) {
            return false;
        }

        return isCompletedCriteriaTree(tree);
    }

    private void buildScenarioState(ScenarioState scenarioState) {
        scenarioState.scenarioID = (int) data.entry.id;
        var step = getStep();

        if (step != null) {
            scenarioState.currentStep = (int) step.id;
        }

        scenarioState.criteriaProgress = getCriteriasProgress();
        scenarioState.bonusObjectives = getBonusObjectivesData();

        // Don't know exactly what this is for, but seems to contain list of scenario steps that we're either on or that are completed
        for (var state : stepStates.entrySet()) {
            if (state.getKey().IsBonusObjective()) {
                continue;
            }

            switch (state.getValue()) {
                case ScenarioStepState.InProgress:
                case ScenarioStepState.Done:
                    break;
                case ScenarioStepState.NotStarted:
                default:
                    continue;
            }

            scenarioState.pickedSteps.add(state.getKey().id);
        }

        scenarioState.scenarioComplete = isComplete();
    }

    private ScenarioStepRecord getFirstStep() {
        // Do it like this because we don't know what order they're in inside the container.
        ScenarioStepRecord firstStep = null;

        for (var scenarioStep : data.steps.values()) {
            if (scenarioStep.IsBonusObjective()) {
                continue;
            }

            if (firstStep == null || scenarioStep.orderIndex < firstStep.orderIndex) {
                firstStep = scenarioStep;
            }
        }

        return firstStep;
    }

    private ArrayList<BonusObjectiveData> getBonusObjectivesData() {
        ArrayList<BonusObjectiveData> bonusObjectivesData = new ArrayList<>();

        for (var scenarioStep : data.steps.values()) {
            if (!scenarioStep.IsBonusObjective()) {
                continue;
            }

            if (global.getCriteriaMgr().getCriteriaTree(scenarioStep.CriteriaTreeId) != null) {
                BonusObjectiveData bonusObjectiveData = new BonusObjectiveData();
                bonusObjectiveData.bonusObjectiveID = (int) scenarioStep.id;
                bonusObjectiveData.objectiveComplete = getStepState(scenarioStep) == ScenarioStepState.Done;
                bonusObjectivesData.add(bonusObjectiveData);
            }
        }

        return bonusObjectivesData;
    }

    private ArrayList<CriteriaProgressPkt> getCriteriasProgress() {
        ArrayList<CriteriaProgressPkt> criteriasProgress = new ArrayList<>();

        if (!criteriaProgress.isEmpty()) {
            for (var pair : criteriaProgress.entrySet()) {
                CriteriaProgressPkt criteriaProgress = new criteriaProgressPkt();
                criteriaProgress.id = pair.getKey();
                criteriaProgress.quantity = pair.getValue().counter;
                criteriaProgress.date = pair.getValue().date;
                criteriaProgress.player = pair.getValue().playerGUID;
                criteriasProgress.add(criteriaProgress);
            }
        }

        return criteriasProgress;
    }

    private void sendBootPlayer(Player player) {
        ScenarioVacate scenarioBoot = new ScenarioVacate();
        scenarioBoot.scenarioID = (int) data.entry.id;
        player.sendPacket(scenarioBoot);
    }

    protected void finalize() throws Throwable {
        for (var guid : players) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                sendBootPlayer(player);
            }
        }

        players.clear();
    }
}
