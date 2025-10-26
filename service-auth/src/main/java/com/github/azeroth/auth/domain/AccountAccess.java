package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account_access")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountAccess {
    
    @Id
    @Column("AccountID")
    private Long accountId;
    
    @Column("SecurityLevel")
    private Byte securityLevel;
    
    @Column("RealmID")
    private Integer realmId;
    
    @Column("Comment")
    private String comment;
}
