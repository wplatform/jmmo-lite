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


@Table(name = "spell_shapeshift_form")
@Db2DataBind(name = "SpellShapeshiftForm.db2", layoutHash = 0x130819AF, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "damageVariance", type = Db2Type.FLOAT),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "combatRoundTime", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "mountTypeID", type = Db2Type.SHORT),
        @Db2Field(name = "creatureType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "bonusActionBar", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "attackIconFileID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"creatureDisplayID1", "creatureDisplayID2", "creatureDisplayID3", "creatureDisplayID4"}, type = Db2Type.INT),
        @Db2Field(name = {"presetSpellID1", "presetSpellID2", "presetSpellID3", "presetSpellID4", "presetSpellID5", "presetSpellID6", "presetSpellID7", "presetSpellID8"}, type = Db2Type.INT)
})
public class SpellShapeshiftForm implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("DamageVariance")
    private float damageVariance;

    @Column("Flags")
    private int flags;

    @Column("CombatRoundTime")
    private short combatRoundTime;

    @Column("MountTypeID")
    private short mountTypeID;

    @Column("CreatureType")
    private byte creatureType;

    @Column("BonusActionBar")
    private byte bonusActionBar;

    @Column("AttackIconFileID")
    private int attackIconFileID;

    @Column("CreatureDisplayID1")
    private int creatureDisplayID1;

    @Column("CreatureDisplayID2")
    private int creatureDisplayID2;

    @Column("CreatureDisplayID3")
    private int creatureDisplayID3;

    @Column("CreatureDisplayID4")
    private int creatureDisplayID4;

    @Column("PresetSpellID1")
    private int presetSpellID1;

    @Column("PresetSpellID2")
    private int presetSpellID2;

    @Column("PresetSpellID3")
    private int presetSpellID3;

    @Column("PresetSpellID4")
    private int presetSpellID4;

    @Column("PresetSpellID5")
    private int presetSpellID5;

    @Column("PresetSpellID6")
    private int presetSpellID6;

    @Column("PresetSpellID7")
    private int presetSpellID7;

    @Column("PresetSpellID8")
    private int presetSpellID8;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
