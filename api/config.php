<?php
// Configuracion de la base de datos
define('DB_HOST', '127.0.0.1');
define('DB_NAME', 'runner_app');
define('DB_USER', 'root');
define('DB_PASS', '');

// Modo debug (mostrar codigos de verificacion en respuesta)
define('DEBUG_MODE', true);

// Conexion a MySQL
function db(): PDO {
    static $pdo = null;
    if ($pdo === null) {
        $pdo = new PDO(
            "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
            DB_USER, DB_PASS,
            [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ]
        );
    }
    return $pdo;
}

// --- Helpers de respuesta ---

function success($data = null, int $code = 200, string $message = ''): void {
    http_response_code($code);
    echo json_encode(['success' => true, 'data' => $data, 'message' => $message]);
    exit;
}

function error(string $message, int $code = 400): void {
    http_response_code($code);
    echo json_encode(['success' => false, 'data' => null, 'message' => $message]);
    exit;
}

// --- Autenticacion ---

function generateToken(): string {
    return bin2hex(random_bytes(32));
}

function createToken(int $userId): string {
    $token = generateToken();
    $stmt = db()->prepare("INSERT INTO api_tokens (user_id, token) VALUES (?, ?)");
    $stmt->execute([$userId, $token]);
    return $token;
}

function deleteUserTokens(int $userId): void {
    db()->prepare("DELETE FROM api_tokens WHERE user_id = ?")->execute([$userId]);
}

function authUser(): array {
    // Apache a veces no pasa Authorization, intentar varias fuentes
    $header = $_SERVER['HTTP_AUTHORIZATION']
        ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION']
        ?? '';
    if (!$header && function_exists('apache_request_headers')) {
        $headers = apache_request_headers();
        $header = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    }
    if (!preg_match('/^Bearer\s+(.+)$/i', $header, $m)) {
        error('Token no proporcionado.', 401);
    }
    $token = $m[1];
    $stmt = db()->prepare("
        SELECT u.* FROM users u
        JOIN api_tokens t ON t.user_id = u.id
        WHERE t.token = ?
    ");
    $stmt->execute([$token]);
    $user = $stmt->fetch();
    if (!$user) error('Token invalido.', 401);

    unset($user['password'], $user['verification_code']);
    return $user;
}

// --- Input ---

function input(): array {
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (stripos($contentType, 'application/json') !== false) {
        return json_decode(file_get_contents('php://input'), true) ?? [];
    }
    return $_POST;
}

// --- Utilidades ---

function userPublic(array $user): array {
    unset($user['password'], $user['verification_code']);
    return $user;
}