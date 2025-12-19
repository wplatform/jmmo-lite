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


@Table(name = "spell_aura_restrictions")
@Db2DataBind(name = "SpellAuraRestrictions.db2", layoutHash = 0x7CDF3311, parentIndexField = 9, fields = {
        @Db2Field(name = "casterAuraSpell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "targetAuraSpell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "excludeCasterAuraSpell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "excludeTargetAuraSpell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "casterAuraState", type = Db2Type.BYTE),
        @Db2Field(name = "targetAuraState", type = Db2Type.BYTE),
        @Db2Field(name = "excludeCasterAuraState", type = Db2Type.BYTE),
        @Db2Field(name = "excludeTargetAuraState", type = Db2Type.BYTE),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class SpellAuraRestriction implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("CasterAuraSpell")
    private int casterAuraSpell;

    @Column("TargetAuraSpell")
    private int targetAuraSpell;

    @Column("ExcludeCasterAuraSpell")
    private int excludeCasterAuraSpell;

    @Column("ExcludeTargetAuraSpell")
    private int excludeTargetAuraSpell;

    @Column("DifficultyID")
    private byte difficultyID;

    @Column("CasterAuraState")
    private byte casterAuraState;

    @Column("TargetAuraState")
    private byte targetAuraState;

    @Column("ExcludeCasterAuraState")
    private byte excludeCasterAuraState;

    @Column("ExcludeTargetAuraState")
    private byte excludeTargetAuraState;

    @Column("SpellID")
    private int spellID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
