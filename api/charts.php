<?php

function weeklyHistory(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT DATE(created_at) AS date, SUM(distance_km) AS km, COUNT(*) AS runs
        FROM runs
        WHERE user_id = ? AND created_at >= DATE_SUB(CURDATE(), INTERVAL 28 DAY)
        GROUP BY DATE(created_at)
        ORDER BY date
    ");
    $stmt->execute([$user['id']]);
    success($stmt->fetchAll());
}

function monthlyHistory(): void {
    global $dynamicUser;
    $user = $dynamicUser;

    $stmt = db()->prepare("
        SELECT DATE_FORMAT(created_at, '%Y-%m') AS month,
               SUM(distance_km) AS km,
               COUNT(*) AS runs,
               SUM(calories) AS calories
        FROM runs
        WHERE user_id = ? AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
        GROUP BY month
        ORDER BY month
    ");
    $stmt->execute([$user['id']]);
    success($stmt->fetchAll());
}