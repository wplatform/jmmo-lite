package com.github.azeroth.dbc.domain;

import com.github.azeroth.common.LocalizedString;
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


@Table(name = "scenario_step")
@Db2DataBind(name = "ScenarioStep.db2", layoutHash = 0x201B0EFC, parentIndexField = 2, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "title", type = Db2Type.STRING),
        @Db2Field(name = "scenarioID", type = Db2Type.SHORT),
        @Db2Field(name = "supersedes", type = Db2Type.SHORT),
        @Db2Field(name = "rewardQuestID", type = Db2Type.SHORT),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "criteriatreeid", type = Db2Type.INT),
        @Db2Field(name = "relatedStep", type = Db2Type.INT, signed = true)
})
public class ScenarioStep implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Description")
    private LocalizedString description;

    @Column("Title")
    private LocalizedString title;

    @Column("ScenarioID")
    private short scenarioID;

    @Column("Supersedes")
    private short supersedes;

    @Column("RewardQuestID")
    private int rewardQuestID;

    @Column("OrderIndex")
    private byte orderIndex;

    @Column("Flags")
    private byte flags;

    @Column("Criteriatreeid")
    private int criteriatreeid;

    @Column("RelatedStep")
    private int relatedStep;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
