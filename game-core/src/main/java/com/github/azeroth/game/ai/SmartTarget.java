package game.ai;

import Framework.Constants.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StructLayout(LayoutKind.Explicit)] public struct SmartTarget
//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: [StructLayout(LayoutKind.Explicit)] public struct SmartTarget
public final class SmartTarget {
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(0)] public SmartTargets type;
    public SmartTargets type = SmartTargets.values()[0];

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(4)] public float x;
    public float x;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(8)] public float y;
    public float y;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(12)] public float z;
    public float z;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(16)] public float o;
    public float o;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public HostilRandom hostilRandom;
    public HostilRandom hostilRandom = new HostilRandom();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public Farthest farthest;
    public Farthest farthest = new Farthest();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public UnitRange unitRange;
    public UnitRange unitRange = new UnitRange();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public UnitGUID unitGUID;
    public UnitGUID unitGUID = new UnitGUID();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public UnitDistance unitDistance;
    public UnitDistance unitDistance = new UnitDistance();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public PlayerDistance playerDistance;
    public PlayerDistance playerDistance = new PlayerDistance();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public PlayerRange playerRange;
    public PlayerRange playerRange = new PlayerRange();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public Stored stored;
    public Stored stored = new Stored();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public GoRange goRange;
    public GoRange goRange = new GoRange();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public GoGUID goGUID;
    public GoGUID goGUID = new GoGUID();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public GoDistance goDistance;
    public GoDistance goDistance = new GoDistance();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public UnitClosest unitClosest;
    public UnitClosest unitClosest = new UnitClosest();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public GoClosest goClosest;
    public GoClosest goClosest = new GoClosest();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public ClosestAttackable closestAttackable;
    public ClosestAttackable closestAttackable = new ClosestAttackable();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public ClosestFriendly closestFriendly;
    public ClosestFriendly closestFriendly = new ClosestFriendly();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public Owner owner;
    public Owner owner = new Owner();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public Vehicle vehicle;
    public Vehicle vehicle = new Vehicle();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public ThreatList threatList;
    public ThreatList threatList = new ThreatList();

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [FieldOffset(20)] public Raw raw;
    public Raw raw = new Raw();

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
        ///#region Structs

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct HostilRandom
    public final static class HostilRandom {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint playerOnly;
        public int playerOnly;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint powerType;
        public int powerType;

        public HostilRandom clone() {
            HostilRandom varCopy = new HostilRandom();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;
            varCopy.powerType = this.powerType;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Farthest
    public final static class Farthest {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint playerOnly;
        public int playerOnly;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint isInLos;
        public int isInLos;

        public Farthest clone() {
            Farthest varCopy = new Farthest();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;
            varCopy.isInLos = this.isInLos;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct UnitRange
    public final static class UnitRange {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint minDist;
        public int minDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxSize;
        public int maxSize;

        public UnitRange clone() {
            UnitRange varCopy = new UnitRange();

            varCopy.creature = this.creature;
            varCopy.minDist = this.minDist;
            varCopy.maxDist = this.maxDist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct UnitGUID
    public final static class UnitGUID {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dbGuid;
        public int dbGuid;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;

        public UnitGUID clone() {
            UnitGUID varCopy = new UnitGUID();

            varCopy.dbGuid = this.dbGuid;
            varCopy.entry = this.entry;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct UnitDistance
    public final static class UnitDistance {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint creature;
        public int creature;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dist;
        public int dist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxSize;
        public int maxSize;

        public UnitDistance clone() {
            UnitDistance varCopy = new UnitDistance();

            varCopy.creature = this.creature;
            varCopy.dist = this.dist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct PlayerDistance
    public final static class PlayerDistance {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dist;
        public int dist;

        public PlayerDistance clone() {
            PlayerDistance varCopy = new PlayerDistance();

            varCopy.dist = this.dist;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct PlayerRange
    public final static class PlayerRange {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint minDist;
        public int minDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;

        public PlayerRange clone() {
            PlayerRange varCopy = new PlayerRange();

            varCopy.minDist = this.minDist;
            varCopy.maxDist = this.maxDist;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Stored
    public final static class Stored {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint id;
        public int id;

        public Stored clone() {
            Stored varCopy = new Stored();

            varCopy.id = this.id;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GoRange
    public final static class GoRange {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint minDist;
        public int minDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxSize;
        public int maxSize;

        public GoRange clone() {
            GoRange varCopy = new GoRange();

            varCopy.entry = this.entry;
            varCopy.minDist = this.minDist;
            varCopy.maxDist = this.maxDist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GoGUID
    public final static class GoGUID {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dbGuid;
        public int dbGuid;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;

        public GoGUID clone() {
            GoGUID varCopy = new GoGUID();

            varCopy.dbGuid = this.dbGuid;
            varCopy.entry = this.entry;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GoDistance
    public final static class GoDistance {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dist;
        public int dist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxSize;
        public int maxSize;

        public GoDistance clone() {
            GoDistance varCopy = new GoDistance();

            varCopy.entry = this.entry;
            varCopy.dist = this.dist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct UnitClosest
    public final static class UnitClosest {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dist;
        public int dist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dead;
        public int dead;

        public UnitClosest clone() {
            UnitClosest varCopy = new UnitClosest();

            varCopy.entry = this.entry;
            varCopy.dist = this.dist;
            varCopy.dead = this.dead;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct GoClosest
    public final static class GoClosest {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint entry;
        public int entry;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint dist;
        public int dist;

        public GoClosest clone() {
            GoClosest varCopy = new GoClosest();

            varCopy.entry = this.entry;
            varCopy.dist = this.dist;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ClosestAttackable
    public final static class ClosestAttackable {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint playerOnly;
        public int playerOnly;

        public ClosestAttackable clone() {
            ClosestAttackable varCopy = new ClosestAttackable();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ClosestFriendly
    public final static class ClosestFriendly {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint playerOnly;
        public int playerOnly;

        public ClosestFriendly clone() {
            ClosestFriendly varCopy = new ClosestFriendly();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Owner
    public final static class Owner {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint useCharmerOrOwner;
        public int useCharmerOrOwner;

        public Owner clone() {
            Owner varCopy = new Owner();

            varCopy.useCharmerOrOwner = this.useCharmerOrOwner;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct Vehicle
    public final static class Vehicle {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint seatMask;
        public int seatMask;

        public Vehicle clone() {
            Vehicle varCopy = new Vehicle();

            varCopy.seatMask = this.seatMask;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: public struct ThreatList
    public final static class ThreatList {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint maxDist;
        public int maxDist;

        public ThreatList clone() {
            ThreatList varCopy = new ThreatList();

            varCopy.maxDist = this.maxDist;

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

        public Raw clone() {
            Raw varCopy = new Raw();

            varCopy.param1 = this.param1;
            varCopy.param2 = this.param2;
            varCopy.param3 = this.param3;
            varCopy.param4 = this.param4;

            return varCopy;
        }
    }

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
        ///#endregion

    public SmartTarget clone() {
        SmartTarget varCopy = new SmartTarget();

        varCopy.type = this.type;
        varCopy.x = this.x;
        varCopy.y = this.y;
        varCopy.z = this.z;
        varCopy.o = this.o;
        varCopy.hostilRandom = this.hostilRandom.clone();
        varCopy.farthest = this.farthest.clone();
        varCopy.unitRange = this.unitRange.clone();
        varCopy.unitGUID = this.unitGUID.clone();
        varCopy.unitDistance = this.unitDistance.clone();
        varCopy.playerDistance = this.playerDistance.clone();
        varCopy.playerRange = this.playerRange.clone();
        varCopy.stored = this.stored.clone();
        varCopy.goRange = this.goRange.clone();
        varCopy.goGUID = this.goGUID.clone();
        varCopy.goDistance = this.goDistance.clone();
        varCopy.unitClosest = this.unitClosest.clone();
        varCopy.goClosest = this.goClosest.clone();
        varCopy.closestAttackable = this.closestAttackable.clone();
        varCopy.closestFriendly = this.closestFriendly.clone();
        varCopy.owner = this.owner.clone();
        varCopy.vehicle = this.vehicle.clone();
        varCopy.threatList = this.threatList.clone();
        varCopy.raw = this.raw.clone();

        return varCopy;
    }
}