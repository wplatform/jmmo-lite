package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battlenet_item_appearances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlenetItemAppearance {
    
    @Id
    @Column("battlenetAccountId")
    private int battlenetAccountId;
    
    @Column("blobIndex")
    private short blobIndex;
    
    @Column("appearanceMask")
    private int appearanceMask;
}
