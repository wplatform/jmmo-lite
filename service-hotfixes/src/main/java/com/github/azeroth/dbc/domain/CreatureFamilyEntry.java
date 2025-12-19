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


@Table(name = "creature_family")
@Db2DataBind(name = "CreatureFamily.db2", layoutHash = 0xE2DC5126, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "minScale", type = Db2Type.FLOAT),
        @Db2Field(name = "maxScale", type = Db2Type.FLOAT),
        @Db2Field(name = "iconFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"skillLine1", "skillLine2"}, type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "petFoodMask", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "minScaleLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "maxScaleLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "petTalentType", type = Db2Type.BYTE, signed = true)
})
public class CreatureFamilyEntry implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("MinScale")
    private float minScale;

    @Column("MaxScale")
    private float maxScale;

    @Column("IconFileID")
    private int iconFileID;

    @Column("SkillLine1")
    private short skillLine1;

    @Column("SkillLine2")
    private short skillLine2;

    @Column("PetFoodMask")
    private short petFoodMask;

    @Column("MinScaleLevel")
    private byte minScaleLevel;

    @Column("MaxScaleLevel")
    private byte maxScaleLevel;

    @Column("PetTalentType")
    private byte petTalentType;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
