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
@Table(name = "rbac_linked_permissions")
public class RbacLinkedPermissions {
    @Id
    @Column("id")
    private Integer id;

    @Id
    @Column("linkedId")
    private Integer linkedId;
}
