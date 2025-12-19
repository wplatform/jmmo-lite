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


@Table(name = "artifact_appearance_set")
@Db2DataBind(name = "ArtifactAppearanceSet.db2", layoutHash = 0x53DFED74, indexField = 7, parentIndexField = 8, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "uiCameraID", type = Db2Type.SHORT),
        @Db2Field(name = "altHandUICameraID", type = Db2Type.SHORT),
        @Db2Field(name = "displayIndex", type = Db2Type.BYTE),
        @Db2Field(name = "forgeAttachmentOverride", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "artifactID", type = Db2Type.BYTE)
})
public class ArtifactAppearanceSet implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("Description")
    private LocalizedString description;

    @Column("UiCameraID")
    private short uiCameraID;

    @Column("AltHandUICameraID")
    private short altHandUICameraID;

    @Column("DisplayIndex")
    private byte displayIndex;

    @Column("ForgeAttachmentOverride")
    private byte forgeAttachmentOverride;

    @Column("Flags")
    private byte flags;

    @Id
    
    @Column("ID")
    private int id;

    @Column("ArtifactID")
    private short artifactID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
