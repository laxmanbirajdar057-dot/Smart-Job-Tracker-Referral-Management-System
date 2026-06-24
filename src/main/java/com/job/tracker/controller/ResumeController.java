package com.job.tracker.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.job.tracker.dto.ResumeDTO.ResumeResponse;
import com.job.tracker.entity.Resume;
import com.job.tracker.security.JwtTokenProvider;
import com.job.tracker.service.ResumeService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // POST /api/resumes/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("label") String label,
            @RequestParam(value = "targetRole", required = false) String targetRole,
            @RequestParam(value = "version", required = false) String version,
            HttpServletRequest request) throws IOException {

        Long userId = getUserId(request);
        return ResponseEntity.ok(resumeService.uploadResume(userId, 
                                                            file, 
                                                            label, 
                                                            targetRole, 
                                                            version
                                                        ));
    }

    // GET /api/resumes
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAll(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(resumeService.getAllResumes(userId));
    }

    // GET /api/resumes/{id}/download
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        Resume resume = resumeService.getRawResume(id, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getCvFileName() + "\"")
                .contentType(MediaType.parseMediaType(resume.getCvFileType()))
                .body(resume.getCvFile());
    }

    // DELETE /api/resumes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        resumeService.deleteResume(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}