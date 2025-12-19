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


@Table(name = "keychain")
@Db2DataBind(name = "Keychain.db2", layoutHash = 0x5B214E82, fields = {
        @Db2Field(name = {"key1", "key2", "key3", "key4", "key5", "key6", "key7", "key8", "key9", "key10", "key11", "key12", "key13", "key14", "key15", "key16", "key17", "key18", "key19", "key20", "key21", "key22", "key23", "key24", "key25", "key26", "key27", "key28", "key29", "key30", "key31", "key32"}, type = Db2Type.BYTE)
})
public class Keychain implements DbcEntity {
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

    @Column("Key17")
    private short key17;

    @Column("Key18")
    private short key18;

    @Column("Key19")
    private short key19;

    @Column("Key20")
    private short key20;

    @Column("Key21")
    private short key21;

    @Column("Key22")
    private short key22;

    @Column("Key23")
    private short key23;

    @Column("Key24")
    private short key24;

    @Column("Key25")
    private short key25;

    @Column("Key26")
    private short key26;

    @Column("Key27")
    private short key27;

    @Column("Key28")
    private short key28;

    @Column("Key29")
    private short key29;

    @Column("Key30")
    private short key30;

    @Column("Key31")
    private short key31;

    @Column("Key32")
    private short key32;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
