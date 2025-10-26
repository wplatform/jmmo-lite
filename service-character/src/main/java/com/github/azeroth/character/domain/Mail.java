package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "mail")
public class Mail {
    @Id

    @Column("id")
    private Long id;


    @Column("messageType")
    private Short messageType;


    @Column("stationery")
    private Byte stationery;


    @Column("mailTemplateId")
    private Integer mailTemplateId;


    @Column("sender")
    private Long sender;


    @Column("receiver")
    private Long receiver;

    
    @Column("subject")
    private String subject;

    
    @Column("body")
    private String body;


    @Column("has_items")
    private Short hasItems;


    @Column("expire_time")
    private Long expireTime;


    @Column("deliver_time")
    private Long deliverTime;


    @Column("money")
    private Long money;


    @Column("cod")
    private Long cod;


    @Column("checked")
    private Short checked;

}