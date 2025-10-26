package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battlenet_account_bans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlenetAccountBan {
    
    @Id
    @Column("id")
    private Long id;
    
    @Column("bandate")
    private Long banDate;
    
    @Column("unbandate")
    private Long unbanDate;
    
    @Column("bannedby")
    private String bannedBy;
    
    @Column("banreason")
    private String banReason;
}
