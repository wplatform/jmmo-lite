package com.github.azeroth.dbc.domain;

import org.springframework.data.relational.core.mapping.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Embeddable
public class HotfixDatumId implements Serializable {
    private static final long serialVersionUID = 8839744695552428397L;
    @Column("Id")
    private int id;

    @Column("TableHash")
    private Long tableHash;

    @Column("RecordId")
    private int recordId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HotfixDatumId entity = (HotfixDatumId) o;
        return Objects.equals(this.recordId, entity.recordId) &&
                Objects.equals(this.id, entity.id) &&
                Objects.equals(this.tableHash, entity.tableHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId, id, tableHash);
    }

}