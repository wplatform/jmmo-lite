package com.github.azeroth.game.ai;


public final class SmartAction {

    public SmartActions type = SmartActions.values()[0];

    public Talk talk = new Talk();  // 修复：首字母大写

    public SimpleTalk simpleTalk = new SimpleTalk();

    public Faction faction = new Faction();

    public MorphOrMount morphOrMount = new MorphOrMount();  // 修复：首字母大写

    public Sound sound = new Sound();  // 修复：首字母大写

    public Emote emote = new Emote();  // 修复：首字母大写

    public Quest quest = new Quest();  // 修复：首字母大写

    public QuestOffer questOffer = new QuestOffer();  // 修复：首字母大写

    public React react = new React();  // 修复：首字母大写

    public RandomEmote randomEmote = new RandomEmote();  // 修复：首字母大写

    public Cast cast = new Cast();  // 修复：首字母大写

    public CrossCast crossCast = new CrossCast();  // 修复：首字母大写

    public SummonCreature summonCreature = new SummonCreature();  // 修复：首字母大写

    public ThreatPCT threatPCT = new ThreatPCT();  // 修复：首字母大写

    public Threat threat = new Threat();  // 修复：首字母大写

    public CastCreatureOrGO castCreatureOrGO = new CastCreatureOrGO();  // 修复：首字母大写

    public AutoAttack autoAttack = new AutoAttack();  // 修复：首字母大写

    public CombatMove combatMove = new CombatMove();  // 修复：首字母大写

    public SetEventPhase setEventPhase = new SetEventPhase();  // 修复：首字母大写

    public IncEventPhase incEventPhase = new IncEventPhase();  // 修复：首字母大写

    public CastedCreatureOrGO castedCreatureOrGO = new CastedCreatureOrGO();  // 修复：首字母大写

    public RemoveAura removeAura = new RemoveAura();  // 修复：首字母大写

    public Follow follow = new Follow();  // 修复：首字母大写

    public RandomPhase randomPhase = new RandomPhase();  // 修复：首字母大写

    public RandomPhaseRange randomPhaseRange = new RandomPhaseRange();  // 修复：首字母大写

    public KilledMonster killedMonster = new KilledMonster();  // 修复：首字母大写

    public SetInstanceData setInstanceData = new SetInstanceData();  // 修复：首字母大写

    public SetInstanceData64 setInstanceData64 = new SetInstanceData64();  // 修复：首字母大写

    public UpdateTemplate updateTemplate = new UpdateTemplate();  // 修复：首字母大写

    public CallHelp callHelp = new CallHelp();  // 修复：首字母大写

    public SetSheath setSheath = new SetSheath();  // 修复：首字母大写

    public ForceDespawn forceDespawn = new ForceDespawn();  // 修复：首字母大写

    public InvincHP invincHP = new InvincHP();  // 修复：首字母大写

    public IngamePhaseId ingamePhaseId = new IngamePhaseId();  // 修复：首字母大写

    public IngamePhaseGroup ingamePhaseGroup = new IngamePhaseGroup();  // 修复：首字母大写

    public SetData setData = new SetData();  // 修复：首字母大写

    public MoveRandom moveRandom = new MoveRandom();  // 修复：首字母大写

    public Visibility visibility = new Visibility();  // 修复：首字母大写

    public SummonGO summonGO = new SummonGO();  // 修复：首字母大写

    public Active active = new Active();  // 修复：首字母大写

    public Taxi taxi = new Taxi();  // 修复：首字母大写

    public WpStart wpStart = new WpStart();  // 修复：首字母大写

    public WpPause wpPause = new WpPause();  // 修复：首字母大写

    public WpStop wpStop = new WpStop();  // 修复：首字母大写

    public Item item = new Item();  // 修复：首字母大写

    public SetRun setRun = new SetRun();  // 修复：首字母大写

    public SetDisableGravity setDisableGravity = new SetDisableGravity();  // 修复：首字母大写

    public Teleport teleport = new Teleport();  // 修复：首字母大写

    public SetCounter setCounter = new SetCounter();  // 修复：首字母大写

    public StoreTargets storeTargets = new StoreTargets();  // 修复：首字母大写

    public TimeEvent timeEvent = new TimeEvent();  // 修复：首字母大写

    public Movie movie = new Movie();  // 修复：首字母大写

    public Equip equip = new Equip();  // 修复：首字母大写

    public Flag flag = new Flag();  // 修复：首字母大写

    public SetunitByte setunitByte = new SetunitByte();  // 修复：首字母大写

    public DelunitByte delunitByte = new DelunitByte();  // 修复：首字母大写

    public TimedActionList timedActionList = new TimedActionList();  // 修复：首字母大写

    public RandTimedActionList randTimedActionList = new RandTimedActionList();  // 修复：首字母大写

    public RandRangeTimedActionList randRangeTimedActionList = new RandRangeTimedActionList();  // 修复：首字母大写

    public InterruptSpellCasting interruptSpellCasting = new InterruptSpellCasting();  // 修复：首字母大写

    public Jump jump = new Jump();  // 修复：首字母大写

    public FleeAssist fleeAssist = new FleeAssist();  // 修复：首字母大写

    public EnableTempGO enableTempGO = new EnableTempGO();  // 修复：首字母大写

    public MoveToPos moveToPos = new MoveToPos();  // 修复：首字母大写

    public SendGossipMenu sendGossipMenu = new SendGossipMenu();  // 修复：首字母大写

    public SetGoLootState setGoLootState = new SetGoLootState();  // 修复：首字母大写

    public SendTargetToTarget sendTargetToTarget = new SendTargetToTarget();  // 修复：首字母大写

    public SetRangedMovement setRangedMovement = new SetRangedMovement();  // 修复：首字母大写

    public SetHealthRegen setHealthRegen = new SetHealthRegen();  // 修复：首字母大写

    public SetRoot setRoot = new SetRoot();  // 修复：首字母大写

    public GoState goState = new GoState();  // 修复：首字母大写

    public CreatureGroup creatureGroup = new CreatureGroup();  // 修复：首字母大写

    public Power power = new Power();  // 修复：首字母大写

    public GameEventStop gameEventStop = new GameEventStop();  // 修复：首字母大写

    public GameEventStart gameEventStart = new GameEventStart();  // 修复：首字母大写

    public ClosestWaypointFromList closestWaypointFromList = new ClosestWaypointFromList();  // 修复：首字母大写

    public MoveOffset moveOffset = new MoveOffset();  // 修复：首字母大写

    public RandomSound randomSound = new RandomSound();  // 修复：首字母大写

    public CorpseDelay corpseDelay = new CorpseDelay();  // 修复：首字母大写

    public DisableEvade disableEvade = new DisableEvade();  // 修复：首字母大写

    public GroupSpawn groupSpawn = new GroupSpawn();  // 修复：首字母大写

    public AuraType auraType = AuraType.values()[0];

    public LoadEquipment loadEquipment = new LoadEquipment();  // 修复：首字母大写

    public RandomTimedEvent randomTimedEvent = new RandomTimedEvent();  // 修复：首字母大写

    public PauseMovement pauseMovement = new PauseMovement();  // 修复：首字母大写

    public RespawnData respawnData = new RespawnData();  // 修复：首字母大写

    public AnimKit animKit = new AnimKit();  // 修复：首字母大写

    public Scene scene = new Scene();  // 修复：首字母大写

    public Cinematic cinematic = new Cinematic();  // 修复：首字母大写

    public MovementSpeed movementSpeed = new MovementSpeed();  // 修复：首字母大写

    public SpellVisualKit spellVisualKit = new SpellVisualKit();  // 修复：首字母大写

    public OverrideLight overrideLight = new OverrideLight();  // 修复：首字母大写

    public OverrideWeather overrideWeather = new OverrideWeather();  // 修复：首字母大写

    public SetHover setHover = new SetHover();  // 修复：首字母大写

    public Evade evade = new Evade();  // 修复：首字母大写

    public SetHealthPct setHealthPct = new SetHealthPct();  // 修复：首字母大写

    public Conversation conversation = new Conversation();  // 修复：首字母大写

    public SetImmunePC setImmunePC = new SetImmunePC();  // 修复：首字母大写

    public SetImmuneNPC setImmuneNPC = new SetImmuneNPC();  // 修复：首字母大写

    public SetUninteractible setUninteractible = new SetUninteractible();  // 修复：首字母大写

    public ActivateGameObject activateGameObject = new ActivateGameObject();  // 修复：首字母大写

    public AddToStoredTargets addToStoredTargets = new AddToStoredTargets();  // 修复：首字母大写

    public BecomePersonalClone becomePersonalClone = new BecomePersonalClone();  // 修复：首字母大写

    public TriggerGameEvent triggerGameEvent = new TriggerGameEvent();  // 修复：首字母大写

    public DoAction doAction = new DoAction();  // 修复：首字母大写

    public Raw raw = new Raw();  // 修复：首字母大写

    // ... 其余代码保持不变
}


public final static class Talk {
    public int textGroupId;
    public int duration;
    public int useTalkTarget;

    public Talk clone() {
        Talk varCopy = new talk();

        varCopy.textGroupId = this.textGroupId;
        varCopy.duration = this.duration;
        varCopy.useTalkTarget = this.useTalkTarget;

        return varCopy;
    }
}

public final static class SimpleTalk {
    public int textGroupId;
    public int duration;

    public SimpleTalk clone() {
        SimpleTalk varCopy = new simpleTalk();

        varCopy.textGroupId = this.textGroupId;
        varCopy.duration = this.duration;

        return varCopy;
    }
}

public final static class Faction {
    public int factionId;

    public Faction clone() {
        Faction varCopy = new faction();

        varCopy.factionId = this.factionId;

        return varCopy;
    }
}

public final static class MorphOrMount {
    public int creature;
    public int model;

    public MorphOrMount clone() {
        MorphOrMount varCopy = new morphOrMount();

        varCopy.creature = this.creature;
        varCopy.model = this.model;

        return varCopy;
    }
}

public final static class Sound {
    public int soundId;
    public int onlySelf;
    public int distance;
    public int keyBroadcastTextId;

    public Sound clone() {
        Sound varCopy = new sound();

        varCopy.soundId = this.soundId;
        varCopy.onlySelf = this.onlySelf;
        varCopy.distance = this.distance;
        varCopy.keyBroadcastTextId = this.keyBroadcastTextId;

        return varCopy;
    }
}

public final static class Emote {
    public int emoteId;

    public Emote clone() {
        Emote varCopy = new emote();

        varCopy.emoteId = this.emoteId;

        return varCopy;
    }
}

public final static class Quest {
    public int questId;

    public Quest clone() {
        Quest varCopy = new quest();

        varCopy.questId = this.questId;

        return varCopy;
    }
}

public final static class QuestOffer {
    public int questId;
    public int directAdd;

}

public final static class React {
    public int state;

}

public final static class RandomEmote {
    public int emote1;
    public int emote2;
    public int emote3;
    public int emote4;
    public int emote5;
    public int emote6;
}

public final static class Cast {
    public int spell;
    public int castFlags;
    public int triggerFlags;
    public int targetsLimit;
}

public final static class CrossCast {
    public int spell;
    public int castFlags;
    public int targetType;
    public int targetParam1;
    public int targetParam2;
    public int targetParam3;

}

public final static class SummonCreature {
    public int creature;
    public int type;
    public int duration;
    public int storageID;
    public int attackInvoker;
    public int flags; // SmartActionSummonCreatureFlags
    public int count;

}

public final static class ThreatPCT {
    public int threatINC;
    public int threatDEC;


}

public final static class CastCreatureOrGO {
    public int quest;
    public int spell;

}

public final static class Threat {
    public int threatINC;
    public int threatDEC;


}

public final static class AutoAttack {
    public int attack;


}

public final static class CombatMove {
    public int move;

}

public final static class SetEventPhase {
    public int phase;


}

public final static class IncEventPhase {
    public int inc;
    public int dec;


}

public final static class CastedCreatureOrGO {
    public int creature;
    public int spell;


}

public final static class RemoveAura {
    public int spell;
    public int charges;
    public int onlyOwnedAuras;


}

public final static class Follow {
    public int dist;
    public int angle;
    public int entry;
    public int credit;
    public int creditType;


}

public final static class RandomPhase {
    public int phase1;
    public int phase2;
    public int phase3;
    public int phase4;
    public int phase5;
    public int phase6;


}

public final static class RandomPhaseRange {
    public int phaseMin;
    public int phaseMax;

}

public final static class KilledMonster {
    public int creature;

}

public final static class SetInstanceData {
    public int field;
    public int data;
    public int type;

}

public final static class SetInstanceData64 {
    public int field;

}

public final static class UpdateTemplate {
    public int creature;
    public int updateLevel;

}

public final static class CallHelp {
    public int range;
    public int withEmote;

}

public final static class SetSheath {
    public int sheath;

}

public final static class ForceDespawn {
    public int delay;
    public int forceRespawnTimer;

}

public final static class InvincHP {
    public int minHP;
    public int percent;

}

public final static class IngamePhaseId {
    public int id;
    public int apply;

}

public final static class IngamePhaseGroup {
    public int groupId;
    public int apply;

}

public final static class SetData {
    public int field;
    public int data;

}

public final static class MoveRandom {
    public int distance;

}

public final static class Visibility {
    public int state;

}

public final static class SummonGO {
    public int entry;
    public int despawnTime;
    public int summonType;

}

public final static class Active {
    public int state;

}

public final static class Taxi {
    public int id;

}

public final static class WpStart {
    public int run;
    public int pathID;
    public int repeat;
    public int quest;

    public int despawnTime;
    //public uint reactState; DO NOT REUSE

    public WpStart clone() {
        WpStart varCopy = new wpStart();

        varCopy.run = this.run;
        varCopy.pathID = this.pathID;
        varCopy.repeat = this.repeat;
        varCopy.quest = this.quest;
        varCopy.despawnTime = this.despawnTime;

        return varCopy;
    }
}

public final static class WpPause {
    public int delay;

    public WpPause clone() {
        WpPause varCopy = new wpPause();

        varCopy.delay = this.delay;

        return varCopy;
    }
}

public final static class WpStop {
    public int despawnTime;
    public int quest;
    public int fail;

    public WpStop clone() {
        WpStop varCopy = new wpStop();

        varCopy.despawnTime = this.despawnTime;
        varCopy.quest = this.quest;
        varCopy.fail = this.fail;

        return varCopy;
    }
}

public final static class Item {
    public int entry;
    public int count;

    public Item clone() {
        Item varCopy = new item();

        varCopy.entry = this.entry;
        varCopy.count = this.count;

        return varCopy;
    }
}

public final static class SetRun {
    public int run;

    public SetRun clone() {
        SetRun varCopy = new setRun();

        varCopy.run = this.run;

        return varCopy;
    }
}

public final static class SetDisableGravity {
    public int disable;

    public SetDisableGravity clone() {
        SetDisableGravity varCopy = new setDisableGravity();

        varCopy.disable = this.disable;

        return varCopy;
    }
}

public final static class Teleport {
    public int mapID;

}

public final static class SetCounter {
    public int counterId;
    public int value;
    public int reset;

}

public final static class StoreTargets {
    public int id;

}

public final static class TimeEvent {
    public int id;
    public int min;
    public int max;
    public int repeatMin;
    public int repeatMax;
    public int chance;

}

public final static class Movie {
    public int entry;

}

public final static class Equip {
    public int entry;
    public int mask;
    public int slot1;
    public int slot2;
    public int slot3;

    public Equip clone() {
        Equip varCopy = new equip();

        varCopy.entry = this.entry;
        varCopy.mask = this.mask;
        varCopy.slot1 = this.slot1;
        varCopy.slot2 = this.slot2;
        varCopy.slot3 = this.slot3;

        return varCopy;
    }
}

public final static class Flag {
    public int flag;

    public Flag clone() {
        Flag varCopy = new flag();

        varCopy.flag = this.flag;

        return varCopy;
    }
}

public final static class SetunitByte {
    public int byte1;
    public int type;

    public SetunitByte clone() {
        SetunitByte varCopy = new setunitByte();

        varCopy.byte1 = this.byte1;
        varCopy.type = this.type;

        return varCopy;
    }
}

public final static class DelunitByte {
    public int byte1;
    public int type;

    public DelunitByte clone() {
        DelunitByte varCopy = new delunitByte();

        varCopy.byte1 = this.byte1;
        varCopy.type = this.type;

        return varCopy;
    }
}

public final static class TimedActionList {
    public int id;
    public int timerType;
    public int allowOverride;

    public TimedActionList clone() {
        TimedActionList varCopy = new timedActionList();

        varCopy.id = this.id;
        varCopy.timerType = this.timerType;
        varCopy.allowOverride = this.allowOverride;

        return varCopy;
    }
}

public final static class RandTimedActionList {
    public int actionList1;
    public int actionList2;
    public int actionList3;
    public int actionList4;
    public int actionList5;
    public int actionList6;

    public RandTimedActionList clone() {
        RandTimedActionList varCopy = new randTimedActionList();

        varCopy.actionList1 = this.actionList1;
        varCopy.actionList2 = this.actionList2;
        varCopy.actionList3 = this.actionList3;
        varCopy.actionList4 = this.actionList4;
        varCopy.actionList5 = this.actionList5;
        varCopy.actionList6 = this.actionList6;

        return varCopy;
    }
}

public final static class RandRangeTimedActionList {
    public int idMin;
    public int idMax;

    public RandRangeTimedActionList clone() {
        RandRangeTimedActionList varCopy = new randRangeTimedActionList();

        varCopy.idMin = this.idMin;
        varCopy.idMax = this.idMax;

        return varCopy;
    }
}

public final static class InterruptSpellCasting {
    public int withDelayed;
    public int spell_id;
    public int withInstant;

    public InterruptSpellCasting clone() {
        InterruptSpellCasting varCopy = new interruptSpellCasting();

        varCopy.withDelayed = this.withDelayed;
        varCopy.spell_id = this.spell_id;
        varCopy.withInstant = this.withInstant;

        return varCopy;
    }
}

public final static class Jump {
    public int speedXY;
    public int speedZ;
    public int gravity;
    public int useDefaultGravity;
    public int pointId;
    public int contactDistance;

    public Jump clone() {
        Jump varCopy = new jump();

        varCopy.speedXY = this.speedXY;
        varCopy.speedZ = this.speedZ;
        varCopy.gravity = this.gravity;
        varCopy.useDefaultGravity = this.useDefaultGravity;
        varCopy.pointId = this.pointId;
        varCopy.contactDistance = this.contactDistance;

        return varCopy;
    }
}

public final static class FleeAssist {
    public int withEmote;

    public FleeAssist clone() {
        FleeAssist varCopy = new fleeAssist();

        varCopy.withEmote = this.withEmote;

        return varCopy;
    }
}

public final static class EnableTempGO {
    public int duration;

    public EnableTempGO clone() {
        EnableTempGO varCopy = new enableTempGO();

        varCopy.duration = this.duration;

        return varCopy;
    }
}

public final static class MoveToPos {
    public int pointId;
    public int transport;
    public int disablePathfinding;
    public int contactDistance;

    public MoveToPos clone() {
        MoveToPos varCopy = new moveToPos();

        varCopy.pointId = this.pointId;
        varCopy.transport = this.transport;
        varCopy.disablePathfinding = this.disablePathfinding;
        varCopy.contactDistance = this.contactDistance;

        return varCopy;
    }
}

public final static class SendGossipMenu {
    public int gossipMenuId;
    public int gossipNpcTextId;

    public SendGossipMenu clone() {
        SendGossipMenu varCopy = new sendGossipMenu();

        varCopy.gossipMenuId = this.gossipMenuId;
        varCopy.gossipNpcTextId = this.gossipNpcTextId;

        return varCopy;
    }
}

public final static class SetGoLootState {
    public int state;

    public SetGoLootState clone() {
        SetGoLootState varCopy = new setGoLootState();

        varCopy.state = this.state;

        return varCopy;
    }
}

public final static class SendTargetToTarget {
    public int id;

    public SendTargetToTarget clone() {
        SendTargetToTarget varCopy = new sendTargetToTarget();

        varCopy.id = this.id;

        return varCopy;
    }
}

public final static class SetRangedMovement {
    public int distance;
    public int angle;

    public SetRangedMovement clone() {
        SetRangedMovement varCopy = new setRangedMovement();

        varCopy.distance = this.distance;
        varCopy.angle = this.angle;

        return varCopy;
    }
}

public final static class SetHealthRegen {
    public int regenHealth;

    public SetHealthRegen clone() {
        SetHealthRegen varCopy = new setHealthRegen();

        varCopy.regenHealth = this.regenHealth;

        return varCopy;
    }
}

public final static class SetRoot {
    public int root;

    public SetRoot clone() {
        SetRoot varCopy = new setRoot();

        varCopy.root = this.root;

        return varCopy;
    }
}

public final static class GoState {
    public int state;

    public GoState clone() {
        GoState varCopy = new goState();

        varCopy.state = this.state;

        return varCopy;
    }
}

public final static class CreatureGroup {
    public int group;
    public int attackInvoker;

    public CreatureGroup clone() {
        CreatureGroup varCopy = new creatureGroup();

        varCopy.group = this.group;
        varCopy.attackInvoker = this.attackInvoker;

        return varCopy;
    }
}

public final static class Power {
    public int powerType;
    public int newPower;

    public Power clone() {
        Power varCopy = new power();

        varCopy.powerType = this.powerType;
        varCopy.newPower = this.newPower;

        return varCopy;
    }
}

public final static class GameEventStop {
    public int id;

    public GameEventStop clone() {
        GameEventStop varCopy = new gameEventStop();

        varCopy.id = this.id;

        return varCopy;
    }
}

public final static class GameEventStart {
    public int id;

    public GameEventStart clone() {
        GameEventStart varCopy = new gameEventStart();

        varCopy.id = this.id;

        return varCopy;
    }
}

public final static class ClosestWaypointFromList {
    public int wp1;
    public int wp2;
    public int wp3;
    public int wp4;
    public int wp5;
    public int wp6;

    public ClosestWaypointFromList clone() {
        ClosestWaypointFromList varCopy = new closestWaypointFromList();

        varCopy.wp1 = this.wp1;
        varCopy.wp2 = this.wp2;
        varCopy.wp3 = this.wp3;
        varCopy.wp4 = this.wp4;
        varCopy.wp5 = this.wp5;
        varCopy.wp6 = this.wp6;

        return varCopy;
    }
}

public final static class MoveOffset {
    public int pointId;

    public MoveOffset clone() {
        MoveOffset varCopy = new moveOffset();

        varCopy.pointId = this.pointId;

        return varCopy;
    }
}

public final static class RandomSound {
    public int sound1;
    public int sound2;
    public int sound3;
    public int sound4;
    public int onlySelf;
    public int distance;

    public RandomSound clone() {
        RandomSound varCopy = new randomSound();

        varCopy.sound1 = this.sound1;
        varCopy.sound2 = this.sound2;
        varCopy.sound3 = this.sound3;
        varCopy.sound4 = this.sound4;
        varCopy.onlySelf = this.onlySelf;
        varCopy.distance = this.distance;

        return varCopy;
    }
}

public final static class CorpseDelay {
    public int timer;
    public int includeDecayRatio;

    public CorpseDelay clone() {
        CorpseDelay varCopy = new corpseDelay();

        varCopy.timer = this.timer;
        varCopy.includeDecayRatio = this.includeDecayRatio;

        return varCopy;
    }
}

public final static class DisableEvade {
    public int disable;

    public DisableEvade clone() {
        DisableEvade varCopy = new disableEvade();

        varCopy.disable = this.disable;

        return varCopy;
    }
}

public final static class GroupSpawn {
    public int groupId;
    public int minDelay;
    public int maxDelay;
    public int spawnflags;

    public GroupSpawn clone() {
        GroupSpawn varCopy = new groupSpawn();

        varCopy.groupId = this.groupId;
        varCopy.minDelay = this.minDelay;
        varCopy.maxDelay = this.maxDelay;
        varCopy.spawnflags = this.spawnflags;

        return varCopy;
    }
}

public final static class LoadEquipment {
    public int id;
    public int force;

    public LoadEquipment clone() {
        LoadEquipment varCopy = new loadEquipment();

        varCopy.id = this.id;
        varCopy.force = this.force;

        return varCopy;
    }
}

public final static class RandomTimedEvent {
    public int minId;
    public int maxId;

    public RandomTimedEvent clone() {
        RandomTimedEvent varCopy = new randomTimedEvent();

        varCopy.minId = this.minId;
        varCopy.maxId = this.maxId;

        return varCopy;
    }
}

public final static class PauseMovement {
    public int movementSlot;
    public int pauseTimer;
    public int force;

    public PauseMovement clone() {
        PauseMovement varCopy = new pauseMovement();

        varCopy.movementSlot = this.movementSlot;
        varCopy.pauseTimer = this.pauseTimer;
        varCopy.force = this.force;

        return varCopy;
    }
}

public final static class RespawnData {
    public int spawnType;
    public int spawnId;

    public RespawnData clone() {
        RespawnData varCopy = new respawnData();

        varCopy.spawnType = this.spawnType;
        varCopy.spawnId = this.spawnId;

        return varCopy;
    }
}

public final static class AnimKit {
    public int animKit;
    public int type;

    public AnimKit clone() {
        AnimKit varCopy = new animKit();

        varCopy.animKit = this.animKit;
        varCopy.type = this.type;

        return varCopy;
    }
}

public final static class Scene {
    public int sceneId;

    public Scene clone() {
        Scene varCopy = new scene();

        varCopy.sceneId = this.sceneId;

        return varCopy;
    }
}

public final static class Cinematic {
    public int entry;

    public Cinematic clone() {
        Cinematic varCopy = new cinematic();

        varCopy.entry = this.entry;

        return varCopy;
    }
}

public final static class MovementSpeed {
    public int movementType;
    public int speedInteger;
    public int speedFraction;

    public MovementSpeed clone() {
        MovementSpeed varCopy = new movementSpeed();

        varCopy.movementType = this.movementType;
        varCopy.speedInteger = this.speedInteger;
        varCopy.speedFraction = this.speedFraction;

        return varCopy;
    }
}

public final static class SpellVisualKit {
    public int spellVisualKitId;
    public int kitType;
    public int duration;

    public SpellVisualKit clone() {
        SpellVisualKit varCopy = new spellVisualKit();

        varCopy.spellVisualKitId = this.spellVisualKitId;
        varCopy.kitType = this.kitType;
        varCopy.duration = this.duration;

        return varCopy;
    }
}

public final static class OverrideLight {
    public int zoneId;
    public int areaLightId;
    public int overrideLightId;
    public int transitionMilliseconds;

    public OverrideLight clone() {
        OverrideLight varCopy = new overrideLight();

        varCopy.zoneId = this.zoneId;
        varCopy.areaLightId = this.areaLightId;
        varCopy.overrideLightId = this.overrideLightId;
        varCopy.transitionMilliseconds = this.transitionMilliseconds;

        return varCopy;
    }
}

public final static class OverrideWeather {
    public int zoneId;
    public int weatherId;
    public int intensity;

    public OverrideWeather clone() {
        OverrideWeather varCopy = new overrideWeather();

        varCopy.zoneId = this.zoneId;
        varCopy.weatherId = this.weatherId;
        varCopy.intensity = this.intensity;

        return varCopy;
    }
}

public final static class SetHover {
    public int enable;

    public SetHover clone() {
        SetHover varCopy = new setHover();

        varCopy.enable = this.enable;

        return varCopy;
    }
}

public final static class Evade {
    public int toRespawnPosition;

    public Evade clone() {
        Evade varCopy = new Evade();

        varCopy.toRespawnPosition = this.toRespawnPosition;

        return varCopy;
    }
}

public final static class SetHealthPct {
    public int percent;

    public SetHealthPct clone() {
        SetHealthPct varCopy = new SetHealthPct();

        varCopy.percent = this.percent;

        return varCopy;
    }
}

public final static class Conversation {
    public int id;

}

public final static class SetImmunePC {
    public int immunePC;

}

public final static class SetImmuneNPC {
    public int immuneNPC;

}

public final static class SetUninteractible {
    public int uninteractible;

}

public final static class ActivateGameObject {
    public int gameObjectAction;
    public int param;

}

public final static class AddToStoredTargets {
    public int id;

    public AddToStoredTargets clone() {
        AddToStoredTargets varCopy = new addToStoredTargets();

        varCopy.id = this.id;

        return varCopy;
    }
}

public final static class BecomePersonalClone {
    public int type;
    public int duration;

    public BecomePersonalClone clone() {
        BecomePersonalClone varCopy = new becomePersonalClone();

        varCopy.type = this.type;
        varCopy.duration = this.duration;

        return varCopy;
    }
}

public final static class TriggerGameEvent {
    public int eventId;
    public int useSaiTargetAsGameEventSource;

    public TriggerGameEvent clone() {
        TriggerGameEvent varCopy = new triggerGameEvent();

        varCopy.eventId = this.eventId;
        varCopy.useSaiTargetAsGameEventSource = this.useSaiTargetAsGameEventSource;

        return varCopy;
    }
}

public final static class DoAction {
    public int actionId;

    public DoAction clone() {
        DoAction varCopy = new doAction();

        varCopy.actionId = this.actionId;

        return varCopy;
    }
}


/// #endregion

public final static class Raw {
    public int param1;
    public int param2;
    public int param3;
    public int param4;
    public int param5;
    public int param6;
    public int param7;

    public Raw clone() {
        Raw varCopy = new raw();

        varCopy.param1 = this.param1;
        varCopy.param2 = this.param2;
        varCopy.param3 = this.param3;
        varCopy.param4 = this.param4;
        varCopy.param5 = this.param5;
        varCopy.param6 = this.param6;
        varCopy.param7 = this.param7;

        return varCopy;
    }
}
}
