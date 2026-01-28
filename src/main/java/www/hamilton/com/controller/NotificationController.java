package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import www.hamilton.com.entity.Notification;
import www.hamilton.com.entity.User;
import www.hamilton.com.repository.NotificationRepository;
import www.hamilton.com.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Bildiriş idarəetmə API-ləri")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Mənim bildirişlərim", description = "Cari istifadəçinin bildirişlərini siyahıya almaq")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<Notification>> myNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(notificationRepository.findByUserOrderByCreatedAtDesc(user));
    }

    @Operation(summary = "Oxunmamış bildiriş sayı", description = "Cari istifadəçinin oxunmamış bildirişlərinin sayını almaq")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        long count = notificationRepository.countByUserAndIsRead(user, false);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Bütün bildirişləri oxunmuş kimi işarələ", description = "Cari istifadəçinin bütün bildirişlərini oxunmuş kimi işarələmək")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Notification> list = notificationRepository.findByUserAndIsRead(user, false);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
        return ResponseEntity.ok().build();
    }
}


