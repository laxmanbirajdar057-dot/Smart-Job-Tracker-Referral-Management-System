package com.job.tracker.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.job.tracker.dto.ResumeDTO.ResumeResponse;
import com.job.tracker.entity.Resume;
import com.job.tracker.entity.User;
import com.job.tracker.repository.ResumeRepository;
import com.job.tracker.repository.UserRepository;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    public ResumeResponse uploadResume(Long userId, MultipartFile file,
                                       String label, String targetRole, String version) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setLabel(label);
        resume.setTargetRole(targetRole);
        resume.setVersion(version);
        resume.setCvFile(file.getBytes());
        resume.setCvFileName(file.getOriginalFilename());
        resume.setCvFileType(file.getContentType());

        resume = resumeRepository.save(resume);
        return toResponse(resume);
    }

    public List<ResumeResponse> getAllResumes(Long userId) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Resume getRawResume(Long id, Long userId) {
        return resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
    }

    public void deleteResume(Long id, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        resumeRepository.delete(resume);
    }

    private ResumeResponse toResponse(Resume resume) {
        ResumeResponse r = new ResumeResponse();
        r.setId(resume.getId());
        r.setLabel(resume.getLabel());
        r.setTargetRole(resume.getTargetRole());
        r.setVersion(resume.getVersion());
        r.setCvFileName(resume.getCvFileName());
        r.setCvFileType(resume.getCvFileType());
        r.setUploadedAt(resume.getUploadedAt());
        return r;
    }
}