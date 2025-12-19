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


@Table(name = "skill_line_ability")
@Db2DataBind(name = "SkillLineAbility.db2", layoutHash = 0x97B5A653, indexField = 1, parentIndexField = 4, fields = {
        @Db2Field(name = "raceMask", type = Db2Type.LONG, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "spell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "supercedesSpell", type = Db2Type.INT, signed = true),
        @Db2Field(name = "skillLine", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "trivialSkillLineRankHigh", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "trivialSkillLineRankLow", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "uniqueBit", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "tradeSkillCategoryID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "numSkillUps", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "classMask", type = Db2Type.INT, signed = true),
        @Db2Field(name = "minSkillLineRank", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "acquireMethod", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE, signed = true)
})
public class SkillLineAbility implements DbcEntity {
    @Column("RaceMask")
    private Long raceMask;

    @Id

    @Column("ID")
    private int id;

    @Column("Spell")
    private int spell;

    @Column("SupercedesSpell")
    private int supercedesSpell;

    @Column("SkillLine")
    private short skillLine;

    @Column("TrivialSkillLineRankHigh")
    private short trivialSkillLineRankHigh;

    @Column("TrivialSkillLineRankLow")
    private short trivialSkillLineRankLow;

    @Column("UniqueBit")
    private short uniqueBit;

    @Column("TradeSkillCategoryID")
    private short tradeSkillCategoryID;

    @Column("NumSkillUps")
    private byte numSkillUps;

    @Column("ClassMask")
    private int classMask;

    @Column("MinSkillLineRank")
    private short minSkillLineRank;

    @Column("AcquireMethod")
    private byte acquireMethod;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
