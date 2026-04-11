<?php
// RunnerApp API — PHP puro (sin framework)
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

require_once __DIR__ . '/config.php';

// Parsear la ruta: /api/auth/login -> auth/login
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$uri = preg_replace('#^.*/api/#', '', $uri);
$uri = trim($uri, '/');
$method = $_SERVER['REQUEST_METHOD'];

// Rutas publicas (sin token)
$publicRoutes = [
    'POST auth/register'        => 'auth.php@register',
    'POST auth/login'           => 'auth.php@login',
    'POST auth/verify-email'    => 'auth.php@verifyEmail',
    'POST auth/resend-code'     => 'auth.php@resendCode',
    'POST auth/forgot-password' => 'auth.php@forgotPassword',
    'POST auth/reset-password'  => 'auth.php@resetPassword',
];

// Rutas protegidas (requieren Bearer token)
$protectedRoutes = [
    'POST auth/logout'        => 'auth.php@logout',

    'GET user/profile'        => 'user.php@profile',
    'POST user/update'        => 'user.php@update',
    'POST user/upload-photo'  => 'user.php@uploadPhoto',
    'GET users/by-country'    => 'user.php@byCountry',
    'POST user/fcm-token'     => 'user.php@updateFcmToken',
    'DELETE user/account'     => 'user.php@deleteAccount',

    'POST runs'               => 'runs.php@store',
    'GET runs'                => 'runs.php@index',

    'POST friends/request'    => 'friends.php@sendRequest',
    'POST friends/respond'    => 'friends.php@respond',
    'GET friends'             => 'friends.php@index',
    'GET friends/pending'     => 'friends.php@pending',

    'GET stats/monthly'         => 'stats.php@monthly',
    'GET stats/weekly-compare'  => 'stats.php@weeklyCompare',
    'GET stats/monthly-compare' => 'stats.php@monthlyCompare',
    'GET stats/leaderboard'     => 'stats.php@leaderboard',

    'GET charts/weekly'       => 'charts.php@weeklyHistory',
    'GET charts/monthly'      => 'charts.php@monthlyHistory',
];

// Rutas con parametros dinamicos (protegidas)
// GET runs/{id}, DELETE runs/{id}, POST runs/{id}/photo
$dynamicUser = null;

// Buscar en rutas publicas
$key = "$method $uri";
if (isset($publicRoutes[$key])) {
    dispatch($publicRoutes[$key]);
}

// Buscar en rutas protegidas
if (isset($protectedRoutes[$key])) {
    $dynamicUser = authUser();
    dispatch($protectedRoutes[$key]);
}

// Rutas dinamicas con parametros
if (preg_match('#^runs/(\d+)/photo$#', $uri, $m) && $method === 'POST') {
    $dynamicUser = authUser();
    $_REQUEST['_id'] = $m[1];
    dispatch('runs.php@uploadPhoto');
}
if (preg_match('#^runs/(\d+)$#', $uri, $m)) {
    $dynamicUser = authUser();
    $_REQUEST['_id'] = $m[1];
    if ($method === 'GET')    dispatch('runs.php@show');
    if ($method === 'DELETE') dispatch('runs.php@destroy');
}

// Ruta no encontrada
error('Ruta no encontrada: ' . $method . ' ' . $uri, 404);

// --- Dispatcher ---
function dispatch(string $route): void {
    [$file, $func] = explode('@', $route);
    require_once __DIR__ . '/' . $file;
    $func();
    exit;
}