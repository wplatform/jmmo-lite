package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account_muted")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountMuted {
    
    @Id
    @Column("guid")
    private Long guid;
    
    @Column("mutedate")
    private Long muteDate;
    
    @Column("mutetime")
    private Long muteTime;
    
    @Column("mutedby")
    private String mutedBy;
    
    @Column("mutereason")
    private String muteReason;
}
