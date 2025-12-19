package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "auction_house")
@Db2DataBind(name = "AuctionHouse.db2", layoutHash = 0x51CFEEFF, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "factionID", type = Db2Type.SHORT),
        @Db2Field(name = "depositRate", type = Db2Type.BYTE),
        @Db2Field(name = "consignmentRate", type = Db2Type.BYTE)
})
public class AuctionHouse implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("FactionID")
    private short factionID;

    @Column("DepositRate")
    private byte depositRate;

    @Column("ConsignmentRate")
    private byte consignmentRate;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
