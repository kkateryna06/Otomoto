package com.example.carmarketanalyzer;

import java.time.Instant;

public record ScraperStatusResponse(
        boolean running,
        boolean scheduleEnabled,
        String scheduleMode,
        long intervalMinutes,
        String timeOfDay,
        boolean recheckScheduleEnabled,
        String recheckScheduleMode,
        long recheckIntervalMinutes,
        String recheckTimeOfDay,
        String searchUrl,
        String lastSearchUrl,
        Instant nextRunAt,
        Instant recheckNextRunAt,
        Instant lastStartedAt,
        Instant lastFinishedAt,
        String lastTrigger,
        String lastResult
) {
}
