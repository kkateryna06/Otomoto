# Quick Start

A short local setup guide for CarMarketAnalyzer. Full documentation is available in [README.md](README.md).

## 1. Prepare PostgreSQL

Create the database:

```sql
CREATE DATABASE otomoto;
```

Set your local PostgreSQL password.

PowerShell:

```powershell
$env:DB_PASSWORD="your-local-password"
```

macOS/Linux:

```bash
export DB_PASSWORD="your-local-password"
```

If you need custom database settings:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/otomoto"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-local-password"
```

## 2. Run The Application

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew bootRun
```

Open:

```text
http://localhost:8080/
```

## 3. Start A Scrape From The UI

1. Paste an Otomoto URL into `Search URL`.
2. Set `Max recheck listings per run`.
3. Click `Save Settings`.
4. Click `Run Scrape`.
5. Watch `Last result`, `Cars`, `Next run`, and `Next recheck`.

`Last result` shows the last run summary:

```text
Completed: found 45, saved 43, errors 2
```

## 4. Check Data Through The API

```bash
curl http://localhost:8080/api/cars/count
curl "http://localhost:8080/api/cars?page=0&size=20"
curl http://localhost:8080/api/cars/brands
curl "http://localhost:8080/api/cars/models?brand=Audi"
```

Filter example:

```bash
curl "http://localhost:8080/api/cars?page=0&size=20&brand=Audi&minYear=2018&maxPrice=60000&actual=true&sortBy=currentPrice&sortDirection=asc"
```

## 5. Main Endpoints

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

## 6. Important Notes

- `photoUrls` stores all photo links from the listing.
- Only the first `MAX_PHOTOS_PER_LISTING` photos are downloaded locally. The default is 3.
- `photoPath` is the cover photo.
- `location` stores `city`, `region`, `postalCode`, `latitude`, `longitude`, `zoom`, and `radius`.
- A price is added to `priceHistory` only when it changes.
- A listing becomes `actual=false` only after 2 consecutive confirmed `404` or `410` responses.
- Temporary errors such as `403`, `429`, `5xx`, and timeout do not mark a listing inactive.

## 7. If Something Does Not Work

Check scraper status and car count:

```bash
curl http://localhost:8080/api/scraper/status
curl http://localhost:8080/api/cars/count
```

Common causes:

- PostgreSQL is not running or `DB_PASSWORD` is wrong.
- Otomoto changed the page structure.
- There is no access to `otomoto.pl`.
- The search URL does not start with `/osobowe`.
- The site is temporarily blocking requests.

More details: [README.md](README.md).
