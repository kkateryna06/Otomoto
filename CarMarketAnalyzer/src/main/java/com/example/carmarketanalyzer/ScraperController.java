package com.example.carmarketanalyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final ScraperService scraperService;

    @GetMapping("/scrape")
    public String startScraping() {
        boolean started = scraperService.startScraping();

        if (!started) {
            return "Scraper is already running.";
        }

        return "Scraping started in background.";
    }

    @GetMapping("/status")
    public ScraperStatusResponse getStatus() {
        return scraperService.getStatus();
    }

    @GetMapping("/settings")
    public ScraperSettingsResponse getSettings() {
        return scraperService.getSettings();
    }

    @PutMapping("/settings")
    public ScraperSettingsResponse updateSettings(@RequestBody ScraperSettingsRequest request) {
        try {
            return scraperService.updateSettings(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/schedule")
    public ScraperStatusResponse configureSchedule(@RequestBody ScraperScheduleRequest request) {
        try {
            return scraperService.configureSchedule(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
