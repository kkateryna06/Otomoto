package com.example.carmarketanalyzer;

public record ScraperScheduleRequest(
        boolean enabled,
        Long intervalMinutes,
        String timeOfDay
) {
}
