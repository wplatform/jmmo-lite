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


@Table(name = "pvp_reward")
@Db2DataBind(name = "PvpReward.db2", layoutHash = 0x72F4C016, fields = {
        @Db2Field(name = "honorLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "prestigeLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "rewardPackID", type = Db2Type.INT, signed = true)
})
public class PvpReward implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("HonorLevel")
    private int honorLevel;

    @Column("PrestigeLevel")
    private int prestigeLevel;

    @Column("RewardPackID")
    private int rewardPackID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
