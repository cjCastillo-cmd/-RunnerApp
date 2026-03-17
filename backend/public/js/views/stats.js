/* ═══════════════════════════════════════════
   Runner App — Vista/Controlador: Stats
   Estadisticas con graficos animados Canvas
   ═══════════════════════════════════════════ */

const StatsView = {

    // Nombres cortos de dias y meses
    DAYS: ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab', 'Dom'],
    MONTHS_SHORT: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],

    // Colores del tema
    VOLT: '#c8ff00',
    VOLT_20: 'rgba(200,255,0,0.20)',
    VOLT_40: 'rgba(200,255,0,0.40)',
    SURFACE: '#141414',
    BORDER: '#2a2a2a',
    TEXT_2: '#999999',
    TEXT_3: '#555555',

    // ── Entrada principal ──
    async load() {
        const content = document.getElementById('stats-content');
        // Mostrar skeletons mientras carga
        document.querySelectorAll('#stats-content .compare-block, #stats-content .chart-block').forEach(el => el.style.display = 'none');

        const [mo, wk, mc] = await Promise.all([
            Api.getMonthlyStats(),
            Api.getWeeklyCompare(),
            Api.getMonthlyCompare()
        ]);

        // 1) Resumen mensual
        if (mo.success) {
            const d = mo.data;
            const el = document.getElementById('stats-summary');
            document.getElementById('stats-summary-grid').innerHTML = `
                <div class="cg-item"><div class="cg-val" style="color:var(--volt)">${parseFloat(d.total_km).toFixed(1)}</div><div class="cg-sub">Kilometros</div></div>
                <div class="cg-item"><div class="cg-val" style="color:var(--orange)">${d.total_calories}</div><div class="cg-sub">Calorias</div></div>
                <div class="cg-item"><div class="cg-val">${d.total_runs}</div><div class="cg-sub">Carreras</div></div>
                <div class="cg-item"><div class="cg-val" style="color:var(--blue)">${parseFloat(d.avg_pace).toFixed(1)}</div><div class="cg-sub">Ritmo Prom</div></div>`;
            el.style.display = '';
        }

        // 2) Grafico de barras semanal + comparacion
        if (wk.success) {
            const w = wk.data;
            const weekData = this._buildWeekData(w.current_week || []);

            const block = document.getElementById('chart-weekly-block');
            block.style.display = '';
            // Esperar un frame para que el canvas tenga layout
            requestAnimationFrame(() => this._drawBarChart('chart-weekly', weekData));

            // Comparacion semanal
            const cmpEl = document.getElementById('stats-weekly-cmp');
            document.getElementById('stats-weekly-grid').innerHTML = `
                <div class="cg-item"><div class="cg-val">${w.current_week_km}</div><div class="cg-sub">Esta semana</div></div>
                <div class="cg-item"><div class="cg-val" style="color:var(--text-3)">${w.previous_week_km}</div><div class="cg-sub">Anterior</div></div>
                <div class="cg-diff"><span class="label">Diferencia</span><span class="val ${w.difference_km >= 0 ? 'up' : 'down'}">${w.difference_km >= 0 ? '+' : ''}${w.difference_km} km</span></div>`;
            cmpEl.style.display = '';
        }

        // 3) Grafico de linea mensual + comparacion
        if (mc.success) {
            const m = mc.data;
            const monthData = this._buildMonthData(m.previous_months || [], m.current_month_km);

            const block = document.getElementById('chart-monthly-block');
            block.style.display = '';
            requestAnimationFrame(() => this._drawLineChart('chart-monthly', monthData));

            // Comparacion mensual
            const cmpEl = document.getElementById('stats-monthly-cmp');
            document.getElementById('stats-monthly-grid').innerHTML = `
                <div class="cg-item"><div class="cg-val">${m.current_month_km}</div><div class="cg-sub">Este mes</div></div>
                <div class="cg-item"><div class="cg-val" style="color:var(--text-3)">${m.previous_month_km}</div><div class="cg-sub">Anterior</div></div>
                <div class="cg-diff"><span class="label">Diferencia</span><span class="val ${m.difference_km >= 0 ? 'up' : 'down'}">${m.difference_km >= 0 ? '+' : ''}${m.difference_km} km</span></div>`;
            cmpEl.style.display = '';
        }
    },

    // ── Preparar datos de la semana (7 dias, lun-dom) ──
    _buildWeekData(currentWeek) {
        const data = new Array(7).fill(0);
        currentWeek.forEach(item => {
            const d = new Date(item.date);
            // getDay: 0=dom, queremos 0=lun
            let idx = d.getDay() - 1;
            if (idx < 0) idx = 6;
            data[idx] = parseFloat(item.km) || 0;
        });
        return data;
    },

    // ── Preparar datos mensuales ──
    _buildMonthData(previousMonths, currentKm) {
        // previous_months viene como [{month:'2026-01', km:12.3}, ...]
        const points = [];
        previousMonths.forEach(item => {
            const parts = (item.month || '').split('-');
            const mIdx = parts.length >= 2 ? parseInt(parts[1], 10) - 1 : 0;
            points.push({ label: this.MONTHS_SHORT[mIdx] || '?', km: parseFloat(item.km) || 0 });
        });
        // Agregar mes actual
        const now = new Date();
        points.push({ label: this.MONTHS_SHORT[now.getMonth()], km: parseFloat(currentKm) || 0 });
        return points;
    },

    // ════════════════════════════════════════════
    //  GRAFICO DE BARRAS (semanal)
    // ════════════════════════════════════════════
    _drawBarChart(canvasId, values) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        const dpr = window.devicePixelRatio || 1;
        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        const ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);

        const W = rect.width;
        const H = rect.height;
        const padTop = 28;
        const padBot = 32;
        const padX = 12;
        const barGap = 10;
        const barCount = 7;
        const chartH = H - padTop - padBot;
        const barW = (W - padX * 2 - barGap * (barCount - 1)) / barCount;
        const maxVal = Math.max(...values, 1);

        const DURATION = 600;
        const start = performance.now();

        const animate = (now) => {
            const elapsed = now - start;
            const progress = Math.min(elapsed / DURATION, 1);
            // Ease out cubic
            const ease = 1 - Math.pow(1 - progress, 3);

            ctx.clearRect(0, 0, W, H);

            for (let i = 0; i < barCount; i++) {
                const x = padX + i * (barW + barGap);
                const fullH = (values[i] / maxVal) * chartH;
                const h = fullH * ease;
                const y = padTop + chartH - h;

                // Barra con degradado
                const grad = ctx.createLinearGradient(x, y, x, padTop + chartH);
                grad.addColorStop(0, this.VOLT);
                grad.addColorStop(1, this.VOLT_40);
                ctx.fillStyle = grad;
                // Borde redondeado arriba
                this._roundRect(ctx, x, y, barW, h, 4);

                // Valor sobre la barra
                if (progress > 0.5 && values[i] > 0) {
                    const alpha = Math.min((progress - 0.5) * 2, 1);
                    ctx.fillStyle = `rgba(255,255,255,${alpha})`;
                    ctx.font = `700 11px 'Inter', sans-serif`;
                    ctx.textAlign = 'center';
                    ctx.fillText(values[i].toFixed(1), x + barW / 2, y - 8);
                }

                // Label del dia
                ctx.fillStyle = this.TEXT_2;
                ctx.font = `600 10px 'Inter', sans-serif`;
                ctx.textAlign = 'center';
                ctx.fillText(this.DAYS[i], x + barW / 2, H - 10);
            }

            // Linea base
            ctx.strokeStyle = this.BORDER;
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(padX, padTop + chartH);
            ctx.lineTo(W - padX, padTop + chartH);
            ctx.stroke();

            if (progress < 1) requestAnimationFrame(animate);
        };

        requestAnimationFrame(animate);
    },

    // Rectangulo con esquinas redondeadas arriba
    _roundRect(ctx, x, y, w, h, r) {
        if (h < 1) return;
        r = Math.min(r, h / 2, w / 2);
        ctx.beginPath();
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + w - r, y);
        ctx.quadraticCurveTo(x + w, y, x + w, y + r);
        ctx.lineTo(x + w, y + h);
        ctx.lineTo(x, y + h);
        ctx.lineTo(x, y + r);
        ctx.quadraticCurveTo(x, y, x + r, y);
        ctx.closePath();
        ctx.fill();
    },

    // ════════════════════════════════════════════
    //  GRAFICO DE LINEA (mensual, bezier + fill)
    // ════════════════════════════════════════════
    _drawLineChart(canvasId, points) {
        const canvas = document.getElementById(canvasId);
        if (!canvas || points.length < 2) return;
        const dpr = window.devicePixelRatio || 1;
        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        const ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);

        const W = rect.width;
        const H = rect.height;
        const padTop = 28;
        const padBot = 36;
        const padX = 32;
        const chartH = H - padTop - padBot;
        const chartW = W - padX * 2;
        const n = points.length;
        const maxVal = Math.max(...points.map(p => p.km), 1);

        // Coordenadas de cada punto
        const coords = points.map((p, i) => ({
            x: padX + (i / (n - 1)) * chartW,
            y: padTop + chartH - (p.km / maxVal) * chartH
        }));

        const DURATION = 800;
        const start = performance.now();

        const animate = (now) => {
            const elapsed = now - start;
            const progress = Math.min(elapsed / DURATION, 1);
            // Ease out quart
            const ease = 1 - Math.pow(1 - progress, 4);

            ctx.clearRect(0, 0, W, H);

            // Lineas guia horizontales
            ctx.strokeStyle = this.BORDER;
            ctx.lineWidth = 0.5;
            for (let i = 0; i <= 4; i++) {
                const gy = padTop + (chartH / 4) * i;
                ctx.beginPath();
                ctx.moveTo(padX, gy);
                ctx.lineTo(W - padX, gy);
                ctx.stroke();
            }

            // Determinar cuantos segmentos dibujar segun progreso
            const totalLen = n - 1;
            const drawLen = ease * totalLen;
            const fullSegments = Math.floor(drawLen);
            const partialT = drawLen - fullSegments;

            // Construir puntos visibles (con interpolacion parcial del ultimo segmento)
            const visible = [];
            for (let i = 0; i <= fullSegments && i < n; i++) {
                visible.push(coords[i]);
            }
            if (fullSegments < totalLen && partialT > 0) {
                const a = coords[fullSegments];
                const b = coords[fullSegments + 1];
                visible.push({
                    x: a.x + (b.x - a.x) * partialT,
                    y: a.y + (b.y - a.y) * partialT
                });
            }

            if (visible.length >= 2) {
                // Dibujar curva bezier
                ctx.beginPath();
                ctx.moveTo(visible[0].x, visible[0].y);
                for (let i = 0; i < visible.length - 1; i++) {
                    const p0 = visible[Math.max(i - 1, 0)];
                    const p1 = visible[i];
                    const p2 = visible[i + 1];
                    const p3 = visible[Math.min(i + 2, visible.length - 1)];
                    // Catmull-Rom -> cubic bezier
                    const tension = 0.3;
                    const cp1x = p1.x + (p2.x - p0.x) * tension;
                    const cp1y = p1.y + (p2.y - p0.y) * tension;
                    const cp2x = p2.x - (p3.x - p1.x) * tension;
                    const cp2y = p2.y - (p3.y - p1.y) * tension;
                    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y);
                }

                // Fill bajo la curva
                const fillPath = new Path2D();
                fillPath.moveTo(visible[0].x, visible[0].y);
                for (let i = 0; i < visible.length - 1; i++) {
                    const p0 = visible[Math.max(i - 1, 0)];
                    const p1 = visible[i];
                    const p2 = visible[i + 1];
                    const p3 = visible[Math.min(i + 2, visible.length - 1)];
                    const tension = 0.3;
                    fillPath.bezierCurveTo(
                        p1.x + (p2.x - p0.x) * tension, p1.y + (p2.y - p0.y) * tension,
                        p2.x - (p3.x - p1.x) * tension, p2.y - (p3.y - p1.y) * tension,
                        p2.x, p2.y
                    );
                }
                const lastV = visible[visible.length - 1];
                fillPath.lineTo(lastV.x, padTop + chartH);
                fillPath.lineTo(visible[0].x, padTop + chartH);
                fillPath.closePath();

                const grad = ctx.createLinearGradient(0, padTop, 0, padTop + chartH);
                grad.addColorStop(0, this.VOLT_20);
                grad.addColorStop(1, 'rgba(200,255,0,0.02)');
                ctx.fillStyle = grad;
                ctx.fill(fillPath);

                // Stroke de la curva
                ctx.strokeStyle = this.VOLT;
                ctx.lineWidth = 2.5;
                ctx.lineJoin = 'round';
                ctx.lineCap = 'round';
                ctx.stroke();
            }

            // Puntos y valores (aparecen despues del 40% de progreso)
            if (progress > 0.4) {
                const dotAlpha = Math.min((progress - 0.4) / 0.3, 1);
                const showCount = Math.min(Math.ceil(ease * n), n);
                for (let i = 0; i < showCount; i++) {
                    const c = coords[i];
                    // Punto
                    ctx.beginPath();
                    ctx.arc(c.x, c.y, 4, 0, Math.PI * 2);
                    ctx.fillStyle = this.VOLT;
                    ctx.globalAlpha = dotAlpha;
                    ctx.fill();
                    ctx.globalAlpha = 1;

                    // Valor encima
                    ctx.fillStyle = `rgba(255,255,255,${dotAlpha})`;
                    ctx.font = `700 10px 'Inter', sans-serif`;
                    ctx.textAlign = 'center';
                    ctx.fillText(points[i].km.toFixed(1), c.x, c.y - 12);
                }
            }

            // Labels de meses en el eje X
            ctx.fillStyle = this.TEXT_2;
            ctx.font = `600 10px 'Inter', sans-serif`;
            ctx.textAlign = 'center';
            ctx.globalAlpha = 1;
            for (let i = 0; i < n; i++) {
                ctx.fillText(points[i].label, coords[i].x, H - 10);
            }

            if (progress < 1) requestAnimationFrame(animate);
        };

        requestAnimationFrame(animate);
    }
};
