package com.job.tracker.dto;

import java.util.List;
import java.util.Set;

public class AtsCheckDTO {

    public static class Response {
        private long score;
        private List<String> passedChecks;
        private List<String> issues;
        private Set<String> detectedSkills;
        private Set<String> missingRoleKeywords;
        private int wordCount;

        public Response(long score, List<String> passedChecks, List<String> issues,
                         Set<String> detectedSkills, Set<String> missingRoleKeywords, int wordCount) {
            this.score = score;
            this.passedChecks = passedChecks;
            this.issues = issues;
            this.detectedSkills = detectedSkills;
            this.missingRoleKeywords = missingRoleKeywords;
            this.wordCount = wordCount;
        }

        public long getScore() { return score; }
        public List<String> getPassedChecks() { return passedChecks; }
        public List<String> getIssues() { return issues; }
        public Set<String> getDetectedSkills() { return detectedSkills; }
        public Set<String> getMissingRoleKeywords() { return missingRoleKeywords; }
        public int getWordCount() { return wordCount; }
    }
}
