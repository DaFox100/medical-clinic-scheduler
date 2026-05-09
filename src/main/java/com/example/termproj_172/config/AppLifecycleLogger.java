package com.example.termproj_172.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppLifecycleLogger {

    private static final Logger logger = LoggerFactory.getLogger(AppLifecycleLogger.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        logger.info("event=application_started message=\"Application startup complete\"");
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("event=application_shutdown message=\"Application is shutting down\"");
    }
}
