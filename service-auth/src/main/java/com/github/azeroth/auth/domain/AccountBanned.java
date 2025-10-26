package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account_banned")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBanned {
    
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
    
    @Column("active")
    private Byte active;
}
