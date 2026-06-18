package com.job.tracker.dto;

import java.time.LocalDate;
import java.util.List;

public class JobDTO {

    public static class CreateJobRequest {
        private String company;
        private String roleName;
        private String jobUrl;
        private String jobDescription;
        private String jobType;
        private String location;
        private String salary;
        private String companySize;
        private LocalDate postedDate;
        private LocalDate deadline;
        private String status;
        private String notes;

        public CreateJobRequest() {
        }

        public CreateJobRequest(String company, String roleName, String jobUrl, String jobDescription,
                                 String jobType, String location, String salary, String companySize,
                                 LocalDate postedDate, LocalDate deadline, String status, String notes) {
            this.company = company;
            this.roleName = roleName;
            this.jobUrl = jobUrl;
            this.jobDescription = jobDescription;
            this.jobType = jobType;
            this.location = location;
            this.salary = salary;
            this.companySize = companySize;
            this.postedDate = postedDate;
            this.deadline = deadline;
            this.status = status;
            this.notes = notes;
        }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public String getJobUrl() { return jobUrl; }
        public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSalary() { return salary; }
        public void setSalary(String salary) { this.salary = salary; }

        public String getCompanySize() { return companySize; }
        public void setCompanySize(String companySize) { this.companySize = companySize; }

        public LocalDate getPostedDate() { return postedDate; }
        public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

        public LocalDate getDeadline() { return deadline; }
        public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class UpdateJobRequest {
        private String company;
        private String roleName;
        private String jobUrl;
        private String jobDescription;
        private String jobType;
        private String location;
        private String salary;
        private String companySize;
        private LocalDate deadline;
        private String status;
        private String notes;

        public UpdateJobRequest() {
        }

        public UpdateJobRequest(String company, String roleName, String jobUrl, String jobDescription,
                                 String jobType, String location, String salary, String companySize,
                                 LocalDate deadline, String status, String notes) {
            this.company = company;
            this.roleName = roleName;
            this.jobUrl = jobUrl;
            this.jobDescription = jobDescription;
            this.jobType = jobType;
            this.location = location;
            this.salary = salary;
            this.companySize = companySize;
            this.deadline = deadline;
            this.status = status;
            this.notes = notes;
        }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public String getJobUrl() { return jobUrl; }
        public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSalary() { return salary; }
        public void setSalary(String salary) { this.salary = salary; }

        public String getCompanySize() { return companySize; }
        public void setCompanySize(String companySize) { this.companySize = companySize; }

        public LocalDate getDeadline() { return deadline; }
        public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class JobResponse {
        private Long id;
        private String company;
        private String roleName;
        private String jobUrl;
        private String jobDescription;
        private String jobType;
        private String location;
        private String salary;
        private String companySize;
        private LocalDate postedDate;
        private LocalDate deadline;
        private String status;
        private String notes;
        private String createdAt;
        private String updatedAt;

        public JobResponse() {
        }

        public JobResponse(Long id, String company, String roleName, String jobUrl, String jobDescription,
                            String jobType, String location, String salary, String companySize,
                            LocalDate postedDate, LocalDate deadline, String status, String notes,
                            String createdAt, String updatedAt) {
            this.id = id;
            this.company = company;
            this.roleName = roleName;
            this.jobUrl = jobUrl;
            this.jobDescription = jobDescription;
            this.jobType = jobType;
            this.location = location;
            this.salary = salary;
            this.companySize = companySize;
            this.postedDate = postedDate;
            this.deadline = deadline;
            this.status = status;
            this.notes = notes;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public String getJobUrl() { return jobUrl; }
        public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSalary() { return salary; }
        public void setSalary(String salary) { this.salary = salary; }

        public String getCompanySize() { return companySize; }
        public void setCompanySize(String companySize) { this.companySize = companySize; }

        public LocalDate getPostedDate() { return postedDate; }
        public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

        public LocalDate getDeadline() { return deadline; }
        public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class JobListResponse {
        private List<JobResponse> data;
        private long totalCount;

        public JobListResponse() {
        }

        public JobListResponse(List<JobResponse> data, long totalCount) {
            this.data = data;
            this.totalCount = totalCount;
        }

        public List<JobResponse> getData() { return data; }
        public void setData(List<JobResponse> data) { this.data = data; }

        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    }
}
