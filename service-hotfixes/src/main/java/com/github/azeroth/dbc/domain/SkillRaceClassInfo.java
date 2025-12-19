package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.defines.PlayerClassMask;
import com.github.azeroth.defines.RaceMask;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "skill_race_class_info")
@Db2DataBind(name = "SkillRaceClassInfo.db2", layoutHash = 0x9752C2CE, parentIndexField = 1, fields = {
        @Db2Field(name = "raceMask", type = Db2Type.LONG, signed = true),
        @Db2Field(name = "skillID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "skillTierID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "availability", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "minLevel", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "classMask", type = Db2Type.INT, signed = true)
})
public class SkillRaceClassInfo implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("RaceMask")
    private Long raceMask;

    @Column("SkillID")
    private short skillID;

    @Column("Flags")
    private int flags;

    @Column("SkillTierID")
    private short skillTierID;

    @Column("Availability")
    private byte availability;

    @Column("MinLevel")
    private byte minLevel;

    @Column("ClassMask")
    private int classMask;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    public RaceMask raceMask() {
        return RaceMask.of(raceMask);
    }

    public PlayerClassMask classMask() {
        return PlayerClassMask.of(classMask);
    }

}
