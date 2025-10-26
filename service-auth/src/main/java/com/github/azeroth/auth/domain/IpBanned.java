package com.github.azeroth.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ip_banned")
public class IpBanned {
    @Id
    @Column("ip")
    private String ip;

    @Id
    @Column("bandate")
    private Long bandate;

    @Column("unbandate")
    private Long unbandate;

    @Column("bannedby")
    private String bannedby;

    @Column("banreason")
    private String banreason;

    @Column("active")
    private Boolean active;
}
