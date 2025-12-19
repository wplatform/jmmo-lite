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


@Table(name = "banned_addons")
@Db2DataBind(name = "BannedAddons.db2", layoutHash = 0xF779B6E5, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "version", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "flags", type = Db2Type.BYTE)
})
public class BannedAddon implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private String name;

    @Column("Version")
    private String version;

    @Column("Flags")
    private byte flags;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
