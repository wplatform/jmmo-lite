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


@Table(name = "reward_pack")
@Db2DataBind(name = "RewardPack.db2", layoutHash = 0xDB6CC0AB, fields = {
        @Db2Field(name = "money", type = Db2Type.INT),
        @Db2Field(name = "artifactXPMultiplier", type = Db2Type.FLOAT),
        @Db2Field(name = "artifactXPDifficulty", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "artifactXPCategoryID", type = Db2Type.BYTE),
        @Db2Field(name = "charTitleID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "treasurePickerID", type = Db2Type.INT)
})
public class RewardPack implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Money")
    private int money;

    @Column("ArtifactXPMultiplier")
    private float artifactXPMultiplier;

    @Column("ArtifactXPDifficulty")
    private byte artifactXPDifficulty;

    @Column("ArtifactXPCategoryID")
    private byte artifactXPCategoryID;

    @Column("CharTitleID")
    private int charTitleID;

    @Column("TreasurePickerID")
    private int treasurePickerID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
