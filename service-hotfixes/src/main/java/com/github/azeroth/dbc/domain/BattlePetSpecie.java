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


@Table(name = "battle_pet_species")
@Db2DataBind(name = "BattlePetSpecies.db2", layoutHash = 0x8A3D97A4, indexField = 8, fields = {
        @Db2Field(name = "sourceText", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "creatureID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "summonSpellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = "petTypeEnum", type = Db2Type.BYTE),
        @Db2Field(name = "sourceTypeEnum", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "id", type = Db2Type.INT),
        @Db2Field(name = "cardUIModelSceneID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "loadoutUIModelSceneID", type = Db2Type.INT, signed = true)
})
public class BattlePetSpecie implements DbcEntity {
    @Column("SourceText")
    private LocalizedString sourceText;

    @Column("Description")
    private LocalizedString description;

    @Column("CreatureID")
    private int creatureID;

    @Column("IconFileDataID")
    private int iconFileDataID;

    @Column("SummonSpellID")
    private int summonSpellID;

    @Column("Flags")
    private short flags;

    @Column("PetTypeEnum")
    private byte petTypeEnum;

    @Column("SourceTypeEnum")
    private byte sourceTypeEnum;

    @Id

    @Column("ID")
    private int id;

    @Column("CardUIModelSceneID")
    private int cardUIModelSceneID;

    @Column("LoadoutUIModelSceneID")
    private int loadoutUIModelSceneID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
