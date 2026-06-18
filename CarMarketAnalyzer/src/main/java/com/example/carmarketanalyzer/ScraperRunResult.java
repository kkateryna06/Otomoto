package com.example.carmarketanalyzer;

public record ScraperRunResult(
        int found,
        int saved,
        int errors
) {
    public static ScraperRunResult empty() {
        return new ScraperRunResult(0, 0, 0);
    }

    public ScraperRunResult plus(ScraperRunResult other) {
        return new ScraperRunResult(
                found + other.found(),
                saved + other.saved(),
                errors + other.errors()
        );
    }

    public ScraperRunResult withError() {
        return new ScraperRunResult(found, saved, errors + 1);
    }

    public String summary() {
        return "Completed: found %d, saved %d, errors %d".formatted(found, saved, errors);
    }
}
