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
@ToString


@Table(name = "totem_category")
@Db2DataBind(name = "TotemCategory.db2", layoutHash = 0x20B9177A, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "totemCategoryMask", type = Db2Type.INT, signed = true),
        @Db2Field(name = "totemCategoryType", type = Db2Type.BYTE)
})
public class TotemCategory implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("TotemCategoryMask")
    private int totemCategoryMask;

    @Column("TotemCategoryType")
    private byte totemCategoryType;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
