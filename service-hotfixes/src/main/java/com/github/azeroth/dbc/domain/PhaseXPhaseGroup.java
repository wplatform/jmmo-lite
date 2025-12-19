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


@Table(name = "phase_x_phase_group")
@Db2DataBind(name = "PhaseXPhaseGroup.db2", layoutHash = 0x66517AF6, parentIndexField = 1, fields = {
        @Db2Field(name = "phaseID", type = Db2Type.SHORT),
        @Db2Field(name = "phaseGroupID", type = Db2Type.SHORT)
})
public class PhaseXPhaseGroup implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("PhaseID")
    private short phaseID;

    @Column("PhaseGroupID")
    private short phaseGroupID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
