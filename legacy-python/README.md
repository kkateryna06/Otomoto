# Legacy Python stack

This directory contains the old Python implementation of the Otomoto backend pipeline. It is kept for reference and possible data recovery, but the current active backend/scraper lives in `../CarMarketAnalyzer`.

## Contents

### `otomoto-data-updater/`

Legacy scraper and database updater.

Main responsibilities:

- Reads Otomoto search URLs from `otomoto-data-updater/links_config.txt`.
- Scrapes new ads from search result pages.
- Parses ad details from embedded page JSON.
- Downloads ad photos into `otomoto-data-updater/car_photos/`.
- Saves raw ad HTML into `otomoto-data-updater/car_htmls/`.
- Writes parsed records into PostgreSQL tables.
- Checks already saved ads and marks dead listings as sold/inactive.
- Updates price history when the current price changes.

Important files:

- `main.py` - interactive entry point for running new-ad scraping, relevance checks, or auto mode.
- `first_run.py` - setup/check script for DB connectivity, table creation, URL config, and old server startup.
- `db_config.py` - PostgreSQL connection settings.
- `links_config.txt` - configured Otomoto search URLs split into `ALL CARS` and `SPECIAL CARS`.
- `update_new_ads.py` - scraping, parsing, image/HTML saving, and new-ad insertion.
- `update_relevant.py` - checks existing ad links and updates sold status or price history.
- `database_update.py` - low-level PostgreSQL insert/update helpers.
- `requirements.txt` - Python dependencies for this legacy stack.

The updater uses two database tables:

- `cars_info` - general market listings.
- `special_cars_info` - selected special/model-specific listings.

### `otomoto-server/`

Legacy FastAPI server that exposes data from the same PostgreSQL database.

Main responsibilities:

- Serves car listings from `cars_info` and `special_cars_info`.
- Supports filtering/searching through query parameters.
- Serves car details by ID.
- Serves saved local images by car ID.
- Exposes helper endpoints for unique values and min/max values.

Important files:

- `app/main.py` - FastAPI app setup and router registration.
- `app/routes.py` - API endpoints.
- `app/models.py` - SQLAlchemy models.
- `app/database.py` - database engine/session setup. It imports `DATABASE_URL` from `../otomoto-data-updater/db_config.py`.
- `requirements.txt` - minimal server dependency list.

## Setup

Install dependencies from the updater requirements file:

```powershell
pip install -r legacy-python/otomoto-data-updater/requirements.txt
```

Configure PostgreSQL in:

```text
legacy-python/otomoto-data-updater/db_config.py
```

Configure search URLs in:

```text
legacy-python/otomoto-data-updater/links_config.txt
```

## Running the updater

From the repository root:

```powershell
cd legacy-python/otomoto-data-updater
python main.py
```

Interactive options:

- `1` - scrape new ads.
- `2` - update relevance/sold status for existing ads.
- `3` - auto mode: periodically scrape and sometimes run relevance checks.

Dataset options:

- `1` - `cars_info`.
- `2` - `special_cars_info`.

## Running the server

From the repository root:

```powershell
cd legacy-python/otomoto-server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Swagger UI:

```text
http://localhost:8000/docs
```

## Notes

- This code is legacy and is not the current source of truth.
- The active Java backend/scraper is `../CarMarketAnalyzer`.
- Generated scraper output is ignored by git: `car_htmls/`, `car_photos/`, `.venv/`, and `__pycache__/`.
- The virtual environment was moved with the legacy stack, but Windows virtual environments can contain absolute paths. If it breaks, recreate it from `otomoto-data-updater/requirements.txt`.
