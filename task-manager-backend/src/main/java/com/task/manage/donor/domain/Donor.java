package com.task.manage.donor.domain;

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
@Table(name = "donors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"donor_name"}, name = "DONOR_NAME_UNIQUE"),
        @UniqueConstraint(columnNames = {"email_address"}, name = "DONOR_EMAIL_UNIQUE")
})
@Getter
@Setter
@NoArgsConstructor
public class Donor extends BaseEntity {

    @Column(name = "donor_name", nullable = false)
    private String donorName;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "contact_number")
    private String contactNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Donor donor = (Donor) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(donorName, donor.donorName)
                .append(emailAddress, donor.emailAddress)
                .append(contactNumber, donor.contactNumber)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(donorName)
                .append(emailAddress)
                .append(contactNumber)
                .toHashCode();
    }
}

