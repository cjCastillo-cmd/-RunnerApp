<?php

namespace App\Http\Controllers;

use App\Models\Friend;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class FriendController extends Controller
{
    public function sendRequest(Request $request)
    {
        $v = Validator::make($request->all(), [
            'friend_id' => 'required|integer|exists:users,id',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $friendId = $request->friend_id;
        if ($friendId == $request->user()->id)
            return $this->error('No puedes agregarte a ti mismo.', 400);

        $exists = Friend::where('user_id', $request->user()->id)
            ->where('friend_id', $friendId)->exists();
        if ($exists) return $this->error('Solicitud ya enviada.', 409);

        // Verificar si ya existe la relacion inversa
        $reverse = Friend::where('user_id', $friendId)
            ->where('friend_id', $request->user()->id)->exists();
        if ($reverse) return $this->error('Este usuario ya te envio una solicitud.', 409);

        $friend = Friend::create([
            'user_id'   => $request->user()->id,
            'friend_id' => $friendId,
            'status'    => 'pending',
        ]);
        return $this->success($friend, 201);
    }

    public function respond(Request $request)
    {
        $v = Validator::make($request->all(), [
            'request_id' => 'required|integer',
            'action'     => 'required|string|in:accepted,rejected',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $friendship = Friend::where('id', $request->request_id)
            ->where('friend_id', $request->user()->id)
            ->first();
        if (!$friendship) return $this->error('Solicitud no encontrada.', 404);

        $friendship->update(['status' => $request->action]);
        return $this->success($friendship);
    }

    public function index(Request $request)
    {
        $friends = Friend::with(['friend:id,name,country,photo_url,total_km'])
            ->where('user_id', $request->user()->id)
            ->where('status', 'accepted')
            ->get();
        return $this->success($friends);
    }

    public function pending(Request $request)
    {
        $pending = Friend::with(['user:id,name,photo_url'])
            ->where('friend_id', $request->user()->id)
            ->where('status', 'pending')
            ->get();
        return $this->success($pending);
    }

}
