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


@Table(name = "mount_type_x_capability")
@Db2DataBind(name = "MountTypeXCapability.db2", layoutHash = 0xA34A8445, parentIndexField = 0, fields = {
        @Db2Field(name = "mountTypeID", type = Db2Type.SHORT),
        @Db2Field(name = "mountCapabilityID", type = Db2Type.SHORT),
        @Db2Field(name = "orderIndex", type = Db2Type.BYTE)
})
public class MountTypeXCapability implements DbcEntity, Comparable<MountTypeXCapability> {

    @Id

    @Column("ID")
    private int id;

    @Column("MountTypeID")
    private short mountTypeID;

    @Column("MountCapabilityID")
    private short mountCapabilityID;

    @Column("OrderIndex")
    private byte orderIndex;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Override
    public int compareTo(MountTypeXCapability o) {
        int result = Short.compare(mountTypeID, o.mountTypeID);
        if (result == 0) {
            result = Byte.compare(orderIndex, o.orderIndex);
        }
        return result;
    }


}
