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


@Table(name = "battle_pet_breed_state")
@Db2DataBind(name = "BattlePetBreedState.db2", layoutHash = 0x68D5C999, parentIndexField = 2, fields = {
        @Db2Field(name = "value", type = Db2Type.SHORT),
        @Db2Field(name = "battlePetStateID", type = Db2Type.BYTE),
        @Db2Field(name = "battlePetBreedID", type = Db2Type.BYTE)
})
public class BattlePetBreedState implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Value")
    private short value;

    @Column("BattlePetStateID")
    private byte battlePetStateID;

    @Column("BattlePetBreedID")
    private byte battlePetBreedID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
