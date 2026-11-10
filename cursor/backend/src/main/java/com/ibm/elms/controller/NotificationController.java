package com.ibm.elms.controller;

import com.ibm.elms.security.SecurityUtils;
import com.ibm.elms.service.NotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return notificationService.listForUser(SecurityUtils.currentUser().getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.unreadCount(SecurityUtils.currentUser().getId()));
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        notificationService.markRead(SecurityUtils.currentUser().getId(), id);
    }
}
