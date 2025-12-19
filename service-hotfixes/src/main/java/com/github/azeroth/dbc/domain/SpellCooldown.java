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


@Table(name = "spell_cooldowns")
@Db2DataBind(name = "SpellCooldowns.db2", layoutHash = 0xCA8D8B3C, parentIndexField = 4, fields = {
        @Db2Field(name = "categoryRecoveryTime", type = Db2Type.INT, signed = true),
        @Db2Field(name = "recoveryTime", type = Db2Type.INT, signed = true),
        @Db2Field(name = "startRecoveryTime", type = Db2Type.INT, signed = true),
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class SpellCooldown implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("CategoryRecoveryTime")
    private int categoryRecoveryTime;

    @Column("RecoveryTime")
    private int recoveryTime;

    @Column("StartRecoveryTime")
    private int startRecoveryTime;

    @Column("DifficultyID")
    private byte difficultyID;

    @Column("SpellID")
    private int spellID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
