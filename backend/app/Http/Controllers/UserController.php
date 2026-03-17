<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\Friend;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class UserController extends Controller
{
    public function profile(Request $request)
    {
        return $this->success($request->user());
    }

    public function update(Request $request)
    {
        $v = Validator::make($request->all(), [
            'name'    => 'sometimes|string|max:100',
            'country' => 'sometimes|string|max:80',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $request->user()->update($request->only(['name', 'country']));
        return $this->success($request->user()->fresh());
    }

    public function uploadPhoto(Request $request)
    {
        $v = Validator::make($request->all(), [
            'photo' => 'required|image|mimes:jpg,jpeg,png|max:3072',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $path = $request->file('photo')->store("photos/{$request->user()->id}", 'public');
        $url  = Storage::url($path);
        $request->user()->update(['photo_url' => $url]);

        return $this->success(['photo_url' => $url]);
    }

    public function byCountry(Request $request)
    {
        $me      = $request->user();
        $myFriends = Friend::where('user_id', $me->id)
            ->pluck('friend_id')
            ->push($me->id);

        $users = User::where('country', $me->country)
            ->whereNotIn('id', $myFriends)
            ->where('email_verified', 1)
            ->select('id','name','country','photo_url','total_km')
            ->orderByDesc('total_km')
            ->limit(50)
            ->get();

        return $this->success($users);
    }

    public function updateFcmToken(Request $request)
    {
        $request->validate(['fcm_token' => 'required|string']);
        $request->user()->update(['fcm_token' => $request->fcm_token]);
        return $this->success(null, 'Token actualizado');
    }

    public function deleteAccount(Request $request)
    {
        $user = $request->user();
        $user->runs()->delete();
        $user->tokens()->delete();
        Friend::where('user_id', $user->id)->orWhere('friend_id', $user->id)->delete();
        $user->delete();
        return $this->success(null, 'Cuenta eliminada');
    }

}
