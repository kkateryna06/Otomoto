# Otomoto App

Android application for browsing car offers from an Otomoto-style backend API. The app focuses on searching, filtering, viewing offer details, browsing car photos, and saving favourite cars locally.

## Features

- Browse paginated car listings.
- Search offers by text query.
- Filter offers by brand, model, fuel type, body type, gearbox, transmission, seller type, price, year, mileage, engine capacity, and engine power.
- View detailed car information, including specification, description, price history, and location map.
- Browse offer photos in a swipeable carousel.
- Save and remove favourite cars.
- Persist favourite cars locally with Room.
- Configure backend server URL from the Settings screen without restarting the app.
- Fallback to bundled mock data in debug builds when the API is unavailable.

## Tech Stack

- Kotlin
- Android Jetpack Compose
- Material 3
- Navigation Compose
- Retrofit
- Gson
- Coil
- Room
- Google Maps Compose
- Gradle Kotlin DSL

## Backend API

The app expects a backend server that exposes car data under the configured base URL.

Main endpoints used by the app:

```text
GET /api/cars
GET /api/cars/{id}
GET /api/cars/{filterName}
GET /photos/{photoPath}
```

Car details may also include external image URLs through:

```json
{
  "photoUrls": [
    "https://example.com/image-1",
    "https://example.com/image-2"
  ]
}
```

If `photoUrls` is present, the details screen uses it for the photo carousel. If it is missing or empty, the app falls back to `photoPath`.

## Server URL

The default server URL is configured in:

```text
app/src/main/java/com/example/otomotoapp/data/PreferencesHelper.kt
```

The URL can also be changed inside the app:

```text
Menu -> Settings -> Server URL -> Save
```

After saving a new URL, the app recreates its repository/API client and reloads data from the new backend.

For Android Emulator, use:

```text
http://10.0.2.2:8080/
```

For a physical Android device, use the local network IP address of the machine running the backend:

```text
http://<computer-lan-ip>:8080/
```

## Google Maps

The app uses Google Maps for car location display. Add a Maps API key to `local.properties`:

```properties
MAPS_API_KEY=your_api_key_here
```

The value is passed to the Android manifest through Gradle.

## Run

From the project root:

```powershell
.\gradlew.bat assembleDebug
```

Then run the app from Android Studio or install the generated debug APK.

On Unix-like shells:

```bash
./gradlew assembleDebug
```

## Project Structure

```text
app/src/main/java/com/example/otomotoapp/
  ApiService.kt                 Retrofit API definitions
  CarRepository.kt              Network and mock-data repository
  MainViewModel.kt              Main app state, filters, pagination, details loading
  Navigation.kt                 Navigation graph
  RetrofitClient.kt             Retrofit builder
  data/                         Data models and preferences helper
  database/                     Room favourites database
  screen_elements/              Shared UI elements
  screens/                      App screens
  ui/                           UI formatting helpers and theme
```

## Development Notes

- `photoUrls` are used directly as image URLs.
- `photoPath` is converted to a backend image URL when `photoUrls` is unavailable.
- Favourite cars are stored locally in Room.
- Debug builds can fall back to `mock_cars.json` when API requests fail.
- UI display formatting is applied only on screen. Raw values from the API are preserved for filtering and requests.

## Roadmap

See:

```text
todo.txt
```
