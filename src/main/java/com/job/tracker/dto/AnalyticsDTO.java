package com.job.tracker.dto;

import java.util.Map;

public class AnalyticsDTO {

    public static class Response {
        private long totalApplications;
        private Map<String, Long> countByStatus;
        private double referralOfferRate;   // % of referred applications that became an OFFER
        private double nonReferralOfferRate; // % of cold applications that became an OFFER
        private long referredCount;
        private long nonReferredCount;

        public Response(long totalApplications, Map<String, Long> countByStatus,
                         double referralOfferRate, double nonReferralOfferRate,
                         long referredCount, long nonReferredCount) {
            this.totalApplications = totalApplications;
            this.countByStatus = countByStatus;
            this.referralOfferRate = referralOfferRate;
            this.nonReferralOfferRate = nonReferralOfferRate;
            this.referredCount = referredCount;
            this.nonReferredCount = nonReferredCount;
        }

        public long getTotalApplications() { return totalApplications; }
        public Map<String, Long> getCountByStatus() { return countByStatus; }
        public double getReferralOfferRate() { return referralOfferRate; }
        public double getNonReferralOfferRate() { return nonReferralOfferRate; }
        public long getReferredCount() { return referredCount; }
        public long getNonReferredCount() { return nonReferredCount; }
    }
}
