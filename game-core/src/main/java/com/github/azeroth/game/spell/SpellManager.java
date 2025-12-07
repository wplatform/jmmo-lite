package com.github.azeroth.game.spell;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.domain.SkillLineAbility;
import com.github.azeroth.defines.Race;
import com.github.azeroth.defines.SkillType;
import com.github.azeroth.defines.SpellSchool;
import com.github.azeroth.defines.SpellSchoolMask;
import com.github.azeroth.game.battlepet.BattlePetMgr;
import com.github.azeroth.game.domain.creature.CreatureTemplate;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.extendability.*;
import com.github.azeroth.game.movement.MotionMaster;
import com.github.azeroth.game.listener.interfaces.ispellmanager.ISpellManagerSpellFix;
import com.github.azeroth.game.listener.interfaces.ispellmanager.ISpellManagerSpellLateFix;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.enums.SpellGroup;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public final class SpellManager {
    private static final HashMap<Integer, PetAura> DEFAULTPETAURAS = new HashMap<Integer, PetAura>();
    private final HashMap<Difficulty, spellInfo> emptyDiffDict = new HashMap<Difficulty, spellInfo>();
    private final HashMap<Integer, SpellChainNode> spellChainNodes = new HashMap<Integer, SpellChainNode>();
    private final MultiMap<Integer, Integer> spellsReqSpell = new MultiMap<Integer, Integer>();
    private final MultiMap<Integer, Integer> spellReq = new MultiMap<Integer, Integer>();
    private final HashMap<Integer, SpellLearnSkillNode> spellLearnSkills = new HashMap<Integer, SpellLearnSkillNode>();
    private final MultiMap<Integer, SpellLearnSpellNode> spellLearnSpells = new MultiMap<Integer, SpellLearnSpellNode>();
    private final HashMap<Map.entry<Integer, Integer>, SpellTargetPosition> spellTargetPositions = new HashMap<Map.entry<Integer, Integer>, SpellTargetPosition>();
    private final MultiMap<Integer, SpellGroup> spellSpellGroup = new MultiMap<Integer, SpellGroup>();
    private final MultiMap<SpellGroup, Integer> spellGroupSpell = new MultiMap<SpellGroup, Integer>();
    private final HashMap<SpellGroup, SpellGroupStackRule> spellGroupStack = new HashMap<SpellGroup, SpellGroupStackRule>();
    private final MultiMap<SpellGroup, auraType> spellSameEffectStack = new MultiMap<SpellGroup, auraType>();
    private final ArrayList<ServersideSpellName> serversideSpellNames = new ArrayList<>();
    private final HashMap<Integer, HashMap<Difficulty, SpellProcEntry>> spellProcMap = new HashMap<Integer, HashMap<Difficulty, SpellProcEntry>>();
    private final HashMap<Integer, SpellThreatEntry> spellThreatMap = new HashMap<Integer, SpellThreatEntry>();
    private final HashMap<Integer, HashMap<Integer, PetAura>> spellPetAuraMap = new HashMap<Integer, HashMap<Integer, PetAura>>();
    private final HashMap<Integer, SpellEnchantProcEntry> spellEnchantProcEventMap = new HashMap<Integer, SpellEnchantProcEntry>();
    private final MultiMap<Integer, SPELLAREA> spellAreaMap = new MultiMap<Integer, SPELLAREA>();
    private final MultiMap<Integer, SPELLAREA> spellAreaForQuestMap = new MultiMap<Integer, SPELLAREA>();
	private final MultiMap<(SpellLinkedType,int),Integer>spellLinkedMap =new MultiMap<(SpellLinkedType,int),Integer>();
    private final MultiMap<Integer, SPELLAREA> spellAreaForQuestEndMap = new MultiMap<Integer, SPELLAREA>();
    private final MultiMap<Integer, SPELLAREA> spellAreaForAuraMap = new MultiMap<Integer, SPELLAREA>();
    private final MultiMap<Integer, SPELLAREA> spellAreaForAreaMap = new MultiMap<Integer, SPELLAREA>();
    private final MultiMap<Integer, SkillLineAbility> skillLineAbilityMap = new MultiMap<Integer, SkillLineAbility>();
    private final HashMap<Integer, MultiMap<Integer, Integer>> petLevelupSpellMap = new HashMap<Integer, MultiMap<Integer, Integer>>();
    private final HashMap<Integer, PetDefaultSpellsEntry> petDefaultSpellsEntries = new HashMap<Integer, PetDefaultSpellsEntry>(); // only spells not listed in related mPetLevelupSpellMap entry
    private final HashMap<Integer, HashMap<Difficulty, spellInfo>> spellInfoMap = new HashMap<Integer, HashMap<Difficulty, spellInfo>>();
    private final HashMap<Tuple<Integer, Byte>, Integer> spellTotemModel = new HashMap<Tuple<Integer, Byte>, Integer>();
    private final HashMap<auraType, AuraEffectHandler> effectHandlers = new HashMap<auraType, AuraEffectHandler>();
    private final HashMap<SpellEffectName, SpellEffectHandler> spellEffectsHandlers = new HashMap<SpellEffectName, SpellEffectHandler>();
    public MultiMap<Integer, Integer> petFamilySpellsStorage = new MultiMap<Integer, Integer>();

    private SpellManager() {
        var currentAsm = Assembly.GetExecutingAssembly();

        for (var type : currentAsm.GetTypes()) {
            for (var methodInfo : type.GetMethods(BindingFlags.instance.getValue() | BindingFlags.NonPublic.getValue())) {
                for (var auraEffect : methodInfo.<AuraEffectHandlerAttribute>GetCustomAttributes()) {
                    if (auraEffect == null) {
                        continue;
                    }

                    var parameters = methodInfo.GetParameters();

                    if (parameters.length < 3) {
                        Log.outError(LogFilter.ServerLoading, "Method: {0} has wrong parameter count: {1} Should be 3. Can't load AuraEffect.", methodInfo.name, parameters.length);

                        continue;
                    }

                    if (parameters[0].ParameterType != AuraApplication.class || parameters[1].ParameterType != AuraEffectHandleModes.class || parameters[2].ParameterType != Boolean.class) {
                        Log.outError(LogFilter.ServerLoading, "Method: {0} has wrong parameter Types: ({1}, {2}, {3}) Should be (AuraApplication, AuraEffectHandleModes, Bool). Can't load AuraEffect.", methodInfo.name, parameters[0].ParameterType, parameters[1].ParameterType, parameters[2].ParameterType);

                        continue;
                    }

                    if (effectHandlers.containsKey(auraEffect.auraType)) {
                        Log.outError(LogFilter.ServerLoading, "Tried to override AuraEffectHandler of {0} with {1} (AuraType {2}).", effectHandlers.get(auraEffect.auraType).GetMethodInfo().name, methodInfo.name, auraEffect.auraType);

                        continue;
                    }

                    effectHandlers.put(auraEffect.auraType, (AuraEffectHandler) methodInfo.CreateDelegate(AuraEffectHandler.class));
                }

                for (var spellEffect : methodInfo.<SpellEffectHandlerAttribute>GetCustomAttributes()) {
                    if (spellEffect == null) {
                        continue;
                    }

                    if (spellEffectsHandlers.containsKey(spellEffect.effectName)) {
                        Log.outError(LogFilter.ServerLoading, "Tried to override SpellEffectsHandler of {0} with {1} (EffectName {2}).", spellEffectsHandlers.get(spellEffect.effectName).toString(), methodInfo.name, spellEffect.effectName);

                        continue;
                    }

                    spellEffectsHandlers.put(spellEffect.effectName, (SpellEffectHandler) methodInfo.CreateDelegate(SpellEffectHandler.class));
                }
            }
        }

        if (spellEffectsHandlers.isEmpty()) {
            Log.outFatal(LogFilter.ServerLoading, "Could'nt find any SpellEffectHandlers. Dev needs to check this out.");
            system.exit(1);
        }
    }

    public static boolean canSpellTriggerProcOnEvent(SpellProcEntry procEntry, ProcEventInfo eventInfo) {
        // proc type doesn't match
        if (!(eventInfo.getTypeMask() & procEntry.getProcFlags())) {
            return false;
        }

        // check XP or honor target requirement
        if (((int) procEntry.getAttributesMask().getValue() & 0x0000001) != 0) {
            var actor = eventInfo.getActor().toPlayer();

            if (actor) {
                if (eventInfo.getActionTarget() && !actor.isHonorOrXPTarget(eventInfo.getActionTarget())) {
                    return false;
                }
            }
        }

        // check power requirement
        if (procEntry.getAttributesMask().hasFlag(ProcAttributes.ReqPowerCost)) {
            if (!eventInfo.getProcSpell()) {
                return false;
            }

            var costs = eventInfo.getProcSpell().getPowerCost();
            var m = tangible.ListHelper.find(costs, cost -> cost.amount > 0);

            if (m == null) {
                return false;
            }
        }

        // always trigger for these types
        if (eventInfo.getTypeMask().hasFlag(procFlags.Heartbeat.getValue() | procFlags.kill.getValue().getValue() | procFlags.Death.getValue().getValue())) {
            return true;
        }

        // check school mask (if set) for other trigger types
        if (procEntry.getSchoolMask() != 0 && !(boolean) (eventInfo.getSchoolMask().getValue() & procEntry.getSchoolMask().getValue())) {
            return false;
        }

        // check spell family name/flags (if set) for spells
        if (eventInfo.getTypeMask().hasFlag(procFlags.SpellMask)) {
            var eventSpellInfo = eventInfo.getSpellInfo();

            if (eventSpellInfo != null) {
                if (!eventSpellInfo.isAffected(procEntry.getSpellFamilyName(), procEntry.getSpellFamilyMask())) {
                    return false;
                }
            }

            // check spell type mask (if set)
            if (procEntry.getSpellTypeMask() != 0 && !(boolean) (eventInfo.getSpellTypeMask().getValue() & procEntry.getSpellTypeMask().getValue())) {
                return false;
            }
        }

        // check spell phase mask
        if (eventInfo.getTypeMask().hasFlag(procFlags.ReqSpellPhaseMask)) {
            if (!(boolean) (eventInfo.getSpellPhaseMask().getValue() & procEntry.getSpellPhaseMask().getValue())) {
                return false;
            }
        }

        // check hit mask (on taken hit or on done hit, but not on spell cast phase)
        if (eventInfo.getTypeMask().hasFlag(procFlags.TakenHitMask) || (eventInfo.getTypeMask().hasFlag(procFlags.DoneHitMask) && !(boolean) (eventInfo.getSpellPhaseMask().getValue() & ProcFlagsSpellPhase.cast.getValue()))) {
            var hitMask = procEntry.getHitMask();

            // get default values if hit mask not set
            if (hitMask == 0) {
                // for taken procs allow normal + critical hits by default
                if (eventInfo.getTypeMask().hasFlag(procFlags.TakenHitMask)) {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.forValue(ProcFlagsHit.NORMAL.getValue() | ProcFlagsHit.critical.getValue()).getValue());
                }
                // for done procs allow normal + critical + absorbs by default
                else {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.forValue(ProcFlagsHit.NORMAL.getValue() | ProcFlagsHit.critical.getValue().getValue() | ProcFlagsHit.absorb.getValue().getValue()).getValue());
                }
            }

            if (!(boolean) (eventInfo.getHitMask().getValue() & hitMask.getValue())) {
                return false;
            }
        }

        return true;
    }

    public boolean isSpellValid(int spellId, Player player) {
        return isSpellValid(spellId, player, true);
    }

    public boolean isSpellValid(int spellId) {
        return isSpellValid(spellId, null, true);
    }

    public boolean isSpellValid(int spellId, Player player, boolean msg) {
        var spellInfo = getSpellInfo(spellId, Difficulty.NONE);

        return isSpellValid(spellInfo, player, msg);
    }

    public boolean isSpellValid(SpellInfo spellInfo, Player player) {
        return isSpellValid(spellInfo, player, true);
    }

    public boolean isSpellValid(SpellInfo spellInfo) {
        return isSpellValid(spellInfo, null, true);
    }

    public boolean isSpellValid(SpellInfo spellInfo, Player player, boolean msg) {
        // not exist
        if (spellInfo == null) {
            return false;
        }

        var needCheckReagents = false;

        // check effects
        for (var spellEffectInfo : spellInfo.getEffects()) {
            switch (spellEffectInfo.effect) {
                case 0:
                    continue;

                    // craft spell for crafting non-existed item (break client recipes list show)
                case CreateItem:
                case CreateLoot: {
                    if (spellEffectInfo.itemType == 0) {
                        // skip auto-loot crafting spells, its not need explicit item info (but have special fake items sometime)
                        if (!spellInfo.isLootCrafting()) {
                            if (msg) {
                                if (player) {
                                    player.sendSysMessage("Craft spell {0} not have create item entry.", spellInfo.getId());
                                } else {
                                    Log.outError(LogFilter.spells, "Craft spell {0} not have create item entry.", spellInfo.getId());
                                }
                            }

                            return false;
                        }
                    }
                    // also possible IsLootCrafting case but fake item must exist anyway
                    else if (global.getObjectMgr().getItemTemplate(spellEffectInfo.itemType) == null) {
                        if (msg) {
                            if (player) {
                                player.sendSysMessage("Craft spell {0} create not-exist in DB item (Entry: {1}) and then...", spellInfo.getId(), spellEffectInfo.itemType);
                            } else {
                                Log.outError(LogFilter.spells, "Craft spell {0} create not-exist in DB item (Entry: {1}) and then...", spellInfo.getId(), spellEffectInfo.itemType);
                            }
                        }

                        return false;
                    }

                    needCheckReagents = true;

                    break;
                }
                case LearnSpell: {
                    var spellInfo2 = getSpellInfo(spellEffectInfo.triggerSpell, Difficulty.NONE);

                    if (!isSpellValid(spellInfo2, player, msg)) {
                        if (msg) {
                            if (player != null) {
                                player.sendSysMessage("Spell {0} learn to broken spell {1}, and then...", spellInfo.getId(), spellEffectInfo.triggerSpell);
                            } else {
                                Log.outError(LogFilter.spells, "Spell {0} learn to invalid spell {1}, and then...", spellInfo.getId(), spellEffectInfo.triggerSpell);
                            }
                        }

                        return false;
                    }

                    break;
                }
            }
        }

        if (needCheckReagents) {
            for (var j = 0; j < SpellConst.MaxReagents; ++j) {
                if (spellInfo.Reagent[j] > 0 && global.getObjectMgr().getItemTemplate((int) spellInfo.Reagent[j]) == null) {
                    if (msg) {
                        if (player != null) {
                            player.sendSysMessage("Craft spell {0} have not-exist reagent in DB item (Entry: {1}) and then...", spellInfo.getId(), spellInfo.Reagent[j]);
                        } else {
                            Log.outError(LogFilter.spells, "Craft spell {0} have not-exist reagent in DB item (Entry: {1}) and then...", spellInfo.getId(), spellInfo.Reagent[j]);
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    public SpellChainNode getSpellChainNode(int spell_id) {
        return spellChainNodes.get(spell_id);
    }

    public int getFirstSpellInChain(int spell_id) {
        var node = getSpellChainNode(spell_id);

        if (node != null) {
            return node.first.getId();
        }

        return spell_id;
    }

    public int getLastSpellInChain(int spell_id) {
        var node = getSpellChainNode(spell_id);

        if (node != null) {
            return node.last.getId();
        }

        return spell_id;
    }

    public int getNextSpellInChain(int spell_id) {
        var node = getSpellChainNode(spell_id);

        if (node != null) {
            if (node.next != null) {
                return node.next.getId();
            }
        }

        return 0;
    }

    public int getPrevSpellInChain(int spell_id) {
        var node = getSpellChainNode(spell_id);

        if (node != null) {
            if (node.prev != null) {
                return node.prev.getId();
            }
        }

        return 0;
    }

    public byte getSpellRank(int spell_id) {
        var node = getSpellChainNode(spell_id);

        if (node != null) {
            return node.rank;
        }

        return 0;
    }

    public int getSpellWithRank(int spell_id, int rank) {
        return getSpellWithRank(spell_id, rank, false);
    }

    public int getSpellWithRank(int spell_id, int rank, boolean strict) {
        var node = getSpellChainNode(spell_id);

        if (node != null) {
            if (rank != node.rank) {
                return getSpellWithRank(node.rank < rank ? node.next.getId() : node.prev.getId(), rank, strict);
            }
        } else if (strict && rank > 1) {
            return 0;
        }

        return spell_id;
    }

    public ArrayList<Integer> getSpellsRequiredForSpellBounds(int spell_id) {
        return spellReq.get(spell_id);
    }

    public ArrayList<Integer> getSpellsRequiringSpellBounds(int spell_id) {
        return spellsReqSpell.get(spell_id);
    }

    public boolean isSpellRequiringSpell(int spellid, int req_spellid) {
        var spellsRequiringSpell = getSpellsRequiringSpellBounds(req_spellid);

        for (var spell : spellsRequiringSpell) {
            if (spell == spellid) {
                return true;
            }
        }

        return false;
    }

    public SpellLearnSkillNode getSpellLearnSkill(int spell_id) {
        return spellLearnSkills.get(spell_id);
    }

    public ArrayList<SpellLearnSpellNode> getSpellLearnSpellMapBounds(int spell_id) {
        return spellLearnSpells.get(spell_id);
    }

    public boolean isSpellLearnToSpell(int spell_id1, int spell_id2) {
        var bounds = getSpellLearnSpellMapBounds(spell_id1);

        for (var bound : bounds) {
            if (bound.spell == spell_id2) {
                return true;
            }
        }

        return false;
    }

    public SpellTargetPosition getSpellTargetPosition(int spell_id, int effIndex) {
        return spellTargetPositions.get(new KeyValuePair<Integer, Integer>(spell_id, effIndex));
    }

    public ArrayList<SpellGroup> getSpellSpellGroupMapBounds(int spell_id) {
        return spellSpellGroup.get(getFirstSpellInChain(spell_id));
    }

    public boolean isSpellMemberOfSpellGroup(int spellid, SpellGroup groupid) {
        var spellGroup = getSpellSpellGroupMapBounds(spellid);

        for (var group : spellGroup) {
            if (group == groupid) {
                return true;
            }
        }

        return false;
    }

    public void getSetOfSpellsInSpellGroup(SpellGroup group_id, tangible.OutObject<ArrayList<Integer>> foundSpells) {
        ArrayList<SpellGroup> usedGroups = new ArrayList<>();
        tangible.RefObject<ArrayList<SpellGroup>> tempRef_usedGroups = new tangible.RefObject<ArrayList<SpellGroup>>(usedGroups);
        getSetOfSpellsInSpellGroup(group_id, foundSpells, tempRef_usedGroups);
        usedGroups = tempRef_usedGroups.refArgValue;
    }

    public boolean addSameEffectStackRuleSpellGroups(SpellInfo spellInfo, AuraType auraType, double amount, HashMap<SpellGroup, Double> groups) {
        var spellId = spellInfo.getFirstRankSpell().getId();
        var spellGroupList = getSpellSpellGroupMapBounds(spellId);

        // Find group with SPELL_GROUP_STACK_RULE_EXCLUSIVE_SAME_EFFECT if it belongs to one
        for (var group : spellGroupList) {
            var found = spellSameEffectStack.get(group);

            if (found != null) {
                // check auraTypes
                if (!found.Any(p -> p == auraType)) {
                    continue;
                }

                // Put the highest amount in the map
                if (!groups.containsKey(group)) {
                    groups.put(group, amount);
                } else {
                    var curr_amount = groups.get(group);

                    // Take absolute second because this also counts for the highest negative aura
                    if (Math.abs(curr_amount) < Math.abs(amount)) {
                        groups.put(group, amount);
                    }
                }

                // return because a spell should be in only one SPELL_GROUP_STACK_RULE_EXCLUSIVE_SAME_EFFECT group per auraType
                return true;
            }
        }

        // Not in a SPELL_GROUP_STACK_RULE_EXCLUSIVE_SAME_EFFECT group, so return false
        return false;
    }

    public SpellGroupStackRule checkSpellGroupStackRules(SpellInfo spellInfo1, SpellInfo spellInfo2) {
        var spellid_1 = spellInfo1.getFirstRankSpell().getId();
        var spellid_2 = spellInfo2.getFirstRankSpell().getId();

        // find SpellGroups which are common for both spells
        var spellGroup1 = getSpellSpellGroupMapBounds(spellid_1);
        ArrayList<SpellGroup> groups = new ArrayList<>();

        for (var group : spellGroup1) {
            if (isSpellMemberOfSpellGroup(spellid_2, group)) {
                var add = true;
                var groupSpell = getSpellGroupSpellMapBounds(group);

                for (var group2 : groupSpell) {
                    if (group2 < 0) {
                        var currGroup = SpellGroup.forValue(Math.abs(group2));

                        if (isSpellMemberOfSpellGroup(spellid_1, currGroup) && isSpellMemberOfSpellGroup(spellid_2, currGroup)) {
                            add = false;

                            break;
                        }
                    }
                }

                if (add) {
                    groups.add(group);
                }
            }
        }

        var rule = SpellGroupStackRule.Default;

        for (var group : groups) {
            var found = spellGroupStack.get(group);

            if (found != 0) {
                rule = found;
            }

            if (rule != 0) {
                break;
            }
        }

        return rule;
    }

    public SpellGroupStackRule getSpellGroupStackRule(SpellGroup group) {
        if (spellGroupStack.containsKey(group)) {
            return spellGroupStack.get(group);
        }

        return SpellGroupStackRule.Default;
    }

    public SpellProcEntry getSpellProcEntry(SpellInfo spellInfo) {
        TValue diffdict;
        var procEntry;

        if ((spellProcMap.containsKey(spellInfo.getId()) && (diffdict = spellProcMap.get(spellInfo.getId())) == diffdict) && diffdict.TryGetValue(spellInfo.getDifficulty(), out procEntry)) {
            return procEntry;
        }

        var difficulty = CliDB.DifficultyStorage.get(spellInfo.getDifficulty());

        if (difficulty != null && diffdict != null) {
            do {
                tangible.OutObject<var> tempOut_procEntry = new tangible.OutObject<var>();
                if (diffdict.TryGetValue(Difficulty.forValue(difficulty.FallbackDifficultyID), tempOut_procEntry)) {
                    procEntry = tempOut_procEntry.outArgValue;
                    return procEntry;
                } else {
                    procEntry = tempOut_procEntry.outArgValue;
                }

                difficulty = CliDB.DifficultyStorage.get(difficulty.FallbackDifficultyID);
            } while (difficulty != null);
        }

        return null;
    }

    public SpellThreatEntry getSpellThreatEntry(int spellID) {
        var spellthreat = spellThreatMap.get(spellID);

        if (spellthreat != null) {
            return spellthreat;
        } else {
            var firstSpell = getFirstSpellInChain(spellID);

            return spellThreatMap.get(firstSpell);
        }
    }

    public ArrayList<SkillLineAbility> getSkillLineAbilityMapBounds(int spell_id) {
        return skillLineAbilityMap.get(spell_id);
    }

    public PetAura getPetAura(int spell_id, byte eff) {
        if (spellPetAuraMap.containsKey(spell_id)) {
            return spellPetAuraMap.get(spell_id).get(eff);
        }

        return null;
    }

    public HashMap<Integer, PetAura> getPetAuras(int spell_id) {
        TValue auras;
        if (spellPetAuraMap.containsKey(spell_id) && (auras = spellPetAuraMap.get(spell_id)) == auras) {
            return auras;
        }

        return DEFAULTPETAURAS;
    }

    public SpellEnchantProcEntry getSpellEnchantProcEvent(int enchId) {
        return spellEnchantProcEventMap.get(enchId);
    }

    public boolean isArenaAllowedEnchancment(int ench_id) {
        var enchantment = CliDB.SpellItemEnchantmentStorage.get(ench_id);

        if (enchantment != null) {
            return enchantment.getFlags().hasFlag(SpellItemEnchantmentFlags.AllowEnteringArena);
        }

        return false;
    }

    public ArrayList<Integer> getSpellLinked(SpellLinkedType type, int spellId) {
        return spellLinkedMap.get((type, spellId));
    }

    public MultiMap<Integer, Integer> getPetLevelupSpellList(CreatureFamily petFamily) {
        return petLevelupSpellMap.get(petFamily);
    }

    public PetDefaultSpellsEntry getPetDefaultSpellsEntry(int id) {
        return petDefaultSpellsEntries.get(id);
    }

    public ArrayList<SPELLAREA> getSpellAreaMapBounds(int spell_id) {
        return spellAreaMap.get(spell_id);
    }

    public ArrayList<SPELLAREA> getSpellAreaForQuestMapBounds(int quest_id) {
        return spellAreaForQuestMap.get(quest_id);
    }

    public ArrayList<SPELLAREA> getSpellAreaForQuestEndMapBounds(int quest_id) {
        return spellAreaForQuestEndMap.get(quest_id);
    }

    public ArrayList<SPELLAREA> getSpellAreaForAuraMapBounds(int spell_id) {
        return spellAreaForAuraMap.get(spell_id);
    }

    public ArrayList<SPELLAREA> getSpellAreaForAreaMapBounds(int area_id) {
        return spellAreaForAreaMap.get(area_id);
    }

    public <T extends Enum> SpellInfo getSpellInfo(T spellId) {
        return getSpellInfo(spellId, Difficulty.DIFFICULTY_NONE);
    }

    
    public <T extends Enum> SpellInfo getSpellInfo(T spellId, Difficulty difficulty) {
        return getSpellInfo((int) spellId, difficulty);
    }

    
    public <T extends Enum> boolean tryGetSpellInfo(T spellId, tangible.OutObject<spellInfo> spellInfo) {
        spellInfo.outArgValue = getSpellInfo(spellId);

        return spellInfo.outArgValue != null;
    }

    
    public <T extends Enum> boolean tryGetSpellInfo(T spellId, Difficulty difficulty, tangible.OutObject<spellInfo> spellInfo) {
        spellInfo.outArgValue = getSpellInfo(spellId, difficulty);

        return spellInfo.outArgValue != null;
    }

    public boolean tryGetSpellInfo(int spellId, tangible.OutObject<spellInfo> spellInfo) {
        spellInfo.outArgValue = getSpellInfo(spellId);

        return spellInfo.outArgValue != null;
    }

    public boolean tryGetSpellInfo(int spellId, Difficulty difficulty, tangible.OutObject<spellInfo> spellInfo) {
        spellInfo.outArgValue = getSpellInfo(spellId, difficulty);

        return spellInfo.outArgValue != null;
    }

    public SpellInfo getSpellInfo(int spellId) {
        return getSpellInfo(spellId, Difficulty.NONE);
    }

    public SpellInfo getSpellInfo(int spellId, Difficulty difficulty) {
        TValue diffDict;
        var spellInfo;

        if ((spellInfoMap.containsKey(spellId) && (diffDict = spellInfoMap.get(spellId)) == diffDict) && diffDict.TryGetValue(difficulty, out spellInfo)) {
            return spellInfo;
        }

        if (diffDict != null) {
            var difficultyEntry = CliDB.DifficultyStorage.get(difficulty);

            if (difficultyEntry != null) {
                do {
                    tangible.OutObject<var> tempOut_spellInfo = new tangible.OutObject<var>();
                    if (diffDict.TryGetValue(Difficulty.forValue(difficultyEntry.FallbackDifficultyID), tempOut_spellInfo)) {
                        spellInfo = tempOut_spellInfo.outArgValue;
                        return spellInfo;
                    } else {
                        spellInfo = tempOut_spellInfo.outArgValue;
                    }

                    difficultyEntry = CliDB.DifficultyStorage.get(difficultyEntry.FallbackDifficultyID);
                } while (difficultyEntry != null);
            }
        }

        return null;
    }

    public SpellInfo assertSpellInfo(int spellId, Difficulty difficulty) {
        var spellInfo = getSpellInfo(spellId, difficulty);

        return spellInfo;
    }

    public void forEachSpellInfo(tangible.Action1Param<spellInfo> callback) {
        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                callback.invoke(spellInfo);
            }
        }
    }

    public void forEachSpellInfoDifficulty(int spellId, tangible.Action1Param<spellInfo> callback) {
        for (var spellInfo : _GetSpellInfo(spellId).values()) {
            callback.invoke(spellInfo);
        }
    }

    public boolean hasSpellInfo(int spellId) {
        return hasSpellInfo(spellId, Difficulty.NONE);
    }

    public boolean hasSpellInfo(int spellId, Difficulty difficulty) {
        return getSpellInfo(spellId, difficulty) != null;
    }

    // SpellInfo object management

    //Extra Shit
    public SpellEffectHandler getSpellEffectHandler(SpellEffectName eff) {
        TValue eh;
        if (!(spellEffectsHandlers.containsKey(eff) && (eh = spellEffectsHandlers.get(eff)) == eh)) {
            Log.outError(LogFilter.spells, "No defined handler for SpellEffect {0}", eff);

            return spellEffectsHandlers.get(SpellEffectName.NONE);
        }

        return eh;
    }

    public AuraEffectHandler getAuraEffectHandler(AuraType type) {
        TValue eh;
        if (!(effectHandlers.containsKey(type) && (eh = effectHandlers.get(type)) == eh)) {
            Log.outError(LogFilter.spells, "No defined handler for AuraEffect {0}", type);

            return effectHandlers.get(AuraType.NONE);
        }

        return eh;
    }

    public SkillRangeType getSkillRangeType(SkillRaceClassInfoRecord rcEntry) {
        var skill = CliDB.SkillLineStorage.get(rcEntry.SkillID);

        if (skill == null) {
            return SkillRangeType.NONE;
        }

        if (global.getObjectMgr().getSkillTier(rcEntry.SkillTierID) != null) {
            return SkillRangeType.rank;
        }

        if (rcEntry.SkillID == (int) SkillType.Runeforging.getValue()) {
            return SkillRangeType.Mono;
        }

        switch (skill.categoryID) {
            case SkillCategory.Armor:
                return SkillRangeType.Mono;
            case SkillCategory.Languages:
                return SkillRangeType.language;
        }

        return SkillRangeType.level;
    }

    public boolean isPrimaryProfessionSkill(int skill) {
        var pSkill = CliDB.SkillLineStorage.get(skill);

        return pSkill != null && pSkill.categoryID == SkillCategory.profession && pSkill.ParentSkillLineID == 0;
    }

    public boolean isWeaponSkill(int skill) {
        var pSkill = CliDB.SkillLineStorage.get(skill);

        return pSkill != null && pSkill.categoryID == SkillCategory.Weapon;
    }

    public boolean isProfessionOrRidingSkill(int skill) {
        return isProfessionSkill(skill) || skill == (int) SkillType.Riding.getValue();
    }

    public boolean isProfessionSkill(int skill) {
        return isPrimaryProfessionSkill(skill) || skill == (int) SkillType.FISHING.getValue() || skill == (int) SkillType.Cooking.getValue();
    }

    public boolean isPartOfSkillLine(SkillType skillId, int spellId) {
        var skillBounds = getSkillLineAbilityMapBounds(spellId);

        if (skillBounds != null) {
            return skillBounds.stream().allMatch(skill -> skill.getSkillLine() == skillId.ordinal());
        }

        return false;
    }

    public SpellSchool getFirstSchoolInMask(SpellSchoolMask mask) {
        for (SpellSchool value : SpellSchool.values()) {
            if ((mask.value & (1 << value.ordinal())) != 0) {
                return value;
            }
        }
        return SpellSchool.NORMAL;
    }

    public int getModelForTotem(int spellId, Race race) {
        return spellTotemModel.get(Tuple.create(spellId, (byte) race.getValue()));
    }

    private boolean isSpellLearnSpell(int spell_id) {
        return spellLearnSpells.ContainsKey(spell_id);
    }

    private ArrayList<Integer> getSpellGroupSpellMapBounds(SpellGroup group_id) {
        return spellGroupSpell.get(group_id);
    }

    private void getSetOfSpellsInSpellGroup(SpellGroup group_id, tangible.OutObject<ArrayList<Integer>> foundSpells, tangible.RefObject<ArrayList<SpellGroup>> usedGroups) {
        foundSpells.outArgValue = new ArrayList<>();

        if (tangible.ListHelper.find(usedGroups.refArgValue, p -> p == group_id) == 0) {
            return;
        }

        usedGroups.refArgValue.add(group_id);

        var groupSpell = getSpellGroupSpellMapBounds(group_id);

        for (var group : groupSpell) {
            if (group < 0) {
                var currGroup = SpellGroup.forValue(Math.abs(group));
                getSetOfSpellsInSpellGroup(currGroup, foundSpells, usedGroups);
            } else {
                foundSpells.outArgValue.add(group);
            }
        }
    }

    private HashMap<Difficulty, spellInfo> _GetSpellInfo(int spellId) {
        TValue diffDict;
        if (spellInfoMap.containsKey(spellId) && (diffDict = spellInfoMap.get(spellId)) == diffDict) {
            return diffDict;
        }

        return emptyDiffDict;
    }

    private void unloadSpellInfoChains() {
        for (var pair : spellChainNodes.entrySet()) {
            for (var spellInfo : _GetSpellInfo(pair.getKey()).values()) {
                spellInfo.chainEntry = null;
            }
        }

        spellChainNodes.clear();
    }

    private boolean isTriggerAura(AuraType type) {
        switch (type) {
            case Dummy:
            case PeriodicDummy:
            case ModConfuse:
            case ModThreat:
            case ModStun:
            case ModDamageDone:
            case ModDamageTaken:
            case ModResistance:
            case ModStealth:
            case ModFear:
            case ModRoot:
            case Transform:
            case ReflectSpells:
            case DamageImmunity:
            case ProcTriggerSpell:
            case ProcTriggerDamage:
            case ModCastingSpeedNotStack:
            case SchoolAbsorb:
            case ModPowerCostSchoolPct:
            case ModPowerCostSchool:
            case ReflectSpellsSchool:
            case MechanicImmunity:
            case ModDamagePercentTaken:
            case SpellMagnet:
            case ModAttackPower:
            case ModPowerRegenPercent:
            case InterceptMeleeRangedAttacks:
            case OverrideClassScripts:
            case ModMechanicResistance:
            case MeleeAttackPowerAttackerBonus:
            case ModMeleeHaste:
            case ModMeleeHaste3:
            case ModAttackerMeleeHitChance:
            case ProcTriggerSpellWithValue:
            case ModSchoolMaskDamageFromCaster:
            case ModSpellDamageFromCaster:
            case AbilityIgnoreAurastate:
            case ModInvisibility:
            case ForceReaction:
            case ModTaunt:
            case ModDetaunt:
            case ModDamagePercentDone:
            case ModAttackPowerPct:
            case ModHitChance:
            case ModWeaponCritPercent:
            case ModBlockPercent:
            case ModRoot2:
                return true;
        }

        return false;
    }

    private boolean isAlwaysTriggeredAura(AuraType type) {
        switch (type) {
            case OverrideClassScripts:
            case ModStealth:
            case ModConfuse:
            case ModFear:
            case ModRoot:
            case ModStun:
            case Transform:
            case ModInvisibility:
            case SpellMagnet:
            case SchoolAbsorb:
            case ModRoot2:
                return true;
        }

        return false;
    }

    private ProcFlagsSpellType getSpellTypeMask(AuraType type) {
        switch (type) {
            case ModStealth:
                return ProcFlagsSpellType.damage.getValue() | ProcFlagsSpellType.NoDmgHeal.getValue();
            case ModConfuse:
            case ModFear:
            case ModRoot:
            case ModRoot2:
            case ModStun:
            case Transform:
            case ModInvisibility:
                return ProcFlagsSpellType.damage;
            default:
                return ProcFlagsSpellType.MaskAll;
        }
    }

    private void addSpellInfo(SpellInfo spellInfo) {
        TValue diffDict;
        if (!(spellInfoMap.containsKey(spellInfo.getId()) && (diffDict = spellInfoMap.get(spellInfo.getId())) == diffDict)) {
            diffDict = new HashMap<Difficulty, spellInfo>();
            spellInfoMap.put(spellInfo.getId(), diffDict);
        }

        diffDict[spellInfo.getDifficulty()] = spellInfo;
    }

    public void loadSpellRanks() {
        var oldMSTime = System.currentTimeMillis();

        HashMap<Integer, Integer> chains = new HashMap<Integer, Integer>();
        ArrayList<Integer> hasPrev = new ArrayList<>();

        for (var skillAbility : CliDB.SkillLineAbilityStorage.values()) {
            if (skillAbility.SupercedesSpell == 0) {
                continue;
            }

            if (!hasSpellInfo(skillAbility.SupercedesSpell, Difficulty.NONE) || !hasSpellInfo(skillAbility.spell, Difficulty.NONE)) {
                continue;
            }

            chains.put(skillAbility.SupercedesSpell, skillAbility.spell);
            hasPrev.add(skillAbility.spell);
        }

        // each first in chains that isn't present in hasPrev is a first rank
        for (var pair : chains.entrySet()) {
            if (hasPrev.contains(pair.getKey())) {
                continue;
            }

            var first = getSpellInfo(pair.getKey(), Difficulty.NONE);
            var next = getSpellInfo(pair.getValue(), Difficulty.NONE);

            if (!spellChainNodes.containsKey(pair.getKey())) {
                spellChainNodes.put(pair.getKey(), new SpellChainNode());
            }

            spellChainNodes.get(pair.getKey()).first = first;
            spellChainNodes.get(pair.getKey()).prev = null;
            spellChainNodes.get(pair.getKey()).next = next;
            spellChainNodes.get(pair.getKey()).last = next;
            spellChainNodes.get(pair.getKey()).rank = 1;

            for (var difficultyInfo : _GetSpellInfo(pair.getKey()).values()) {
                difficultyInfo.chainEntry = spellChainNodes.get(pair.getKey());
            }

            if (!spellChainNodes.containsKey(pair.getValue())) {
                spellChainNodes.put(pair.getValue(), new SpellChainNode());
            }

            spellChainNodes.get(pair.getValue()).first = first;
            spellChainNodes.get(pair.getValue()).prev = first;
            spellChainNodes.get(pair.getValue()).next = null;
            spellChainNodes.get(pair.getValue()).last = next;
            spellChainNodes.get(pair.getValue()).rank = 2;

            for (var difficultyInfo : _GetSpellInfo(pair.getValue()).values()) {
                difficultyInfo.chainEntry = spellChainNodes.get(pair.getValue());
            }

            byte rank = 3;
            var nextPair = chains.find(pair.getValue());

            while (nextPair.key != 0) {
                var prev = getSpellInfo(nextPair.key, Difficulty.NONE); // already checked in previous iteration (or above, in case this is the first one)
                var last = getSpellInfo(nextPair.value, Difficulty.NONE);

                if (last == null) {
                    break;
                }

                if (!spellChainNodes.containsKey(nextPair.key)) {
                    spellChainNodes.put(nextPair.key, new SpellChainNode());
                }

                spellChainNodes.get(nextPair.key).next = last;

                if (!spellChainNodes.containsKey(nextPair.value)) {
                    spellChainNodes.put(nextPair.value, new SpellChainNode());
                }

                spellChainNodes.get(nextPair.value).first = first;
                spellChainNodes.get(nextPair.value).prev = prev;
                spellChainNodes.get(nextPair.value).next = null;
                spellChainNodes.get(nextPair.value).last = last;
                spellChainNodes.get(nextPair.value).rank = rank++;

                for (var difficultyInfo : _GetSpellInfo(nextPair.value).values()) {
                    difficultyInfo.chainEntry = spellChainNodes.get(nextPair.value);
                }

                // fill 'last'
                do {
                    spellChainNodes.get(prev.id).last = last;
                    prev = spellChainNodes.get(prev.id).prev;
                } while (prev != null);

                nextPair = chains.find(nextPair.value);
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell rank records in {1}ms", spellChainNodes.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellRequired() {
        var oldMSTime = System.currentTimeMillis();

        spellsReqSpell.clear(); // need for reload case
        spellReq.clear(); // need for reload case

        //                                                   0        1
        var result = DB.World.query("SELECT spell_id, req_spell from spell_required");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell required records. DB table `spell_required` is empty.");

            return;
        }

        int count = 0;

        do {
            var spell_id = result.<Integer>Read(0);
            var spell_req = result.<Integer>Read(1);

            // check if chain is made with valid first spell
            var spell = getSpellInfo(spell_id, Difficulty.NONE);

            if (spell == null) {
                Logs.SQL.error("spell_id {0} in `spell_required` table is not found in dbcs, skipped", spell_id);

                continue;
            }

            var req_spell = getSpellInfo(spell_req, Difficulty.NONE);

            if (req_spell == null) {
                Logs.SQL.error("req_spell {0} in `spell_required` table is not found in dbcs, skipped", spell_req);

                continue;
            }

            if (spell.isRankOf(req_spell)) {
                Logs.SQL.error("req_spell {0} and spell_id {1} in `spell_required` table are ranks of the same spell, entry not needed, skipped", spell_req, spell_id);

                continue;
            }

            if (isSpellRequiringSpell(spell_id, spell_req)) {
                Logs.SQL.error("duplicated entry of req_spell {0} and spell_id {1} in `spell_required`, skipped", spell_req, spell_id);

                continue;
            }

            spellReq.add(spell_id, spell_req);
            spellsReqSpell.add(spell_req, spell_id);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell required records in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }


    ///#region Loads

    public void loadSpellLearnSkills() {
        spellLearnSkills.clear();

        // search auto-learned skills and add its to map also for use in unlearn spells/talents
        int dbc_count = 0;

        for (var kvp : spellInfoMap.values()) {
            for (var entry : kvp.VALUES) {
                if (entry.Difficulty != Difficulty.NONE) {
                    continue;
                }

                for (var spellEffectInfo : entry.effects) {
                    SpellLearnSkillNode dbc_node = new SpellLearnSkillNode();

                    switch (spellEffectInfo.effect) {
                        case SpellEffectName.Skill:
                            dbc_node.skill = SkillType.forValue(spellEffectInfo.miscValue);
                            dbc_node.step = (short) spellEffectInfo.calcValue();

                            if (dbc_node.skill != SkillType.Riding) {
                                dbc_node.value = 1;
                            } else {
                                dbc_node.value = (short) (dbc_node.Step * 75);
                            }

                            dbc_node.maxvalue = (short) (dbc_node.Step * 75);

                            break;
                        case SpellEffectName.DualWield:
                            dbc_node.skill = SkillType.DualWield;
                            dbc_node.step = 1;
                            dbc_node.value = 1;
                            dbc_node.maxvalue = 1;

                            break;
                        default:
                            continue;
                    }

                    spellLearnSkills.put(entry.id, dbc_node);
                    ++dbc_count;

                    break;
                }
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} Spell Learn Skills from DBC", dbc_count);
    }

    public void loadSpellLearnSpells() {
        var oldMSTime = System.currentTimeMillis();

        spellLearnSpells.clear();

        //                                         0      1        2
        var result = DB.World.query("SELECT entry, spellID, Active FROM spell_learn_spell");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell learn spells. DB table `spell_learn_spell` is empty.");

            return;
        }

        int count = 0;

        do {
            var spell_id = result.<Integer>Read(0);

            var node = new SpellLearnSpellNode();
            node.spell = result.<Integer>Read(1);
            node.overridesSpell = 0;
            node.active = result.<Boolean>Read(2);
            node.autoLearned = false;

            var spellInfo = getSpellInfo(spell_id, Difficulty.NONE);

            if (spellInfo == null) {
                Logs.SQL.error("Spell {0} listed in `spell_learn_spell` does not exist", spell_id);

                continue;
            }

            if (!hasSpellInfo(node.spell, Difficulty.NONE)) {
                Logs.SQL.error("Spell {0} listed in `spell_learn_spell` learning not existed spell {1}", spell_id, node.spell);

                continue;
            }

            if (spellInfo.hasAttribute(SpellCustomAttributes.IsTalent)) {
                Logs.SQL.error("Spell {0} listed in `spell_learn_spell` attempt learning talent spell {1}, skipped", spell_id, node.spell);

                continue;
            }

            spellLearnSpells.add(spell_id, node);
            ++count;
        } while (result.NextRow());

        // search auto-learned spells and add its to map also for use in unlearn spells/talents
        int dbc_count = 0;

        for (var kvp : spellInfoMap.values()) {
            for (var entry : kvp.VALUES) {
                if (entry.Difficulty != Difficulty.NONE) {
                    continue;
                }

                for (var spellEffectInfo : entry.effects) {
                    if (spellEffectInfo.effect == SpellEffectName.LearnSpell) {
                        var dbc_node = new SpellLearnSpellNode();
                        dbc_node.spell = spellEffectInfo.triggerSpell;
                        dbc_node.active = true; // all dbc based learned spells is active (show in spell book or hide by client itself)
                        dbc_node.overridesSpell = 0;

                        // ignore learning not existed spells (broken/outdated/or generic learnig spell 483
                        if (getSpellInfo(dbc_node.spell, Difficulty.NONE) == null) {
                            continue;
                        }

                        // talent or passive spells or skill-step spells auto-cast and not need dependent learning,
                        // pet teaching spells must not be dependent learning (cast)
                        // other required explicit dependent learning
                        dbc_node.autoLearned = spellEffectInfo.targetA.target == targets.UnitPet || entry.hasAttribute(SpellCustomAttributes.IsTalent) || entry.isPassive || entry.hasEffect(SpellEffectName.skillStep);

                        var db_node_bounds = getSpellLearnSpellMapBounds(entry.id);

                        var found = false;

                        for (var bound : db_node_bounds) {
                            if (bound.spell == dbc_node.spell) {
                                Logs.SQL.error("Spell {0} auto-learn spell {1} in spell.dbc then the record in `spell_learn_spell` is redundant, please fix DB.", entry.id, dbc_node.spell);

                                found = true;

                                break;
                            }
                        }

                        if (!found) // add new spell-spell pair if not found
                        {
                            spellLearnSpells.add(entry.id, dbc_node);
                            ++dbc_count;
                        }
                    }
                }
            }
        }

        for (var spellLearnSpell : CliDB.SpellLearnSpellStorage.values()) {
            if (!hasSpellInfo(spellLearnSpell.spellID, Difficulty.NONE) || !hasSpellInfo(spellLearnSpell.LearnSpellID, Difficulty.NONE)) {
                continue;
            }

            var db_node_bounds = spellLearnSpells.get(spellLearnSpell.spellID);
            var found = false;

            for (var spellNode : db_node_bounds) {
                if (spellNode.spell == spellLearnSpell.LearnSpellID) {
                    Logs.SQL.error(String.format("Found redundant record (entry: %1$s, SpellID: %2$s) in `spell_learn_spell`, spell added automatically from SpellLearnSpell.db2", spellLearnSpell.spellID, spellLearnSpell.LearnSpellID));
                    found = true;

                    break;
                }
            }

            if (found) {
                continue;
            }

            // Check if it is already found in spell.dbc, ignore silently if yes
            var dbc_node_bounds = getSpellLearnSpellMapBounds(spellLearnSpell.spellID);
            found = false;

            for (var spellNode : dbc_node_bounds) {
                if (spellNode.spell == spellLearnSpell.LearnSpellID) {
                    found = true;

                    break;
                }
            }

            if (found) {
                continue;
            }

            SpellLearnSpellNode dbcLearnNode = new SpellLearnSpellNode();
            dbcLearnNode.spell = spellLearnSpell.LearnSpellID;
            dbcLearnNode.overridesSpell = spellLearnSpell.OverridesSpellID;
            dbcLearnNode.active = true;
            dbcLearnNode.autoLearned = false;

            spellLearnSpells.add(spellLearnSpell.spellID, dbcLearnNode);
            ++dbc_count;
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell learn spells, {1} found in spell.dbc in {2} ms", count, dbc_count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellTargetPositions() {
        var oldMSTime = System.currentTimeMillis();

        spellTargetPositions.clear(); // need for reload case

        //                                         0   1         2           3                  4                  5
        var result = DB.World.query("SELECT ID, effectIndex, mapID, positionX, positionY, PositionZ FROM spell_target_position");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell target coordinates. DB table `spell_target_position` is empty.");

            return;
        }

        int count = 0;

        do {
            var spellId = result.<Integer>Read(0);
            int effIndex = result.<Byte>Read(1);

            SpellTargetPosition st = new SpellTargetPosition();
            st.targetMapId = result.<Integer>Read(2);
            st.X = result.<Float>Read(3);
            st.Y = result.<Float>Read(4);
            st.Z = result.<Float>Read(5);

            var mapEntry = CliDB.MapStorage.get(st.targetMapId);

            if (mapEntry == null) {
                Logs.SQL.error("Spell (ID: {0}, EffectIndex: {1}) is using a non-existant mapID (ID: {2})", spellId, effIndex, st.targetMapId);

                continue;
            }

            if (st.X == 0 && st.Y == 0 && st.Z == 0) {
                Logs.SQL.error("Spell (ID: {0}, EffectIndex: {1}) target coordinates not provided.", spellId, effIndex);

                continue;
            }

            var spellInfo = getSpellInfo(spellId, Difficulty.NONE);

            if (spellInfo == null) {
                Logs.SQL.error("Spell (ID: {0}) listed in `spell_target_position` does not exist.", spellId);

                continue;
            }

            if (effIndex >= spellInfo.effects.count) {
                Logs.SQL.error("Spell (Id: {0}, effIndex: {1}) listed in `spell_target_position` does not have an effect at index {2}.", spellId, effIndex, effIndex);

                continue;
            }

            // target facing is in degrees for 6484 & 9268... (blizz sucks)
            if (spellInfo.getEffect(effIndex).positionFacing > 2 * Math.PI) {
                st.orientation = spellInfo.getEffect(effIndex).PositionFacing * (float) Math.PI / 180;
            } else {
                st.orientation = spellInfo.getEffect(effIndex).positionFacing;
            }

            if (spellInfo.getEffect(effIndex).targetA.target == targets.DestDb || spellInfo.getEffect(effIndex).targetB.target == targets.DestDb) {
                var key = new KeyValuePair<Integer, Integer>(spellId, effIndex);
                spellTargetPositions.put(key, st);
                ++count;
            } else {
                Logs.SQL.error("Spell (Id: {0}, effIndex: {1}) listed in `spell_target_position` does not have target TARGET_DEST_DB (17).", spellId, effIndex);

                continue;
            }
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell teleport coordinates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellGroups() {
        var oldMSTime = System.currentTimeMillis();

        spellSpellGroup.clear(); // need for reload case
        spellGroupSpell.clear();

        //                                                0     1
        var result = DB.World.query("SELECT id, spell_id FROM spell_group");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell group definitions. DB table `spell_group` is empty.");

            return;
        }

        ArrayList<Integer> groups = new ArrayList<>();
        int count = 0;

        do {
            var group_id = result.<Integer>Read(0);

            if (group_id <= 1000 && group_id >= (int) SpellGroup.CoreRangeMax.getValue()) {
                Logs.SQL.error("SpellGroup id {0} listed in `spell_group` is in core range, but is not defined in core!", group_id);

                continue;
            }

            var spell_id = result.<Integer>Read(1);

            groups.add(group_id);
            spellGroupSpell.add(SpellGroup.forValue(group_id), spell_id);
        } while (result.NextRow());

        spellGroupSpell.RemoveIfMatching((group) ->
        {
            if (group.value < 0) {
                if (!groups.contains((int) Math.abs(group.value))) {
                    Logs.SQL.error("SpellGroup id {0} listed in `spell_group` does not exist", Math.abs(group.value));

                    return true;
                }
            } else {
                var spellInfo = getSpellInfo((int) group.value, Difficulty.NONE);

                if (spellInfo == null) {
                    Logs.SQL.error("Spell {0} listed in `spell_group` does not exist", group.value);

                    return true;
                } else if (spellInfo.getRank() > 1) {
                    Logs.SQL.error("Spell {0} listed in `spell_group` is not first rank of spell", group.value);

                    return true;
                }
            }

            return false;
        });

        for (var group : groups) {
            ArrayList<Integer> spells;
            tangible.OutObject<ArrayList<Integer>> tempOut_spells = new tangible.OutObject<ArrayList<Integer>>();
            getSetOfSpellsInSpellGroup(SpellGroup.forValue(group), tempOut_spells);
            spells = tempOut_spells.outArgValue;

            for (var spell : spells) {
                ++count;
                spellSpellGroup.add((int) spell, SpellGroup.forValue(group));
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell group definitions in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellGroupStackRules() {
        var oldMSTime = System.currentTimeMillis();

        spellGroupStack.clear(); // need for reload case
        spellSameEffectStack.clear();

        ArrayList<SpellGroup> sameEffectGroups = new ArrayList<>();

        //                                         0         1
        var result = DB.World.query("SELECT group_id, stack_rule FROM spell_group_stack_rules");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell group stack rules. DB table `spell_group_stack_rules` is empty.");

            return;
        }

        int count = 0;

        do {
            var group_id = SpellGroup.forValue(result.<Integer>Read(0));
            var stack_rule = SpellGroupStackRule.forValue(result.<Byte>Read(1));

            if (stack_rule.getValue() >= SpellGroupStackRule.max.getValue()) {
                Logs.SQL.error("SpellGroupStackRule {0} listed in `spell_group_stack_rules` does not exist", stack_rule);

                continue;
            }

            var spellGroup = getSpellGroupSpellMapBounds(group_id);

            if (spellGroup == null) {
                Logs.SQL.error("SpellGroup id {0} listed in `spell_group_stack_rules` does not exist", group_id);

                continue;
            }

            spellGroupStack.put(group_id, stack_rule);

            // different container for same effect stack rules, need to check effect types
            if (stack_rule == SpellGroupStackRule.ExclusiveSameEffect) {
                sameEffectGroups.add(group_id);
            }

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell group stack rules in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));

        count = 0;
        oldMSTime = System.currentTimeMillis();
        Log.outInfo(LogFilter.ServerLoading, "Parsing SPELL_GROUP_STACK_RULE_EXCLUSIVE_SAME_EFFECT stack rules...");

        for (var group_id : sameEffectGroups) {
            ArrayList<Integer> spellIds;
            tangible.OutObject<ArrayList<Integer>> tempOut_spellIds = new tangible.OutObject<ArrayList<Integer>>();
            getSetOfSpellsInSpellGroup(group_id, tempOut_spellIds);
            spellIds = tempOut_spellIds.outArgValue;

            ArrayList<auraType> auraTypes = new ArrayList<>();

            {
                // we have to 'guess' what effect this group corresponds to
                ArrayList<auraType> frequencyContainer = new ArrayList<>();

                // only waylay for the moment (shared group)
                AuraType[] SubGroups = {AuraType.ModMeleeHaste, AuraType.ModMeleeRangedHaste, AuraType.ModRangedHaste};

                for (int spellId : spellIds) {
                    var spellInfo = getSpellInfo(spellId, Difficulty.NONE);

                    for (var spellEffectInfo : spellInfo.getEffects()) {
                        if (!spellEffectInfo.isAura()) {
                            continue;
                        }

                        var auraName = spellEffectInfo.applyAuraName;

                        if (SubGroups.contains(auraName)) {
                            // count as first aura
                            auraName = SubGroups[0];
                        }

                        frequencyContainer.add(auraName);
                    }
                }

                AuraType auraType = AuraType.forValue(0);
                var auraTypeCount = 0;

                for (var auraName : frequencyContainer) {
                    var currentCount = frequencyContainer.size() (p -> p == auraName);

                    if (currentCount > auraTypeCount) {
                        auraType = auraName;
                        auraTypeCount = currentCount;
                    }
                }

                if (auraType == SubGroups[0]) {
                    auraTypes.addAll(Arrays.asList(SubGroups));

                    break;
                }

                if (auraTypes.isEmpty()) {
                    auraTypes.add(auraType);
                }
            }

            // re-check spells against guessed group
            for (int spellId : spellIds) {
                var spellInfo = getSpellInfo(spellId, Difficulty.NONE);

                var found = false;

                while (spellInfo != null) {
                    for (var auraType : auraTypes) {
                        if (spellInfo.hasAura(auraType)) {
                            found = true;

                            break;
                        }
                    }

                    if (found) {
                        break;
                    }

                    spellInfo = spellInfo.getNextRankSpell();
                }

                // not found either, log error
                if (!found) {
                    Logs.SQL.error(String.format("SpellId %1$s listed in `spell_group` with stack rule 3 does not share aura assigned for group %2$s", spellId, group_id));
                }
            }

            spellSameEffectStack.set(group_id, auraTypes);
            ++count;
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Parsed %1$s SPELL_GROUP_STACK_RULE_EXCLUSIVE_SAME_EFFECT stack rules in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public void loadSpellProcs() {
        var oldMSTime = System.currentTimeMillis();

        spellProcMap.clear(); // need for reload case

        //                                         0        1           2                3                 4                 5                 6
        var result = DB.World.query("SELECT spellId, schoolMask, spellFamilyName, SpellFamilyMask0, SpellFamilyMask1, SpellFamilyMask2, SpellFamilyMask3, " + "ProcFlag, ProcFlags2, spellTypeMask, spellPhaseMask, hitMask, attributesMask, disableEffectsMask, procsPerMinute, chance, cooldown, Charges FROM spell_proc");

        int count = 0;

        if (!result.isEmpty()) {
            do {
                var spellId = result.<Integer>Read(0);

                var allRanks = false;

                if (spellId < 0) {
                    allRanks = true;
                    spellId = -spellId;
                }

                var spellInfo = getSpellInfo((int) spellId, Difficulty.NONE);

                if (spellInfo == null) {
                    Logs.SQL.error("Spell {0} listed in `spell_proc` does not exist", spellId);

                    continue;
                }

                if (allRanks) {
                    if (spellInfo.getFirstRankSpell().getId() != (int) spellId) {
                        Logs.SQL.error("Spell {0} listed in `spell_proc` is not first rank of spell.", spellId);

                        continue;
                    }
                }

                SpellProcEntry baseProcEntry = new SpellProcEntry();

                baseProcEntry.setSchoolMask(spellSchoolMask.forValue(result.<Integer>Read(1)));
                baseProcEntry.setSpellFamilyName(SpellFamilyNames.forValue(result.<Integer>Read(2)));
                baseProcEntry.setSpellFamilyMask(new Flag128(result.<Integer>Read(3), result.<Integer>Read(4), result.<Integer>Read(5), result.<Integer>Read(6)));
                baseProcEntry.setProcFlags(new EnumFlag<ProcFlag>(result.<Integer>Read(7), result.<Integer>Read(8), 2));
                baseProcEntry.setSpellTypeMask(ProcFlagsSpellType.forValue(result.<Integer>Read(9)));
                baseProcEntry.setSpellPhaseMask(ProcFlagsSpellPhase.forValue(result.<Integer>Read(10)));
                baseProcEntry.setHitMask(ProcFlagsHit.forValue(result.<Integer>Read(11)));
                baseProcEntry.setAttributesMask(ProcAttributes.forValue(result.<Integer>Read(12)));
                baseProcEntry.setDisableEffectsMask(result.<Integer>Read(13));
                baseProcEntry.setProcsPerMinute(result.<Float>Read(14));
                baseProcEntry.setChance(result.<Float>Read(15));
                baseProcEntry.setCooldown(result.<Integer>Read(16));
                baseProcEntry.setCharges(result.<Integer>Read(17));

                while (spellInfo != null) {
                    if (!spellProcMap.containsKey(spellInfo.getId(), spellInfo.getDifficulty())) {
                        Logs.SQL.error("Spell {0} listed in `spell_proc` has duplicate entry in the table", spellInfo.getId());

                        break;
                    }

                    var procEntry = baseProcEntry;

                    // take defaults from dbcs
                    if (!procEntry.getProcFlags()) {
                        procEntry.setProcFlags(spellInfo.getProcFlags());
                    }

                    if (procEntry.getCharges() == 0) {
                        procEntry.setCharges(spellInfo.getProcCharges());
                    }

                    if (procEntry.getChance() == 0 && procEntry.getProcsPerMinute() == 0) {
                        procEntry.setChance(spellInfo.getProcChance());
                    }

                    if (procEntry.getCooldown() == 0) {
                        procEntry.setCooldown(spellInfo.getProcCooldown());
                    }

                    // validate data
                    if ((boolean) (procEntry.getSchoolMask().getValue() & ~spellSchoolMask.All.getValue())) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has wrong `SchoolMask` set: {1}", spellInfo.getId(), procEntry.getSchoolMask());
                    }

                    if (procEntry.getSpellFamilyName() != 0 && !global.getDB2Mgr().IsValidSpellFamiliyName(procEntry.getSpellFamilyName())) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has wrong `SpellFamilyName` set: {1}", spellInfo.getId(), procEntry.getSpellFamilyName());
                    }

                    if (procEntry.getChance() < 0) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has negative second in `Chance` field", spellInfo.getId());
                        procEntry.setChance(0);
                    }

                    if (procEntry.getProcsPerMinute() < 0) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has negative second in `ProcsPerMinute` field", spellInfo.getId());
                        procEntry.setProcsPerMinute(0);
                    }

                    if (!procEntry.getProcFlags()) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} doesn't have `ProcFlag` second defined, proc will not be triggered", spellInfo.getId());
                    }

                    if ((boolean) (procEntry.getSpellTypeMask().getValue() & ~ProcFlagsSpellType.MaskAll.getValue())) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has wrong `SpellTypeMask` set: {1}", spellInfo.getId(), procEntry.getSpellTypeMask());
                    }

                    if (procEntry.getSpellTypeMask() != 0 && !procEntry.getProcFlags().hasFlag(procFlags.SpellMask)) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has `SpellTypeMask` second defined, but it won't be used for defined `ProcFlag` second", spellInfo.getId());
                    }

                    if (procEntry.getSpellPhaseMask() == 0 && procEntry.getProcFlags().hasFlag(procFlags.ReqSpellPhaseMask)) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} doesn't have `SpellPhaseMask` second defined, but it's required for defined `ProcFlag` second, proc will not be triggered", spellInfo.getId());
                    }

                    if ((boolean) (procEntry.getSpellPhaseMask().getValue() & ~ProcFlagsSpellPhase.MaskAll.getValue())) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has wrong `SpellPhaseMask` set: {1}", spellInfo.getId(), procEntry.getSpellPhaseMask());
                    }

                    if (procEntry.getSpellPhaseMask() != 0 && !procEntry.getProcFlags().hasFlag(procFlags.ReqSpellPhaseMask)) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has `SpellPhaseMask` second defined, but it won't be used for defined `ProcFlag` second", spellInfo.getId());
                    }

                    if ((boolean) (procEntry.getHitMask().getValue() & ~ProcFlagsHit.MaskAll.getValue())) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has wrong `HitMask` set: {1}", spellInfo.getId(), procEntry.getHitMask());
                    }

                    if (procEntry.getHitMask() != 0 && !(procEntry.getProcFlags().hasFlag(procFlags.TakenHitMask) || (procEntry.getProcFlags().hasFlag(procFlags.DoneHitMask) && (procEntry.getSpellPhaseMask() == 0 || (boolean) (procEntry.getSpellPhaseMask().getValue() & (ProcFlagsSpellPhase.hit.getValue() | ProcFlagsSpellPhase.Finish.getValue()).getValue()))))) {
                        Logs.SQL.error("`spell_proc` table entry for spellId {0} has `HitMask` second defined, but it won't be used for defined `ProcFlag` and `SpellPhaseMask` values", spellInfo.getId());
                    }

                    for (var spellEffectInfo : spellInfo.getEffects()) {
                        if ((procEntry.getDisableEffectsMask() & (1 << (int) spellEffectInfo.effectIndex)) != 0 && !spellEffectInfo.isAura()) {
                            Logs.SQL.error(String.format("The `spell_proc` table entry for spellId %1$s has DisableEffectsMask with effect %2$s, but effect %3$s is not an aura effect", spellInfo.getId(), spellEffectInfo.effectIndex, spellEffectInfo.effectIndex));
                        }
                    }

                    if (procEntry.getAttributesMask().hasFlag(ProcAttributes.ReqSpellmod)) {
                        var found = false;

                        for (var spellEffectInfo : spellInfo.getEffects()) {
                            if (!spellEffectInfo.isAura()) {
                                continue;
                            }

                            if (spellEffectInfo.applyAuraName == AuraType.AddPctModifier || spellEffectInfo.applyAuraName == AuraType.AddFlatModifier || spellEffectInfo.applyAuraName == AuraType.AddPctModifierBySpellLabel || spellEffectInfo.applyAuraName == AuraType.AddFlatModifierBySpellLabel) {
                                found = true;

                                break;
                            }
                        }

                        if (!found) {
                            Logs.SQL.error(String.format("The `spell_proc` table entry for spellId %1$s has Attribute PROC_ATTR_REQ_SPELLMOD, but spell has no spell mods. Proc will not be triggered", spellInfo.getId()));
                        }
                    }

                    if ((procEntry.getAttributesMask().getValue() & ~ProcAttributes.AllAllowed.getValue()) != 0) {
                        Logs.SQL.error(String.format("The `spell_proc` table entry for spellId %1$s has `AttributesMask` second specifying invalid attributes 0x%2$X.", spellInfo.getId(), (procEntry.getAttributesMask().getValue() & ~ProcAttributes.AllAllowed.getValue())));
                        procEntry.setAttributesMask(ProcAttributes.forValue(procEntry.getAttributesMask().getValue() & ProcAttributes.AllAllowed.getValue()));
                    }

                    spellProcMap.put(spellInfo.getId(), spellInfo.getDifficulty(), procEntry);
                    ++count;

                    if (allRanks) {
                        spellInfo = spellInfo.getNextRankSpell();
                    } else {
                        break;
                    }
                }
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell proc conditions and data in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell proc conditions and data. DB table `spell_proc` is empty.");
        }

        // This generates default procs to retain compatibility with previous proc system
        Log.outInfo(LogFilter.ServerLoading, "Generating spell proc data from SPELLMAP...");
        count = 0;
        oldMSTime = System.currentTimeMillis();

        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                // Data already present in DB, overwrites default proc
                if (spellProcMap.containsKey(spellInfo.id, spellInfo.Difficulty)) {
                    continue;
                }

                // Nothing to do if no flags set
                if (spellInfo.procFlags == null) {
                    continue;
                }

                var addTriggerFlag = false;
                var procSpellTypeMask = ProcFlagsSpellType.NONE;
                int nonProcMask = 0;

                for (var spellEffectInfo : spellInfo.effects) {
                    if (!spellEffectInfo.isEffect()) {
                        continue;
                    }

                    var auraName = spellEffectInfo.applyAuraName;

                    if (auraName == 0) {
                        continue;
                    }

                    if (!isTriggerAura(auraName)) {
                        // explicitly disable non proccing auras to avoid losing charges on self proc
                        nonProcMask |= 1 << (int) spellEffectInfo.effectIndex;

                        continue;
                    }

                    procSpellTypeMask = ProcFlagsSpellType.forValue(procSpellTypeMask.getValue() | getSpellTypeMask(auraName).getValue());

                    if (isAlwaysTriggeredAura(auraName)) {
                        addTriggerFlag = true;
                    }

                    // many proc auras with taken procFlag mask don't have attribute "can proc with triggered"
                    // they should proc nevertheless (example mage armor spells with judgement)
                    if (!addTriggerFlag && spellInfo.procFlags.hasFlag(procFlags.TakenHitMask)) {
                        switch (auraName) {
                            case AuraType.ProcTriggerSpell:
                            case AuraType.ProcTriggerDamage:
                                addTriggerFlag = true;

                                break;
                            default:
                                break;
                        }
                    }
                }

                if (procSpellTypeMask == 0) {
                    for (var spellEffectInfo : spellInfo.effects) {
                        if (spellEffectInfo.isAura()) {
                            Logs.SQL.debug(String.format("Spell Id %1$s has DBC ProcFlag 0x%2$X 0x%3$X, but it's of non-proc aura type, it probably needs an entry in `spell_proc` table to be handled correctly.", spellInfo.id, spellInfo.ProcFlags[0], spellInfo.ProcFlags[1]));

                            break;
                        }
                    }

                    continue;
                }

                SpellProcEntry procEntry = new SpellProcEntry();
                procEntry.setSchoolMask(spellSchoolMask.forValue(0));
                procEntry.setProcFlags(spellInfo.procFlags);
                procEntry.setSpellFamilyName(SpellFamilyNames.forValue(0));

                for (var spellEffectInfo : spellInfo.effects) {
                    if (spellEffectInfo.isEffect() && isTriggerAura(spellEffectInfo.applyAuraName)) {
                        procEntry.setSpellFamilyMask(procEntry.getSpellFamilyMask() | spellEffectInfo.spellClassMask);
                    }
                }

                if (procEntry.getSpellFamilyMask()) {
                    procEntry.setSpellFamilyName(spellInfo.spellFamilyName);
                }

                procEntry.setSpellTypeMask(procSpellTypeMask);
                procEntry.setSpellPhaseMask(ProcFlagsSpellPhase.hit);
                procEntry.setHitMask(ProcFlagsHit.NONE); // uses default proc @see SpellMgr::CanSpellTriggerProcOnEvent

                for (var spellEffectInfo : spellInfo.effects) {
                    if (!spellEffectInfo.isAura()) {
                        continue;
                    }

                    switch (spellEffectInfo.applyAuraName) {
                        // Reflect auras should only proc off reflects
                        case AuraType.ReflectSpells:
                        case AuraType.ReflectSpellsSchool:
                            procEntry.setHitMask(ProcFlagsHit.Reflect);

                            break;
                        // Only drop charge on crit
                        case AuraType.ModWeaponCritPercent:
                            procEntry.setHitMask(ProcFlagsHit.critical);

                            break;
                        // Only drop charge on block
                        case AuraType.ModBlockPercent:
                            procEntry.setHitMask(ProcFlagsHit.Block);

                            break;
                        // proc auras with another aura reducing hit chance (eg 63767) only proc on missed attack
                        case AuraType.ModHitChance:
                            if (spellEffectInfo.calcValue() <= -100) {
                                procEntry.setHitMask(ProcFlagsHit.Miss);
                            }

                            break;
                        default:
                            continue;
                    }

                    break;
                }

                procEntry.setAttributesMask(ProcAttributes.forValue(0));
                procEntry.setDisableEffectsMask(nonProcMask);

                if (spellInfo.procFlags.hasFlag(procFlags.kill)) {
                    procEntry.setAttributesMask(ProcAttributes.forValue(procEntry.getAttributesMask().getValue() | ProcAttributes.ReqExpOrHonor.getValue()));
                }

                if (addTriggerFlag) {
                    procEntry.setAttributesMask(ProcAttributes.forValue(procEntry.getAttributesMask().getValue() | ProcAttributes.TriggeredCanProc.getValue()));
                }

                procEntry.setProcsPerMinute(0);
                procEntry.setChance(spellInfo.procChance);
                procEntry.setCooldown(spellInfo.procCooldown);
                procEntry.setCharges(spellInfo.procCharges);

                spellProcMap.put(spellInfo.id, spellInfo.Difficulty, procEntry);
                ++count;
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Generated spell proc data for {0} spells in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellThreats() {
        var oldMSTime = System.currentTimeMillis();

        spellThreatMap.clear(); // need for reload case

        //                                           0      1        2       3
        var result = DB.World.query("SELECT entry, flatMod, pctMod, apPctMod FROM spell_threat");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 aggro generating spells. DB table `spell_threat` is empty.");

            return;
        }

        int count = 0;

        do {
            var entry = result.<Integer>Read(0);

            if (!hasSpellInfo(entry, Difficulty.NONE)) {
                Logs.SQL.error("Spell {0} listed in `spell_threat` does not exist", entry);

                continue;
            }

            SpellThreatEntry ste = new SpellThreatEntry();
            ste.flatMod = result.<Integer>Read(1);
            ste.pctMod = result.<Float>Read(2);
            ste.apPctMod = result.<Float>Read(3);

            spellThreatMap.put(entry, ste);
            count++;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} SpellThreatEntries in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSkillLineAbilityMap() {
        var oldMSTime = System.currentTimeMillis();

        skillLineAbilityMap.clear();

        for (var skill : CliDB.SkillLineAbilityStorage.values()) {
            skillLineAbilityMap.add(skill.spell, skill);
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} SkillLineAbility MultiMap Data in {1} ms", skillLineAbilityMap.count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellPetAuras() {
        var oldMSTime = System.currentTimeMillis();

        spellPetAuraMap.clear(); // need for reload case

        //                                                  0       1       2    3
        var result = DB.World.query("SELECT spell, effectId, pet, aura FROM spell_pet_auras");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell pet auras. DB table `spell_pet_auras` is empty.");

            return;
        }

        int count = 0;

        do {
            var spell = result.<Integer>Read(0);
            var eff = result.<Byte>Read(1);
            var pet = result.<Integer>Read(2);
            var aura = result.<Integer>Read(3);

            var petAura = getPetAura(spell, eff);

            if (petAura != null) {
                petAura.addAura(pet, aura);
            } else {
                var spellInfo = getSpellInfo(spell, Difficulty.NONE);

                if (spellInfo == null) {
                    Logs.SQL.error("Spell {0} listed in `spell_pet_auras` does not exist", spell);

                    continue;
                }

                if (eff >= spellInfo.effects.count) {
                    Log.outError(LogFilter.spells, "Spell {0} listed in `spell_pet_auras` does not have effect at index {1}", spell, eff);

                    continue;
                }

                if (spellInfo.getEffect(eff).effect != SpellEffectName.DUMMY && (spellInfo.getEffect(eff).effect != SpellEffectName.ApplyAura || spellInfo.getEffect(eff).applyAuraName != AuraType.DUMMY)) {
                    Log.outError(LogFilter.spells, "Spell {0} listed in `spell_pet_auras` does not have dummy aura or dummy effect", spell);

                    continue;
                }

                var spellInfo2 = getSpellInfo(aura, Difficulty.NONE);

                if (spellInfo2 == null) {
                    Logs.SQL.error("Aura {0} listed in `spell_pet_auras` does not exist", aura);

                    continue;
                }

                PetAura pa = new PetAura(pet, aura, spellInfo.getEffect(eff).targetA.target == targets.UnitPet, spellInfo.getEffect(eff).calcValue());

                if (!spellPetAuraMap.containsKey(spell)) {
                    spellPetAuraMap.put(spell, new HashMap<Integer, PetAura>());
                }

                spellPetAuraMap.get(spell).put(eff, pa);
            }

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell pet auras in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellEnchantProcData() {
        var oldMSTime = System.currentTimeMillis();

        spellEnchantProcEventMap.clear(); // need for reload case

        //                                         0          1       2               3        4
        var result = DB.World.query("SELECT enchantID, chance, procsPerMinute, hitMask, AttributesMask FROM spell_enchant_proc_data");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell enchant proc event conditions. DB table `spell_enchant_proc_data` is empty.");

            return;
        }

        int count = 0;

        do {
            var enchantId = result.<Integer>Read(0);

            var ench = CliDB.SpellItemEnchantmentStorage.get(enchantId);

            if (ench == null) {
                Logs.SQL.error("Enchancment {0} listed in `spell_enchant_proc_data` does not exist", enchantId);

                continue;
            }

            SpellEnchantProcEntry spe = new SpellEnchantProcEntry();
            spe.chance = result.<Integer>Read(1);
            spe.procsPerMinute = result.<Float>Read(2);
            spe.hitMask = result.<Integer>Read(3);
            spe.attributesMask = EnchantProcAttributes.forValue(result.<Integer>Read(4));

            spellEnchantProcEventMap.put(enchantId, spe);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} enchant proc data definitions in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellLinked() {
        var oldMSTime = System.currentTimeMillis();

        spellLinkedMap.clear(); // need for reload case

        //                                                0              1             2
        var result = DB.World.query("SELECT spell_trigger, spell_effect, type FROM spell_linked_spell");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 linked spells. DB table `spell_linked_spell` is empty.");

            return;
        }

        int count = 0;

        do {
            var trigger = result.<Integer>Read(0);
            var effect = result.<Integer>Read(1);
            var type = SpellLinkedType.forValue(result.<Byte>Read(2));

            var spellInfo = getSpellInfo((int) Math.abs(trigger), Difficulty.NONE);

            if (spellInfo == null) {
                Logs.SQL.error("Spell {0} listed in `spell_linked_spell` does not exist", Math.abs(trigger));

                continue;
            }

            if (effect >= 0) {
                for (var spellEffectInfo : spellInfo.getEffects()) {
                    if (spellEffectInfo.calcValue() == Math.abs(effect)) {
                        Logs.SQL.error(String.format("The spell %1$s Effect: %2$s listed in `spell_linked_spell` has same bp%3$s like effect (possible hack)", Math.abs(trigger), Math.abs(effect), spellEffectInfo.effectIndex));
                    }
                }
            }

            if (!hasSpellInfo((int) Math.abs(effect), Difficulty.NONE)) {
                Logs.SQL.error("Spell {0} listed in `spell_linked_spell` does not exist", Math.abs(effect));

                continue;
            }

            if (type.getValue() < SpellLinkedType.cast.getValue() || type.getValue() > SpellLinkedType.Remove.getValue()) {
                Logs.SQL.error(String.format("The spell trigger %1$s, effect %2$s listed in `spell_linked_spell` has invalid link type %3$s, skipped.", trigger, effect, type));

                continue;
            }

            if (trigger < 0) {
                if (type != SpellLinkedType.cast) {
                    Logs.SQL.error(String.format("The spell trigger %1$s listed in `spell_linked_spell` has invalid link type %2$s, changed to 0.", trigger, type));
                }

                trigger = -trigger;
                type = SpellLinkedType.Remove;
            }


            if (type != SpellLinkedType.aura) {
                if (trigger == effect) {
                    Logs.SQL.error(String.format("The spell trigger %1$s, effect %2$s listed in `spell_linked_spell` triggers itself (infinite loop), skipped.", trigger, effect));

                    continue;
                }
            }

            spellLinkedMap.add((type, (int) trigger), effect);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} linked spells in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadPetLevelupSpellMap() {
        var oldMSTime = System.currentTimeMillis();

        petLevelupSpellMap.clear(); // need for reload case

        int count = 0;
        int family_count = 0;

        for (var creatureFamily : CliDB.CreatureFamilyStorage.values()) {
            for (byte j = 0; j < 2; ++j) {
                if (creatureFamily.SkillLine[j] == 0) {
                    continue;
                }

                var skillLineAbilities = global.getDB2Mgr().GetSkillLineAbilitiesBySkill((int) creatureFamily.SkillLine[j]);

                if (skillLineAbilities == null) {
                    continue;
                }

                for (var skillLine : skillLineAbilities) {
                    if (skillLine.AcquireMethod != AbilityLearnType.OnSkillLearn) {
                        continue;
                    }

                    var spell = getSpellInfo(skillLine.spell, Difficulty.NONE);

                    if (spell == null) // not exist or triggered or talent
                    {
                        continue;
                    }

                    if (spell.getSpellLevel() == 0) {
                        continue;
                    }

                    if (!petLevelupSpellMap.containsKey(creatureFamily.id)) {
                        petLevelupSpellMap.put(creatureFamily.id, new MultiMap<Integer, Integer>());
                    }

                    var spellSet = petLevelupSpellMap.get(creatureFamily.id);

                    if (spellSet.count == 0) {
                        ++family_count;
                    }

                    spellSet.add(spell.getSpellLevel(), spell.getId());
                    ++count;
                }
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} pet levelup and default spells for {1} families in {2} ms", count, family_count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadPetDefaultSpells() {
        var oldMSTime = System.currentTimeMillis();

        petDefaultSpellsEntries.clear();

        int countCreature = 0;

        Log.outInfo(LogFilter.ServerLoading, "Loading summonable creature templates...");

        // different summon spells
        for (var kvp : spellInfoMap.values()) {
            for (var spellEntry : kvp.VALUES) {
                if (spellEntry.Difficulty != Difficulty.NONE) {
                    for (var spellEffectInfo : spellEntry.effects) {
                        if (spellEffectInfo.effect == SpellEffectName.summon || spellEffectInfo.effect == SpellEffectName.SummonPet) {
                            var creature_id = spellEffectInfo.miscValue;
                            var cInfo = global.getObjectMgr().getCreatureTemplate((int) creature_id);

                            if (cInfo == null) {
                                continue;
                            }

                            // get default pet spells from creature_template
                            var petSpellsId = cInfo.entry;

                            if (petDefaultSpellsEntries.get(cInfo.entry) != null) {
                                continue;
                            }

                            PetDefaultSpellsEntry petDefSpells = new PetDefaultSpellsEntry();

                            for (byte j = 0; j < SharedConst.MaxCreatureSpellDataSlots; ++j) {
                                petDefSpells.Spellid[j] = cInfo.Spells[j];
                            }

                            if (loadPetDefaultSpells_helper(cInfo, petDefSpells)) {
                                petDefaultSpellsEntries.put(petSpellsId, petDefSpells);
                                ++countCreature;
                            }
                        }
                    }
                }
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} summonable creature templates in {1} ms", countCreature, time.GetMSTimeDiffToNow(oldMSTime));
    }

    private boolean loadPetDefaultSpells_helper(CreatureTemplate cInfo, PetDefaultSpellsEntry petDefSpells) {
        // skip empty list;
        var have_spell = false;

        for (byte j = 0; j < SharedConst.MaxCreatureSpellDataSlots; ++j) {
            if (petDefSpells.Spellid[j] != 0) {
                have_spell = true;

                break;
            }
        }

        if (!have_spell) {
            return false;
        }

        // remove duplicates with levelupSpells if any
        var levelupSpells = cInfo.family != 0 ? getPetLevelupSpellList(cInfo.family) : null;

        if (levelupSpells != null) {
            for (byte j = 0; j < SharedConst.MaxCreatureSpellDataSlots; ++j) {
                if (petDefSpells.Spellid[j] == 0) {
                    continue;
                }

                for (var pair : levelupSpells.KeyValueList) {
                    if (pair.value == petDefSpells.Spellid[j]) {
                        petDefSpells.Spellid[j] = 0;

                        break;
                    }
                }
            }
        }

        // skip empty list;
        have_spell = false;

        for (byte j = 0; j < SharedConst.MaxCreatureSpellDataSlots; ++j) {
            if (petDefSpells.Spellid[j] != 0) {
                have_spell = true;

                break;
            }
        }

        return have_spell;
    }

    public void loadSpellAreas() {
        var oldMSTime = System.currentTimeMillis();

        spellAreaMap.clear(); // need for reload case
        spellAreaForAreaMap.clear();
        spellAreaForQuestMap.clear();
        spellAreaForQuestEndMap.clear();
        spellAreaForAuraMap.clear();

        //                                            0     1         2              3               4                 5          6          7       8      9
        var result = DB.World.query("SELECT spell, area, quest_start, quest_start_status, quest_end_status, quest_end, aura_spell, racemask, gender, flags FROM spell_area");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell area requirements. DB table `spell_area` is empty.");

            return;
        }

        int count = 0;

        do {
            var spell = result.<Integer>Read(0);

            SpellArea spellArea = new SPELLAREA();
            spellArea.spellId = spell;
            spellArea.areaId = result.<Integer>Read(1);
            spellArea.questStart = result.<Integer>Read(2);
            spellArea.questStartStatus = result.<Integer>Read(3);
            spellArea.questEndStatus = result.<Integer>Read(4);
            spellArea.questEnd = result.<Integer>Read(5);
            spellArea.auraSpell = result.<Integer>Read(6);
            spellArea.raceMask = result.<Long>Read(7);
            spellArea.gender = gender.forValue(result.<Integer>Read(8));
            spellArea.flags = SpellAreaFlag.forValue(result.<Byte>Read(9));

            var spellInfo = getSpellInfo(spell, Difficulty.NONE);

            if (spellInfo != null) {
                if (spellArea.flags.hasFlag(SpellAreaFlag.AutoCast)) {
                    spellInfo.Attributes |= SpellAttr0.NoAuraCancel;
                }
            } else {
                Logs.SQL.error("Spell {0} listed in `spell_area` does not exist", spell);

                continue;
            }

            {
                var ok = true;
                var sa_bounds = getSpellAreaMapBounds(spellArea.spellId);

                for (var bound : sa_bounds) {
                    if (spellArea.spellId != bound.spellId) {
                        continue;
                    }

                    if (spellArea.areaId != bound.areaId) {
                        continue;
                    }

                    if (spellArea.questStart != bound.questStart) {
                        continue;
                    }

                    if (spellArea.auraSpell != bound.auraSpell) {
                        continue;
                    }

                    if ((spellArea.raceMask & bound.raceMask) == 0) {
                        continue;
                    }

                    if (spellArea.gender != bound.gender) {
                        continue;
                    }

                    // duplicate by requirements
                    ok = false;

                    break;
                }

                if (!ok) {
                    Logs.SQL.error("Spell {0} listed in `spell_area` already listed with similar requirements.", spell);

                    continue;
                }
            }

            if (spellArea.areaId != 0 && !CliDB.AreaTableStorage.containsKey(spellArea.areaId)) {
                Logs.SQL.error("Spell {0} listed in `spell_area` have wrong area ({1}) requirement", spell, spellArea.areaId);

                continue;
            }

            if (spellArea.questStart != 0 && global.getObjectMgr().getQuestTemplate(spellArea.questStart) == null) {
                Logs.SQL.error("Spell {0} listed in `spell_area` have wrong start quest ({1}) requirement", spell, spellArea.questStart);

                continue;
            }

            if (spellArea.questEnd != 0) {
                if (global.getObjectMgr().getQuestTemplate(spellArea.questEnd) == null) {
                    Logs.SQL.error("Spell {0} listed in `spell_area` have wrong end quest ({1}) requirement", spell, spellArea.questEnd);

                    continue;
                }
            }

            if (spellArea.auraSpell != 0) {
                var info = getSpellInfo((int) Math.abs(spellArea.auraSpell), Difficulty.NONE);

                if (info == null) {
                    Logs.SQL.error("Spell {0} listed in `spell_area` have wrong aura spell ({1}) requirement", spell, Math.abs(spellArea.auraSpell));

                    continue;
                }

                if (Math.abs(spellArea.auraSpell) == spellArea.spellId) {
                    Logs.SQL.error("Spell {0} listed in `spell_area` have aura spell ({1}) requirement for itself", spell, Math.abs(spellArea.auraSpell));

                    continue;
                }

                // not allow autocast chains by auraSpell field (but allow use as alternative if not present)
                if (spellArea.flags.hasFlag(SpellAreaFlag.AutoCast) && spellArea.auraSpell > 0) {
                    var chain = false;
                    var saBound = getSpellAreaForAuraMapBounds(spellArea.spellId);

                    for (var bound : saBound) {
                        if (bound.flags.hasFlag(SpellAreaFlag.AutoCast) && bound.auraSpell > 0) {
                            chain = true;

                            break;
                        }
                    }

                    if (chain) {
                        Logs.SQL.error("Spell {0} listed in `spell_area` have aura spell ({1}) requirement that itself autocast from aura", spell, spellArea.auraSpell);

                        continue;
                    }

                    var saBound2 = getSpellAreaMapBounds((int) spellArea.auraSpell);

                    for (var bound : saBound2) {
                        if (bound.flags.hasFlag(SpellAreaFlag.AutoCast) && bound.auraSpell > 0) {
                            chain = true;

                            break;
                        }
                    }

                    if (chain) {
                        Logs.SQL.error("Spell {0} listed in `spell_area` have aura spell ({1}) requirement that itself autocast from aura", spell, spellArea.auraSpell);

                        continue;
                    }
                }
            }

            if (spellArea.raceMask != 0 && (spellArea.raceMask & SharedConst.RaceMaskAllPlayable) == 0) {
                Logs.SQL.error("Spell {0} listed in `spell_area` have wrong race mask ({1}) requirement", spell, spellArea.raceMask);

                continue;
            }

            if (spellArea.gender != gender.NONE && spellArea.gender != gender.Female && spellArea.gender != gender.Male) {
                Logs.SQL.error("Spell {0} listed in `spell_area` have wrong gender ({1}) requirement", spell, spellArea.gender);

                continue;
            }

            spellAreaMap.add(spell, spellArea);
            var sa = spellAreaMap.get(spell);

            // for search by current zone/subzone at zone/subzone change
            if (spellArea.areaId != 0) {
                spellAreaForAreaMap.AddRange(spellArea.areaId, sa);
            }

            // for search at quest update checks
            if (spellArea.questStart != 0 || spellArea.questEnd != 0) {
                if (spellArea.questStart == spellArea.questEnd) {
                    spellAreaForQuestMap.AddRange(spellArea.questStart, sa);
                } else {
                    if (spellArea.questStart != 0) {
                        spellAreaForQuestMap.AddRange(spellArea.questStart, sa);
                    }

                    if (spellArea.questEnd != 0) {
                        spellAreaForQuestMap.AddRange(spellArea.questEnd, sa);
                    }
                }
            }

            // for search at quest start/reward
            if (spellArea.questEnd != 0) {
                spellAreaForQuestEndMap.AddRange(spellArea.questEnd, sa);
            }

            // for search at aura apply
            if (spellArea.auraSpell != 0) {
                spellAreaForAuraMap.AddRange((int) Math.abs(spellArea.auraSpell), sa);
            }

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell area requirements in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellInfoStore() {
        var oldMSTime = System.currentTimeMillis();

        spellInfoMap.clear();
        var loadData = new HashMap<( int id, Difficulty difficulty),SpellInfoLoadHelper > ();

        HashMap<Integer, BattlePetSpecie> battlePetSpeciesByCreature = new HashMap<Integer, BattlePetSpecie>();

        for (var battlePetSpecies : CliDB.BattlePetSpeciesStorage.values()) {
            if (battlePetSpecies.creatureID != 0) {
                battlePetSpeciesByCreature.put(battlePetSpecies.creatureID, battlePetSpecies);
            }
        }


//		SpellInfoLoadHelper GetLoadHelper(uint spellId, uint difficulty)
//			{
//				var first = (spellId, (Difficulty)difficulty);
//
//				if (!loadData.ContainsKey(first))
//					loadData[first] = new SpellInfoLoadHelper();
//
//				return loadData[first];
//			}

        for (var effect : CliDB.SpellEffectStorage.values()) {
            GetLoadHelper(effect.spellID, effect.difficultyID).Effects[effect.EffectIndex] = effect;

            if (effect.effect == SpellEffectName.summon.getValue()) {
                var summonProperties = CliDB.SummonPropertiesStorage.get(effect.EffectMiscValue[1]);

                if (summonProperties != null) {
                    if (summonProperties.slot == summonSlot.MiniPet.getValue() && summonProperties.getFlags().hasFlag(SummonPropertiesFlags.SummonFromBattlePetJournal)) {
                        var battlePetSpecies = battlePetSpeciesByCreature.get(effect.EffectMiscValue[0]);

                        if (battlePetSpecies != null) {
                            BattlePetMgr.addBattlePetSpeciesBySpell(effect.spellID, battlePetSpecies);
                        }
                    }
                }
            }

            if (effect.effect == SpellEffectName.language.getValue()) {
                global.getLanguageMgr().loadSpellEffectLanguage(effect);
            }
        }

        for (var auraOptions : CliDB.SpellAuraOptionsStorage.values()) {
            GetLoadHelper(auraOptions.spellID, auraOptions.difficultyID).auraOptions = auraOptions;
        }

        for (var auraRestrictions : CliDB.SpellAuraRestrictionsStorage.values()) {
            GetLoadHelper(auraRestrictions.spellID, auraRestrictions.difficultyID).auraRestrictions = auraRestrictions;
        }

        for (var castingRequirements : CliDB.SpellCastingRequirementsStorage.values()) {
            GetLoadHelper(castingRequirements.spellID, 0).castingRequirements = castingRequirements;
        }

        for (var categories : CliDB.SpellCategoriesStorage.values()) {
            GetLoadHelper(categories.spellID, categories.difficultyID).categories = categories;
        }

        for (var classOptions : CliDB.SpellClassOptionsStorage.values()) {
            GetLoadHelper(classOptions.spellID, 0).classOptions = classOptions;
        }

        for (var cooldowns : CliDB.SpellCooldownsStorage.values()) {
            GetLoadHelper(cooldowns.spellID, cooldowns.difficultyID).cooldowns = cooldowns;
        }

        for (var equippedItems : CliDB.SpellEquippedItemsStorage.values()) {
            GetLoadHelper(equippedItems.spellID, 0).equippedItems = equippedItems;
        }

        for (var interrupts : CliDB.SpellInterruptsStorage.values()) {
            GetLoadHelper(interrupts.spellID, interrupts.difficultyID).interrupts = interrupts;
        }

        for (var label : CliDB.SpellLabelStorage.values()) {
            GetLoadHelper(label.spellID, 0).labels.add(label);
        }

        for (var levels : CliDB.SpellLevelsStorage.values()) {
            GetLoadHelper(levels.spellID, levels.difficultyID).levels = levels;
        }

        for (var misc : CliDB.SpellMiscStorage.values()) {
            if (misc.difficultyID == 0) {
                GetLoadHelper(misc.spellID, misc.difficultyID).misc = misc;
            }
        }

        for (var power : CliDB.SpellPowerStorage.values()) {
            int difficulty = 0;
            var index = power.orderIndex;

            var powerDifficulty = CliDB.SpellPowerDifficultyStorage.get(power.id);

            if (powerDifficulty != null) {
                difficulty = powerDifficulty.difficultyID;
                index = powerDifficulty.orderIndex;
            }

            GetLoadHelper(power.spellID, difficulty).Powers[index] = power;
        }

        for (var reagents : CliDB.SpellReagentsStorage.values()) {
            GetLoadHelper(reagents.spellID, 0).reagents = reagents;
        }

        for (var reagentsCurrency : CliDB.SpellReagentsCurrencyStorage.values()) {
            GetLoadHelper(new integer(reagentsCurrency.spellID, 0)).reagentsCurrency.add(reagentsCurrency);
        }

        for (var scaling : CliDB.SpellScalingStorage.values()) {
            GetLoadHelper(scaling.spellID, 0).scaling = scaling;
        }

        for (var shapeshift : CliDB.SpellShapeshiftStorage.values()) {
            GetLoadHelper(shapeshift.spellID, 0).shapeshift = shapeshift;
        }

        for (var targetRestrictions : CliDB.SpellTargetRestrictionsStorage.values()) {
            GetLoadHelper(targetRestrictions.spellID, targetRestrictions.difficultyID).targetRestrictions = targetRestrictions;
        }

        for (var totems : CliDB.SpellTotemsStorage.values()) {
            GetLoadHelper(totems.spellID, 0).totems = totems;
        }

        for (var visual : CliDB.SpellXSpellVisualStorage.values()) {
            var visuals = GetLoadHelper(visual.spellID, visual.difficultyID).visuals;
            visuals.add(visual);
        }

        // sorted with unconditional visuals being last
        for (var data : loadData.entrySet()) {
            data.getValue().visuals.Sort((left, right) ->
            {
                return right.CasterPlayerConditionID.CompareTo(left.CasterPlayerConditionID);
            });
        }

        for (var empwerRank : CliDB.SpellEmpowerStageStorage) {
            T empowerRecord;
            tangible.OutObject<T> tempOut_empowerRecord = new tangible.OutObject<T>();
            if (CliDB.SpellEmpowerStorage.TryGetValue(empwerRank.value.SpellEmpowerID, tempOut_empowerRecord)) {
                empowerRecord = tempOut_empowerRecord.outArgValue;
                GetLoadHelper(empowerRecord.spellID, 0).empowerStages.add(empwerRank.value);
            } else {
                empowerRecord = tempOut_empowerRecord.outArgValue;
                Log.outWarn(LogFilter.ServerLoading, String.format("SpellEmpowerStageStorage contains SpellEmpowerID: %1$s that is not found in SpellEmpowerStorage.", empwerRank.value.SpellEmpowerID));
            }
        }

        for (var data : loadData.entrySet()) {
            var spellNameEntry = CliDB.SpellNameStorage.get(data.getKey().id);

            if (spellNameEntry == null) {
                continue;
            }

            // fill blanks
            var difficultyEntry = CliDB.DifficultyStorage.get(data.getKey().difficulty);

            if (difficultyEntry != null) {
                do {
                    var fallbackData = loadData.get((data.getKey().id, Difficulty.forValue(difficultyEntry.FallbackDifficultyID)))
                    ;

                    if (fallbackData != null) {
                        if (data.getValue().auraOptions == null) {
                            data.getValue().auraOptions = fallbackData.auraOptions;
                        }

                        if (data.getValue().auraRestrictions == null) {
                            data.getValue().auraRestrictions = fallbackData.auraRestrictions;
                        }

                        if (data.getValue().castingRequirements == null) {
                            data.getValue().castingRequirements = fallbackData.castingRequirements;
                        }

                        if (data.getValue().categories == null) {
                            data.getValue().categories = fallbackData.categories;
                        }

                        if (data.getValue().classOptions == null) {
                            data.getValue().classOptions = fallbackData.classOptions;
                        }

                        if (data.getValue().cooldowns == null) {
                            data.getValue().cooldowns = fallbackData.cooldowns;
                        }

                        for (var fallbackEff : fallbackData.effects) {
                            if (!data.getValue().effects.ContainsKey(fallbackEff.key)) {
                                data.getValue().Effects[fallbackEff.Key] = fallbackEff.value;
                            }
                        }

                        if (data.getValue().equippedItems == null) {
                            data.getValue().equippedItems = fallbackData.equippedItems;
                        }

                        if (data.getValue().interrupts == null) {
                            data.getValue().interrupts = fallbackData.interrupts;
                        }

                        if (data.getValue().labels.isEmpty()) {
                            data.getValue().labels = fallbackData.labels;
                        }

                        if (data.getValue().levels == null) {
                            data.getValue().levels = fallbackData.levels;
                        }

                        if (data.getValue().misc == null) {
                            data.getValue().misc = fallbackData.misc;
                        }

                        for (var i = 0; i < fallbackData.powers.length; ++i) {
                            if (data.getValue().Powers[i] == null) {
                                data.getValue().Powers[i] = fallbackData.Powers[i];
                            }
                        }

                        if (data.getValue().reagents == null) {
                            data.getValue().reagents = fallbackData.reagents;
                        }

                        if (data.getValue().reagentsCurrency.isEmpty()) {
                            data.getValue().reagentsCurrency = fallbackData.reagentsCurrency;
                        }

                        if (data.getValue().scaling == null) {
                            data.getValue().scaling = fallbackData.scaling;
                        }

                        if (data.getValue().shapeshift == null) {
                            data.getValue().shapeshift = fallbackData.shapeshift;
                        }

                        if (data.getValue().targetRestrictions == null) {
                            data.getValue().targetRestrictions = fallbackData.targetRestrictions;
                        }

                        if (data.getValue().totems == null) {
                            data.getValue().totems = fallbackData.totems;
                        }

                        // visuals fall back only to first difficulty that defines any visual
                        // they do not stack all difficulties in fallback chain
                        if (data.getValue().visuals.isEmpty()) {
                            data.getValue().visuals = fallbackData.visuals;
                        }
                    }

                    difficultyEntry = CliDB.DifficultyStorage.get(difficultyEntry.FallbackDifficultyID);
                } while (difficultyEntry != null);
            }

            //first first = id, difficulty
            //second first = id


            addSpellInfo(new spellInfo(spellNameEntry, data.getKey().difficulty, data.getValue()));
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded SpellInfo store in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void unloadSpellInfoImplicitTargetConditionLists() {
        for (var kvp : spellInfoMap.values()) {
            for (var spell : kvp.VALUES) {
                spell.unloadImplicitTargetConditionLists();
            }
        }
    }

    public void loadSpellInfoServerside() {
        var oldMSTime = System.currentTimeMillis();

        MultiMap < ( int spellId, Difficulty difficulty),SpellEffectRecord > spellEffects = new MultiMap<(
        int spellId, Difficulty difficulty),SpellEffectRecord > ();

        //                                                0        1            2             3       4           5                6
        var effectsResult = DB.World.query("SELECT spellID, effectIndex, difficultyID, effect, EffectAura, EffectAmplitude, effectAttributes, " + "EffectAuraPeriod, EffectBonusCoefficient, EffectChainAmplitude, EffectChainTargets, EffectItemType, EffectMechanic, EffectPointsPerResource, " + "EffectPosFacing, EffectRealPointsPerLevel, EffectTriggerSpell, BonusCoefficientFromAP, PvpMultiplier, coefficient, variance, " + "ResourceCoefficient, GroupSizeBasePointsCoefficient, effectBasePoints, EffectMiscValue1, EffectMiscValue2, EffectRadiusIndex1, " + "EffectRadiusIndex2, EffectSpellClassMask1, EffectSpellClassMask2, EffectSpellClassMask3, EffectSpellClassMask4, ImplicitTarget1, " + "ImplicitTarget2 FROM serverside_spell_effect");

        if (!effectsResult.isEmpty()) {
            do {
                var spellId = effectsResult.<Integer>Read(0);
                var difficulty = Difficulty.forValue(effectsResult.<Integer>Read(2));
                SpellEffectRecord effect = new SpellEffectRecord();
                effect.effectIndex = effectsResult.<Integer>Read(1);
                effect.effect = effectsResult.<Integer>Read(3);
                effect.EffectAura = effectsResult.<SHORT>Read(4);
                effect.EffectAmplitude = effectsResult.<Float>Read(5);
                effect.effectAttributes = SpellEffectAttributes.forValue(effectsResult.<Integer>Read(6));
                effect.EffectAuraPeriod = effectsResult.<Integer>Read(7);
                effect.EffectBonusCoefficient = effectsResult.<Float>Read(8);
                effect.EffectChainAmplitude = effectsResult.<Float>Read(9);
                effect.EffectChainTargets = effectsResult.<Integer>Read(10);
                effect.EffectItemType = effectsResult.<Integer>Read(11);
                effect.EffectMechanic = effectsResult.<Integer>Read(12);
                effect.EffectPointsPerResource = effectsResult.<Float>Read(13);
                effect.EffectPosFacing = effectsResult.<Float>Read(14);
                effect.EffectRealPointsPerLevel = effectsResult.<Float>Read(15);
                effect.EffectTriggerSpell = effectsResult.<Integer>Read(16);
                effect.BonusCoefficientFromAP = effectsResult.<Float>Read(17);
                effect.PvpMultiplier = effectsResult.<Float>Read(18);
                effect.coefficient = effectsResult.<Float>Read(19);
                effect.variance = effectsResult.<Float>Read(20);
                effect.resourceCoefficient = effectsResult.<Float>Read(21);
                effect.GroupSizeBasePointsCoefficient = effectsResult.<Float>Read(22);
                effect.effectBasePoints = effectsResult.<Float>Read(23);
                effect.EffectMiscValue[0] = effectsResult.<Integer>Read(24);
                effect.EffectMiscValue[1] = effectsResult.<Integer>Read(25);
                effect.EffectRadiusIndex[0] = effectsResult.<Integer>Read(26);
                effect.EffectRadiusIndex[1] = effectsResult.<Integer>Read(27);
                effect.EffectSpellClassMask = new Flag128(effectsResult.<Integer>Read(28), effectsResult.<Integer>Read(29), effectsResult.<Integer>Read(30), effectsResult.<Integer>Read(31));
                effect.ImplicitTarget[0] = effectsResult.<SHORT>Read(32);
                effect.ImplicitTarget[1] = effectsResult.<SHORT>Read(33);

                var existingSpellBounds = _GetSpellInfo(spellId);

                if (existingSpellBounds.isEmpty()) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s effext index %3$s references a regular spell loaded from file. Adding serverside effects to existing spells is not allowed.", spellId, difficulty, effect.effectIndex));

                    continue;
                }

                if (difficulty != Difficulty.NONE && !CliDB.DifficultyStorage.HasRecord((int) difficulty.getValue())) {
                    Logs.SQL.error(String.format("Serverside spell %1$s effect index %2$s references non-existing difficulty %3$s, skipped", spellId, effect.effectIndex, difficulty));

                    continue;
                }

                if (effect.effect >= (int) SpellEffectName.TotalSpellEffects.getValue()) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s has invalid effect type %3$s at index %4$s, skipped", spellId, difficulty, effect.effect, effect.effectIndex));

                    continue;
                }

                if (effect.EffectAura >= (int) AuraType.Total.getValue()) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s has invalid aura type %3$s at index %4$s, skipped", spellId, difficulty, effect.EffectAura, effect.effectIndex));

                    continue;
                }

                if (effect.ImplicitTarget[0] >= (int) targets.TotalSpellTargets.getValue()) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s has invalid targetA type %3$s at index %4$s, skipped", spellId, difficulty, effect.ImplicitTarget[0], effect.effectIndex));

                    continue;
                }

                if (effect.ImplicitTarget[1] >= (int) targets.TotalSpellTargets.getValue()) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s has invalid targetB type %3$s at index %4$s, skipped", spellId, difficulty, effect.ImplicitTarget[1], effect.effectIndex));

                    continue;
                }

                if (effect.EffectRadiusIndex[0] != 0 && !CliDB.SpellRadiusStorage.HasRecord(effect.EffectRadiusIndex[0])) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s has invalid radius id %3$s at index %4$s, set to 0", spellId, difficulty, effect.EffectRadiusIndex[0], effect.effectIndex));
                }

                if (effect.EffectRadiusIndex[1] != 0 && !CliDB.SpellRadiusStorage.HasRecord(effect.EffectRadiusIndex[1])) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s has invalid max radius id %3$s at index %4$s, set to 0", spellId, difficulty, effect.EffectRadiusIndex[1], effect.effectIndex));
                }

                spellEffects.add((spellId, difficulty), effect);
            } while (effectsResult.NextRow());
        }

        //                                               0   1             2           3       4         5           6             7              8
        var spellsResult = DB.World.query("SELECT id, difficultyID, categoryId, dispel, mechanic, Attributes, AttributesEx, AttributesEx2, AttributesEx3, " + "AttributesEx4, AttributesEx5, AttributesEx6, AttributesEx7, AttributesEx8, AttributesEx9, AttributesEx10, AttributesEx11, AttributesEx12, AttributesEx13, " + "AttributesEx14, stances, stancesNot, targets, targetCreatureType, requiresSpellFocus, facingCasterFlags, casterAuraState, targetAuraState, " + "ExcludeCasterAuraState, excludeTargetAuraState, casterAuraSpell, targetAuraSpell, excludeCasterAuraSpell, excludeTargetAuraSpell, " + "CasterAuraType, targetAuraType, excludeCasterAuraType, excludeTargetAuraType, CastingTimeIndex, " + "RecoveryTime, categoryRecoveryTime, startRecoveryCategory, startRecoveryTime, interruptFlags, AuraInterruptFlags1, auraInterruptFlags2, " + "ChannelInterruptFlags1, channelInterruptFlags2, procFlags, ProcFlags2, procChance, procCharges, procCooldown, ProcBasePPM, maxLevel, baseLevel, spellLevel, " + "DurationIndex, RangeIndex, speed, launchDelay, stackAmount, equippedItemClass, equippedItemSubClassMask, equippedItemInventoryTypeMask, contentTuningId, " + "SpellName, coneAngle, ConeWidth, maxTargetLevel, maxAffectedTargets, spellFamilyName, SpellFamilyFlags1, SpellFamilyFlags2, SpellFamilyFlags3, SpellFamilyFlags4, " + "DmgClass, preventionType, AreaGroupId, schoolMask, ChargeCategoryId FROM serverside_spell");

        if (!spellsResult.isEmpty()) {
            do {
                var spellId = spellsResult.<Integer>Read(0);
                var difficulty = Difficulty.forValue(spellsResult.<Integer>Read(1));

                if (CliDB.SpellNameStorage.HasRecord(spellId)) {
                    Logs.SQL.error(String.format("Serverside spell %1$s difficulty %2$s is already loaded from file. Overriding existing spells is not allowed.", spellId, difficulty));

                    continue;
                }

                serversideSpellNames.add(new ServersideSpellName(spellId, spellsResult.<String>Read(66)));

                SpellInfo spellInfo = new spellInfo(serversideSpellNames.get(serversideSpellNames.size() - 1).name, difficulty, spellEffects.get((spellId, difficulty)))
                ;
                spellInfo.setCategoryId(spellsResult.<Integer>Read(2));
                spellInfo.setDispel(DispelType.forValue(spellsResult.<Integer>Read(3)));
                spellInfo.setMechanic(mechanics.forValue(spellsResult.<Integer>Read(4)));
                spellInfo.setAttributes(SpellAttr0.forValue(spellsResult.<Integer>Read(5)));
                spellInfo.setAttributesEx(SpellAttr1.forValue(spellsResult.<Integer>Read(6)));
                spellInfo.setAttributesEx2(SpellAttr2.forValue(spellsResult.<Integer>Read(7)));
                spellInfo.setAttributesEx3(SpellAttr3.forValue(spellsResult.<Integer>Read(8)));
                spellInfo.setAttributesEx4(SpellAttr4.forValue(spellsResult.<Integer>Read(9)));
                spellInfo.setAttributesEx5(SpellAttr5.forValue(spellsResult.<Integer>Read(10)));
                spellInfo.setAttributesEx6(SpellAttr6.forValue(spellsResult.<Integer>Read(11)));
                spellInfo.setAttributesEx7(SpellAttr7.forValue(spellsResult.<Integer>Read(12)));
                spellInfo.setAttributesEx8(SpellAttr8.forValue(spellsResult.<Integer>Read(13)));
                spellInfo.setAttributesEx9(SpellAttr9.forValue(spellsResult.<Integer>Read(14)));
                spellInfo.setAttributesEx10(SpellAttr10.forValue(spellsResult.<Integer>Read(15)));
                spellInfo.setAttributesEx11(SpellAttr11.forValue(spellsResult.<Integer>Read(16)));
                spellInfo.setAttributesEx12(SpellAttr12.forValue(spellsResult.<Integer>Read(17)));
                spellInfo.setAttributesEx13(SpellAttr13.forValue(spellsResult.<Integer>Read(18)));
                spellInfo.setAttributesEx14(SpellAttr14.forValue(spellsResult.<Integer>Read(19)));
                spellInfo.setStances(spellsResult.<Long>Read(20));
                spellInfo.setStancesNot(spellsResult.<Long>Read(21));
                spellInfo.setTargets(SpellCastTargetFlags.forValue(spellsResult.<Integer>Read(22)));
                spellInfo.setTargetCreatureType(spellsResult.<Integer>Read(23));
                spellInfo.setRequiresSpellFocus(spellsResult.<Integer>Read(24));
                spellInfo.setFacingCasterFlags(spellsResult.<Integer>Read(25));
                spellInfo.setCasterAuraState(AuraStateType.forValue(spellsResult.<Integer>Read(26)));
                spellInfo.setTargetAuraState(AuraStateType.forValue(spellsResult.<Integer>Read(27)));
                spellInfo.setExcludeCasterAuraState(AuraStateType.forValue(spellsResult.<Integer>Read(28)));
                spellInfo.setExcludeTargetAuraState(AuraStateType.forValue(spellsResult.<Integer>Read(29)));
                spellInfo.setCasterAuraSpell(spellsResult.<Integer>Read(30));
                spellInfo.setTargetAuraSpell(spellsResult.<Integer>Read(31));
                spellInfo.setExcludeCasterAuraSpell(spellsResult.<Integer>Read(32));
                spellInfo.setExcludeTargetAuraSpell(spellsResult.<Integer>Read(33));
                spellInfo.setCasterAuraType(AuraType.forValue(spellsResult.<Integer>Read(34)));
                spellInfo.setTargetAuraType(AuraType.forValue(spellsResult.<Integer>Read(35)));
                spellInfo.setExcludeCasterAuraType(AuraType.forValue(spellsResult.<Integer>Read(36)));
                spellInfo.setExcludeTargetAuraType(AuraType.forValue(spellsResult.<Integer>Read(37)));
                spellInfo.setCastTimeEntry(CliDB.SpellCastTimesStorage.get(spellsResult.<Integer>Read(38)));
                spellInfo.setRecoveryTime(spellsResult.<Integer>Read(39));
                spellInfo.setCategoryRecoveryTime(spellsResult.<Integer>Read(40));
                spellInfo.setStartRecoveryCategory(spellsResult.<Integer>Read(41));
                spellInfo.setStartRecoveryTime(spellsResult.<Integer>Read(42));
                spellInfo.setInterruptFlags(SpellInterruptFlags.forValue(spellsResult.<Integer>Read(43)));
                spellInfo.setAuraInterruptFlags(SpellAuraInterruptFlag.forValue(spellsResult.<Integer>Read(44)));
                spellInfo.setAuraInterruptFlags2(SpellAuraInterruptFlag2.forValue(spellsResult.<Integer>Read(45)));
                spellInfo.setChannelInterruptFlags(SpellAuraInterruptFlag.forValue(spellsResult.<Integer>Read(46)));
                spellInfo.setChannelInterruptFlags2(SpellAuraInterruptFlag2.forValue(spellsResult.<Integer>Read(47)));
                spellInfo.setProcFlags(new EnumFlag<ProcFlag>(spellsResult.<Integer>Read(48), spellsResult.<Integer>Read(49)));
                spellInfo.setProcChance(spellsResult.<Integer>Read(50));
                spellInfo.setProcCharges(spellsResult.<Integer>Read(51));
                spellInfo.setProcCooldown(spellsResult.<Integer>Read(52));
                spellInfo.setProcBasePpm(spellsResult.<Float>Read(53));
                spellInfo.setMaxLevel(spellsResult.<Integer>Read(54));
                spellInfo.setBaseLevel(spellsResult.<Integer>Read(55));
                spellInfo.setSpellLevel(spellsResult.<Integer>Read(56));
                spellInfo.setDurationEntry(CliDB.SpellDurationStorage.get(spellsResult.<Integer>Read(57)));
                spellInfo.setRangeEntry(CliDB.SpellRangeStorage.get(spellsResult.<Integer>Read(58)));
                spellInfo.setSpeed(spellsResult.<Float>Read(59));
                spellInfo.setLaunchDelay(spellsResult.<Float>Read(60));
                spellInfo.setStackAmount(spellsResult.<Integer>Read(61));
                spellInfo.setEquippedItemClass(itemClass.forValue(spellsResult.<Integer>Read(62)));
                spellInfo.setEquippedItemSubClassMask(spellsResult.<Integer>Read(63));
                spellInfo.setEquippedItemInventoryTypeMask(spellsResult.<Integer>Read(64));
                spellInfo.setContentTuningId(spellsResult.<Integer>Read(65));
                spellInfo.setConeAngle(spellsResult.<Float>Read(67));
                spellInfo.setWidth(spellsResult.<Float>Read(68));
                spellInfo.setMaxTargetLevel(spellsResult.<Integer>Read(69));
                spellInfo.setMaxAffectedTargets(spellsResult.<Integer>Read(70));
                spellInfo.setSpellFamilyName(SpellFamilyNames.forValue(spellsResult.<Integer>Read(71)));
                spellInfo.setSpellFamilyFlags(new Flag128(spellsResult.<Integer>Read(72), spellsResult.<Integer>Read(73), spellsResult.<Integer>Read(74), spellsResult.<Integer>Read(75)));
                spellInfo.setDmgClass(SpellDmgClass.forValue(spellsResult.<Integer>Read(76)));
                spellInfo.setPreventionType(SpellPreventionType.forValue(spellsResult.<Integer>Read(77)));
                spellInfo.setRequiredAreasId(spellsResult.<Integer>Read(78));
                spellInfo.setSchoolMask(spellSchoolMask.forValue(spellsResult.<Integer>Read(79)));
                spellInfo.chargeCategoryId = spellsResult.<Integer>Read(80);

                addSpellInfo(spellInfo);
            } while (spellsResult.NextRow());
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s serverside spells %2$s ms", serversideSpellNames.size(), time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public void loadSpellInfoCustomAttributes() {
        var oldMSTime = System.currentTimeMillis();
        var oldMSTime2 = oldMSTime;

        var result = DB.World.query("SELECT entry, attributes FROM spell_custom_attr");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell custom attributes from DB. DB table `spell_custom_attr` is empty.");
        } else {
            int count = 0;

            do {
                var spellId = result.<Integer>Read(0);
                var attributes = result.<Integer>Read(1);

                var spells = _GetSpellInfo(spellId);

                if (spells.isEmpty()) {
                    Logs.SQL.error("Table `spell_custom_attr` has wrong spell (entry: {0}), ignored.", spellId);

                    continue;
                }

                for (var spellInfo : spells.values()) {
                    if (attributes.hasFlag((int) SpellCustomAttributes.ShareDamage.getValue())) {
                        if (!spellInfo.hasEffect(SpellEffectName.SchoolDamage)) {
                            Logs.SQL.error("Spell {0} listed in table `spell_custom_attr` with SPELL_ATTR0_CU_SHARE_DAMAGE has no SPELL_EFFECT_SCHOOL_DAMAGE, ignored.", spellId);

                            continue;
                        }
                    }

                    spellInfo.AttributesCu |= SpellCustomAttributes.forValue(attributes);
                }

                ++count;
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell custom attributes from DB in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime2));
        }

        ArrayList<Integer> talentSpells = new ArrayList<>();

        for (var talentInfo : CliDB.TalentStorage.values()) {
            talentSpells.add(talentInfo.spellID);
        }

        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                for (var spellEffectInfo : spellInfo.effects) {
                    // all bleed effects and spells ignore armor
                    if ((spellInfo.getEffectMechanicMask(spellEffectInfo.effectIndex) & (1 << mechanics.Bleed.getValue())) != 0) {
                        spellInfo.AttributesCu |= SpellCustomAttributes.IgnoreArmor;
                    }

                    switch (spellEffectInfo.applyAuraName) {
                        case AuraType.ModPossess:
                        case AuraType.ModConfuse:
                        case AuraType.ModCharm:
                        case AuraType.AoeCharm:
                        case AuraType.ModFear:
                        case AuraType.ModStun:
                            spellInfo.AttributesCu |= SpellCustomAttributes.AuraCC;

                            break;
                    }

                    switch (spellEffectInfo.applyAuraName) {
                        case AuraType.OpenStable: // No point in saving this, since the stable dialog can't be open on aura load anyway.
                            // Auras that require both caster & target to be in world cannot be saved
                        case AuraType.ControlVehicle:
                        case AuraType.BindSight:
                        case AuraType.ModPossess:
                        case AuraType.ModPossessPet:
                        case AuraType.ModCharm:
                        case AuraType.AoeCharm:
                            // Controlled by Battleground
                        case AuraType.BattleGroundPlayerPosition:
                        case AuraType.BattleGroundPlayerPositionFactional:
                            spellInfo.AttributesCu |= SpellCustomAttributes.AuraCannotBeSaved;

                            break;
                    }

                    switch (spellEffectInfo.effect) {
                        case SpellEffectName.SchoolDamage:
                        case SpellEffectName.HealthLeech:
                        case SpellEffectName.Heal:
                        case SpellEffectName.WeaponDamageNoSchool:
                        case SpellEffectName.WeaponPercentDamage:
                        case SpellEffectName.WeaponDamage:
                        case SpellEffectName.PowerBurn:
                        case SpellEffectName.HealMechanical:
                        case SpellEffectName.NormalizedWeaponDmg:
                        case SpellEffectName.HealPct:
                        case SpellEffectName.DamageFromMaxHealthPCT:
                            spellInfo.AttributesCu |= SpellCustomAttributes.CanCrit;

                            break;
                    }

                    switch (spellEffectInfo.effect) {
                        case SpellEffectName.SchoolDamage:
                        case SpellEffectName.WeaponDamage:
                        case SpellEffectName.WeaponDamageNoSchool:
                        case SpellEffectName.NormalizedWeaponDmg:
                        case SpellEffectName.WeaponPercentDamage:
                        case SpellEffectName.Heal:
                            spellInfo.AttributesCu |= SpellCustomAttributes.DirectDamage;

                            break;
                        case SpellEffectName.PowerDrain:
                        case SpellEffectName.PowerBurn:
                        case SpellEffectName.HealMaxHealth:
                        case SpellEffectName.HealthLeech:
                        case SpellEffectName.HealPct:
                        case SpellEffectName.EnergizePct:
                        case SpellEffectName.Energize:
                        case SpellEffectName.HealMechanical:
                            spellInfo.AttributesCu |= SpellCustomAttributes.NoInitialThreat;

                            break;
                        case SpellEffectName.Charge:
                        case SpellEffectName.ChargeDest:
                        case SpellEffectName.Jump:
                        case SpellEffectName.JumpDest:
                        case SpellEffectName.LeapBack:
                            spellInfo.AttributesCu |= SpellCustomAttributes.charge;

                            break;
                        case SpellEffectName.Pickpocket:
                            spellInfo.AttributesCu |= SpellCustomAttributes.PickPocket;

                            break;
                        case SpellEffectName.EnchantItem:
                        case SpellEffectName.EnchantItemTemporary:
                        case SpellEffectName.EnchantItemPrismatic:
                        case SpellEffectName.EnchantHeldItem: {
                            // only enchanting profession enchantments procs can stack
                            if (isPartOfSkillLine(SkillType.Enchanting, spellInfo.id)) {
                                var enchantId = (int) spellEffectInfo.miscValue;
                                var enchant = CliDB.SpellItemEnchantmentStorage.get(enchantId);

                                if (enchant == null) {
                                    break;
                                }

                                for (var s = 0; s < ItemConst.MaxItemEnchantmentEffects; ++s) {
                                    if (enchant.Effect[s] != ItemEnchantmentType.CombatSpell) {
                                        continue;
                                    }

                                    for (var procInfo : _GetSpellInfo(enchant.EffectArg[s]).values()) {
                                        // if proced directly from enchantment, not via proc aura
                                        // NOTE: Enchant Weapon - Blade Ward also has proc aura spell and is proced directly
                                        // however its not expected to stack so this check is good
                                        if (procInfo.hasAura(AuraType.ProcTriggerSpell)) {
                                            continue;
                                        }

                                        procInfo.AttributesCu |= SpellCustomAttributes.EnchantProc;
                                    }
                                }
                            }

                            break;
                        }
                    }
                }

                // spells ignoring hit result should not be binary
                if (!spellInfo.hasAttribute(SpellAttr3.AlwaysHit)) {
                    var setFlag = false;

                    for (var spellEffectInfo : spellInfo.effects) {
                        if (spellEffectInfo.isEffect()) {
                            switch (spellEffectInfo.effect) {
                                case SpellEffectName.SchoolDamage:
                                case SpellEffectName.WeaponDamage:
                                case SpellEffectName.WeaponDamageNoSchool:
                                case SpellEffectName.NormalizedWeaponDmg:
                                case SpellEffectName.WeaponPercentDamage:
                                case SpellEffectName.TriggerSpell:
                                case SpellEffectName.TriggerSpellWithValue:
                                    break;
                                case SpellEffectName.PersistentAreaAura:
                                case SpellEffectName.ApplyAura:
                                case SpellEffectName.ApplyAreaAuraParty:
                                case SpellEffectName.ApplyAreaAuraRaid:
                                case SpellEffectName.ApplyAreaAuraFriend:
                                case SpellEffectName.ApplyAreaAuraEnemy:
                                case SpellEffectName.ApplyAreaAuraPet:
                                case SpellEffectName.ApplyAreaAuraOwner:
                                case SpellEffectName.ApplyAuraOnPet:
                                case SpellEffectName.ApplyAreaAuraSummons:
                                case SpellEffectName.ApplyAreaAuraPartyNonrandom: {
                                    if (spellEffectInfo.applyAuraName == AuraType.PeriodicDamage || spellEffectInfo.applyAuraName == AuraType.PeriodicDamagePercent || spellEffectInfo.applyAuraName == AuraType.PeriodicDummy || spellEffectInfo.applyAuraName == AuraType.PeriodicLeech || spellEffectInfo.applyAuraName == AuraType.PeriodicHealthFunnel || spellEffectInfo.applyAuraName == AuraType.PeriodicDummy) {
                                        break;
                                    }


									goto default
                                        ;
                                }
                                default: {
                                    // No second and not interrupt cast or crowd control without SPELL_ATTR0_UNAFFECTED_BY_INVULNERABILITY flag
                                    if (spellEffectInfo.calcValue() == 0 && !((spellEffectInfo.effect == SpellEffectName.InterruptCast || spellInfo.hasAttribute(SpellCustomAttributes.AuraCC)) && !spellInfo.hasAttribute(SpellAttr0.NoImmunities))) {
                                        break;
                                    }

                                    // Sindragosa Frost Breath
                                    if (spellInfo.id == 69649 || spellInfo.id == 71056 || spellInfo.id == 71057 || spellInfo.id == 71058 || spellInfo.id == 73061 || spellInfo.id == 73062 || spellInfo.id == 73063 || spellInfo.id == 73064) {
                                        break;
                                    }

                                    // Frostbolt
                                    if (spellInfo.spellFamilyName == SpellFamilyNames.Mage && spellInfo.SpellFamilyFlags[0].hasFlag(0x20)) {
                                        break;
                                    }

                                    // Frost Fever
                                    if (spellInfo.id == 55095) {
                                        break;
                                    }

                                    // Haunt
                                    if (spellInfo.spellFamilyName == SpellFamilyNames.Warlock && spellInfo.SpellFamilyFlags[1].hasFlag(0x40000)) {
                                        break;
                                    }

                                    setFlag = true;

                                    break;
                                }
                            }

                            if (setFlag) {
                                spellInfo.AttributesCu |= SpellCustomAttributes.BinarySpell;

                                break;
                            }
                        }
                    }
                }

                // Remove normal school mask to properly calculate damage
                if (spellInfo.schoolMask.hasFlag(spellSchoolMask.NORMAL) && spellInfo.schoolMask.hasFlag(spellSchoolMask.Magic)) {
                    spellInfo.schoolMask &= ~spellSchoolMask.NORMAL;
                    spellInfo.AttributesCu |= SpellCustomAttributes.SchoolmaskNormalWithMagic;
                }

                spellInfo.initializeSpellPositivity();

                if (talentSpells.contains(spellInfo.id)) {
                    spellInfo.AttributesCu |= SpellCustomAttributes.IsTalent;
                }

                if (MathUtil.fuzzyNe(spellInfo.width, 0.0f)) {
                    spellInfo.AttributesCu |= SpellCustomAttributes.ConeLine;
                }

                switch (spellInfo.spellFamilyName) {
                    case SpellFamilyNames.Warrior:
                        // Shout / Piercing Howl
                        if (spellInfo.SpellFamilyFlags[0].hasFlag(0x20000)) {
                            spellInfo.AttributesCu |= SpellCustomAttributes.AuraCC;
                        }

                        break;
                    case SpellFamilyNames.Druid:
                        // Roar
                        if (spellInfo.SpellFamilyFlags[0].hasFlag(0x8)) {
                            spellInfo.AttributesCu |= SpellCustomAttributes.AuraCC;
                        }

                        break;
                    case SpellFamilyNames.Generic:
                        // Stoneclaw Totem effect
                        if (spellInfo.id == 5729) {
                            spellInfo.AttributesCu |= SpellCustomAttributes.AuraCC;
                        }

                        break;
                    default:
                        break;
                }

                spellInfo.initializeExplicitTargetMask();

                if (spellInfo.speed > 0.0f) {

//					bool visualNeedsAmmo(SpellXSpellVisualRecord spellXspellVisual)
//						{
//							var spellVisual = CliDB.SpellVisualStorage.get(spellXspellVisual.spellVisualID);
//
//							if (spellVisual == null)
//								return false;
//
//							var spellVisualMissiles = global.DB2Mgr.GetSpellVisualMissiles(spellVisual.SpellVisualMissileSetID);
//
//							if (spellVisualMissiles.isEmpty())
//								return false;
//
//							foreach (var spellVisualMissile in spellVisualMissiles)
//							{
//								var spellVisualEffectName = CliDB.SpellVisualEffectNameStorage.get(spellVisualMissile.SpellVisualEffectNameID);
//
//								if (spellVisualEffectName == null)
//									continue;
//
//								var type = (SpellVisualEffectNameType)spellVisualEffectName.type;
//
//								if (type == SpellVisualEffectNameType.UnitAmmoBasic || type == SpellVisualEffectNameType.UnitAmmoPreferred)
//									return true;
//							}
//
//							return false;
//						}

                    for (var spellXspellVisual : spellInfo.SpellVisuals) {
                        if (visualNeedsAmmo(spellXspellVisual)) {
                            spellInfo.AttributesCu |= SpellCustomAttributes.NeedsAmmoData;

                            break;
                        }
                    }
                }

                // Saving to DB happens before removing from world - skip saving these auras
                if (spellInfo.hasAuraInterruptFlag(SpellAuraInterruptFlag.LeaveWorld)) {
                    spellInfo.AttributesCu |= SpellCustomAttributes.AuraCannotBeSaved;
                }
            }
        }

        // addition for binary spells, omit spells triggering other spells
        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                if (!spellInfo.hasAttribute(SpellCustomAttributes.BinarySpell)) {
                    var allNonBinary = true;
                    var overrideAttr = false;

                    for (var spellEffectInfo : spellInfo.effects) {
                        if (spellEffectInfo.isAura() && spellEffectInfo.triggerSpell != 0) {
                            switch (spellEffectInfo.applyAuraName) {
                                case AuraType.PeriodicTriggerSpell:
                                case AuraType.PeriodicTriggerSpellFromClient:
                                case AuraType.PeriodicTriggerSpellWithValue:
                                    var triggerSpell = global.getSpellMgr().getSpellInfo(spellEffectInfo.triggerSpell, Difficulty.NONE);

                                    if (triggerSpell != null) {
                                        overrideAttr = true;

                                        if (triggerSpell.hasAttribute(SpellCustomAttributes.BinarySpell)) {
                                            allNonBinary = false;
                                        }
                                    }

                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    if (overrideAttr && allNonBinary) {
                        spellInfo.AttributesCu &= ~SpellCustomAttributes.BinarySpell;
                    }
                }

                // remove attribute from spells that can't crit
                if (spellInfo.hasAttribute(SpellCustomAttributes.CanCrit)) {
                    if (spellInfo.hasAttribute(SpellAttr2.CantCrit)) {
                        spellInfo.AttributesCu &= ~SpellCustomAttributes.CanCrit;
                    }
                }
            }
        }

        // add custom attribute to liquid auras
        for (var liquid : CliDB.LiquidTypeStorage.values()) {
            if (liquid.spellID != 0) {
                for (var spellInfo : _GetSpellInfo(liquid.spellID).values()) {
                    spellInfo.AttributesCu |= SpellCustomAttributes.AuraCannotBeSaved;
                }
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded SpellInfo custom attributes in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    private void applySpellFix(int[] spellIds, tangible.Action1Param<spellInfo> fix) {
        for (int spellId : spellIds) {
            var range = _GetSpellInfo(spellId);

            if (range.isEmpty()) {
                Log.outError(LogFilter.ServerLoading, String.format("Spell info correction specified for non-existing spell %1$s", spellId));

                continue;
            }

            for (var spellInfo : range.values()) {
                fix.invoke(spellInfo);
            }
        }
    }

    private void applySpellEffectFix(SpellInfo spellInfo, int effectIndex, tangible.Action1Param<SpellEffectInfo> fix) {
        if (spellInfo.getEffects().size() <= effectIndex) {
            Log.outError(LogFilter.ServerLoading, String.format("Spell effect info correction specified for non-existing effect %1$s of spell %2$s", effectIndex, spellInfo.getId()));

            return;
        }

        fix.invoke(spellInfo.getEffect(effectIndex));
    }

    public void loadSpellInfosLateFix() {
        for (var fix : IOHelpers.<ISpellManagerSpellLateFix>GetAllObjectsFromAssemblies(Paths.get(AppContext.BaseDirectory).resolve("Scripts").toString())) {
            applySpellFix(fix.getSpellIds(), fix::ApplySpellFix);
        }
    }

    public void loadSpellInfoCorrections() {
        var oldMSTime = System.currentTimeMillis();

        for (var fix : IOHelpers.<ISpellManagerSpellFix>GetAllObjectsFromAssemblies(Paths.get(AppContext.BaseDirectory).resolve("Scripts").toString())) {
            applySpellFix(fix.getSpellIds(), fix::ApplySpellFix);
        }

        {
            // Some spells have no amplitude set
            applySpellFix(new int[]{6727, 7331, 34589, 52562, 57550, 65755}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.applyAuraPeriod = 1 * time.InMilliseconds;
                });
            });

            applySpellFix(new int[]{24707, 26263, 29055}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
                {
                    spellEffectInfo.applyAuraPeriod = 1 * time.InMilliseconds;
                    ;
                });
            });

            // Karazhan - Chess NPC AI, action timer
            applySpellFix(new int[]{37504}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
                {
                    spellEffectInfo.applyAuraPeriod = 5 * time.InMilliseconds;
                    ;
                });
            });

            // Vomit
            applySpellFix(new int[]{43327}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
                {
                    spellEffectInfo.applyAuraPeriod = 1 * time.InMilliseconds;
                    ;
                });
            });
        }

        {
            // specific code for cases with no trigger spell provided in field
            // Brood Affliction: Bronze
            applySpellFix(new int[]{23170}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.triggerSpell = 23171;
                });
            });

            // Feed Captured Animal
            applySpellFix(new int[]{29917}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.triggerSpell = 29916;
                });
            });

            // Remote Toy
            applySpellFix(new int[]{37027}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.triggerSpell = 37029;
                });
            });

            // Eye of Grillok
            applySpellFix(new int[]{38495}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.triggerSpell = 38530;
                });
            });

            // Tear of Azzinoth Summon Channel - it's not really supposed to do anything, and this only prevents the console spam
            applySpellFix(new int[]{39857}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.triggerSpell = 39856;
                });
            });

            // Personalized Weather
            applySpellFix(new int[]{46736}, spellInfo ->
            {
                applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
                {
                    spellEffectInfo.triggerSpell = 46737;
                    spellEffectInfo.applyAuraName = AuraType.PeriodicTriggerSpell;
                });
            });
        }

        // Allows those to crit
        applySpellFix(new int[]{379, 71607, 71646, 71610, 71641}, spellInfo ->
        {
            // We need more spells to find a general way (if there is any)
            spellInfo.dmgClass = SpellDmgClass.Magic;
        });

        applySpellFix(new int[]{63026, 63137}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.DestDb);
            });
        });

        // Summon Skeletons
        applySpellFix(new int[]{52611, 52612}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.miscValueB = 64;
            });
        });

        applySpellFix(new int[]{40244, 40245, 40246, 40247, 42835}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.NONE;
            });
        });

        applySpellFix(new int[]{63665, 31298, 51904, 68933, 29200}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitCaster);
                spellEffectInfo.targetB = new spellImplicitTargetInfo();
            });
        });

        applySpellFix(new int[]{56690, 60586, 60776, 60881, 60864}, spellInfo ->
        {
            spellInfo.AttributesEx4 |= SpellAttr4.IgnoreDamageTakenModifiers;
        });

        // Howl of Azgalor
        applySpellFix(new int[]{31344}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards100); // 100yards instead of 50000?!
            });
        });

        applySpellFix(new int[]{42818, 42821}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(6); // 100 yards
        });

        // They Must Burn Bomb aura (self)
        applySpellFix(new int[]{36350}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.triggerSpell = 36325; // They Must Burn Bomb Drop (DND)
            });
        });

        applySpellFix(new int[]{31347, 36327, 39365, 41071, 42442, 42611, 44978, 45001, 45002, 45004, 45006, 45010, 45761, 45863, 48246, 41635, 44869, 45027, 45976, 52124, 52479, 61588, 55479, 28560, 53096, 70743, 70614, 4020, 52438, 52449, 53609, 53457, 45907, 52953, 58121, 43109, 58552, 58533, 21855, 38762, 51122, 71848, 36146, 33711, 38794}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 1;
        });

        applySpellFix(new int[]{36384, 47731}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 2;
        });

        applySpellFix(new int[]{28542, 29213, 29576, 37790, 39992, 40816, 41303, 41376, 45248, 46771, 66588}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 3;
        });

        applySpellFix(new int[]{38310, 53385}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 4;
        });

        applySpellFix(new int[]{42005, 38296, 37676, 46008, 45641, 55665, 28796, 37135}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 5;
        });

        applySpellFix(new int[]{40827, 40859, 40860, 40861, 54098, 54835}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 10;
        });

        // Unholy Frenzy
        applySpellFix(new int[]{50312}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 15;
        });

        // Fingers of Frost
        applySpellFix(new int[]{44544}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.SpellClassMask[0] |= 0x20000;
            });
        });

        applySpellFix(new int[]{52212, 41485, 41487}, spellInfo ->
        {
            spellInfo.AttributesEx6 |= SpellAttr6.IgnorePhaseShift;
        });

        // Oscillation Field
        applySpellFix(new int[]{37408}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.DotStackingRule;
        });

        // Crafty's Ultra-Advanced Proto-Typical Shortening Blaster
        applySpellFix(new int[]{51912}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.applyAuraPeriod = 3000;
            });
        });

        // Nether Portal - Perseverence
        applySpellFix(new int[]{30421}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 2, spellEffectInfo ->
            {
                spellEffectInfo.basePoints += 30000;
            });
        });

        // Parasitic Shadowfiend Passive
        applySpellFix(new int[]{41913}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.applyAuraName = AuraType.DUMMY; // proc debuff, and summon infinite fiends
            });
        });

        applySpellFix(new int[]{27892, 27928, 27935}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards10);
            });
        });

        // Wrath of the Plaguebringer
        applySpellFix(new int[]{29214, 54836}, spellInfo ->
        {
            // target allys instead of enemies, target A is src_caster, spells with effect like that have ally target
            // this is the only known exception, probably just wrong data
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.UnitSrcAreaAlly);
            });
            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.UnitSrcAreaAlly);
            });
        });

        // Earthbind totem (instant pulse)
        applySpellFix(new int[]{6474}, spellInfo ->
        {
            spellInfo.AttributesEx5 |= SpellAttr5.ExtraInitialPeriod;
        });

        applySpellFix(new int[]{70728, 70840}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitCaster);
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.UnitPet);
            });
        });

        // Ride Carpet
        applySpellFix(new int[]{45602}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.basePoints = 0; // force seat 0, vehicle doesn't have the required seat flags for "no seat specified (-1)"
            });
        });

        // Easter Lay Noblegarden Egg Aura - Interrupt flags copied from aura which this aura is linked with
        applySpellFix(new int[]{61719}, spellInfo ->
        {
            spellInfo.auraInterruptFlags = SpellAuraInterruptFlag.forValue(SpellAuraInterruptFlag.HostileActionReceived.getValue() | SpellAuraInterruptFlag.damage.getValue());
        });

        applySpellFix(new int[]{71838, 71839}, spellInfo ->
        {
            spellInfo.AttributesEx2 |= SpellAttr2.CantCrit;
        });

        applySpellFix(new int[]{51597, 56606, 61791}, spellInfo ->
        {
            /** @todo: remove this when basepoints of all Ride Vehicle auras are calculated correctly
             */
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.basePoints = 1;
            });
        });

        // Summon Scourged Captive
        applySpellFix(new int[]{51597}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.scaling.variance = 0.0f;
            });
        });

        // Black Magic
        applySpellFix(new int[]{59630}, spellInfo ->
        {
            spellInfo.Attributes |= SpellAttr0.Passive;
        });

        // Paralyze
        applySpellFix(new int[]{48278}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.DotStackingRule;
        });

        applySpellFix(new int[]{51798, 47134}, spellInfo ->
        {
            //! HACK: This spell break quest complete for alliance and on retail not used
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.NONE;
            });
        });

        // Siege Cannon (Tol Barad)
        applySpellFix(new int[]{85123}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards200);
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitSrcAreaEntry);
            });
        });

        // Gathering Storms
        applySpellFix(new int[]{198300}, spellInfo ->
        {
            spellInfo.procCharges = 1; // override proc charges, has 0 (unlimited) in db2
        });

        applySpellFix(new int[]{15538, 42490, 42492, 43115}, spellInfo ->
        {
            spellInfo.AttributesEx |= SpellAttr1.NoThreat;
        });

        // Test Ribbon Pole Channel
        applySpellFix(new int[]{29726}, spellInfo ->
        {
            spellInfo.channelInterruptFlags &= ~SpellAuraInterruptFlag.action;
        });

        // Sic'em
        applySpellFix(new int[]{42767}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitNearbyEntry);
            });
        });

        // Burn Body
        applySpellFix(new int[]{42793}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 2, spellEffectInfo ->
            {
                spellEffectInfo.miscValue = 24008; // Fallen Combatant
            });
        });

        // Gift of the Naaru (priest and monk variants)
        applySpellFix(new int[]{59544, 121093}, spellInfo ->
        {
            spellInfo.SpellFamilyFlags[2] = 0x80000000;
        });

        applySpellFix(new int[]{50661, 68979, 48714, 7853}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(13); // 50000yd
        });

        applySpellFix(new int[]{44327, 44408}, spellInfo ->
        {
            spellInfo.speed = 0.0f;
        });

        // Summon Corpse Scarabs
        applySpellFix(new int[]{28864, 29105}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards10);
            });
        });

        // Tag Greater Felfire Diemetradon
        applySpellFix(new int[]{37851, 37918}, spellInfo ->
        {
            spellInfo.recoveryTime = 3000;
        });

        // Jormungar Strike
        applySpellFix(new int[]{56513}, spellInfo ->
        {
            spellInfo.recoveryTime = 2000;
        });

        applySpellFix(new int[]{54997, 56524}, spellInfo ->
        {
            spellInfo.recoveryTime = 6000;
        });

        applySpellFix(new int[]{47911, 48620, 51752}, spellInfo ->
        {
            spellInfo.recoveryTime = 10000;
        });

        applySpellFix(new int[]{37727, 54996}, spellInfo ->
        {
            spellInfo.recoveryTime = 12000;
        });

        // Signal Helmet to Attack
        applySpellFix(new int[]{51748}, spellInfo ->
        {
            spellInfo.recoveryTime = 15000;
        });

        // Charge
        applySpellFix(new int[]{51756, 37919, 37917}, spellInfo ->
        {
            spellInfo.recoveryTime = 20000;
        });

        // Summon Frigid Bones
        applySpellFix(new int[]{53525}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(4); // 2 minutes
        });

        // Dark Conclave Ritualist Channel
        applySpellFix(new int[]{38469}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(6); // 100yd
        });

        // Chrono Shift (enemy slow part)
        applySpellFix(new int[]{236299}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(6); // 100yd
        });

        //
        // VIOLET HOLD SPELLS
        //
        // Water Globule (Ichoron)
        applySpellFix(new int[]{54258, 54264, 54265, 54266, 54267}, spellInfo ->
        {
            // in 3.3.5 there is only one radius in dbc which is 0 yards in this case
            // use max radius from 4.3.4
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards25);
            });
        });
        // ENDOF VIOLET HOLD

        //
        // ULDUAR SPELLS
        //
        // Pursued (Flame Leviathan)
        applySpellFix(new int[]{62374}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards50000); // 50000yd
            });
        });

        // Focused Eyebeam Summon trigger (Kologarn)
        applySpellFix(new int[]{63342}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 1;
        });

        applySpellFix(new int[]{65584, 64381}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.DotStackingRule;
        });

        applySpellFix(new int[]{63018, 65121, 63024, 64234}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 1;
        });

        applySpellFix(new int[]{64386, 64389, 64678}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(28); // 5 seconds, wrong DBC data?
        });

        // Potent Pheromones (Freya)
        applySpellFix(new int[]{64321}, spellInfo ->
        {
            // spell should dispel area aura, but doesn't have the attribute
            // may be db data bug, or blizz may keep reapplying area auras every update with checking immunity
            // that will be clear if we get more spells with problem like this
            spellInfo.AttributesEx |= SpellAttr1.ImmunityPurgesEffect;
        });

        // Blizzard (Thorim)
        applySpellFix(new int[]{62576, 62602}, spellInfo ->
        {
            // DBC data is wrong for 0, it's a different dynobject target than 1
            // Both effects should be shared by the same DynObject
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.DestCasterLeft);
            });
        });

        // Spinning Up (Mimiron)
        applySpellFix(new int[]{63414}, spellInfo ->
        {
            spellInfo.channelInterruptFlags = SpellAuraInterruptFlag.NONE;
            spellInfo.channelInterruptFlags2 = SpellAuraInterruptFlag2.NONE;
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.UnitCaster);
            });
        });

        // Rocket Strike (Mimiron)
        applySpellFix(new int[]{63036}, spellInfo ->
        {
            spellInfo.speed = 0;
        });

        // Magnetic Field (Mimiron)
        applySpellFix(new int[]{64668}, spellInfo ->
        {
            spellInfo.mechanic = mechanics.NONE;
        });

        // Empowering Shadows (Yogg-Saron)
        applySpellFix(new int[]{64468, 64486}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 3; // same for both modes?
        });

        // Cosmic Smash (Algalon the Observer)
        applySpellFix(new int[]{62301}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 1;
        });

        // Cosmic Smash (Algalon the Observer)
        applySpellFix(new int[]{64598}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 3;
        });

        // Cosmic Smash (Algalon the Observer)
        applySpellFix(new int[]{62293}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.DestCaster);
            });
        });

        // Cosmic Smash (Algalon the Observer)
        applySpellFix(new int[]{62311, 64596}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(6); // 100yd
        });

        applySpellFix(new int[]{64014, 64024, 64025, 64028, 64029, 64030, 64031, 64032, 65042}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.DestDb);
            });
        });
        // ENDOF ULDUAR SPELLS

        //
        // TRIAL OF THE CRUSADER SPELLS
        //
        // Infernal Eruption
        applySpellFix(new int[]{66258}, spellInfo ->
        {
            // increase duration from 15 to 18 seconds because caster is already
            // unsummoned when spell missile hits the ground so nothing happen in result
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(85);
        });
        // ENDOF TRIAL OF THE CRUSADER SPELLS

        //
        // ICECROWN CITADEL SPELLS
        //
        applySpellFix(new int[]{70781, 70856, 70857, 70858, 70859, 70860, 70861}, spellInfo ->
        {
            // THESE SPELLS ARE WORKING CORRECTLY EVEN WITHOUT THIS HACK
            // THE ONLY REASON ITS HERE IS THAT CURRENT GRID SYSTEM
            // DOES NOT ALLOW FAR OBJECT SELECTION (dist > 333)
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.DestDb);
            });
        });

        // Shadow's Fate
        applySpellFix(new int[]{71169}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.DotStackingRule;
        });

        // Resistant Skin (Deathbringer Saurfang adds)
        applySpellFix(new int[]{72723}, spellInfo ->
        {
            // this spell initially granted Shadow damage immunity, however it was removed but the data was left in client
            applySpellEffectFix(spellInfo, 2, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.NONE;
            });
        });

        // Coldflame Jets (Traps after Saurfang)
        applySpellFix(new int[]{70460}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(1); // 10 seconds
        });

        applySpellFix(new int[]{71412, 71415}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitTargetAny);
            });
        });

        // Awaken Plagued Zombies
        applySpellFix(new int[]{71159}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(21);
        });

        // Volatile Ooze Beam Protection (Professor Putricide)
        applySpellFix(new int[]{70530}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.ApplyAura; // for an unknown reason this was SPELL_EFFECT_APPLY_AREA_AURA_RAID
            });
        });

        // Mutated Strength (Professor Putricide)
        applySpellFix(new int[]{71604}, spellInfo ->
        {
            // THIS IS HERE BECAUSE COOLDOWN ON CREATURE PROCS WERE NOT IMPLEMENTED WHEN THE SCRIPT WAS WRITTEN
            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.NONE;
            });
        });

        // Unbound Plague (Professor Putricide) (needs target selection script)
        applySpellFix(new int[]{70911}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.UnitTargetEnemy);
            });
        });

        // Empowered Flare (Blood Prince Council)
        applySpellFix(new int[]{71708}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.IgnoreCasterModifiers;
        });

        // Swarming Shadows
        applySpellFix(new int[]{71266}, spellInfo ->
        {
            spellInfo.requiredAreasId = 0; // originally, these require area 4522, which is... outside of Icecrown Citadel
        });

        // Corruption
        applySpellFix(new int[]{70602}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.DotStackingRule;
        });

        // Column of Frost (visual marker)
        applySpellFix(new int[]{70715}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(32); // 6 seconds (missing)
        });

        // Mana Void (periodic aura)
        applySpellFix(new int[]{71085}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(9); // 30 seconds (missing)
        });

        // Summon Suppressor (needs target selection script)
        applySpellFix(new int[]{70936}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(157); // 90yd

            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitTargetAny);
                spellEffectInfo.targetB = new spellImplicitTargetInfo();
            });
        });

        // Sindragosa's Fury
        applySpellFix(new int[]{70598}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.DestDest);
            });
        });

        // Frost Bomb
        applySpellFix(new int[]{69846}, spellInfo ->
        {
            spellInfo.speed = 0.0f; // This spell's summon happens instantly
        });

        // Chilled to the Bone
        applySpellFix(new int[]{70106}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.IgnoreCasterModifiers;
            spellInfo.AttributesEx6 |= SpellAttr6.IgnoreCasterDamageModifiers;
        });

        // Ice Lock
        applySpellFix(new int[]{71614}, spellInfo ->
        {
            spellInfo.mechanic = mechanics.Stun;
        });

        // Defile
        applySpellFix(new int[]{72762}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(559); // 53 seconds
        });

        // Defile
        applySpellFix(new int[]{72743}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(22); // 45 seconds
        });

        // Defile
        applySpellFix(new int[]{72754}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards200); // 200yd
            });

            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards200); // 200yd
            });
        });

        // Val'kyr Target Search
        applySpellFix(new int[]{69030}, spellInfo ->
        {
            spellInfo.Attributes |= SpellAttr0.NoImmunities;
        });

        // Raging Spirit Visual
        applySpellFix(new int[]{69198}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(13); // 50000yd
        });

        // Harvest Soul
        applySpellFix(new int[]{73655}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.IgnoreCasterModifiers;
        });

        // Summon Shadow Trap
        applySpellFix(new int[]{73540}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(3); // 60 seconds
        });

        // Shadow trap (visual)
        applySpellFix(new int[]{73530}, spellInfo ->
        {
            spellInfo.durationEntry = CliDB.SpellDurationStorage.get(27); // 3 seconds
        });

        // Summon Spirit Bomb
        applySpellFix(new int[]{74302}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 2;
        });

        // Summon Spirit Bomb
        applySpellFix(new int[]{73579}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards25); // 25yd
            });
        });

        // Raise Dead
        applySpellFix(new int[]{72376}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 3;
        });

        // Jump
        applySpellFix(new int[]{71809}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(5); // 40yd

            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards10); // 10yd
                spellEffectInfo.miscValue = 190;
            });
        });

        // Broken Frostmourne
        applySpellFix(new int[]{72405}, spellInfo ->
        {
            spellInfo.AttributesEx |= SpellAttr1.NoThreat;

            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards20); // 20yd
            });
        });
        // ENDOF ICECROWN CITADEL SPELLS

        //
        // RUBY SANCTUM SPELLS
        //
        // Soul Consumption
        applySpellFix(new int[]{74799}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.radiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards12);
            });
        });

        // Twilight Mending
        applySpellFix(new int[]{75509}, spellInfo ->
        {
            spellInfo.AttributesEx6 |= SpellAttr6.IgnorePhaseShift;
            spellInfo.AttributesEx2 |= SpellAttr2.IgnoreLineOfSight;
        });

        // Awaken Flames
        applySpellFix(new int[]{75888}, spellInfo ->
        {
            spellInfo.AttributesEx |= SpellAttr1.ExcludeCaster;
        });
        // ENDOF RUBY SANCTUM SPELLS

        //
        // EYE OF ETERNITY SPELLS
        //
        applySpellFix(new int[]{57473, 57431, 56091, 56092, 57090, 57143}, spellInfo ->
        {
            // All spells work even without these changes. The LOS attribute is due to problem
            // from collision between maps & gos with active destroyed state.
            spellInfo.AttributesEx2 |= SpellAttr2.IgnoreLineOfSight;
        });

        // Arcane Barrage (cast by players and NONMELEEDAMAGELOG with caster Scion of Eternity (original caster)).
        applySpellFix(new int[]{63934}, spellInfo ->
        {
            // This would never crit on retail and it has attribute for SPELL_ATTR3_NO_DONE_BONUS because is handled from player,
            // until someone figures how to make scions not critting without hack and without making them main casters this should stay here.
            spellInfo.AttributesEx2 |= SpellAttr2.CantCrit;
        });
        // ENDOF EYE OF ETERNITY SPELLS

        applySpellFix(new int[]{40055, 40165, 40166, 40167}, spellInfo ->
        {
            spellInfo.Attributes |= SpellAttr0.AuraIsDebuff;
        });

        //
        // STONECORE SPELLS
        //
        applySpellFix(new int[]{95284, 95285}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetB = new spellImplicitTargetInfo(targets.DestDb);
            });
        });
        // ENDOF STONECORE SPELLS

        //
        // HALLS OF ORIGINATION SPELLS
        //
        applySpellFix(new int[]{76606, 76608}, spellInfo ->
        {
            // Little hack, Increase the radius so it can hit the Cave In Stalkers in the platform.
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.maxRadiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards45);
            });
        });

        // ENDOF HALLS OF ORIGINATION SPELLS

        // Threatening Gaze
        applySpellFix(new int[]{24314}, spellInfo ->
        {
            spellInfo.auraInterruptFlags |= SpellAuraInterruptFlag.forValue(SpellAuraInterruptFlag.action.getValue() | SpellAuraInterruptFlag.Moving.getValue().getValue() | SpellAuraInterruptFlag.Anim.getValue().getValue());
        });

        // Travel Form (dummy) - cannot be cast indoors.
        applySpellFix(new int[]{783}, spellInfo ->
        {
            spellInfo.Attributes |= SpellAttr0.OnlyOutdoors;
        });

        // Tree of Life (Passive)
        applySpellFix(new int[]{5420}, spellInfo ->
        {
            spellInfo.stances = 1 << (ShapeShiftForm.treeOfLife.getValue() - 1);
        });

        // Gaze of Occu'thar
        applySpellFix(new int[]{96942}, spellInfo ->
        {
            spellInfo.AttributesEx &= ~SpellAttr1.IsChannelled;
        });

        // Evolution
        applySpellFix(new int[]{75610}, spellInfo ->
        {
            spellInfo.maxAffectedTargets = 1;
        });

        // Evolution
        applySpellFix(new int[]{75697}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitSrcAreaEntry);
            });
        });

        //
        // ISLE OF CONQUEST SPELLS
        //
        // Teleport
        applySpellFix(new int[]{66551}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(13); // 50000yd
        });
        // ENDOF ISLE OF CONQUEST SPELLS

        // Aura of Fear
        applySpellFix(new int[]{40453}, spellInfo ->
        {
            // Bad DBC data? Copying 25820 here due to spell description
            // either is a periodic with chance on tick, or a proc

            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.applyAuraName = AuraType.ProcTriggerSpell;
                spellEffectInfo.applyAuraPeriod = 0;
            });

            spellInfo.procChance = 10;
        });

        // Survey Sinkholes
        applySpellFix(new int[]{45853}, spellInfo ->
        {
            spellInfo.rangeEntry = CliDB.SpellRangeStorage.get(5); // 40 yards
        });

        // Baron Rivendare (Stratholme) - Unholy Aura
        applySpellFix(new int[]{17466, 17467}, spellInfo ->
        {
            spellInfo.AttributesEx2 |= SpellAttr2.NoInitialThreat;
        });

        // Spore - Spore Visual
        applySpellFix(new int[]{42525}, spellInfo ->
        {
            spellInfo.AttributesEx3 |= SpellAttr3.AllowAuraWhileDead;
            spellInfo.AttributesEx2 |= SpellAttr2.AllowDeadTarget;
        });

        // Soul sickness (Forge of Souls)
        applySpellFix(new int[]{69131}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.applyAuraName = AuraType.ModDecreaseSpeed;
            });
        });

        //
        // FIRELANDS SPELLS
        //
        // Torment Searcher
        applySpellFix(new int[]{99253}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.maxRadiusEntry = CliDB.SpellRadiusStorage.get(EffectRadiusIndex.Yards15);
            });
        });

        // Torment Damage
        applySpellFix(new int[]{99256}, spellInfo ->
        {
            spellInfo.Attributes |= SpellAttr0.AuraIsDebuff;
        });

        // Blaze of Glory
        applySpellFix(new int[]{99252}, spellInfo ->
        {
            spellInfo.auraInterruptFlags |= SpellAuraInterruptFlag.LeaveWorld;
        });
        // ENDOF FIRELANDS SPELLS

        //
        // ANTORUS THE BURNING THRONE SPELLS
        //

        // Decimation
        applySpellFix(new int[]{244449}, spellInfo ->
        {
            // For some reason there is a instakill effect that serves absolutely no purpose.
            // Until we figure out what it's actually used for we disable it.
            applySpellEffectFix(spellInfo, 2, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.NONE;
            });
        });

        // ENDOF ANTORUS THE BURNING THRONE SPELLS

        // Summon Master Li Fei
        applySpellFix(new int[]{102445}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.DestDb);
            });
        });

        // Earthquake
        applySpellFix(new int[]{61882}, spellInfo ->
        {
            spellInfo.negativeEffects.add(2);
        });

        // Headless Horseman Climax - Return Head (Hallow End)
        // Headless Horseman Climax - Body Regen (confuse only - removed on death)
        // Headless Horseman Climax - Head Is Dead
        applySpellFix(new int[]{42401, 43105, 42428}, spellInfo ->
        {
            spellInfo.Attributes |= SpellAttr0.NoImmunities;
        });

        // Horde / Alliance switch (BG mercenary system)
        applySpellFix(new int[]{195838, 195843}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.ApplyAura;
            });
            applySpellEffectFix(spellInfo, 1, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.ApplyAura;
            });
            applySpellEffectFix(spellInfo, 2, spellEffectInfo ->
            {
                spellEffectInfo.effect = SpellEffectName.ApplyAura;
            });
        });

        // Fire Cannon
        applySpellFix(new int[]{181593}, spellInfo ->
        {
            applySpellEffectFix(spellInfo, 0, spellEffectInfo ->
            {
                // This spell never triggers, theory is that it was supposed to be only triggered until target reaches some health percentage
                // but was broken and always caused visuals to break, then target was changed to immediately spawn with desired health
                // leaving old data in db2
                spellEffectInfo.triggerSpell = 0;
            });
        });

        // Ray of Frost (Fingers of Frost charges)
        applySpellFix(new int[]{269748}, spellInfo ->
        {
            spellInfo.AttributesEx &= ~SpellAttr1.IsChannelled;
        });

        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                // Fix range for trajectory triggered spell
                for (var spellEffectInfo : spellInfo.effects) {
                    if (spellEffectInfo.isEffect() && (spellEffectInfo.targetA.target == targets.DestTraj || spellEffectInfo.targetB.target == targets.DestTraj)) {
                        // Get triggered spell if any
                        for (var spellInfoTrigger : _GetSpellInfo(spellEffectInfo.triggerSpell).values()) {
                            var maxRangeMain = spellInfo.getMaxRange();
                            var maxRangeTrigger = spellInfoTrigger.getMaxRange();

                            // check if triggered spell has enough max range to cover trajectory
                            if (maxRangeTrigger < maxRangeMain) {
                                spellInfoTrigger.rangeEntry = spellInfo.rangeEntry;
                            }
                        }
                    }

                    switch (spellEffectInfo.effect) {
                        case SpellEffectName.Charge:
                        case SpellEffectName.ChargeDest:
                        case SpellEffectName.Jump:
                        case SpellEffectName.JumpDest:
                        case SpellEffectName.LeapBack:
                            if (spellInfo.speed == 0 && spellInfo.spellFamilyName == 0 && !spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                                spellInfo.speed = MotionMaster.SPEED_CHARGE;
                            }

                            break;
                    }

                    if (spellEffectInfo.targetA.selectionCategory == SpellTargetSelectionCategories.Cone || spellEffectInfo.targetB.selectionCategory == SpellTargetSelectionCategories.Cone) {
                        if (MathUtil.fuzzyEq(spellInfo.coneAngle, 0.0f)) {
                            spellInfo.coneAngle = 90.0f;
                        }
                    }

                    // Area auras may not target area (they're self cast)
                    if (spellEffectInfo.IsAreaAuraEffect && spellEffectInfo.IsTargetingArea) {
                        spellEffectInfo.targetA = new spellImplicitTargetInfo(targets.UnitCaster);
                        spellEffectInfo.targetB = new spellImplicitTargetInfo();
                    }
                }

                // disable proc for magnet auras, they're handled differently
                if (spellInfo.hasAura(AuraType.SpellMagnet)) {
                    spellInfo.procFlags = new EnumFlag<ProcFlag>();
                }

                // due to the way spell system works, unit would change orientation in Spell::_cast
                if (spellInfo.hasAura(AuraType.ControlVehicle)) {
                    spellInfo.AttributesEx5 |= SpellAttr5.AiDoesntFaceTarget;
                }

                if (spellInfo.activeIconFileDataId == 135754) // flight
                {
                    spellInfo.Attributes |= SpellAttr0.Passive;
                }

                if (spellInfo.isSingleTarget() && spellInfo.maxAffectedTargets == 0) {
                    spellInfo.maxAffectedTargets = 1;
                }
            }
        }

        var properties = CliDB.SummonPropertiesStorage.get(121);

        if (properties != null) {
            properties.title = SummonTitle.totem;
        }

        properties = CliDB.SummonPropertiesStorage.get(647); // 52893

        if (properties != null) {
            properties.title = SummonTitle.totem;
        }

        properties = CliDB.SummonPropertiesStorage.get(628);

        if (properties != null) // Hungry Plaguehound
        {
            properties.Control = SummonCategory.pet;
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded SpellInfo corrections in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellInfoSpellSpecificAndAuraState() {
        var oldMSTime = System.currentTimeMillis();

        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                // AuraState depends on SpellSpecific
                spellInfo._LoadSpellSpecific();
                spellInfo._LoadAuraState();
            }
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded SpellInfo SpellSpecific and AuraState in %1$s ms", time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public void loadSpellInfoDiminishing() {
        var oldMSTime = System.currentTimeMillis();

        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                if (spellInfo == null) {
                    continue;
                }

                spellInfo._LoadSpellDiminishInfo();
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded SpellInfo diminishing infos in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadSpellInfoImmunities() {
        var oldMSTime = System.currentTimeMillis();

        for (var kvp : spellInfoMap.values()) {
            for (var spellInfo : kvp.VALUES) {
                if (spellInfo == null) {
                    continue;
                }

                spellInfo._LoadImmunityInfo();
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded SpellInfo immunity infos in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    public void loadPetFamilySpellsStore() {
        HashMap<Integer, SpellLevelsRecord> levelsBySpell = new HashMap<Integer, SpellLevelsRecord>();

        for (var levels : CliDB.SpellLevelsStorage.values()) {
            if (levels.difficultyID == 0) {
                levelsBySpell.put(levels.spellID, levels);
            }
        }

        for (var skillLine : CliDB.SkillLineAbilityStorage.values()) {
            var spellInfo = getSpellInfo(skillLine.spell, Difficulty.NONE);

            if (spellInfo == null) {
                continue;
            }

            var levels = levelsBySpell.get(skillLine.spell);

            if (levels != null && levels.spellLevel != 0) {
                continue;
            }

            if (spellInfo.isPassive) {
                for (var cFamily : CliDB.CreatureFamilyStorage.values()) {
                    if (skillLine.skillLine != cFamily.SkillLine[0] && skillLine.skillLine != cFamily.SkillLine[1]) {
                        continue;
                    }

                    if (skillLine.AcquireMethod != AbilityLearnType.OnSkillLearn) {
                        continue;
                    }

                    global.getSpellMgr().petFamilySpellsStorage.add(cFamily.id, spellInfo.id);
                }
            }
        }
    }

    public void loadSpellTotemModel() {
        var oldMSTime = System.currentTimeMillis();

        var result = DB.World.query("SELECT spellID, raceID, DisplayID from spell_totem_model");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell totem model records. DB table `spell_totem_model` is empty.");

            return;
        }

        int count = 0;

        do {
            var spellId = result.<Integer>Read(0);
            var race = result.<Byte>Read(1);
            var displayId = result.<Integer>Read(2);

            var spellEntry = getSpellInfo(spellId, Difficulty.NONE);

            if (spellEntry == null) {
                Logs.SQL.error(String.format("SpellID: %1$s in `spell_totem_model` table could not be found in dbc, skipped.", spellId));

                continue;
            }

            if (!CliDB.ChrRacesStorage.containsKey(race)) {
                Logs.SQL.error(String.format("Race %1$s defined in `spell_totem_model` does not exists, skipped.", race));

                continue;
            }

            if (!CliDB.CreatureDisplayInfoStorage.containsKey(displayId)) {
                Logs.SQL.error(String.format("SpellID: %1$s defined in `spell_totem_model` has non-existing model (%2$s).", spellId, displayId));

                continue;
            }

            spellTotemModel.put(Tuple.create(spellId, race), displayId);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s spell totem model records in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    @FunctionalInterface
    public interface AuraEffectHandler {
        void invoke(AuraEffect effect, AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply);
    }

    @FunctionalInterface
    public interface SpellEffectHandler {
        void invoke(Spell spell);
    }


    ///#endregion
}
