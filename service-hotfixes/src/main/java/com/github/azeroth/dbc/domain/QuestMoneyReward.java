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


@Table(name = "quest_money_reward")
@Db2DataBind(name = "QuestMoneyReward.db2", layoutHash = 0x86397302, fields = {
        @Db2Field(name = {"difficulty1", "difficulty2", "difficulty3", "difficulty4", "difficulty5", "difficulty6", "difficulty7", "difficulty8", "difficulty9", "difficulty10"}, type = Db2Type.INT)
})
public class QuestMoneyReward implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Difficulty1")
    private int difficulty1;

    @Column("Difficulty2")
    private int difficulty2;

    @Column("Difficulty3")
    private int difficulty3;

    @Column("Difficulty4")
    private int difficulty4;

    @Column("Difficulty5")
    private int difficulty5;

    @Column("Difficulty6")
    private int difficulty6;

    @Column("Difficulty7")
    private int difficulty7;

    @Column("Difficulty8")
    private int difficulty8;

    @Column("Difficulty9")
    private int difficulty9;

    @Column("Difficulty10")
    private int difficulty10;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
