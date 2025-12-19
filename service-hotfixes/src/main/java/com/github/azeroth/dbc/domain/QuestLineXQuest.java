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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "quest_line_x_quest")
@Db2DataBind(name = "QuestLineXQuest.db2", layoutHash = 0x83C5B32B, parentIndexField = 0, fields = {
        @Db2Field(name = "questLineID", type = Db2Type.SHORT),
        @Db2Field(name = "questID", type = Db2Type.SHORT),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE)
})
public class QuestLineXQuest implements DbcEntity, Comparable<QuestLineXQuest> {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("QuestLineID")
    private int questLineID;


    @Column("QuestID")
    private int questID;


    @Column("OrderIndex")
    private int orderIndex;

    @Override
    public int compareTo(QuestLineXQuest o) {
        return Integer.compare(this.orderIndex, o.orderIndex);
    }
}