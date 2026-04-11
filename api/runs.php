<?php

function store(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $data = input();

    $distanceKm  = (float)($data['distance_km'] ?? 0);
    $durationSec = (int)($data['duration_sec'] ?? 0);
    $startLat    = (float)($data['start_lat'] ?? 0);
    $startLng    = (float)($data['start_lng'] ?? 0);
    $endLat      = (float)($data['end_lat'] ?? 0);
    $endLng      = (float)($data['end_lng'] ?? 0);
    $routeJson   = $data['route_json'] ?? null;

    $calories = (int)($distanceKm * 70);
    $avgPace  = $distanceKm > 0 ? round(($durationSec / 60) / $distanceKm, 2) : null;

    $stmt = db()->prepare("
        INSERT INTO runs (user_id, distance_km, calories, duration_sec, start_lat, start_lng, end_lat, end_lng, route_json, avg_pace)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([$user['id'], $distanceKm, $calories, $durationSec, $startLat, $startLng, $endLat, $endLng, $routeJson, $avgPace]);
    $runId = (int)db()->lastInsertId();

    // Actualizar totales del usuario
    db()->prepare("UPDATE users SET total_km = total_km + ?, total_calories = total_calories + ? WHERE id = ?")
        ->execute([$distanceKm, $calories, $user['id']]);

    $stmt = db()->prepare("SELECT * FROM runs WHERE id = ?");
    $stmt->execute([$runId]);
    success($stmt->fetch(), 201);
}

function index(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("SELECT * FROM runs WHERE user_id = ? ORDER BY created_at DESC LIMIT 50");
    $stmt->execute([$user['id']]);
    success($stmt->fetchAll());
}

function show(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $id = (int)$_REQUEST['_id'];

    $stmt = db()->prepare("SELECT * FROM runs WHERE id = ? AND user_id = ?");
    $stmt->execute([$id, $user['id']]);
    $run = $stmt->fetch();

    if (!$run) error('Carrera no encontrada.', 404);
    success($run);
}

function destroy(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $id = (int)$_REQUEST['_id'];

    $stmt = db()->prepare("SELECT * FROM runs WHERE id = ? AND user_id = ?");
    $stmt->execute([$id, $user['id']]);
    $run = $stmt->fetch();

    if (!$run) error('Carrera no encontrada.', 404);

    // Restar totales del usuario
    db()->prepare("UPDATE users SET total_km = total_km - ?, total_calories = total_calories - ? WHERE id = ?")
        ->execute([$run['distance_km'], $run['calories'], $user['id']]);

    db()->prepare("DELETE FROM runs WHERE id = ?")->execute([$id]);
    success(['message' => 'Carrera eliminada.']);
}

function uploadPhoto(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $id = (int)$_REQUEST['_id'];

    $stmt = db()->prepare("SELECT * FROM runs WHERE id = ? AND user_id = ?");
    $stmt->execute([$id, $user['id']]);
    $run = $stmt->fetch();
    if (!$run) error('Carrera no encontrada.', 404);

    if (!isset($_FILES['photo'])) error('Foto requerida.', 422);

    $file = $_FILES['photo'];
    $ext  = pathinfo($file['name'], PATHINFO_EXTENSION);
    $name = 'run_' . $id . '_' . time() . '.' . $ext;
    $dest = __DIR__ . '/uploads/' . $name;

    if (!move_uploaded_file($file['tmp_name'], $dest)) {
        error('Error al subir la foto.', 500);
    }

    $url = '/api/uploads/' . $name;
    db()->prepare("UPDATE runs SET photo_url = ? WHERE id = ?")->execute([$url, $id]);

    success(['photo_url' => $url]);
}