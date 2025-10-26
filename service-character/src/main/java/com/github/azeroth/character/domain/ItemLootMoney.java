package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "item_loot_money")
public class ItemLootMoney {
    @Id

    @Column("container_id")
    private Long id;


    @Column("money")
    private Long money;

}