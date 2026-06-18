package com.job.tracker.service;

import com.job.tracker.dto.JobDTO.CreateJobRequest;
import com.job.tracker.dto.JobDTO.JobListResponse;
import com.job.tracker.dto.JobDTO.JobResponse;
import com.job.tracker.dto.JobDTO.UpdateJobRequest;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.User;
import com.job.tracker.repository.JobRepository;
import com.job.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public JobResponse createJob(Long userId, CreateJobRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = new Job();
        job.setUser(user);
        job.setCompany(request.getCompany());
        job.setRoleName(request.getRoleName());
        job.setJobUrl(request.getJobUrl());
        job.setJobDescription(request.getJobDescription());
        job.setJobType(parseJobType(request.getJobType()));
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setCompanySize(request.getCompanySize());
        job.setPostedDate(request.getPostedDate());
        job.setDeadline(request.getDeadline());
        job.setStatus(parseStatus(request.getStatus()));
        job.setNotes(request.getNotes());

        job = jobRepository.save(job);

        return toJobResponse(job);
    }

    public JobListResponse getAllJobs(Long userId, String status) {
        List<Job> jobs;

        if (status != null && !status.isBlank()) {
            jobs = jobRepository.findByUserIdAndStatus(userId, parseStatus(status));
        } else {
            jobs = jobRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        List<JobResponse> data = jobs.stream()
                .map(this::toJobResponse)
                .collect(Collectors.toList());

        JobListResponse response = new JobListResponse();
        response.setData(data);
        response.setTotalCount(data.size());
        return response;
    }

    public JobResponse updateJob(Long id, Long userId, UpdateJobRequest request) {
        Job job = jobRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (request.getCompany() != null) job.setCompany(request.getCompany());
        if (request.getRoleName() != null) job.setRoleName(request.getRoleName());
        if (request.getJobUrl() != null) job.setJobUrl(request.getJobUrl());
        if (request.getJobDescription() != null) job.setJobDescription(request.getJobDescription());
        if (request.getJobType() != null) job.setJobType(parseJobType(request.getJobType()));
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getSalary() != null) job.setSalary(request.getSalary());
        if (request.getCompanySize() != null) job.setCompanySize(request.getCompanySize());
        if (request.getDeadline() != null) job.setDeadline(request.getDeadline());
        if (request.getStatus() != null) job.setStatus(parseStatus(request.getStatus()));
        if (request.getNotes() != null) job.setNotes(request.getNotes());

        job = jobRepository.save(job);

        return toJobResponse(job);
    }

    public void deleteJob(Long id, Long userId) {
        Job job = jobRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        jobRepository.delete(job);
    }

    public long getJobCount(Long userId) {
        return jobRepository.countByUserId(userId);
    }

    public JobResponse getJob(Long id, Long userId) {
        Job job = jobRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return toJobResponse(job);
    }

    // ================= HELPERS =================

    private Job.JobType parseJobType(String jobType) {
        if (jobType == null || jobType.isBlank()) return null;
        try {
            return Job.JobType.valueOf(jobType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid jobType: " + jobType);
        }
    }

    private Job.ApplicationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return Job.ApplicationStatus.SAVED;
        try {
            return Job.ApplicationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    private JobResponse toJobResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setCompany(job.getCompany());
        response.setRoleName(job.getRoleName());
        response.setJobUrl(job.getJobUrl());
        response.setJobDescription(job.getJobDescription());
        response.setJobType(job.getJobType() != null ? job.getJobType().name() : null);
        response.setLocation(job.getLocation());
        response.setSalary(job.getSalary());
        response.setCompanySize(job.getCompanySize());
        response.setPostedDate(job.getPostedDate());
        response.setDeadline(job.getDeadline());
        response.setStatus(job.getStatus() != null ? job.getStatus().name() : null);
        response.setNotes(job.getNotes());
        response.setCreatedAt(job.getCreatedAt() != null ? job.getCreatedAt().format(TIMESTAMP_FORMAT) : null);
        response.setUpdatedAt(job.getUpdatedAt() != null ? job.getUpdatedAt().format(TIMESTAMP_FORMAT) : null);
        return response;
    }
}