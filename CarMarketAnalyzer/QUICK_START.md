# Quick Start

A short guide for running CarMarketAnalyzer locally.

## 1. Prepare PostgreSQL

Create the database:

```sql
CREATE DATABASE otomoto;
```

The application uses these settings from `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/otomoto}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
```

Set your local database password before starting the app.

PowerShell:

```powershell
$env:DB_PASSWORD="your-local-password"
```

macOS or Linux:

```bash
export DB_PASSWORD="your-local-password"
```

## 2. Run The Application

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew bootRun
```

The application will be available at:

```text
http://localhost:8080
```

## 3. Control The Parser In The Browser

Open:

```text
http://localhost:8080/
```

This is the main way to manage parser settings. You do not need PowerShell for changing the search URL, starting the scraper, running rechecks, or configuring the schedule.

Use the UI to:

- change the Otomoto search URL;
- set the maximum number of listings checked by one recheck run;
- run the scraper;
- recheck already saved active listings;
- enable interval or daily scheduled scraping;
- enable interval or daily scheduled rechecks;
- disable either schedule;
- check scraper status and stored car count.

Recommended flow:

1. Paste the Otomoto URL into `Search URL`.
2. Set `Max recheck listings per run`.
3. Click `Save URL`.
4. Click `Run Scrape`.
5. Watch `Last result`, `Next run`, `Next recheck`, and `Cars` in the status card.
6. Click `Recheck Listings` when you only want to update prices and check whether stored listings are still actual.
7. Use `Scrape Schedule` for full automatic scraper runs.
8. Use `Recheck Schedule` for automatic price/actuality checks without scanning the search page.

The scraper inserts new listings and updates existing listings by the same `url`. If the price changed, the new price is appended to `priceHistory`.

Rechecks are batched. One manual or scheduled recheck processes at most `Max recheck listings per run` active listings, ordered by oldest `lastCheckedAt`. This prevents a large active database from being fully checked every time.

The batch includes only listings that are due for another check:

| Listing age | Recheck interval |
| --- | --- |
| Less than 3 days | 12 hours |
| 3-14 days | 24 hours |
| 14+ days | 72 hours |

## 4. Check The Results

```bash
curl http://localhost:8080/api/cars/count
curl http://localhost:8080/api/cars
curl "http://localhost:8080/api/cars?page=0&size=20"
```

`page` starts from 0. `size` can be from 1 to 100.

Filter examples:

```bash
curl "http://localhost:8080/api/cars?page=0&size=20&brand=Audi&minYear=2018&maxPrice=60000&actual=true"
curl "http://localhost:8080/api/cars?page=0&size=20&q=hybrid&maxMileage=120000&sortBy=currentPrice&sortDirection=asc"
```

Supported filters include `q`, `brand`, `model`, `fuelType`, `bodyType`, `gearbox`, `transmission`, `sellerType`, `minYear`, `maxYear`, `minMileage`, `maxMileage`, `minPrice`, `maxPrice`, `minEngineCapacity`, `maxEngineCapacity`, `minEnginePower`, `maxEnginePower`, `actual`, `postedFrom`, and `postedTo`.

Get one car by ID:

```bash
curl http://localhost:8080/api/cars/1
```

Each car includes listing status fields:

```json
{
  "actual": true,
  "lastSeenAt": "2026-06-04T18:00:00Z",
  "lastCheckedAt": "2026-06-04T18:00:00Z",
  "disappearedAt": null,
  "unavailableChecksCount": 0
}
```

Meaning:

- `actual=true`: the listing is considered active.
- `actual=false`: the listing was confirmed as disappeared.
- `disappearedAt`: when the listing was marked as disappeared.
- `priceHistory`: JSON map of timestamp to price. A new entry is added only when the price changes.

The app marks a listing as disappeared only after 2 consecutive confirmed `404` or `410` checks. Temporary errors like `403`, `429`, `5xx`, timeout, or missing page data do not mark the listing as disappeared.

## 5. Add A Car Manually

```bash
curl -X POST http://localhost:8080/api/cars \
  -H "Content-Type: application/json" \
  -d '{
    "brand": "Audi",
    "model": "A4",
    "year": 2020,
    "mileage": 50000,
    "fuelType": "Benzyna",
    "url": "https://example.com/car"
  }'
```

## 6. Optional API Commands

The UI uses these endpoints internally. Use them only when you specifically want to script parser actions.

Start scraper:

```bash
curl http://localhost:8080/api/scraper/scrape
```

Response:

```text
Scraping started in background.
```

Recheck already saved active listings:

```bash
curl http://localhost:8080/api/scraper/recheck
```

Response:

```text
Listing recheck started in background.
```

Change the scraper search URL:

```powershell
Invoke-RestMethod `
  -Method PUT `
  -Uri "http://localhost:8080/api/scraper/settings" `
  -ContentType "application/json" `
  -Body '{"searchUrl": "https://www.otomoto.pl/osobowe/bmw/od-2006?search%5Bfilter_enum_fuel_type%5D=petrol&search%5Bfilter_float_price%3Ato%5D=15000", "maxRecheckListingsPerRun": 100}'
```

Check current scraper settings:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/scraper/settings"
```

Enable scheduled scraping:

```powershell
curl -X POST http://localhost:8080/api/scraper/schedule `
  -H "Content-Type: application/json" `
  -d '{\"enabled\": true, \"intervalMinutes\": 60}'
```

Enable scheduled scraping every day at 03:00:

```powershell
Invoke-RestMethod `
  -Method POST `
  -Uri "http://localhost:8080/api/scraper/schedule" `
  -ContentType "application/json" `
  -Body '{"enabled": true, "timeOfDay": "03:00"}'
```

Disable scheduled scraping:

```powershell
curl -X POST http://localhost:8080/api/scraper/schedule `
  -H "Content-Type: application/json" `
  -d '{\"enabled\": false}'
```

Enable scheduled rechecks every 180 minutes:

```powershell
curl -X POST http://localhost:8080/api/scraper/recheck/schedule `
  -H "Content-Type: application/json" `
  -d '{\"enabled\": true, \"intervalMinutes\": 180}'
```

Enable scheduled rechecks every day at 06:00:

```powershell
Invoke-RestMethod `
  -Method POST `
  -Uri "http://localhost:8080/api/scraper/recheck/schedule" `
  -ContentType "application/json" `
  -Body '{"enabled": true, "timeOfDay": "06:00"}'
```

Disable scheduled rechecks:

```powershell
curl -X POST http://localhost:8080/api/scraper/recheck/schedule `
  -H "Content-Type: application/json" `
  -d '{\"enabled\": false}'
```

## 7. Common Problems

### No database connection

Make sure PostgreSQL is running, the `otomoto` database exists, and your environment variables match your local database settings.

### The scraper saves no cars

Check the application logs. Common causes are changed HTML on `otomoto.pl`, no access to the site, blocked requests, or no `/oferta/` links on the page.

### Prices are not duplicated

This is expected. The scraper appends to `priceHistory` only when the current price differs from the latest stored price.

### A disappeared listing is still actual

Run the recheck again later:

```bash
curl http://localhost:8080/api/scraper/recheck
```

The listing becomes `actual=false` only after 2 consecutive confirmed unavailable checks.

### One listing fails

This does not stop the whole process. The scraper skips the problematic listing and continues.

## Main Endpoints

```text
GET    /api/scraper/scrape
GET    /api/scraper/recheck
GET    /api/scraper/status
GET    /api/scraper/settings
PUT    /api/scraper/settings
POST   /api/scraper/schedule
POST   /api/scraper/recheck/schedule
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
