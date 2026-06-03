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

## 3. Start The Scraper

```bash
curl http://localhost:8080/api/scraper/scrape
```

Expected response:

```text
Scraping started in background. Check logs for progress.
```

Change the scraper search URL:

```powershell
Invoke-RestMethod `
  -Method PUT `
  -Uri "http://localhost:8080/api/scraper/settings" `
  -ContentType "application/json" `
  -Body '{"searchUrl": "https://www.otomoto.pl/osobowe/bmw/od-2006?search%5Bfilter_enum_fuel_type%5D=petrol&search%5Bfilter_float_price%3Ato%5D=15000"}'
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

## 4. Check The Results

```bash
curl http://localhost:8080/api/cars/count
curl http://localhost:8080/api/cars
```

Get one car by ID:

```bash
curl http://localhost:8080/api/cars/1
```

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

## 6. Common Problems

### No database connection

Make sure PostgreSQL is running, the `otomoto` database exists, and your environment variables match your local database settings.

### The scraper saves no cars

Check the application logs. Common causes are changed HTML on `otomoto.pl`, no access to the site, blocked requests, or no `/oferta/` links on the page.

### One listing fails

This does not stop the whole process. The scraper skips the problematic listing and continues.

## Main Endpoints

```text
GET    /api/scraper/scrape
GET    /api/scraper/status
GET    /api/scraper/settings
PUT    /api/scraper/settings
POST   /api/scraper/schedule
GET    /api/cars
GET    /api/cars/{id}
GET    /api/cars/count
POST   /api/cars
DELETE /api/cars/{id}
```
