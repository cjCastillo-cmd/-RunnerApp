<?php

namespace App\Http\Controllers;

abstract class Controller
{
    protected function success($data, int $status = 200)
    {
        return response()->json(['success' => true, 'data' => $data], $status);
    }

    protected function error($message, int $status = 400)
    {
        return response()->json(['success' => false, 'message' => $message], $status);
    }
}
