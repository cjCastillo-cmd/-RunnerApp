<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\RunController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\FriendController;
use App\Http\Controllers\StatsController;
use App\Http\Controllers\ChartController;

// Rutas publicas con rate limiting (sin autenticacion)
Route::prefix('auth')->middleware('throttle:10,1')->group(function () {
    Route::post('register',         [AuthController::class, 'register']);
    Route::post('login',            [AuthController::class, 'login']);
    Route::post('verify-email',     [AuthController::class, 'verifyEmail']);
    Route::post('resend-code',      [AuthController::class, 'resendCode'])->middleware('throttle:3,1');
    Route::post('forgot-password',  [AuthController::class, 'forgotPassword'])->middleware('throttle:3,1');
    Route::post('reset-password',   [AuthController::class, 'resetPassword']);
});

// Rutas protegidas (requieren Bearer Token)
Route::middleware('auth:sanctum')->group(function () {

    // Auth
    Route::post('auth/logout', [AuthController::class, 'logout']);

    // Usuario / Perfil
    Route::get ('user/profile',        [UserController::class, 'profile']);
    Route::post('user/update',         [UserController::class, 'update']);
    Route::post('user/upload-photo',   [UserController::class, 'uploadPhoto']);
    Route::get ('users/by-country',    [UserController::class, 'byCountry']);

    // Carreras
    Route::post('runs',                [RunController::class, 'store']);
    Route::get ('runs',                [RunController::class, 'index']);
    Route::get ('runs/{id}',           [RunController::class, 'show']);
    Route::delete('runs/{id}',         [RunController::class, 'destroy']);

    // Amigos
    Route::post('friends/request',     [FriendController::class, 'sendRequest']);
    Route::post('friends/respond',     [FriendController::class, 'respond']);
    Route::get ('friends',             [FriendController::class, 'index']);
    Route::get ('friends/pending',     [FriendController::class, 'pending']);

    // Estadisticas
    Route::get ('stats/monthly',       [StatsController::class, 'monthly']);
    Route::get ('stats/weekly-compare',[StatsController::class, 'weeklyCompare']);
    Route::get ('stats/monthly-compare',[StatsController::class, 'monthlyCompare']);
    Route::get ('stats/leaderboard',   [StatsController::class, 'leaderboard']);

    // Charts
    Route::get ('charts/weekly',       [ChartController::class, 'weeklyHistory']);
    Route::get ('charts/monthly',      [ChartController::class, 'monthlyHistory']);

    // FCM token
    Route::post('user/fcm-token',      [UserController::class, 'updateFcmToken']);

    // Foto de carrera
    Route::post('runs/{id}/photo',     [RunController::class, 'uploadPhoto']);

    // Eliminar cuenta
    Route::delete('user/account',      [UserController::class, 'deleteAccount']);
});
