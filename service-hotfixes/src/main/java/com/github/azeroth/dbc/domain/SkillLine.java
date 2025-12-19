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


@Table(name = "skill_line")
@Db2DataBind(name = "SkillLine.db2", layoutHash = 0x3F7E88AF, fields = {
        @Db2Field(name = "displayName", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "alternateVerb", type = Db2Type.STRING),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "categoryID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "canLink", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "spellIconFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "parentSkillLineID", type = Db2Type.INT)
})
public class SkillLine implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("DisplayName")
    private LocalizedString displayName;

    @Column("Description")
    private LocalizedString description;

    @Column("AlternateVerb")
    private LocalizedString alternateVerb;

    @Column("Flags")
    private int flags;

    @Column("CategoryID")
    private byte categoryID;

    @Column("CanLink")
    private byte canLink;

    @Column("SpellIconFileID")
    private int spellIconFileID;

    @Column("ParentSkillLineID")
    private int parentSkillLineID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
