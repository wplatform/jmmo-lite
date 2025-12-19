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


@Table(name = "quest_faction_reward")
@Db2DataBind(name = "QuestFactionReward.db2", layoutHash = 0xB0E02541, fields = {
        @Db2Field(name = {"difficulty1", "difficulty2", "difficulty3", "difficulty4", "difficulty5", "difficulty6", "difficulty7", "difficulty8", "difficulty9", "difficulty10"}, type = Db2Type.SHORT, signed = true)
})
public class QuestFactionReward implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Difficulty1")
    private short difficulty1;

    @Column("Difficulty2")
    private short difficulty2;

    @Column("Difficulty3")
    private short difficulty3;

    @Column("Difficulty4")
    private short difficulty4;

    @Column("Difficulty5")
    private short difficulty5;

    @Column("Difficulty6")
    private short difficulty6;

    @Column("Difficulty7")
    private short difficulty7;

    @Column("Difficulty8")
    private short difficulty8;

    @Column("Difficulty9")
    private short difficulty9;

    @Column("Difficulty10")
    private short difficulty10;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
