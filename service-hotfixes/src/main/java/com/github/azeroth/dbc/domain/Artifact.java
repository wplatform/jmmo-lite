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


@Table(name = "artifact")
@Db2DataBind(name = "Artifact.db2", layoutHash = 0x76CF31A8, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "uiBarOverlayColor", type = Db2Type.INT, signed = true),
        @Db2Field(name = "uiBarBackgroundColor", type = Db2Type.INT, signed = true),
        @Db2Field(name = "uiNameColor", type = Db2Type.INT, signed = true),
        @Db2Field(name = "uiTextureKitID", type = Db2Type.SHORT),
        @Db2Field(name = "chrSpecializationID", type = Db2Type.SHORT),
        @Db2Field(name = "artifactCategoryID", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "uiModelSceneID", type = Db2Type.INT),
        @Db2Field(name = "spellVisualKitID", type = Db2Type.INT)
})
public class Artifact implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("UiBarOverlayColor")
    private int uiBarOverlayColor;

    @Column("UiBarBackgroundColor")
    private int uiBarBackgroundColor;

    @Column("UiNameColor")
    private int uiNameColor;

    @Column("UiTextureKitID")
    private short uiTextureKitID;

    @Column("ChrSpecializationID")
    private short chrSpecializationID;

    @Column("ArtifactCategoryID")
    private byte artifactCategoryID;

    @Column("Flags")
    private byte flags;

    @Column("UiModelSceneID")
    private int uiModelSceneID;

    @Column("SpellVisualKitID")
    private int spellVisualKitID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
