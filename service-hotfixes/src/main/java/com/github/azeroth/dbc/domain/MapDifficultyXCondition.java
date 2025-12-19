package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
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


@Table(name = "map_difficulty_x_condition")
@Db2DataBind(name = "MapDifficultyXCondition.db2", layoutHash = 0x5F5D7102, parentIndexField = 3, fields = {
        @Db2Field(name = "failureDescription", type = Db2Type.STRING),
        @Db2Field(name = "playerConditionID", type = Db2Type.INT),
        @Db2Field(name = "orderIndex", type = Db2Type.INT, signed = true),
        @Db2Field(name = "mapDifficultyId", type = Db2Type.INT),
})
public class MapDifficultyXCondition implements DbcEntity, Comparable<MapDifficultyXCondition> {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("FailureDescription")
    private LocalizedString failureDescription;


    @Column("PlayerConditionID")
    private int playerConditionID;


    @Column("OrderIndex")
    private int orderIndex;


    @Column("MapDifficultyID")
    private int mapDifficultyId;

    @Override
    public int compareTo(MapDifficultyXCondition o) {
        return Integer.compare(orderIndex, o.orderIndex);
    }
}