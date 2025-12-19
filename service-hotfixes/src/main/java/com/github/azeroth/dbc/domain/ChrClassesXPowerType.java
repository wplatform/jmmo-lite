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


@Table(name = "chr_classes_x_power_types")
@Db2DataBind(name = "ChrClassesXPowerTypes.db2", layoutHash = 0xAF977B23, parentIndexField = 1, fields = {
        @Db2Field(name = "powerType", type = Db2Type.BYTE),
        @Db2Field(name = "classID", type = Db2Type.BYTE)
})
public class ChrClassesXPowerType implements DbcEntity, Comparable<ChrClassesXPowerType> {
    @Id

    @Column("ID")
    private int id;

    @Column("PowerType")
    private byte powerType;

    @Column("ClassID")
    private byte classID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    @Override
    public int compareTo(ChrClassesXPowerType o) {
        int result = Byte.compare(this.classID, o.classID);
        if (result == 0) {
            result = Byte.compare(this.powerType, o.powerType);
        }
        return result;
    }
}
