<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class PushNotificationService
{
    public static function send(string $fcmToken, string $title, string $body): void
    {
        if (empty($fcmToken)) return;

        try {
            $serverKey = config('services.firebase.server_key');
            if (empty($serverKey)) {
                Log::warning('Firebase server key no configurada');
                return;
            }

            Http::withHeaders([
                'Authorization' => 'key=' . $serverKey,
                'Content-Type' => 'application/json',
            ])->post('https://fcm.googleapis.com/fcm/send', [
                'to' => $fcmToken,
                'notification' => [
                    'title' => $title,
                    'body' => $body,
                ],
            ]);
        } catch (\Exception $e) {
            Log::error('Error enviando push notification: ' . $e->getMessage());
        }
    }
}
