package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
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


@Table(name = "artifact_power_rank")
@Db2DataBind(name = "ArtifactPowerRank.db2", layoutHash = 0xA87EACC4, parentIndexField = 4, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "auraPointsOverride", type = Db2Type.FLOAT),
        @Db2Field(name = "itemBonusListID", type = Db2Type.SHORT),
        @Db2Field(name = "rankIndex", type = Db2Type.BYTE),
        @Db2Field(name = "artifactPowerID", type = Db2Type.SHORT)
})
public class ArtifactPowerRank implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("AuraPointsOverride")
    private float auraPointsOverride;

    @Column("ItemBonusListID")
    private short itemBonusListID;

    @Column("RankIndex")
    private byte rankIndex;

    @Column("ArtifactPowerID")
    private short artifactPowerID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
