package com.job.tracker.dto;

import java.time.LocalDate;

public class ResumeDTO {

    public static class ResumeResponse {
        private Long id;
        private String label;
        private String targetRole;
        private String version;
        private String cvFileName;
        private String cvFileType;
        private LocalDate uploadedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getTargetRole() { return targetRole; }
        public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getCvFileName() { return cvFileName; }
        public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }

        public String getCvFileType() { return cvFileType; }
        public void setCvFileType(String cvFileType) { this.cvFileType = cvFileType; }

        public LocalDate getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDate uploadedAt) { this.uploadedAt = uploadedAt; }
    }
}