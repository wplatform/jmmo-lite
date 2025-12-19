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


@Table(name = "durability_costs")
@Db2DataBind(name = "DurabilityCosts.db2", layoutHash = 0x8447966A, fields = {
        @Db2Field(name = {"weaponSubClassCost1", "weaponSubClassCost2", "weaponSubClassCost3", "weaponSubClassCost4", "weaponSubClassCost5", "weaponSubClassCost6", "weaponSubClassCost7", "weaponSubClassCost8", "weaponSubClassCost9", "weaponSubClassCost10", "weaponSubClassCost11", "weaponSubClassCost12", "weaponSubClassCost13", "weaponSubClassCost14", "weaponSubClassCost15", "weaponSubClassCost16", "weaponSubClassCost17", "weaponSubClassCost18", "weaponSubClassCost19", "weaponSubClassCost20", "weaponSubClassCost21"}, type = Db2Type.SHORT),
        @Db2Field(name = {"armorSubClassCost1", "armorSubClassCost2", "armorSubClassCost3", "armorSubClassCost4", "armorSubClassCost5", "armorSubClassCost6", "armorSubClassCost7", "armorSubClassCost8"}, type = Db2Type.SHORT)
})
public class DurabilityCost implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("WeaponSubClassCost1")
    private short weaponSubClassCost1;

    @Column("WeaponSubClassCost2")
    private short weaponSubClassCost2;

    @Column("WeaponSubClassCost3")
    private short weaponSubClassCost3;

    @Column("WeaponSubClassCost4")
    private short weaponSubClassCost4;

    @Column("WeaponSubClassCost5")
    private short weaponSubClassCost5;

    @Column("WeaponSubClassCost6")
    private short weaponSubClassCost6;

    @Column("WeaponSubClassCost7")
    private short weaponSubClassCost7;

    @Column("WeaponSubClassCost8")
    private short weaponSubClassCost8;

    @Column("WeaponSubClassCost9")
    private short weaponSubClassCost9;

    @Column("WeaponSubClassCost10")
    private short weaponSubClassCost10;

    @Column("WeaponSubClassCost11")
    private short weaponSubClassCost11;

    @Column("WeaponSubClassCost12")
    private short weaponSubClassCost12;

    @Column("WeaponSubClassCost13")
    private short weaponSubClassCost13;

    @Column("WeaponSubClassCost14")
    private short weaponSubClassCost14;

    @Column("WeaponSubClassCost15")
    private short weaponSubClassCost15;

    @Column("WeaponSubClassCost16")
    private short weaponSubClassCost16;

    @Column("WeaponSubClassCost17")
    private short weaponSubClassCost17;

    @Column("WeaponSubClassCost18")
    private short weaponSubClassCost18;

    @Column("WeaponSubClassCost19")
    private short weaponSubClassCost19;

    @Column("WeaponSubClassCost20")
    private short weaponSubClassCost20;

    @Column("WeaponSubClassCost21")
    private short weaponSubClassCost21;

    @Column("ArmorSubClassCost1")
    private short armorSubClassCost1;

    @Column("ArmorSubClassCost2")
    private short armorSubClassCost2;

    @Column("ArmorSubClassCost3")
    private short armorSubClassCost3;

    @Column("ArmorSubClassCost4")
    private short armorSubClassCost4;

    @Column("ArmorSubClassCost5")
    private short armorSubClassCost5;

    @Column("ArmorSubClassCost6")
    private short armorSubClassCost6;

    @Column("ArmorSubClassCost7")
    private short armorSubClassCost7;

    @Column("ArmorSubClassCost8")
    private short armorSubClassCost8;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
