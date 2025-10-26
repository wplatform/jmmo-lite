package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battlenet_account_mounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlenetAccountMounts {
    
    @Id
    @Column("battlenetAccountId")
    private Long battlenetAccountId;
    
    @Column("mountSpellId")
    private Integer mountSpellId;
    
    @Column("flags")
    private Byte flags;
}
