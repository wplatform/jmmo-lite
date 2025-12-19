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


@Table(name = "friendship_reputation")
@Db2DataBind(name = "FriendshipReputation.db2", layoutHash = 0x406EE0AB, indexField = 3, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "textureFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "factionID", type = Db2Type.SHORT),
        @Db2Field(name = "id", type = Db2Type.INT, signed = true)
})
public class FriendshipReputation implements DbcEntity {
    @Column("Description")
    private LocalizedString description;

    @Column("TextureFileID")
    private int textureFileID;

    @Column("FactionID")
    private short factionID;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
