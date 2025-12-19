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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "battle_pet_ability")
@Db2DataBind(name = "BattlePetAbility.db2", layoutHash = 0x0F29944D, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "iconFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "petTypeEnum", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "cooldown", type = Db2Type.INT),
        @Db2Field(name = "battlePetVisualID", type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
})
public class BattlePetAbility implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Name")
    private LocalizedString name;

    
    @Column("Description")
    private LocalizedString description;


    @Column("IconFileDataID")
    private int iconFileDataID;


    @Column("PetTypeEnum")
    private byte petTypeEnum;


    @Column("Cooldown")
    private Long cooldown;


    @Column("BattlePetVisualID")
    private int battlePetVisualID;


    @Column("Flags")
    private short flags;

}