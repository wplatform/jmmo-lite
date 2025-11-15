package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battlenet_account_heirlooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlenetAccountHeirloom {
    
    @Id
    @Column("accountId")
    private Integer accountId;
    
    @Column("itemId")
    private Integer itemId;
    
    @Column("flags")
    private Integer flags;
}
