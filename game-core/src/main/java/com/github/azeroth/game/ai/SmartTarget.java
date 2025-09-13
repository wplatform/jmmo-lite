package com.github.azeroth.game.ai;



public final class SmartTarget {
    
    public SmartTargets type = SmartTargets.values()[0];

    
    public float x;

    
    public float y;

    
    public float z;

    
    public float o;

    
    public hostilRandom hostilRandom = new hostilRandom();

    
    public farthest farthest = new farthest();

    
    public unitRange unitRange = new unitRange();

    
    public unitGUID unitGUID = new unitGUID();

    
    public unitDistance unitDistance = new unitDistance();

    
    public playerDistance playerDistance = new playerDistance();

    
    public playerRange playerRange = new playerRange();

    
    public stored stored = new stored();

    
    public goRange goRange = new goRange();

    
    public goGUID goGUID = new goGUID();

    
    public goDistance goDistance = new goDistance();

    
    public unitClosest unitClosest = new unitClosest();

    
    public goClosest goClosest = new goClosest();

    
    public closestAttackable closestAttackable = new closestAttackable();

    
    public closestFriendly closestFriendly = new closestFriendly();

    
    public owner owner = new owner();

    
    public vehicle vehicle = new vehicle();

    
    public threatList threatList = new threatList();

    
    public raw raw = new raw();


    ///#region Structs

    public SmartTarget clone() {
        SmartTarget varCopy = new smartTarget();

        varCopy.type = this.type;
        varCopy.x = this.x;
        varCopy.y = this.y;
        varCopy.z = this.z;
        varCopy.o = this.o;
        varCopy.hostilRandom = this.hostilRandom;
        varCopy.farthest = this.farthest;
        varCopy.unitRange = this.unitRange;
        varCopy.unitGUID = this.unitGUID;
        varCopy.unitDistance = this.unitDistance;
        varCopy.playerDistance = this.playerDistance;
        varCopy.playerRange = this.playerRange;
        varCopy.stored = this.stored;
        varCopy.goRange = this.goRange;
        varCopy.goGUID = this.goGUID;
        varCopy.goDistance = this.goDistance;
        varCopy.unitClosest = this.unitClosest;
        varCopy.goClosest = this.goClosest;
        varCopy.closestAttackable = this.closestAttackable;
        varCopy.closestFriendly = this.closestFriendly;
        varCopy.owner = this.owner;
        varCopy.vehicle = this.vehicle;
        varCopy.threatList = this.threatList;
        varCopy.raw = this.raw;

        return varCopy;
    }

    public final static class HostilRandom {
        public int maxDist;
        public int playerOnly;
        public int powerType;

        public HostilRandom clone() {
            HostilRandom varCopy = new hostilRandom();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;
            varCopy.powerType = this.powerType;

            return varCopy;
        }
    }

    public final static class Farthest {
        public int maxDist;
        public int playerOnly;
        public int isInLos;

        public Farthest clone() {
            Farthest varCopy = new farthest();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;
            varCopy.isInLos = this.isInLos;

            return varCopy;
        }
    }

    public final static class UnitRange {
        public int creature;
        public int minDist;
        public int maxDist;
        public int maxSize;

        public UnitRange clone() {
            UnitRange varCopy = new unitRange();

            varCopy.creature = this.creature;
            varCopy.minDist = this.minDist;
            varCopy.maxDist = this.maxDist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

    public final static class UnitGUID {
        public int dbGuid;
        public int entry;

        public UnitGUID clone() {
            UnitGUID varCopy = new unitGUID();

            varCopy.dbGuid = this.dbGuid;
            varCopy.entry = this.entry;

            return varCopy;
        }
    }

    public final static class UnitDistance {
        public int creature;
        public int dist;
        public int maxSize;

        public UnitDistance clone() {
            UnitDistance varCopy = new unitDistance();

            varCopy.creature = this.creature;
            varCopy.dist = this.dist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

    public final static class PlayerDistance {
        public int dist;

        public PlayerDistance clone() {
            PlayerDistance varCopy = new playerDistance();

            varCopy.dist = this.dist;

            return varCopy;
        }
    }

    public final static class PlayerRange {
        public int minDist;
        public int maxDist;

        public PlayerRange clone() {
            PlayerRange varCopy = new playerRange();

            varCopy.minDist = this.minDist;
            varCopy.maxDist = this.maxDist;

            return varCopy;
        }
    }

    public final static class Stored {
        public int id;

        public Stored clone() {
            Stored varCopy = new stored();

            varCopy.id = this.id;

            return varCopy;
        }
    }

    public final static class GoRange {
        public int entry;
        public int minDist;
        public int maxDist;
        public int maxSize;

        public GoRange clone() {
            GoRange varCopy = new goRange();

            varCopy.entry = this.entry;
            varCopy.minDist = this.minDist;
            varCopy.maxDist = this.maxDist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

    public final static class GoGUID {
        public int dbGuid;
        public int entry;

        public GoGUID clone() {
            GoGUID varCopy = new goGUID();

            varCopy.dbGuid = this.dbGuid;
            varCopy.entry = this.entry;

            return varCopy;
        }
    }

    public final static class GoDistance {
        public int entry;
        public int dist;
        public int maxSize;

        public GoDistance clone() {
            GoDistance varCopy = new goDistance();

            varCopy.entry = this.entry;
            varCopy.dist = this.dist;
            varCopy.maxSize = this.maxSize;

            return varCopy;
        }
    }

    public final static class UnitClosest {
        public int entry;
        public int dist;
        public int dead;

        public UnitClosest clone() {
            UnitClosest varCopy = new unitClosest();

            varCopy.entry = this.entry;
            varCopy.dist = this.dist;
            varCopy.dead = this.dead;

            return varCopy;
        }
    }

    public final static class GoClosest {
        public int entry;
        public int dist;

        public GoClosest clone() {
            GoClosest varCopy = new goClosest();

            varCopy.entry = this.entry;
            varCopy.dist = this.dist;

            return varCopy;
        }
    }

    public final static class ClosestAttackable {
        public int maxDist;
        public int playerOnly;

        public ClosestAttackable clone() {
            ClosestAttackable varCopy = new closestAttackable();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;

            return varCopy;
        }
    }

    public final static class ClosestFriendly {
        public int maxDist;
        public int playerOnly;

        public ClosestFriendly clone() {
            ClosestFriendly varCopy = new closestFriendly();

            varCopy.maxDist = this.maxDist;
            varCopy.playerOnly = this.playerOnly;

            return varCopy;
        }
    }

    public final static class Owner {
        public int useCharmerOrOwner;

        public Owner clone() {
            Owner varCopy = new owner();

            varCopy.useCharmerOrOwner = this.useCharmerOrOwner;

            return varCopy;
        }
    }

    public final static class Vehicle {
        public int seatMask;

        public Vehicle clone() {
            Vehicle varCopy = new vehicle();

            varCopy.seatMask = this.seatMask;

            return varCopy;
        }
    }

    public final static class ThreatList {
        public int maxDist;

        public ThreatList clone() {
            ThreatList varCopy = new threatList();

            varCopy.maxDist = this.maxDist;

            return varCopy;
        }
    }


    ///#endregion

    public final static class Raw {
        public int param1;
        public int param2;
        public int param3;
        public int param4;

        public Raw clone() {
            Raw varCopy = new raw();

            varCopy.param1 = this.param1;
            varCopy.param2 = this.param2;
            varCopy.param3 = this.param3;
            varCopy.param4 = this.param4;

            return varCopy;
        }
    }
}
