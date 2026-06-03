package com.example.carmarketanalyzer;

import java.time.Instant;

public record ScraperStatusResponse(
        boolean running,
        boolean scheduleEnabled,
        String scheduleMode,
        long intervalMinutes,
        String timeOfDay,
        String searchUrl,
        String lastSearchUrl,
        Instant nextRunAt,
        Instant lastStartedAt,
        Instant lastFinishedAt,
        String lastTrigger,
        String lastResult
) {
}
