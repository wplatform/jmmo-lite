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


@Table(name = "spell_duration")
@Db2DataBind(name = "SpellDuration.db2", layoutHash = 0x0D6C9082, fields = {
        @Db2Field(name = "duration", type = Db2Type.INT, signed = true),
        @Db2Field(name = "maxDuration", type = Db2Type.INT, signed = true),
        @Db2Field(name = "durationPerLevel", type = Db2Type.INT)
})
public class SpellDuration implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Duration")
    private int duration;

    @Column("MaxDuration")
    private int maxDuration;

    @Column("DurationPerLevel")
    private int durationPerLevel;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
