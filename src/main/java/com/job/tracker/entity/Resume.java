package com.job.tracker.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String label;           // e.g. "Backend-Java-CV"

    private String targetRole;      // e.g. "Java Backend Developer"

    private String version;         // e.g. "v1", "v2"

    @Column(name = "cv_file", columnDefinition = "LONGBLOB")
    private byte[] cvFile;

    @Column(name = "cv_file_name")
    private String cvFileName;      // e.g. "laxman_backend_v2.pdf"

    @Column(name = "cv_file_type")
    private String cvFileType;      // "application/pdf"

    @Column(name = "uploaded_at")
    private LocalDate uploadedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (uploadedAt == null) uploadedAt = LocalDate.now();
    }

    // ===== GETTERS / SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public byte[] getCvFile() { return cvFile; }
    public void setCvFile(byte[] cvFile) { this.cvFile = cvFile; }

    public String getCvFileName() { return cvFileName; }
    public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }

    public String getCvFileType() { return cvFileType; }
    public void setCvFileType(String cvFileType) { this.cvFileType = cvFileType; }

    public LocalDate getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDate uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}