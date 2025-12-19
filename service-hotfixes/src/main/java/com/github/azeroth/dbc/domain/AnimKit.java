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


@Table(name = "anim_kit")
@Db2DataBind(name = "AnimKit.db2", layoutHash = 0x81D6D250, fields = {
        @Db2Field(name = "oneShotDuration", type = Db2Type.INT),
        @Db2Field(name = "oneShotStopAnimKitID", type = Db2Type.SHORT),
        @Db2Field(name = "lowDefAnimKitID", type = Db2Type.SHORT)
})
public class AnimKit implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("OneShotDuration")
    private int oneShotDuration;

    @Column("OneShotStopAnimKitID")
    private short oneShotStopAnimKitID;

    @Column("LowDefAnimKitID")
    private short lowDefAnimKitID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
