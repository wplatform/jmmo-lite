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


@Table(name = "spell_visual_missile")
@Db2DataBind(name = "SpellVisualMissile.db2", layoutHash = 0x00BA67A5, indexField = 12, parentIndexField = 15, fields = {
        @Db2Field(name = "followGroundHeight", type = Db2Type.INT, signed = true),
        @Db2Field(name = "followGroundDropSpeed", type = Db2Type.INT),
        @Db2Field(name = "flags", type = Db2Type.INT),
        @Db2Field(name = {"castOffset1", "castOffset2", "castOffset3"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"impactOffset1", "impactOffset2", "impactOffset3"}, type = Db2Type.FLOAT),
        @Db2Field(name = "spellVisualEffectNameID", type = Db2Type.SHORT),
        @Db2Field(name = "castPositionerID", type = Db2Type.SHORT),
        @Db2Field(name = "impactPositionerID", type = Db2Type.SHORT),
        @Db2Field(name = "followGroundApproach", type = Db2Type.SHORT),
        @Db2Field(name = "spellMissileMotionID", type = Db2Type.SHORT),
        @Db2Field(name = "attachment", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "destinationAttachment", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "soundEntriesID", type = Db2Type.INT),
        @Db2Field(name = "animKitID", type = Db2Type.INT),
        @Db2Field(name = "spellVisualMissileSetID", type = Db2Type.SHORT)
})
public class SpellVisualMissile implements DbcEntity {
    @Column("FollowGroundHeight")
    private int followGroundHeight;

    @Column("FollowGroundDropSpeed")
    private int followGroundDropSpeed;

    @Column("Flags")
    private int flags;

    @Column("CastOffset1")
    private float castOffset1;

    @Column("CastOffset2")
    private float castOffset2;

    @Column("CastOffset3")
    private float castOffset3;

    @Column("ImpactOffset1")
    private float impactOffset1;

    @Column("ImpactOffset2")
    private float impactOffset2;

    @Column("ImpactOffset3")
    private float impactOffset3;

    @Column("SpellVisualEffectNameID")
    private short spellVisualEffectNameID;

    @Column("CastPositionerID")
    private short castPositionerID;

    @Column("ImpactPositionerID")
    private short impactPositionerID;

    @Column("FollowGroundApproach")
    private int followGroundApproach;

    @Column("SpellMissileMotionID")
    private short spellMissileMotionID;

    @Column("Attachment")
    private byte attachment;

    @Column("DestinationAttachment")
    private byte destinationAttachment;

    @Id
    
    @Column("ID")
    private int id;

    @Column("SoundEntriesID")
    private int soundEntriesID;

    @Column("AnimKitID")
    private int animKitID;

    @Column("SpellVisualMissileSetID")
    private short spellVisualMissileSetID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
