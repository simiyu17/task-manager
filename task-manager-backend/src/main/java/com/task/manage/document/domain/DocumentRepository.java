package com.task.manage.document.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByTaskId(Long taskId);

    List<Document> findByTaskIdAndDocumentType(Long taskId, Document.DocumentType documentType);

    boolean existsByTaskIdAndDocumentType(Long taskId, Document.DocumentType documentType);

    @Query("SELECT MAX(d.version) FROM Document d WHERE d.task.id = :taskId AND d.documentType = :documentType")
    Optional<Integer> findMaxVersionByTaskIdAndDocumentType(Long taskId, Document.DocumentType documentType);

    Optional<Document> findByTaskIdAndVersionAndDocumentType(Long taskId, Integer version, Document.DocumentType documentType);
}
