package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Table(name = "item_soulbound_trade_data")
public class ItemSoulboundTradeDatum {
    @Id
    @Column("itemGuid")
    private Long id;


    @Column("allowedPlayers")
    private String allowedPlayers;

}