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


@Table(name = "char_titles")
@Db2DataBind(name = "CharTitles.db2", layoutHash = 0x7A58AA5F, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "name1", type = Db2Type.STRING),
        @Db2Field(name = "maskID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE, signed = true)
})
public class CharTitle implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("Name1")
    private LocalizedString name1;

    @Column("MaskID")
    private short maskID;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
