package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.dbc.defines.ChrSpecializationFlag;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "chr_specialization")
@Db2DataBind(name = "ChrSpecialization.db2", layoutHash = 0x3D86B8F7, indexField = 9, parentIndexField = 4, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "femaleName", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = {"masterySpellID1", "masterySpellID2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "classID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "petTalentType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "role", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "primaryStatPriority", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "spellIconFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.INT),
        @Db2Field(name = "animReplacements", type = Db2Type.INT, signed = true)
})
public class ChrSpecialization implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("FemaleName")
    private LocalizedString femaleName;

    @Column("Description")
    private LocalizedString description;

    @Column("MasterySpellID1")
    private int masterySpellID1;

    @Column("MasterySpellID2")
    private int masterySpellID2;

    @Column("ClassID")
    private byte classID;

    @Column("OrderIndex")
    private byte orderIndex;

    @Column("PetTalentType")
    private byte petTalentType;

    @Column("Role")
    private byte role;

    @Column("PrimaryStatPriority")
    private byte primaryStatPriority;

    @Id

    @Column("ID")
    private int id;

    @Column("SpellIconFileID")
    private int spellIconFileID;

    @Column("Flags")
    private int flags;

    @Column("AnimReplacements")
    private int animReplacements;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    public EnumFlag<ChrSpecializationFlag> flags() {
        return EnumFlag.of(ChrSpecializationFlag.class, this.flags);
    }
}
