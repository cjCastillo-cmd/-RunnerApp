<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $v = Validator::make($request->all(), [
            'name'     => 'required|string|max:100',
            'email'    => 'required|email|unique:users,email',
            'password' => 'required|string|min:6|confirmed',
            'country'  => 'required|string|max:80',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $code = rand(100000, 999999);

        $user = User::create([
            'name'              => $request->name,
            'email'             => $request->email,
            'password'          => Hash::make($request->password),
            'country'           => $request->country,
            'verification_code' => (string) $code,
            'email_verified'    => 0,
        ]);

        // En local: el codigo se guarda en logs (storage/logs/laravel.log)
        $this->sendVerificationEmail($user->email, $user->name, $code);

        $response = [
            'user_id' => $user->id,
            'message' => 'Registro exitoso. Revisa tu correo para verificar tu cuenta.',
        ];
        if (config('app.debug')) $response['debug_code'] = $code;

        return $this->success($response, 201);
    }

    public function verifyEmail(Request $request)
    {
        $v = Validator::make($request->all(), [
            'email' => 'required|email',
            'code'  => 'required|string',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $user = User::where('email', $request->email)->first();
        if (!$user) return $this->error('Usuario no encontrado', 404);
        if ($user->email_verified) return $this->success(['message' => 'Cuenta ya verificada.']);
        if ($user->verification_code !== $request->code)
            return $this->error('Codigo de verificacion incorrecto.', 400);

        $user->update(['email_verified' => 1, 'verification_code' => null]);
        $token = $user->createToken('runner_app')->plainTextToken;

        return $this->success(['token' => $token, 'user' => $user]);
    }

    public function resendCode(Request $request)
    {
        $user = User::where('email', $request->email)->first();
        if (!$user) return $this->error('Usuario no encontrado', 404);

        $code = rand(100000, 999999);
        $user->update(['verification_code' => (string) $code]);
        $this->sendVerificationEmail($user->email, $user->name, $code);

        $response = ['message' => 'Codigo reenviado.'];
        if (config('app.debug')) $response['debug_code'] = $code;

        return $this->success($response);
    }

    public function login(Request $request)
    {
        $v = Validator::make($request->all(), [
            'email'    => 'required|email',
            'password' => 'required|string',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $user = User::where('email', $request->email)->first();

        if (!$user || !Hash::check($request->password, $user->password))
            return $this->error('Credenciales incorrectas.', 401);

        if (!$user->email_verified)
            return $this->error('Debes verificar tu correo electronico.', 403);

        $user->tokens()->delete();
        $token = $user->createToken('runner_app')->plainTextToken;

        return $this->success(['token' => $token, 'user' => $user]);
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();
        return $this->success(['message' => 'Sesion cerrada.']);
    }

    public function forgotPassword(Request $request)
    {
        $user = User::where('email', $request->email)->first();
        if (!$user) return $this->error('Correo no registrado.', 404);

        $token = Str::random(60);
        \DB::table('password_resets')->updateOrInsert(
            ['email' => $request->email],
            ['token' => Hash::make($token), 'created_at' => now()]
        );

        Mail::send('emails.reset_password', ['token' => $token, 'name' => $user->name],
            fn($m) => $m->to($user->email)->subject('Restablecer contrasena - Runner App'));

        $response = ['message' => 'Revisa tu correo para restablecer la contrasena.'];
        if (config('app.debug')) $response['debug_token'] = $token;

        return $this->success($response);
    }

    public function resetPassword(Request $request)
    {
        $v = Validator::make($request->all(), [
            'email'    => 'required|email',
            'token'    => 'required|string',
            'password' => 'required|string|min:6|confirmed',
        ]);
        if ($v->fails()) return $this->error($v->errors(), 422);

        $record = \DB::table('password_resets')
            ->where('email', $request->email)->first();

        if (!$record || !Hash::check($request->token, $record->token))
            return $this->error('Token invalido o expirado.', 400);

        User::where('email', $request->email)
            ->update(['password' => Hash::make($request->password)]);

        \DB::table('password_resets')->where('email', $request->email)->delete();

        return $this->success(['message' => 'Contrasena actualizada correctamente.']);
    }

    private function sendVerificationEmail(string $email, string $name, int $code): void
    {
        Mail::send('emails.verify', ['code' => $code, 'name' => $name],
            fn($m) => $m->to($email)->subject("$code es tu codigo Runner App"));
    }
}
