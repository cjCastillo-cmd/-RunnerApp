# RunnerApp — Contexto del Proyecto

## Stack tecnico
- **Lenguaje**: Kotlin (JVM 17)
- **UI**: XML con ViewBinding (no Compose)
- **Arquitectura**: MVVM (ViewModel + LiveData + Repository)
- **DI**: Manual (sin Hilt/Koin)
- **Base de datos local**: Room (KSP)
- **Networking**: Retrofit + OkHttp + Gson
- **Mapas**: osmdroid (OpenStreetMap, reemplazo de Google Maps por ser gratuito)
- **GPS**: FusedLocationProvider (play-services-location)
- **Graficos**: MPAndroidChart
- **Imagenes**: Glide
- **Backend**: Laravel (PHP) en `C:\xampp\htdocs\runner_backend`
- **Min SDK**: 24 | **Target SDK**: 36

## Estructura del proyecto
```
ui/          -> Activities y ViewModels por feature (auth, main, run, stats, friends, profile, splash)
data/        -> Room entities, DAOs, repositories, SyncManager
network/     -> Retrofit API definitions
services/    -> RunningService (GPS foreground), FCMService (push)
utils/       -> Helpers (NetworkHelper, etc.)
```

## Convenciones
- Cada feature tiene su propio paquete en `ui/`
- ViewModels exponen LiveData, no StateFlow
- Repositorios manejan la logica de datos (local + remoto)
- BASE_URL debug: `http://192.168.1.156/runner_backend/public/api/`
- Navegacion entre Activities (no Navigation Component single-activity)

## Testing
- Unit tests: JUnit 4
- Instrumented tests: AndroidJUnit + Espresso
- Runner: `./gradlew test` (unit) | `./gradlew connectedAndroidTest` (instrumented)

## Build
- `./gradlew assembleDebug` — build debug
- `./gradlew assembleRelease` — build release (firmado con runner-release.jks)
- `./gradlew lint` — analisis estatico
