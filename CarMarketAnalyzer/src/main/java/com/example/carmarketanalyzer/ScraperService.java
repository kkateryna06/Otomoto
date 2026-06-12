package com.example.carmarketanalyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private static final Duration DEFAULT_INTERVAL = Duration.ofHours(24);
    private static final String DEFAULT_SEARCH_URL = "https://www.otomoto.pl/osobowe";
    private static final String SCHEDULE_MODE_DISABLED = "disabled";
    private static final String SCHEDULE_MODE_INTERVAL = "interval";
    private static final String SCHEDULE_MODE_DAILY = "daily";
    private static final int DEFAULT_MAX_RECHECK_LISTINGS_PER_RUN = 100;
    private static final int MAX_RECHECK_LISTINGS_PER_RUN_LIMIT = 1000;

    private final OtoMotoScraper otoMotoScraper;
    private final TaskScheduler scraperTaskScheduler;
    private final TaskExecutor scraperTaskExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object scheduleLock = new Object();

    private volatile ScheduledFuture<?> scheduledTask;
    private volatile boolean scheduleEnabled;
    private volatile String scheduleMode = SCHEDULE_MODE_DISABLED;
    private volatile Duration interval = DEFAULT_INTERVAL;
    private volatile LocalTime timeOfDay;
    private volatile Instant nextRunAt;
    private volatile ScheduledFuture<?> recheckScheduledTask;
    private volatile boolean recheckScheduleEnabled;
    private volatile String recheckScheduleMode = SCHEDULE_MODE_DISABLED;
    private volatile Duration recheckInterval = DEFAULT_INTERVAL;
    private volatile LocalTime recheckTimeOfDay;
    private volatile Instant recheckNextRunAt;
    private volatile Instant lastStartedAt;
    private volatile Instant lastFinishedAt;
    private volatile String lastTrigger;
    private volatile String searchUrl = DEFAULT_SEARCH_URL;
    private volatile int maxRecheckListingsPerRun = DEFAULT_MAX_RECHECK_LISTINGS_PER_RUN;
    private volatile String lastSearchUrl;
    private volatile String lastResult = "Never run";

    public boolean startScraping() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }

        scraperTaskExecutor.execute(() -> runScraper("manual"));
        return true;
    }

    public boolean startRechecking() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }

        scraperTaskExecutor.execute(() -> runRecheck("manual-recheck"));
        return true;
    }

    public ScraperStatusResponse configureSchedule(ScraperScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("schedule request is required");
        }

        synchronized (scheduleLock) {
            cancelScheduledTask();
            scheduleEnabled = request.enabled();

            if (!request.enabled()) {
                scheduleMode = SCHEDULE_MODE_DISABLED;
                nextRunAt = null;
                return getStatus();
            }

            if (hasText(request.timeOfDay())) {
                timeOfDay = parseTimeOfDay(request.timeOfDay());
                scheduleMode = SCHEDULE_MODE_DAILY;
                scheduleNextDailyRun();
            } else {
                long intervalMinutes = parseIntervalMinutes(request.intervalMinutes());
                interval = Duration.ofMinutes(intervalMinutes);
                timeOfDay = null;
                scheduleMode = SCHEDULE_MODE_INTERVAL;
                nextRunAt = Instant.now().plus(interval);
                scheduledTask = scraperTaskScheduler.scheduleAtFixedRate(
                        this::startScheduledScraping,
                        nextRunAt,
                        interval
                );
            }

            return getStatus();
        }
    }

    public ScraperStatusResponse configureRecheckSchedule(ScraperScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("recheck schedule request is required");
        }

        synchronized (scheduleLock) {
            cancelRecheckScheduledTask();
            recheckScheduleEnabled = request.enabled();

            if (!request.enabled()) {
                recheckScheduleMode = SCHEDULE_MODE_DISABLED;
                recheckNextRunAt = null;
                return getStatus();
            }

            if (hasText(request.timeOfDay())) {
                recheckTimeOfDay = parseTimeOfDay(request.timeOfDay());
                recheckScheduleMode = SCHEDULE_MODE_DAILY;
                scheduleNextDailyRecheckRun();
            } else {
                long intervalMinutes = parseIntervalMinutes(request.intervalMinutes());
                recheckInterval = Duration.ofMinutes(intervalMinutes);
                recheckTimeOfDay = null;
                recheckScheduleMode = SCHEDULE_MODE_INTERVAL;
                recheckNextRunAt = Instant.now().plus(recheckInterval);
                recheckScheduledTask = scraperTaskScheduler.scheduleAtFixedRate(
                        this::startScheduledRecheck,
                        recheckNextRunAt,
                        recheckInterval
                );
            }

            return getStatus();
        }
    }

    public ScraperSettingsResponse getSettings() {
        return new ScraperSettingsResponse(searchUrl, maxRecheckListingsPerRun);
    }

    public ScraperSettingsResponse updateSettings(ScraperSettingsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("settings request is required");
        }

        String normalizedSearchUrl = normalizeSearchUrl(request.searchUrl());
        searchUrl = normalizedSearchUrl;
        if (request.maxRecheckListingsPerRun() != null) {
            maxRecheckListingsPerRun = parseMaxRecheckListingsPerRun(request.maxRecheckListingsPerRun());
        }
        return getSettings();
    }

    public ScraperStatusResponse getStatus() {
        return new ScraperStatusResponse(
                running.get(),
                scheduleEnabled,
                scheduleMode,
                interval.toMinutes(),
                timeOfDay == null ? null : timeOfDay.toString(),
                recheckScheduleEnabled,
                recheckScheduleMode,
                recheckInterval.toMinutes(),
                recheckTimeOfDay == null ? null : recheckTimeOfDay.toString(),
                searchUrl,
                lastSearchUrl,
                nextRunAt,
                recheckNextRunAt,
                lastStartedAt,
                lastFinishedAt,
                lastTrigger,
                lastResult
        );
    }

    private void startScheduledScraping() {
        if (!running.compareAndSet(false, true)) {
            log.info("Scheduled scraping skipped because scraper is already running");
            updateNextIntervalRunAt();
            return;
        }

        runScraper("scheduled");
    }

    private void startScheduledRecheck() {
        if (!running.compareAndSet(false, true)) {
            log.info("Scheduled recheck skipped because scraper is already running");
            updateNextRecheckIntervalRunAt();
            return;
        }

        runRecheck("scheduled-recheck");
    }

    private void startDailyScheduledScraping() {
        startScheduledScraping();

        synchronized (scheduleLock) {
            if (scheduleEnabled && SCHEDULE_MODE_DAILY.equals(scheduleMode)) {
                scheduleNextDailyRun();
            }
        }
    }

    private void startDailyScheduledRecheck() {
        startScheduledRecheck();

        synchronized (scheduleLock) {
            if (recheckScheduleEnabled && SCHEDULE_MODE_DAILY.equals(recheckScheduleMode)) {
                scheduleNextDailyRecheckRun();
            }
        }
    }

    private void runScraper(String trigger) {
        String currentSearchUrl = searchUrl;
        lastSearchUrl = currentSearchUrl;
        lastTrigger = trigger;
        lastStartedAt = Instant.now();
        lastResult = "Running";
        updateNextIntervalRunAt();

        try {
            log.info("Scraping started by {} with URL {}", trigger, currentSearchUrl);
            otoMotoScraper.scrapeAndSave(currentSearchUrl, maxRecheckListingsPerRun);
            lastResult = "Completed";
        } catch (Exception e) {
            lastResult = "Failed: " + e.getMessage();
            log.error("Scraping failed", e);
        } finally {
            lastFinishedAt = Instant.now();
            running.set(false);
        }
    }

    private void runRecheck(String trigger) {
        lastTrigger = trigger;
        lastStartedAt = Instant.now();
        lastResult = "Running";
        updateNextRecheckIntervalRunAt();

        try {
            log.info("Listing recheck started by {}", trigger);
            otoMotoScraper.recheckExistingListings(maxRecheckListingsPerRun);
            lastResult = "Completed";
        } catch (Exception e) {
            lastResult = "Failed: " + e.getMessage();
            log.error("Listing recheck failed", e);
        } finally {
            lastFinishedAt = Instant.now();
            running.set(false);
        }
    }

    private long parseIntervalMinutes(Long intervalMinutes) {
        if (intervalMinutes == null || intervalMinutes < 1) {
            throw new IllegalArgumentException("intervalMinutes must be at least 1 when schedule is enabled");
        }

        return intervalMinutes;
    }

    private int parseMaxRecheckListingsPerRun(Integer value) {
        if (value == null || value < 1 || value > MAX_RECHECK_LISTINGS_PER_RUN_LIMIT) {
            throw new IllegalArgumentException("maxRecheckListingsPerRun must be between 1 and " + MAX_RECHECK_LISTINGS_PER_RUN_LIMIT);
        }

        return value;
    }

    private LocalTime parseTimeOfDay(String rawTimeOfDay) {
        try {
            return LocalTime.parse(rawTimeOfDay.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("timeOfDay must use HH:mm format, for example 03:00", e);
        }
    }

    private void scheduleNextDailyRun() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextRun = now.with(timeOfDay);

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1);
        }

        nextRunAt = nextRun.toInstant();
        scheduledTask = scraperTaskScheduler.schedule(this::startDailyScheduledScraping, nextRunAt);
    }

    private void scheduleNextDailyRecheckRun() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextRun = now.with(recheckTimeOfDay);

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1);
        }

        recheckNextRunAt = nextRun.toInstant();
        recheckScheduledTask = scraperTaskScheduler.schedule(this::startDailyScheduledRecheck, recheckNextRunAt);
    }

    private String normalizeSearchUrl(String rawSearchUrl) {
        if (rawSearchUrl == null || rawSearchUrl.isBlank()) {
            throw new IllegalArgumentException("searchUrl is required");
        }

        try {
            URI uri = new URI(rawSearchUrl.trim()).normalize();
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (!"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("searchUrl must use https");
            }

            String normalizedHost = host == null ? "" : host.toLowerCase(Locale.ROOT);
            if (!"otomoto.pl".equals(normalizedHost) && !normalizedHost.endsWith(".otomoto.pl")) {
                throw new IllegalArgumentException("searchUrl must point to otomoto.pl");
            }

            if (uri.getPath() == null || !uri.getPath().startsWith("/osobowe")) {
                throw new IllegalArgumentException("searchUrl path must start with /osobowe");
            }

            return uri.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("searchUrl must be a valid URL", e);
        }
    }

    private void updateNextIntervalRunAt() {
        synchronized (scheduleLock) {
            if (scheduleEnabled && SCHEDULE_MODE_INTERVAL.equals(scheduleMode)) {
                nextRunAt = Instant.now().plus(interval);
            }
        }
    }

    private void updateNextRecheckIntervalRunAt() {
        synchronized (scheduleLock) {
            if (recheckScheduleEnabled && SCHEDULE_MODE_INTERVAL.equals(recheckScheduleMode)) {
                recheckNextRunAt = Instant.now().plus(recheckInterval);
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void cancelScheduledTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    private void cancelRecheckScheduledTask() {
        if (recheckScheduledTask != null) {
            recheckScheduledTask.cancel(false);
            recheckScheduledTask = null;
        }
    }
}
