package org.example.kidsmathapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kidsmathapp.entity.Child;
import org.example.kidsmathapp.entity.Progress;
import org.example.kidsmathapp.entity.User;
import org.example.kidsmathapp.repository.ChildRepository;
import org.example.kidsmathapp.repository.ProgressRepository;
import org.example.kidsmathapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentReportEmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final ProgressRepository progressRepository;

    @Value("${spring.mail.from:noreply@kidsmathapp.com}")
    private String fromEmail;

    /**
     * Send weekly Sunday digest for all parents who have active children.
     * Called by POST /api/v1/internal/jobs/weekly-email
     */
    @Transactional(readOnly = true)
    public void sendWeeklyDigests() {
        List<User> parents = userRepository.findAll();
        int sent = 0, failed = 0;
        for (User parent : parents) {
            try {
                sendDigestForParent(parent);
                sent++;
            } catch (Exception e) {
                // Never crash the batch — log and continue
                log.error("Failed to send email to {}: {}", parent.getEmail(), e.getMessage());
                failed++;
            }
        }
        log.info("Weekly digest: {} sent, {} failed", sent, failed);
    }

    private void sendDigestForParent(User parent) {
        List<Child> children = childRepository.findByParentId(parent.getId());
        if (children.isEmpty()) return;

        StringBuilder body = new StringBuilder();
        body.append("Weekly Progress Report\n\n");

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        for (Child child : children) {
            List<Progress> weekProgress = progressRepository.findByChildIdAndCompletedAtAfter(child.getId(), weekAgo);
            int daysActive = (int) weekProgress.stream()
                    .filter(p -> p.getCompletedAt() != null)
                    .map(p -> p.getCompletedAt().toLocalDate())
                    .distinct().count();
            int totalMinutes = weekProgress.stream()
                    .mapToInt(p -> p.getTimeSpentSeconds() != null ? p.getTimeSpentSeconds() / 60 : 0)
                    .sum();

            body.append("--- ").append(child.getName()).append(" ---\n");
            body.append("Days active: ").append(daysActive).append("/7\n");
            body.append("Minutes practiced: ").append(totalMinutes).append("\n");

            if (weekProgress.isEmpty()) {
                body.append(child.getName())
                    .append(" hasn't practiced this week. Even 5 minutes helps!\n");
            }
            body.append("\n");
        }

        body.append("View full report at: https://kidsmathapp.app/parent\n");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(parent.getEmail());
        msg.setSubject("Weekly Progress Report - " + children.get(0).getName());
        msg.setText(body.toString());
        mailSender.send(msg);
    }
}
