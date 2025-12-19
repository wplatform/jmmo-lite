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
public class HotfixBlobId implements Serializable {
    private static final long serialVersionUID = -1857299270108049483L;
    @Column("TableHash")
    private Long tableHash;

    @Column("RecordId")
    private int recordId;

    @Column("locale")
    private String locale;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HotfixBlobId entity = (HotfixBlobId) o;
        return Objects.equals(this.recordId, entity.recordId) &&
                Objects.equals(this.locale, entity.locale) &&
                Objects.equals(this.tableHash, entity.tableHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId, locale, tableHash);
    }

}