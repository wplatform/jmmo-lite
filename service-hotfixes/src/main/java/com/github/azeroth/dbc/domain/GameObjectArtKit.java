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


@Table(name = "gameobject_art_kit")
@Db2DataBind(name = "GameObjectArtKit.db2", layoutHash = 0x6F65BC41, fields = {
        @Db2Field(name = "attachModelFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"textureVariationFileID1", "textureVariationFileID2", "textureVariationFileID3"}, type = Db2Type.INT, signed = true)
})
public class GameObjectArtKit implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("AttachModelFileID")
    private int attachModelFileID;

    @Column("TextureVariationFileID1")
    private int textureVariationFileID1;

    @Column("TextureVariationFileID2")
    private int textureVariationFileID2;

    @Column("TextureVariationFileID3")
    private int textureVariationFileID3;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
