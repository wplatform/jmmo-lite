package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "item_refund_instance")
public class ItemRefundInstance {
    @Id
    @Column("item_guid")
    private Long itemGuid;

    @Id
    @Column("player_guid")
    private Long playerGuid;


    @Column("paidMoney")
    private Long paidMoney;


    @Column("paidExtendedCost")
    private Integer paidExtendedCost;

}