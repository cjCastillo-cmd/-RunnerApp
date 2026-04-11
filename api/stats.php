<?php

function monthly(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT
            COUNT(*) AS total_runs,
            COALESCE(SUM(distance_km), 0) AS total_km,
            COALESCE(SUM(calories), 0) AS total_calories,
            COALESCE(AVG(avg_pace), 0) AS avg_pace
        FROM runs
        WHERE user_id = ? AND YEAR(created_at) = YEAR(NOW()) AND MONTH(created_at) = MONTH(NOW())
    ");
    $stmt->execute([$user['id']]);
    $data = $stmt->fetch();

    $data['total_km']       = round((float)$data['total_km'], 2);
    $data['total_calories'] = (int)$data['total_calories'];
    $data['total_runs']     = (int)$data['total_runs'];
    $data['avg_pace']       = round((float)$data['avg_pace'], 2);

    success($data);
}

function weeklyCompare(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT COALESCE(SUM(distance_km), 0) AS km
        FROM runs
        WHERE user_id = ? AND created_at >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
    ");
    $stmt->execute([$user['id']]);
    $current = (float)$stmt->fetch()['km'];

    $stmt = db()->prepare("
        SELECT COALESCE(SUM(distance_km), 0) AS km
        FROM runs
        WHERE user_id = ?
          AND created_at >= DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)
          AND created_at < DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
    ");
    $stmt->execute([$user['id']]);
    $previous = (float)$stmt->fetch()['km'];

    $diff = $current - $previous;

    success([
        'current_week_km'  => round($current, 2),
        'previous_week_km' => round($previous, 2),
        'difference_km'    => round($diff, 2),
        'percentage'       => $previous > 0 ? round(($diff / $previous) * 100, 1) : null,
    ]);
}

function monthlyCompare(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT COALESCE(SUM(distance_km), 0) AS km
        FROM runs
        WHERE user_id = ? AND YEAR(created_at) = YEAR(NOW()) AND MONTH(created_at) = MONTH(NOW())
    ");
    $stmt->execute([$user['id']]);
    $currentKm = (float)$stmt->fetch()['km'];

    $stmt = db()->prepare("
        SELECT COALESCE(SUM(distance_km), 0) AS km
        FROM runs
        WHERE user_id = ?
          AND YEAR(created_at) = YEAR(DATE_SUB(NOW(), INTERVAL 1 MONTH))
          AND MONTH(created_at) = MONTH(DATE_SUB(NOW(), INTERVAL 1 MONTH))
    ");
    $stmt->execute([$user['id']]);
    $previousKm = (float)$stmt->fetch()['km'];

    $diff = $currentKm - $previousKm;

    success([
        'current_month_km'  => round($currentKm, 2),
        'previous_month_km' => round($previousKm, 2),
        'difference_km'     => round($diff, 2),
        'percentage'        => $previousKm > 0 ? round(($diff / $previousKm) * 100, 1) : null,
    ]);
}

function leaderboard(): void {
    global $dynamicUser;
    $user = $dynamicUser;
    $userId = $user['id'];

    // Obtener IDs de amigos aceptados
    $stmt = db()->prepare("
        SELECT user_id, friend_id FROM friends
        WHERE (user_id = ? OR friend_id = ?) AND status = 'accepted'
    ");
    $stmt->execute([$userId, $userId]);
    $rows = $stmt->fetchAll();

    $ids = [$userId];
    foreach ($rows as $row) {
        $ids[] = (int)$row['user_id'];
        $ids[] = (int)$row['friend_id'];
    }
    $ids = array_unique($ids);

    $placeholders = implode(',', array_fill(0, count($ids), '?'));

    $stmt = db()->prepare("
        SELECT r.user_id, SUM(r.distance_km) AS km_this_month,
               u.name, u.photo_url, u.country
        FROM runs r
        JOIN users u ON u.id = r.user_id
        WHERE r.user_id IN ($placeholders)
          AND YEAR(r.created_at) = YEAR(NOW())
          AND MONTH(r.created_at) = MONTH(NOW())
        GROUP BY r.user_id
        ORDER BY km_this_month DESC
    ");
    $stmt->execute(array_values($ids));
    $results = $stmt->fetchAll();

    $board = [];
    foreach ($results as $i => $row) {
        $board[] = [
            'position'      => $i + 1,
            'user_id'       => (int)$row['user_id'],
            'name'          => $row['name'],
            'photo_url'     => $row['photo_url'],
            'country'       => $row['country'],
            'km_this_month' => round((float)$row['km_this_month'], 2),
            'is_me'         => (int)$row['user_id'] === $userId,
        ];
    }

    success($board);
}