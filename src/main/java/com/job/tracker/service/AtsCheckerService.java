package com.job.tracker.service;

import com.job.tracker.dto.AtsCheckDTO;
import com.job.tracker.entity.Resume;
import com.job.tracker.util.SkillKeywords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks a resume in isolation (no job description needed) for whether an
 * Applicant Tracking System could parse it cleanly — distinct from
 * MatchScoreService, which compares a resume against one specific job.
 */
@Service
public class AtsCheckerService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3,5}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{3,4}");

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeTextExtractor textExtractor;

    public AtsCheckDTO.Response checkResume(Long resumeId, Long userId) {
        Resume resume = resumeService.getRawResume(resumeId, userId);
        String text = textExtractor.extractText(resume.getCvFile());
        String lower = text.toLowerCase();

        List<String> passed = new ArrayList<>();
        List<String> issues = new ArrayList<>();

        // 1. File format check — PDF/DOCX parse reliably; other formats often don't
        String fileType = resume.getCvFileType() == null ? "" : resume.getCvFileType().toLowerCase();
        boolean safeFormat = fileType.contains("pdf") || fileType.contains("word")
                || fileType.contains("officedocument");
        if (safeFormat) {
            passed.add("File format (" + fileType + ") is ATS-friendly");
        } else {
            issues.add("File format '" + fileType + "' may not parse well in ATS systems — use PDF or DOCX");
        }

        // 2. Contact info — email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            passed.add("Email address detected");
        } else {
            issues.add("No email address detected — ATS may reject applications without a parseable email");
        }

        // 3. Contact info — phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
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

        // 8. Recognized skill keyword density
        Set<String> detectedSkills = SkillKeywords.DICTIONARY.stream()
                .filter(lower::contains)
                .collect(Collectors.toCollection(HashSet::new));
        if (detectedSkills.size() >= 5) {
            passed.add("Contains " + detectedSkills.size() + " recognizable technical skill keywords");
        } else {
            issues.add("Only " + detectedSkills.size() + " recognizable skill keywords found — ATS keyword matching may score this low");
        }

        int totalChecks = passed.size() + issues.size();
        long score = totalChecks == 0 ? 0 : Math.round((passed.size() * 100.0) / totalChecks);

        return new AtsCheckDTO.Response(score, passed, issues, detectedSkills, wordCount);
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
