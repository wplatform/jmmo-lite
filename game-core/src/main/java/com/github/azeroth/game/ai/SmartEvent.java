package com.github.azeroth.game.ai;









//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StructLayout(LayoutKind.Explicit)] public struct SmartEvent
//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: [StructLayout(LayoutKind.Explicit)] public struct SmartEvent
public final class SmartEvent {
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(0)] public SmartEvents type;
    public SmartEvents type = SmartEvents.values()[0];

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public uint event_phase_mask;

//ORIGINAL LINE: [FieldOffset(4)] public uint event_phase_mask;
    public int eventPhaseMask;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(8)] public uint event_chance;

//ORIGINAL LINE: [FieldOffset(8)] public uint event_chance;
    public int eventChance;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(12)] public SmartEventFlags event_flags;
    public SmartEventFlags eventFlags = SmartEventFlags.values()[0];

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public MinMaxRepeat minMaxRepeat;
    public MinMaxRepeat minMaxRepeat = new MinMaxRepeat();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Kill kill;
    public Kill kill = new Kill();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public SpellHit spellHit;
    public SpellHit spellHit = new SpellHit();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Los los;
    public Los los = new Los();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Respawn respawn;
    public Respawn respawn = new Respawn();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public MinMax minMax;
    public MinMax minMax = new MinMax();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public TargetCasting targetCasting;
    public TargetCasting targetCasting = new TargetCasting();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public FriendlyCC friendlyCC;
    public FriendlyCC friendlyCC = new FriendlyCC();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public MissingBuff missingBuff;
    public MissingBuff missingBuff = new MissingBuff();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Summoned summoned;
    public Summoned summoned = new Summoned();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Quest quest;
    public Quest quest = new Quest();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public QuestObjective questObjective;
    public QuestObjective questObjective = new QuestObjective();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Emote emote;
    public Emote emote = new Emote();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Aura aura;
    public Aura aura = new Aura();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Charm charm;
    public Charm charm = new Charm();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public MovementInform movementInform;
    public MovementInform movementInform = new MovementInform();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public DataSet dataSet;
    public DataSet dataSet = new DataSet();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Waypoint waypoint;
    public Waypoint waypoint = new Waypoint();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public TransportAddCreature transportAddCreature;
    public TransportAddCreature transportAddCreature = new TransportAddCreature();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public TransportRelocate transportRelocate;
    public TransportRelocate transportRelocate = new TransportRelocate();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public InstancePlayerEnter instancePlayerEnter;
    public InstancePlayerEnter instancePlayerEnter = new InstancePlayerEnter();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Areatrigger areatrigger;
    public Areatrigger areatrigger = new Areatrigger();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public TextOver textOver;
    public TextOver textOver = new TextOver();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public TimedEvent timedEvent;
    public TimedEvent timedEvent = new TimedEvent();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public GossipHello gossipHello;
    public GossipHello gossipHello = new GossipHello();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Gossip gossip;
    public Gossip gossip = new Gossip();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public GameEvent gameEvent;
    public GameEvent gameEvent = new GameEvent();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public GoLootStateChanged goLootStateChanged;
    public GoLootStateChanged goLootStateChanged = new GoLootStateChanged();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public EventInform eventInform;
    public EventInform eventInform = new EventInform();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public DoAction doAction;
    public DoAction doAction = new DoAction();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public FriendlyHealthPct friendlyHealthPct;
    public FriendlyHealthPct friendlyHealthPct = new FriendlyHealthPct();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Distance distance;
    public Distance distance = new Distance();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Counter counter;
    public Counter counter = new Counter();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public SpellCast spellCast;
    public SpellCast spellCast = new SpellCast();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Spell spell;
    public Spell spell = new Spell();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public Raw raw;
    public Raw raw = new Raw();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(40)] public string param_string;
    public String paramString;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
        ///#region Structs

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MinMaxRepeat
    public final static class MinMaxRepeat {

//ORIGINAL LINE: public uint min;
        public int min;

//ORIGINAL LINE: public uint max;
        public int max;

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

        public MinMaxRepeat clone() {
            MinMaxRepeat varCopy = new MinMaxRepeat();

            varCopy.min = this.min;
            varCopy.max = this.max;
            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Kill
    public final static class Kill {

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

//ORIGINAL LINE: public uint playerOnly;
        public int playerOnly;

//ORIGINAL LINE: public uint creature;
        public int creature;

        public Kill clone() {
            Kill varCopy = new Kill();

            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;
            varCopy.playerOnly = this.playerOnly;
            varCopy.creature = this.creature;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SpellHit
    public final static class SpellHit {

//ORIGINAL LINE: public uint spell;
        public int spell;

//ORIGINAL LINE: public uint school;
        public int school;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public SpellHit clone() {
            SpellHit varCopy = new SpellHit();

            varCopy.spell = this.spell;
            varCopy.school = this.school;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Los
    public final static class Los {

//ORIGINAL LINE: public uint hostilityMode;
        public int hostilityMode;

//ORIGINAL LINE: public uint maxDist;
        public int maxDist;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

//ORIGINAL LINE: public uint playerOnly;
        public int playerOnly;

        public Los clone() {
            Los varCopy = new Los();

            varCopy.hostilityMode = this.hostilityMode;
            varCopy.maxDist = this.maxDist;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;
            varCopy.playerOnly = this.playerOnly;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Respawn
    public final static class Respawn {

//ORIGINAL LINE: public uint type;
        public int type;

//ORIGINAL LINE: public uint map;
        public int map;

//ORIGINAL LINE: public uint area;
        public int area;

        public Respawn clone() {
            Respawn varCopy = new Respawn();

            varCopy.type = this.type;
            varCopy.map = this.map;
            varCopy.area = this.area;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MinMax
    public final static class MinMax {

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

        public MinMax clone() {
            MinMax varCopy = new MinMax();

            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TargetCasting
    public final static class TargetCasting {

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

//ORIGINAL LINE: public uint spellId;
        public int spellId;

        public TargetCasting clone() {
            TargetCasting varCopy = new TargetCasting();

            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;
            varCopy.spellId = this.spellId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct FriendlyCC
    public final static class FriendlyCC {

//ORIGINAL LINE: public uint radius;
        public int radius;

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

        public FriendlyCC clone() {
            FriendlyCC varCopy = new FriendlyCC();

            varCopy.radius = this.radius;
            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MissingBuff
    public final static class MissingBuff {

//ORIGINAL LINE: public uint spell;
        public int spell;

//ORIGINAL LINE: public uint radius;
        public int radius;

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

        public MissingBuff clone() {
            MissingBuff varCopy = new MissingBuff();

            varCopy.spell = this.spell;
            varCopy.radius = this.radius;
            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Summoned
    public final static class Summoned {

//ORIGINAL LINE: public uint creature;
        public int creature;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public Summoned clone() {
            Summoned varCopy = new Summoned();

            varCopy.creature = this.creature;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Quest
    public final static class Quest {

//ORIGINAL LINE: public uint questId;
        public int questId;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public Quest clone() {
            Quest varCopy = new Quest();

            varCopy.questId = this.questId;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct QuestObjective
    public final static class QuestObjective {

//ORIGINAL LINE: public uint id;
        public int id;

        public QuestObjective clone() {
            QuestObjective varCopy = new QuestObjective();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Emote
    public final static class Emote {

//ORIGINAL LINE: public uint emoteId;
        public int emoteId;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public Emote clone() {
            Emote varCopy = new Emote();

            varCopy.emoteId = this.emoteId;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Aura
    public final static class Aura {

//ORIGINAL LINE: public uint spell;
        public int spell;

//ORIGINAL LINE: public uint count;
        public int count;

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

        public Aura clone() {
            Aura varCopy = new Aura();

            varCopy.spell = this.spell;
            varCopy.count = this.count;
            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Charm
    public final static class Charm {

//ORIGINAL LINE: public uint onRemove;
        public int onRemove;

        public Charm clone() {
            Charm varCopy = new Charm();

            varCopy.onRemove = this.onRemove;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct MovementInform
    public final static class MovementInform {

//ORIGINAL LINE: public uint type;
        public int type;

//ORIGINAL LINE: public uint id;
        public int id;

        public MovementInform clone() {
            MovementInform varCopy = new MovementInform();

            varCopy.type = this.type;
            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct DataSet
    public final static class DataSet {

//ORIGINAL LINE: public uint id;
        public int id;

//ORIGINAL LINE: public uint value;
        public int value;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public DataSet clone() {
            DataSet varCopy = new DataSet();

            varCopy.id = this.id;
            varCopy.value = this.value;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Waypoint
    public final static class Waypoint {

//ORIGINAL LINE: public uint pointID;
        public int pointID;

//ORIGINAL LINE: public uint pathID;
        public int pathID;

        public Waypoint clone() {
            Waypoint varCopy = new Waypoint();

            varCopy.pointID = this.pointID;
            varCopy.pathID = this.pathID;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TransportAddCreature
    public final static class TransportAddCreature {

//ORIGINAL LINE: public uint creature;
        public int creature;

        public TransportAddCreature clone() {
            TransportAddCreature varCopy = new TransportAddCreature();

            varCopy.creature = this.creature;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TransportRelocate
    public final static class TransportRelocate {

//ORIGINAL LINE: public uint pointID;
        public int pointID;

        public TransportRelocate clone() {
            TransportRelocate varCopy = new TransportRelocate();

            varCopy.pointID = this.pointID;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct InstancePlayerEnter
    public final static class InstancePlayerEnter {

//ORIGINAL LINE: public uint team;
        public int team;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public InstancePlayerEnter clone() {
            InstancePlayerEnter varCopy = new InstancePlayerEnter();

            varCopy.team = this.team;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Areatrigger
    public final static class Areatrigger {

//ORIGINAL LINE: public uint id;
        public int id;

        public Areatrigger clone() {
            Areatrigger varCopy = new Areatrigger();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TextOver
    public final static class TextOver {

//ORIGINAL LINE: public uint textGroupID;
        public int textGroupID;

//ORIGINAL LINE: public uint creatureEntry;
        public int creatureEntry;

        public TextOver clone() {
            TextOver varCopy = new TextOver();

            varCopy.textGroupID = this.textGroupID;
            varCopy.creatureEntry = this.creatureEntry;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct TimedEvent
    public final static class TimedEvent {

//ORIGINAL LINE: public uint id;
        public int id;

        public TimedEvent clone() {
            TimedEvent varCopy = new TimedEvent();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GossipHello
    public final static class GossipHello {

//ORIGINAL LINE: public uint filter;
        public int filter;

        public GossipHello clone() {
            GossipHello varCopy = new GossipHello();

            varCopy.filter = this.filter;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Gossip
    public final static class Gossip {

//ORIGINAL LINE: public uint sender;
        public int sender;

//ORIGINAL LINE: public uint action;
        public int action;

        public Gossip clone() {
            Gossip varCopy = new Gossip();

            varCopy.sender = this.sender;
            varCopy.action = this.action;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GameEvent
    public final static class GameEvent {

//ORIGINAL LINE: public uint gameEventId;
        public int gameEventId;

        public GameEvent clone() {
            GameEvent varCopy = new GameEvent();

            varCopy.gameEventId = this.gameEventId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GoLootStateChanged
    public final static class GoLootStateChanged {

//ORIGINAL LINE: public uint lootState;
        public int lootState;

        public GoLootStateChanged clone() {
            GoLootStateChanged varCopy = new GoLootStateChanged();

            varCopy.lootState = this.lootState;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct EventInform
    public final static class EventInform {

//ORIGINAL LINE: public uint eventId;
        public int eventId;

        public EventInform clone() {
            EventInform varCopy = new EventInform();

            varCopy.eventId = this.eventId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct DoAction
    public final static class DoAction {

//ORIGINAL LINE: public uint eventId;
        public int eventId;

        public DoAction clone() {
            DoAction varCopy = new DoAction();

            varCopy.eventId = this.eventId;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct FriendlyHealthPct
    public final static class FriendlyHealthPct {

//ORIGINAL LINE: public uint minHpPct;
        public int minHpPct;

//ORIGINAL LINE: public uint maxHpPct;
        public int maxHpPct;

//ORIGINAL LINE: public uint repeatMin;
        public int repeatMin;

//ORIGINAL LINE: public uint repeatMax;
        public int repeatMax;

//ORIGINAL LINE: public uint radius;
        public int radius;

        public FriendlyHealthPct clone() {
            FriendlyHealthPct varCopy = new FriendlyHealthPct();

            varCopy.minHpPct = this.minHpPct;
            varCopy.maxHpPct = this.maxHpPct;
            varCopy.repeatMin = this.repeatMin;
            varCopy.repeatMax = this.repeatMax;
            varCopy.radius = this.radius;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Distance
    public final static class Distance {

//ORIGINAL LINE: public uint guid;
        public int guid;

//ORIGINAL LINE: public uint entry;
        public int entry;

//ORIGINAL LINE: public uint dist;
        public int dist;

//ORIGINAL LINE: public uint repeat;
        public int repeat;

        public Distance clone() {
            Distance varCopy = new Distance();

            varCopy.guid = this.guid;
            varCopy.entry = this.entry;
            varCopy.dist = this.dist;
            varCopy.repeat = this.repeat;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Counter
    public final static class Counter {

//ORIGINAL LINE: public uint id;
        public int id;

//ORIGINAL LINE: public uint value;
        public int value;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public Counter clone() {
            Counter varCopy = new Counter();

            varCopy.id = this.id;
            varCopy.value = this.value;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct SpellCast
    public final static class SpellCast {

//ORIGINAL LINE: public uint spell;
        public int spell;

//ORIGINAL LINE: public uint cooldownMin;
        public int cooldownMin;

//ORIGINAL LINE: public uint cooldownMax;
        public int cooldownMax;

        public SpellCast clone() {
            SpellCast varCopy = new SpellCast();

            varCopy.spell = this.spell;
            varCopy.cooldownMin = this.cooldownMin;
            varCopy.cooldownMax = this.cooldownMax;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Spell
    public final static class Spell {

//ORIGINAL LINE: public uint effIndex;
        public int effIndex;

        public Spell clone() {
            Spell varCopy = new Spell();

            varCopy.effIndex = this.effIndex;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Raw
    public final static class Raw {

//ORIGINAL LINE: public uint param1;
        public int param1;

//ORIGINAL LINE: public uint param2;
        public int param2;

//ORIGINAL LINE: public uint param3;
        public int param3;

//ORIGINAL LINE: public uint param4;
        public int param4;

//ORIGINAL LINE: public uint param5;
        public int param5;

        public Raw clone() {
            Raw varCopy = new Raw();

            varCopy.param1 = this.param1;
            varCopy.param2 = this.param2;
            varCopy.param3 = this.param3;
            varCopy.param4 = this.param4;
            varCopy.param5 = this.param5;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
        ///#endregion

    public SmartEvent clone() {
        SmartEvent varCopy = new SmartEvent();

        varCopy.type = this.type;
        varCopy.event_phase_mask = this.event_phase_mask;
        varCopy.event_chance = this.event_chance;
        varCopy.event_flags = this.event_flags;
        varCopy.minMaxRepeat = this.minMaxRepeat.clone();
        varCopy.kill = this.kill.clone();
        varCopy.spellHit = this.spellHit.clone();
        varCopy.los = this.los.clone();
        varCopy.respawn = this.respawn.clone();
        varCopy.minMax = this.minMax.clone();
        varCopy.targetCasting = this.targetCasting.clone();
        varCopy.friendlyCC = this.friendlyCC.clone();
        varCopy.missingBuff = this.missingBuff.clone();
        varCopy.summoned = this.summoned.clone();
        varCopy.quest = this.quest.clone();
        varCopy.questObjective = this.questObjective.clone();
        varCopy.emote = this.emote.clone();
        varCopy.aura = this.aura.clone();
        varCopy.charm = this.charm.clone();
        varCopy.movementInform = this.movementInform.clone();
        varCopy.dataSet = this.dataSet.clone();
        varCopy.waypoint = this.waypoint.clone();
        varCopy.transportAddCreature = this.transportAddCreature.clone();
        varCopy.transportRelocate = this.transportRelocate.clone();
        varCopy.instancePlayerEnter = this.instancePlayerEnter.clone();
        varCopy.areatrigger = this.areatrigger.clone();
        varCopy.textOver = this.textOver.clone();
        varCopy.timedEvent = this.timedEvent.clone();
        varCopy.gossipHello = this.gossipHello.clone();
        varCopy.gossip = this.gossip.clone();
        varCopy.gameEvent = this.gameEvent.clone();
        varCopy.goLootStateChanged = this.goLootStateChanged.clone();
        varCopy.eventInform = this.eventInform.clone();
        varCopy.doAction = this.doAction.clone();
        varCopy.friendlyHealthPct = this.friendlyHealthPct.clone();
        varCopy.distance = this.distance.clone();
        varCopy.counter = this.counter.clone();
        varCopy.spellCast = this.spellCast.clone();
        varCopy.spell = this.spell.clone();
        varCopy.raw = this.raw.clone();
        varCopy.param_string = this.param_string;

        return varCopy;
    }
}