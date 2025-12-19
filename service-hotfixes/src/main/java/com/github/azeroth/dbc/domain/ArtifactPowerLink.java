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


@Table(name = "artifact_power_link")
@Db2DataBind(name = "ArtifactPowerLink.db2", layoutHash = 0xE179618C, fields = {
        @Db2Field(name = "powerA", type = Db2Type.SHORT),
        @Db2Field(name = "powerB", type = Db2Type.SHORT)
})
public class ArtifactPowerLink implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("PowerA")
    private short powerA;

    @Column("PowerB")
    private short powerB;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
