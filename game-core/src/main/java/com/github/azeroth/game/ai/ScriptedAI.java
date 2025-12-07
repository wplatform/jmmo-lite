package game.ai;

import Framework.Constants.*;
import game.datastorage.*;
import game.entities.*;
import game.maps.*;
import game.spells.*;
import game.*;
import java.util.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class ScriptedAI extends CreatureAI {
    private final Difficulty difficulty = Difficulty.values()[0];
    private final boolean isHeroic;
    private boolean isCombatMovementAllowed;

    public ScriptedAI(Creature creature) {
        super(creature);
        isCombatMovementAllowed = true;
        isHeroic = me.getMap().isHeroic();
        difficulty = me.getMap().getDifficultyID();
    }

    public final void attackStartNoMove(Unit target) {
        if (target == null) {
            return;
        }

        if (me.attack(target, true)) {
            doStartNoMovement(target);
        }
    }

    // Called before JustEngagedWith even before the creature is in combat.
    @Override
    public void attackStart(Unit target) {
        if (isCombatMovementAllowed()) {
            super.attackStart(target);
        } else {
            attackStartNoMove(target);
        }
    }
    //Called at World update tick
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        //Check if we have a current target
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }

    //Start movement toward victim

    public final void doStartMovement(Unit target, float distance) {
        doStartMovement(target, distance, 0.0f);
    }

    public final void doStartMovement(Unit target) {
        doStartMovement(target, 0.0f, 0.0f);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void DoStartMovement(Unit target, float distance = 0.0f, float angle = 0.0f)
    public final void doStartMovement(Unit target, float distance, float angle) {
        if (target != null) {
            me.getMotionMaster().moveChase(target, distance, angle);
        }
    }

    //Start no movement on victim
    public final void doStartNoMovement(Unit target) {
        if (target == null) {
            return;
        }

        me.getMotionMaster().moveIdle();
    }

    //Stop attack of current victim
    public final void doStopAttack() {
        if (me.getVictim() != null) {
            me.attackStop();
        }
    }

    //Cast spell by spell info

    public final void doCastSpell(Unit target, SpellInfo spellInfo) {
        doCastSpell(target, spellInfo, false);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void DoCastSpell(Unit target, SpellInfo spellInfo, bool triggered = false)
    public final void doCastSpell(Unit target, SpellInfo spellInfo, boolean triggered) {
        if (target == null || me.isNonMeleeSpellCast(false)) {
            return;
        }

        me.stopMoving();
        me.castSpell(target, spellInfo.id, triggered);
    }

    //Plays a sound to all nearby players
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public static void DoPlaySoundToSet(WorldObject source, uint soundId)
    public static void doPlaySoundToSet(WorldObject source, int soundId) {
        if (source == null) {
            return;
        }

        if (!CliDB.soundKitStorage.containsKey(soundId)) {
            Log.outError(LogFilter.ScriptsAi, String.format("ScriptedAI::DoPlaySoundToSet: Invalid soundId %1$s used in DoPlaySoundToSet (Source: %2$s)", soundId, source.getGUID().clone()));

            return;
        }

        source.playDirectSound(soundId);
    }

    /** 
      Add specified amount of threat directly to victim (ignores redirection effects) - also puts victim in combat and engages them if necessary
     
     @param victim 
     @param amount 
     @param who 
    */

    public final void addThreat(Unit victim, double amount) {
        addThreat(victim, amount, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void AddThreat(Unit victim, double amount, Unit who = null)
    public final void addThreat(Unit victim, double amount, Unit who) {
        if (!victim) {
            return;
        }

        if (!who) {
            who = me;
        }

        who.getThreatManager().addThreat(victim, amount, null, true, true);
    }

    /** 
      Adds/removes the specified percentage from the specified victim's threat (to who, or me if not specified)
     
     @param victim 
     @param pct 
     @param who 
    */

    public final void modifyThreatByPercent(Unit victim, int pct) {
        modifyThreatByPercent(victim, pct, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void ModifyThreatByPercent(Unit victim, int pct, Unit who = null)
    public final void modifyThreatByPercent(Unit victim, int pct, Unit who) {
        if (!victim) {
            return;
        }

        if (!who) {
            who = me;
        }

        who.getThreatManager().modifyThreatByPercent(victim, pct);
    }

    /** 
      Resets the victim's threat level to who (or me if not specified) to zero
     
     @param victim 
     @param who 
    */
    public final void resetThreat(Unit victim, Unit who) {
        if (!victim) {
            return;
        }

        if (!who) {
            who = me;
        }

        who.getThreatManager().resetThreat(victim);
    }

    /** 
      Resets the specified unit's threat list (me if not specified) - does not delete entries, just sets their threat to zero
     
     @param who 
    */

    public final void resetThreatList() {
        resetThreatList(null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void ResetThreatList(Unit who = null)
    public final void resetThreatList(Unit who) {
        if (!who) {
            who = me;
        }

        who.getThreatManager().resetAllThreat();
    }

    /** 
      Returns the threat level of victim towards who (or me if not specified)
     
     @param victim 
     @param who 
     @return  
    */

    public final double getThreat(Unit victim) {
        return getThreat(victim, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public double GetThreat(Unit victim, Unit who = null)
    public final double getThreat(Unit victim, Unit who) {
        if (!victim) {
            return 0.0f;
        }

        if (!who) {
            who = me;
        }

        return who.getThreatManager().getThreat(victim);
    }

    //Spawns a creature relative to me
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public Creature DoSpawnCreature(uint entry, float offsetX, float offsetY, float offsetZ, float angle, TempSummonType type, TimeSpan despawntime)
    public final Creature doSpawnCreature(int entry, float offsetX, float offsetY, float offsetZ, float angle, TempSummonType type, TimeSpan despawntime) {
        return me.summonCreature(entry, new Position(me.location.x + offsetX, me.location.y + offsetY, me.location.z + offsetZ, angle), type, despawntime);
    }

    //Returns spells that meet the specified criteria from the creatures spell list
    public final SpellInfo selectSpell(Unit target, SpellSchoolMask school, Mechanics mechanic, SelectTargetType targets, float rangeMin, float rangeMax, SelectEffect effect) {
        //No target so we can't cast
        if (target == null) {
            return null;
        }

        //Silenced so we can't cast
        if (me.isSilenced(school == SpellSchoolMask.None ? SpellSchoolMask.Magic : school)) {
            return null;
        }

        //Using the extended script system we first create a list of viable spells
        var apSpell = new SpellInfo[SharedConst.MaxCreatureSpells];

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint spellCount = 0;
        int spellCount = 0;

        //Check if each spell is viable(set it to null if not)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: for (uint i = 0; i < SharedConst.MaxCreatureSpells; i++)
        for (int i = 0; i < SharedConst.MaxCreatureSpells; i++) {
            var tempSpell = Global.getSpellMgr().getSpellInfo(me.spells[i], me.getMap().getDifficultyID());
            var aiSpell = getAISpellInfo(me.spells[i], me.getMap().getDifficultyID());

            //This spell doesn't exist
            if (tempSpell == null || aiSpell == null) {
                continue;
            }

            // Targets and Effects checked first as most used restrictions
            //Check the spell targets if specified
            if (targets != 0 && !(boolean)(aiSpell.targets & (1 << (targets.getValue() - 1)))) {
                continue;
            }

            //Check the type of spell if we are looking for a specific spell type
            if (effect != 0 && !(boolean)(aiSpell.effects & (1 << (effect.getValue() - 1)))) {
                continue;
            }

            //Check for school if specified
            if (school != 0 && (tempSpell.schoolMask.getValue() & school.getValue()) == 0) {
                continue;
            }

            //Check for spell mechanic if specified
            if (mechanic != 0 && tempSpell.mechanic != mechanic) {
                continue;
            }

            // Continue if we don't have the mana to actually cast this spell
            var hasPower = true;

            for (var cost : tempSpell.calcPowerCost(me, tempSpell.getSchoolMask())) {
                if (cost.amount > me.getPower(cost.power)) {
                    hasPower = false;

                    break;
                }
            }

            if (!hasPower) {
                continue;
            }

            //Check if the spell meets our range requirements
            if (rangeMin != 0 && me.getSpellMinRangeForTarget(target, tempSpell) < rangeMin) {
                continue;
            }

            if (rangeMax != 0 && me.getSpellMaxRangeForTarget(target, tempSpell) > rangeMax) {
                continue;
            }

            //Check if our target is in range
            if (me.isWithinDistInMap(target, me.getSpellMinRangeForTarget(target, tempSpell)) || !me.isWithinDistInMap(target, me.getSpellMaxRangeForTarget(target, tempSpell))) {
                continue;
            }

            //All good so lets add it to the spell list
            apSpell[spellCount] = tempSpell;
            ++spellCount;
        }

        //We got our usable spells so now lets randomly pick one
        if (spellCount == 0) {
            return null;
        }

        return apSpell[RandomHelper.IRand(0, (int)(spellCount - 1))];
    }


    public final void doTeleportTo(float x, float y, float z) {
        doTeleportTo(x, y, z, 0);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void DoTeleportTo(float x, float y, float z, uint time = 0)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final void doTeleportTo(float x, float y, float z, int time) {
        me.location.relocate(x, y, z);
        var speed = me.getDistance(x, y, z) / (time * 0.001f);
        me.monsterMoveWithSpeed(x, y, z, speed);
    }

    public final void doTeleportTo(float[] position) {
        me.nearTeleportTo(position[0], position[1], position[2], position[3]);
    }

    //Teleports a player without dropping threat (only teleports to same map)
    public final void doTeleportPlayer(Unit unit, float x, float y, float z, float o) {
        if (unit == null) {
            return;
        }

        var player = unit.getAsPlayer();

        if (player != null) {
            player.teleportTo(unit.location.mapId, x, y, z, o, TeleportToOptions.NotLeaveCombat);
        } else {
            Log.outError(LogFilter.ScriptsAi, String.format("ScriptedAI::DoTeleportPlayer: Creature %1$s Tried to teleport non-player unit (%2$s) to X: %3$s Y: %4$s Z: %5$s O: %6$s. Aborted.", me.getGUID().clone(), unit.getGUID().clone(), x, y, z, o));
        }
    }

    public final void doTeleportAll(float x, float y, float z, float o) {
        var map = me.getMap();

        if (!map.isDungeon()) {
            return;
        }

        var playerList = map.getPlayers();

        for (var player : playerList) {
            if (player.isAlive()) {
                player.teleportTo(me.location.mapId, x, y, z, o, TeleportToOptions.NotLeaveCombat);
            }
        }
    }

    //Returns friendly unit with the most amount of hp missing from max hp

    public final Unit doSelectLowestHpFriendly(float range) {
        return doSelectLowestHpFriendly(range, 1);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Unit DoSelectLowestHpFriendly(float range, uint minHPDiff = 1)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final Unit doSelectLowestHpFriendly(float range, int minHPDiff) {
        var uCheck = new MostHPMissingInRange<Unit>(me, range, minHPDiff);
        var searcher = new UnitLastSearcher(me, uCheck, GridType.All);
        Cell.visitGrid(me, searcher, range);

        return searcher.getTarget();
    }

    //Returns a list of friendly CC'd units within range
    public final ArrayList<Creature> doFindFriendlyCC(float range) {
        ArrayList<Creature> list = new ArrayList<Creature>();
        var uCheck = new FriendlyCCedInRange(me, range);
        var searcher = new CreatureListSearcher(me, list, uCheck, GridType.All);
        Cell.visitGrid(me, searcher, range);

        return list;
    }

    //Returns a list of all friendly units missing a specific buff within range
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public List<Creature> DoFindFriendlyMissingBuff(float range, uint spellId)
    public final ArrayList<Creature> doFindFriendlyMissingBuff(float range, int spellId) {
        ArrayList<Creature> list = new ArrayList<Creature>();
        var uCheck = new FriendlyMissingBuffInRange(me, range, spellId);
        var searcher = new CreatureListSearcher(me, list, uCheck, GridType.All);
        Cell.visitGrid(me, searcher, range);

        return list;
    }

    //Return a player with at least minimumRange from me
    public final Player getPlayerAtMinimumRange(float minimumRange) {
        var check = new PlayerAtMinimumRangeAway(me, minimumRange);
        var searcher = new PlayerSearcher(me, check, GridType.World);
        Cell.visitGrid(me, searcher, minimumRange);

        return searcher.getTarget();
    }


    public final void setEquipmentSlots(boolean loadDefault, int mainHand, int offHand) {
        setEquipmentSlots(loadDefault, mainHand, offHand, -1);
    }

    public final void setEquipmentSlots(boolean loadDefault, int mainHand) {
        setEquipmentSlots(loadDefault, mainHand, -1, -1);
    }

    public final void setEquipmentSlots(boolean loadDefault) {
        setEquipmentSlots(loadDefault, -1, -1, -1);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void SetEquipmentSlots(bool loadDefault, int mainHand = -1, int offHand = -1, int ranged = -1)
    public final void setEquipmentSlots(boolean loadDefault, int mainHand, int offHand, int ranged) {
        if (loadDefault) {
            me.loadEquipment(me.getOriginalEquipmentId(), true);

            return;
        }

        if (mainHand >= 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Me.SetVirtualItem(0, (uint)mainHand);
            me.setVirtualItem(0, (int)mainHand);
        }

        if (offHand >= 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Me.SetVirtualItem(1, (uint)offHand);
            me.setVirtualItem(1, (int)offHand);
        }

        if (ranged >= 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: Me.SetVirtualItem(2, (uint)ranged);
            me.setVirtualItem(2, (int)ranged);
        }
    }

    // Used to control if MoveChase() is to be used or not in AttackStart(). Some creatures does not chase victims
    // NOTE: If you use SetCombatMovement while the creature is in combat, it will do NOTHING - This only affects AttackStart
    //       You should make the necessary to make it happen so.
    //       Remember that if you modified _isCombatMovementAllowed (e.g: using SetCombatMovement) it will not be reset at Reset().
    //       It will keep the last value you set.
    public final void setCombatMovement(boolean allowMovement) {
        isCombatMovementAllowed = allowMovement;
    }


    public static Creature getClosestCreatureWithEntry(WorldObject source, int entry, float maxSearchRange) {
        return getClosestCreatureWithEntry(source, entry, maxSearchRange, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public static Creature GetClosestCreatureWithEntry(WorldObject source, uint entry, float maxSearchRange, bool alive = true)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public static Creature getClosestCreatureWithEntry(WorldObject source, int entry, float maxSearchRange, boolean alive) {
        return source.findNearestCreature(entry, maxSearchRange, alive);
    }

    public static Creature getClosestCreatureWithOptions(WorldObject source, float maxSearchRange, FindCreatureOptions options) {
        return source.findNearestCreatureWithOptions(maxSearchRange, options.clone());
    }


    public static GameObject getClosestGameObjectWithEntry(WorldObject source, int entry, float maxSearchRange) {
        return getClosestGameObjectWithEntry(source, entry, maxSearchRange, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public static GameObject GetClosestGameObjectWithEntry(WorldObject source, uint entry, float maxSearchRange, bool spawnedOnly = true)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public static GameObject getClosestGameObjectWithEntry(WorldObject source, int entry, float maxSearchRange, boolean spawnedOnly) {
        return source.findNearestGameObject(entry, maxSearchRange, spawnedOnly);
    }

    public final boolean healthBelowPct(int pct) {
        return me.healthBelowPct(pct);
    }

    public final boolean healthAbovePct(int pct) {
        return me.healthAbovePct(pct);
    }

    public final boolean isCombatMovementAllowed() {
        return isCombatMovementAllowed;
    }

    // return true for heroic mode. i.e.
    //   - for dungeon in mode 10-heroic,
    //   - for raid in mode 10-Heroic
    //   - for raid in mode 25-heroic
    // DO NOT USE to check raid in mode 25-normal.
    public final boolean isHeroic() {
        return isHeroic;
    }

    // return the dungeon or raid difficulty
    public final Difficulty getDifficulty() {
        return difficulty;
    }

    // return true for 25 man or 25 man heroic mode
    public final boolean is25ManRaid() {
        return difficulty == Difficulty.Raid25N || difficulty == Difficulty.Raid25HC;
    }

    public final <T> T dungeonMode(T normal5, T heroic10) {
        return switch (difficulty) {
            case Normal -> normal5;
            default -> heroic10;
        };
    }

    public final <T> T raidMode(T normal10, T normal25) {
        return switch (difficulty) {
            case Raid10N -> normal10;
            default -> normal25;
        };
    }

    public final <T> T raidMode(T normal10, T normal25, T heroic10, T heroic25) {
        return switch (difficulty) {
            case Raid10N -> normal10;
            case Raid25N -> normal25;
            case Raid10HC -> heroic10;
            default -> heroic25;
        };
    }

    /** 
      Stops combat, ignoring restrictions, for the given creature
     
     @param who 
     @param reset 
    */

    private void forceCombatStop(Creature who) {
        forceCombatStop(who, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ForceCombatStop(Creature who, bool reset = true)
    private void forceCombatStop(Creature who, boolean reset) {
        if (who == null || !who.isInCombat()) {
            return;
        }

        who.combatStop(true);
        who.doNotReacquireSpellFocusTarget();
        who.getMotionMaster().clear(MovementGeneratorPriority.Normal);

        if (reset) {
            who.loadCreaturesAddon();
            who.setTappedBy(null);
            who.resetPlayerDamageReq();
            who.lastDamagedTime = 0;
            who.setCannotReachTarget(false);
        }
    }

    /** 
      Stops combat, ignoring restrictions, for the found creatures
     
     @param entry 
     @param maxSearchRange 
     @param samePhase 
     @param reset 
    */

    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange, boolean samePhase) {
        forceCombatStopForCreatureEntry(entry, maxSearchRange, samePhase, true);
    }

    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange) {
        forceCombatStopForCreatureEntry(entry, maxSearchRange, true, true);
    }

    private void forceCombatStopForCreatureEntry(int entry) {
        forceCombatStopForCreatureEntry(entry, 250.0f, true, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ForceCombatStopForCreatureEntry(uint entry, float maxSearchRange = 250.0f, bool samePhase = true, bool reset = true)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    private void forceCombatStopForCreatureEntry(int entry, float maxSearchRange, boolean samePhase, boolean reset) {
        Log.outDebug(LogFilter.ScriptsAi, String.format("BossAI::ForceStopCombatForCreature: called on %1$s. Debug info: %2$s", me.getGUID().clone(), me.getDebugInfo()));

        ArrayList<Creature> creatures = new ArrayList<Creature>();
        AllCreaturesOfEntryInRange check = new AllCreaturesOfEntryInRange(me, entry, maxSearchRange);
        CreatureListSearcher searcher = new CreatureListSearcher(me, creatures, check, GridType.Grid);

        if (!samePhase) {
            PhasingHandler.setAlwaysVisible(me, true, false);
        }

        Cell.visitGrid(me, searcher, maxSearchRange);

        if (!samePhase) {
            PhasingHandler.setAlwaysVisible(me, false, false);
        }

        for (var creature : creatures) {
            forceCombatStop(creature, reset);
        }
    }

    /** 
      Stops combat, ignoring restrictions, for the found creatures
     
     @param creatureEntries 
     @param maxSearchRange 
     @param samePhase 
     @param reset 
    */

    private void forceCombatStopForCreatureEntry(java.util.ArrayList<Integer> creatureEntries, float maxSearchRange, boolean samePhase) {
        forceCombatStopForCreatureEntry(creatureEntries, maxSearchRange, samePhase, true);
    }

    private void forceCombatStopForCreatureEntry(java.util.ArrayList<Integer> creatureEntries, float maxSearchRange) {
        forceCombatStopForCreatureEntry(creatureEntries, maxSearchRange, true, true);
    }

    private void forceCombatStopForCreatureEntry(java.util.ArrayList<Integer> creatureEntries) {
        forceCombatStopForCreatureEntry(creatureEntries, 250.0f, true, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void ForceCombatStopForCreatureEntry(List<uint> creatureEntries, float maxSearchRange = 250.0f, bool samePhase = true, bool reset = true)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    private void forceCombatStopForCreatureEntry(ArrayList<Integer> creatureEntries, float maxSearchRange, boolean samePhase, boolean reset) {
        for (var entry : creatureEntries) {
            forceCombatStopForCreatureEntry(entry, maxSearchRange, samePhase, reset);
        }
    }
}