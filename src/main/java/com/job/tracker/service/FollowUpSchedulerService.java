package com.job.tracker.service;

import com.job.tracker.dto.JobDTO.JobListResponse;
import com.job.tracker.entity.User;
import com.job.tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * SMART FEATURE: FOLLOW-UP AUTOMATION.
 *
 * Runs once a day and scans every user's applications for ones that need a
 * follow-up nudge (stale 7+ days, or a deadline within 3 days). Today this
 * logs a summary per user; the same JobService.getJobsNeedingFollowUp() call
 * backs the on-demand /api/jobs/follow-ups endpoint the frontend polls, and
 * this is the natural place to plug in spring-boot-starter-mail later to send
 * an actual reminder email instead of just logging.
 */
@Service
public class FollowUpSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(FollowUpSchedulerService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobService jobService;

    // Runs every day at 8:00 AM server time
    @Scheduled(cron = "0 0 8 * * *")
    public void scanForFollowUps() {
        log.info("Running daily follow-up scan...");
        int usersWithFollowUps = 0;

        for (User user : userRepository.findAll()) {
            JobListResponse followUps = jobService.getJobsNeedingFollowUp(user.getId());
            if (followUps.getTotalCount() > 0) {
                usersWithFollowUps++;
                log.info("User {} ({}) has {} application(s) needing follow-up",
                        user.getId(), user.getEmail(), followUps.getTotalCount());
            }
        }

        log.info("Follow-up scan complete. {} user(s) have pending follow-ups.", usersWithFollowUps);
    }
}
