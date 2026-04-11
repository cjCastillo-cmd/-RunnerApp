<?php

function register(): void {
    $data = input();
    $name     = trim($data['name'] ?? '');
    $email    = trim($data['email'] ?? '');
    $password = $data['password'] ?? '';
    $country  = trim($data['country'] ?? '');

    if (!$name || !$email || !$password || !$country) {
        error('Campos requeridos: name, email, password, country.', 422);
    }

    // Verificar email unico
    $stmt = db()->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) error('El email ya esta registrado.', 422);

    $code = rand(100000, 999999);
    $hash = password_hash($password, PASSWORD_BCRYPT);

    $stmt = db()->prepare("
        INSERT INTO users (name, email, password, country, verification_code, email_verified)
        VALUES (?, ?, ?, ?, ?, 0)
    ");
    $stmt->execute([$name, $email, $hash, $country, (string)$code]);
    $userId = (int)db()->lastInsertId();

    // Auto-verificar para demo
    db()->prepare("UPDATE users SET email_verified = 1, verification_code = NULL WHERE id = ?")
        ->execute([$userId]);

    $token = createToken($userId);

    $stmt = db()->prepare("SELECT * FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $newUser = userPublic($stmt->fetch());

    success(['token' => $token, 'user' => $newUser], 201);
}

function verifyEmail(): void {
    $data  = input();
    $email = trim($data['email'] ?? '');
    $code  = trim($data['code'] ?? '');

    if (!$email || !$code) error('Campos requeridos: email, code.', 422);

    $stmt = db()->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user) error('Usuario no encontrado.', 404);
    if ($user['email_verified']) {
        $token = createToken($user['id']);
        success(['token' => $token, 'user' => userPublic($user)]);
    }
    if ($user['verification_code'] !== $code) {
        error('Codigo de verificacion incorrecto.', 400);
    }

    db()->prepare("UPDATE users SET email_verified = 1, verification_code = NULL WHERE id = ?")
        ->execute([$user['id']]);

    $token = createToken($user['id']);
    $user['email_verified'] = 1;
    success(['token' => $token, 'user' => userPublic($user)]);
}

function resendCode(): void {
    $data  = input();
    $email = trim($data['email'] ?? '');

    $stmt = db()->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    if (!$user) error('Usuario no encontrado.', 404);

    $code = rand(100000, 999999);
    db()->prepare("UPDATE users SET verification_code = ? WHERE id = ?")
        ->execute([(string)$code, $user['id']]);

    $response = ['message' => 'Codigo reenviado.'];
    if (DEBUG_MODE) $response['debug_code'] = $code;

    success($response);
}

function login(): void {
    $data     = input();
    $email    = trim($data['email'] ?? '');
    $password = $data['password'] ?? '';

    if (!$email || !$password) error('Campos requeridos: email, password.', 422);

    $stmt = db()->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user || !password_hash_verify($password, $user['password'])) {
        error('Credenciales incorrectas.', 401);
    }
    if (!$user['email_verified']) {
        error('Debes verificar tu correo electronico.', 403);
    }

    // Eliminar tokens anteriores y crear uno nuevo
    deleteUserTokens($user['id']);
    $token = createToken($user['id']);

    success(['token' => $token, 'user' => userPublic($user)]);
}

function logout(): void {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    preg_match('/^Bearer\s+(.+)$/i', $header, $m);
    $token = $m[1] ?? '';

    db()->prepare("DELETE FROM api_tokens WHERE token = ?")->execute([$token]);
    success(['message' => 'Sesion cerrada.']);
}

function forgotPassword(): void {
    $data  = input();
    $email = trim($data['email'] ?? '');

    $stmt = db()->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    if (!$user) error('Correo no registrado.', 404);

    $token = bin2hex(random_bytes(30));
    $hash  = password_hash($token, PASSWORD_BCRYPT);

    db()->prepare("REPLACE INTO password_resets (email, token, created_at) VALUES (?, ?, NOW())")
        ->execute([$email, $hash]);

    $response = ['message' => 'Token generado para restablecer contrasena.'];
    if (DEBUG_MODE) $response['debug_token'] = $token;

    success($response);
}

function resetPassword(): void {
    $data     = input();
    $email    = trim($data['email'] ?? '');
    $token    = $data['token'] ?? '';
    $password = $data['password'] ?? '';

    if (!$email || !$token || !$password) {
        error('Campos requeridos: email, token, password.', 422);
    }

    $stmt = db()->prepare("SELECT * FROM password_resets WHERE email = ?");
    $stmt->execute([$email]);
    $record = $stmt->fetch();

    if (!$record || !password_hash_verify($token, $record['token'])) {
        error('Token invalido o expirado.', 400);
    }

    $hash = password_hash($password, PASSWORD_BCRYPT);
    db()->prepare("UPDATE users SET password = ? WHERE email = ?")->execute([$hash, $email]);
    db()->prepare("DELETE FROM password_resets WHERE email = ?")->execute([$email]);

    success(['message' => 'Contrasena actualizada correctamente.']);
}

// Helper para verificar password (compatible con Hash::check de Laravel)
function password_hash_verify(string $plain, string $hashed): bool {
    return password_verify($plain, $hashed);
}