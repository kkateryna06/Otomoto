# CarMarketAnalyzer

Spring Boot application for collecting car listings from `otomoto.pl`, storing them in PostgreSQL, and exposing the data through a REST API.

## Features

- Starts scraping from `https://www.otomoto.pl/osobowe`.
- Loads listing pages with jsoup.
- Extracts basic car data: brand, model, year, mileage, fuel type, gearbox, body type, price, description, URL, and location.
- Saves records to PostgreSQL in the `cars_info` table.
- Updates existing listings by URL instead of inserting duplicates.
- Tracks price changes in JSON price history.
- Rechecks known listings by direct URL and marks disappeared listings as not actual.
- Rechecks listings in bounded batches ordered by oldest check time.
- Provides REST endpoints for starting the scraper and managing cars.
- Provides a browser UI for scraper settings, manual runs, rechecks, separate schedules, and status.
- Allows changing the scraper search URL through the API.
- Supports configurable scheduled scraping through the scraper API.
- Continues processing when a single listing fails.

## Tech Stack

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- jsoup 1.22.1
- Lombok
- Gradle

## Project Structure

```text
src/main/java/com/example/carmarketanalyzer/
  CarMarketAnalyzerApplication.java  # Spring Boot entry point
  CarController.java                 # REST API for cars
  ScraperController.java             # REST API for scraper control
  ScraperService.java                # Manual and scheduled scraper execution
  OtoMotoScraper.java                # Main scraping logic
  CarRepository.java                 # Spring Data JPA repository
  data/Car.java                      # JPA entity

src/main/resources/
  application.properties             # Database, JPA, and logging settings
  static/index.html                  # Browser UI for scraper control
```

## Requirements

- Java 21+
- PostgreSQL 12+
- The included Gradle wrapper, or a local Gradle installation
- Network access to `otomoto.pl`

## Database Setup

Create the database:

```sql
CREATE DATABASE otomoto;
```

The application reads database settings from `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/otomoto}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
```

Set the database password through an environment variable before running the app:

```powershell
$env:DB_PASSWORD="your-local-password"
```

On macOS or Linux:

```bash
export DB_PASSWORD="your-local-password"
```

## Run

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew bootRun
```

The application runs at:

```text
http://localhost:8080
```

## Scraper UI

Open:

```text
http://localhost:8080/
```

This is the recommended way to control the parser locally. You do not need to use PowerShell commands for scraper settings after the application is running.

The UI allows you to:

- edit and save the scraper search URL;
- set the maximum number of listings checked by one recheck run;
- start a manual scrape;
- start a listing recheck without scraping the search page;
- enable interval or daily scheduled scraping;
- enable interval or daily scheduled rechecks;
- disable either schedule;
- see scraper status, next scrape run, next recheck run, last result, and stored car count.

Typical workflow:

1. Start the application with `.\gradlew bootRun`.
2. Open `http://localhost:8080/` in a browser.
3. Paste an Otomoto search URL into the `Search URL` field.
4. Set `Max recheck listings per run`.
5. Click `Save URL`.
6. Click `Run Scrape` to collect/update listings.
7. Click `Recheck Listings` when you only want to verify existing stored listings and update prices/actuality.
8. Use `Scrape Schedule` to configure automatic full scraper runs.
9. Use `Recheck Schedule` to configure automatic direct URL checks for existing listings.

Note: scraper settings are stored in application memory. After an application restart, the search URL returns to the default unless you save it again from the UI or change the default in code.

## REST API

### Scraper

```http
GET /api/scraper/scrape
```

Starts the scraper in a background thread.

During a scraper run the application:

- collects listings from the configured search page;
- inserts new listings;
- updates existing listings found by the same `url`;
- appends a new price entry only when the current price differs from the latest stored price;
- rechecks known active listings that were not found on the current search page.

```http
GET /api/scraper/recheck
```

Starts a background recheck of already stored active listings without scraping the search page. This is useful for updating prices and detecting disappeared listings on demand.

```http
GET /api/scraper/status
```

Returns scraper status, including whether a run is active, current search URL, scrape schedule state, recheck schedule state, next run times, and last run timestamps.

```http
GET /api/scraper/settings
```

Returns current scraper settings.

```http
PUT /api/scraper/settings
Content-Type: application/json

{
  "searchUrl": "https://www.otomoto.pl/osobowe/bmw/od-2006?search%5Bfilter_enum_fuel_type%5D=petrol&search%5Bfilter_float_price%3Ato%5D=15000",
  "maxRecheckListingsPerRun": 100
}
```

Changes parser settings. The search URL is used by both manual and scheduled scraper runs. The URL must use `https`, point to `otomoto.pl`, and start with `/osobowe`. `maxRecheckListingsPerRun` controls how many stored active listings one recheck job may process.

```http
POST /api/scraper/schedule
Content-Type: application/json

{
  "enabled": true,
  "intervalMinutes": 60
}
```

Enables scheduled scraping every 60 minutes. The first scheduled run starts after the configured interval.

```http
POST /api/scraper/schedule
Content-Type: application/json

{
  "enabled": true,
  "timeOfDay": "03:00"
}
```

Enables scheduled scraping once per day at 03:00 in the server's local time zone. The first scheduled run starts at the next matching time.

```http
POST /api/scraper/schedule
Content-Type: application/json

{
  "enabled": false
}
```

Disables scheduled scraping.

```http
POST /api/scraper/recheck/schedule
Content-Type: application/json

{
  "enabled": true,
  "intervalMinutes": 180
}
```

Enables scheduled listing rechecks every 180 minutes. This updates prices and listing actuality without scraping the search page. Each run processes only up to `maxRecheckListingsPerRun` listings.

```http
POST /api/scraper/recheck/schedule
Content-Type: application/json

{
  "enabled": true,
  "timeOfDay": "06:00"
}
```

Enables scheduled listing rechecks once per day at 06:00 in the server's local time zone.

```http
POST /api/scraper/recheck/schedule
Content-Type: application/json

{
  "enabled": false
}
```

Disables scheduled listing rechecks.

### Cars

```http
GET    /api/cars
GET    /api/cars?page=0&size=20
GET    /api/cars/{id}
GET    /api/cars/count
GET    /api/cars/brands
GET    /api/cars/models?brand=Audi
GET    /api/cars/fuel-types
GET    /api/cars/body-types
GET    /api/cars/gearboxes
GET    /api/cars/transmissions
POST   /api/cars
DELETE /api/cars/{id}
```

`GET /api/cars` without query parameters returns the full list for backwards compatibility.
Use `page` and `size` to get a Spring page response. `page` is zero-based, and `size` must be between 1 and 100.
Any filter parameter also switches the response to a Spring page response.

Lookup endpoints return sorted unique non-empty values. `GET /api/cars/models` requires the `brand` query parameter to keep the response scoped.

Supported filters:

| Parameter | Example |
| --- | --- |
| `q` | `q=audi` |
| `brand`, `model`, `fuelType`, `bodyType`, `gearbox`, `transmission`, `sellerType` | `brand=Audi&brand=BMW` |
| `minYear`, `maxYear` | `minYear=2018&maxYear=2024` |
| `minMileage`, `maxMileage` | `maxMileage=120000` |
| `minPrice`, `maxPrice` | `minPrice=20000&maxPrice=60000` |
| `minEngineCapacity`, `maxEngineCapacity` | `minEngineCapacity=1400` |
| `minEnginePower`, `maxEnginePower` | `minEnginePower=100` |
| `actual` | `actual=true` |
| `postedFrom`, `postedTo` | `postedFrom=2026-06-01T00:00:00Z` |
| `sortBy`, `sortDirection` | `sortBy=year&sortDirection=desc` |

Examples:

```bash
curl http://localhost:8080/api/scraper/scrape
curl http://localhost:8080/api/scraper/recheck
curl -X PUT http://localhost:8080/api/scraper/settings \
  -H "Content-Type: application/json" \
  -d '{"searchUrl": "https://www.otomoto.pl/osobowe/bmw/od-2006?search%5Bfilter_enum_fuel_type%5D=petrol&search%5Bfilter_float_price%3Ato%5D=15000", "maxRecheckListingsPerRun": 100}'
curl -X POST http://localhost:8080/api/scraper/schedule \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "intervalMinutes": 60}'
curl -X POST http://localhost:8080/api/scraper/schedule \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "timeOfDay": "03:00"}'
curl -X POST http://localhost:8080/api/scraper/recheck/schedule \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "intervalMinutes": 180}'
curl http://localhost:8080/api/cars/count
curl http://localhost:8080/api/cars
curl "http://localhost:8080/api/cars?page=0&size=20"
curl "http://localhost:8080/api/cars?page=0&size=20&brand=Audi&minYear=2018&maxPrice=60000&actual=true&sortBy=currentPrice&sortDirection=asc"
curl http://localhost:8080/api/cars/brands
curl "http://localhost:8080/api/cars/models?brand=Audi"
curl http://localhost:8080/api/cars/1
```

Add a car manually:

```bash
curl -X POST http://localhost:8080/api/cars \
  -H "Content-Type: application/json" \
  -d '{
    "brand": "BMW",
    "model": "320i",
    "year": 2020,
    "mileage": 50000,
    "fuelType": "Benzyna",
    "engineCapacity": 2000,
    "bodyType": "Sedan",
    "gearbox": "Automatyczna",
    "url": "https://example.com/car",
    "description": "Manual entry"
  }'
```

Delete a car:

```bash
curl -X DELETE http://localhost:8080/api/cars/1
```

## Data Model

Entity: `com.example.carmarketanalyzer.data.Car`

Table: `cars_info`

Main fields:

| Field | Description |
| --- | --- |
| `id` | Primary key, mapped to the `car_id` column |
| `brand`, `model` | Car brand and model |
| `version`, `generation` | Version and generation |
| `year`, `mileage` | Production year and mileage |
| `fuelType` | Fuel type |
| `engineCapacity`, `enginePower` | Engine displacement and power |
| `currentPrice` | Latest known price used for filtering and sorting |
| `priceHistory` | JSON price history. Keys are timestamps, values are prices, for example `{"2026-06-04T18:00:00Z": 45000}` |
| `bodyType`, `gearbox`, `transmission` | Body type, gearbox, and transmission |
| `color`, `doorCount`, `seats`, `sellerType` | Additional listing details |
| `url` | Listing URL |
| `location` | JSON location data |
| `photoPath`, `htmlPath` | Reserved paths for downloaded files |
| `description` | Listing description |
| `actual` | `true` when the listing is considered active, mapped to `is_actual` |
| `firstSeenAt` | First time the listing was stored or seen by this application |
| `lastSeenAt` | Last time the listing page was successfully loaded |
| `lastCheckedAt` | Last time the listing URL was checked |
| `disappearedAt` | Time when the listing was confirmed as disappeared |
| `unavailableChecksCount` | Consecutive confirmed unavailable checks |

With `spring.jpa.hibernate.ddl-auto=update`, Hibernate creates the new columns automatically on application startup. For production-like databases, review the generated schema before relying on automatic migrations.

## How The Scraper Works

1. Loads the configured search page. The default is `https://www.otomoto.pl/osobowe`.
2. Finds listing links that contain `/oferta/`.
3. Opens each listing page.
4. Extracts the title, price, parameters, description, and location.
5. Validates that at least `brand` and `url` are present.
6. Finds an existing car by `url`.
7. Inserts the car if it is new, or updates the existing row if the URL already exists.
8. Appends the current price to `priceHistory` only when it differs from the latest stored price.
9. Marks successfully loaded listings as actual and refreshes `lastSeenAt` / `lastCheckedAt`.
10. Rechecks previously active stored listings by direct URL.
11. Marks a listing as not actual only after 2 consecutive confirmed `404` or `410` checks.
12. Waits 1 second between listing requests.

The scraper does not mark a listing as disappeared for inconclusive checks such as `403`, `429`, `5xx`, timeout, missing `__NEXT_DATA__`, or other temporary errors. In those cases it only updates `lastCheckedAt` and logs a warning.

Rechecks are batched. A single manual or scheduled recheck processes at most `maxRecheckListingsPerRun` active listings. The batch is ordered by `lastCheckedAt`: never-checked listings go first, then the oldest checked listings. This prevents large databases from being fully scanned on every scheduled run.

Recheck eligibility also depends on listing age:

| Listing age in this database | Minimum time between checks |
| --- | --- |
| Less than 3 days | 12 hours |
| 3-14 days | 24 hours |
| 14+ days | 72 hours |

Listing age is based on `firstSeenAt`. For older rows where `firstSeenAt` is still empty, the application falls back to `lastSeenAt`; rows without either timestamp are eligible first.

## Listing Recheck Rules

Direct URL checks use this status model:

| Condition | Result |
| --- | --- |
| Page loads and contains `script#__NEXT_DATA__` | Listing is active. Price and details are updated. |
| HTTP `404` or `410` | `unavailableChecksCount` is incremented. After 2 consecutive checks, `actual=false` and `disappearedAt` is set. |
| HTTP `403`, `429`, `5xx`, timeout, or missing data script | Check is inconclusive. The listing remains actual. |

Manual recheck:

```bash
curl http://localhost:8080/api/scraper/recheck
```

Scheduled scraping also performs rechecks after processing the configured search page.

The HTML structure of `otomoto.pl` can change. If the scraper returns no results, check the CSS selectors in `OtoMotoScraper.java`.

## Logging

Logging is configured in `application.properties`:

```properties
logging.level.com.example.carmarketanalyzer=DEBUG
logging.level.org.springframework.web=INFO
```

Typical messages:

```text
Found 50 listings
Saved or updated car: Audi A4 from https://...
Successfully saved or updated 45 cars in database
Rechecking 120 known active listings
Error parsing listing: ...
```

## Troubleshooting

### The app does not start because of PostgreSQL

Make sure PostgreSQL is running, the `otomoto` database exists, and `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` match your local database settings.

### No cars are saved after starting the scraper

Check:

```bash
curl http://localhost:8080/api/scraper/status
curl http://localhost:8080/api/cars/count
```

Also check the application logs. Common causes are a changed `otomoto.pl` HTML structure, no network access to the site, blocked requests, or outdated selectors.

### One listing fails

This is expected. The scraper logs the error and continues with the next listing.

### A listing is still actual after one 404

This is expected. The scraper requires 2 consecutive confirmed unavailable checks before setting `actual=false` and `disappearedAt`. This avoids false removals caused by temporary site or network problems.

## Possible Improvements

- Make the `otomoto.pl` selectors more resilient.
- Store listing photos and HTML snapshots.
- Add search and filtering by database fields.
- Export data to CSV or Excel.
