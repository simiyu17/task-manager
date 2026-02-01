package com.task.manage.document.domain;

import com.task.manage.shared.domain.BaseEntity;
import com.task.manage.task.domain.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "documents",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "task_id" , "version", "document_type"}, name = "UNIQUE_DOCUMENT")}
)
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "version")
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_location")
    private FileLocation fileLocation;

    @Column(name = "is_final")
    private boolean isFinal;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(task, document.task)
                .append(version, document.version)
                .append(documentType, document.documentType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(task)
                .append(version)
                .append(documentType)
                .toHashCode();
    }

    @Getter
    enum DocumentType {
        WBS, CONCEPT_NOTE, INCEPTION_REPORT, DRAFT_REPORT, FINAL_REPORT;

        private final String displayName;

        DocumentType() {
            this.displayName = this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
        }
    }

    @Getter
    enum FileLocation {
        LOCAL_DISK, AWS_S3, AZURE_BLOB, GOOGLE_GCP;

        private final String displayName;

        FileLocation() {
            this.displayName = this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
        }
    }
}
