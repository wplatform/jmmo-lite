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
@Table(name = "rbac_default_permissions")
public class RbacDefaultPermissions {
    @Id
    @Column("secId")
    private Integer secId;

    @Id
    @Column("permissionId")
    private Integer permissionId;

    @Column("realmId")
    private Integer realmId;
}
