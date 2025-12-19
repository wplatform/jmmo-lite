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


@Table(name = "spell_reagents")
@Db2DataBind(name = "SpellReagents.db2", layoutHash = 0x0463C688, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"reagent1", "reagent2", "reagent3", "reagent4", "reagent5", "reagent6", "reagent7", "reagent8"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"reagentCount1", "reagentCount2", "reagentCount3", "reagentCount4", "reagentCount5", "reagentCount6", "reagentCount7", "reagentCount8"}, type = Db2Type.SHORT, signed = true)
})
public class SpellReagent implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("Reagent1")
    private int reagent1;

    @Column("Reagent2")
    private int reagent2;

    @Column("Reagent3")
    private int reagent3;

    @Column("Reagent4")
    private int reagent4;

    @Column("Reagent5")
    private int reagent5;

    @Column("Reagent6")
    private int reagent6;

    @Column("Reagent7")
    private int reagent7;

    @Column("Reagent8")
    private int reagent8;

    @Column("ReagentCount1")
    private short reagentCount1;

    @Column("ReagentCount2")
    private short reagentCount2;

    @Column("ReagentCount3")
    private short reagentCount3;

    @Column("ReagentCount4")
    private short reagentCount4;

    @Column("ReagentCount5")
    private short reagentCount5;

    @Column("ReagentCount6")
    private short reagentCount6;

    @Column("ReagentCount7")
    private short reagentCount7;

    @Column("ReagentCount8")
    private short reagentCount8;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
