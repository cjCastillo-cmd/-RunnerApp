<?php

namespace App\Http\Controllers;

use App\Models\Run;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class RunController extends Controller
{
    public function store(Request $request)
    {
        $v = Validator::make($request->all(), [
            'distance_km'  => 'required|numeric|min:0',
            'duration_sec' => 'required|integer|min:0',
            'start_lat'    => 'required|numeric',
            'start_lng'    => 'required|numeric',
            'end_lat'      => 'required|numeric',
            'end_lng'      => 'required|numeric',
            'route_json'   => 'nullable|string',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $km       = (float) $request->distance_km;
        $calories = (int) ($km * 70);
        $avgPace  = $km > 0 ? round(($request->duration_sec / 60) / $km, 2) : null;

        $run = Run::create([
            'user_id'      => $request->user()->id,
            'distance_km'  => $km,
            'calories'     => $calories,
            'duration_sec' => $request->duration_sec,
            'start_lat'    => $request->start_lat,
            'start_lng'    => $request->start_lng,
            'end_lat'      => $request->end_lat,
            'end_lng'      => $request->end_lng,
            'route_json'   => $request->route_json,
            'avg_pace'     => $avgPace,
        ]);

        $request->user()->increment('total_km', $km);
        $request->user()->increment('total_calories', $calories);

        return $this->success($run, 201);
    }

    public function index(Request $request)
    {
        $runs = Run::where('user_id', $request->user()->id)
            ->orderBy('created_at', 'desc')
            ->limit(50)
            ->get();

        return $this->success($runs);
    }

    public function show(Request $request, int $id)
    {
        $run = Run::where('id', $id)
            ->where('user_id', $request->user()->id)
            ->first();

        if (!$run) return $this->error('Carrera no encontrada.', 404);
        return $this->success($run);
    }

    public function destroy(Request $request, int $id)
    {
        $run = Run::where('id', $id)
            ->where('user_id', $request->user()->id)
            ->first();

        if (!$run) return $this->error('Carrera no encontrada.', 404);

        $request->user()->decrement('total_km', $run->distance_km);
        $request->user()->decrement('total_calories', $run->calories);

        $run->delete();
        return $this->success(['message' => 'Carrera eliminada.']);
    }

    public function uploadPhoto(Request $request, int $id)
    {
        $run = Run::where('id', $id)
            ->where('user_id', $request->user()->id)
            ->first();

        if (!$run) return $this->error('Carrera no encontrada.', 404);

        $request->validate(['photo' => 'required|image|max:5120']);
        $path = $request->file('photo')->store('run_photos', 'public');
        $run->update(['photo_url' => Storage::url($path)]);

        return $this->success(['photo_url' => $run->photo_url]);
    }

}
