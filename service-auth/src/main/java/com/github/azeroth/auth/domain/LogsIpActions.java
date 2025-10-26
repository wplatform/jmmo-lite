package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logs_ip_actions")
public class LogsIpActions {
    @Id
    @Column("id")
    private Integer id;

    @Column("account_id")
    private Integer accountId;

    @Column("character_guid")
    private Long characterGuid;
    
    @Column("type")
    private Byte type;

    @Column("ip")
    private String ip;
    
    @Column("systemnote")
    private String systemNote;
    
    @Column("unixtime")
    private Long unixTime;
    
    @Column("time")
    private Instant time;
    
    @Column("comment")
    private String comment;
}
