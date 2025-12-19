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
@Table(name = "faction")
@Db2DataBind(name = "Faction.db2", layoutHash = 0x6BFE8737, indexField = 3, fields = {
        @Db2Field(name = {"reputationRaceMask1", "reputationRaceMask2", "reputationRaceMask3", "reputationRaceMask4"}, type = Db2Type.LONG, signed = true),
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = {"reputationBase1", "reputationBase2", "reputationBase3", "reputationBase4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"parentFactionMod1", "parentFactionMod2"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"reputationMax1", "reputationMax2", "reputationMax3", "reputationMax4"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "reputationIndex", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = {"reputationClassMask1", "reputationClassMask2", "reputationClassMask3", "reputationClassMask4"}, type = Db2Type.SHORT, signed = true),
        @Db2Field(name = {"reputationFlags1", "reputationFlags2", "reputationFlags3", "reputationFlags4"}, type = Db2Type.SHORT),
        @Db2Field(name = "parentFactionID", type = Db2Type.SHORT),
        @Db2Field(name = "paragonFactionID", type = Db2Type.SHORT),
        @Db2Field(name = {"parentFactionCap1", "parentFactionCap2"}, type = Db2Type.BYTE),
        @Db2Field(name = "expansion", type = Db2Type.BYTE),
        @Db2Field(name = "friendshipRepID", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE)
})
public class Faction implements DbcEntity {
    @Column("ReputationRaceMask1")
    private Long reputationRaceMask1;

    @Column("ReputationRaceMask2")
    private Long reputationRaceMask2;

    @Column("ReputationRaceMask3")
    private Long reputationRaceMask3;

    @Column("ReputationRaceMask4")
    private Long reputationRaceMask4;

    @Column("Name")
    private LocalizedString name;

    @Column("Description")
    private LocalizedString description;

    @Id

    @Column("ID")
    private int id;

    @Column("ReputationBase1")
    private int reputationBase1;

    @Column("ReputationBase2")
    private int reputationBase2;

    @Column("ReputationBase3")
    private int reputationBase3;

    @Column("ReputationBase4")
    private int reputationBase4;

    @Column("ParentFactionMod1")
    private float parentFactionMod1;

    @Column("ParentFactionMod2")
    private float parentFactionMod2;

    @Column("ReputationMax1")
    private int reputationMax1;

    @Column("ReputationMax2")
    private int reputationMax2;

    @Column("ReputationMax3")
    private int reputationMax3;

    @Column("ReputationMax4")
    private int reputationMax4;

    @Column("ReputationIndex")
    private short reputationIndex;

    @Column("ReputationClassMask1")
    private short reputationClassMask1;

    @Column("ReputationClassMask2")
    private short reputationClassMask2;

    @Column("ReputationClassMask3")
    private short reputationClassMask3;

    @Column("ReputationClassMask4")
    private short reputationClassMask4;

    @Column("ReputationFlags1")
    private short reputationFlags1;

    @Column("ReputationFlags2")
    private short reputationFlags2;

    @Column("ReputationFlags3")
    private short reputationFlags3;

    @Column("ReputationFlags4")
    private short reputationFlags4;

    @Column("ParentFactionID")
    private short parentFactionID;

    @Column("ParagonFactionID")
    private short paragonFactionID;

    @Column("ParentFactionCap1")
    private byte parentFactionCap1;

    @Column("ParentFactionCap2")
    private byte parentFactionCap2;

    @Column("Expansion")
    private byte expansion;

    @Column("FriendshipRepID")
    private byte friendshipRepID;

    @Column("Flags")
    private short flags;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;


    public boolean canHaveReputation() {
        return reputationIndex >= 0;
    }

}
