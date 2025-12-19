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


@Table(name = "battle_pet_species_state")
@Db2DataBind(name = "BattlePetSpeciesState.db2", layoutHash = 0x8F958D5C, parentIndexField = 2, fields = {
        @Db2Field(name = "value", type = Db2Type.INT, signed = true),
        @Db2Field(name = "battlePetStateID", type = Db2Type.BYTE),
        @Db2Field(name = "battlePetSpeciesID", type = Db2Type.SHORT)
})
public class BattlePetSpeciesState implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Value")
    private int value;

    @Column("BattlePetStateID")
    private short battlePetStateID;

    @Column("BattlePetSpeciesID")
    private short battlePetSpeciesID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
