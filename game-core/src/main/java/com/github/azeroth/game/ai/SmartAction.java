package game.ai;

import Framework.Constants.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StructLayout(LayoutKind.Explicit)] public struct SmartAction
//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: [StructLayout(LayoutKind.Explicit)] public struct SmartAction
public final class SmartAction {
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(0)] public SmartActions type;
    public SmartActions type = SmartActions.values()[0];

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Talk talk;
    public Talk talk = new Talk();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SimpleTalk simpleTalk;
    public SimpleTalk simpleTalk = new SimpleTalk();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Faction faction;
    public Faction faction = new Faction();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public MorphOrMount morphOrMount;
    public MorphOrMount morphOrMount = new MorphOrMount();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Sound sound;
    public Sound sound = new Sound();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Emote emote;
    public Emote emote = new Emote();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Quest quest;
    public Quest quest = new Quest();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public QuestOffer questOffer;
    public QuestOffer questOffer = new QuestOffer();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public React react;
    public React react = new React();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandomEmote randomEmote;
    public RandomEmote randomEmote = new RandomEmote();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Cast cast;
    public Cast cast = new Cast();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CrossCast crossCast;
    public CrossCast crossCast = new CrossCast();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SummonCreature summonCreature;
    public SummonCreature summonCreature = new SummonCreature();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public ThreatPCT threatPCT;
    public ThreatPCT threatPCT = new ThreatPCT();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Threat threat;
    public Threat threat = new Threat();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CastCreatureOrGO castCreatureOrGO;
    public CastCreatureOrGO castCreatureOrGO = new CastCreatureOrGO();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public AutoAttack autoAttack;
    public AutoAttack autoAttack = new AutoAttack();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CombatMove combatMove;
    public CombatMove combatMove = new CombatMove();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetEventPhase setEventPhase;
    public SetEventPhase setEventPhase = new SetEventPhase();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public IncEventPhase incEventPhase;
    public IncEventPhase incEventPhase = new IncEventPhase();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CastedCreatureOrGO castedCreatureOrGO;
    public CastedCreatureOrGO castedCreatureOrGO = new CastedCreatureOrGO();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RemoveAura removeAura;
    public RemoveAura removeAura = new RemoveAura();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Follow follow;
    public Follow follow = new Follow();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandomPhase randomPhase;
    public RandomPhase randomPhase = new RandomPhase();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandomPhaseRange randomPhaseRange;
    public RandomPhaseRange randomPhaseRange = new RandomPhaseRange();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public KilledMonster killedMonster;
    public KilledMonster killedMonster = new KilledMonster();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetInstanceData setInstanceData;
    public SetInstanceData setInstanceData = new SetInstanceData();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetInstanceData64 setInstanceData64;
    public SetInstanceData64 setInstanceData64 = new SetInstanceData64();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public UpdateTemplate updateTemplate;
    public UpdateTemplate updateTemplate = new UpdateTemplate();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CallHelp callHelp;
    public CallHelp callHelp = new CallHelp();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetSheath setSheath;
    public SetSheath setSheath = new SetSheath();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public ForceDespawn forceDespawn;
    public ForceDespawn forceDespawn = new ForceDespawn();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public InvincHP invincHP;
    public InvincHP invincHP = new InvincHP();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public IngamePhaseId ingamePhaseId;
    public IngamePhaseId ingamePhaseId = new IngamePhaseId();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public IngamePhaseGroup ingamePhaseGroup;
    public IngamePhaseGroup ingamePhaseGroup = new IngamePhaseGroup();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetData setData;
    public SetData setData = new SetData();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public MoveRandom moveRandom;
    public MoveRandom moveRandom = new MoveRandom();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Visibility visibility;
    public Visibility visibility = new Visibility();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SummonGO summonGO;
    public SummonGO summonGO = new SummonGO();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Active active;
    public Active active = new Active();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Taxi taxi;
    public Taxi taxi = new Taxi();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public WpStart wpStart;
    public WpStart wpStart = new WpStart();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public WpPause wpPause;
    public WpPause wpPause = new WpPause();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public WpStop wpStop;
    public WpStop wpStop = new WpStop();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Item item;
    public Item item = new Item();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetRun setRun;
    public SetRun setRun = new SetRun();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetDisableGravity setDisableGravity;
    public SetDisableGravity setDisableGravity = new SetDisableGravity();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Teleport teleport;
    public Teleport teleport = new Teleport();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetCounter setCounter;
    public SetCounter setCounter = new SetCounter();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public StoreTargets storeTargets;
    public StoreTargets storeTargets = new StoreTargets();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public TimeEvent timeEvent;
    public TimeEvent timeEvent = new TimeEvent();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Movie movie;
    public Movie movie = new Movie();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Equip equip;
    public Equip equip = new Equip();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Flag flag;
    public Flag flag = new Flag();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetunitByte setunitByte;
    public SetunitByte setunitByte = new SetunitByte();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public DelunitByte delunitByte;
    public DelunitByte delunitByte = new DelunitByte();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public TimedActionList timedActionList;
    public TimedActionList timedActionList = new TimedActionList();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandTimedActionList randTimedActionList;
    public RandTimedActionList randTimedActionList = new RandTimedActionList();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandRangeTimedActionList randRangeTimedActionList;
    public RandRangeTimedActionList randRangeTimedActionList = new RandRangeTimedActionList();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public InterruptSpellCasting interruptSpellCasting;
    public InterruptSpellCasting interruptSpellCasting = new InterruptSpellCasting();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Jump jump;
    public Jump jump = new Jump();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public FleeAssist fleeAssist;
    public FleeAssist fleeAssist = new FleeAssist();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public EnableTempGO enableTempGO;
    public EnableTempGO enableTempGO = new EnableTempGO();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public MoveToPos moveToPos;
    public MoveToPos moveToPos = new MoveToPos();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SendGossipMenu sendGossipMenu;
    public SendGossipMenu sendGossipMenu = new SendGossipMenu();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetGoLootState setGoLootState;
    public SetGoLootState setGoLootState = new SetGoLootState();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SendTargetToTarget sendTargetToTarget;
    public SendTargetToTarget sendTargetToTarget = new SendTargetToTarget();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetRangedMovement setRangedMovement;
    public SetRangedMovement setRangedMovement = new SetRangedMovement();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetHealthRegen setHealthRegen;
    public SetHealthRegen setHealthRegen = new SetHealthRegen();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetRoot setRoot;
    public SetRoot setRoot = new SetRoot();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public GoState goState;
    public GoState goState = new GoState();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CreatureGroup creatureGroup;
    public CreatureGroup creatureGroup = new CreatureGroup();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Power power;
    public Power power = new Power();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public GameEventStop gameEventStop;
    public GameEventStop gameEventStop = new GameEventStop();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public GameEventStart gameEventStart;
    public GameEventStart gameEventStart = new GameEventStart();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public ClosestWaypointFromList closestWaypointFromList;
    public ClosestWaypointFromList closestWaypointFromList = new ClosestWaypointFromList();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public MoveOffset moveOffset;
    public MoveOffset moveOffset = new MoveOffset();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandomSound randomSound;
    public RandomSound randomSound = new RandomSound();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public CorpseDelay corpseDelay;
    public CorpseDelay corpseDelay = new CorpseDelay();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public DisableEvade disableEvade;
    public DisableEvade disableEvade = new DisableEvade();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public GroupSpawn groupSpawn;
    public GroupSpawn groupSpawn = new GroupSpawn();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public AuraType auraType;
    public AuraType auraType = AuraType.values()[0];

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public LoadEquipment loadEquipment;
    public LoadEquipment loadEquipment = new LoadEquipment();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RandomTimedEvent randomTimedEvent;
    public RandomTimedEvent randomTimedEvent = new RandomTimedEvent();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public PauseMovement pauseMovement;
    public PauseMovement pauseMovement = new PauseMovement();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public RespawnData respawnData;
    public RespawnData respawnData = new RespawnData();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public AnimKit animKit;
    public AnimKit animKit = new AnimKit();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Scene scene;
    public Scene scene = new Scene();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Cinematic cinematic;
    public Cinematic cinematic = new Cinematic();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public MovementSpeed movementSpeed;
    public MovementSpeed movementSpeed = new MovementSpeed();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SpellVisualKit spellVisualKit;
    public SpellVisualKit spellVisualKit = new SpellVisualKit();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public OverrideLight overrideLight;
    public OverrideLight overrideLight = new OverrideLight();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public OverrideWeather overrideWeather;
    public OverrideWeather overrideWeather = new OverrideWeather();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetHover setHover;
    public SetHover setHover = new SetHover();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Evade evade;
    public Evade evade = new Evade();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetHealthPct setHealthPct;
    public SetHealthPct setHealthPct = new SetHealthPct();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Conversation conversation;
    public Conversation conversation = new Conversation();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetImmunePC setImmunePC;
    public SetImmunePC setImmunePC = new SetImmunePC();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetImmuneNPC setImmuneNPC;
    public SetImmuneNPC setImmuneNPC = new SetImmuneNPC();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public SetUninteractible setUninteractible;
    public SetUninteractible setUninteractible = new SetUninteractible();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public ActivateGameObject activateGameObject;
    public ActivateGameObject activateGameObject = new ActivateGameObject();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public AddToStoredTargets addToStoredTargets;
    public AddToStoredTargets addToStoredTargets = new AddToStoredTargets();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public BecomePersonalClone becomePersonalClone;
    public BecomePersonalClone becomePersonalClone = new BecomePersonalClone();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public TriggerGameEvent triggerGameEvent;
    public TriggerGameEvent triggerGameEvent = new TriggerGameEvent();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public DoAction doAction;
    public DoAction doAction = new DoAction();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public Raw raw;
    public Raw raw = new Raw();

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
        ///#region Stucts

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Talk
    public final static class Talk {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint textGroupId;
        public int textGroupId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint duration;
        public int duration;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint useTalkTarget;
        public int useTalkTarget;

        public Talk clone() {
            Talk varCopy = new Talk();

            varCopy.textGroupId = this.textGroupId;
            varCopy.duration = this.duration;
            varCopy.useTalkTarget = this.useTalkTarget;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SimpleTalk
    public final static class SimpleTalk {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint textGroupId;
        public int textGroupId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint duration;
        public int duration;

        public SimpleTalk clone() {
            SimpleTalk varCopy = new SimpleTalk();

            varCopy.textGroupId = this.textGroupId;
            varCopy.duration = this.duration;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Faction
    public final static class Faction {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint factionId;
        public int factionId;

        public Faction clone() {
            Faction varCopy = new Faction();

            varCopy.factionId = this.factionId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MorphOrMount
    public final static class MorphOrMount {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint model;
        public int model;

        public MorphOrMount clone() {
            MorphOrMount varCopy = new MorphOrMount();

            varCopy.creature = this.creature;
            varCopy.model = this.model;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Sound
    public final static class Sound {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint soundId;
        public int soundId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint onlySelf;
        public int onlySelf;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint distance;
        public int distance;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint keyBroadcastTextId;
        public int keyBroadcastTextId;

        public Sound clone() {
            Sound varCopy = new Sound();

            varCopy.soundId = this.soundId;
            varCopy.onlySelf = this.onlySelf;
            varCopy.distance = this.distance;
            varCopy.keyBroadcastTextId = this.keyBroadcastTextId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Emote
    public final static class Emote {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emoteId;
        public int emoteId;

        public Emote clone() {
            Emote varCopy = new Emote();

            varCopy.emoteId = this.emoteId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Quest
    public final static class Quest {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint questId;
        public int questId;

        public Quest clone() {
            Quest varCopy = new Quest();

            varCopy.questId = this.questId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct QuestOffer
    public final static class QuestOffer {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint questId;
        public int questId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint directAdd;
        public int directAdd;

        public QuestOffer clone() {
            QuestOffer varCopy = new QuestOffer();

            varCopy.questId = this.questId;
            varCopy.directAdd = this.directAdd;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct React
    public final static class React {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint state;
        public int state;

        public React clone() {
            React varCopy = new React();

            varCopy.state = this.state;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandomEmote
    public final static class RandomEmote {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emote1;
        public int emote1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emote2;
        public int emote2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emote3;
        public int emote3;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emote4;
        public int emote4;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emote5;
        public int emote5;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint emote6;
        public int emote6;

        public RandomEmote clone() {
            RandomEmote varCopy = new RandomEmote();

            varCopy.emote1 = this.emote1;
            varCopy.emote2 = this.emote2;
            varCopy.emote3 = this.emote3;
            varCopy.emote4 = this.emote4;
            varCopy.emote5 = this.emote5;
            varCopy.emote6 = this.emote6;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Cast
    public final static class Cast {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spell;
        public int spell;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint castFlags;
        public int castFlags;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint triggerFlags;
        public int triggerFlags;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint targetsLimit;
        public int targetsLimit;

        public Cast clone() {
            Cast varCopy = new Cast();

            varCopy.spell = this.spell;
            varCopy.castFlags = this.castFlags;
            varCopy.triggerFlags = this.triggerFlags;
            varCopy.targetsLimit = this.targetsLimit;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CrossCast
    public final static class CrossCast {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spell;
        public int spell;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint castFlags;
        public int castFlags;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint targetType;
        public int targetType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint targetParam1;
        public int targetParam1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint targetParam2;
        public int targetParam2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint targetParam3;
        public int targetParam3;

        public CrossCast clone() {
            CrossCast varCopy = new CrossCast();

            varCopy.spell = this.spell;
            varCopy.castFlags = this.castFlags;
            varCopy.targetType = this.targetType;
            varCopy.targetParam1 = this.targetParam1;
            varCopy.targetParam2 = this.targetParam2;
            varCopy.targetParam3 = this.targetParam3;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SummonCreature
    public final static class SummonCreature {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint type;
        public int type;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint duration;
        public int duration;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint storageID;
        public int storageID;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint attackInvoker;
        public int attackInvoker;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint flags;
        public int flags; // SmartActionSummonCreatureFlags
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint count;
        public int count;

        public SummonCreature clone() {
            SummonCreature varCopy = new SummonCreature();

            varCopy.creature = this.creature;
            varCopy.type = this.type;
            varCopy.duration = this.duration;
            varCopy.storageID = this.storageID;
            varCopy.attackInvoker = this.attackInvoker;
            varCopy.flags = this.flags;
            varCopy.count = this.count;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ThreatPCT
    public final static class ThreatPCT {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint threatINC;
        public int threatINC;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint threatDEC;
        public int threatDEC;

        public ThreatPCT clone() {
            ThreatPCT varCopy = new ThreatPCT();

            varCopy.threatINC = this.threatINC;
            varCopy.threatDEC = this.threatDEC;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CastCreatureOrGO
    public final static class CastCreatureOrGO {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint quest;
        public int quest;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spell;
        public int spell;

        public CastCreatureOrGO clone() {
            CastCreatureOrGO varCopy = new CastCreatureOrGO();

            varCopy.quest = this.quest;
            varCopy.spell = this.spell;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Threat
    public final static class Threat {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint threatINC;
        public int threatINC;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint threatDEC;
        public int threatDEC;

        public Threat clone() {
            Threat varCopy = new Threat();

            varCopy.threatINC = this.threatINC;
            varCopy.threatDEC = this.threatDEC;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct AutoAttack
    public final static class AutoAttack {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint attack;
        public int attack;

        public AutoAttack clone() {
            AutoAttack varCopy = new AutoAttack();

            varCopy.attack = this.attack;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CombatMove
    public final static class CombatMove {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint move;
        public int move;

        public CombatMove clone() {
            CombatMove varCopy = new CombatMove();

            varCopy.move = this.move;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetEventPhase
    public final static class SetEventPhase {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase;
        public int phase;

        public SetEventPhase clone() {
            SetEventPhase varCopy = new SetEventPhase();

            varCopy.phase = this.phase;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct IncEventPhase
    public final static class IncEventPhase {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint inc;
        public int inc;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dec;
        public int dec;

        public IncEventPhase clone() {
            IncEventPhase varCopy = new IncEventPhase();

            varCopy.inc = this.inc;
            varCopy.dec = this.dec;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CastedCreatureOrGO
    public final static class CastedCreatureOrGO {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spell;
        public int spell;

        public CastedCreatureOrGO clone() {
            CastedCreatureOrGO varCopy = new CastedCreatureOrGO();

            varCopy.creature = this.creature;
            varCopy.spell = this.spell;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RemoveAura
    public final static class RemoveAura {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spell;
        public int spell;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint charges;
        public int charges;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint onlyOwnedAuras;
        public int onlyOwnedAuras;

        public RemoveAura clone() {
            RemoveAura varCopy = new RemoveAura();

            varCopy.spell = this.spell;
            varCopy.charges = this.charges;
            varCopy.onlyOwnedAuras = this.onlyOwnedAuras;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Follow
    public final static class Follow {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dist;
        public int dist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint angle;
        public int angle;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint credit;
        public int credit;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creditType;
        public int creditType;

        public Follow clone() {
            Follow varCopy = new Follow();

            varCopy.dist = this.dist;
            varCopy.angle = this.angle;
            varCopy.entry = this.entry;
            varCopy.credit = this.credit;
            varCopy.creditType = this.creditType;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandomPhase
    public final static class RandomPhase {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase1;
        public int phase1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase2;
        public int phase2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase3;
        public int phase3;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase4;
        public int phase4;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase5;
        public int phase5;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phase6;
        public int phase6;

        public RandomPhase clone() {
            RandomPhase varCopy = new RandomPhase();

            varCopy.phase1 = this.phase1;
            varCopy.phase2 = this.phase2;
            varCopy.phase3 = this.phase3;
            varCopy.phase4 = this.phase4;
            varCopy.phase5 = this.phase5;
            varCopy.phase6 = this.phase6;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandomPhaseRange
    public final static class RandomPhaseRange {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phaseMin;
        public int phaseMin;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint phaseMax;
        public int phaseMax;

        public RandomPhaseRange clone() {
            RandomPhaseRange varCopy = new RandomPhaseRange();

            varCopy.phaseMin = this.phaseMin;
            varCopy.phaseMax = this.phaseMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct KilledMonster
    public final static class KilledMonster {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;

        public KilledMonster clone() {
            KilledMonster varCopy = new KilledMonster();

            varCopy.creature = this.creature;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetInstanceData
    public final static class SetInstanceData {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint field;
        public int field;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint data;
        public int data;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint type;
        public int type;

        public SetInstanceData clone() {
            SetInstanceData varCopy = new SetInstanceData();

            varCopy.field = this.field;
            varCopy.data = this.data;
            varCopy.type = this.type;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetInstanceData64
    public final static class SetInstanceData64 {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint field;
        public int field;

        public SetInstanceData64 clone() {
            SetInstanceData64 varCopy = new SetInstanceData64();

            varCopy.field = this.field;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct UpdateTemplate
    public final static class UpdateTemplate {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint updateLevel;
        public int updateLevel;

        public UpdateTemplate clone() {
            UpdateTemplate varCopy = new UpdateTemplate();

            varCopy.creature = this.creature;
            varCopy.updateLevel = this.updateLevel;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CallHelp
    public final static class CallHelp {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint range;
        public int range;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint withEmote;
        public int withEmote;

        public CallHelp clone() {
            CallHelp varCopy = new CallHelp();

            varCopy.range = this.range;
            varCopy.withEmote = this.withEmote;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetSheath
    public final static class SetSheath {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint sheath;
        public int sheath;

        public SetSheath clone() {
            SetSheath varCopy = new SetSheath();

            varCopy.sheath = this.sheath;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ForceDespawn
    public final static class ForceDespawn {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint delay;
        public int delay;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint forceRespawnTimer;
        public int forceRespawnTimer;

        public ForceDespawn clone() {
            ForceDespawn varCopy = new ForceDespawn();

            varCopy.delay = this.delay;
            varCopy.forceRespawnTimer = this.forceRespawnTimer;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct InvincHP
    public final static class InvincHP {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint minHP;
        public int minHP;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint percent;
        public int percent;

        public InvincHP clone() {
            InvincHP varCopy = new InvincHP();

            varCopy.minHP = this.minHP;
            varCopy.percent = this.percent;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct IngamePhaseId
    public final static class IngamePhaseId {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint apply;
        public int apply;

        public IngamePhaseId clone() {
            IngamePhaseId varCopy = new IngamePhaseId();

            varCopy.id = this.id;
            varCopy.apply = this.apply;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct IngamePhaseGroup
    public final static class IngamePhaseGroup {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint groupId;
        public int groupId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint apply;
        public int apply;

        public IngamePhaseGroup clone() {
            IngamePhaseGroup varCopy = new IngamePhaseGroup();

            varCopy.groupId = this.groupId;
            varCopy.apply = this.apply;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetData
    public final static class SetData {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint field;
        public int field;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint data;
        public int data;

        public SetData clone() {
            SetData varCopy = new SetData();

            varCopy.field = this.field;
            varCopy.data = this.data;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MoveRandom
    public final static class MoveRandom {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint distance;
        public int distance;

        public MoveRandom clone() {
            MoveRandom varCopy = new MoveRandom();

            varCopy.distance = this.distance;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Visibility
    public final static class Visibility {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint state;
        public int state;

        public Visibility clone() {
            Visibility varCopy = new Visibility();

            varCopy.state = this.state;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SummonGO
    public final static class SummonGO {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint despawnTime;
        public int despawnTime;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint summonType;
        public int summonType;

        public SummonGO clone() {
            SummonGO varCopy = new SummonGO();

            varCopy.entry = this.entry;
            varCopy.despawnTime = this.despawnTime;
            varCopy.summonType = this.summonType;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Active
    public final static class Active {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint state;
        public int state;

        public Active clone() {
            Active varCopy = new Active();

            varCopy.state = this.state;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Taxi
    public final static class Taxi {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public Taxi clone() {
            Taxi varCopy = new Taxi();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct WpStart
    public final static class WpStart {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint run;
        public int run;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint pathID;
        public int pathID;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint repeat;
        public int repeat;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint quest;
        public int quest;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint despawnTime;
        public int despawnTime;
        //public uint reactState; DO NOT REUSE

        public WpStart clone() {
            WpStart varCopy = new WpStart();

            varCopy.run = this.run;
            varCopy.pathID = this.pathID;
            varCopy.repeat = this.repeat;
            varCopy.quest = this.quest;
            varCopy.despawnTime = this.despawnTime;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct WpPause
    public final static class WpPause {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint delay;
        public int delay;

        public WpPause clone() {
            WpPause varCopy = new WpPause();

            varCopy.delay = this.delay;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct WpStop
    public final static class WpStop {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint despawnTime;
        public int despawnTime;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint quest;
        public int quest;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint fail;
        public int fail;

        public WpStop clone() {
            WpStop varCopy = new WpStop();

            varCopy.despawnTime = this.despawnTime;
            varCopy.quest = this.quest;
            varCopy.fail = this.fail;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Item
    public final static class Item {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint count;
        public int count;

        public Item clone() {
            Item varCopy = new Item();

            varCopy.entry = this.entry;
            varCopy.count = this.count;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetRun
    public final static class SetRun {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint run;
        public int run;

        public SetRun clone() {
            SetRun varCopy = new SetRun();

            varCopy.run = this.run;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetDisableGravity
    public final static class SetDisableGravity {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint disable;
        public int disable;

        public SetDisableGravity clone() {
            SetDisableGravity varCopy = new SetDisableGravity();

            varCopy.disable = this.disable;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Teleport
    public final static class Teleport {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint mapID;
        public int mapID;

        public Teleport clone() {
            Teleport varCopy = new Teleport();

            varCopy.mapID = this.mapID;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetCounter
    public final static class SetCounter {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint counterId;
        public int counterId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint value;
        public int value;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint reset;
        public int reset;

        public SetCounter clone() {
            SetCounter varCopy = new SetCounter();

            varCopy.counterId = this.counterId;
            varCopy.value = this.value;
            varCopy.reset = this.reset;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct StoreTargets
    public final static class StoreTargets {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public StoreTargets clone() {
            StoreTargets varCopy = new StoreTargets();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TimeEvent
    public final static class TimeEvent {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint min;
        public int min;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint max;
        public int max;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint chance;
        public int chance;

        public TimeEvent clone() {
            TimeEvent varCopy = new TimeEvent();

            varCopy.id = this.id;
            varCopy.min = this.min;
            varCopy.max = this.max;
            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;
            varCopy.chance = this.chance;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Movie
    public final static class Movie {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;

        public Movie clone() {
            Movie varCopy = new Movie();

            varCopy.entry = this.entry;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Equip
    public final static class Equip {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint mask;
        public int mask;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint slot1;
        public int slot1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint slot2;
        public int slot2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint slot3;
        public int slot3;

        public Equip clone() {
            Equip varCopy = new Equip();

            varCopy.entry = this.entry;
            varCopy.mask = this.mask;
            varCopy.slot1 = this.slot1;
            varCopy.slot2 = this.slot2;
            varCopy.slot3 = this.slot3;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Flag
    public final static class Flag {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint flag;
        public int flag;

        public Flag clone() {
            Flag varCopy = new Flag();

            varCopy.flag = this.flag;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetunitByte
    public final static class SetunitByte {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint byte1;
        public int byte1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint type;
        public int type;

        public SetunitByte clone() {
            SetunitByte varCopy = new SetunitByte();

            varCopy.byte1 = this.byte1;
            varCopy.type = this.type;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct DelunitByte
    public final static class DelunitByte {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint byte1;
        public int byte1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint type;
        public int type;

        public DelunitByte clone() {
            DelunitByte varCopy = new DelunitByte();

            varCopy.byte1 = this.byte1;
            varCopy.type = this.type;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TimedActionList
    public final static class TimedActionList {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint timerType;
        public int timerType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint allowOverride;
        public int allowOverride;

        public TimedActionList clone() {
            TimedActionList varCopy = new TimedActionList();

            varCopy.id = this.id;
            varCopy.timerType = this.timerType;
            varCopy.allowOverride = this.allowOverride;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandTimedActionList
    public final static class RandTimedActionList {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionList1;
        public int actionList1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionList2;
        public int actionList2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionList3;
        public int actionList3;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionList4;
        public int actionList4;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionList5;
        public int actionList5;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionList6;
        public int actionList6;

        public RandTimedActionList clone() {
            RandTimedActionList varCopy = new RandTimedActionList();

            varCopy.actionList1 = this.actionList1;
            varCopy.actionList2 = this.actionList2;
            varCopy.actionList3 = this.actionList3;
            varCopy.actionList4 = this.actionList4;
            varCopy.actionList5 = this.actionList5;
            varCopy.actionList6 = this.actionList6;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandRangeTimedActionList
    public final static class RandRangeTimedActionList {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint idMin;
        public int idMin;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint idMax;
        public int idMax;

        public RandRangeTimedActionList clone() {
            RandRangeTimedActionList varCopy = new RandRangeTimedActionList();

            varCopy.idMin = this.idMin;
            varCopy.idMax = this.idMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct InterruptSpellCasting
    public final static class InterruptSpellCasting {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint withDelayed;
        public int withDelayed;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spell_id;
        public int spellId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint withInstant;
        public int withInstant;

        public InterruptSpellCasting clone() {
            InterruptSpellCasting varCopy = new InterruptSpellCasting();

            varCopy.withDelayed = this.withDelayed;
            varCopy.spell_id = this.spell_id;
            varCopy.withInstant = this.withInstant;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Jump
    public final static class Jump {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint SpeedXY;
        public int speedXY;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint SpeedZ;
        public int speedZ;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint Gravity;
        public int gravity;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint UseDefaultGravity;
        public int useDefaultGravity;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint PointId;
        public int pointId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint ContactDistance;
        public int contactDistance;

        public Jump clone() {
            Jump varCopy = new Jump();

            varCopy.SpeedXY = this.SpeedXY;
            varCopy.SpeedZ = this.SpeedZ;
            varCopy.Gravity = this.Gravity;
            varCopy.UseDefaultGravity = this.UseDefaultGravity;
            varCopy.PointId = this.PointId;
            varCopy.ContactDistance = this.ContactDistance;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct FleeAssist
    public final static class FleeAssist {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint withEmote;
        public int withEmote;

        public FleeAssist clone() {
            FleeAssist varCopy = new FleeAssist();

            varCopy.withEmote = this.withEmote;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct EnableTempGO
    public final static class EnableTempGO {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint duration;
        public int duration;

        public EnableTempGO clone() {
            EnableTempGO varCopy = new EnableTempGO();

            varCopy.duration = this.duration;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MoveToPos
    public final static class MoveToPos {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint pointId;
        public int pointId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint transport;
        public int transport;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint disablePathfinding;
        public int disablePathfinding;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint contactDistance;
        public int contactDistance;

        public MoveToPos clone() {
            MoveToPos varCopy = new MoveToPos();

            varCopy.pointId = this.pointId;
            varCopy.transport = this.transport;
            varCopy.disablePathfinding = this.disablePathfinding;
            varCopy.contactDistance = this.contactDistance;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SendGossipMenu
    public final static class SendGossipMenu {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint gossipMenuId;
        public int gossipMenuId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint gossipNpcTextId;
        public int gossipNpcTextId;

        public SendGossipMenu clone() {
            SendGossipMenu varCopy = new SendGossipMenu();

            varCopy.gossipMenuId = this.gossipMenuId;
            varCopy.gossipNpcTextId = this.gossipNpcTextId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetGoLootState
    public final static class SetGoLootState {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint state;
        public int state;

        public SetGoLootState clone() {
            SetGoLootState varCopy = new SetGoLootState();

            varCopy.state = this.state;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SendTargetToTarget
    public final static class SendTargetToTarget {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public SendTargetToTarget clone() {
            SendTargetToTarget varCopy = new SendTargetToTarget();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetRangedMovement
    public final static class SetRangedMovement {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint distance;
        public int distance;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint angle;
        public int angle;

        public SetRangedMovement clone() {
            SetRangedMovement varCopy = new SetRangedMovement();

            varCopy.distance = this.distance;
            varCopy.angle = this.angle;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetHealthRegen
    public final static class SetHealthRegen {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint regenHealth;
        public int regenHealth;

        public SetHealthRegen clone() {
            SetHealthRegen varCopy = new SetHealthRegen();

            varCopy.regenHealth = this.regenHealth;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetRoot
    public final static class SetRoot {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint root;
        public int root;

        public SetRoot clone() {
            SetRoot varCopy = new SetRoot();

            varCopy.root = this.root;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GoState
    public final static class GoState {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint state;
        public int state;

        public GoState clone() {
            GoState varCopy = new GoState();

            varCopy.state = this.state;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CreatureGroup
    public final static class CreatureGroup {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint group;
        public int group;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint attackInvoker;
        public int attackInvoker;

        public CreatureGroup clone() {
            CreatureGroup varCopy = new CreatureGroup();

            varCopy.group = this.group;
            varCopy.attackInvoker = this.attackInvoker;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Power
    public final static class Power {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint powerType;
        public int powerType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint newPower;
        public int newPower;

        public Power clone() {
            Power varCopy = new Power();

            varCopy.powerType = this.powerType;
            varCopy.newPower = this.newPower;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GameEventStop
    public final static class GameEventStop {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public GameEventStop clone() {
            GameEventStop varCopy = new GameEventStop();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GameEventStart
    public final static class GameEventStart {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public GameEventStart clone() {
            GameEventStart varCopy = new GameEventStart();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ClosestWaypointFromList
    public final static class ClosestWaypointFromList {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint wp1;
        public int wp1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint wp2;
        public int wp2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint wp3;
        public int wp3;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint wp4;
        public int wp4;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint wp5;
        public int wp5;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint wp6;
        public int wp6;

        public ClosestWaypointFromList clone() {
            ClosestWaypointFromList varCopy = new ClosestWaypointFromList();

            varCopy.wp1 = this.wp1;
            varCopy.wp2 = this.wp2;
            varCopy.wp3 = this.wp3;
            varCopy.wp4 = this.wp4;
            varCopy.wp5 = this.wp5;
            varCopy.wp6 = this.wp6;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MoveOffset
    public final static class MoveOffset {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint PointId;
        public int pointId;

        public MoveOffset clone() {
            MoveOffset varCopy = new MoveOffset();

            varCopy.PointId = this.PointId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandomSound
    public final static class RandomSound {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint sound1;
        public int sound1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint sound2;
        public int sound2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint sound3;
        public int sound3;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint sound4;
        public int sound4;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint onlySelf;
        public int onlySelf;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint distance;
        public int distance;

        public RandomSound clone() {
            RandomSound varCopy = new RandomSound();

            varCopy.sound1 = this.sound1;
            varCopy.sound2 = this.sound2;
            varCopy.sound3 = this.sound3;
            varCopy.sound4 = this.sound4;
            varCopy.onlySelf = this.onlySelf;
            varCopy.distance = this.distance;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct CorpseDelay
    public final static class CorpseDelay {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint timer;
        public int timer;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint includeDecayRatio;
        public int includeDecayRatio;

        public CorpseDelay clone() {
            CorpseDelay varCopy = new CorpseDelay();

            varCopy.timer = this.timer;
            varCopy.includeDecayRatio = this.includeDecayRatio;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct DisableEvade
    public final static class DisableEvade {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint disable;
        public int disable;

        public DisableEvade clone() {
            DisableEvade varCopy = new DisableEvade();

            varCopy.disable = this.disable;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GroupSpawn
    public final static class GroupSpawn {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint groupId;
        public int groupId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint minDelay;
        public int minDelay;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDelay;
        public int maxDelay;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spawnflags;
        public int spawnflags;

        public GroupSpawn clone() {
            GroupSpawn varCopy = new GroupSpawn();

            varCopy.groupId = this.groupId;
            varCopy.minDelay = this.minDelay;
            varCopy.maxDelay = this.maxDelay;
            varCopy.spawnflags = this.spawnflags;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct LoadEquipment
    public final static class LoadEquipment {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint force;
        public int force;

        public LoadEquipment clone() {
            LoadEquipment varCopy = new LoadEquipment();

            varCopy.id = this.id;
            varCopy.force = this.force;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RandomTimedEvent
    public final static class RandomTimedEvent {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint minId;
        public int minId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxId;
        public int maxId;

        public RandomTimedEvent clone() {
            RandomTimedEvent varCopy = new RandomTimedEvent();

            varCopy.minId = this.minId;
            varCopy.maxId = this.maxId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct PauseMovement
    public final static class PauseMovement {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint movementSlot;
        public int movementSlot;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint pauseTimer;
        public int pauseTimer;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint force;
        public int force;

        public PauseMovement clone() {
            PauseMovement varCopy = new PauseMovement();

            varCopy.movementSlot = this.movementSlot;
            varCopy.pauseTimer = this.pauseTimer;
            varCopy.force = this.force;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct RespawnData
    public final static class RespawnData {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spawnType;
        public int spawnType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spawnId;
        public int spawnId;

        public RespawnData clone() {
            RespawnData varCopy = new RespawnData();

            varCopy.spawnType = this.spawnType;
            varCopy.spawnId = this.spawnId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct AnimKit
    public final static class AnimKit {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint animKit;
        public int animKit;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint type;
        public int type;

        public AnimKit clone() {
            AnimKit varCopy = new AnimKit();

            varCopy.animKit = this.animKit;
            varCopy.type = this.type;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Scene
    public final static class Scene {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint sceneId;
        public int sceneId;

        public Scene clone() {
            Scene varCopy = new Scene();

            varCopy.sceneId = this.sceneId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Cinematic
    public final static class Cinematic {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;

        public Cinematic clone() {
            Cinematic varCopy = new Cinematic();

            varCopy.entry = this.entry;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MovementSpeed
    public final static class MovementSpeed {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint movementType;
        public int movementType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint speedInteger;
        public int speedInteger;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint speedFraction;
        public int speedFraction;

        public MovementSpeed clone() {
            MovementSpeed varCopy = new MovementSpeed();

            varCopy.movementType = this.movementType;
            varCopy.speedInteger = this.speedInteger;
            varCopy.speedFraction = this.speedFraction;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SpellVisualKit
    public final static class SpellVisualKit {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint spellVisualKitId;
        public int spellVisualKitId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint kitType;
        public int kitType;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint duration;
        public int duration;

        public SpellVisualKit clone() {
            SpellVisualKit varCopy = new SpellVisualKit();

            varCopy.spellVisualKitId = this.spellVisualKitId;
            varCopy.kitType = this.kitType;
            varCopy.duration = this.duration;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct OverrideLight
    public final static class OverrideLight {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint zoneId;
        public int zoneId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint areaLightId;
        public int areaLightId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint overrideLightId;
        public int overrideLightId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint transitionMilliseconds;
        public int transitionMilliseconds;

        public OverrideLight clone() {
            OverrideLight varCopy = new OverrideLight();

            varCopy.zoneId = this.zoneId;
            varCopy.areaLightId = this.areaLightId;
            varCopy.overrideLightId = this.overrideLightId;
            varCopy.transitionMilliseconds = this.transitionMilliseconds;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct OverrideWeather
    public final static class OverrideWeather {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint zoneId;
        public int zoneId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint weatherId;
        public int weatherId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint intensity;
        public int intensity;

        public OverrideWeather clone() {
            OverrideWeather varCopy = new OverrideWeather();

            varCopy.zoneId = this.zoneId;
            varCopy.weatherId = this.weatherId;
            varCopy.intensity = this.intensity;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetHover
    public final static class SetHover {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint enable;
        public int enable;

        public SetHover clone() {
            SetHover varCopy = new SetHover();

            varCopy.enable = this.enable;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Evade
    public final static class Evade {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint toRespawnPosition;
        public int toRespawnPosition;

        public Evade clone() {
            Evade varCopy = new Evade();

            varCopy.toRespawnPosition = this.toRespawnPosition;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetHealthPct
    public final static class SetHealthPct {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint percent;
        public int percent;

        public SetHealthPct clone() {
            SetHealthPct varCopy = new SetHealthPct();

            varCopy.percent = this.percent;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Conversation
    public final static class Conversation {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public Conversation clone() {
            Conversation varCopy = new Conversation();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetImmunePC
    public final static class SetImmunePC {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint immunePC;
        public int immunePC;

        public SetImmunePC clone() {
            SetImmunePC varCopy = new SetImmunePC();

            varCopy.immunePC = this.immunePC;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetImmuneNPC
    public final static class SetImmuneNPC {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint immuneNPC;
        public int immuneNPC;

        public SetImmuneNPC clone() {
            SetImmuneNPC varCopy = new SetImmuneNPC();

            varCopy.immuneNPC = this.immuneNPC;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SetUninteractible
    public final static class SetUninteractible {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint uninteractible;
        public int uninteractible;

        public SetUninteractible clone() {
            SetUninteractible varCopy = new SetUninteractible();

            varCopy.uninteractible = this.uninteractible;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ActivateGameObject
    public final static class ActivateGameObject {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint gameObjectAction;
        public int gameObjectAction;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param;
        public int param;

        public ActivateGameObject clone() {
            ActivateGameObject varCopy = new ActivateGameObject();

            varCopy.gameObjectAction = this.gameObjectAction;
            varCopy.param = this.param;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct AddToStoredTargets
    public final static class AddToStoredTargets {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public AddToStoredTargets clone() {
            AddToStoredTargets varCopy = new AddToStoredTargets();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct BecomePersonalClone
    public final static class BecomePersonalClone {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint type;
        public int type;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint duration;
        public int duration;

        public BecomePersonalClone clone() {
            BecomePersonalClone varCopy = new BecomePersonalClone();

            varCopy.type = this.type;
            varCopy.duration = this.duration;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TriggerGameEvent
    public final static class TriggerGameEvent {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint eventId;
        public int eventId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint useSaiTargetAsGameEventSource;
        public int useSaiTargetAsGameEventSource;

        public TriggerGameEvent clone() {
            TriggerGameEvent varCopy = new TriggerGameEvent();

            varCopy.eventId = this.eventId;
            varCopy.useSaiTargetAsGameEventSource = this.useSaiTargetAsGameEventSource;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct DoAction
    public final static class DoAction {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint actionId;
        public int actionId;

        public DoAction clone() {
            DoAction varCopy = new DoAction();

            varCopy.actionId = this.actionId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Raw
    public final static class Raw {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param1;
        public int param1;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param2;
        public int param2;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param3;
        public int param3;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param4;
        public int param4;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param5;
        public int param5;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param6;
        public int param6;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint param7;
        public int param7;

        public Raw clone() {
            Raw varCopy = new Raw();

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

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
        ///#endregion

    public SmartAction clone() {
        SmartAction varCopy = new SmartAction();

        varCopy.type = this.type;
        varCopy.talk = this.talk.clone();
        varCopy.simpleTalk = this.simpleTalk.clone();
        varCopy.faction = this.faction.clone();
        varCopy.morphOrMount = this.morphOrMount.clone();
        varCopy.sound = this.sound.clone();
        varCopy.emote = this.emote.clone();
        varCopy.quest = this.quest.clone();
        varCopy.questOffer = this.questOffer.clone();
        varCopy.react = this.react.clone();
        varCopy.randomEmote = this.randomEmote.clone();
        varCopy.cast = this.cast.clone();
        varCopy.crossCast = this.crossCast.clone();
        varCopy.summonCreature = this.summonCreature.clone();
        varCopy.threatPCT = this.threatPCT.clone();
        varCopy.threat = this.threat.clone();
        varCopy.castCreatureOrGO = this.castCreatureOrGO.clone();
        varCopy.autoAttack = this.autoAttack.clone();
        varCopy.combatMove = this.combatMove.clone();
        varCopy.setEventPhase = this.setEventPhase.clone();
        varCopy.incEventPhase = this.incEventPhase.clone();
        varCopy.castedCreatureOrGO = this.castedCreatureOrGO.clone();
        varCopy.removeAura = this.removeAura.clone();
        varCopy.follow = this.follow.clone();
        varCopy.randomPhase = this.randomPhase.clone();
        varCopy.randomPhaseRange = this.randomPhaseRange.clone();
        varCopy.killedMonster = this.killedMonster.clone();
        varCopy.setInstanceData = this.setInstanceData.clone();
        varCopy.setInstanceData64 = this.setInstanceData64.clone();
        varCopy.updateTemplate = this.updateTemplate.clone();
        varCopy.callHelp = this.callHelp.clone();
        varCopy.setSheath = this.setSheath.clone();
        varCopy.forceDespawn = this.forceDespawn.clone();
        varCopy.invincHP = this.invincHP.clone();
        varCopy.ingamePhaseId = this.ingamePhaseId.clone();
        varCopy.ingamePhaseGroup = this.ingamePhaseGroup.clone();
        varCopy.setData = this.setData.clone();
        varCopy.moveRandom = this.moveRandom.clone();
        varCopy.visibility = this.visibility.clone();
        varCopy.summonGO = this.summonGO.clone();
        varCopy.active = this.active.clone();
        varCopy.taxi = this.taxi.clone();
        varCopy.wpStart = this.wpStart.clone();
        varCopy.wpPause = this.wpPause.clone();
        varCopy.wpStop = this.wpStop.clone();
        varCopy.item = this.item.clone();
        varCopy.setRun = this.setRun.clone();
        varCopy.setDisableGravity = this.setDisableGravity.clone();
        varCopy.teleport = this.teleport.clone();
        varCopy.setCounter = this.setCounter.clone();
        varCopy.storeTargets = this.storeTargets.clone();
        varCopy.timeEvent = this.timeEvent.clone();
        varCopy.movie = this.movie.clone();
        varCopy.equip = this.equip.clone();
        varCopy.flag = this.flag.clone();
        varCopy.setunitByte = this.setunitByte.clone();
        varCopy.delunitByte = this.delunitByte.clone();
        varCopy.timedActionList = this.timedActionList.clone();
        varCopy.randTimedActionList = this.randTimedActionList.clone();
        varCopy.randRangeTimedActionList = this.randRangeTimedActionList.clone();
        varCopy.interruptSpellCasting = this.interruptSpellCasting.clone();
        varCopy.jump = this.jump.clone();
        varCopy.fleeAssist = this.fleeAssist.clone();
        varCopy.enableTempGO = this.enableTempGO.clone();
        varCopy.moveToPos = this.moveToPos.clone();
        varCopy.sendGossipMenu = this.sendGossipMenu.clone();
        varCopy.setGoLootState = this.setGoLootState.clone();
        varCopy.sendTargetToTarget = this.sendTargetToTarget.clone();
        varCopy.setRangedMovement = this.setRangedMovement.clone();
        varCopy.setHealthRegen = this.setHealthRegen.clone();
        varCopy.setRoot = this.setRoot.clone();
        varCopy.goState = this.goState.clone();
        varCopy.creatureGroup = this.creatureGroup.clone();
        varCopy.power = this.power.clone();
        varCopy.gameEventStop = this.gameEventStop.clone();
        varCopy.gameEventStart = this.gameEventStart.clone();
        varCopy.closestWaypointFromList = this.closestWaypointFromList.clone();
        varCopy.moveOffset = this.moveOffset.clone();
        varCopy.randomSound = this.randomSound.clone();
        varCopy.corpseDelay = this.corpseDelay.clone();
        varCopy.disableEvade = this.disableEvade.clone();
        varCopy.groupSpawn = this.groupSpawn.clone();
        varCopy.auraType = this.auraType;
        varCopy.loadEquipment = this.loadEquipment.clone();
        varCopy.randomTimedEvent = this.randomTimedEvent.clone();
        varCopy.pauseMovement = this.pauseMovement.clone();
        varCopy.respawnData = this.respawnData.clone();
        varCopy.animKit = this.animKit.clone();
        varCopy.scene = this.scene.clone();
        varCopy.cinematic = this.cinematic.clone();
        varCopy.movementSpeed = this.movementSpeed.clone();
        varCopy.spellVisualKit = this.spellVisualKit.clone();
        varCopy.overrideLight = this.overrideLight.clone();
        varCopy.overrideWeather = this.overrideWeather.clone();
        varCopy.setHover = this.setHover.clone();
        varCopy.evade = this.evade.clone();
        varCopy.setHealthPct = this.setHealthPct.clone();
        varCopy.conversation = this.conversation.clone();
        varCopy.setImmunePC = this.setImmunePC.clone();
        varCopy.setImmuneNPC = this.setImmuneNPC.clone();
        varCopy.setUninteractible = this.setUninteractible.clone();
        varCopy.activateGameObject = this.activateGameObject.clone();
        varCopy.addToStoredTargets = this.addToStoredTargets.clone();
        varCopy.becomePersonalClone = this.becomePersonalClone.clone();
        varCopy.triggerGameEvent = this.triggerGameEvent.clone();
        varCopy.doAction = this.doAction.clone();
        varCopy.raw = this.raw.clone();

        return varCopy;
    }
}