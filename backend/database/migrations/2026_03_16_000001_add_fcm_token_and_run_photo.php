<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('fcm_token')->nullable()->after('photo_url');
        });

        Schema::table('runs', function (Blueprint $table) {
            $table->string('photo_url')->nullable()->after('route_json');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('fcm_token');
        });
        Schema::table('runs', function (Blueprint $table) {
            $table->dropColumn('photo_url');
        });
    }
};
