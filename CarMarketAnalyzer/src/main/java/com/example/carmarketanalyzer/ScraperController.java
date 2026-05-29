package com.example.carmarketanalyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final OtoMotoScraper otoMotoScraper;

    @GetMapping("/scrape")
    public String startScraping() {
        log.info("Scraping started");
        new Thread(() -> {
            try {
                otoMotoScraper.scrapeAndSave();
            } catch (Exception e) {
                log.error("Scraping error", e);
            }
        }).start();
        return "Scraping started in background. Check logs for progress.";
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Scraper is running";
    }
}
