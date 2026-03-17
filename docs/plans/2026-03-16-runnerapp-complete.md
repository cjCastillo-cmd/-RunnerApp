# RunnerApp — Plan Completo de Mejoras

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Completar todas las features faltantes de RunnerApp: graficos, push notifications, fotos en carreras, ViewModels (MVVM), dark/light mode, i18n, tests instrumentados, CI/CD y preparacion Play Store.

**Architecture:** El backend Laravel ya esta completo en `C:\xampp\htdocs\runner_backend`. Todo el trabajo es Android-side salvo push notifications que requiere cambios en ambos lados. Se sigue el patron MVVM con Repository, migrando la logica de Activities/Fragments a ViewModels.

**Tech Stack:** Kotlin, Retrofit, Room, Google Maps, MPAndroidChart (graficos), Firebase Cloud Messaging (push), Espresso (tests UI), GitHub Actions (CI/CD)

---

## FASE 1: Graficos de Progreso (MPAndroidChart)

### Task 1.1: Agregar dependencia MPAndroidChart

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `settings.gradle.kts` (agregar JitPack si no esta)

**Step 1: Agregar repositorio JitPack**

En `settings.gradle.kts`, dentro de `dependencyResolutionManagement.repositories`:
```kotlin
maven { url = uri("https://jitpack.io") }
```

**Step 2: Agregar dependencia**

En `app/build.gradle.kts`:
```kotlin
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

**Step 3: Sync Gradle**

Run: `./gradlew app:dependencies --configuration releaseRuntimeClasspath | grep -i chart`
Expected: MPAndroidChart aparece en el arbol

**Step 4: Commit**
```bash
git add app/build.gradle.kts settings.gradle.kts
git commit -m "Agregar dependencia MPAndroidChart para graficos"
```

---

### Task 1.2: Agregar endpoints de historial semanal al backend

**Files:**
- Create: `C:\xampp\htdocs\runner_backend\app\Http\Controllers\ChartController.php`
- Modify: `C:\xampp\htdocs\runner_backend\routes\api.php`

**Step 1: Crear ChartController**

```php
<?php

namespace App\Http\Controllers;

use App\Models\Run;
use Carbon\Carbon;
use Illuminate\Http\Request;

class ChartController extends Controller
{
    // Km por dia de las ultimas 4 semanas
    public function weeklyHistory(Request $request)
    {
        $user = $request->user();
        $startDate = Carbon::now()->subWeeks(4)->startOfDay();

        $runs = Run::where('user_id', $user->id)
            ->where('created_at', '>=', $startDate)
            ->selectRaw('DATE(created_at) as date, SUM(distance_km) as km, COUNT(*) as runs')
            ->groupBy('date')
            ->orderBy('date')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $runs
        ]);
    }

    // Km por mes de los ultimos 6 meses
    public function monthlyHistory(Request $request)
    {
        $user = $request->user();
        $startDate = Carbon::now()->subMonths(6)->startOfMonth();

        $runs = Run::where('user_id', $user->id)
            ->where('created_at', '>=', $startDate)
            ->selectRaw("DATE_FORMAT(created_at, '%Y-%m') as month, SUM(distance_km) as km, COUNT(*) as runs, SUM(calories) as calories")
            ->groupBy('month')
            ->orderBy('month')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $runs
        ]);
    }
}
```

**Step 2: Agregar rutas**

En `routes/api.php`, dentro del grupo auth:sanctum:
```php
Route::get('charts/weekly', [ChartController::class, 'weeklyHistory']);
Route::get('charts/monthly', [ChartController::class, 'monthlyHistory']);
```

**Step 3: Commit**
```bash
cd C:\xampp\htdocs\runner_backend
git add -A && git commit -m "Agregar endpoints de charts: historial semanal y mensual"
```

---

### Task 1.3: Agregar endpoints de charts al ApiService Android

**Files:**
- Modify: `app/src/main/java/com/gymnasioforce/runnerapp/network/ApiService.kt`
- Modify: `app/src/main/java/com/gymnasioforce/runnerapp/network/ApiModels.kt`

**Step 1: Agregar modelos de datos**

En `ApiModels.kt`:
```kotlin
data class DailyChartData(
    @SerializedName("date") val date: String,
    @SerializedName("km") val km: Double,
    @SerializedName("runs") val runs: Int
)

data class MonthlyChartData(
    @SerializedName("month") val month: String,
    @SerializedName("km") val km: Double,
    @SerializedName("runs") val runs: Int,
    @SerializedName("calories") val calories: Int
)
```

**Step 2: Agregar endpoints**

En `ApiService.kt`:
```kotlin
@GET("charts/weekly")
suspend fun getWeeklyChart(): Response<ApiResponse<List<DailyChartData>>>

@GET("charts/monthly")
suspend fun getMonthlyChart(): Response<ApiResponse<List<MonthlyChartData>>>
```

**Step 3: Commit**
```bash
git add -A && git commit -m "Agregar modelos y endpoints de charts al ApiService"
```

---

### Task 1.4: Crear pantalla de graficos en StatsFragment

**Files:**
- Modify: `app/src/main/res/layout/fragment_stats.xml`
- Modify: `app/src/main/java/com/gymnasioforce/runnerapp/ui/stats/StatsFragment.kt`

**Step 1: Agregar charts al layout**

Agregar debajo de las comparaciones existentes en `fragment_stats.xml`:
```xml
<!-- Grafico semanal: barras de km por dia -->
<TextView
    android:id="@+id/tvWeeklyChartTitle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/weekly_distance"
    android:textColor="@color/text_primary"
    android:textSize="18sp"
    android:fontFamily="sans-serif-medium"
    android:layout_marginTop="24dp" />

<com.github.mikephil.charting.charts.BarChart
    android:id="@+id/chartWeekly"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:layout_marginTop="8dp" />

<!-- Grafico mensual: linea de km por mes -->
<TextView
    android:id="@+id/tvMonthlyChartTitle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/monthly_progress"
    android:textColor="@color/text_primary"
    android:textSize="18sp"
    android:fontFamily="sans-serif-medium"
    android:layout_marginTop="24dp" />

<com.github.mikephil.charting.charts.LineChart
    android:id="@+id/chartMonthly"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="24dp" />
```

**Step 2: Implementar carga de datos y configuracion de charts**

En `StatsFragment.kt`, agregar funciones para configurar los charts con el estilo oscuro de la app (fondo transparente, texto claro, grid deshabilitado, colores de acento #4D8AFF).

- BarChart: Barras azul electrico, labels con dia de semana, animacion
- LineChart: Linea azul con fill gradient, markers con mes abreviado

**Step 3: Agregar strings**

En `strings.xml`:
```xml
<string name="weekly_distance">Distancia semanal</string>
<string name="monthly_progress">Progreso mensual</string>
```

**Step 4: Commit**
```bash
git add -A && git commit -m "Agregar graficos de progreso semanal y mensual en Stats"
```

---

## FASE 2: Push Notifications (Firebase Cloud Messaging)

### Task 2.1: Configurar Firebase en el proyecto Android

**Files:**
- Modify: `build.gradle.kts` (project-level)
- Modify: `app/build.gradle.kts`
- Create: `app/google-services.json` (desde Firebase Console)

**Step 1: Agregar plugin de Google Services**

Project-level `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

App-level `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}
```

**Step 2: Crear proyecto en Firebase Console**
- Ir a console.firebase.google.com
- Crear proyecto "RunnerApp"
- Agregar app Android con package `com.gymnasioforce.runnerapp`
- Descargar `google-services.json` a `app/`

**Step 3: Sync y verificar**

Run: `./gradlew app:dependencies | grep firebase`

**Step 4: Commit**
```bash
git add app/build.gradle.kts build.gradle.kts
# NO commitear google-services.json (contiene API keys)
git commit -m "Configurar Firebase y dependencia FCM"
```

---

### Task 2.2: Crear servicio FCM en Android

**Files:**
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/services/FCMService.kt`
- Modify: `AndroidManifest.xml`

**Step 1: Crear FCMService**

```kotlin
package com.gymnasioforce.runnerapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.main.MainActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userToken = Prefs.getToken(this)
        if (userToken != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.getInstance(this@FCMService)
                        .apiService.updateFcmToken(mapOf("fcm_token" to token))
                } catch (_: Exception) {}
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "social_notifications"
        val nm = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notificaciones sociales", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

**Step 2: Registrar en AndroidManifest.xml**

```xml
<service
    android:name=".services.FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**Step 3: Agregar endpoint al ApiService**

```kotlin
@POST("user/fcm-token")
suspend fun updateFcmToken(@Body body: Map<String, String>): Response<ApiResponse<Any>>
```

**Step 4: Enviar token al login**

En `LoginActivity`, despues del login exitoso:
```kotlin
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    lifecycleScope.launch {
        try { api.updateFcmToken(mapOf("fcm_token" to token)) } catch (_: Exception) {}
    }
}
```

**Step 5: Commit**
```bash
git add -A && git commit -m "Agregar servicio FCM para push notifications"
```

---

### Task 2.3: Agregar soporte FCM al backend Laravel

**Files:**
- Create migration: `add_fcm_token_to_users`
- Modify: `UserController.php`
- Create: `app/Services/PushNotificationService.php`
- Modify: `FriendController.php` (notificar al aceptar/enviar request)

**Step 1: Migration**
```bash
cd C:\xampp\htdocs\runner_backend
php artisan make:migration add_fcm_token_to_users_table
```

```php
Schema::table('users', function (Blueprint $table) {
    $table->string('fcm_token')->nullable()->after('photo_url');
});
```

**Step 2: Endpoint para guardar token**

En `routes/api.php`:
```php
Route::post('user/fcm-token', [UserController::class, 'updateFcmToken']);
```

En `UserController.php`:
```php
public function updateFcmToken(Request $request)
{
    $request->validate(['fcm_token' => 'required|string']);
    $request->user()->update(['fcm_token' => $request->fcm_token]);
    return response()->json(['success' => true]);
}
```

**Step 3: Crear PushNotificationService**

Servicio que envia push via HTTP a FCM v1 API usando la server key de Firebase.

**Step 4: Integrar en FriendController**

- Al enviar request: notificar al receptor "X quiere ser tu amigo"
- Al aceptar request: notificar al emisor "X acepto tu solicitud"

**Step 5: Commit**
```bash
git add -A && git commit -m "Agregar soporte FCM al backend: token storage y push service"
```

---

## FASE 3: Fotos en Carreras

### Task 3.1: Agregar campo de foto al modelo Run

**Files:**
- Create migration en Laravel: `add_photo_to_runs`
- Modify: `RunController.php`
- Modify: `ApiModels.kt` (Android)
- Modify: `ApiService.kt` (Android)

**Step 1: Migration backend**

```php
Schema::table('runs', function (Blueprint $table) {
    $table->string('photo_url')->nullable()->after('route_json');
});
```

**Step 2: Endpoint para subir foto de carrera**

En `RunController.php`, nuevo metodo:
```php
public function uploadPhoto(Request $request, $id)
{
    $run = Run::where('id', $id)->where('user_id', $request->user()->id)->firstOrFail();
    $request->validate(['photo' => 'required|image|max:5120']);
    $path = $request->file('photo')->store('run_photos', 'public');
    $run->update(['photo_url' => '/storage/' . $path]);
    return response()->json(['success' => true, 'data' => ['photo_url' => $run->photo_url]]);
}
```

**Step 3: Ruta backend**
```php
Route::post('runs/{id}/photo', [RunController::class, 'uploadPhoto']);
```

**Step 4: Android - agregar al ApiService**
```kotlin
@Multipart
@POST("runs/{id}/photo")
suspend fun uploadRunPhoto(
    @Path("id") runId: Int,
    @Part photo: MultipartBody.Part
): Response<ApiResponse<Map<String, String>>>
```

**Step 5: Agregar photoUrl al modelo Run**
```kotlin
data class Run(
    // ... campos existentes ...
    @SerializedName("photo_url") val photoUrl: String? = null
)
```

**Step 6: Commit**
```bash
git add -A && git commit -m "Agregar soporte para fotos en carreras"
```

---

### Task 3.2: UI para capturar/seleccionar foto en RunSummaryActivity

**Files:**
- Modify: `app/src/main/res/layout/activity_run_summary.xml`
- Modify: `app/src/main/java/com/gymnasioforce/runnerapp/ui/run/RunSummaryActivity.kt`

**Step 1: Agregar boton de foto al layout**

Agregar un boton con icono de camara entre las stats y los botones de compartir/cerrar.

**Step 2: Implementar seleccion de foto**

Usar ActivityResultContracts para abrir camara o galeria. Subir foto al endpoint `runs/{id}/photo` despues de guardar la carrera.

**Step 3: Mostrar foto en RunDetailActivity**

Cargar con Glide si `photoUrl` no es null.

**Step 4: Commit**
```bash
git add -A && git commit -m "Agregar UI para capturar y mostrar fotos de carreras"
```

---

## FASE 4: ViewModels y Arquitectura MVVM

### Task 4.1: Crear Repository layer

**Files:**
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/data/repository/RunRepository.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/data/repository/UserRepository.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/data/repository/FriendRepository.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/data/repository/StatsRepository.kt`

**Step 1: RunRepository**

```kotlin
package com.gymnasioforce.runnerapp.data.repository

import android.content.Context
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.data.local.RunEntity
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.Run
import com.gymnasioforce.runnerapp.network.SaveRunRequest

class RunRepository(context: Context) {
    private val api = RetrofitClient.getInstance(context).apiService
    private val dao = AppDatabase.getInstance(context).runDao()

    suspend fun getRuns(): Result<List<Run>> {
        return try {
            val response = api.getRuns()
            if (response.isSuccessful && response.body()?.success == true) {
                val runs = response.body()!!.data!!
                // Cache local
                dao.deleteAll()
                dao.insertAll(runs.map { it.toEntity() })
                Result.success(runs)
            } else {
                // Fallback a cache local
                Result.success(dao.getAll().map { it.toRun() })
            }
        } catch (e: Exception) {
            val cached = dao.getAll()
            if (cached.isNotEmpty()) Result.success(cached.map { it.toRun() })
            else Result.failure(e)
        }
    }

    suspend fun saveRun(request: SaveRunRequest): Result<Run> {
        return try {
            val response = api.saveRun(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Error al guardar carrera"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRun(id: Int): Result<Unit> {
        return try {
            val response = api.deleteRun(id)
            if (response.isSuccessful) {
                dao.getById(id)?.let { dao.delete(it) }
                Result.success(Unit)
            } else Result.failure(Exception("Error al eliminar"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
```

Implementar `UserRepository`, `FriendRepository`, `StatsRepository` con patrones similares.

**Step 2: Commit**
```bash
git add -A && git commit -m "Crear capa Repository para acceso a datos"
```

---

### Task 4.2: Crear ViewModels

**Files:**
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/ui/main/HomeViewModel.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/ui/stats/StatsViewModel.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/ui/friends/FriendsViewModel.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/ui/profile/ProfileViewModel.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/ui/run/RunViewModel.kt`
- Create: `app/src/main/java/com/gymnasioforce/runnerapp/ui/auth/AuthViewModel.kt`

**Step 1: HomeViewModel**

```kotlin
package com.gymnasioforce.runnerapp.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.gymnasioforce.runnerapp.data.repository.RunRepository
import com.gymnasioforce.runnerapp.data.repository.StatsRepository
import com.gymnasioforce.runnerapp.network.Run
import com.gymnasioforce.runnerapp.network.MonthlyStats
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val runRepo = RunRepository(application)
    private val statsRepo = StatsRepository(application)

    private val _runs = MutableLiveData<List<Run>>()
    val runs: LiveData<List<Run>> = _runs

    private val _monthlyStats = MutableLiveData<MonthlyStats?>()
    val monthlyStats: LiveData<MonthlyStats?> = _monthlyStats

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            runRepo.getRuns()
                .onSuccess { _runs.value = it }
                .onFailure { _error.value = it.message }

            statsRepo.getMonthlyStats()
                .onSuccess { _monthlyStats.value = it }

            _loading.value = false
        }
    }
}
```

Repetir patron para cada ViewModel: LiveData para estado, metodos para acciones, Repository para datos.

**Step 2: Migrar Fragments para usar ViewModels**

Ejemplo `HomeFragment.kt`:
```kotlin
private val viewModel: HomeViewModel by viewModels()

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.runs.observe(viewLifecycleOwner) { runs ->
        adapter.submitList(runs)
    }
    viewModel.loading.observe(viewLifecycleOwner) { loading ->
        binding.swipeRefresh.isRefreshing = loading
    }
    viewModel.error.observe(viewLifecycleOwner) { error ->
        error?.let { showToast(it) }
    }

    binding.swipeRefresh.setOnRefreshListener { viewModel.loadData() }
    viewModel.loadData()
}
```

**Step 3: Commit por cada ViewModel migrado**

---

## FASE 5: Dark/Light Mode Toggle

### Task 5.1: Crear tema claro

**Files:**
- Create: `app/src/main/res/values-night/colors.xml` (ya existe como default oscuro)
- Modify: `app/src/main/res/values/colors.xml` (convertir a tema claro)
- Create: `app/src/main/res/values/colors_light.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/java/com/gymnasioforce/runnerapp/utils/Prefs.kt`

**Step 1: Reestructurar colores**

Mover colores oscuros actuales a `values-night/colors.xml`. Crear colores claros en `values/colors.xml`:

```xml
<!-- values/colors.xml (tema claro) -->
<color name="background">#FAFBFC</color>
<color name="surface">#FFFFFF</color>
<color name="surface_border">#E1E4E8</color>
<color name="text_primary">#1F2328</color>
<color name="text_secondary">#656D76</color>
<color name="accent_volt">#2563EB</color>
<!-- accent se mantiene azul para coherencia -->
```

```xml
<!-- values-night/colors.xml (tema oscuro existente) -->
<color name="background">#0D1117</color>
<color name="surface">#161B22</color>
<!-- ... colores actuales ... -->
```

**Step 2: Guardar preferencia de tema en Prefs**

```kotlin
fun getThemeMode(context: Context): Int {
    return getPrefs(context).getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

fun setThemeMode(context: Context, mode: Int) {
    getPrefs(context).edit().putInt("theme_mode", mode).apply()
}
```

**Step 3: Aplicar tema en Application/BaseActivity**

```kotlin
AppCompatDelegate.setDefaultNightMode(Prefs.getThemeMode(this))
```

**Step 4: Agregar toggle en ProfileFragment**

Agregar un switch o selector de 3 opciones: Claro / Oscuro / Sistema.

**Step 5: Commit**
```bash
git add -A && git commit -m "Agregar soporte dark/light mode con toggle en perfil"
```

---

## FASE 6: Internacionalizacion (i18n)

### Task 6.1: Crear strings en ingles

**Files:**
- Create: `app/src/main/res/values-en/strings.xml`

**Step 1: Copiar strings.xml y traducir al ingles**

Copiar todo `values/strings.xml` a `values-en/strings.xml` y traducir cada string al ingles. El espanol queda como idioma por defecto.

**Step 2: Traducir array de paises**

El array `country_list` tambien necesita traduccion (o dejarlo en espanol si los nombres son iguales/similares).

**Step 3: Verificar que no hay strings hardcodeadas**

Run: `./gradlew lint` — buscar warnings de "Hardcoded text"

**Step 4: Commit**
```bash
git add -A && git commit -m "Agregar traduccion al ingles (i18n)"
```

---

## FASE 7: Tests Instrumentados (Espresso)

### Task 7.1: Configurar Espresso y test de Login

**Files:**
- Modify: `app/build.gradle.kts` (dependencias de test)
- Create: `app/src/androidTest/java/com/gymnasioforce/runnerapp/ui/auth/LoginActivityTest.kt`

**Step 1: Agregar dependencias**

```kotlin
androidTestImplementation("androidx.test:runner:1.6.2")
androidTestImplementation("androidx.test:rules:1.6.1")
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
```

**Step 2: Test de LoginActivity**

```kotlin
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginForm_displaysAllElements() {
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun emptyEmail_showsError() {
        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.tilEmail)).check(matches(hasDescendant(withText(R.string.error_email_required))))
    }

    @Test
    fun invalidEmail_showsError() {
        onView(withId(R.id.etEmail)).perform(typeText("notanemail"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.tilEmail)).check(matches(hasDescendant(withText(R.string.error_email_invalid))))
    }
}
```

**Step 3: Commit**
```bash
git add -A && git commit -m "Agregar tests instrumentados de LoginActivity"
```

---

### Task 7.2: Tests de navegacion principal

**Files:**
- Create: `app/src/androidTest/java/com/gymnasioforce/runnerapp/ui/main/MainNavigationTest.kt`

Test que verifica:
- Bottom navigation muestra 4 tabs
- Click en cada tab muestra el fragment correcto
- FAB es visible y clickeable

**Commit**: `"Agregar tests de navegacion principal"`

---

### Task 7.3: Tests de RunSummaryActivity

**Files:**
- Create: `app/src/androidTest/java/com/gymnasioforce/runnerapp/ui/run/RunSummaryTest.kt`

Test que verifica:
- Stats se muestran correctamente
- Boton compartir lanza intent
- Boton cerrar finaliza activity

**Commit**: `"Agregar tests de RunSummaryActivity"`

---

## FASE 8: CI/CD (GitHub Actions)

### Task 8.1: Crear workflow de build y test

**Files:**
- Create: `.github/workflows/android.yml`

**Step 1: Crear workflow**

```yaml
name: Android CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}

      - name: Grant execute permission
        run: chmod +x gradlew

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk

  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: chmod +x gradlew
      - run: ./gradlew lint
      - uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html
```

**Step 2: Commit**
```bash
git add .github/workflows/android.yml
git commit -m "Agregar CI/CD con GitHub Actions: build, test y lint"
```

---

## FASE 9: Preparacion Play Store

### Task 9.1: Crear politica de privacidad

**Files:**
- Create: `docs/privacy-policy.html`

Politica basica que cubre:
- Datos recolectados (ubicacion GPS, email, nombre, pais)
- Uso de datos (tracking de carreras, estadisticas, social)
- Almacenamiento (servidor propio, no terceros)
- Derechos del usuario (eliminar cuenta, exportar datos)

**Commit**: `"Agregar politica de privacidad"`

---

### Task 9.2: Optimizar assets para Play Store

**Files:**
- Verify: `app/src/main/res/mipmap-*` (iconos en todas las densidades)
- Create: `docs/store/` (screenshots, feature graphic, descripcion)

**Step 1: Verificar iconos adaptivos**

Asegurarse de que existen `ic_launcher.xml` (adaptive icon) en `mipmap-anydpi-v26/`.

**Step 2: Crear metadata de Play Store**

```
docs/store/
├── description_es.txt    (descripcion corta y larga)
├── description_en.txt    (traduccion)
├── feature-graphic.png   (1024x500)
├── screenshots/          (min 2, max 8 por idioma)
└── changelog.txt         (notas de version)
```

**Step 3: Verificar versionCode y versionName**

En `app/build.gradle.kts`:
```kotlin
versionCode = 1
versionName = "1.0.0"
```

**Step 4: Commit**
```bash
git add -A && git commit -m "Preparar assets y metadata para Play Store"
```

---

### Task 9.3: Endpoint para eliminar cuenta (requerido por Play Store)

**Files:**
- Modify: `C:\xampp\htdocs\runner_backend\routes\api.php`
- Modify: `C:\xampp\htdocs\runner_backend\app\Http\Controllers\UserController.php`
- Modify: `ApiService.kt` (Android)
- Modify: `ProfileFragment.kt` (agregar boton)

**Step 1: Backend - endpoint de eliminacion**

```php
public function deleteAccount(Request $request)
{
    $user = $request->user();
    $user->runs()->delete();
    $user->tokens()->delete();
    Friend::where('user_id', $user->id)->orWhere('friend_id', $user->id)->delete();
    $user->delete();
    return response()->json(['success' => true, 'message' => 'Cuenta eliminada']);
}
```

Ruta: `DELETE /user/account`

**Step 2: Android - agregar al perfil**

Boton rojo "Eliminar cuenta" con dialogo de confirmacion.

**Step 3: Commit**
```bash
git add -A && git commit -m "Agregar eliminacion de cuenta (requerido Play Store)"
```

---

## RESUMEN DE FASES

| Fase | Descripcion | Tasks | Prioridad |
|------|-------------|-------|-----------|
| 1 | Graficos (MPAndroidChart) | 4 | Alta |
| 2 | Push Notifications (FCM) | 3 | Alta |
| 3 | Fotos en carreras | 2 | Media |
| 4 | ViewModels / MVVM | 2 | Media |
| 5 | Dark/Light mode | 1 | Media |
| 6 | i18n (ingles) | 1 | Baja |
| 7 | Tests instrumentados | 3 | Media |
| 8 | CI/CD (GitHub Actions) | 1 | Baja |
| 9 | Play Store prep | 3 | Alta |

**Total: 20 tasks, ~9 fases**

---

## ORDEN DE EJECUCION RECOMENDADO

1. **Fase 4** (ViewModels) — base arquitectonica, facilita todo lo demas
2. **Fase 1** (Graficos) — feature visible de alto impacto
3. **Fase 5** (Dark/Light mode) — mejora UX rapida
4. **Fase 3** (Fotos en carreras) — feature social
5. **Fase 2** (Push notifications) — requiere Firebase Console setup
6. **Fase 6** (i18n) — traduccion mecanica
7. **Fase 7** (Tests) — calidad
8. **Fase 8** (CI/CD) — automatizacion
9. **Fase 9** (Play Store) — release final
