package com.task.manage.partner.domain;

import com.task.manage.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "partners", uniqueConstraints = { @UniqueConstraint(columnNames = { "partner_name" }, name = "NAME_UNIQUE")})
@Getter
@Setter
@NoArgsConstructor
public class Partner extends BaseEntity {

    @Column(name = "partner_name")
    private String partnerName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Partner partner = (Partner) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(partnerName, partner.partnerName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(partnerName)
                .toHashCode();
    }
}
