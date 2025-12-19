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


@Table(name = "pvp_talent_unlock")
@Db2DataBind(name = "PvpTalentUnlock.db2", layoutHash = 0x465C83BC, fields = {
        @Db2Field(name = "tierID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "columnIndex", type = Db2Type.INT, signed = true),
        @Db2Field(name = "honorLevel", type = Db2Type.INT, signed = true)
})
public class PvpTalentUnlock implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("TierID")
    private int tierID;

    @Column("ColumnIndex")
    private int columnIndex;

    @Column("HonorLevel")
    private int honorLevel;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
