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


@Table(name = "spell_interrupts")
@Db2DataBind(name = "SpellInterrupts.db2", layoutHash = 0x2FA8EA94, parentIndexField = 4, fields = {
        @Db2Field(name = "difficultyID", type = Db2Type.BYTE),
        @Db2Field(name = "interruptFlags", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = {"auraInterruptFlags1", "auraInterruptFlags2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"channelInterruptFlags1", "channelInterruptFlags2"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class SpellInterrupt implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("DifficultyID")
    private byte difficultyID;

    @Column("InterruptFlags")
    private short interruptFlags;

    @Column("AuraInterruptFlags1")
    private int auraInterruptFlags1;

    @Column("AuraInterruptFlags2")
    private int auraInterruptFlags2;

    @Column("ChannelInterruptFlags1")
    private int channelInterruptFlags1;

    @Column("ChannelInterruptFlags2")
    private int channelInterruptFlags2;

    @Column("SpellID")
    private int spellID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
