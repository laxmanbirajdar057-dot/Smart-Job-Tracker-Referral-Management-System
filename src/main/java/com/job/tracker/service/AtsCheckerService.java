package com.job.tracker.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.job.tracker.dto.AtsCheckDTO;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.Resume;
import com.job.tracker.exception.ResourceNotFoundException;
import com.job.tracker.repository.JobRepository;
import com.job.tracker.util.SkillKeywords;

/**
 * SMART FEATURE: ATS CHECKER — role/JD-aware.
 *
 * Job-scoped like MatchScoreService and AiResumeFeedbackService: checks
 * resume parseability/structure (format, contact info, sections, word count)
 * AND how many of THIS job's specific keywords the resume covers — a real
 * ATS scores a resume against the posting it was submitted against, not
 * against a generic skill list.
 */
@Service
public class AtsCheckerService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3,5}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{3,4}");

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeTextExtractor textExtractor;

    public AtsCheckDTO.Response checkResumeForJob(Long jobId, Long resumeId, Long userId) {

        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Resume resume = resumeId != null
                ? resumeService.getRawResume(resumeId, userId)
                : job.getResume();

        if (resume == null) {
            throw new ResourceNotFoundException(
                    "No resume linked to this job — pass ?resumeId= or attach a resume to the job first");
        }

        String text = textExtractor.extractText(resume.getCvFile());
        String lower = text.toLowerCase();

        List<String> passed = new ArrayList<>();
        List<String> issues = new ArrayList<>();

        // 1. File format check
        String fileType = resume.getCvFileType() == null ? "" : resume.getCvFileType().toLowerCase();
        boolean safeFormat = fileType.contains("pdf") || fileType.contains("word")
                || fileType.contains("officedocument");
        if (safeFormat) {
            passed.add("File format (" + fileType + ") is ATS-friendly");
        } else {
            issues.add("File format '" + fileType + "' may not parse well in ATS systems — use PDF or DOCX");
        }

        // 2. Contact info — email
        if (EMAIL_PATTERN.matcher(text).find()) {
            passed.add("Email address detected");
        } else {
            issues.add("No email address detected — ATS may reject applications without a parseable email");
        }

        // 3. Contact info — phone
        if (PHONE_PATTERN.matcher(text).find()) {
            passed.add("Phone number detected");
        } else {
            issues.add("No phone number detected in a standard format");
        }

        // 4-6. Standard section headers
        checkSectionHeader(lower, "experience", passed, issues);
        checkSectionHeader(lower, "education", passed, issues);
        checkSectionHeader(lower, "skills", passed, issues);

        // 7. Word count in a healthy range
        int wordCount = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        if (wordCount >= 250 && wordCount <= 1200) {
            passed.add("Word count (" + wordCount + ") is in a healthy range");
        } else if (wordCount < 250) {
            issues.add("Resume is short (" + wordCount + " words) — ATS and recruiters may read this as sparse");
        } else {
            issues.add("Resume is long (" + wordCount + " words) — consider trimming to 1-2 pages");
        }

        // 8. Role name mentioned somewhere (title alignment matters to ATS parsers)
        String roleName = job.getRoleName() == null ? "" : job.getRoleName().toLowerCase();
        if (!roleName.isBlank() && lower.contains(roleName)) {
            passed.add("Resume mentions the target role title (\"" + job.getRoleName() + "\")");
        } else if (!roleName.isBlank()) {
            issues.add("Resume never mentions the exact role title \"" + job.getRoleName() + "\" — ATS title-matching may miss it");
        }

        // 9. JD-specific keyword coverage — the role/JD-aware part
        String jdText = job.getJobDescription() == null ? "" : job.getJobDescription().toLowerCase();
        Set<String> jdKeywords = SkillKeywords.DICTIONARY.stream()
                .filter(jdText::contains)
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> resumeKeywords = SkillKeywords.DICTIONARY.stream()
                .filter(lower::contains)
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> matchedJdKeywords = new HashSet<>(jdKeywords);
        matchedJdKeywords.retainAll(resumeKeywords);

        Set<String> missingJdKeywords = new HashSet<>(jdKeywords);
        missingJdKeywords.removeAll(matchedJdKeywords);

        if (!jdKeywords.isEmpty()) {
            if (missingJdKeywords.isEmpty()) {
                passed.add("Covers all " + jdKeywords.size() + " keywords found in this job's description");
            } else {
                issues.add(matchedJdKeywords.size() + "/" + jdKeywords.size()
                        + " job-specific keywords covered — missing: " + String.join(", ", missingJdKeywords));
            }
        }

        int totalChecks = passed.size() + issues.size();
        long score = totalChecks == 0 ? 0 : Math.round((passed.size() * 100.0) / totalChecks);

        return new AtsCheckDTO.Response(score, passed, issues, resumeKeywords, missingJdKeywords, wordCount);
    }

    private void checkSectionHeader(String lowerText, String sectionName, List<String> passed, List<String> issues) {
        if (lowerText.contains(sectionName)) {
            passed.add("'" + capitalize(sectionName) + "' section header found");
        } else {
            issues.add("No clear '" + capitalize(sectionName) + "' section header found");
        }
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
