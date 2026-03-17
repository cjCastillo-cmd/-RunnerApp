/* ═══════════════════════════════════════════
   Runner App — Vista/Controlador: Running
   Carrera activa con GPS + Mapa en vivo
   Resumen + Compartir + Detalle con mapa
   ═══════════════════════════════════════════ */

const RunningView = {
    interval: null,
    seconds: 0,
    distance: 0,
    lastMilestone: 0,
    detailMap: null,
    liveMap: null,
    watchId: null,
    routePoints: [],
    lastPos: null,
    livePolyline: null,
    liveMarker: null,

    start() {
        this.seconds = 0;
        this.distance = 0;
        this.lastMilestone = 0;
        this.routePoints = [];
        this.lastPos = null;
        App.showScreen('s-running', 'slide-in-right');
        document.getElementById('bnav').classList.remove('show');

        // Inicializar mapa despues de que la pantalla sea visible
        setTimeout(() => this.initLiveMap(), 400);

        // Timer cada segundo
        this.interval = setInterval(() => {
            this.seconds++;
            document.getElementById('run-timer').textContent = formatTime(this.seconds);

            const pace = this.distance > 0 ? (this.seconds / 60) / this.distance : 0;
            document.getElementById('run-pace').textContent = formatPace(pace);
        }, 1000);

        // GPS real con Geolocation API
        if (navigator.geolocation) {
            this.watchId = navigator.geolocation.watchPosition(
                pos => this.onGpsUpdate(pos),
                err => this.onGpsError(err),
                { enableHighAccuracy: true, maximumAge: 3000, timeout: 10000 }
            );
        } else {
            toast('GPS no disponible, usando simulacion', 'info');
            this.startSimulation();
        }
    },

    initLiveMap() {
        const mapEl = document.getElementById('run-live-map');

        if (this.liveMap) {
            this.liveMap.remove();
            this.liveMap = null;
        }

        this.liveMap = L.map(mapEl, {
            zoomControl: false,
            attributionControl: false,
            zoom: 17,
            center: [14.0818, -87.2068]
        });

        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            maxZoom: 19
        }).addTo(this.liveMap);

        // Polyline de la ruta en tiempo real
        this.livePolyline = L.polyline([], {
            color: '#c8ff00',
            weight: 4,
            opacity: 0.9
        }).addTo(this.liveMap);

        // Forzar recalculo de tamaño del mapa
        setTimeout(() => this.liveMap.invalidateSize(), 100);

        // Marcador de posicion actual
        const pulseIcon = L.divIcon({
            className: '',
            html: '<div style="width:16px;height:16px;background:#c8ff00;border-radius:50%;border:3px solid #fff;box-shadow:0 0 12px rgba(200,255,0,.6)"></div>',
            iconSize: [16, 16],
            iconAnchor: [8, 8]
        });
        this.liveMarker = L.marker([14.0818, -87.2068], { icon: pulseIcon }).addTo(this.liveMap);
    },

    onGpsUpdate(pos) {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        const point = [lat, lng];

        // Calcular distancia si hay punto anterior
        if (this.lastPos) {
            const d = this.haversine(this.lastPos[0], this.lastPos[1], lat, lng);
            // Filtrar saltos de GPS mayores a 100m (ruido)
            if (d < 0.1) {
                this.distance += d;
            }
        }

        this.lastPos = point;
        this.routePoints.push({ lat, lng, time: Date.now() });

        // Notificacion de kilometro completado
        const currentKm = Math.floor(this.distance);
        if (currentKm > this.lastMilestone) {
            this.lastMilestone = currentKm;
            toast(currentKm + ' KM completado!', 'success');
            if (navigator.vibrate) navigator.vibrate(200);
        }

        // Actualizar UI
        document.getElementById('run-km').textContent = this.distance.toFixed(2);
        document.getElementById('run-cal').textContent = Math.round(this.distance * 70);

        // Actualizar mapa
        if (this.liveMap) {
            this.livePolyline.addLatLng(point);
            this.liveMarker.setLatLng(point);
            this.liveMap.panTo(point, { animate: true, duration: 0.5 });

            // Centrar en primer punto
            if (this.routePoints.length === 1) {
                this.liveMap.setView(point, 17);
            }
        }
    },

    onGpsError(err) {
        console.warn('GPS error:', err.message);
        // Si no hay permisos o GPS no disponible, usar simulacion
        if (this.routePoints.length === 0) {
            toast('GPS no disponible, usando simulacion', 'info');
            this.startSimulation();
        }
    },

    // Simulacion de GPS para testing en escritorio
    startSimulation() {
        let simLat = 14.0818;
        let simLng = -87.2068;
        this.simInterval = setInterval(() => {
            simLat += (Math.random() - 0.3) * 0.0003;
            simLng += (Math.random() - 0.3) * 0.0003;
            this.onGpsUpdate({
                coords: { latitude: simLat, longitude: simLng, accuracy: 10 }
            });
        }, 2000);
    },

    // Formula Haversine para distancia en km
    haversine(lat1, lon1, lat2, lon2) {
        const R = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a = Math.sin(dLat / 2) ** 2 +
                  Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                  Math.sin(dLon / 2) ** 2;
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    },

    cleanupTracking() {
        clearInterval(this.interval);
        if (this.watchId !== null) {
            navigator.geolocation.clearWatch(this.watchId);
            this.watchId = null;
        }
        if (this.simInterval) {
            clearInterval(this.simInterval);
            this.simInterval = null;
        }
        if (this.liveMap) {
            this.liveMap.remove();
            this.liveMap = null;
        }
    },

    async stop() {
        this.cleanupTracking();

        const btn = document.getElementById('run-stop');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> GUARDANDO';

        // Preparar datos con ruta GPS
        const startPt = this.routePoints[0] || { lat: 14.0818, lng: -87.2068 };
        const endPt = this.routePoints[this.routePoints.length - 1] || startPt;

        await Api.saveRun({
            distance_km: +this.distance.toFixed(3),
            duration_sec: this.seconds,
            start_lat: startPt.lat,
            start_lng: startPt.lng,
            end_lat: endPt.lat,
            end_lng: endPt.lng,
            route_json: JSON.stringify(this.routePoints.map(p => ({ lat: p.lat, lng: p.lng })))
        });

        btn.disabled = false;
        btn.textContent = 'FINALIZAR';

        // Mostrar resumen
        const cal = Math.round(this.distance * 70);
        const pace = this.distance > 0 ? (this.seconds / 60) / this.distance : 0;

        document.getElementById('sum-km').textContent = this.distance.toFixed(2);
        document.getElementById('sum-time').textContent = formatTimeShort(this.seconds);
        document.getElementById('sum-cal').textContent = cal;
        document.getElementById('sum-pace').textContent = formatPace(pace);

        App.showScreen('s-summary', 'fade-in');

        // Animar numeros
        setTimeout(() => {
            animateCount(document.getElementById('sum-km'), this.distance, 2, 800);
            animateCount(document.getElementById('sum-cal'), cal, 0, 800);
        }, 500);
    },

    closeSummary() {
        document.getElementById('bnav').classList.add('show');
        toast('Carrera guardada!');
        App.nav('s-home');
    },

    // ─── Compartir carrera ───
    share() {
        const km = document.getElementById('sum-km').textContent;
        const time = document.getElementById('sum-time').textContent;
        const text = `Acabo de correr ${km} km en ${time} con Runner App!`;

        if (navigator.share) {
            navigator.share({ title: 'Runner App', text }).catch(() => {});
        } else {
            navigator.clipboard.writeText(text).then(() => {
                toast('Copiado al portapapeles!', 'info');
            }).catch(() => {
                prompt('Copia este texto:', text);
            });
        }
    },

    cancel() {
        this.cleanupTracking();
        document.getElementById('bnav').classList.add('show');
        App.nav('s-home');
    },

    // ─── Detalle de carrera con mapa ───
    openDetail(run) {
        document.getElementById('bnav').classList.remove('show');
        App.showScreen('s-run-detail', 'slide-in-right');

        // Stats
        document.getElementById('det-km').textContent = parseFloat(run.distance_km).toFixed(2);
        document.getElementById('det-time').textContent = formatTimeShort(run.duration_sec);
        document.getElementById('det-cal').textContent = run.calories;
        const pace = run.avg_pace || (run.duration_sec > 0 && run.distance_km > 0 ? (run.duration_sec / 60) / run.distance_km : 0);
        document.getElementById('det-pace').textContent = formatPace(pace);
        document.getElementById('det-date').textContent = new Date(run.created_at).toLocaleDateString('es', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

        // Boton eliminar
        const delBtn = document.getElementById('detail-delete-btn');
        delBtn.onclick = async () => {
            if (!confirm('Eliminar esta carrera?')) return;
            const r = await Api.deleteRun(run.id);
            if (r.success) {
                toast('Carrera eliminada');
                document.getElementById('bnav').classList.add('show');
                App.nav('s-home');
            }
        };

        // Inicializar mapa con Leaflet
        setTimeout(() => this.initMap(run), 100);
    },

    initMap(run) {
        const mapEl = document.getElementById('detail-map');

        if (this.detailMap) {
            this.detailMap.remove();
            this.detailMap = null;
        }

        const startLat = run.start_lat || 14.0818;
        const startLng = run.start_lng || -87.2068;
        const endLat = run.end_lat || startLat;
        const endLng = run.end_lng || startLng;

        this.detailMap = L.map(mapEl, {
            zoomControl: false,
            attributionControl: false
        });

        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            maxZoom: 19
        }).addTo(this.detailMap);

        setTimeout(() => this.detailMap.invalidateSize(), 100);

        let routePoints = [];

        if (run.route_json) {
            try {
                const parsed = typeof run.route_json === 'string' ? JSON.parse(run.route_json) : run.route_json;
                routePoints = parsed.map(p => [p.lat || p[0], p.lng || p[1]]);
            } catch (e) {}
        }

        // Si no hay ruta guardada, generar puntos entre start y end
        if (routePoints.length < 2) {
            const steps = 20;
            for (let i = 0; i <= steps; i++) {
                const t = i / steps;
                routePoints.push([
                    startLat + (endLat - startLat) * t + (Math.random() - 0.5) * 0.001,
                    startLng + (endLng - startLng) * t + (Math.random() - 0.5) * 0.001
                ]);
            }
        }

        const polyline = L.polyline(routePoints, {
            color: '#c8ff00',
            weight: 4,
            opacity: 0.9
        }).addTo(this.detailMap);

        const startIcon = L.divIcon({ className: '', html: '<div style="width:14px;height:14px;background:#30d158;border-radius:50%;border:3px solid #fff"></div>' });
        const endIcon = L.divIcon({ className: '', html: '<div style="width:14px;height:14px;background:#ff3b30;border-radius:50%;border:3px solid #fff"></div>' });

        L.marker(routePoints[0], { icon: startIcon }).addTo(this.detailMap);
        L.marker(routePoints[routePoints.length - 1], { icon: endIcon }).addTo(this.detailMap);

        this.detailMap.fitBounds(polyline.getBounds(), { padding: [40, 40] });
    }
};
