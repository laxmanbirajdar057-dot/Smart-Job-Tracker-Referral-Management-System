package com.job.tracker.repository;

import com.job.tracker.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Job> findByUserIdAndStatus(Long userId, Job.ApplicationStatus status);

    List<Job> findByUserIdAndCompany(Long userId, String company);

    long countByUserId(Long userId);

    Optional<Job> findByIdAndUserId(Long id, Long userId);

    List<Job> findByReferralId(Long referralId);

    long countByReferralId(Long referralId);

    long countByUserIdAndReferralIsNotNull(Long userId);

    Optional<Job> findByUserIdAndCompanyIgnoreCaseAndRoleNameIgnoreCase(
            Long userId, String company, String roleName);
}
