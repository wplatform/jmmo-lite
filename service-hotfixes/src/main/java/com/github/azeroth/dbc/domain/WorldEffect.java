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


@Table(name = "world_effect")
@Db2DataBind(name = "WorldEffect.db2", layoutHash = 0x2E9B9BFD, fields = {
        @Db2Field(name = "targetAsset", type = Db2Type.INT, signed = true),
        @Db2Field(name = "combatConditionID", type = Db2Type.SHORT),
        @Db2Field(name = "targetType", type = Db2Type.BYTE),
        @Db2Field(name = "whenToDisplay", type = Db2Type.BYTE),
        @Db2Field(name = "questFeedbackEffectID", type = Db2Type.INT),
        @Db2Field(name = "playerConditionID", type = Db2Type.INT)
})
public class WorldEffect implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("TargetAsset")
    private int targetAsset;

    @Column("CombatConditionID")
    private short combatConditionID;

    @Column("TargetType")
    private byte targetType;

    @Column("WhenToDisplay")
    private byte whenToDisplay;

    @Column("QuestFeedbackEffectID")
    private int questFeedbackEffectID;

    @Column("PlayerConditionID")
    private int playerConditionID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
