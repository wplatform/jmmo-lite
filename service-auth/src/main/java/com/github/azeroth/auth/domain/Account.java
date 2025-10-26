package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @Column("id")
    private Integer id;
    
    @Column("username")
    private String userName;
    
    @Column("salt")
    private byte[] salt;
    
    @Column("verifier")
    private byte[] verifier;
    
    @Column("session_key_auth")
    private byte[] sessionKeyAuth;
    
    @Column("session_key_bnet")
    private byte[] sessionKeyBnet;
    
    @Column("sha_pass_hash")
    private String shaPassHash;
    
    @Column("token_key")
    private String tokenKey;
    
    @Column("email")
    private String email;
    
    @Column("reg_mail")
    private String regMail;
    
    @Column("joindate")
    private LocalDateTime joinDate;
    
    @Column("last_ip")
    private String lastIp;
    
    @Column("last_attempt_ip")
    private String lastAttemptIp;
    
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
    
    @Column("expansion")
    private Byte expansion;
    
    @Column("mutetime")
    private Long muteTime;
    
    @Column("mutereason")
    private String muteReason;
    
    @Column("muteby")
    private String muteBy;
    
    @Column("locale")
    private Byte locale;
    
    @Column("os")
    private String os;
    
    @Column("recruiter")
    private Long recruiter;
    
    @Column("battlenet_account")
    private Integer battleNetAccountId;
    
    @Column("battlenet_index")
    private Byte battleNetIndex;
}
