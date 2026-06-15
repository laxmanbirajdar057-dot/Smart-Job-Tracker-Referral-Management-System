package com.job.tracker.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String roleName;

    private String jobUrl;

    private String jobDescription;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private String location;

    private String salary;

    private String companySize;

    private LocalDate postedDate;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // public void setUser(User user) {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }

    public enum JobType {
        FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, TEMPORARY
    }

    public enum ApplicationStatus {
        SAVED, APPLIED, SCREENING, TECHNICAL_ROUND, HR_ROUND, OFFER, REJECTED, NEGOTIATING
    }
}