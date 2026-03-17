/* ═══════════════════════════════════════════
   Runner App — Vista/Controlador: Profile
   Perfil de usuario, edicion, foto, logros
   ═══════════════════════════════════════════ */

const ACHIEVEMENTS = [
    // Distancia
    { id: 'dist_1',    icon: '👟', title: 'Primer Kilometro',  desc: '1 km recorrido total',          type: 'km',   target: 1 },
    { id: 'dist_10',   icon: '🏃', title: '10K Runner',        desc: '10 km recorridos total',         type: 'km',   target: 10 },
    { id: 'dist_42',   icon: '🏅', title: 'Maratonista',       desc: '42.195 km recorridos total',     type: 'km',   target: 42.195 },
    { id: 'dist_100',  icon: '⚡', title: 'Ultra Runner',      desc: '100 km recorridos total',        type: 'km',   target: 100 },
    { id: 'dist_500',  icon: '🏆', title: 'Leyenda',           desc: '500 km recorridos total',        type: 'km',   target: 500 },
    // Carreras
    { id: 'runs_1',    icon: '🚀', title: 'Primera Carrera',   desc: 'Completa tu primera carrera',    type: 'runs', target: 1 },
    { id: 'runs_10',   icon: '🔥', title: 'Corredor Frecuente', desc: '10 carreras completadas',       type: 'runs', target: 10 },
    { id: 'runs_50',   icon: '💪', title: 'Dedicado',          desc: '50 carreras completadas',        type: 'runs', target: 50 },
    { id: 'runs_100',  icon: '🌟', title: 'Imparable',         desc: '100 carreras completadas',       type: 'runs', target: 100 },
    // Calorias
    { id: 'cal_1k',    icon: '🔥', title: 'Quema Calorias',    desc: '1,000 calorias quemadas total',  type: 'cal',  target: 1000 },
    { id: 'cal_10k',   icon: '🌋', title: 'Inferno',           desc: '10,000 calorias quemadas total', type: 'cal',  target: 10000 },
];

const ProfileView = {
    async load() {
        const r = await Api.getProfile();
        if (!r.success) return;

        const u = r.data;
        App.setUser(u);

        // Avatar
        const avatarEl = document.getElementById('pf-avatar');
        const letterEl = document.getElementById('pf-avatar-letter');
        if (u.photo_url) {
            letterEl.style.display = 'none';
            let img = avatarEl.querySelector('img');
            if (!img) {
                img = document.createElement('img');
                avatarEl.insertBefore(img, avatarEl.firstChild);
            }
            img.src = u.photo_url;
            img.alt = u.name;
        } else {
            letterEl.style.display = '';
            letterEl.textContent = (u.name || '?')[0];
            const img = avatarEl.querySelector('img');
            if (img) img.remove();
        }

        // Datos
        document.getElementById('pf-name').textContent = u.name.toUpperCase();
        document.getElementById('pf-country').textContent = u.country;
        animateCount(document.getElementById('pf-km'), parseFloat(u.total_km), 1);
        animateCount(document.getElementById('pf-cal'), parseInt(u.total_calories));
        document.getElementById('pf-name-in').value = u.name;
        document.getElementById('pf-country-in').value = u.country;

        // Carreras totales
        const runs = await Api.getRuns();
        let totalRuns = 0;
        if (runs.success) {
            totalRuns = runs.data.total || runs.data.data?.length || 0;
            animateCount(document.getElementById('pf-runs'), totalRuns);
        }

        // Renderizar logros
        this.renderAchievements(
            parseFloat(u.total_km) || 0,
            parseInt(u.total_calories) || 0,
            totalRuns
        );
    },

    renderAchievements(km, cal, runs) {
        const container = document.getElementById('achievements-list');
        if (!container) return;

        const values = { km, cal, runs };
        let unlocked = 0;

        let html = '';
        ACHIEVEMENTS.forEach((ach, i) => {
            const current = values[ach.type] || 0;
            const pct = Math.min(100, Math.round((current / ach.target) * 100));
            const done = pct >= 100;
            if (done) unlocked++;

            html += `
                <div class="achievement-item${done ? ' achievement-unlocked' : ''}" style="animation-delay:${i * 0.05}s">
                    <div class="achievement-icon">${ach.icon}</div>
                    <div class="achievement-info">
                        <div class="achievement-title">${ach.title}</div>
                        <div class="achievement-desc">${ach.desc}</div>
                        <div class="achievement-bar">
                            <div class="achievement-bar-fill" data-width="${pct}"></div>
                        </div>
                    </div>
                    <div class="achievement-pct">${pct}%</div>
                </div>`;
        });

        container.innerHTML = html;

        // Contador
        const counter = document.getElementById('ach-counter');
        if (counter) counter.textContent = `${unlocked}/${ACHIEVEMENTS.length} desbloqueados`;

        // Animar barras de progreso despues del render
        requestAnimationFrame(() => {
            container.querySelectorAll('.achievement-bar-fill').forEach(bar => {
                bar.style.width = bar.dataset.width + '%';
            });
        });
    },

    async update() {
        const r = await Api.updateProfile({
            name: document.getElementById('pf-name-in').value,
            country: document.getElementById('pf-country-in').value
        });
        if (r.success) {
            App.setUser(r.data);
            this.load();
            toast('Perfil actualizado!');
        } else {
            toast('Error al actualizar', 'error');
        }
    },

    async uploadPhoto(input) {
        const file = input.files[0];
        if (!file) return;
        if (file.size > 5 * 1024 * 1024) {
            toast('La imagen no debe superar 5MB', 'error');
            return;
        }

        const fd = new FormData();
        fd.append('photo', file);
        toast('Subiendo foto...', 'info');

        const r = await Api.uploadPhoto(fd);
        if (r.success) {
            toast('Foto actualizada!');
            this.load();
        } else {
            toast(r.message || 'Error al subir foto', 'error');
        }
        input.value = '';
    }
};
