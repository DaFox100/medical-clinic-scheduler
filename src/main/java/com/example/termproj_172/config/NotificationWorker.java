package com.example.termproj_172.config;

import com.example.termproj_172.services.NotificationDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.notification.worker-enabled", havingValue = "true", matchIfMissing = true)
public class NotificationWorker {
    private final NotificationDispatcher dispatcher;
    public NotificationWorker(NotificationDispatcher dispatcher) { this.dispatcher = dispatcher; }

    @Scheduled(fixedDelayString = "${app.notification.poll-delay-ms:2000}")
    public void deliverPending() {
        for (int i = 0; i < 20 && dispatcher.dispatchOne(); i++) {
            // Bound each poll so shutdown and other scheduled tasks can proceed.
        }
    }
}
