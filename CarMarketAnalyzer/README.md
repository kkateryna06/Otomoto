# CarMarketAnalyzer

Spring Boot application for collecting car listings from `otomoto.pl`, storing them in PostgreSQL, and exposing the data through a REST API.

## Features

- Starts scraping from `https://www.otomoto.pl/osobowe`.
- Loads listing pages with jsoup.
- Extracts basic car data: brand, model, year, mileage, fuel type, gearbox, body type, price, description, URL, and location.
- Saves records to PostgreSQL in the `cars_info` table.
- Provides REST endpoints for starting the scraper and managing cars.
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

## REST API

### Scraper

```http
GET /api/scraper/scrape
```

Starts the scraper in a background thread.

```http
GET /api/scraper/status
```

Returns scraper status, including whether a run is active, current search URL, whether schedule is enabled, schedule mode, next run time, and last run timestamps.

```http
GET /api/scraper/settings
```

Returns current scraper settings.

```http
PUT /api/scraper/settings
Content-Type: application/json

{
  "searchUrl": "https://www.otomoto.pl/osobowe/bmw/od-2006?search%5Bfilter_enum_fuel_type%5D=petrol&search%5Bfilter_float_price%3Ato%5D=15000"
}
```

Changes the search page used by both manual and scheduled scraper runs. The URL must use `https`, point to `otomoto.pl`, and start with `/osobowe`.

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

### Cars

```http
GET    /api/cars
GET    /api/cars/{id}
GET    /api/cars/count
POST   /api/cars
DELETE /api/cars/{id}
```

Examples:

```bash
curl http://localhost:8080/api/scraper/scrape
curl -X PUT http://localhost:8080/api/scraper/settings \
  -H "Content-Type: application/json" \
  -d '{"searchUrl": "https://www.otomoto.pl/osobowe/bmw/od-2006?search%5Bfilter_enum_fuel_type%5D=petrol&search%5Bfilter_float_price%3Ato%5D=15000"}'
curl -X POST http://localhost:8080/api/scraper/schedule \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "intervalMinutes": 60}'
curl -X POST http://localhost:8080/api/scraper/schedule \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "timeOfDay": "03:00"}'
curl http://localhost:8080/api/cars/count
curl http://localhost:8080/api/cars
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
| `priceHistory` | JSON with price data, for example `{"current": "45000"}` |
| `bodyType`, `gearbox`, `transmission` | Body type, gearbox, and transmission |
| `color`, `doorCount`, `seats`, `sellerType` | Additional listing details |
| `url` | Listing URL |
| `location` | JSON location data |
| `photoPath`, `htmlPath` | Reserved paths for downloaded files |
| `description` | Listing description |

## How The Scraper Works

1. Loads the search page `https://www.otomoto.pl/osobowe`.
2. Finds listing links that contain `/oferta/`.
3. Opens each listing page.
4. Extracts the title, price, parameters, description, and location.
5. Validates that at least `brand` and `url` are present.
6. Saves the car through `CarRepository`.
7. Waits 1 second between listings.

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
Saved car: Audi A4 from https://...
Successfully saved 45 cars to database
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

## Possible Improvements

- Detect duplicate listings by `url`.
- Make the `otomoto.pl` selectors more resilient.
- Store listing photos and HTML snapshots.
- Add search and filtering by database fields.
- Export data to CSV or Excel.
