<?php

namespace App\Http\Controllers;

use App\Models\Run;
use Carbon\Carbon;
use Illuminate\Http\Request;

class ChartController extends Controller
{
    // Km por dia de las ultimas 4 semanas
    public function weeklyHistory(Request $request)
    {
        $user = $request->user();
        $startDate = Carbon::now()->subWeeks(4)->startOfDay();

        $runs = Run::where('user_id', $user->id)
            ->where('created_at', '>=', $startDate)
            ->selectRaw('DATE(created_at) as date, SUM(distance_km) as km, COUNT(*) as runs')
            ->groupBy('date')
            ->orderBy('date')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $runs
        ]);
    }

    // Km por mes de los ultimos 6 meses
    public function monthlyHistory(Request $request)
    {
        $user = $request->user();
        $startDate = Carbon::now()->subMonths(6)->startOfMonth();

        $runs = Run::where('user_id', $user->id)
            ->where('created_at', '>=', $startDate)
            ->selectRaw("DATE_FORMAT(created_at, '%Y-%m') as month, SUM(distance_km) as km, COUNT(*) as runs, SUM(calories) as calories")
            ->groupBy('month')
            ->orderBy('month')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $runs
        ]);
    }
}
