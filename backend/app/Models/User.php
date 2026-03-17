<?php

namespace App\Models;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens;

    protected $fillable = [
        'name','email','password','country',
        'photo_url','fcm_token','email_verified','verification_code',
        'total_km','total_calories',
    ];

    protected $hidden = ['password','remember_token','verification_code'];

    protected $casts = ['email_verified' => 'boolean'];

    public function runs()
    {
        return $this->hasMany(Run::class);
    }

    public function friends()
    {
        return $this->hasMany(Friend::class);
    }
}
