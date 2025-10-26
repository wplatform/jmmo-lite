package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battlenet_account_toys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlenetAccountToys {
    
    @Id
    @Column("accountId")
    private Long accountId;
    
    @Column("itemId")
    private Long itemId;
    
    @Column("isFavourite")
    private Byte isFavourite;
    
    @Column("hasFanfare")
    private Byte hasFanfare;
}
