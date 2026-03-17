<?php

namespace App\Http\Controllers;

use App\Models\Run;
use App\Models\Friend;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class StatsController extends Controller
{
    public function monthly(Request $request)
    {
        $userId = $request->user()->id;
        $now    = now();

        $data = Run::where('user_id', $userId)
            ->whereYear ('created_at', $now->year)
            ->whereMonth('created_at', $now->month)
            ->selectRaw('
                COUNT(*)               AS total_runs,
                COALESCE(SUM(distance_km),0) AS total_km,
                COALESCE(SUM(calories),0)    AS total_calories,
                COALESCE(AVG(avg_pace),0)    AS avg_pace
            ')
            ->first();

        return $this->success($data);
    }

    public function weeklyCompare(Request $request)
    {
        $userId = $request->user()->id;

        $thisWeekStart = now()->startOfWeek();
        $lastWeekStart = now()->subWeek()->startOfWeek();
        $lastWeekEnd   = now()->subWeek()->endOfWeek();

        $current = Run::where('user_id', $userId)
            ->where('created_at', '>=', $thisWeekStart)
            ->sum('distance_km');

        $previous = Run::where('user_id', $userId)
            ->whereBetween('created_at', [$lastWeekStart, $lastWeekEnd])
            ->sum('distance_km');

        $diff = $current - $previous;

        return $this->success([
            'current_week_km'  => round($current, 2),
            'previous_week_km' => round($previous, 2),
            'difference_km'    => round($diff, 2),
            'percentage'       => $previous > 0 ? round(($diff / $previous) * 100, 1) : null,
        ]);
    }

    public function monthlyCompare(Request $request)
    {
        $userId = $request->user()->id;
        $now    = now();

        $currentKm = Run::where('user_id', $userId)
            ->whereYear ('created_at', $now->year)
            ->whereMonth('created_at', $now->month)
            ->sum('distance_km');

        $prevDate   = $now->copy()->subMonth();
        $previousKm = Run::where('user_id', $userId)
            ->whereYear ('created_at', $prevDate->year)
            ->whereMonth('created_at', $prevDate->month)
            ->sum('distance_km');

        $diff = $currentKm - $previousKm;

        return $this->success([
            'current_month_km'  => round($currentKm, 2),
            'previous_month_km' => round($previousKm, 2),
            'difference_km'     => round($diff, 2),
            'percentage'        => $previousKm > 0 ? round(($diff / $previousKm) * 100, 1) : null,
        ]);
    }

    public function leaderboard(Request $request)
    {
        $userId    = $request->user()->id;
        $now       = now();

        $friendIds = Friend::where(function ($q) use ($userId) {
            $q->where('user_id', $userId)->orWhere('friend_id', $userId);
        })->where('status', 'accepted')
            ->pluck('user_id')
            ->merge(
                Friend::where(function ($q) use ($userId) {
                    $q->where('user_id', $userId)->orWhere('friend_id', $userId);
                })->where('status', 'accepted')->pluck('friend_id')
            )
            ->push($userId)
            ->unique()
            ->values()
            ->toArray();

        $board = Run::whereIn('user_id', $friendIds)
            ->whereYear ('created_at', $now->year)
            ->whereMonth('created_at', $now->month)
            ->select('user_id', DB::raw('SUM(distance_km) AS km_this_month'))
            ->groupBy('user_id')
            ->orderByDesc('km_this_month')
            ->with('user:id,name,photo_url,country')
            ->get()
            ->map(function ($item, $index) use ($userId) {
                return [
                    'position'      => $index + 1,
                    'user_id'       => $item->user_id,
                    'name'          => $item->user->name ?? 'N/A',
                    'photo_url'     => $item->user->photo_url ?? null,
                    'country'       => $item->user->country ?? '',
                    'km_this_month' => round($item->km_this_month, 2),
                    'is_me'         => $item->user_id === $userId,
                ];
            });

        return $this->success($board);
    }

}
