package com.job.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.job.tracker.dto.AiFeedbackDTO;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.Resume;
import com.job.tracker.exception.ResourceNotFoundException;
import com.job.tracker.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SMART FEATURE: AI RESUME FEEDBACK.
 *
 * Unlike MatchScoreService (plain keyword overlap), this asks an LLM to
 * actually reason about fit: what's genuinely strong, what's missing, and
 * concrete rewording suggestions — the kind of qualitative judgment a
 * keyword whitelist can't produce.
 */
@Service
public class AiResumeFeedbackService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private  ResumeTextExtractor textExtractor;

    @Autowired
    private GeminiClientService geminiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiFeedbackDTO.Response getFeedback(Long jobId, Long resumeId, Long userId) {

        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Resume resume = resumeId != null
                ? resumeService.getRawResume(resumeId, userId)
                : job.getResume();

        if (resume == null) {
            throw new ResourceNotFoundException(
                    "No resume linked to this job — pass ?resumeId= or attach a resume to the job first");
        }

        String resumeText = textExtractor.extractText(resume.getCvFile());
        String jobDescription = job.getJobDescription() == null ? "" : job.getJobDescription();

        String prompt = buildPrompt(job.getCompany(), job.getRoleName(), jobDescription, resumeText);

        String rawReply = geminiClient.complete(prompt, 800);
        return parseResponse(rawReply);
    }

    private String buildPrompt(String company, String roleName, String jobDescription, String resumeText) {
        return """
                You are helping a job applicant improve their resume for a specific role.

                Company: %s
                Role: %s

                Job description:
                %s

                Resume text:
                %s

                Respond with ONLY a JSON object (no markdown, no code fences, no preamble) in exactly this shape:
                {
                  "overallImpression": "2-3 sentence honest assessment of fit for this specific role",
                  "strengths": ["specific strength tied to the JD", "..."],
                  "gaps": ["specific missing skill or experience relevant to this JD", "..."],
                  "suggestions": ["concrete, actionable rewording or addition the applicant can make", "..."]
                }

                Keep each list to 3-5 items. Be specific to this JD and this resume, not generic advice.
                """.formatted(company, roleName, jobDescription, resumeText);
    }

    private AiFeedbackDTO.Response parseResponse(String rawReply) {
        try {
            // Gemini sometimes wraps JSON in code fences despite instructions — strip them defensively
            String cleaned = rawReply.replaceAll("```json|```", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);

            AiFeedbackDTO.Response response = new AiFeedbackDTO.Response();
            response.setOverallImpression(node.path("overallImpression").asText());
            response.setStrengths(toList(node.path("strengths")));
            response.setGaps(toList(node.path("gaps")));
            response.setSuggestions(toList(node.path("suggestions")));
            return response;
        } catch (Exception e) {
            // Fallback: surface the raw text rather than failing the whole request
            AiFeedbackDTO.Response response = new AiFeedbackDTO.Response();
            response.setOverallImpression(rawReply);
            response.setStrengths(List.of());
            response.setGaps(List.of());
            response.setSuggestions(List.of());
            return response;
        }
    }

    private List<String> toList(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode.isArray()) {
            arrayNode.forEach(n -> result.add(n.asText()));
        }
        return result;
    }
}
