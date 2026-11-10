package com.ibm.elms.service;

import com.ibm.elms.domain.entity.Notification;
import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.NotificationRepository;
import com.ibm.elms.repository.UserAccountRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String mailFrom;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserAccountRepository userAccountRepository,
            JavaMailSender mailSender,
            @Value("${elms.mail.enabled:false}") boolean mailEnabled,
            @Value("${elms.mail.from:noreply@elms.local}") String mailFrom) {
        this.notificationRepository = notificationRepository;
        this.userAccountRepository = userAccountRepository;
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.mailFrom = mailFrom;
    }

    @Transactional
    public void notifyUser(Long userId, String title, String message) {
        UserAccount user = userAccountRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);

        if (mailEnabled) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(mailFrom);
                mail.setTo(user.getEmail());
                mail.setSubject(title);
                mail.setText(message);
                mailSender.send(mail);
            } catch (Exception ex) {
                log.warn("Failed to send email to {}: {}", user.getEmail(), ex.getMessage());
            }
        } else {
            log.info("Notification to {}: {} - {}", user.getEmail(), title, message);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> Map.<String, Object>of(
                        "id", n.getId(),
                        "title", n.getTitle(),
                        "message", n.getMessage(),
                        "read", n.isReadFlag(),
                        "createdAt", n.getCreatedAt().toString()))
                .toList();
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ApiException("Notification not found", HttpStatus.NOT_FOUND));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
        notification.setReadFlag(true);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }
}
