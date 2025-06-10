# Otomoto cars

## Overview
This project started as a personal tool to track the used car market — to see which cars sell fast and which linger with active listings. The idea was to build a handy Android app that gathers all this data in one place for easy browsing.

I also plan to add smart alerts to notify when a great deal pops up. And looking ahead, I want to integrate an AI model that can evaluate cars based on their specs and photos — so you don’t have to be a car expert to know what’s worth your attention.

This project combines web scraping, a robust backend API, and a sleek mobile app to create a seamless experience for car hunters.


## Project Structure
A brief overview of the three main components of the project:

### otomoto-data-updater/
A Python module responsible for scraping car listings from Otomoto.pl and updating the local database.

- Allows you to manually specify search result URLs from Otomoto (e.g., sedans under 30,000 PLN) for targeted scraping.

- Parses each listing page and stores structured data in a local PostgreSQL database.

- Automatically downloads car photos and saves them locally - a key part of the project, as visual content adds major value.

- Designed to be run manually, but can be easily scheduled for periodic updates.

Supports two independent datasets:
- **All cars** — general listings to observe market trends.
- **Special cars** — focused tracking for specific models you're interested in.

This allows for more organized data analysis or segmentation — but both can be used together or just one of them.


### otomoto-server/

A backend API built with FastAPI that serves data from the local database.

- Provides REST endpoints for listing cars, filtering, searching, accessing images, and more.

- Uses SQLAlchemy for database interaction and Pydantic for request/response validation.

- Swagger UI available at /docs for easy testing and development.

- CORS configured for secure communication with the Android app.

### otomoto-app/

A Kotlin-based Android application for browsing and filtering cars.

- Connects to the backend using Retrofit and displays data in a clean, user-friendly interface.

- Includes a filter system (by price, make, model, etc.).

- Uses Room database to store favorite (saved) cars locally.

- Contains two main screens: a list of available cars and a detailed car view with photos.

- Plans to add user authentication and cloud-based storage for favorites and preferences.


## Features
### Data Collection

- Web scraping of car listings from Otomoto via custom search URLs using BeautifulSoup

- Photo downloading and local storage of vehicle images for rich visual display

- Structured PostgreSQL database to store and update vehicle information

### Backend API (FastAPI)

- REST API for retrieving car data

- Filtering by attributes like make, price, year, etc.

- Swagger UI for interactive API documentation

- Console logging for incoming requests and server actions

### Mobile App (Kotlin)

- Car listing screen and detailed car view

- Filtering UI and saved search support

- Room local database for storing favorite cars


## Installation and Setup

### Prerequisites

- Python 3.9+ (for server and scraper)

- PostgreSQL (database)

- Kotlin 1.5+ and Android Studio (for mobile app)

- Additional: pip for installing Python dependencies

### Data Updater Setup

1. Go to otomoto-data-updater/ directory

```
cd otomoto-data-updater
```

2. Install dependencies:

```
pip install -r requirements.txt
```

3. Paste the Otomoto search URLs of interest into configuration file `config.txt`

4. Run the script:


```
python main.py
```

### Server Setup

1. Go to the otomoto-server/ directory

```
cd otomoto-server
```

2. Install dependencies:

```
pip install -r requirements.txt
```

3. Configure environment variables (e.g. database connection):

```
DATABASE_URL=postgresql://user:password@localhost/dbname
```

4. Start the server:

```
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

5. Open Swagger UI in a browser at: http://localhost:8000/docs

### Mobile App Setup

1. Open the otomoto-app/ directory in Android Studio

2. Sync the project with Gradle

3. Run the application on an emulator or real device

4. If necessary, change the server URL in the code to http://localhost:8000 (or the server IP address)


## Usage
### Data Updater

The updater script handles scraping and updating vehicle listings from Otomoto.pl.
It supports two types of actions and two target datasets:

1. **Choose an action:**
```
1 - collect new ads
2 - update relevant (check if saved listings are still active)
```

2. **Choose target dataset:**
```
1 - All cars (general market listings)
2 - Special cars (personal interest listings)
```

> "All cars" is used to track the general market (e.g. all cars under 20,000 PLN). 
> "Special cars" is meant for focused tracking of specific models or brands you're personally interested in.

**Note:** If you're only using one dataset (e.g. All cars), you can consistently select `1` for both prompts.

The script will fetch or update data accordingly, saving it to the corresponding database and downloading car photos.

### Server API

Examples of endpoints:

```
GET /allcars — get a list of all cars

GET /allcars/{id} — get details of a specific car

GET /allcars/search/{value} — get unique field values ​​(e.g. mark)

GET /allcars/searchminmax/{value} — minimum and maximum values ​​(e.g. price)

GET /allcars/{id}/photo — get a photo by ID
```

_Authorization is not required yet._

### Mobile App
The application allows you to conveniently view and filter cars:

- Main screen with a general list of cars

- Detailed screen with characteristics and a photo

- Local saving of saved cars via Room Database

- Filtering by parameters (will be expanded)

- Launch is performed via Android Studio (see the Installation section).

### API Documentation
Full documentation for the API is available at:

```
http://localhost:8000/docs
```

Swagger UI allows you to:

- Test endpoints directly in the browser

- View possible request parameters

- See the structure of the returned data


## Future Plans
- ML model for evaluating ads based on characteristics and photos
- Analytics on the car market — which cars are sold faster, which ones stagnate
- Price tracking for specific ads
- Sorting ads on the main screen (by price, date, etc.)
- Search by model/brand (currently not implemented)
- Push notifications when interesting cars appear
- Authorization and synchronization of favorites via account
- Ability to add links to desired cars via the app
- Complete redesign of the app (light/dark theme, aesthetic UI, animations)
- Option to set number of cars per page (e.g. 10 / 20 / 50)

## Final words

This isn’t just another to-do app or a movie list built from a tutorial. This project is personal — born from curiosity, built with care, and driven by a real passion for cars and technology.

I wanted a tool to help me explore and understand the used car market more deeply. Over time, I’ve poured not just code, but time, thought, and a bit of soul into it.

If it looks a little raw in places — that’s because it’s alive. And I’m always learning, improving, and dreaming about what’s next.  

:D
