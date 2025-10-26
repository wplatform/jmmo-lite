package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rbac_account_permissions")
public class RbacAccountPermissions {
    @Id
    @Column("accountId")
    private Integer accountId;

    @Id
    @Column("permissionId")
    private Integer permissionId;

    @Column("granted")
    private Boolean granted;

    @Column("realmId")
    private Integer realmId;
}
