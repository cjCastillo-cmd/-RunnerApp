<?php

function profile(): void {
    global $dynamicUser;
    success($dynamicUser);
}

function update(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $data = input();

    $fields = [];
    $params = [];

    if (isset($data['name']) && trim($data['name']) !== '') {
        $fields[] = "name = ?";
        $params[] = trim($data['name']);
    }
    if (isset($data['country']) && trim($data['country']) !== '') {
        $fields[] = "country = ?";
        $params[] = trim($data['country']);
    }

    if (empty($fields)) error('Nada que actualizar.', 422);

    $params[] = $user['id'];
    db()->prepare("UPDATE users SET " . implode(', ', $fields) . " WHERE id = ?")
        ->execute($params);

    // Devolver usuario actualizado
    $stmt = db()->prepare("SELECT * FROM users WHERE id = ?");
    $stmt->execute([$user['id']]);
    success(userPublic($stmt->fetch()));
}

function uploadPhoto(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    if (!isset($_FILES['photo'])) error('Foto requerida.', 422);

    $file = $_FILES['photo'];
    $ext  = pathinfo($file['name'], PATHINFO_EXTENSION);
    $name = 'user_' . $user['id'] . '_' . time() . '.' . $ext;
    $dest = __DIR__ . '/uploads/' . $name;

    if (!move_uploaded_file($file['tmp_name'], $dest)) {
        error('Error al subir la foto.', 500);
    }

    $url = '/api/uploads/' . $name;
    db()->prepare("UPDATE users SET photo_url = ? WHERE id = ?")->execute([$url, $user['id']]);

    success(['photo_url' => $url]);
}

function byCountry(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    // IDs de amigos (para excluirlos)
    $stmt = db()->prepare("SELECT friend_id FROM friends WHERE user_id = ?");
    $stmt->execute([$user['id']]);
    $friendIds = array_column($stmt->fetchAll(), 'friend_id');
    $friendIds[] = $user['id'];

    $placeholders = implode(',', array_fill(0, count($friendIds), '?'));
    $params = array_merge([$user['country']], $friendIds);

    $stmt = db()->prepare("
        SELECT id, name, email, country, photo_url, total_km, total_calories, email_verified
        FROM users
        WHERE country = ? AND id NOT IN ($placeholders) AND email_verified = 1
        ORDER BY total_km DESC
        LIMIT 50
    ");
    $stmt->execute($params);
    $users = array_map(function($u) {
        $u['id'] = (int)$u['id'];
        $u['total_km'] = (float)$u['total_km'];
        $u['total_calories'] = (int)$u['total_calories'];
        $u['email_verified'] = (bool)$u['email_verified'];
        return $u;
    }, $stmt->fetchAll());
    success($users);
}

function updateFcmToken(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $data = input();
    $fcmToken = $data['fcm_token'] ?? '';

    if (!$fcmToken) error('fcm_token requerido.', 422);

    db()->prepare("UPDATE users SET fcm_token = ? WHERE id = ?")->execute([$fcmToken, $user['id']]);
    success(null, 200, 'Token actualizado');
}

function deleteAccount(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    db()->prepare("DELETE FROM runs WHERE user_id = ?")->execute([$user['id']]);
    db()->prepare("DELETE FROM friends WHERE user_id = ? OR friend_id = ?")->execute([$user['id'], $user['id']]);
    db()->prepare("DELETE FROM api_tokens WHERE user_id = ?")->execute([$user['id']]);
    db()->prepare("DELETE FROM users WHERE id = ?")->execute([$user['id']]);

    success(null, 200, 'Cuenta eliminada');
}