# CarMarketAnalyzer

CarMarketAnalyzer is a Spring Boot application for collecting car listings from `otomoto.pl`, storing them in PostgreSQL, and exposing the data through a REST API.

The application supports manual and scheduled scraping, direct rechecks of already stored listings, price history tracking, listing actuality checks, photo link storage, limited local photo downloads, and basic seller location data.

## Features

- Loads an Otomoto search page. The default URL is `https://www.otomoto.pl/osobowe`.
- Finds listing links containing `/oferta/`.
- Opens each listing page and reads data from `script#__NEXT_DATA__`.
- Stores cars in PostgreSQL in the `cars_info` table.
- Updates existing listings by `url` instead of inserting duplicates.
- Stores the latest price in `currentPrice` and price history in JSON `priceHistory`.
- Adds a new price history entry only when the price changes.
- Stores all found photo links in `photoUrls`.
- Downloads only the first `app.max-photos-per-listing` photos locally. The default is 3.
- Stores the cover photo path in `photoPath` and downloaded photo paths in `localPhotoPaths`.
- Stores location data: `city`, `region`, `postalCode`, `latitude`, `longitude`, `zoom`, `radius`.
- Rechecks already stored active listings directly by URL.
- Marks a listing as inactive only after 2 confirmed `404` or `410` responses.
- Keeps listings active on temporary or inconclusive errors: `403`, `429`, `5xx`, timeout, or missing `__NEXT_DATA__`.
- Provides a browser UI for scraper control.
- Supports manual scrape, manual recheck, interval schedules, and daily schedules.
- Reports the last run result: found, saved, and errors.

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
  ScraperService.java                # Manual/scheduled scrape and recheck execution
  OtoMotoScraper.java                # Main scrape, parse, save, and recheck logic
  ScraperRunResult.java              # Run result: found/saved/errors
  CarRepository.java                 # Spring Data JPA repository
  PhotoStorageConfig.java            # Serves local photos through /photos/**
  data/Car.java                      # JPA entity

src/main/resources/
  application.properties             # Database, JPA, logging, and photo settings
  static/index.html                  # Browser UI
```

## Requirements

- Java 21+
- PostgreSQL 12+
- Network access to `otomoto.pl`
- The included Gradle wrapper

## Database Setup

Create the database:

```sql
CREATE DATABASE otomoto;
```

Database settings are read from `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/otomoto}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
```

At minimum, set your local PostgreSQL password.

PowerShell:

```powershell
$env:DB_PASSWORD="your-local-password"
```

macOS/Linux:

```bash
export DB_PASSWORD="your-local-password"
```

If needed, override the full database connection:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/otomoto"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-local-password"
```

## Application Settings

```properties
app.photo-storage-dir=${PHOTO_STORAGE_DIR:data/photos}
app.max-photos-per-listing=${MAX_PHOTOS_PER_LISTING:3}
```

- `PHOTO_STORAGE_DIR` controls where downloaded photos are stored.
- `MAX_PHOTOS_PER_LISTING` controls how many photos are downloaded locally per listing.

Important: `photoUrls` stores all found photo links from the listing. `MAX_PHOTOS_PER_LISTING` limits only local downloads.

## Run

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew bootRun
```

The application will be available at:

```text
http://localhost:8080
```

Browser UI:

```text
http://localhost:8080/
```

## Browser UI

The UI is the main way to control the scraper locally.

The UI lets you:

- change `Search URL`;
- change `Max recheck listings per run`;
- start a scrape;
- start a direct recheck of already stored active listings;
- enable interval or daily schedules for scrape;
- enable interval or daily schedules for recheck;
- disable schedules;
- view status, next run times, last run result, and stored car count.

Typical workflow:

1. Start the application.
2. Open `http://localhost:8080/`.
3. Paste an Otomoto URL into `Search URL`.
4. Set `Max recheck listings per run`.
5. Click `Save Settings`.
6. Click `Run Scrape`.
7. Watch `Last result`, `Next run`, `Next recheck`, and `Cars`.
8. Use `Recheck Listings` when you only want to update prices and actuality without reading the search page.

Scraper settings are kept in application memory. After an application restart, the search URL returns to the default unless you save it again from the UI or change the default in code.

## How Scraping Works

1. Loads the configured search page.
2. Collects unique listing links containing `/oferta/`.
3. Opens each listing page.
4. Reads data from `script#__NEXT_DATA__`.
5. Stores car specs, price, description, URL, photos, and location.
6. Validates that at least `brand` and `url` are present.
7. Updates an existing row if the URL already exists.
8. Inserts a new row if the URL is new.
9. Adds a price history entry only when the price changed.
10. After scraping the search page, rechecks a bounded batch of known active listings that were not found on the current search page.
11. Waits 1 second between listing requests.

Technical values from Otomoto, such as `fuelType`, `bodyType`, `gearbox`, `transmission`, and `color`, are stored as they come from `parametersDict.*.values[].value`, without artificial capitalization. Display fields such as `brand`, `model`, `version`, and `generation` are read from labels.

## Recheck And Actuality Rules

Recheck verifies already stored active listings directly by URL.

Rules:

| Condition | Result |
| --- | --- |
| Page loads and contains `script#__NEXT_DATA__` | Listing is active. Details and price are updated. |
| HTTP `404` or `410` | `unavailableChecksCount` is incremented. After 2 consecutive confirmations, `actual=false` is set. |
| HTTP `403`, `429`, `5xx`, timeout, or missing `__NEXT_DATA__` | Check is inconclusive. The listing remains active. |

Rechecks run in bounded batches. One run checks at most `maxRecheckListingsPerRun` listings.

Recheck priority:

1. Listings that were never checked.
2. Listings with the oldest `lastCheckedAt`.
3. Older listings are checked less often.

Intervals:

| Listing age | Minimum recheck interval |
| --- | --- |
| Less than 3 days | 12 hours |
| 3-14 days | 24 hours |
| 14+ days | 72 hours |

Listing age is based on `postedAt` when available, otherwise `lastSeenAt`.

## REST API

### Scraper

```http
GET /api/scraper/scrape
```

Starts scraping in a background thread. If the scraper is already running, returns `Scraper is already running.`.

```http
GET /api/scraper/recheck
```

Starts a direct recheck of stored active listings without reading the search page.

```http
GET /api/scraper/status
```

Returns scraper status:

- `running`
- `scheduleEnabled`
- `scheduleMode`
- `intervalMinutes`
- `timeOfDay`
- `recheckScheduleEnabled`
- `recheckScheduleMode`
- `recheckIntervalMinutes`
- `recheckTimeOfDay`
- `searchUrl`
- `lastSearchUrl`
- `nextRunAt`
- `recheckNextRunAt`
- `lastStartedAt`
- `lastFinishedAt`
- `lastTrigger`
- `lastResult`

Example `lastResult`:

```text
Completed: found 45, saved 43, errors 2
```

```http
GET /api/scraper/settings
```

Returns current settings:

```json
{
  "searchUrl": "https://www.otomoto.pl/osobowe",
  "maxRecheckListingsPerRun": 100
}
```

```http
PUT /api/scraper/settings
Content-Type: application/json

{
  "searchUrl": "https://www.otomoto.pl/osobowe/bmw",
  "maxRecheckListingsPerRun": 100
}
```

Validation:

- URL must use `https`.
- Host must be `otomoto.pl` or a subdomain of `otomoto.pl`.
- Path must start with `/osobowe`.
- `maxRecheckListingsPerRun` must be between 1 and 1000.

```http
POST /api/scraper/schedule
Content-Type: application/json

{
  "enabled": true,
  "intervalMinutes": 60
}
```

Enables scheduled scraping every 60 minutes.

```http
POST /api/scraper/schedule
Content-Type: application/json

{
  "enabled": true,
  "timeOfDay": "03:00"
}
```

Enables daily scraping at 03:00 in the server's local time zone.

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

Enables scheduled rechecks every 180 minutes.

```http
POST /api/scraper/recheck/schedule
Content-Type: application/json

{
  "enabled": true,
  "timeOfDay": "06:00"
}
```

Enables daily rechecks at 06:00.

```http
POST /api/scraper/recheck/schedule
Content-Type: application/json

{
  "enabled": false
}
```

Disables scheduled rechecks.

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

`GET /api/cars` without query parameters returns all cars for compatibility. For normal use, prefer pagination.

Pagination:

- `page` is zero-based.
- `size` must be between 1 and 100.
- Any filter parameter switches the response to a Spring Page response.

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

Allowed `sortBy` values:

```text
id, brand, model, year, mileage, currentPrice,
engineCapacity, enginePower, postedAt, lastSeenAt, lastCheckedAt
```

Text filters are case-insensitive.

Examples:

```bash
curl http://localhost:8080/api/cars/count
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
    "fuelType": "petrol",
    "engineCapacity": 2000,
    "bodyType": "sedan",
    "gearbox": "automatic",
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
| `id` | Primary key |
| `brand`, `model` | Car brand and model |
| `version`, `generation` | Version and generation |
| `year`, `mileage` | Production year and mileage |
| `fuelType` | Fuel type from Otomoto technical value |
| `engineCapacity`, `enginePower` | Engine displacement and power |
| `currentPrice` | Latest known price |
| `priceHistory` | JSON map: timestamp -> price |
| `bodyType`, `gearbox`, `transmission` | Body type, gearbox, and transmission |
| `urbanConsumption`, `extraUrbanConsumption` | Fuel consumption, when present |
| `color`, `doorCount`, `seats`, `sellerType` | Additional listing details |
| `url` | Listing URL |
| `location` | JSON location object |
| `photoPath` | Cover photo, usually `/photos/.../cover.jpg` |
| `photoUrls` | All found photo URLs from the listing |
| `localPhotoPaths` | Public paths of downloaded local photos |
| `htmlPath` | Reserved field for an HTML snapshot |
| `description` | Listing description |
| `postedAt` | Listing creation date from Otomoto |
| `actual` | Whether the listing is considered active |
| `lastSeenAt` | Last time the listing was successfully seen |
| `lastCheckedAt` | Last time the listing URL was checked |
| `disappearedAt` | When the listing was confirmed as disappeared |
| `unavailableChecksCount` | Consecutive confirmed unavailable checks |

Example `location`:

```json
{
  "city": "Leszno",
  "region": "wielkopolskie",
  "postalCode": "64-100",
  "latitude": 51.82748484,
  "longitude": 16.49790597,
  "zoom": 16,
  "radius": 0
}
```

`region` is stored as a stable lowercase slug from `canonicals.region`. Display formatting or translation should be handled by the client.

Example `priceHistory`:

```json
{
  "2026-06-18T10:15:30Z": 45000,
  "2026-06-20T09:40:12Z": 43500
}
```

## Photos

The scraper separates photo links from local files:

- `photoUrls` stores all photo links from the listing JSON.
- `localPhotoPaths` stores only downloaded local photos.
- `photoPath` stores the first local photo and is used as the cover.

By default, 3 photos are downloaded:

```properties
app.max-photos-per-listing=${MAX_PHOTOS_PER_LISTING:3}
```

Local photos are stored in:

```properties
app.photo-storage-dir=${PHOTO_STORAGE_DIR:data/photos}
```

And served through:

```text
/photos/**
```

Example:

```text
http://localhost:8080/photos/audi-a4-2020-abc123/cover.jpg
```

## Logging

Settings:

```properties
logging.level.com.example.carmarketanalyzer=DEBUG
logging.level.org.springframework.web=INFO
```

Typical messages:

```text
Found 50 listings
Search page result: found 50, saved 45, errors 5
Recheck result: checked 20, saved 20, errors 1
Scraping finished by manual: Completed: found 70, saved 65, errors 6
```

In a full scrape, `found` includes listings found on the search page plus checked listings from the recheck phase. `saved` means a row was inserted or updated.

## Checks And Tests

Compile:

```powershell
.\gradlew.bat compileJava
```

Run tests:

```powershell
.\gradlew.bat test
```

Note: the current `@SpringBootTest` starts the Spring context with the real datasource settings. If `DB_PASSWORD` is missing or PostgreSQL is unavailable, the test can fail on database connection.

## Troubleshooting

### The app does not start because of PostgreSQL

Check:

- PostgreSQL is running.
- The `otomoto` database exists.
- `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are correct.

### The scraper saves no cars

Check:

```bash
curl http://localhost:8080/api/scraper/status
curl http://localhost:8080/api/cars/count
```

Likely causes:

- Otomoto changed the HTML/JSON page structure.
- There is no access to `otomoto.pl`.
- The site is blocking requests.
- The search page has no `/oferta/` links.
- The listing does not contain the required `script#__NEXT_DATA__`.

### Price is not duplicated in priceHistory

This is expected. A new `priceHistory` entry is added only when the current price differs from the latest stored price.

### A listing is still actual after one 404

This is expected. The scraper requires 2 consecutive confirmed `404` or `410` responses before setting `actual=false`.

### One listing fails during parsing

This does not stop the whole scrape. The error is logged, the `errors` counter is incremented, and the scraper continues.

## Known Limitations And Possible Improvements

- `CarController` currently exposes the JPA entity directly. A DTO would be cleaner.
- For location filters, `city`, `region`, `latitude`, and `longitude` should become separate columns.
- `GET /api/cars` without pagination can become expensive on a large database.
- `GET /api/scraper/scrape` and `GET /api/scraper/recheck` change server state. They should eventually move to `POST`.
- For a reliable schema, add Flyway or Liquibase instead of relying on `ddl-auto=update`.
- Tests should use a separate test profile with H2 or Testcontainers.
