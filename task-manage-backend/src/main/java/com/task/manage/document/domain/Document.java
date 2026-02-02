package com.task.manage.document.domain;

import com.fasterxml.jackson.annotation.JsonValue;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    public enum DocumentType {
        WBS, CONCEPT_NOTE, INCEPTION_REPORT, DRAFT_REPORT, FINAL_REPORT;

        @JsonValue
        public String getName() {
            return this.name();
        }

        public String getDisplayName() {
            return this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
        }

        /**
         * Get DocumentType from string value (case-insensitive)
         * @param value the string value
         * @return DocumentType or null if not found
         */
        public static DocumentType fromString(String value) {
            if (value == null) {
                return null;
            }
            for (DocumentType type : DocumentType.values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Getter
    public enum FileLocation {
        LOCAL_DISK, AWS_S3, AZURE_BLOB, GOOGLE_GCP;

        @JsonValue
        public String getName() {
            return this.name();
        }

        public String getDisplayName() {
            return this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
        }

        /**
         * Get FileLocation from string value (case-insensitive)
         * @param value the string value
         * @return FileLocation or null if not found
         */
        public static FileLocation fromString(String value) {
            if (value == null) {
                return null;
            }
            for (FileLocation location : FileLocation.values()) {
                if (location.name().equalsIgnoreCase(value)) {
                    return location;
                }
            }
            return null;
        }
    }
}
