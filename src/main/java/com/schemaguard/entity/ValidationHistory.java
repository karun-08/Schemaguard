package com.schemaguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_history")
public class ValidationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long contractId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private boolean valid;

    @Column(columnDefinition = "TEXT")
    private String errors;

    @Column(columnDefinition = "TEXT")
    private String missingColumns;

    @Column(columnDefinition = "TEXT")
    private String unexpectedColumns;

    private LocalDateTime createdAt;

    public ValidationHistory() {
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getMissingColumns() {
        return missingColumns;
    }

    public void setMissingColumns(String missingColumns) {
        this.missingColumns = missingColumns;
    }

    public String getUnexpectedColumns() {
        return unexpectedColumns;
    }

    public void setUnexpectedColumns(String unexpectedColumns) {
        this.unexpectedColumns = unexpectedColumns;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
