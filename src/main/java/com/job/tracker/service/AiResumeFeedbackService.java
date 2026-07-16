package com.job.tracker.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.job.tracker.dto.AiFeedbackDTO;
import com.job.tracker.entity.Job;
import com.job.tracker.entity.Resume;
import com.job.tracker.exception.ResourceNotFoundException;
import com.job.tracker.repository.JobRepository;

@Service
public class AiResumeFeedbackService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeTextExtractor textExtractor;

    @Autowired
    private GeminiClientService geminiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "aiFeedback", key = "#jobId + '-' + #resumeId + '-' + #userId")
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

        String resumeText = truncate(textExtractor.extractText(resume.getCvFile()), 6000);
        String jobDescription = truncate(job.getJobDescription() == null ? "" : job.getJobDescription(), 4000);

        String prompt = buildPrompt(job.getCompany(), job.getRoleName(), jobDescription, resumeText);

        String rawReply = geminiClient.completeJson(prompt, 2048);
        return parseResponse(rawReply);
    }

    private String truncate(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) : text;
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
            String cleaned = rawReply.replaceAll("```json|```", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);

            AiFeedbackDTO.Response response = new AiFeedbackDTO.Response();
            response.setOverallImpression(node.path("overallImpression").asText());
            response.setStrengths(toList(node.path("strengths")));
            response.setGaps(toList(node.path("gaps")));
            response.setSuggestions(toList(node.path("suggestions")));
            return response;
        } catch (Exception e) {
            boolean looksTruncated = !rawReply.trim().endsWith("}");
            AiFeedbackDTO.Response response = new AiFeedbackDTO.Response();
            response.setOverallImpression(looksTruncated
                    ? "AI response was cut off before finishing (hit the token limit) — showing partial output below."
                    : "Couldn't parse a structured response — showing raw AI output below.");
            response.setStrengths(List.of());
            response.setGaps(List.of());
            response.setSuggestions(List.of(rawReply));
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