package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("battlenet_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BNetAccount {
    
    @Id
    @Column("id")
    private Integer id;
    
    @Column("email")
    private String email;

    @Column("srp_version")
    private Byte srpVersion;

    @Column("salt")
    private byte[] salt;

    @Column("verifier")
    private byte[] verifier;
    
    @Column("joindate")
    private LocalDateTime joinDate;

    @Column("last_ip")
    private String lastIp;
    
    @Column("failed_logins")
    private Byte failedLogins;
    
    @Column("locked")
    private Byte locked;
    
    @Column("lock_country")
    private String lockCountry;
    
    @Column("last_login")
    private LocalDateTime lastLogin;
    
    @Column("online")
    private Byte online;
    
    @Column("locale")
    private Byte locale;
    
    @Column("os")
    private String os;
    
    @Column("LastCharacterUndelete")
    private Long lastCharacterUndelete;
    
    @Column("LoginTicket")
    private String loginTicket;
    
    @Column("LoginTicketExpiry")
    private Long loginTicketExpiry;
}
