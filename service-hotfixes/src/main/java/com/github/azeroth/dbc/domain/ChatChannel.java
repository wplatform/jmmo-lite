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


@Table(name = "chat_channels")
@Db2DataBind(name = "ChatChannels.db2", layoutHash = 0x1A325E80, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "shortcut", type = Db2Type.STRING),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "factionGroup", type = Db2Type.BYTE, signed = true)
})
public class ChatChannel implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("Shortcut")
    private LocalizedString shortcut;

    @Column("Flags")
    private int flags;

    @Column("FactionGroup")
    private byte factionGroup;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
