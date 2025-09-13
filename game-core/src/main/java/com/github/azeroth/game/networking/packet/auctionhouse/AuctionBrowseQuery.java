package com.github.azeroth.game.networking.packet.auctionhouse;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AuctionBrowseQuery extends ClientPacket {
    public ObjectGuid auctioneer = ObjectGuid.EMPTY;
    public int offset;
    public byte minLevel = 1;
    public byte maxLevel = SharedConst.maxLevel;
    public AuctionHouseFilterMask filters = AuctionHouseFilterMask.values()[0];
    public byte[] knownPets;
    public byte maxPetLevel;
    public AddOnInfo taintedBy = null;
    public String name;
    public Array<AuctionListFilterClass> itemClassFilters = new Array<AuctionListFilterClass>(7);
    public Array<AuctionSortDef> sorts = new Array<AuctionSortDef>(2);

    public AuctionBrowseQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        offset = this.readUInt32();
        minLevel = this.readUInt8();
        maxLevel = this.readUInt8();
        filters = AuctionHouseFilterMask.forValue(this.readUInt32());
        var knownPetSize = this.readUInt32();
        maxPetLevel = this.readByte();

        var sizeLimit = CliDB.BattlePetSpeciesStorage.GetNumRows() / 8 + 1;

        if (knownPetSize >= sizeLimit) {
            throw new RuntimeException(String.format("Attempted to read more array elements from packet %1$s than allowed %2$s", knownPetSize, sizeLimit));
        }

        knownPets = new byte[knownPetSize];

        for (var i = 0; i < knownPetSize; ++i) {
            KnownPets[i] = this.readUInt8();
        }

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var nameLength = this.<Integer>readBit(8);
        var itemClassFilterCount = this.<Integer>readBit(3);
        var sortSize = this.<Integer>readBit(2);

        for (var i = 0; i < sortSize; ++i) {
            sorts.set(i, new AuctionSortDef(this));
        }

        if (taintedBy != null) {
            taintedBy.getValue().read(this);
        }

        name = this.readString(nameLength);

        for (var i = 0; i < itemClassFilterCount; ++i) // AuctionListFilterClass filterClass in itemClassFilters)
        {
            itemClassFilters.set(i, new AuctionListFilterClass(this));
        }
    }
}

//Structs

