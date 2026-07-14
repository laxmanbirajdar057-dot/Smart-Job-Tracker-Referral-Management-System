package com.job.tracker.dto;

import java.util.Set;

public class MatchScoreDTO {

    public static class Response {
        private long score;
        private Set<String> matchedSkills;
        private Set<String> missingSkills;
        private Long resumeIdUsed;

        public Response(long score, Set<String> matchedSkills, Set<String> missingSkills, Long resumeIdUsed) {
            this.score = score;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.resumeIdUsed = resumeIdUsed;
        }

        public long getScore() { return score; }
        public Set<String> getMatchedSkills() { return matchedSkills; }
        public Set<String> getMissingSkills() { return missingSkills; }
        public Long getResumeIdUsed() { return resumeIdUsed; }
    }
}
