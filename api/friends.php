<?php

function sendRequest(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $data = input();

    $friendId = (int)($data['friend_id'] ?? 0);
    if (!$friendId) error('friend_id requerido.', 422);
    if ($friendId == $user['id']) error('No puedes agregarte a ti mismo.', 400);

    // Verificar que el usuario existe
    $stmt = db()->prepare("SELECT id FROM users WHERE id = ?");
    $stmt->execute([$friendId]);
    if (!$stmt->fetch()) error('Usuario no encontrado.', 404);

    // Verificar si ya existe la solicitud
    $stmt = db()->prepare("SELECT id FROM friends WHERE user_id = ? AND friend_id = ?");
    $stmt->execute([$user['id'], $friendId]);
    if ($stmt->fetch()) error('Solicitud ya enviada.', 409);

    // Verificar relacion inversa
    $stmt = db()->prepare("SELECT id FROM friends WHERE user_id = ? AND friend_id = ?");
    $stmt->execute([$friendId, $user['id']]);
    if ($stmt->fetch()) error('Este usuario ya te envio una solicitud.', 409);

    $stmt = db()->prepare("INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending')");
    $stmt->execute([$user['id'], $friendId]);

    $id = (int)db()->lastInsertId();
    $stmt = db()->prepare("SELECT * FROM friends WHERE id = ?");
    $stmt->execute([$id]);
    success($stmt->fetch(), 201);
}

function respond(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $data = input();

    $requestId = (int)($data['request_id'] ?? 0);
    $action    = $data['action'] ?? '';

    if (!$requestId || !in_array($action, ['accepted', 'rejected'])) {
        error('Campos requeridos: request_id, action (accepted/rejected).', 422);
    }

    $stmt = db()->prepare("SELECT * FROM friends WHERE id = ? AND friend_id = ?");
    $stmt->execute([$requestId, $user['id']]);
    $friendship = $stmt->fetch();

    if (!$friendship) error('Solicitud no encontrada.', 404);

    db()->prepare("UPDATE friends SET status = ? WHERE id = ?")->execute([$action, $requestId]);

    $stmt = db()->prepare("SELECT * FROM friends WHERE id = ?");
    $stmt->execute([$requestId]);
    success($stmt->fetch());
}

function index(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT f.*, u.id AS friend_user_id, u.name, u.country, u.photo_url, u.total_km
        FROM friends f
        JOIN users u ON u.id = f.friend_id
        WHERE f.user_id = ? AND f.status = 'accepted'
    ");
    $stmt->execute([$user['id']]);
    $rows = $stmt->fetchAll();

    $result = array_map(function($row) {
        return [
            'id'        => $row['id'],
            'user_id'   => $row['user_id'],
            'friend_id' => $row['friend_id'],
            'status'    => $row['status'],
            'created_at'=> $row['created_at'],
            'updated_at'=> $row['updated_at'],
            'friend'    => [
                'id'        => $row['friend_user_id'],
                'name'      => $row['name'],
                'country'   => $row['country'],
                'photo_url' => $row['photo_url'],
                'total_km'  => $row['total_km'],
            ],
        ];
    }, $rows);

    success($result);
}

function pending(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT f.*, u.id AS sender_id, u.name, u.photo_url
        FROM friends f
        JOIN users u ON u.id = f.user_id
        WHERE f.friend_id = ? AND f.status = 'pending'
    ");
    $stmt->execute([$user['id']]);
    $rows = $stmt->fetchAll();

    $result = array_map(function($row) {
        return [
            'id'        => $row['id'],
            'user_id'   => $row['user_id'],
            'friend_id' => $row['friend_id'],
            'status'    => $row['status'],
            'created_at'=> $row['created_at'],
            'updated_at'=> $row['updated_at'],
            'user'      => [
                'id'        => $row['sender_id'],
                'name'      => $row['name'],
                'photo_url' => $row['photo_url'],
            ],
        ];
    }, $rows);

    success($result);
}