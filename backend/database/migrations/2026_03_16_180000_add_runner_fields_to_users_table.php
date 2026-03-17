<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            if (!Schema::hasColumn('users', 'country')) {
                $table->string('country', 80)->nullable()->after('password');
            }
            if (!Schema::hasColumn('users', 'photo_url')) {
                $table->string('photo_url')->nullable()->after('country');
            }
            if (!Schema::hasColumn('users', 'email_verified')) {
                $table->boolean('email_verified')->default(false)->after('photo_url');
            }
            if (!Schema::hasColumn('users', 'verification_code')) {
                $table->string('verification_code')->nullable()->after('email_verified');
            }
            if (!Schema::hasColumn('users', 'total_km')) {
                $table->double('total_km')->default(0)->after('verification_code');
            }
            if (!Schema::hasColumn('users', 'total_calories')) {
                $table->integer('total_calories')->default(0)->after('total_km');
            }
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['country', 'photo_url', 'email_verified', 'verification_code', 'total_km', 'total_calories']);
        });
    }
};
