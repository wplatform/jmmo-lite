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


@Table(name = "spell_procs_per_minute_mod")
@Db2DataBind(name = "SpellProcsPerMinuteMod.db2", layoutHash = 0x2503C18B, parentIndexField = 3, fields = {
        @Db2Field(name = "coeff", type = Db2Type.FLOAT),
        @Db2Field(name = "param", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "type", type = Db2Type.BYTE),
        @Db2Field(name = "spellProcsPerMinuteID", type = Db2Type.SHORT)
})
public class SpellProcsPerMinuteMod implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Coeff")
    private float coeff;

    @Column("Param")
    private short param;

    @Column("Type")
    private byte type;

    @Column("SpellProcsPerMinuteID")
    private short spellProcsPerMinuteID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
