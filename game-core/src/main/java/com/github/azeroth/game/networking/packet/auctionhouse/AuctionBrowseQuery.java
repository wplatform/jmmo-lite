package com.github.azeroth.game.networking.packet.auctionhouse;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.auctionhouse.AuctionHouseFilterMask;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.packet.addon.AddOnInfo;
import io.netty.buffer.ByteBuf;

public final class AuctionBrowseQuery extends ClientPacket {
    public ObjectGuid auctioneer;
    public int offset;
    public byte minLevel;
    public byte maxLevel;
    public EnumFlag<AuctionHouseFilterMask> filters;
    public byte[] knownPets;
    public byte maxPetLevel;
    public AddOnInfo taintedBy = null;
    public String name;
    public AuctionListFilterClass[] itemClassFilters = new AuctionListFilterClass[7];
    public AuctionSortDef[] sorts = new AuctionSortDef[2];

    public AuctionBrowseQuery(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        auctioneer = this.readPackedGuid();
        offset = this.readUInt32();
        minLevel = this.readByte();
        maxLevel = this.readByte();
        filters = EnumFlag.of(AuctionHouseFilterMask.class, this.readUInt32());
        var knownPetSize = this.readUInt32();
        maxPetLevel = this.readByte();

        knownPets = new byte[knownPetSize];

        for (var i = 0; i < knownPetSize; ++i) {
            knownPets[i] = this.readByte();
        }

        if (this.readBit()) {
            taintedBy = new AddOnInfo();
        }

        var nameLength = this.<Integer>readBit(8);
        var itemClassFilterCount = this.<Integer>readBit(3);
        var sortSize = this.<Integer>readBit(2);

        for (var i = 0; i < sortSize; ++i) {
            sorts[i] = new AuctionSortDef(this);
        }

        if (taintedBy != null) {
            taintedBy.read(this);
        }

        name = this.readString(nameLength);
        // AuctionListFilterClass filterClass in itemClassFilters)
        for (var i = 0; i < itemClassFilterCount; ++i) {
            itemClassFilters[i] = new AuctionListFilterClass(this);
        }
    }
}

//Structs

