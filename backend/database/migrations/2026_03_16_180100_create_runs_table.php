<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        if (!Schema::hasTable('runs')) {
            Schema::create('runs', function (Blueprint $table) {
                $table->id();
                $table->foreignId('user_id')->constrained()->onDelete('cascade');
                $table->double('distance_km');
                $table->integer('calories');
                $table->integer('duration_sec');
                $table->double('start_lat');
                $table->double('start_lng');
                $table->double('end_lat');
                $table->double('end_lng');
                $table->double('avg_pace')->nullable();
                $table->longText('route_json')->nullable();
                $table->timestamps();

                $table->index('user_id');
                $table->index('created_at');
            });
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('runs');
    }
};