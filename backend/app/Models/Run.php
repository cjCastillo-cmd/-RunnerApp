<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Run extends Model
{
    protected $fillable = [
        'user_id','distance_km','calories','duration_sec',
        'start_lat','start_lng','end_lat','end_lng',
        'route_json','avg_pace','photo_url',
    ];

    protected $casts = [
        'distance_km' => 'float',
        'avg_pace'    => 'float',
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function getFormattedDurationAttribute(): string
    {
        $h = intdiv($this->duration_sec, 3600);
        $m = intdiv($this->duration_sec % 3600, 60);
        $s = $this->duration_sec % 60;
        return sprintf('%02d:%02d:%02d', $h, $m, $s);
    }
}
