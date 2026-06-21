package com.job.tracker.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.job.tracker.dto.ReferralDTO.CreateReferralRequest;
import com.job.tracker.dto.ReferralDTO.ReferralListResponse;
import com.job.tracker.dto.ReferralDTO.ReferralResponse;
import com.job.tracker.dto.ReferralDTO.ReferralStats;
import com.job.tracker.dto.ReferralDTO.UpdateReferralRequest;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.Referral;
import com.job.tracker.entity.User;
import com.job.tracker.repository.JobRepository;
import com.job.tracker.repository.ReferralRepository;
import com.job.tracker.repository.UserRepository;

@Service
public class ReferralService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private JobRepository jobRepository;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ReferralResponse createReferral(Long userId, CreateReferralRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getReferrerName() == null || request.getReferrerName().isBlank()) {
            throw new RuntimeException("referrerName is required");
        }
        System.out.println("CREATE REFERRAL CALLED");
        System.out.println(request.getReferrerName());

        Referral referral = new Referral();
        referral.setUser(user);
        referral.setReferrerName(request.getReferrerName());
        referral.setReferrerEmail(request.getReferrerEmail());
        referral.setReferrerPhone(request.getReferrerPhone());
        referral.setCompany(request.getCompany());
        referral.setReferrerLinkedinUrl(request.getReferrerLinkedinUrl());
        referral.setRelationship(parseRelationship(request.getRelationship()));
        referral.setNotes(request.getNotes());

        referral.setStatus(
                request.getStatus() != null ? request.getStatus() : "REQUESTED");

        referral = referralRepository.save(referral);

        return toReferralResponse(referral);

        
    }
    

    public ReferralListResponse getAllReferrals(Long userId) {
        List<Referral> referrals = referralRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<ReferralResponse> data = referrals.stream()
                .map(this::toReferralResponse)
                .collect(Collectors.toList());

        ReferralListResponse response = new ReferralListResponse();
        response.setData(data);
        response.setTotalCount(data.size());
        return response;
    }

    public ReferralResponse getReferral(Long id, Long userId) {
        Referral referral = referralRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Referral not found"));
        return toReferralResponse(referral);
    }

    public ReferralResponse updateReferral(Long id, Long userId, UpdateReferralRequest request) {
        Referral referral = referralRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Referral not found"));

        if (request.getReferrerName() != null)
            referral.setReferrerName(request.getReferrerName());
        if (request.getReferrerEmail() != null)
            referral.setReferrerEmail(request.getReferrerEmail());
        if (request.getReferrerPhone() != null)
            referral.setReferrerPhone(request.getReferrerPhone());
        if (request.getCompany() != null)
            referral.setCompany(request.getCompany());
        if (request.getReferrerLinkedinUrl() != null)
            referral.setReferrerLinkedinUrl(request.getReferrerLinkedinUrl());
        if (request.getRelationship() != null)
            referral.setRelationship(parseRelationship(request.getRelationship()));
        if (request.getNotes() != null)
            referral.setNotes(request.getNotes());
        if (request.getStatus() != null)
            referral.setStatus(request.getStatus());

        referral = referralRepository.save(referral);

        return toReferralResponse(referral);
    }

    public void deleteReferral(Long id, Long userId) {
        Referral referral = referralRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Referral not found"));

        // Unlink any jobs pointing at this referral first, otherwise the
        // foreign key on jobs.referral_id blocks the delete.
        List<Job> linkedJobs = jobRepository.findByReferralId(id);
        for (Job job : linkedJobs) {
            job.setReferral(null);
        }
        jobRepository.saveAll(linkedJobs);

        referralRepository.delete(referral);
    }

    public ReferralStats getReferralStats(Long userId) {
        long totalReferrals = referralRepository.countByUserId(userId);
        long totalApplications = jobRepository.countByUserId(userId);
        long totalViaReferral = jobRepository.countByUserIdAndReferralIsNotNull(userId);

        ReferralStats stats = new ReferralStats();
        stats.setTotalReferrals(totalReferrals);
        stats.setTotalApplications(totalApplications);
        stats.setTotalApplicationsViaReferral(totalViaReferral);
        stats.setReferralRatePercent(
                totalApplications == 0 ? 0.0 : (totalViaReferral * 100.0) / totalApplications);
        return stats;
    }

    // ================= HELPERS =================

    private Referral.RelationshipType parseRelationship(String relationship) {
        if (relationship == null || relationship.isBlank())
            return null;
        try {
            return Referral.RelationshipType.valueOf(relationship.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid relationship: " + relationship);
        }
    }

    private ReferralResponse toReferralResponse(Referral referral) {
        ReferralResponse response = new ReferralResponse();
        response.setId(referral.getId());
        response.setReferrerName(referral.getReferrerName());
        response.setReferrerEmail(referral.getReferrerEmail());
        response.setReferrerPhone(referral.getReferrerPhone());
        response.setCompany(referral.getCompany());
        response.setReferrerLinkedinUrl(referral.getReferrerLinkedinUrl());
        response.setRelationship(referral.getRelationship() != null ? referral.getRelationship().name() : null);
        response.setNotes(referral.getNotes());
        response.setStatus(referral.getStatus());
        response.setReferredJobCount(jobRepository.countByReferralId(referral.getId()));
        response.setCreatedAt(
                referral.getCreatedAt() != null ? referral.getCreatedAt().format(TIMESTAMP_FORMAT) : null);
        response.setUpdatedAt(
                referral.getUpdatedAt() != null ? referral.getUpdatedAt().format(TIMESTAMP_FORMAT) : null);
        return response;
    }
}
