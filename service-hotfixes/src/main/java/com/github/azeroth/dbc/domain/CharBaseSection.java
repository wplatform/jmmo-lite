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


@Table(name = "char_base_section")
@Db2DataBind(name = "CharBaseSection.db2", layoutHash = 0x4F08B5F3, fields = {
        @Db2Field(name = "variationEnum", type = Db2Type.BYTE),
        @Db2Field(name = "resolutionVariationEnum", type = Db2Type.BYTE),
        @Db2Field(name = "layoutResType", type = Db2Type.BYTE)
})
public class CharBaseSection implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("VariationEnum")
    private byte variationEnum;

    @Column("ResolutionVariationEnum")
    private byte resolutionVariationEnum;

    @Column("LayoutResType")
    private byte layoutResType;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
