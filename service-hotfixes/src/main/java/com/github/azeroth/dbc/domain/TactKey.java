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


@Table(name = "tact_key")
@Db2DataBind(name = "TactKey.db2", layoutHash = 0xF0F98B62, fields = {
        @Db2Field(name = {"key1", "key2", "key3", "key4", "key5", "key6", "key7", "key8", "key9", "key10", "key11", "key12", "key13", "key14", "key15", "key16"}, type = Db2Type.BYTE)
})
public class TactKey implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Key1")
    private short key1;

    @Column("Key2")
    private short key2;

    @Column("Key3")
    private short key3;

    @Column("Key4")
    private short key4;

    @Column("Key5")
    private short key5;

    @Column("Key6")
    private short key6;

    @Column("Key7")
    private short key7;

    @Column("Key8")
    private short key8;

    @Column("Key9")
    private short key9;

    @Column("Key10")
    private short key10;

    @Column("Key11")
    private short key11;

    @Column("Key12")
    private short key12;

    @Column("Key13")
    private short key13;

    @Column("Key14")
    private short key14;

    @Column("Key15")
    private short key15;

    @Column("Key16")
    private short key16;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
