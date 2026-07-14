package com.job.tracker.service;

import com.job.tracker.dto.MatchScoreDTO;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.Resume;
import com.job.tracker.exception.ResourceNotFoundException;
import com.job.tracker.repository.JobRepository;
import com.job.tracker.util.SkillKeywords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchScoreService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeTextExtractor textExtractor;

    /**
     * Computes a resume<->job-description match score using a curated skill
     * dictionary. Every skill in SkillKeywords.DICTIONARY is checked as a
     * substring against both texts (lowercased), then we take the intersection.
     *
     * @param resumeId optional — if null, falls back to the resume already linked to the job
     */
    public MatchScoreDTO.Response calculateMatch(Long jobId, Long resumeId, Long userId) {

        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Resume resume;
        if (resumeId != null) {
            resume = resumeService.getRawResume(resumeId, userId);
        } else {
            resume = job.getResume();
        }

        if (resume == null) {
            throw new ResourceNotFoundException(
                    "No resume linked to this job — pass ?resumeId= or attach a resume to the job first");
        }

        String jdText = job.getJobDescription() == null ? "" : job.getJobDescription().toLowerCase();
        String resumeText = textExtractor.extractText(resume.getCvFile()).toLowerCase();

        Set<String> jdSkills = SkillKeywords.DICTIONARY.stream()
                .filter(jdText::contains)
                .collect(Collectors.toSet());

        Set<String> resumeSkills = SkillKeywords.DICTIONARY.stream()
                .filter(resumeText::contains)
                .collect(Collectors.toSet());

        Set<String> matched = new HashSet<>(jdSkills);
        matched.retainAll(resumeSkills);

        Set<String> missing = new HashSet<>(jdSkills);
        missing.removeAll(matched);

        double score = jdSkills.isEmpty() ? 0.0 : (matched.size() * 100.0) / jdSkills.size();

        return new MatchScoreDTO.Response(Math.round(score), matched, missing, resume.getId());
    }
}
