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


@Table(name = "spell_visual")
@Db2DataBind(name = "SpellVisual.db2", layoutHash = 0x1C1301D2, fields = {
        @Db2Field(name = {"missileCastOffset1", "missileCastOffset2", "missileCastOffset3"}, type = Db2Type.FLOAT),
        @Db2Field(name = {"missileImpactOffset1", "missileImpactOffset2", "missileImpactOffset3"}, type = Db2Type.FLOAT),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "spellVisualMissileSetID", type = Db2Type.SHORT),
        @Db2Field(name = "missileDestinationAttachment", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "missileAttachment", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "missileCastPositionerID", type = Db2Type.INT),
        @Db2Field(name = "missileImpactPositionerID", type = Db2Type.INT),
        @Db2Field(name = "missileTargetingKit", type = Db2Type.INT, signed = true),
        @Db2Field(name = "animEventSoundID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "damageNumberDelay", type = Db2Type.SHORT),
        @Db2Field(name = "hostileSpellVisualID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "casterSpellVisualID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "lowViolenceSpellVisualID", type = Db2Type.INT, signed = true)
})
public class SpellVisual implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("MissileCastOffset1")
    private float missileCastOffset1;

    @Column("MissileCastOffset2")
    private float missileCastOffset2;

    @Column("MissileCastOffset3")
    private float missileCastOffset3;

    @Column("MissileImpactOffset1")
    private float missileImpactOffset1;

    @Column("MissileImpactOffset2")
    private float missileImpactOffset2;

    @Column("MissileImpactOffset3")
    private float missileImpactOffset3;

    @Column("Flags")
    private int flags;

    @Column("SpellVisualMissileSetID")
    private short spellVisualMissileSetID;

    @Column("MissileDestinationAttachment")
    private byte missileDestinationAttachment;

    @Column("MissileAttachment")
    private byte missileAttachment;

    @Column("MissileCastPositionerID")
    private int missileCastPositionerID;

    @Column("MissileImpactPositionerID")
    private int missileImpactPositionerID;

    @Column("MissileTargetingKit")
    private int missileTargetingKit;

    @Column("AnimEventSoundID")
    private int animEventSoundID;

    @Column("DamageNumberDelay")
    private short damageNumberDelay;

    @Column("HostileSpellVisualID")
    private int hostileSpellVisualID;

    @Column("CasterSpellVisualID")
    private int casterSpellVisualID;

    @Column("LowViolenceSpellVisualID")
    private int lowViolenceSpellVisualID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
