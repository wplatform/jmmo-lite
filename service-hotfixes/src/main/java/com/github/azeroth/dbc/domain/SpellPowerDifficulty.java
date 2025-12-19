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


@Table(name = "spell_power_difficulty")
@Db2DataBind(name = "SpellPowerDifficulty.db2", layoutHash = 0x74714FF7, indexField = 2, fields = {
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class SpellPowerDifficulty implements DbcEntity {
    @Column("DifficultyID")
    private byte difficultyID;

    @Column("OrderIndex")
    private byte orderIndex;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
