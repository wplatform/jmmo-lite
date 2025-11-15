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


@Table(name = "battle_pet_breed_quality")
@Db2DataBind(name = "BattlePetBreedQuality.db2", layoutHash = 0xF3E3FDFC, fields = {
        @Db2Field(name = "maxQualityRoll", type = Db2Type.BYTE),
        @Db2Field(name = "stateMultiplier", type = Db2Type.FLOAT),
        @Db2Field(name = "qualityEnum", type = Db2Type.BYTE)
})
public class BattlePetBreedQuality implements DbcEntity {
    @Id
    @Column("ID")
    private int id;

    @Column("MaxQualityRoll")
    private byte maxQualityRoll;

    @Column("StateMultiplier")
    private float stateMultiplier;

    @Column("QualityEnum")
    private byte qualityEnum;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;

}

