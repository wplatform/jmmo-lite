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


@Table(name = "guild_color_emblem")
@Db2DataBind(name = "GuildColorEmblem.db2", layoutHash = 0xCC0CEFF1, fields = {
        @Db2Field(name = "red", type = Db2Type.BYTE),
        @Db2Field(name = "green", type = Db2Type.BYTE),
        @Db2Field(name = "blue", type = Db2Type.BYTE)
})
public class GuildColorEmblem implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Red")
    private short red;

    @Column("Green")
    private short green;

    @Column("Blue")
    private short blue;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
