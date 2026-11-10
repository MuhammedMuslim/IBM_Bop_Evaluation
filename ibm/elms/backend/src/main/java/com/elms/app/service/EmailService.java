package com.elms.app.service;

import com.elms.app.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendLeaveSubmittedToManager(User manager, User employee, LeaveRequest request) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(manager.getEmail());
            msg.setSubject("[ELMS] Leave Request Pending Review – " + employee.getFullName());
            msg.setText(String.format("""
                    Dear %s,
                    
                    %s has submitted a %s leave request from %s to %s (%.1f working days).
                    
                    Reason: %s
                    
                    Please log in to ELMS to review this request.
                    
                    Regards,
                    ELMS System
                    """,
                    manager.getFullName(),
                    employee.getFullName(),
                    request.getLeaveType().getName(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getWorkingDays(),
                    request.getReason() != null ? request.getReason() : "N/A"));
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send manager notification email: {}", e.getMessage());
        }
    }

    @Async
    public void sendLeaveDecisionToEmployee(User employee, LeaveRequest request) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(employee.getEmail());
            msg.setSubject("[ELMS] Leave Request " + request.getStatus().name() + " – " + request.getLeaveType().getName());
            msg.setText(String.format("""
                    Dear %s,
                    
                    Your %s leave request from %s to %s has been %s.
                    
                    Manager Comment: %s
                    
                    Regards,
                    ELMS System
                    """,
                    employee.getFullName(),
                    request.getLeaveType().getName(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getStatus().name().toLowerCase(),
                    request.getManagerComment() != null ? request.getManagerComment() : "N/A"));
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send employee notification email: {}", e.getMessage());
        }
    }
}
