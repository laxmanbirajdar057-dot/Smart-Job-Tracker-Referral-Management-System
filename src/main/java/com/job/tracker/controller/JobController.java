package com.job.tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.job.tracker.dto.JobDTO;
import com.job.tracker.dto.MatchScoreDTO;
import com.job.tracker.dto.AnalyticsDTO;
import com.job.tracker.dto.AiFeedbackDTO;
import com.job.tracker.service.JobService;
import com.job.tracker.service.MatchScoreService;
import com.job.tracker.service.AiResumeFeedbackService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final MatchScoreService matchScoreService;
    private final AiResumeFeedbackService aiResumeFeedbackService;

    public JobController(JobService jobService, MatchScoreService matchScoreService,
                          AiResumeFeedbackService aiResumeFeedbackService) {
        this.jobService = jobService;
        this.matchScoreService = matchScoreService;
        this.aiResumeFeedbackService = aiResumeFeedbackService;
    }

    @PostMapping
    public ResponseEntity<JobDTO.JobResponse> createJob(
            @Valid @RequestBody JobDTO.CreateJobRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        JobDTO.JobResponse response = jobService.createJob(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<JobDTO.JobListResponse> getAllJobs(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        JobDTO.JobListResponse response = jobService.getAllJobs(userId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO.JobResponse> getJob(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        JobDTO.JobResponse response = jobService.getJob(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobDTO.JobResponse> updateJob(
            @PathVariable Long id,
            @RequestBody JobDTO.UpdateJobRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        JobDTO.JobResponse response = jobService.updateJob(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        jobService.deleteJob(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getJobCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        long count = jobService.getJobCount(userId);
        return ResponseEntity.ok(count);
    }

    // ===== SMART FEATURE: RESUME <-> JD MATCH SCORE =====
    @GetMapping("/{jobId}/match-score")
    public ResponseEntity<MatchScoreDTO.Response> getMatchScore(
            @PathVariable Long jobId,
            @RequestParam(required = false) Long resumeId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        MatchScoreDTO.Response response = matchScoreService.calculateMatch(jobId, resumeId, userId);
        return ResponseEntity.ok(response);
    }

    // ===== SMART FEATURE: ANALYTICS DASHBOARD =====
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDTO.Response> getAnalytics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(jobService.getAnalytics(userId));
    }

    // ===== SMART FEATURE: FOLLOW-UP REMINDERS (on-demand recompute) =====
    @GetMapping("/follow-ups")
    public ResponseEntity<JobDTO.JobListResponse> getFollowUps(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(jobService.getJobsNeedingFollowUp(userId));
    }

    // ===== AI FEATURE: LLM-POWERED RESUME FEEDBACK =====
    @GetMapping("/{jobId}/ai-feedback")
    public ResponseEntity<AiFeedbackDTO.Response> getAiFeedback(
            @PathVariable Long jobId,
            @RequestParam(required = false) Long resumeId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(aiResumeFeedbackService.getFeedback(jobId, resumeId, userId));
    }

}
