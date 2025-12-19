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


@Table(name = "lock")
@Db2DataBind(name = "Lock.db2", layoutHash = 0xDAC7F42F, fields = {
        @Db2Field(name = {"index1", "index2", "index3", "index4", "index5", "index6", "index7", "index8"}, type = Db2Type.INT, signed = true),
        @Db2Field(name = {"skill1", "skill2", "skill3", "skill4", "skill5", "skill6", "skill7", "skill8"}, type = Db2Type.SHORT),
        @Db2Field(name = {"type1", "type2", "type3", "type4", "type5", "type6", "type7", "type8"}, type = Db2Type.BYTE),
        @Db2Field(name = {"action1", "action2", "action3", "action4", "action5", "action6", "action7", "action8"}, type = Db2Type.BYTE)
})
public class Lock implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Index1")
    private int index1;

    @Column("Index2")
    private int index2;

    @Column("Index3")
    private int index3;

    @Column("Index4")
    private int index4;

    @Column("Index5")
    private int index5;

    @Column("Index6")
    private int index6;

    @Column("Index7")
    private int index7;

    @Column("Index8")
    private int index8;

    @Column("Skill1")
    private short skill1;

    @Column("Skill2")
    private short skill2;

    @Column("Skill3")
    private short skill3;

    @Column("Skill4")
    private short skill4;

    @Column("Skill5")
    private short skill5;

    @Column("Skill6")
    private short skill6;

    @Column("Skill7")
    private short skill7;

    @Column("Skill8")
    private short skill8;

    @Column("Type1")
    private byte type1;

    @Column("Type2")
    private byte type2;

    @Column("Type3")
    private byte type3;

    @Column("Type4")
    private byte type4;

    @Column("Type5")
    private byte type5;

    @Column("Type6")
    private byte type6;

    @Column("Type7")
    private byte type7;

    @Column("Type8")
    private byte type8;

    @Column("Action1")
    private byte action1;

    @Column("Action2")
    private byte action2;

    @Column("Action3")
    private byte action3;

    @Column("Action4")
    private byte action4;

    @Column("Action5")
    private byte action5;

    @Column("Action6")
    private byte action6;

    @Column("Action7")
    private byte action7;

    @Column("Action8")
    private byte action8;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
