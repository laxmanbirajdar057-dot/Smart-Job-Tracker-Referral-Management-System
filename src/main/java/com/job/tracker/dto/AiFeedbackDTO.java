package com.job.tracker.dto;

import java.util.List;

public class AiFeedbackDTO {

    public static class Response {
        private String overallImpression;
        private List<String> strengths;
        private List<String> gaps;
        private List<String> suggestions;

        public Response() {
        }

        public Response(String overallImpression, List<String> strengths,
                         List<String> gaps, List<String> suggestions) {
            this.overallImpression = overallImpression;
            this.strengths = strengths;
            this.gaps = gaps;
            this.suggestions = suggestions;
        }

        public String getOverallImpression() { return overallImpression; }
        public void setOverallImpression(String overallImpression) { this.overallImpression = overallImpression; }

        public List<String> getStrengths() { return strengths; }
        public void setStrengths(List<String> strengths) { this.strengths = strengths; }

        public List<String> getGaps() { return gaps; }
        public void setGaps(List<String> gaps) { this.gaps = gaps; }

        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    }
}
